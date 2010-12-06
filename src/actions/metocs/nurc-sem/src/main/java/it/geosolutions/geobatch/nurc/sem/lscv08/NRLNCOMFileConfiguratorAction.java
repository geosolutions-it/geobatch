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
import it.geosolutions.geobatch.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.metocs.base.METOCSBaseConfiguratorAction;
import it.geosolutions.geobatch.metocs.jaxb.model.MetocElementType;
import it.geosolutions.geobatch.metocs.utils.io.METOCSActionsIOUtils;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to transform lscv08::NRL_NCOM Model
 * 
 */
public class NRLNCOMFileConfiguratorAction extends METOCSBaseConfiguratorAction {

    public static final long NCOMstartTime;

    static {
        GregorianCalendar NCOMcalendar = new GregorianCalendar(2000, 00, 01, 00, 00, 00);
        NCOMcalendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        NCOMstartTime = NCOMcalendar.getTimeInMillis();
    }

    private Array time1Data;

    protected NRLNCOMFileConfiguratorAction(MetocActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    @Override
    protected File unzipMetocArchive(FileSystemMonitorEvent event, String fileSuffix, File outDir,
            File tempFile) throws IOException {
        return ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix)) ? Utilities
                .decompress("NCOM", event.getSource(), tempFile)
                : Utilities.createTodayPrefixedDirectory("NCOM", outDir);
    }

    @Override
    protected void writeDownNetCDF(File outDir, String inputFileName) throws IOException,
            InvalidRangeException, JAXBException, ParseException {
        // input dimensions
        final Dimension lon_dim = ncGridFile.findDimension("lon");

        final Dimension lat_dim = ncGridFile.findDimension("lat");

        final Dimension depth_dim = ncGridFile.findDimension("depth");

        final Dimension time_dim = ncGridFile.findDimension("time");

        int nLat = lat_dim.getLength();
        int nLon = lon_dim.getLength();
        int nTimes = (time_dim != null ? time_dim.getLength() : 0);
        int nDepths = (depth_dim != null ? depth_dim.getLength() : 0);

        // input VARIABLES
        final Variable lonOriginalVar = ncGridFile.findVariable("lon");
        
        final DataType lonDataType = lonOriginalVar.getDataType();

        final Variable latOriginalVar = ncGridFile.findVariable("lat");
        
        final DataType latDataType = latOriginalVar.getDataType();

        boolean hasDepth = false;
        Variable depthOriginalVar = null;
        @SuppressWarnings("unused")
        DataType depthDataType = null;
        if (ncGridFile.findVariable("depth") != null) {
            hasDepth = true;
            depthOriginalVar = ncGridFile.findVariable("depth");
            depthDataType = depthOriginalVar.getDataType();
            nDepths = depth_dim.getLength();
        }

        final Variable timeOriginalVar = ncGridFile.findVariable("time");
        @SuppressWarnings("unused")
        final DataType timeDataType = timeOriginalVar.getDataType();

        final Array lonOriginalData = lonOriginalVar.read();
        final Array latOriginalData = latOriginalVar.read();
        final Array depthOriginalData = hasDepth ? depthOriginalVar.read() : null;
        final Array timeOriginalData = timeOriginalVar.read();

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
        double noData = definingOutputVariables(hasDepth, nLat, nLon, nTimes, nDepths,
                METOCSActionsIOUtils.DOWN);

        // time Variable data
        final SimpleDateFormat toSdf = cruiseName.equalsIgnoreCase("rep10")?
        		new SimpleDateFormat("yyyyMMddHH") : new SimpleDateFormat("yyyyMMdd");
        toSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        final Date timeOriginDate = toSdf.parse(inputFileName.substring(inputFileName
                .lastIndexOf("_") + 1));

        int TAU = normalizingTimes(timeOriginalData, time_dim, timeOriginDate);

        // Setting up global Attributes ...
        settingNCGlobalAttributes(noData, timeOriginDate, TAU);

        // writing bin data ...
        writingDataSets(lon_dim, lat_dim, depth_dim, time_dim, hasDepth, lonOriginalData,
                latOriginalData, depthOriginalData, noData, time1Data, latDataType, lonDataType);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Utility and conversion specific methods implementations...
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int normalizingTimes(final Array timeOriginalData, final Dimension timeDim,
            final Date timeOriginDate) throws ParseException, NumberFormatException {
        time1Data = NetCDFConverterUtilities.getArray(timeDim.getLength(), DataType.DOUBLE);
        int TAU = 0;
        for (int t = 0; t < timeDim.getLength(); t++) {
            // hours since 2000-01-01 00:00 UTC
            long timeValue = timeOriginalData.getLong(timeOriginalData.getIndex().set(t));
            if (t == 0 && timeDim.getLength() > 1) {
                TAU = (int) (timeOriginalData.getLong(timeOriginalData.getIndex().set(t + 1)) - timeValue);
            } else if (t == 0) {
                TAU = (int) ((timeValue * 3600 * 1000) + NCOMstartTime - timeOriginDate.getTime());
            }
            // adding time offset
            timeValue = (NCOMstartTime - startTime) + (timeValue * 3600000);
            // converting back to seconds and storing to data
            time1Data.setLong(time1Data.getIndex().set(t), timeValue / 1000);
        }

        return TAU;
    }

    /**
     * @param lon_dim
     * @param lat_dim
     * @param depth_dim
     * @param time_dim
     * @param hasDepth
     * @param lonOriginalData
     * @param latOriginalData
     * @param depthOriginalData
     * @param noData
     * @param timeOriginalData
     * @throws IOException
     * @throws InvalidRangeException
     */
    protected void writingDataSets(Dimension lonDim, Dimension latDim, Dimension depthDim,
            Dimension timeDim, boolean hasDepth, Array lonOriginalData, Array latOriginalData,
            Array depthOriginalData, double noData, Array timeOriginalData, DataType latDataType,
            DataType lonDataType) throws IOException, InvalidRangeException {
        ncFileOut.create();

        // writing time Variable data
        ncFileOut.write(METOCSActionsIOUtils.TIME_DIM, timeOriginalData);

        // writing depth Variable data
        if (hasDepth)
            ncFileOut.write(METOCSActionsIOUtils.DEPTH_DIM, depthOriginalData);

        // writing lat Variable data
        ncFileOut.write(METOCSActionsIOUtils.LAT_DIM, latOriginalData);

        // writing lon Variable data
        ncFileOut.write(METOCSActionsIOUtils.LON_DIM, lonOriginalData);

        for (String varName : foundVariables.keySet()) {
            Variable var = foundVariables.get(varName);
            double offset = 0.0;
            double scale = 1.0;

            Attribute offsetAtt = var.findAttribute("add_offset");
            Attribute scaleAtt = var.findAttribute("scale_factor");

            offset = (offsetAtt != null ? offsetAtt.getNumericValue().doubleValue() : offset);
            scale = (scaleAtt != null ? scaleAtt.getNumericValue().doubleValue() : scale);

            Array originalVarArray = var.read();
            Array destArray = NetCDFConverterUtilities.getArray(originalVarArray.getShape(),
                    DataType.DOUBLE);

            for (int t = 0; t < timeDim.getLength(); t++)
                for (int z = 0; z < (hasDepth
                        && NetCDFConverterUtilities.hasThisDimension(var,
                                METOCSActionsIOUtils.DEPTH_DIM) ? depthDim.getLength() : 1); z++)
                    for (int y = 0; y < latDim.getLength(); y++)
                        for (int x = 0; x < lonDim.getLength(); x++) {
                            if (!hasDepth
                                    || !NetCDFConverterUtilities.hasThisDimension(var,
                                            METOCSActionsIOUtils.DEPTH_DIM)) {
                                double originalValue = originalVarArray.getDouble(originalVarArray
                                        .getIndex().set(t, y, x));
                                destArray.setDouble(destArray.getIndex().set(t, y, x),
                                        (originalValue != noData ? (originalValue * scale) + offset
                                                : noData));
                            } else {
                                double originalValue = originalVarArray.getDouble(originalVarArray
                                        .getIndex().set(t, z, y, x));
                                destArray.setDouble(destArray.getIndex().set(t, z, y, x),
                                        (originalValue != noData ? (originalValue * scale) + offset
                                                : noData));
                            }
                        }

            ncFileOut.write(foundVariableBriefNames.get(varName), destArray);
        }
    }

    @Override
    protected void createOutputFile(File outDir, String inputFileName) throws IOException {
//      outputFile = new File(outDir, "lscv08_NCOM"
        outputFile = new File(outDir, cruiseName + "_NCOM"
                + (inputFileName.contains("nest") ? "nest"
                        + inputFileName.substring(inputFileName.indexOf("nest") + "nest".length(),
                                inputFileName.indexOf("nest") + "nest".length() + 1) : "")
                + "-Forecast-T" + new Date().getTime()
                + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
        ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());
    }

    @Override
    protected void fillVariablesMaps() throws UnsupportedEncodingException {
        for (Object obj : ncGridFile.getVariables()) {
            final Variable var = (Variable) obj;
            final String varName = var.getName();
            if (!varName.equalsIgnoreCase("lon") && !varName.equalsIgnoreCase("lat")
                    && !varName.equalsIgnoreCase("depth") && !varName.equalsIgnoreCase("time")) {

                if (foundVariables.get(varName) == null) {
                    String longName = null;
                    String briefName = null;
                    String uom = null;

                    for (MetocElementType m : metocDictionary.getMetoc()) {
                        if ((varName.equalsIgnoreCase("salinity") && m.getName().equals("salinity"))
                                || (varName.equalsIgnoreCase("water_temp") && m.getName().equals(
                                        "water temperature"))
                                || (varName.equalsIgnoreCase("surf_el") && m.getName().equals(
                                        "sea surface height"))
                                || (varName.equalsIgnoreCase("water_u") && m.getName().equals(
                                        "water velocity u-component"))
                                || (varName.equalsIgnoreCase("water_v") && m.getName().equals(
                                        "water velocity v-component"))
                                || (varName.equalsIgnoreCase("water_w") && m.getName().equals(
                                        "vertical water velocity"))) {
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

    @Override
    protected double definingOutputVariables(boolean hasDepth, int nLat, int nLon, int nTimes,
            int nDepths, String depthName) {
        final List<Dimension> outDimensions = METOCSActionsIOUtils
                .createNetCDFCFGeodeticDimensions(ncFileOut, true, nTimes, hasDepth,
                        hasDepth ? nDepths : 0, depthName, true, nLat, true, nLon);

        double noData = Double.NaN;

        // defining output variable
        for (String varName : foundVariables.keySet()) {
            boolean hasLocalDepth = hasDepth
                    && NetCDFConverterUtilities.hasThisDimension(foundVariables.get(varName),
                            METOCSActionsIOUtils.DEPTH_DIM);

            List<Dimension> localDimensions = new ArrayList<Dimension>(outDimensions);
            if (hasDepth && !hasLocalDepth) {
                for (Dimension dim : localDimensions) {
                    if (dim.getName().equals(METOCSActionsIOUtils.DEPTH_DIM)) {
                        localDimensions.remove(dim);
                        break;
                    }
                }
            }
            // SIMONE: replaced foundVariables.get(varName).getDataType()
            // with DataType.DOUBLE
            ncFileOut.addVariable(foundVariableBriefNames.get(varName), DataType.DOUBLE,
                    localDimensions);
            // NetCDFConverterUtilities.setVariableAttributes(foundVariables.get(varName),
            // ncFileOut, foundVariableBriefNames.get(varName), new String[]
            // { "positions" });
            ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "long_name",
                    foundVariableLongNames.get(varName));
            ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "units",
                    foundVariableUoM.get(varName));

            Attribute missingValue = foundVariables.get(varName).findAttribute("missing_value");
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

}