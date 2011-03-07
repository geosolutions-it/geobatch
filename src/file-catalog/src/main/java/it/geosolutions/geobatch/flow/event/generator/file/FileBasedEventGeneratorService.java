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

package it.geosolutions.geobatch.flow.event.generator.file;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.flow.event.generator.BaseEventGeneratorService;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Simone Giannecchini, GeoSolutions
 * @author Ivano Picco
 * 
 */
public class FileBasedEventGeneratorService extends
        BaseEventGeneratorService<FileSystemEvent, FileBasedEventGeneratorConfiguration> {

    public FileBasedEventGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = Logger.getLogger(FileBasedEventGeneratorService.class
            .toString());

    /*
     * (non-Javadoc)
     * 
     * @seeit.geosolutions.geobatch.flow.event.generator.EventGeneratorService#
     * canCreateEventGenerator (java.util.Map)
     */
    public boolean canCreateEventGenerator(FileBasedEventGeneratorConfiguration configuration) {
        final OsType osType = configuration.getOsType();
        if (osType == null)
            return false;
        final File sensedDir;
        try {
            sensedDir = Path.findLocation(configuration.getWorkingDirectory(), new File(
                    ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
            if (sensedDir != null) {
                if (sensedDir.exists() && sensedDir.isDirectory() && sensedDir.canRead()) // TODO
                                                                                          // message
                    return true;
            }
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeit.geosolutions.geobatch.flow.event.generator.EventGeneratorService#
     * createEventGenerator(java .util.Map)
     */
    public FileBasedEventGenerator<FileSystemEvent> createEventGenerator(
            FileBasedEventGeneratorConfiguration configuration) {

        try {
            final OsType osType = configuration.getOsType();
            final FileSystemEventType eventType = configuration.getEventType();
            final File sensedDir;
            sensedDir = Path.findLocation(configuration.getWorkingDirectory(), new File(
                    ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
            if (sensedDir != null) {
                if (!sensedDir.exists() || !sensedDir.isDirectory() || !sensedDir.canRead()){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe("FileBasedEventGenerator: Watching dir is not valid!");
                    return null;
                }
            }
            final boolean keepFiles = configuration.getKeepFiles();

            return new FileBasedEventGenerator<FileSystemEvent>(configuration);
            
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return null;
    }

}
