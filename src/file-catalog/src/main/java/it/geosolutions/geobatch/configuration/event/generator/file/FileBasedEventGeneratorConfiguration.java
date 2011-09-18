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
public class FileBasedEventGeneratorConfiguration extends EventGeneratorConfiguration implements Configuration
{

    /**
     * The type of OS which will be used by the embedded File System Watcher.
     */
    private OsType osType;

    /**
     * The type of File System Event accepted by the generator. The events can
     * be of kind FILE_ADDED, FILE_REMOVED, FILE_MODIFIED, etc...
     */
    private FileSystemEventType eventType;

    private FileSystemMonitorType monitorType;

    /**
     * The configuring directory.
     */
    private String watchDirectory;

    /**
     * A flag used to keep files in watchDirectory when flow is started.
     */
    private boolean keepFiles = false;

    /**
     * The wild-card used to catch the kind of input files.
     */
    private String wildCard;

    /**
     * The polling interval in millisec can be: - a cron string - an integer -
     * null
     */
    private String interval;

    /**
     *
     * @param id
     * @param name
     * @param description
     * @param dirty
     * @param osType
     * @param eventType
     * @param watchDirectory
     * @param interval
     * @param wildCard
     * @param keepFiles
     */
    public FileBasedEventGeneratorConfiguration(String id, String name,
        String description, boolean dirty, OsType osType,
        FileSystemEventType eventType, String watchDirectory,
        String interval, String wildCard, boolean keepFiles)
    {
        super(id, name, description, dirty);
        this.osType = osType;
        this.eventType = eventType;
        this.watchDirectory = watchDirectory;
        this.interval = interval;
        this.wildCard = wildCard;
        this.keepFiles = keepFiles;
    }

    public void setWatchDirectory(String watchDirectory)
    {
        this.watchDirectory = watchDirectory;
    }

    /**
     * @return
     */
    public FileSystemMonitorType getMonitorType()
    {
        return monitorType;
    }

    /**
     * @param monitorType
     */
    public void setMonitorType(FileSystemMonitorType monitorType)
    {
        this.monitorType = monitorType;
    }

    public String getInterval()
    {
        return interval;
    }

    public void setInterval(String interval)
    {
        this.interval = interval;
    }

    public String getWatchDirectory()
    {
        return watchDirectory;
    }

    /**
     * Getter for the OS type attribute.
     *
     * @return osType
     */
    public OsType getOsType()
    {
        return osType;
    }

    /**
     * Setter for the OS type attribute.
     *
     * @param osType
     */
    public void setOsType(OsType osType)
    {
        this.osType = osType;
    }

    /**
     * Getter for the wild card attribute.
     *
     * @return wildCard
     */
    public String getWildCard()
    {
        return wildCard;
    }

    /**
     * Setter for the wild card attribute.
     *
     * @param wildCard
     */
    public void setWildCard(String wildCard)
    {
        this.wildCard = wildCard;
    }

    /**
     * Getter for the event type attribute.
     *
     * @return eventType
     */
    public FileSystemEventType getEventType()
    {
        return eventType;
    }

    /**
     * Setter for the event type attribute.
     *
     * @param eventType
     */
    public void setEventType(FileSystemEventType eventType)
    {
        this.eventType = eventType;
    }

    /**
     * Getter for the keep files in watchDirectory flag.
     *
     * @return keepFiles
     */
    public boolean getKeepFiles()
    {
        return keepFiles;
    }

    /**
     * Setter for the keep files in watchDirectory flag.
     *
     * @param keepFiles
     */
    public void setKeepFiles(boolean keepFiles)
    {
        this.keepFiles = keepFiles;
    }

}
