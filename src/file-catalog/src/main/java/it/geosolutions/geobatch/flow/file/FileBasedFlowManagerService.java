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

import java.io.File;
import java.io.IOException;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.FlowManagerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileBasedFlowManagerService extends BaseService
    implements FlowManagerService<FileSystemEvent, FileBasedFlowConfiguration>
{

    // 06 04 2011 carlo: commented out -> never used
    // public final static Param WORKING_DIR = new Param("WorkingDir", String.class, "WorkingDir",
    // true);

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedFlowManagerService.class.toString());

    // private FileBasedFlowManagerService() {
    // super(true);
    // }

    public FileBasedFlowManagerService(String id, String name, String description)
    {
        super(id, name, description);
    }

    public boolean canCreateFlowManager(FileBasedFlowConfiguration configuration)
    {

        final String workingDir = configuration.getWorkingDirectory();
        if (workingDir != null)
        {
            final File dir = new File((String) workingDir);
            if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
            {
                if (LOGGER.isErrorEnabled())
                {
                    LOGGER.error("Bad working dir '" + dir + "'");
                }

                return false;
            }
        }

        return true;
    }

    public FileBasedFlowManager createFlowManager(FileBasedFlowConfiguration configuration)
    {

        final String workingDir = configuration.getWorkingDirectory();
        if (workingDir != null)
        {
            final File dir = new File((String) workingDir);
            if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
            {
                if (LOGGER.isErrorEnabled())
                {
                    LOGGER.error("Bad working dir '" + dir + "'");
                }

                return null;
            }

            try
            {
                final FileBasedFlowManager manager = new FileBasedFlowManager(configuration);

                return manager;
            }
            catch (NullPointerException e)
            {
                if (LOGGER.isErrorEnabled())
                {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            }
            catch (IOException e)
            {
                if (LOGGER.isErrorEnabled())
                {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            }
        }

        return null;
    }

}
