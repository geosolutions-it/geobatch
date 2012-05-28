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
package it.geosolutions.geobatch.geoserver.shapefile;

import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerShapeActionConfiguration;
import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.UploadMethod;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.datastore.GSShapefileDatastoreEncoder;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTDatastoreManager;
import it.geosolutions.tools.compress.file.Compressor;
import it.geosolutions.tools.compress.file.Extract;
import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Geoserver ShapeFile action.
 * 
 * Process shapefiles and inject them into a Geoserver instance.
 * 
 * Accept:<br>
 * - a list of mandatory files(ref. to the shape file standard for details) - a compressed archive
 * (ref. to the Extract class to see accepted formats)
 * 
 * Check the content of the input and build a valid ZIP file which represent the output of this
 * action.
 * 
 * The same output is sent to the configured GeoServer using the GS REST api.
 * 
 * TODO: check for store/layer existence
 * TODO: Handle CRSs for multiple files (see #16)
 * TODO: Handle styles for multiple files (see #16)
 * 
 * @author AlFa
 * @author ETj
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * @author Oscar Fonts
 * 
 * @version 0.1 - date: 11 feb 2007
 * @version 0.2 - date: 25 Apr 2011
 */
public class ShapeFileAction extends BaseAction<EventObject> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShapeFileAction.class);
    
    private final static ShapefileDataStoreFactory SHP_FACTORY= new ShapefileDataStoreFactory();

    public ShapeFileAction(final GeoServerActionConfiguration configuration) throws IOException {
        super(configuration);
    }

    /**
     * 
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {

        listenerForwarder.setTask("config");
        listenerForwarder.started();

        try {
            //
            // Initializing input variables
            //
        	GeoServerActionConfiguration configuration=getConfiguration();
            if (configuration == null) {
                throw new IllegalStateException("ActionConfig is null.");
            }
            
            // how many files do we have?
            final int inputSize = events.size();
            
            // Fetch the first event in the queue.
            // We may have one in these 2 cases:
            // 1) a single event for a .zip file
            // 2) a list of events for a (.shp+.dbf+.shx) collection, plus some other optional files

            final EventObject event = events.peek();

            // the name of the shapefile
            String[] shapeNames;

            // the output (to send to the geoserver) file
            File zippedFile = null;

            // list of file to send to the GeoServer
            File[] files=null;
            File tmpDirFile=null;
            Integer epsgCode=null;
            
            if (inputSize == 1) {
            	//
            	// SINGLE FILE, is a zip or throw error
            	//
                zippedFile = toFile(event);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Testing for compressed file: " + zippedFile);

                // try to extract
                tmpDirFile = Extract.extract(zippedFile,getTempDir(),false);
                listenerForwarder.progressing(5, "File extracted");
                
                //if the output (Extract) file is not a dir the event was a not compressed file so
                //we have to throw and error
                if(tmpDirFile==null){
                    throw new IllegalStateException("Not valid input: we need a zip file ");
                }
                
                if (!tmpDirFile.isDirectory()) {
                    throw new IllegalStateException("Not valid input: we need a zip file ");
                }

                // collect extracted files
                final Collector c = new Collector(FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(tmpDirFile.getName()))); // no filter
                final List<File> fileList = c.collect(tmpDirFile);
                files = fileList.toArray(new File[1]);
                
                // Check if there is at least one shp there
                shapeNames = acceptable(files);
                
                // If not, throw error
                if (shapeNames == null) {
                    final String message = "Input is not a zipped file nor a valid collection of files";
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(message);
                    throw new IllegalStateException(message);
                }
                
            } else {
            	//
            	// Multiple separated files, let's look for the right one
            	//
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("Checking input collection...");

                listenerForwarder.progressing(5, "Checking input collection...");

                // collect files
                files = new File[events.size()];
                int i = 0;                
                for (EventObject ev : events) {
                    files[i++] = toFile(ev);
                }

                // Get tmp dir from the absolute path of the first captured file
                tmpDirFile = new File(FilenameUtils.getFullPath(files[0].getAbsolutePath()));
                
                // Check for shapefile names
                shapeNames = acceptable(files);
                
                // Not found any valid shapefile
                if (shapeNames == null) {
                	throw new IllegalStateException("The file list do not contains mandatory files");
                }
                
                for(String shape: shapeNames) {
                    FileDataStore store = null;
                   
                    try{
                    	store=SHP_FACTORY.createDataStore(new File(tmpDirFile,shape+".shp").toURI().toURL());
                        CoordinateReferenceSystem crs = store.getSchema().getCoordinateReferenceSystem();
                        epsgCode= crs!=null?CRS.lookupEpsgCode(crs, true):null;
                    } finally {
                    	if(store!=null){
                    		try{
                    			store.dispose();
                    		}catch (Exception e) {
        						if(LOGGER.isTraceEnabled()){
        							LOGGER.trace(e.getLocalizedMessage(),e);
        						}
        					}
                    	}
                    }                	
                }
                
                // zip to a single file if method is not external.
                // Will use the first shapeName as the zip name.
                if(!configuration.getDataTransferMethod().equalsIgnoreCase("external")) {
	                zippedFile = Compressor.deflate(getTempDir(),shapeNames[0], files);
	                if (zippedFile == null) {
	                    throw new IllegalStateException("Unable to create the zip file");
	                }
                }

            } 

            listenerForwarder.progressing(10, "In progress");

            // Get GS reader & publisher
            GeoServerRESTReader reader = new GeoServerRESTReader(configuration.getGeoserverURL(),
            		configuration.getGeoserverUID(), configuration.getGeoserverPWD());            
            GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
                    configuration.getGeoserverURL(), configuration.getGeoserverUID(),
                    configuration.getGeoserverPWD());

            // Check if the namespace exists in GS. If not, create it.
            if(!reader.getWorkspaceNames().contains(configuration.getDefaultNamespace())) {
            	if(configuration.getDefaultNamespaceUri() != null) {
            		publisher.createNamespace(configuration.getDefaultNamespace(),
            			new URI(configuration.getDefaultNamespaceUri()));
            	} else {
            		// No uri given; create a workspace
            		publisher.createWorkspace(configuration.getDefaultNamespace());
            	}
            }
            
            // TODO: check if a layer with the same name already exists in GS           
        	// TODO: Handle CRSs for multiple files
        	// TODO: Handle styles for multiple files (see comment on #16)

            //
            // SENDING data to GeoServer via REST protocol.
            //

            // decide CRS
            // default crs
            final String defaultCRS = configuration.getCrs();
            String finalEPSGCode = defaultCRS;
            // retain original CRS if the code is there
            if (epsgCode == null) {
                // we do not have a valid EPSG code
                if (defaultCRS == null) {
                    final String message = "Input file has no CRS neither the configuration provides a default one";
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(message);
                    }
                    if (!configuration.isFailIgnored())
                        throw new ActionException(ShapeFileAction.class, message);
                }
            } else {
                finalEPSGCode = "EPSG:" + epsgCode;
            }
            
            // decide CRS management
            ProjectionPolicy projectionPolicy = ProjectionPolicy.NONE;
            if (defaultCRS == null) {
                // we do not have a valid CRS, we use the default one
                projectionPolicy = ProjectionPolicy.FORCE_DECLARED;
            } else
            // we DO have a valid CRS
            if (epsgCode == null) {
                // we do not have a CRS with a valid EPSG code, let's reproject on
                // the fly
                projectionPolicy = ProjectionPolicy.REPROJECT_TO_DECLARED;
            }
            
            String transferMethod = configuration.getDataTransferMethod();
            if (transferMethod == null) {
                transferMethod = "DIRECT"; // default one
            }
            
            UploadMethod uMethod=null;
            if ("DIRECT".equalsIgnoreCase(transferMethod)) {
                uMethod=UploadMethod.file;
            } else if ("EXTERNAL".equalsIgnoreCase(configuration.getDataTransferMethod())) {
                uMethod=UploadMethod.external;
            } else {
                throw new IllegalArgumentException("Unsupported transfer method: "+configuration.getDataTransferMethod());
            }
            
            // Get some common parameters
            String wsName = configuration.getDefaultNamespace();
            String dsName = configuration.getStoreName() == null? shapeNames[0] : configuration.getStoreName();
            String lyrName = configuration.getLayerName() == null? shapeNames[0] : configuration.getLayerName();
            String styleName = configuration.getDefaultStyle() != null ? configuration.getDefaultStyle() : "polygon";

            // DIRECT Upload is the only supported method
            boolean success = false;
            
            // Either publish a single shapefile, or a collection of shapefiles
            if(shapeNames.length==1) {
            	success = publisher.publishShp(wsName,
                        dsName, 
                        null,
                        lyrName,
                        uMethod,
                        zippedFile.toURI(),
                        finalEPSGCode,
                        projectionPolicy,
                        styleName);
            } else {
            	success = publisher.publishShpCollection(wsName, dsName, zippedFile.toURI());
            }
            
            if (success) {
                final String message = "Shape file SUCCESFULLY sent";
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(message);
                listenerForwarder.progressing(90, message);
            } else {
                final String message = "Shape file FAILED to be sent";
                final ActionException ae = new ActionException(this, message);
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message, ae);
                listenerForwarder.failed(ae);
                throw ae;
            }
            
            // If we have shape specific config, apply now
            if (configuration instanceof GeoServerShapeActionConfiguration) {
            	// Log
                String message = "Configuring shape datastore connection parameters";
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(message);
                
            	// Get config
            	GeoServerShapeActionConfiguration shpConfig = (GeoServerShapeActionConfiguration)configuration;
            	
            	// Get managers from geoserver-manager
            	GeoServerRESTManager manager = new GeoServerRESTManager(new URL(shpConfig.getGeoserverURL()),
            			shpConfig.getGeoserverUID(), shpConfig.getGeoserverPWD());
            	GeoServerRESTDatastoreManager dsManager = manager.getDatastoreManager();
            	
            	// Read config from GS
            	RESTDataStore dsRead = manager.getReader().getDatastore(wsName, dsName);
            	GSShapefileDatastoreEncoder dsWrite = new GSShapefileDatastoreEncoder(dsRead);
            	
            	// Update store params
            	if (shpConfig.getUrl() != null)
            		dsWrite.setUrl(shpConfig.getUrl());
            	if (shpConfig.getCharset() != null)
            		dsWrite.setCharset(shpConfig.getCharset());
            	if (shpConfig.getCreateSpatialIndex() != null)
            		dsWrite.setCreateSpatialIndex(shpConfig.getCreateSpatialIndex());
            	if (shpConfig.getMemoryMappedBuffer() != null)
            		dsWrite.setMemoryMappedBuffer(shpConfig.getMemoryMappedBuffer());
            	if (shpConfig.getCacheAndReuseMemoryMaps() != null)
            	dsWrite.setCacheAndReuseMemoryMaps(shpConfig.getCacheAndReuseMemoryMaps());
            	
            	// Push changes to GS
            	success = dsManager.update(wsName, dsWrite);
            	
            	// Success or die
                if (success) {
                    message = "Shape datastore SUCCESFULLY configured";
                    if (LOGGER.isInfoEnabled())
                        LOGGER.info(message);
                    listenerForwarder.progressing(100, message);
                } else {
                    message = "Shape datastore FAILED to be configured";
                    final ActionException ae = new ActionException(this, message);
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(message, ae);
                    listenerForwarder.failed(ae);
                    throw ae;
                }
            }

            return events;
            
        } catch (Throwable t) {
            final ActionException ae = new ActionException(this, t.getMessage(), t);
            if (LOGGER.isErrorEnabled())
                LOGGER.error(ae.getLocalizedMessage(), ae);
            listenerForwarder.failed(ae); // fails the Action
            throw ae;
        }
    }

    /**
     * check for mandatory files in the passed list:
     * <ul>
     * <li>.shp — shape format; the feature geometry itself
     * <li>.dbf — attribute format; columnar attributes for each shape, in dBase IV
     * format
     * <li>.shx — shape index format; a positional index of the feature geometry to
     * allow seeking forwards and backwards quickly
     * </ul>
     * 
     * There can be multiple shapefiles.
     * 
     * @param files a list of files to check for.
     * @return null if 'files' do not contain needed files,
     * or the name of the acceptable shape files (*.shp) otherwise.
     */
    private static String[] acceptable(final File[] files) {
    	
        if (files == null) {
            return null;
        }
        
        // Placeholders for all candidates and acceptable ones
        List<String> candidates = new ArrayList<String>();
        List<String> acceptable = new ArrayList<String>();
        
        // Get all the candidate file names
        for(File file : files) {
        	if(file != null) {
        		candidates.add(FilenameUtils.getName(file.getName()));
        	}
        }
        
        // Get the acceptable ones.
        // That is: a .shp for what .shx and .dbf associate files exist
        for(String fileName : candidates) {
        	final String baseName = FilenameUtils.getBaseName(fileName);
        	final String extension = FilenameUtils.getExtension(fileName);
        	if (extension.equals("shp") &&
        		//candidates.contains(baseName+".shx") && // is index really mandatory?
        		candidates.contains(baseName+".dbf")) {
        			acceptable.add(baseName);
    		}
    	}

        // Return acceptable as an array, or null if none.
        if (acceptable.isEmpty()) {
        	return null;
        } else {
        	return acceptable.toArray(new String[1]);
        }
    }
    
    private File toFile(EventObject eo) {
    	Object o = eo.getSource();
    	if(o instanceof File) {
    		return (File)o;
    	} else {
    		return null;
    	}
    }
}
