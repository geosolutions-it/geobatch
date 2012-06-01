/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.filesystemmonitor.monitor;

import java.io.File;
import java.util.EventObject;

/**
 * @author Alessio Fabiani, GeoSolutions
 */
public class FileSystemEvent extends EventObject {


    /**
     * 
     */
    private static final long serialVersionUID = 7915893220009824087L;

    
    private final FileSystemEventType eventType;

    
    private final long timestamp;

    public FileSystemEvent(File source, FileSystemEventType eventType) {
        super(source);
        this.timestamp = System.currentTimeMillis();
        this.eventType = eventType;
    }

    /**
     * @return Returns the eventType.
     * @uml.property name="eventType"
     */
    public FileSystemEventType getEventType() {
        return eventType;
    }

    @Override
    public File getSource() {
        return (File) super.getSource();
    }

    /**
     * @return
     * @uml.property  name="timestamp"
     */
    public long getTimestamp() {
        return timestamp;
    }
    


    @Override
    public String toString() {
        return "FileSystemEvent [eventType=" + eventType + ", timestamp=" + timestamp
                + ", source=" + source + "]";
    }
}