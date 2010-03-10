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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ ShapeFileGeoServerGeneratorService.java $ Revision: x.x $ 19/feb/07 16:16:13
 */
public class ShapeFileGeoServerGeneratorService extends
        GeoServerConfiguratorService<FileSystemMonitorEvent, GeoServerActionConfiguration> {
    private final static Logger LOGGER = Logger.getLogger(ShapeFileGeoServerGeneratorService.class
            .toString());

    public ShapeFileGeoServerConfigurator createAction(GeoServerActionConfiguration configuration) {
        try {
            return new ShapeFileGeoServerConfigurator(configuration);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public boolean canCreateAction(GeoServerActionConfiguration configuration) {
        final boolean superRetVal = super.canCreateAction(configuration);
        return superRetVal;
    }

}
