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

package it.geosolutions.geobatch.detection;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.base.Utils;
import it.geosolutions.geobatch.base.Utils.FolderContentType;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Action which allows to run a script to convert a detection file to a shapefile 
 * and ingest it on geoserver via rest
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class DetectionManager extends DetectionManagerConfiguratorAction<FileSystemMonitorEvent> {

	private final static Logger LOGGER = Logger.getLogger(DetectionManager.class.toString());

	/**
	 * 
	 * @param configuration
	 */
    public DetectionManager(DetectionManagerConfiguration configuration) throws IOException {
		super(configuration);
	}

    /**
     * 
     */
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events) 
		throws ActionException {
    	try {
    		listenerForwarder.started();
            
            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "+ events.size());
            
            if (this.configuration == null) {
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            
            final FileSystemMonitorEvent event = events.remove();
            final File inputFile = event.getSource();
            
            // //
            //
            // Get the directory containing the data from the specified
            // XML file
            //
            // //
            final List<String> missionDirs = Utils.getDataDirectories(inputFile, FolderContentType.DETECTIONS);
            
            if (missionDirs==null || missionDirs.isEmpty()){
            	LOGGER.warning("Unable to find target data location from the specified file: "+inputFile.getAbsolutePath());
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
	            final File fileDir = new File(directory); //Mission dir
	            if (fileDir != null && fileDir.isDirectory()) {
	                final File[] foundFiles = fileDir.listFiles(FILEFILTER);
	                if (foundFiles != null && foundFiles.length>0){
	                	initTime = Utils.setInitTime(directory,2);
	                    final String subDir = buildDetectionsSubDir(initTime, fileDir);
	                    ingestDetection(fileDir, subDir);
	                }
	            }
	            if (LOGGER.isLoggable(Level.INFO))
	            	LOGGER.info("Done");
            }            
            if (LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("End Of processing");
            
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
