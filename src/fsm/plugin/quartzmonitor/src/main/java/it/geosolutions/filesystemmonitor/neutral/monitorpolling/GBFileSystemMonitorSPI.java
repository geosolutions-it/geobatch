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
package it.geosolutions.filesystemmonitor.neutral.monitorpolling;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorType;
import it.geosolutions.geobatch.tools.file.IOUtils;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBFileSystemMonitorSPI implements FileSystemMonitorSPI {
    private final static Logger LOGGER = LoggerFactory.getLogger(GBFileSystemMonitorSPI.class.toString());

    public final static long DEFAULT_MAX_LOOKING_INTERVAL = IOUtils.MAX_WAITING_TIME_FOR_LOCK; // milliseconds

    public boolean canWatch(OsType osType) {
        return true;
    }

    public boolean isAvailable() {
        return true;
    }

    public GBFileSystemMonitor createInstance(Map<String, ?> configuration) {
        // get the params
        // polling interval
        String interval = null;

        // file
        File file = null;

        // wildcard
        String wildcard = null;

        // event type
        FileSystemEventType type = null;

        try {
            Object element = configuration.get(FileSystemMonitorSPI.INTERVAL_KEY);
            if (element != null && element.getClass().isAssignableFrom(String.class))
                interval = (String) element;

            element = configuration.get(FileSystemMonitorSPI.SOURCE_KEY);
            if (element != null && element.getClass().isAssignableFrom(File.class))
                file = (File) element;

            element = configuration.get(FileSystemMonitorSPI.WILDCARD_KEY);
            if (element != null && element.getClass().isAssignableFrom(String.class))
                wildcard = (String) element;

            element = configuration.get(FileSystemMonitorSPI.TYPE_KEY);
            if (element != null && element.getClass().isAssignableFrom(FileSystemEventType.class))
                type = (FileSystemEventType) element;
        } catch (NullPointerException npe) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Exception during FileSystemWatcher instantiation: "
                                + npe.getLocalizedMessage(), npe);
        }
        // checks
        if (!file.exists() || !file.canRead())
            return null;
        try {
            if (wildcard != null) {
                return new GBFileSystemMonitor(file.getAbsolutePath(), wildcard, type,
                        interval, true, DEFAULT_MAX_LOOKING_INTERVAL);
            }
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Exception during FileSystemWatcher instantiation: "
                                + e.getLocalizedMessage(), e);
        }

        return null;
    }

    public FileSystemMonitorType getType() {
        return FileSystemMonitorType.DEFAULT;
    }

}
