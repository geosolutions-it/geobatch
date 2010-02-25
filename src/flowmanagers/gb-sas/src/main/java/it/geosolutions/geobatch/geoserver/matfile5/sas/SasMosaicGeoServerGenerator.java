/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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

package it.geosolutions.geobatch.geoserver.matfile5.sas;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.base.Utils;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.mosaic.Mosaicer;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicReader;

/**
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * 
 */
public class SasMosaicGeoServerGenerator
		extends GeoServerConfiguratorAction<FileSystemMonitorEvent> {

    public final static String SAS_STYLE = "sas";
    
    public final static String SAS_RAW_STYLE = "sasraw";
    
    public final static String DEFAULT_STYLE = "raster";
    
    public final static String GEOSERVER_VERSION = "2.X";
    
    private final static String NAMESPACE = "namespace";
    private final static String STORE = "store";
    private final static String LAYERNAME = "layername";
       
    public SasMosaicGeoServerGenerator(GeoServerActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {

            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            
            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            final String dataType = configuration.getDatatype();
            
            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if ((workingDir == null) || !workingDir.exists() || 
                    (!workingDir.isDirectory()&&dataType.equalsIgnoreCase("imagemosaic"))) {
                throw new IllegalStateException("GeoServerDataDirectory is null or does not exist.");
            }
            
            final String inputFileName = workingDir.getAbsolutePath();
            String baseFileName = null;
            final String coverageStoreId; 

            if (dataType.equalsIgnoreCase("imagemosaic")){
                coverageStoreId = FilenameUtils.getName(inputFileName);
                final ImageMosaicFormat format = new ImageMosaicFormat();
                ImageMosaicReader coverageReader = null;
    
                // //
                // Trying to read the mosaic
                // //
                try {
                    coverageReader = (ImageMosaicReader) format.getReader(workingDir);
    
                    if (coverageReader == null) {
                        LOGGER.log(Level.SEVERE, "No valid Mosaic found for this Data Flow!");
                        throw new IllegalStateException(
                                "No valid Mosaic found for this Data Flow!");
                    }
                } finally {
                    if (coverageReader != null) {
                        try {
                            coverageReader.dispose();
                        } catch (Throwable e) {
                            if (LOGGER.isLoggable(Level.FINEST))
                                LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                        }
                    }
                }
            } else if (dataType.equalsIgnoreCase("geotiff")){
            	coverageStoreId = FilenameUtils.getBaseName(inputFileName);
                final GeoTiffFormat format = new GeoTiffFormat ();
                GeoTiffReader coverageReader = null;
    
                // //
                // Trying to read the geotiff
                // //
                try {
                    coverageReader = (GeoTiffReader) format.getReader(workingDir);
    
                    if (coverageReader == null) {
                        LOGGER.log(Level.SEVERE, "No valid Mosaic found for this Data Flow!");
                        throw new IllegalStateException(
                                "No valid Mosaic found for this Data Flow!");
                    }
                } finally {
                    if (coverageReader != null) {
                        try {
                            coverageReader.dispose();
                        } catch (Throwable e) {
                            if (LOGGER.isLoggable(Level.FINEST))
                                LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                        }
                    }
                }
            } else {
            	LOGGER.log(Level.SEVERE,"Unsupported format type" + dataType);
                return null;
            }
            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////
            
            final Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("namespace",	getConfiguration().getDefaultNamespace());
            queryParams.put("wmspath",		getConfiguration().getWmsPath());
            final String[] returnedLayer = GeoServerRESTHelper.send(workingDir, 
                    workingDir, 
                    configuration.getGeoserverURL(),
                    configuration.getGeoserverUID(),
                    configuration.getGeoserverPWD(),
                    coverageStoreId,
                    baseFileName,
                    queryParams,
                    "",
                    configuration.getDataTransferMethod(),
                    dataType, GEOSERVER_VERSION, null, getConfiguration().getDefaultStyle());
//            if (returnedLayer!=null && returnedLayer.length==3){
//            	writeGeowebcacheConfigurationFile(returnedLayer);
//            }
            return events;
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }
    }
    
    private void writeGeowebcacheConfigurationFile(final String[] returnedLayer) throws IOException {
    	StringBuilder sb = new StringBuilder(NAMESPACE).append("=").append(returnedLayer[1])
    	.append("&").append(STORE).append("=").append(returnedLayer[0])
    	.append("&").append(LAYERNAME).append("=").append(returnedLayer[2]);
    	final File tempFile = File.createTempFile("gwc", ".txt");
    	final BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
    	try{
    		writer.write(sb.toString());
    	}catch (Throwable th){
    		if (LOGGER.isLoggable(Level.FINE))
    			LOGGER.fine("Error while writing geowebcache configuration file "+th.getLocalizedMessage());
    	}finally{
    		if (writer != null){
    			try{
    				writer.close();
    			}catch (Throwable th){
    				if (LOGGER.isLoggable(Level.FINE))
    	    			LOGGER.log(Level.FINE, "Error while closing the writer"+th.getLocalizedMessage(),th);
    	    				
    			}
    		}
    	}
    	
    	final File outputFile = new File(new StringBuilder(((SasMosaicGeoServerConfiguration)configuration).getGeowebcacheWatchingDir())
    	.append(IOUtils.FILE_SEPARATOR).append(FilenameUtils.getName(tempFile.getAbsolutePath())).toString());
    	IOUtils.copyFile(tempFile, outputFile);
    	IOUtils.deleteFile(tempFile);
	}

    /**
     * Setup a Geoserver Ingestion action to send data to Geoserver via REST 
     * @param mosaicToBeIngested the location of the mosaic to be ingested 
     * @param prefix one of {@link Mosaicer#BALANCED_PREFIX} or {@link Mosaicer#RAW_PREFIX}
     * @throws Exception
     */
    public static void ingest(final String dataToBeIngested, final String wmsPath,
    		final String geoserverURL, final String geoserverUID, final String geoserverPWD,
    		final String geoserverUploadMethod, final String style, final String datatype, 
    		final String geowebcacheWatchingDir) throws Exception{
      // //
      //
      // Setting up the GeoserverActionConfiguration properties
      //
      // //
      final SasMosaicGeoServerConfiguration geoserverConfig = new SasMosaicGeoServerConfiguration();
      geoserverConfig.setGeoserverURL(geoserverURL);
      geoserverConfig.setGeoserverUID(geoserverUID);
      geoserverConfig.setGeoserverPWD(geoserverPWD);
      geoserverConfig.setDataTransferMethod(geoserverUploadMethod);
      geoserverConfig.setWorkingDirectory(dataToBeIngested);
      geoserverConfig.setDefaultNamespace("it.geosolutions");
      geoserverConfig.setWmsPath(wmsPath);
      geoserverConfig.setDatatype(datatype);
      geoserverConfig.setGeowebcacheWatchingDir(geowebcacheWatchingDir);
      
      //Setting styles
      
      geoserverConfig.setDefaultStyle(style);
      final List<String> styles = new ArrayList<String>();
      styles.add(style);
      geoserverConfig.setStyles(styles);
      
      final SasMosaicGeoServerGenerator geoserverIngestion  = new SasMosaicGeoServerGenerator(geoserverConfig);
      geoserverIngestion.execute(null);
    }

    /**
     * Build a WMSPath from the specified String 
     * Input names are in the form: DATE_missionXX_LegXXXX_CHANNEL
     * As an instance: DATE=090316 and CHANNEL=port
     * 
     * @param name
     * @return
     */
	public static String buildWmsPath(final String name) {
		if (name==null || name.trim().length()==0)
			return "";
		final int missionIndex = name.indexOf("_");
        final String timePrefix = name.substring(0,missionIndex);
        final int legIndex = name.indexOf(Utils.LEG_PREFIX);
        String missionPrefix = name.substring(missionIndex+1,legIndex);
//        final int indexOfMissionNumber = missionPrefix.lastIndexOf("_");
//        missionPrefix = new StringBuffer("mission").append(missionPrefix.substring(indexOfMissionNumber+1)).toString();
        final String legPath = name.substring(legIndex+1);
        final String wmsPath = new StringBuilder("/").append(timePrefix).append("/").append(missionPrefix).append("/").append(legPath.replace("_","/")).toString();
        return wmsPath;
	}
    
}
