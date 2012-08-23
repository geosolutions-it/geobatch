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

package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author AlFa
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $ ImageMosaicService.java $ Revision: 0.1 $ 12/feb/07 12:07:32
 * @version $ ImageMosaicService.java $ Revision: 0.2 $ 12/may/11 12:00:22
 * 
 */
public abstract class ImageMosaicService<T extends EventObject, C extends ActionConfiguration>
        extends BaseService implements ActionService<T, C> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicService.class);

    public ImageMosaicService(String id) {
        super(id);
    }

    /**
     * @deprecated name and description are not used
     */
    public ImageMosaicService(String id, String name, String description) {
        super(id, name, description);
    }

    public boolean canCreateAction(C configuration) {
        // data flow configuration must not be null.

        if (configuration == null) {
            final String message = "Cannot create the ImageMosaicAction:  Configuration is null.";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            return false;
        }

        return true;
    }

}
