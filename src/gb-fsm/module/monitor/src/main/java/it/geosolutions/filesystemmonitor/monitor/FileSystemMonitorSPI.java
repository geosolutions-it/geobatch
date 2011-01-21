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

import it.geosolutions.filesystemmonitor.OsType;

import java.util.Map;

/**
 * ServiceProviderInterface class for  {@link ThreadedFileSystemMonitor}  implementations.
 * @author  Simone Giannecchini
 * @since  0.2
 */
public interface FileSystemMonitorSPI {

    public final static String SOURCE = "source";

    public final static String WILDCARD = "wildcard";
    
    public final static String TYPE = "type";
    
    /**
     * Get the type of this FileSystemMonitor
     * @return
     * @uml.property  name="type"
     * @uml.associationEnd  
     */
    public FileSystemMonitorType getType();

    /**
     * Tells me if a certain implementation of this interface is able to run on the specified
     * operating system.
     * 
     * @param osType
     *            identfies an operating system.
     * @return <code>true</code> if this {@link FileSystemMonitor} runs on this operating system,
     *         <code>false</code> otherwise.
     */
    public boolean canWatch(OsType osType);

    /**
     * 
     * @return an instance of a {@link FileSystemMonitor}.
     * 
     */
    public FileSystemMonitor createInstance(final Map<String, ?> configuration);

    public boolean isAvailable();

}
