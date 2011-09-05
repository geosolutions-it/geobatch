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

package it.geosolutions.geobatch.geoserver.shapefile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author AlFa
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $ ShapeFileGeoServerGeneratorService.java $ Revision: 0.1 $ 19/feb/07 16:16:13
 * @version $ ShapeFileGeoServerGeneratorService.java $ Revision: 0.2 $ 28/Apr/11 16:42:23
 */
public class ShapeFileGeoServerService extends BaseService implements
        ActionService<FileSystemEvent, GeoServerActionConfiguration> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShapeFileGeoServerService.class);

    public ShapeFileGeoServerService(String id, String name, String description) {
        super(id, name, description);
    }
    
    public ShapeFileAction createAction(final GeoServerActionConfiguration configuration) {
        try {
            return new ShapeFileAction(configuration);
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean canCreateAction(final GeoServerActionConfiguration configuration) {
        // data flow configuration must not be null.
        if (configuration == null) {
            final String message = "ShapeFileGeoServerService::canCreateAction():  Cannot create the ShapeFileAction:  Configuration is null.";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            return false;
        }

        try {
            // absolutize working dir
            final String wd = Path.getAbsolutePath(configuration.getWorkingDirectory());
            if (wd != null) {
                configuration.setWorkingDirectory(wd);
            } else {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("ShapeFileGeoServerService::canCreateAction(): "
                                    + "unable to create action, it's not possible to get an absolute working dir.");
            }
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            return false;
        }

        final File workingDir = new File(configuration.getWorkingDirectory());

        if (!workingDir.exists() || !workingDir.isDirectory()) {
            final String message = "ShapeFileGeoServerService::canCreateAction(): Cannot create the ShapeFileAction: "
                    + "GeoServer working Dir does not exist.";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            return false;
        }

        return true;
    }

}
