/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.nurc.sem.lscv08;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.metocs.base.METOCSBaseConfiguratorAction;
import it.geosolutions.geobatch.metocs.jaxb.model.MetocElementType;
import it.geosolutions.geobatch.metocs.utils.io.METOCSActionsIOUtils;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to transform lscv08::MERCATOR Model
 * 
 */
public class MERCATORFileConfiguratorAction extends METOCSBaseConfiguratorAction {

    private Attribute referenceTime;

    private Attribute forecastDate;

    protected MERCATORFileConfiguratorAction(MetocActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    @Override
    protected File unzipMetocArchive(FileSystemEvent event, String fileSuffix, File outDir,
            File tempFile) throws IOException {
        return ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix)) ? Utilities
                .decompress("MERCATOR", event.getSource(), tempFile)
                : Utilities.createTodayPrefixedDirectory("MERCATOR", outDir);
    }

    @Override
    protected void writeDownNetCDF(File outDir, String inputFileName) throws IOException,
            InvalidRangeException, JAXBException, ParseException {
        // input dimensions
        final Dimension lon_dim = ncGridFile.findDimension("longitude");

        final Dimension lat_dim = ncGridFile.findDimension("latitude");

        final Dimension depth_dim = ncGridFile.findDimension("depth");

        // input VARIABLES
        final Variable lonOriginalVar = ncGridFile.findVariable("longitude");

        final DataType lonDataType = lonOriginalVar.getDataType();

        final Variable latOriginalVar = ncGridFile.findVariable("latitude");

        final DataType latDataType = latOriginalVar.getDataType();

        final Variable depthOriginalVar = ncGridFile.findVariable("depth");
        @SuppressWarnings("unused")
        final DataType depthDataType = depthOriginalVar.getDataType();

        final Array lonOriginalData = lonOriginalVar.read();
        final Array latOriginalData = latOriginalVar.read();
        final Array depthOriginalData = depthOriginalVar.read();

        // building envelope
        buildEnvelope(lon_dim, lat_dim, lonOriginalData, latOriginalData);

        // ////
        // ... create the output file data structure
        // ////
        createOutputFile(outDir, inputFileName);

        // copying NetCDF input file global attributes
        // copyNCGlobalAttrs();

        // Grabbing the Variables Dictionary
        getMetocsDictionary();

        // finding specific model variables
        fillVariablesMaps();

        // defining the file header and structure
        double noData = definingOutputVariables(true, lat_dim.getLength(), lon_dim.getLength(), 1,
                depth_dim.getLength(), METOCSActionsIOUtils.DOWN);

        // MERCATOR OCEAN MODEL Global Attributes
        referenceTime = ncGridFile.findGlobalAttributeIgnoreCase("bulletin_date");
        forecastDate = ncGridFile.findGlobalAttributeIgnoreCase("forecast_range");

        // time normalization and model TAU
        final SimpleDateFormat toSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        toSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        final Date timeOriginDate = toSdf
                .parse(referenceTime.getStringValue().trim().toLowerCase());

        int TAU = normalizingTimes(null, null, timeOriginDate);

        // Setting up global Attributes ...
        settingNCGlobalAttributes(noData, timeOriginDate, TAU);

        // writing bin data ...
        writingDataSets(lon_dim, lat_dim, depth_dim, null, true, lonOriginalData, latOriginalData,
                depthOriginalData, noData, null, latDataType, lonDataType);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Utility and conversion specific methods implementations...
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return
     * @throws ParseException
     * @throws NumberFormatException
     */
    protected int normalizingTimes(final Array timeOriginalData, final Dimension timeDim,
            final Date timeOriginDate) throws ParseException, NumberFormatException {
        final String forecastDays = forecastDate.getStringValue();
        int TAU = 0;
        if (forecastDays != null) {
            final int index = forecastDays.indexOf("-day_forecast");
            if (index != -1) {
                int numDay = Integer.parseInt(forecastDays.substring(0, index));
                TAU = numDay * 24;
            }
        }

        return TAU;
    }

    /**
     * @throws UnsupportedEncodingException
     */
    protected void fillVariablesMaps() throws UnsupportedEncodingException {
        for (Object obj : ncGridFile.getVariables()) {
            final Variable var = (Variable) obj;
            final String varName = var.getName();
            if (!varName.equalsIgnoreCase("longitude") && !varName.equalsIgnoreCase("latitude")
                    && !varName.equalsIgnoreCase("depth")) {

                if (foundVariables.get(varName) == null) {
                    String longName = null;
                    String briefName = null;
                    String uom = null;

                    for (MetocElementType m : metocDictionary.getMetoc()) {
                        if ((varName.equalsIgnoreCase("salinity") && m.getName().equals("salinity"))
                                || (varName.equalsIgnoreCase("temperature") && m.getName().equals(
                                        "water temperature"))
                                || (varName.equalsIgnoreCase("u") && m.getName().equals(
                                        "water velocity u-component"))
                                || (varName.equalsIgnoreCase("v") && m.getName().equals(
                                        "water velocity v-component"))) {
                            longName = m.getName();
                            briefName = m.getBrief();
                            uom = m.getDefaultUom();
                            uom = uom.indexOf(":") > 0 ? URLDecoder.decode(uom.substring(uom
                                    .lastIndexOf(":") + 1), "UTF-8") : uom;
                            break;
                        }
                    }

                    if (longName != null && briefName != null) {
                        foundVariables.put(varName, var);
                        foundVariableLongNames.put(varName, longName);
                        foundVariableBriefNames.put(varName, briefName);
                        foundVariableUoM.put(varName, uom);
                    }
                }
            }
        }
    }

    /**
     * 
     * @param ncFileOut
     * @param referenceTime
     * @param forecastDate
     * @return
     */
    private static void setTime(NetcdfFileWriteable ncFileOut, final Attribute referenceTime,
            final Attribute forecastDate) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // sdf.setDateFormatSymbols(new DateFormatSymbols(Locale.CANADA));
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        long millisFromStartDate = 0;
        if (referenceTime != null && forecastDate != null) {
            final String timeOrigin = referenceTime.getStringValue();
            final String forecastDays = forecastDate.getStringValue();

            Date startDate = null;
            try {
                startDate = sdf.parse(timeOrigin);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Unable to parse time origin");
            }

            if (forecastDays != null) {
                final int index = forecastDays.indexOf("-day_forecast");
                if (index != -1) {
                    int numDay = Integer.parseInt(forecastDays.substring(0, index));

                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                    calendar.setTime(startDate);
                    calendar.add(GregorianCalendar.DATE, numDay);

                    millisFromStartDate = calendar.getTimeInMillis() - startTime;
                }
            } else
                throw new IllegalArgumentException("Unable to find forecast day");
        }

        // writing time variable data
        ArrayFloat timeData = new ArrayFloat(new int[] { 1 });
        Index timeIndex = timeData.getIndex();
        timeData.setFloat(timeIndex.set(0), millisFromStartDate / 1000.0f);
        try {
            ncFileOut.write(METOCSActionsIOUtils.TIME_DIM, timeData);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidRangeException e) {
            throw new IllegalArgumentException(
                    "Unable to store time data to the output NetCDF file.");
        }
    }

    @Override
    protected void createOutputFile(File outDir, String inputFileName) throws IOException {
//        outputFile = new File(outDir, "lscv08_MERCATOR-Forecast-T" + new Date().getTime()
        outputFile = new File(outDir, cruiseName + "_MERCATOR-Forecast-T" + new Date().getTime()
                + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
        ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());
    }

    @Override
    protected double definingOutputVariables(boolean hasDepth, int nLat, int nLon, int nTimes,
            int nDepths, String depthName) {
        final List<Dimension> outDimensions = METOCSActionsIOUtils
                .createNetCDFCFGeodeticDimensions(ncFileOut, true, 1, hasDepth, nDepths, depthName,
                        true, nLat, true, nLon);

        double noData = Double.NaN;

        // defining output variable
        for (String varName : foundVariables.keySet()) {
            // SIMONE: replaced foundVariables.get(varName).getDataType()
            // with DataType.DOUBLE
            ncFileOut.addVariable(foundVariableBriefNames.get(varName), foundVariables.get(varName)
                    .getDataType(), outDimensions);
            // NetCDFConverterUtilities.setVariableAttributes(foundVariables.get(varName),
            // ncFileOut, foundVariableBriefNames.get(varName), new String[]
            // { "positions" });
            ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "long_name",
                    foundVariableLongNames.get(varName));
            ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "units",
                    foundVariableUoM.get(varName));

            Attribute missingValue = foundVariables.get(varName).findAttribute("_FillValue");
            if (missingValue != null) {
                double nD = missingValue.getNumericValue().doubleValue();
                ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "missing_value", nD);
            }
            if (Double.isNaN(noData)) {
                if (missingValue != null) {
                    noData = missingValue.getNumericValue().doubleValue();
                }
            }
        }

        return noData;
    }

    @Override
    protected void writingDataSets(Dimension lonDim, Dimension latDim, Dimension depthDim,
            Dimension timeDim, boolean hasDepth, Array lonOriginalData, Array latOriginalData,
            Array depthOriginalData, double noData, Array timeOriginalData, DataType latDataType,
            DataType lonDataType) throws IOException, InvalidRangeException {
        ncFileOut.create();

        // writing time Variable data
        setTime(ncFileOut, referenceTime, forecastDate);

        // writing depth Variable data
        ncFileOut.write(METOCSActionsIOUtils.DEPTH_DIM, depthOriginalData);

        // writing lat Variable data
        ncFileOut.write(METOCSActionsIOUtils.LAT_DIM, latOriginalData);

        // writing lon Variable data
        ncFileOut.write(METOCSActionsIOUtils.LON_DIM, lonOriginalData);

        for (String varName : foundVariables.keySet()) {
            final Variable var = foundVariables.get(varName);

            // //
            // defining the SampleModel data type
            // //
            final SampleModel outSampleModel = Utilities.getSampleModel(var.getDataType(), lonDim
                    .getLength(), latDim.getLength(), 1);

            Array originalVarArray = var.read();

            for (int z = 0; z < depthDim.getLength(); z++) {

                WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

                METOCSActionsIOUtils.write2DData(userRaster, var, originalVarArray, false, false,
                        new int[] { z, latDim.getLength(), lonDim.getLength() }, false);

                // Resampling to a Regular Grid ...
                // if (LOGGER.isLoggable(Level.INFO))
                // LOGGER.info("Resampling to a Regular Grid ...");
                // userRaster = METOCSActionsIOUtils.warping(
                // bbox,
                // lonOriginalData,
                // latOriginalData,
                // lon_dim.getLength(), lat_dim.getLength(),
                // 2, userRaster, 0,
                // false);

                final Variable outVar = ncFileOut
                        .findVariable(foundVariableBriefNames.get(varName));
                final Array outVarData = outVar.read();

                for (int y = 0; y < latDim.getLength(); y++)
                    for (int x = 0; x < lonDim.getLength(); x++)
                        outVarData.setFloat(outVarData.getIndex().set(0, z, y, x), userRaster
                                .getSampleFloat(x, y, 0));

                ncFileOut.write(foundVariableBriefNames.get(varName), outVarData);
            }
        }
    }
}