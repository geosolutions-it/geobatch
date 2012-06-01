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

package it.geosolutions.geobatch.geotiff.retile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ ShapeFileDTOProducerSPI.java $ Revision: x.x $ 19/feb/07 16:16:13
 */
public class GeotiffRetilerService extends BaseService implements
        ActionService<FileSystemEvent, GeotiffRetilerConfiguration> {

    public GeotiffRetilerService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(GeotiffRetiler.class.toString());

    public boolean canCreateAction(GeotiffRetilerConfiguration configuration) {
        
        return true;
    }

    public GeotiffRetiler createAction(GeotiffRetilerConfiguration configuration) {
        try {
            return new GeotiffRetiler(configuration);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
