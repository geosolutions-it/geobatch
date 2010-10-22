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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.metocs.base.METOCSBaseConfiguratorAction;
import it.geosolutions.geobatch.metocs.jaxb.model.MetocElementType;
import it.geosolutions.geobatch.metocs.jaxb.model.Metocs;
import it.geosolutions.geobatch.metocs.utils.io.METOCSActionsIOUtils;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;
import it.geosolutions.geobatch.utils.IOUtils;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

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

    protected MERCATORFileConfiguratorAction(MetocActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    @Override
    protected File unzipMetocArchive(FileSystemMonitorEvent event, String fileSuffix, File outDir,
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
        @SuppressWarnings("unused")
        final DataType lonDataType = lonOriginalVar.getDataType();

        final Variable latOriginalVar = ncGridFile.findVariable("latitude");
        @SuppressWarnings("unused")
        final DataType latDataType = latOriginalVar.getDataType();

        final Variable depthOriginalVar = ncGridFile.findVariable("depth");
        @SuppressWarnings("unused")
        final DataType depthDataType = depthOriginalVar.getDataType();

        final Array lonOriginalData = lonOriginalVar.read();
        final Array latOriginalData = latOriginalVar.read();
        final Array depthOriginalData = depthOriginalVar.read();

        double[] bbox = METOCSActionsIOUtils.computeExtrema(latOriginalData, lonOriginalData,
                lat_dim, lon_dim);

        // building Envelope
        final GeneralEnvelope envelope = new GeneralEnvelope(METOCSActionsIOUtils.WGS_84);
        envelope.setRange(0, bbox[0], bbox[2]);
        envelope.setRange(1, bbox[1], bbox[3]);

        // ////
        // ... create the output file data structure
        // ////
        outputFile = new File(outDir, "lscv08_MERCATOR-Forecast-T" + new Date().getTime()
                + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
        ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

        // NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut,
        // ncFileIn.getGlobalAttributes());

        // Grabbing the Variables Dictionary
        JAXBContext context = JAXBContext.newInstance(Metocs.class);
        Unmarshaller um = context.createUnmarshaller();

        File metocDictionaryFile = IOUtils.findLocation(configuration.getMetocDictionaryPath(),
                new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(metocDictionaryFile));

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

        // defining the file header and structure
        final List<Dimension> outDimensions = METOCSActionsIOUtils
                .createNetCDFCFGeodeticDimensions(ncFileOut, true, 1, true, depth_dim.getLength(),
                        METOCSActionsIOUtils.DOWN, true, lat_dim.getLength(), true, lon_dim
                                .getLength());

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

            if (Double.isNaN(noData)) {
                Attribute missingValue = foundVariables.get(varName).findAttribute("_FillValue");
                if (missingValue != null) {
                    noData = missingValue.getNumericValue().doubleValue();
                    ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName),
                            "missing_value", noData);
                }
            }
        }

        // MERCATOR OCEAN MODEL Global Attributes
        Attribute referenceTime = ncGridFile.findGlobalAttributeIgnoreCase("bulletin_date");
        Attribute forecastDate = ncGridFile.findGlobalAttributeIgnoreCase("forecast_range");

        final SimpleDateFormat toSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        toSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        final Date timeOriginDate = toSdf
                .parse(referenceTime.getStringValue().trim().toLowerCase());
        int TAU = 0;

        final String forecastDays = forecastDate.getStringValue();
        if (forecastDays != null) {
            final int index = forecastDays.indexOf("-day_forecast");
            if (index != -1) {
                int numDay = Integer.parseInt(forecastDays.substring(0, index));
                TAU = numDay * 24;
            }
        }

        // Setting up global Attributes ...
        settingNCGlobalAttributes(noData, timeOriginDate, TAU);

        // writing bin data ...
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
            final SampleModel outSampleModel = Utilities.getSampleModel(var.getDataType(), lon_dim
                    .getLength(), lat_dim.getLength(), 1);

            Array originalVarArray = var.read();

            for (int z = 0; z < depth_dim.getLength(); z++) {

                WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

                METOCSActionsIOUtils.write2DData(userRaster, var, originalVarArray, false, false,
                        new int[] { z, lat_dim.getLength(), lon_dim.getLength() }, false);

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

                for (int y = 0; y < lat_dim.getLength(); y++)
                    for (int x = 0; x < lon_dim.getLength(); x++)
                        outVarData.setFloat(outVarData.getIndex().set(0, z, y, x), userRaster
                                .getSampleFloat(x, y, 0));

                ncFileOut.write(foundVariableBriefNames.get(varName), outVarData);
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
}