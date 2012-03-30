/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
package it.geosolutions.geobatch.flow.file;

import java.io.File;
import java.io.IOException;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.FlowManagerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedFlowManagerService extends BaseService
        implements FlowManagerService<FileSystemEvent, FileBasedFlowConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedFlowManagerService.class);

    public FileBasedFlowManagerService(String id, String name, String description) {
        super(id, name, description);
    }

    public boolean canCreateFlowManager(FileBasedFlowConfiguration configuration) {

        final String ovrTDir = configuration.getOverrideTempDir();
        if ( ovrTDir != null ) {
            final File dir = new File(ovrTDir);
            if ( !dir.isAbsolute() ) {
                LOGGER.error("Override directory must be absolute [" + ovrTDir + "]");
                return false;
            }

            if ( !dir.exists() || !dir.isDirectory() || !dir.canWrite() ) {
                LOGGER.error("Bad override dir '" + dir + "'");
                return false;
            }
        }

        return true;
    }

    public FileBasedFlowManager createFlowManager(FileBasedFlowConfiguration configuration) {

        if ( !canCreateFlowManager(configuration) ) {
            throw new IllegalStateException("FlowManager can not be created with current configuration");
        }

        try {
            return new FileBasedFlowManager(configuration);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
