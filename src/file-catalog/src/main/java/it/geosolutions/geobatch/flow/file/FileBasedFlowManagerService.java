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



package it.geosolutions.geobatch.flow.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.FlowManagerService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataAccessFactory.Param;

public class FileBasedFlowManagerService extends BaseService implements
        FlowManagerService<FileSystemMonitorEvent, FileBasedFlowConfiguration> {

    private FileBasedFlowManagerService() {
        super(true);
    }

    public final static Param WORKING_DIR = new Param("WorkingDir", String.class, "WorkingDir",
            true);

    private final static Logger LOGGER = Logger.getLogger(FileBasedFlowManagerService.class
            .toString());

    public boolean canCreateFlowManager(FileBasedFlowConfiguration configuration) {

        final String workingDir = configuration.getWorkingDirectory();
        if (workingDir != null) {
            final File dir = new File((String) workingDir);
            if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
                // TODO message
                return false;
        }

        return true;

    }

    public FileBasedFlowManager createFlowManager(FileBasedFlowConfiguration configuration) {

        final String workingDir = configuration.getWorkingDirectory();
        if (workingDir != null) {
            final File dir = new File((String) workingDir);
            if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
                // TODO message
                return null;

            try {
                final FileBasedFlowManager manager = new FileBasedFlowManager();
                manager.setConfiguration(configuration);
                return manager;
            } catch (IOException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);

            }

        }
        return null;
    }

}
