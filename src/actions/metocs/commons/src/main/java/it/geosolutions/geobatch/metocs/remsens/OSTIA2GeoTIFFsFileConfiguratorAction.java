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
package it.geosolutions.geobatch.metocs.remsens;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.metocs.utils.io.METOCSActionsIOUtils;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;
import it.geosolutions.geobatch.tools.file.Path;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;


import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * 
 * Public class to split OSTIA files to GeoTIFFs and consequently send them to GeoServer along with
 * their basic metadata.
 */
public class OSTIA2GeoTIFFsFileConfiguratorAction extends
        GeoServerAction<FileSystemEvent> {

    public static final long OSTIA_START_TIME;

    static {
        GregorianCalendar ostiaCalendar = new GregorianCalendar(1981, 00, 01, 00, 00, 00);
        ostiaCalendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        OSTIA_START_TIME = ostiaCalendar.getTimeInMillis();
    }
    
    /**
     * GeoTIFF Writer Default Params
     */
    public final static String GEOSERVER_VERSION = "2.x";

    private static final int DEFAULT_TILE_SIZE = 256;

    private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

    private static final String DEFAULT_COMPRESSION_TYPE = "LZW";

    protected OSTIA2GeoTIFFsFileConfiguratorAction(final GeoServerActionConfiguration configuration)
            throws IOException, NoSuchAuthorityCodeException, FactoryException {
        super(configuration);
    }

    /**
     * EXECUTE METHOD
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
            throws ActionException {

        /**
         * Static DateFormat Converter
         */
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Starting with processing...");
        NetcdfFile ncFileIn = null;
        File inputFile = null;
        try {
            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: " + events.size());
            FileSystemEvent event = events.remove();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                LOGGER.error("DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            final File workingDir = Path.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if ((workingDir == null) || !workingDir.exists() || !workingDir.isDirectory()) {
                LOGGER.error("WorkingDirectory is null or does not exist.");
                throw new IllegalStateException("WorkingDirectory is null or does not exist.");
            }

            // ... BUSINESS LOGIC ... //
            String inputFileName = event.getSource().getAbsolutePath();
            final String filePrefix = FilenameUtils.getBaseName(inputFileName);
            final String fileSuffix = FilenameUtils.getExtension(inputFileName);
            final String fileNameFilter = getConfiguration().getStoreFilePrefix();

            String baseFileName = null;

            if (fileNameFilter != null) {
                if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
                        && ("nc".equalsIgnoreCase(fileSuffix) || "netcdf".equalsIgnoreCase(fileSuffix))) {
                    // etj: are we missing something here?
                    baseFileName = filePrefix;
                }
            } else if ("nc".equalsIgnoreCase(fileSuffix) || "netcdf".equalsIgnoreCase(fileSuffix)) {
                baseFileName = filePrefix;
            }

            if (baseFileName == null) {
                LOGGER.error("Unexpected file '" + inputFileName + "'");
                throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
            }

            final String ostiaDir = "rep10_OSTIA";
            inputFileName = FilenameUtils.getBaseName(inputFileName);
            inputFileName = (inputFileName.lastIndexOf("-") > 0 ? inputFileName.substring(0,
                    inputFileName.lastIndexOf("-")) : inputFileName);
            inputFile = new File(event.getSource().getAbsolutePath());
            ncFileIn = NetcdfFile.open(inputFile.getAbsolutePath());
            final File outDir = Utilities.createTodayDirectory(workingDir, inputFileName);

            final String TAU = "0";
            final double noData = -32768;

            final Variable timeOriginalVar = ncFileIn.findVariable("time");
            final Array timeOriginalData = timeOriginalVar.read();
            
            long timeValue = timeOriginalData.getLong(timeOriginalData.getIndex().set(0));
            timeValue = OSTIA_START_TIME + timeValue * 1000;

            final Calendar timeInstant = new GregorianCalendar(TimeZone.getTimeZone("GMT+0"));
            timeInstant.setTimeInMillis(timeValue);
            
            final SimpleDateFormat fromSdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'");
            fromSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

            final String time = fromSdf.format(timeInstant.getTimeInMillis());

            final Dimension latDim = ncFileIn.findDimension(METOCSActionsIOUtils.LAT_DIM);
            final boolean latDimExists = latDim != null;

            final Dimension lonDim = ncFileIn.findDimension(METOCSActionsIOUtils.LON_DIM);
            final boolean lonDimExists = lonDim != null;

            if (!latDimExists || !lonDimExists) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Invalid input NetCDF-CF Geodetic file: longitude and/or latitude dimensions could not be found!");
                throw new IllegalStateException(
                        "Invalid input NetCDF-CF Geodetic file: longitude and/or latitude dimensions could not be found!");
            }

            int nLat = latDim.getLength();
            int nLon = lonDim.getLength();

            final Variable lonOriginalVar = ncFileIn.findVariable(METOCSActionsIOUtils.LON_DIM);
            final Variable latOriginalVar = ncFileIn.findVariable(METOCSActionsIOUtils.LAT_DIM);

            final Array latOriginalData = latOriginalVar.read();
            final Array lonOriginalData = lonOriginalVar.read();

            float[] bbox = METOCSActionsIOUtils.computeExtremaAsFloat(latOriginalData, lonOriginalData, latDim, lonDim);

            // building Envelope
            final GeneralEnvelope envelope = new GeneralEnvelope(METOCSActionsIOUtils.WGS_84);
                        
            //Using String format to avoid loss of precision from Float to Double
            envelope.setRange(0, Double.parseDouble(Float.toString(bbox[0])), Double.parseDouble(Float.toString(bbox[2])));
            envelope.setRange(1, Double.parseDouble(Float.toString(bbox[1])), Double.parseDouble(Float.toString(bbox[3])));

            // Storing variables Variables as GeoTIFFs
            final List<Variable> foundVariables = ncFileIn.getVariables();
            final ArrayList<String> variables = new ArrayList<String>();
            int numVars = 0;

            for (Variable var : foundVariables) {
                if (var != null) {
                    String varName = var.getName();
                    if (varName.equalsIgnoreCase(METOCSActionsIOUtils.LAT_DIM)
                            || varName.equalsIgnoreCase(METOCSActionsIOUtils.LON_DIM)
                            || varName.equalsIgnoreCase(METOCSActionsIOUtils.TIME_DIM)
                            || varName.equalsIgnoreCase(METOCSActionsIOUtils.HEIGHT_DIM)
                            || varName.equalsIgnoreCase(METOCSActionsIOUtils.DEPTH_DIM))
                        continue;
                    variables.add(varName);

                    boolean canProceed = false;
                    
                    if (varName.equalsIgnoreCase("analysed_sst")){
                        varName = "sst";
                    } else if (varName.equalsIgnoreCase("analysis_error")){
                        varName = "sst-error";
                    } else {
                        continue;
                    }
                    double offset = 0.0;
                    double scale = 1.0;
                    double [] scaleFactors = null;
//                    Attribute offsetAtt = var.findAttribute("add_offset");
                    Attribute scaleAtt = var.findAttribute("scale_factor");

//                    offset = (offsetAtt != null ? Double.parseDouble(Float.toString(offsetAtt.getNumericValue().floatValue())) : offset);
                    scale = (scaleAtt != null ? Double.parseDouble(Float.toString(scaleAtt.getNumericValue().floatValue())) : scale);
                    
                    //Converting to celsisu degrees. Offset isn't taken into account.
                    if (offset != 0.0 || scale != 1.0){
                        scaleFactors = new double[]{offset, scale};
                    }
                    final File gtiffOutputDir = new File(outDir.getAbsolutePath()
                            + File.separator
                            + ostiaDir
                            + "_"
                            + varName.replaceAll("_", "")
                            +  "_T" + new Date().getTime());

                    if (!gtiffOutputDir.exists())
                        canProceed = gtiffOutputDir.mkdirs();

                    canProceed = gtiffOutputDir.isDirectory();

                    if (canProceed) {
                        // //
                        // defining the SampleModel data type
                        // //
                        final SampleModel outSampleModel = Utilities.getSampleModel(DataType.DOUBLE, nLon, nLat, 1);

                        Array originalVarArray = var.read();
                        Attribute missingValue = var.findAttribute("_FillValue");
                        double localNoData = noData;
                        if (missingValue != null) {
                            localNoData = missingValue.getNumericValue().doubleValue();
                        }

                        WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);
                       

                        METOCSActionsIOUtils.write2DData(userRaster, var, originalVarArray,
                                false, false, new int[] {0, nLat, nLon }, true, null, false, scaleFactors);

                        // ////
                        // producing the Coverage here...
                        // ////
                        final StringBuilder coverageName = new StringBuilder("Ostia_MULTI-OSTIA")
                                .append("_").append(varName.replaceAll("_", ""))
                                .append("_").append("0000.000").append("_")
                                .append("0000.000").append("_")
                                .append(time).append("_").append(time).append("_")
                                .append(TAU).append("_").append(localNoData);

                        File gtiffFile = Utilities.storeCoverageAsGeoTIFF(gtiffOutputDir,
                                coverageName.toString(), varName, userRaster, noData,
                                envelope, DEFAULT_COMPRESSION_TYPE,
                                DEFAULT_COMPRESSION_RATIO, DEFAULT_TILE_SIZE);

                        // ... setting up the appropriate event for the next
                        // action
                        events.add(new FileSystemEvent(gtiffOutputDir,
                                FileSystemEventType.FILE_ADDED));
                    }

                    numVars++;
                }
            }

            return events;
        } catch (Throwable t) {
            LOGGER.error(t.getLocalizedMessage(), t);
            JAI.getDefaultInstance().getTileCache().flush();
            return null;
        } finally {
            try {
                if (ncFileIn != null) {
                    ncFileIn.close();
                }

                if (inputFile != null && inputFile.exists()) {
                    inputFile.delete();
                }
            } catch (IOException e) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                JAI.getDefaultInstance().getTileCache().flush();
            }
        }
    }
}