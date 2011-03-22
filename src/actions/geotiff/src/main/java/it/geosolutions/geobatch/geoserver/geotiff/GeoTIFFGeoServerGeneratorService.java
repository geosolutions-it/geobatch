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

package it.geosolutions.geobatch.geoserver.geotiff;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author
 * 
 * @version $ ShapeFileDTOProducerSPI.java $ Revision: x.x $ 19/feb/07 16:16:13
 */
public class GeoTIFFGeoServerGeneratorService extends
        GeoServerConfiguratorService<FileSystemEvent, GeoServerActionConfiguration> {

    private final static Logger LOGGER = Logger.getLogger(GeoTIFFGeoServerGenerator.class
            .toString());

    public GeoTIFFGeoServerGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    public GeoTIFFGeoServerGenerator createAction(GeoServerActionConfiguration configuration) {
        try {
            return new GeoTIFFGeoServerGenerator(configuration);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean canCreateAction(GeoServerActionConfiguration configuration) {
        try {
            // absolutize working dir
            String wd = Path.getAbsolutePath(configuration.getWorkingDirectory());
            if (wd != null) {
                configuration.setWorkingDirectory(wd);
                return true;
            } else {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(
                            Level.WARNING,
                            "GeoTIFFGeoServerGeneratorService::canCreateAction(): "
                                    + "unable to create action, it's not possible to get an absolute working dir.");
            }
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return false;
    }

}
