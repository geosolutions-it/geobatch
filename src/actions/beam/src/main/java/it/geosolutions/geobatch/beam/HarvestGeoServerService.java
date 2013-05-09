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

package it.geosolutions.geobatch.beam;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.Service;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorService;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author cancellieri
 * @author Daniele Romagnoli, GeoSolutions SAS
 * 
 */
public class HarvestGeoServerService extends
        GeoServerConfiguratorService<FileSystemEvent, HarvestGeoServerActionConfiguration> implements
        Service {

    private final static Logger LOGGER = LoggerFactory.getLogger(HarvestGeoServerAction.class);

    public HarvestGeoServerService(String id, String name, String description) {
        super(id, name, description);
    }

    public HarvestGeoServerAction createAction(HarvestGeoServerActionConfiguration configuration) {
        try {
            return new HarvestGeoServerAction(configuration);
        } catch (IOException e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean canCreateAction(GeoServerActionConfiguration configuration) {
        // checks
        if (configuration == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to create the consumer: Null configuration provided");
            }
            return false;
        }

        if (configuration.getDefaultNamespace() == null
                || configuration.getDefaultNamespace().length() <= 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to create the consumer: default Namespace is null!!");
            }
            return false;
        }
        return true;
    }

}
