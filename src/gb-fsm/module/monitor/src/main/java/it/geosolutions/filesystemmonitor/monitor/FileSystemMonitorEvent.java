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
package it.geosolutions.filesystemmonitor.monitor;

import java.io.File;
import java.util.EventObject;

/**
 * @author Alessio Fabiani, GeoSolutions
 */
public class FileSystemMonitorEvent extends EventObject {


    /**
     * 
     */
    private static final long serialVersionUID = 7915893220009824087L;

    /**
     * @uml.property name="notification"
     */
    private final FileSystemMonitorNotifications notification;

    private final long timestamp;

    public FileSystemMonitorEvent(File source, FileSystemMonitorNotifications notification) {
        super(source);
        this.timestamp = System.currentTimeMillis();
        this.notification = notification;
    }

    /**
     * @return Returns the notification.
     * @uml.property name="notification"
     */
    public FileSystemMonitorNotifications getNotification() {
        return notification;
    }

    @Override
    public File getSource() {
        return (File) super.getSource();
    }

    public long getTimestamp() {
        return timestamp;
    }
    


    @Override
    public String toString() {
        return "FileSystemMonitorEvent [notification=" + notification + ", timestamp=" + timestamp
                + ", source=" + source + "]";
    }
}