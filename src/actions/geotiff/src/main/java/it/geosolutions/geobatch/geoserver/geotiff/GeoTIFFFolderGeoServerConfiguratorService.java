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
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorService;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author
 * 
 * @version $ GeoTIFFFolderGeoServerConfiguratorService.java $ Revision: x.x $ 19/feb/07 16:16:13
 */
public class GeoTIFFFolderGeoServerConfiguratorService extends
        GeoServerConfiguratorService<FileSystemEvent, GeoServerActionConfiguration> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeoTIFFFolderGeoServerConfiguratorService.class.toString());

    public GeoTIFFFolderGeoServerConfiguratorService(String id, String name, String description) {
        super(id, name, description);
    }

    public GeoTIFFFolderGeoServerConfigurator createAction(
            GeoServerActionConfiguration configuration) {
        try {
            return new GeoTIFFFolderGeoServerConfigurator(configuration);
        } catch (IOException e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean canCreateAction(GeoServerActionConfiguration configuration) {
        final boolean superRetVal = super.canCreateAction(configuration);
        return superRetVal;
    }

}
