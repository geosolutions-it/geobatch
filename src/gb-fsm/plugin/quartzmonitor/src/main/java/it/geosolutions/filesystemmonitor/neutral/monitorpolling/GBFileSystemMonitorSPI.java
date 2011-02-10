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
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorType;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GBFileSystemMonitorSPI implements FileSystemMonitorSPI {
    private final static Logger LOGGER = Logger.getLogger(GBFileSystemMonitorSPI.class.toString());

    public final static long DEFAULT_POLLING_INTERVAL = 1000; // milliseconds
    public final static long DEFAULT_MAX_LOOKING_INTERVAL = 10000; // milliseconds
    
    private final static String INTERVAL_KEY= "interval";

    public boolean canWatch(OsType osType) {
        return true;
    }

    public boolean isAvailable() {
        return true;
    }

    public GBFileSystemMonitor createInstance(Map<String, ?> configuration) {
        // get the params
        // polling interval
        Long interval = null;

        // file
        File file = null;

        // wildcard
        String wildcard = null;
        try{
            Object element = configuration.get(INTERVAL_KEY);
            if (element != null && element.getClass().isAssignableFrom(Long.class))
                interval = (Long) element;
    
            element = configuration.get(SOURCE);
            if (element != null && element.getClass().isAssignableFrom(File.class))
                file = (File) element;
    
            element = configuration.get(WILDCARD);
            if (element != null && element.getClass().isAssignableFrom(String.class))
                wildcard = (String) element;
        }
        catch (NullPointerException npe){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Exception during FileSystemWatcher instantiation: "+npe.getLocalizedMessage());
        }
        // checks
        if (!file.exists() || !file.canRead())
            return null;
        try{
            if (wildcard != null){
                if (interval!=null){
                    if (interval != null && interval > 0)
                        return new GBFileSystemMonitor(
                                file.getAbsolutePath(), wildcard, interval, true, DEFAULT_MAX_LOOKING_INTERVAL);
                    else
                        return new GBFileSystemMonitor(
                                file.getAbsolutePath(), wildcard, DEFAULT_POLLING_INTERVAL, true, DEFAULT_MAX_LOOKING_INTERVAL);
                }
                else {
                        return new GBFileSystemMonitor(
                                file.getAbsolutePath(), wildcard, DEFAULT_POLLING_INTERVAL, true, DEFAULT_MAX_LOOKING_INTERVAL);
                }
            }
        }
        catch (Exception e){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Exception during FileSystemWatcher instantiation: "+e.getLocalizedMessage());
        }

        return null;
    }

    public FileSystemMonitorType getType() {
        return FileSystemMonitorType.DEFAULT;
    }

}

