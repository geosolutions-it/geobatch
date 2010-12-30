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

package it.geosolutions.geobatch.compose;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.base.Utils;
import it.geosolutions.geobatch.base.Utils.FolderContentType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.geoserver.matfile5.sas.SasMosaicGeoServerGenerator;
import it.geosolutions.geobatch.mosaic.Mosaicer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class Composer
	extends ComposerConfiguratorAction<FileSystemMonitorEvent> {

	/**
	 * 
	 * @param configuration
	 */
    public Composer(ComposerConfiguration configuration) throws IOException {
		super(configuration);
	}

    /**
     * 
     * @param events
     * @return
     * @throws ActionException
     */
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events) 
       throws ActionException {
        try {
            listenerForwarder.started();

            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "+ events.size());
            
           
            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            
            setJAIHints(configuration);
            
            // get the first event
            final FileSystemMonitorEvent event = events.remove();
            final File inputFile = event.getSource();
            
            // //
            // Get the directory containing the data from the specified
            // XML file
            // //
            final List<String> missionDirs = Utils.getDataDirectories(inputFile, FolderContentType.LEGS);
            
            if (missionDirs==null || missionDirs.isEmpty()){
            	LOGGER.warning("Unable to find LegData location from the specified file: "+inputFile.getAbsolutePath());
            	return events;
            }
            final int nMissions = missionDirs.size();
            if (LOGGER.isLoggable(Level.INFO))
            	LOGGER.info(new StringBuilder("Found ").append(nMissions).append(" mission").append(nMissions>1?"s":"").toString());
            
            for (String mission : missionDirs){
            	String initTime = null;
            	if (LOGGER.isLoggable(Level.INFO))
                	LOGGER.info("Processing Mission: " + mission);
            	
            	final String directory = mission;
            	
	            // Preparing parameters
	            final double compressionRatio = configuration.getCompressionRatio();
	            final String compressionScheme = configuration.getCompressionScheme();
	            final String inputFormats = configuration.getInputFormats();
	            final String outputFormat = configuration.getOutputFormat();
	            final int downsampleStep = configuration.getDownsampleStep();
	            final int numSteps = configuration.getNumSteps();
	            final String rawScaleAlgorithm = configuration.getRawScaleAlgorithm();
	            final String mosaicScaleAlgorithm = configuration.getMosaicScaleAlgorithm();
	            final int tileH = configuration.getTileH();
	            final int tileW = configuration.getTileW();
	            final int chunkW = configuration.getChunkW();
	            final int chunkH = configuration.getChunkH();
	            final String baseDir = configuration.getOutputBaseFolder();
	            
	            //TODO: Refactor this search to leverage on a PATH_DEPTH parameter.
	            //Actually is looking for specifiedDir/dirdepth1/dirdepth2/
	            
	            // //
	            //
	            // Checking LEGS for the current MISSION
	            //
	            // //
	            ArrayList<File> directories = null;
	            final File fileDir = new File(directory); //Mission dir
	            if (fileDir != null && fileDir.isDirectory()) {
	                final File[] foundFiles = fileDir.listFiles();
	                if (foundFiles != null && foundFiles.length>0){
	                    directories = new ArrayList<File>();
	                    for (File file : foundFiles){
	                        if (file.exists() && file.isDirectory()){
	                            directories.add(file);
	                        }
	                    }
	                }
	            }
	            
	            // //
	            //
	            // Mission Scan: Looking for LEGS
	            //
	            // //
	            if (directories != null && !directories.isEmpty()){
	                Collections.sort(directories);
	                final String leavesFolders = configuration.getLeavesFolders();
	                final String leaves[] = leavesFolders.split(";");
	                if (leaves != null){
	                    final List<String> leavesArray = Arrays.asList(leaves);
	                    final Set<String> leavesSet = new HashSet<String>(leavesArray);
	                    
	                    // //
	                    //
	                    // Leg Scan
	                    //
	                    // //
	                    for (File legDir : directories){
	                        if (legDir.isDirectory()){
	                            final File subFolders[] = legDir.listFiles();
	                            if (subFolders != null){
	                            	
	                            	// //
	                            	//
	                            	// Channel scan (leaves)
	                            	//
	                            	// //
	                                for (int i=0; i<subFolders.length; i++){
	                                    final File leaf = subFolders[i];
	                                    final String leafName = leaf.getName();
	                                    if (leavesSet.contains(leafName)){
	                                      
	                                      final String leafPath = leaf.getAbsolutePath();
	                                      
	                                      // Initialize time
	                                      if (initTime == null){
	                                          initTime = Utils.setInitTime(leafPath,2);
	                                      }
	                                      
	                                      //Build the output directory path
	                                      final StringBuilder outputDir = new StringBuilder(baseDir)
	                                      .append(Utils.SEPARATOR).append(initTime).append(Utils.SEPARATOR)
	                                      .append(fileDir.getName()).append(Utils.SEPARATOR)
	                                      .append(legDir.getName()).append(Utils.SEPARATOR)
	                                      .append(leafName);
	                                      
	                                      // //
	                                      //
	                                      // 1) Mosaic Composition
	                                      //
	                                      // //
	                                      final String mosaicTobeIngested = composeMosaic(events,leafPath,outputDir.toString(), compressionRatio, compressionScheme,
	                                              inputFormats, outputFormat, tileW, tileH, numSteps, downsampleStep, rawScaleAlgorithm, mosaicScaleAlgorithm,
	                                              chunkW, chunkH, initTime, configuration.getGeoserverURL(),configuration.getGeoserverUID(),configuration.getGeoserverPWD(),
	                                              configuration.getGeoserverUploadMethod(), configuration.getCorePoolSize(), configuration.getMaxPoolSize(), 
	                                              configuration.getMaxWaitingTime(), configuration.getGeowebcacheWatchingDir());
	                                      
	                                      // //
	                                      //
	                                      // 2) Mosaic Ingestion
	                                      //
	                                      // //
	                                      if (mosaicTobeIngested != null && mosaicTobeIngested.trim().length()>0){
		                                      final String style = SasMosaicGeoServerGenerator.SAS_STYLE;
		                                      final int index = mosaicTobeIngested.lastIndexOf(Mosaicer.MOSAIC_PREFIX);
		                                            
		                                      //Setting up the wmspath.
		                                      //Actually it is set by simply changing mosaic's name underscores to slashes.
		                                      //TODO: can be improved
		                                      final String path = mosaicTobeIngested.substring(index + Mosaicer.MOSAIC_PREFIX.length());
		                                      final String wmsPath = SasMosaicGeoServerGenerator.buildWmsPath(path);
		                                      
		                                      SasMosaicGeoServerGenerator.ingest(mosaicTobeIngested, wmsPath,configuration.getGeoserverURL(),configuration.getGeoserverUID()
		                                    		  ,configuration.getGeoserverPWD(),configuration.getGeoserverUploadMethod(), style, "imagemosaic"
		                                    		  ,configuration.getGeowebcacheWatchingDir());
		                                      
		                                      //TODO: Add geowebcache ingestion
	                                      	}
	                                      	else{
	                                      		if (LOGGER.isLoggable(Level.WARNING))
	                                      			LOGGER.warning("unable to build a mosaic for the following dataset:" + leafPath);
	                                      	}
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	            }
            }
            listenerForwarder.completed();
            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }

    }

}
