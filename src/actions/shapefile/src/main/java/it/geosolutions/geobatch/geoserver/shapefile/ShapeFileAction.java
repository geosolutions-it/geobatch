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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.UploadMethod;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.tools.compress.file.Compressor;
import it.geosolutions.tools.compress.file.Extract;
import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.io.IOException;
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
 * 
 * @author AlFa
 * @author ETj
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version 0.1 - date: 11 feb 2007
 * @version 0.2 - date: 25 Apr 2011
 */
public class ShapeFileAction extends BaseAction<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShapeFileAction.class);
    
    private final static ShapefileDataStoreFactory SHP_FACTORY= new ShapefileDataStoreFactory();

    public ShapeFileAction(final GeoServerActionConfiguration configuration) throws IOException {
        super(configuration);
    }

    /**
     * 
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {

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
            // 2) a list of events for the .shp+.dbf+.shx+ some other optional
            // files

            final FileSystemEvent event = events.peek();

            // the name of the shapefile
            String shapeName = null;

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
                zippedFile = event.getSource();
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
                if (fileList != null) {
                    files = fileList.toArray(new File[1]);
                } else {
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
                boolean found=false;
                for (FileSystemEvent ev : events) {
                    files[i++] = ev.getSource();
                    if(FilenameUtils.getExtension(files[i-1].getAbsolutePath()).equalsIgnoreCase("shp")){
                    	
                    	// is this a real shapefile?
                    	found=true;
                    	
                    	// is it a shape?
                        //
                        // check the original CRS
                        //
                        FileDataStore store = null;
                       
                        try{
                        	store=SHP_FACTORY.createDataStore(new File(tmpDirFile,shapeName+".shp").toURI().toURL());
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
                }
                // found any valid shapefile?
                if(!found){
                	 throw new IllegalStateException("Unable to create the zip file");
                }
                
                // zip to a single file if method is not external
                if(!configuration.getDataTransferMethod().equalsIgnoreCase("external")) {
	                zippedFile = Compressor.deflate(getTempDir(),shapeName, files);
	                if (zippedFile == null) {
	                    throw new IllegalStateException("Unable to create the zip file");
	                }
                }

            } 
            // obtain the shape file name and check for mandatory file
            if ((shapeName = acceptable(files)) == null) {
                throw new IllegalStateException("The file list do not contains mandatory files");
            }
            listenerForwarder.progressing(10, "In progress");

            // TODO: check if the store do not exists and if so create it
            // TODO: check if a layer with the same name already exists in GS
            // GeoServerRESTReader reader = new GeoServerRESTReader(configuration.getGeoserverURL(),
            // configuration.getGeoserverUID(), configuration.getGeoserverPWD());

            //
            // SENDING data to GeoServer via REST protocol.
            //
            GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
                    configuration.getGeoserverURL(), configuration.getGeoserverUID(),
                    configuration.getGeoserverPWD());
            
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
            // DIRECT Upload is the only supported method
            if (publisher.publishShp(configuration.getDefaultNamespace(),
                                     configuration.getStoreName() == null? shapeName : configuration.getStoreName(), 
                                     null,
                                     configuration.getLayerName() == null? shapeName : configuration.getLayerName(),
                                     uMethod,
                                     zippedFile.toURI(),
                                     finalEPSGCode,
                                     projectionPolicy,
                                     configuration.getDefaultStyle() != null ? configuration.getDefaultStyle() : "polygon" )) {
                final String message = "Shape file SUCCESFULLY sent";
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(message);
                listenerForwarder.progressing(100, message);
            } else {
                final String message = "Shape file FAILED to be sent";
                final ActionException ae = new ActionException(this, message);
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message, ae);
                listenerForwarder.failed(ae);
                throw ae;
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
     * check for mandatory files in the passed list: .shp — shape format; the feature geometry
     * itself <br>
     * .shx — shape index format; a positional index of the feature geometry to allow seeking
     * forwards and backwards quickly <br>
     * .dbf — attribute format; columnar attributes for each shape, in dBase IV format
     * 
     * 
     * @param files
     *            a list of file to check for
     * @return null if 'files' do not contain needed files or contain more than 1 shape file, the
     *         name of the shape file otherwise.
     */
    private static String acceptable(final File[] files) {
        if (files == null)
            return null;

        String shapeFileName = null;
        // if ==3 the incoming file list is acceptable
        int acceptable = 0;
        for (File file : files) {
            if (file == null)
                continue;
            final String ext = FilenameUtils.getExtension(file.getAbsolutePath());

            if (ext.equals("shp")) {
                ++acceptable;
                /*
                 * check if there are more than 1 shp file in the list if so an error occur
                 */
                if (shapeFileName == null)
                    shapeFileName = FilenameUtils.getBaseName(file.getName());
                else
                    return null;
            } else if (ext.equals("shx")) {
                ++acceptable;
            } else if (ext.equals("dbf")) {
                ++acceptable;
            }
        }

        if (acceptable == 3) {
            return shapeFileName;
        } else
            return null;
    }

}
