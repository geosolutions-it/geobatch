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
package it.geosolutions.geobatch.metocs.base;

import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.io.IOException;
import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Public class to generate NetCDF_CF-2-GeoTIFFs Services
 * 
 */
public class NetCDFCFGeodetic2GeoTIFFsGeneratorService extends BaseService implements
        ActionService<EventObject, NetCDFCFGeodetic2GeoTIFFsConfiguration> {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(NetCDFCFGeodetic2GeoTIFFsGeneratorService.class);

    public NetCDFCFGeodetic2GeoTIFFsGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Action creator
     * 
     * @param configuration
     *            The data base action configuration
     * @return new JGSFLoDeSSSWANFileConfigurator()
     */
    public NetCDFCFGeodetic2GeoTIFFsFileAction createAction(
            NetCDFCFGeodetic2GeoTIFFsConfiguration configuration) {
        try {
            return new NetCDFCFGeodetic2GeoTIFFsFileAction(configuration);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public boolean canCreateAction(NetCDFCFGeodetic2GeoTIFFsConfiguration configuration) {
        try {
            // absolutize working dir
            String wd = Path.getAbsolutePath(configuration.getWorkingDirectory());
            if (wd != null) {
                configuration.setWorkingDirectory(wd);
                return true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("NetCDFCFGeodetic2GeoTIFFsFileAction::canCreateAction(): "
                            + "unable to create action, it's not possible to get an absolute working dir.");
            }
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

}