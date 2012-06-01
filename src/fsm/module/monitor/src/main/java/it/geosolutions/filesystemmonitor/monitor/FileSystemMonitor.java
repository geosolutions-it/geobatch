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

/**
 * @author  Simone Giannecchini, GeoSolutions
 */
public interface FileSystemMonitor {

    /**
     * Start the file monitor polling.
     */
    public void start();

    /**
     * Stop the file monitor polling.
     */
    public void stop();

    public void pause();

    public void reset();

    /**
     * Check if {@link FileSystemMonitor} is running
     */
    public boolean isRunning();

    public boolean isPaused();

    /**
     * Disposing all the collections of objects I have created along the path, which are, listeners,
     * files and of course the time.
     * 
     */
    public void dispose();

    /**
     * @return  the watchDirectory
     * @uml.property  name="file"
     */
    public File getFile();

    /**
     * @return the wildCard
     * @uml.property name="wildCard"
     */
    public String getWildCard();

    public void addListener(FileSystemListener fileListener);

    public void removeListener(FileSystemListener fileListener);
}