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
package it.geosolutions.geobatch.imagemosaic;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Public class to generate JGSFLoDeSS Services
 * 
 */
public class ImageMosaicGeneratorService extends
        ImageMosaicService<EventObject, ImageMosaicConfiguration> {

    public ImageMosaicGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicGeneratorService.class
            .toString());

    /**
     * Action creator
     * 
     * @param configuration
     *            The data base action configuration
     * @return new JGSFLoDeSSSWANFileConfigurator()
     */
    public ImageMosaicAction createAction(ImageMosaicConfiguration configuration) {
            return new ImageMosaicAction(configuration);
    }

    @Override
    public boolean canCreateAction(ImageMosaicConfiguration configuration) {
        try {            
            // //
            // data flow configuration and dataStore name must not be null.
            // //

            if (configuration == null) {
                final String message = "ImageMosaicAction: DataFlowConfig is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message,new IllegalStateException(message));
            } else if ((configuration.getGeoserverURL() == null)) {
                final String message = "GeoServerURL is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message,new IllegalStateException(message));
            } else if (configuration.getGeoserverURL().isEmpty()) {
                final String message = "GeoServerURL is empty.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message,new IllegalStateException(message));
            }
        } catch (Error e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

}