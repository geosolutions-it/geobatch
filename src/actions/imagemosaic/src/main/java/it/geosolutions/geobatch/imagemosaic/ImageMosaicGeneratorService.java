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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.tools.configuration.Path;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Public class to generate JGSFLoDeSS Services
 * 
 */
public class ImageMosaicGeneratorService extends
        ImageMosaicService<FileSystemEvent, ImageMosaicConfiguration> {

    public ImageMosaicGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = Logger.getLogger(ImageMosaicGeneratorService.class
            .toString());

    /**
     * Action creator
     * 
     * @param configuration
     *            The data base action configuration
     * @return new JGSFLoDeSSSWANFileConfigurator()
     */
    public ImageMosaicAction createAction(ImageMosaicConfiguration configuration) {
        try {
            // absolutize working dir
            String wd=Path.getAbsolutePath(configuration.getWorkingDirectory());
            if (wd!=null){
                configuration.setWorkingDirectory(wd);
                return new ImageMosaicAction(configuration);
            }
            else
                return null;
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public boolean canCreateAction(ImageMosaicConfiguration configuration) {
        final boolean superRetVal = super.canCreateAction(configuration);
        return superRetVal;
    }

}