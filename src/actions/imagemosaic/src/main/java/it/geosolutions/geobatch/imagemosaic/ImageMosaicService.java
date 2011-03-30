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

package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.io.File;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ ImageMosaicService.java $ Revision: 0.1 $ 12/feb/07 12:07:32
 */
public abstract class ImageMosaicService<T extends EventObject, C extends ActionConfiguration>
        extends BaseService implements ActionService<T, C> {
    protected final static Logger LOGGER = Logger.getLogger(ImageMosaicService.class.toString());

    public ImageMosaicService(String id, String name, String description) {
        super(id, name, description);
    }

    // public ImageMosaicService() {
    // super(true);
    // }

    public boolean canCreateAction(C configuration) {
        // data flow configuration must not be null.

        if (configuration == null) {
            final String message = "ImageMosaicService::canCreateAction():  Cannot create the ImageMosaicAction:  Configuration is null.";
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, message);
            return false;
        }

        try {
            // absolutize working dir
            final String wd = Path.getAbsolutePath(configuration.getWorkingDirectory());
            if (wd != null) {
                configuration.setWorkingDirectory(wd);
            } else {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(
                            Level.SEVERE,
                            "ImageMosaicService::canCreateAction(): "
                                    + "unable to create action, it's not possible to get an absolute working dir.");
            }
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return false;
        }

        final File workingDir = new File(configuration.getWorkingDirectory());

        if (!workingDir.exists() || !workingDir.isDirectory()) {
            final String message = "ImageMosaicService::canCreateAction(): Cannot create the ImageMosaicAction: "
                    + "GeoServer working Dir does not exist.";
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, message);
            return false;
        }

        return true;
    }

}
