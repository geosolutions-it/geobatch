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

package it.geosolutions.geobatch.configuration.event.generator.file;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorType;
import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;

/**
 * Conf for the event generators based on xml marshalled files.
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class FileBasedEventGeneratorConfiguration extends EventGeneratorConfiguration implements
        Configuration {

    /**
     * The type of OS which will be used by the embedded File System Watcher.
     * @uml.property  name="osType"
     * @uml.associationEnd  
     */
    private OsType osType;

    /**
     * The type of File System Event accepted by the generator. The events can be of kind FILE_ADDED, FILE_REMOVED, FILE_MODIFIED, etc...
     * @uml.property  name="eventType"
     * @uml.associationEnd  
     */
    private FileSystemEventType eventType;
    

    /**
     * @uml.property  name="monitorType"
     * @uml.associationEnd  
     */
    private FileSystemMonitorType monitorType;

    /**
     * The configuring directory.
     * @uml.property  name="watchDirectory"
     */
    private String watchDirectory;

    /**
     * A flag used to keep files in watchDirectory when flow is started.
     * @uml.property  name="keepFiles"
     */
    private boolean keepFiles = false;

    /**
     * The wild-card used to catch the kind of input files.
     * @uml.property  name="wildCard"
     */
    private String wildCard;


    /**
     * @return
     * @uml.property  name="monitorType"
     */
    public FileSystemMonitorType getMonitorType() {
        return monitorType;
    }

    /**
     * @param monitorType
     * @uml.property  name="monitorType"
     */
    public void setMonitorType(FileSystemMonitorType monitorType) {
        this.monitorType = monitorType;
    }
    
    /**
     * Default Constructor.
     */
    public FileBasedEventGeneratorConfiguration() {
        super();
    }

    /**
     * 
     * @param id
     * @param name
     * @param description
     * @param dirty
     * @param osType
     * @param eventType
     * @param workingDirectory
     * @param wildCard
     */
    public FileBasedEventGeneratorConfiguration(String id, String name, String description,
            boolean dirty, OsType osType, FileSystemEventType eventType,
            String workingDirectory, String wildCard) {
        super(id, name, description, dirty);
        this.osType = osType;
        this.eventType = eventType;
        this.watchDirectory = workingDirectory;
        this.wildCard = wildCard;
    }

    /**
     * 
     * @param id
     * @param name
     * @param description
     * @param dirty
     * @param osType
     * @param eventType
     * @param workingDirectory
     * @param wildCard
     * @param keepFiles
     */
    public FileBasedEventGeneratorConfiguration(String id, String name, String description,
            boolean dirty, OsType osType, FileSystemEventType eventType,
            String workingDirectory, String wildCard, boolean keepFiles) {
        super(id, name, description, dirty);
        this.osType = osType;
        this.eventType = eventType;
        this.watchDirectory = workingDirectory;
        this.wildCard = wildCard;
        this.keepFiles = keepFiles;
    }

    /**
     * Getter for the OS type attribute.
     * @return  osType
     * @uml.property  name="osType"
     */
    public OsType getOsType() {
        return osType;
    }

    /**
     * Setter for the OS type attribute.
     * @param  osType
     * @uml.property  name="osType"
     */
    public void setOsType(OsType osType) {
        this.osType = osType;
    }

    /**
     * Getter for the configuring directory attribute.
     * 
     * @return workinfDirectory
     */
    public String getWorkingDirectory() {
        return watchDirectory;
    }

    /**
     * Setter for the configuring directory attribute.
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.watchDirectory = workingDirectory;
    }

    /**
     * Getter for the wild card attribute.
     * @return  wildCard
     * @uml.property  name="wildCard"
     */
    public String getWildCard() {
        return wildCard;
    }

    /**
     * Setter for the wild card attribute.
     * @param  wildCard
     * @uml.property  name="wildCard"
     */
    public void setWildCard(String wildCard) {
        this.wildCard = wildCard;
    }

    /**
     * Getter for the event type attribute.
     * @return  eventType
     * @uml.property  name="eventType"
     */
    public FileSystemEventType getEventType() {
        return eventType;
    }

    /**
     * Setter for the event type attribute.
     * @param  eventType
     * @uml.property  name="eventType"
     */
    public void setEventType(FileSystemEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Getter for the keep files in watchDirectory flag.
     * 
     * @return keepFiles
     */
    public boolean getKeepFiles() {
        return keepFiles;
    }

    /**
     * Setter for the keep files in watchDirectory flag.
     * @param  keepFiles
     * @uml.property  name="keepFiles"
     */
    public void setKeepFiles(boolean keepFiles) {
        this.keepFiles = keepFiles;
    }

}