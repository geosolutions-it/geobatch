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
package it.geosolutions.geobatch.base;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.geotiff.overview.GeoTiffOverviewsEmbedder;
import it.geosolutions.geobatch.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration;
import it.geosolutions.geobatch.task.TaskExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.imageio.stream.FileImageInputStream;

public class Utils {

	public static final String LEG_PREFIX = "_Leg";

    public static String MISSION_LEGS_LOCATION = "<missionLegsLocation>";

    public static String MISSION_LEGS_LOCATION_END = "</missionLegsLocation>";
	
    public static String MISSION_DETECTIONS_LOCATION = "<missionDetectionsLocation>";

    public static String MISSION_DETECTIONS_LOCATION_END = "</missionDetectionsLocation>";

    public enum FolderContentType{
    	LEGS, DETECTIONS;
    }
    
    
    private final static Logger LOGGER = Logger.getLogger(TaskExecutor.class.toString());
    
	/**
	 * Build a proper run name.
	 * 
	 * @param location
	 * @param time
	 * @param prefix
	 * @return
	 */
	public static String buildRunName(final String location, final String time, final String prefix){
    	String dirName = "";
    	final File dir = new File(location);
         final String channelName = dir.getName();
         final String leg = dir.getParent();
         final File legF = new File(leg);
         final String legName = legF.getName();
         final String mission = legF.getParent();
         final File missionF = new File(mission);
//         final String missionName = missionF.getName();
         String missionName = missionF.getName();
         final int missionIndex = missionName.lastIndexOf("_");
         if (missionIndex!=-1){
        	 final String missionCollapsed = missionName.substring(0,missionIndex).replace("_", "-");
             missionName = new StringBuilder("mission").append(missionCollapsed).append(missionName.substring(missionIndex+1)).toString();
         }
         else {
        	 missionName = new StringBuilder("mission").append(missionName).toString();
         }
        	 
         dirName = new StringBuilder(location).append(Utils.SEPARATOR).append(prefix)
         .append(time).append("_")
         .append(missionName).append(LEG_PREFIX)
         .append(legName.substring(3,legName.length())).append("_")
         .append(channelName).toString();
         return dirName;
    }
	
	/**
     * Find Data directories from the specified input file.
     * @param xmlFile
     * @return
     */
    public static List<String> getDataDirectories(final File xmlFile, final FolderContentType type){
    	//TODO: Improve me, leveraging on real XML (DOM)
    	String startTag = null;
    	String endTag = null;
    	switch (type){
    	case LEGS:
    		startTag = MISSION_LEGS_LOCATION;
    		endTag = MISSION_LEGS_LOCATION_END;
    		break;
    	case DETECTIONS:
    		startTag = MISSION_DETECTIONS_LOCATION;
    		endTag = MISSION_DETECTIONS_LOCATION_END;
    		break;
    	}
    	
    	final List<String> directories = new ArrayList<String>();
        String dataDir = null;
        if (xmlFile!=null){
            try {
                final FileImageInputStream fis = new FileImageInputStream(xmlFile);
                String location=null;
                while ((location = fis.readLine()) != null){
                    if (location.startsWith(startTag)){
                    	if (location.endsWith(endTag)){
                    		dataDir=location.substring(location.indexOf(startTag)+startTag.length(), location.length()-(endTag.length())).trim();
                    	}
                    	else{
                    		String next = fis.readLine();
                    		if (next!=null){
                            	if (next.endsWith(endTag)){
                            		dataDir=next.substring(0, next.length()-(endTag.length())).trim();
                            	}
                            	else{
                            		String nextLine = fis.readLine();
                            		if (nextLine!=null){
                            			dataDir = next.trim();
                            		}
                            		else{
	                           			 LOGGER.warning("Unable to find missions");
	                           			 return directories;
                            		}
                            	}
                    		}
                    		else{
                    			 LOGGER.warning("Unable to find missions");
                    			 return directories;
                    		}
                    		
                    	}
                    	
                        directories.add(dataDir);
                    }
                }
                
            } catch (FileNotFoundException e) {
                LOGGER.warning("Unable to find the specified file: " + xmlFile);
            } catch (IOException e) {
                LOGGER.warning(new StringBuilder("Problems occurred while reading: ")
                .append(xmlFile).append("due to ").append(e.getLocalizedMessage()).toString());
            }
        }
        return directories;
    }

    /**
     * Set the time of this Mission
     * 
     * @param leafPath
     */
    public static String setInitTime(final String dirPath, final int stripCount) {
        //TODO: improve ME
        //actually, get this time from the file name
        //next step is acquiring it from the matlab file, when also producing XML files
        String initTime = null;
        final File fileDir = new File(dirPath);
        boolean found = false;
        if (fileDir != null && fileDir.isDirectory()) {
            final File files[] = fileDir.listFiles();
            List<File> filesArray = Arrays.asList(files);
            Collections.sort(filesArray);
            final File file = filesArray.get(0);
           
            if (file!=null){
                final String fileName = file.getName();
                String date = fileName;
                int index=0;
                
                //Files are named like this:
                //muscle_col2_090316_1_2_p_5790_5962_40_150.tif
                
                for (int i=0;i<stripCount&&index!=-1;i++){
                    index = date.indexOf("_");
                    date = date.substring(index+1, date.length());
                }
                if (index!=-1){
                    final int indexOf = date.indexOf("_");
                    if (indexOf!=-1){
                        initTime = date.substring(0,indexOf);
                        found = true;
                    }
                }
            }
        }
        if(!found)
            initTime = new SimpleDateFormat("yyyyMMdd").format(new Date());
        //Current time in case it's unable to find it from the file
        return initTime;
    }
    
    /**
     * Build the proper directories hierarchy.
     * 
     * @param outputDirectory
     *                the path of the output dir to be built
     */
    public static synchronized void makeDirectories(final String outputDirectory) {
        final File makeDir = new File(outputDirectory);
        
        // Recursive check. back to the parents until a folder already exists.
        if (!makeDir.exists() || !makeDir.isDirectory()) {
            makeDirectories(makeDir.getParent());
            makeDir.mkdir();
        }
    }
    
	/**
     * Add overviews to the specified file
     * 
     * @param fileName
	 * @param gtovConfiguration 
     * @throws Exception 
     */
    public static void addOverviews(final String fileName, final GeoTiffOverviewsEmbedderConfiguration gtovConfiguration) throws Exception {
    	if (gtovConfiguration == null)
    		throw new IllegalArgumentException("Null GeoTiffOverviewsEmbedderConfiguration provided");
    	if (fileName == null || fileName.trim().length()<1)
    		throw new IllegalArgumentException("Invalid filepath");
    	final GeoTiffOverviewsEmbedder embedder = new GeoTiffOverviewsEmbedder(gtovConfiguration);
		final Queue<FileSystemMonitorEvent> events = new LinkedBlockingQueue<FileSystemMonitorEvent>();
		final FileSystemMonitorEvent fse = new FileSystemMonitorEvent(new File(fileName), FileSystemMonitorNotifications.FILE_ADDED);
		events.add(fse);
		embedder.execute(events);
    }
	
	public static final char SEPARATOR = File.separatorChar;

}
