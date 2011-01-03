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
package it.geosolutions.geobatch.metocs.base;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.action.tools.adapter.EventAdapter;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.metocs.MetocConfigurationAction;
import it.geosolutions.geobatch.metocs.utils.io.METOCSActionsIOUtils;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;
import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * 
 * Public class to split NetCDF_CF Geodetic to GeoTIFFs and consequently send them to GeoServer
 * along with their basic metadata.
 * 
 * For the NetCDF_CF Geodetic file we assume that it contains georectified geodetic grids and
 * therefore has a maximum set of dimensions as follows:
 * 
 * lat { lat:long_name = "Latitude" lat:units = "degrees_north" }
 * 
 * lon { lon:long_name = "Longitude" lon:units = "degrees_east" }
 * 
 * time { time:long_name = "time" time:units = "seconds since 1980-1-1 0:0:0" }
 * 
 * depth { depth:long_name = "depth"; depth:units = "m"; depth:positive = "down"; }
 * 
 * height { height:long_name = "height"; height:units = "m"; height:positive = "up"; }
 * 
 */
public class NetCDFCFGeodetic2GeoTIFFsFileAction
                extends MetocConfigurationAction<EventObject> 
                implements EventAdapter<NetcdfEvent>{

    /**
     * GeoTIFF Writer Default Params
     */
    public final static String GEOSERVER_VERSION = "2.x";

    private static final int DEFAULT_TILE_SIZE = 256;

    private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

    private static final String DEFAULT_COMPRESSION_TYPE = "LZW";

    /**
     * Static DateFormat Converter
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'");

    protected NetCDFCFGeodetic2GeoTIFFsFileAction(MetocActionConfiguration configuration)
            throws IOException {
        super(configuration);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }
    
    /**
     * This method define the mapping between input and output EventObject instance
     * @param ieo is the object to transform
     * @return the EventObject adapted
     */
    public NetcdfEvent adapter(EventObject ieo) throws ActionException{
        NetcdfEvent eo=null;
        if (ieo!=null)
            try {
                /**
                 * Map the FileSystemMonitorEvent to a NetCDFDataset
                 * event object 
                 */
                if (ieo instanceof FileSystemMonitorEvent){
                
                    NetcdfFile ncFileIn = null;
                    File inputFile = null;
                    
                    FileSystemMonitorEvent fs_event = (FileSystemMonitorEvent) ieo;
                    
                    inputFile = new File(fs_event.getSource().getAbsolutePath());

                    /**
                     * Here we assume that each FileSystemMonitorEvent file
                     * represent a valid NetcdfFile.
                     * This is done (without checks) since the specific class 
                     * implementation name define the file type should be passed.
                     * Be careful when build flux
                     */
// TODO we should check if this file is a netcdf file!
                    
                    ncFileIn = NetcdfFile.open(inputFile.getAbsolutePath());
                    NetcdfDataset d=new NetcdfDataset(ncFileIn);//TODO: add performBackup arg
                    eo=new NetcdfEvent(d);
                }
                /**
                 * if it is a NetcdfEvent we only have to return a NetcdfEvent 
                 * input instance
                 */
                else if (ieo instanceof NetcdfEvent){
                    return (NetcdfEvent)ieo;
                }
                else
                    throw new ActionException(this, "Passed event is not a FileSystemMonitorEvent instance");
        } catch (IOException ioe) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
            throw new ActionException(this, "ioe.getLocalizedMessage()");
        }
        return eo;
    }

    /**
     * EXECUTE METHOD
     */
    public Queue<EventObject> execute(Queue<EventObject> events)
            throws ActionException {

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Starting with processing...");
        NetcdfFile ncFileIn = null;
        File inputFile = null;
        try {
            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            
            NetcdfEvent event=adapter(events.remove());

            ncFileIn=event.getSource();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
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
                LOGGER.log(Level.SEVERE, "WorkingDirectory is null or does not exist.");
                throw new IllegalStateException("WorkingDirectory is null or does not exist.");
            }

//            // ... BUSINESS LOGIC ... //
          //TODO
            String inputFileName=null;
            if (ncFileIn!=null)
                inputFileName = ncFileIn.getLocation();
            else
                throw new IllegalArgumentException("Unable to locate event file sources");
            
            final String filePrefix = FilenameUtils.getBaseName(inputFileName);
            final String fileSuffix = FilenameUtils.getExtension(inputFileName);
            final String fileNameFilter = getConfiguration().getStoreFilePrefix();

            String baseFileName = null;

            if (fileNameFilter != null) {
                if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
                        && ("nc".equalsIgnoreCase(fileSuffix) || "netcdf"
                                .equalsIgnoreCase(fileSuffix))) {
                    // etj: are we missing something here?
                    baseFileName = filePrefix;
                }
            } else if ("nc".equalsIgnoreCase(fileSuffix) || "netcdf".equalsIgnoreCase(fileSuffix)) {
                baseFileName = filePrefix;
            }

            if (baseFileName == null) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
                throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
            }

            inputFileName = FilenameUtils.getBaseName(inputFileName);
            inputFileName = (inputFileName.lastIndexOf("-") > 0 ? inputFileName.substring(0,
            inputFileName.lastIndexOf("-")) : inputFileName);
            
            final File outDir = (!configuration.isTimeUnStampedOutputDir() ? Utilities
                    .createTodayDirectory(workingDir, inputFileName) : Utilities.createDirectory(
                    workingDir, inputFileName));

            // input DIMENSIONS
            final Dimension timeDim = ncFileIn.findDimension(METOCSActionsIOUtils.TIME_DIM);
            final boolean timeDimExists = timeDim != null;

            /*
             * @note Carlo Cancellieri 16 Dec 2010
             * Search the global attributes as global attributes or as 
             * attributes of the root group.
             */
            Attribute baseTimeAttr=null;
            if ((baseTimeAttr=ncFileIn.findGlobalAttribute("base_time"))==null)
                if ((baseTimeAttr=ncFileIn.getRootGroup().findAttribute("base_time"))==null){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, "Unable to find \'base_time\' global variable in the source file");
                    throw new Exception("Unable to find \'base_time\' global variable in the source file");
                }
            final String baseTime = baseTimeAttr.getStringValue();
            baseTimeAttr=null;
            /*
             * @note Carlo Cancellieri 16 Dec 2010
             * Search the global attributes as global attributes or as 
             * attributes of the root group.
             */
            Attribute tauAttr=null;
            if ((tauAttr=ncFileIn.findGlobalAttribute("tau"))==null)
                if ((tauAttr=ncFileIn.getRootGroup().findAttribute("tau"))==null){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, "Unable to find \'tau\' global variable in the source file");
                    throw new Exception("Unable to find \'tau\' global variable in the source file");
                }
// TODO check -> if tauAttr.getNumericValue()==NULL
            final String TAU = String.valueOf(tauAttr.getNumericValue().intValue());
            tauAttr=null;
            
            /*
             * @note Carlo Cancellieri 16 Dec 2010
             * Search the global attributes as global attributes or as 
             * attributes of the root group.
             */
            Attribute nodataAttr=null;
            if ((nodataAttr=ncFileIn.findGlobalAttribute("nodata"))==null)
                if ((nodataAttr=ncFileIn.getRootGroup().findAttribute("nodata"))==null){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, "Unable to find \'nodata\' global variable in the source file");
                    throw new Exception("Unable to find \'nodata\' global variable in the source file");
                }
// TODO check -> if nodataAttr.getNumericValue()==NULL
            final double noData = nodataAttr.getNumericValue().doubleValue();
            nodataAttr=null;

            final Dimension depthDim = ncFileIn.findDimension(METOCSActionsIOUtils.DEPTH_DIM);
            final boolean depthDimExists = depthDim != null;

            final Dimension heightDim = ncFileIn.findDimension(METOCSActionsIOUtils.HEIGHT_DIM);
            final boolean heightDimExists = heightDim != null;

            final Dimension latDim = ncFileIn.findDimension(METOCSActionsIOUtils.LAT_DIM);
            final boolean latDimExists = latDim != null;

            final Dimension lonDim = ncFileIn.findDimension(METOCSActionsIOUtils.LON_DIM);
            final boolean lonDimExists = lonDim != null;

            // dimensions' checks
            final boolean hasZeta = depthDimExists || heightDimExists;

            if (!latDimExists || !lonDimExists) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe("Invalid input NetCDF-CF Geodetic file: longitude and/or latitude dimensions could not be found!");
                throw new IllegalStateException(
                        "Invalid input NetCDF-CF Geodetic file: longitude and/or latitude dimensions could not be found!");
            }

            int nTime = timeDimExists ? timeDim.getLength() : 0;
            int nZeta = 0;
            int nLat = latDim.getLength();
            int nLon = lonDim.getLength();

            // input VARIABLES
            final Variable timeOriginalVar = ncFileIn.findVariable(METOCSActionsIOUtils.TIME_DIM);
            final Array timeOriginalData = timeOriginalVar.read();
            final Index timeOriginalIndex = timeOriginalData.getIndex();

            final Variable lonOriginalVar = ncFileIn.findVariable(METOCSActionsIOUtils.LON_DIM);

            final Variable latOriginalVar = ncFileIn.findVariable(METOCSActionsIOUtils.LAT_DIM);

            final Array latOriginalData = latOriginalVar.read();
            final Array lonOriginalData = lonOriginalVar.read();

            // //
            //
            // Depth related variables
            //
            // //
            Variable zetaOriginalVar = null;
            Array zetaOriginalData = null;

            if (hasZeta) {
                zetaOriginalVar = ncFileIn
                        .findVariable(depthDimExists ? METOCSActionsIOUtils.DEPTH_DIM
                                : METOCSActionsIOUtils.HEIGHT_DIM);
                if (zetaOriginalVar != null) {
                    nZeta = depthDimExists ? depthDim.getLength() : heightDim.getLength();
                    zetaOriginalData = zetaOriginalVar.read();
                }
            }

            double[] bbox = METOCSActionsIOUtils.computeExtrema(latOriginalData, lonOriginalData,
                    latDim, lonDim);

            // building Envelope
            final GeneralEnvelope envelope = new GeneralEnvelope(METOCSActionsIOUtils.WGS_84);
            envelope.setRange(0, bbox[0], bbox[2]);
            envelope.setRange(1, bbox[1], bbox[3]);

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

                    final File gtiffOutputDir = new File(outDir.getAbsolutePath()
                            + File.separator
                            + inputFileName
                            + "_"
                            + varName.replaceAll("_", "")
                            + (!configuration.isTimeUnStampedOutputDir() ? "_T"
                                    + new Date().getTime() : ""));

                    if (!gtiffOutputDir.exists())
                        canProceed = gtiffOutputDir.mkdirs();

                    canProceed = gtiffOutputDir.isDirectory();

                    if (canProceed) {
                        // //
                        // defining the SampleModel data type
                        // //
                        final SampleModel outSampleModel = Utilities.getSampleModel(var
                                .getDataType(), nLon, nLat, 1);

                        Array originalVarArray = var.read();
                        Attribute missingValue = var.findAttribute("missing_value");
                        double localNoData = noData;
                        if (missingValue != null) {
                            /**
                             * Transforming float to a double some data introduce errors
                             *  
                            System.out.println("FLOAT_1: "+missingValue.getNumericValue().floatValue());
                            System.out.println("FLOAT_2: "+(float)missingValue.getNumericValue().floatValue());
                            System.out.println("FLOAT_3: "+(double)missingValue.getNumericValue().floatValue());
                            System.out.println("F3: "+missingValue.getNumericValue().toString());
                            System.out.println("F4: "+Float.parseFloat(missingValue.getNumericValue().toString()));
                            System.out.println("F5: "+Double.parseDouble(missingValue.getNumericValue().toString()));
                            System.out.println("F6: "+(double)Double.parseDouble(missingValue.getNumericValue().toString()));
                            
                            DataType dt=missingValue.getDataType();
                            if (dt==DataType.DOUBLE)
                                localNoData = missingValue.getNumericValue().doubleValue();
                            else if (dt==DataType.FLOAT)
                                localNoData = Double.parseDouble(missingValue.getNumericValue().toString());
                            else if (dt==DataType.INT){
                                localNoData = missingValue.getNumericValue().intValue();
                            }
                            else if (dt==DataType.SHORT)
                                localNoData = missingValue.getNumericValue().shortValue();
                            else if (dt==DataType.LONG)
                                localNoData = missingValue.getNumericValue().longValue();
                            else if (dt==DataType.BYTE)
                                localNoData = missingValue.getNumericValue().byteValue();
                            else
                                throw new NumberFormatException("Unable to enstablish missing_value data type");
                                */
                            
                            // this will do the work
                            localNoData =Double.parseDouble(missingValue.getNumericValue().toString());
                            
                        }
                        final boolean hasLocalZLevel = NetCDFConverterUtilities.hasThisDimension(
                                var, METOCSActionsIOUtils.DEPTH_DIM)
                                || NetCDFConverterUtilities.hasThisDimension(var,
                                        METOCSActionsIOUtils.HEIGHT_DIM);

                        for (int z = 0; z < (hasLocalZLevel ? nZeta : 1); z++) {
                            for (int t = 0; t < (timeDimExists ? nTime : 1); t++) {
                                WritableRaster userRaster = Raster.createWritableRaster(
                                        outSampleModel, null);

                                METOCSActionsIOUtils.write2DData(userRaster, var, originalVarArray,
                                        false, false, (hasLocalZLevel ? new int[] { t, z, nLat,
                                                nLon } : new int[] { t, nLat, nLon }), configuration.isFlipY());

                                // ////
                                // producing the Coverage here...
                                // ////
                                final StringBuilder coverageName = new StringBuilder(inputFileName)
                                        .append("_").append(varName.replaceAll("_", ""))
                                        .append("_").append(
                                                hasLocalZLevel ? elevLevelFormat(zetaOriginalData
                                                        .getDouble(zetaOriginalData.getIndex().set(
                                                                z))) : "0000.000").append("_")
                                        .append(
                                                hasLocalZLevel ? elevLevelFormat(zetaOriginalData
                                                        .getDouble(zetaOriginalData.getIndex().set(
                                                                z))) : "0000.000").append("_")
                                        .append(baseTime).append("_").append(
                                                Integer.parseInt(TAU) == 0 ? baseTime
                                                        : timeDimExists ? sdf
                                                                .format(getTimeInstant(
                                                                        timeOriginalData,
                                                                        timeOriginalIndex, t))
                                                                : "00000000T000000000Z").append("_")
                                        .append(TAU).append("_").append(localNoData);

                                File gtiffFile = Utilities.storeCoverageAsGeoTIFF(gtiffOutputDir,
                                        coverageName.toString(), varName, userRaster, noData,
                                        envelope, DEFAULT_COMPRESSION_TYPE,
                                        DEFAULT_COMPRESSION_RATIO, DEFAULT_TILE_SIZE);
                            }
                        }

                        // ... setting up the appropriate event for the next
                        // action
                        events.add(new FileSystemMonitorEvent(gtiffOutputDir,
                                FileSystemMonitorNotifications.FILE_ADDED));
                    }

                    numVars++;
                }
            }

            return events;
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
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
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            } finally {
                JAI.getDefaultInstance().getTileCache().flush();
            }
        }
    }

    /**
     * @param timeOriginalData
     * @param timeOriginalIndex
     * @param t
     * @return
     */
    private static long getTimeInstant(final Array timeOriginalData, final Index timeOriginalIndex,
            int t) {
        long timeValue = timeOriginalData.getLong(timeOriginalIndex.set(t));
        timeValue = METOCSActionsIOUtils.startTime + timeValue * 1000;

        final Calendar roundedTimeInstant = new GregorianCalendar(TimeZone.getTimeZone("GMT+0"));
        roundedTimeInstant.setTimeInMillis(timeValue);

        int minutes = roundedTimeInstant.get(Calendar.MINUTE);
        int hours = roundedTimeInstant.get(Calendar.HOUR);

        if (minutes > 50)
            hours++;

        roundedTimeInstant.set(Calendar.SECOND, 0);
        roundedTimeInstant.set(Calendar.MINUTE, 0);
        roundedTimeInstant.set(Calendar.HOUR, hours);

        return roundedTimeInstant.getTimeInMillis();
    }

    /**
     * 
     * @param d
     * @return
     */
    private static String elevLevelFormat(double d) {
        String[] parts = String.valueOf(d).split("\\.");

        String integerPart = parts[0];
        String decimalPart = parts[1];

        while (integerPart.length() % 4 != 0)
            integerPart = "0" + integerPart;

        decimalPart = decimalPart.length() > 3 ? decimalPart.substring(0, 3) : decimalPart;

        while (decimalPart.length() % 3 != 0)
            decimalPart = decimalPart + "0";

        return integerPart + "." + decimalPart;
    }


}