/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.actions.geostore;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Luca Paolino - luca.paolino@geo-solutions.it
 * 
 */
public class GeostoreGeneratorService extends BaseService implements
    ActionService<FileSystemEvent, GeostoreActionConfiguration> {

    public GeostoreGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(GeostoreGeneratorService.class);

    public GeostoreAction createAction(GeostoreActionConfiguration configuration) {
        try {
            return new GeostoreAction(configuration);
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean canCreateAction(GeostoreActionConfiguration configuration) {
        try {
            // absolutize working dir
            String url = configuration.getUrl();
            String username = configuration.getUser();
            String password = configuration.getPassword();

            if (url == null) {
                LOGGER.error("The URL cannot be set to null, check the configuration file");
                return false;
            }

            if (username == null) {
                LOGGER.error("The username cannot be set to null, check the configuration file");
                return false;
            }

            if (password == null) {
                LOGGER.error("The password cannot be set to null, check the configuration file");
                return false;
            }
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

}
