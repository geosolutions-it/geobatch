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
package it.geosolutions.geobatch.nurc.sem.lscv08;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorService;
import it.geosolutions.geobatch.metocs.commons.MetocActionConfiguration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Public class to generate lscv08::NRL_NCOM Model Services
 * 
 */
public class NRLNCOMGeneratorService extends
        GeoServerConfiguratorService<FileSystemEvent, MetocActionConfiguration> {

    private final static Logger LOGGER = LoggerFactory.getLogger(NRLNCOMGeneratorService.class.toString());

    public NRLNCOMGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }
    
    /**
     * Action creator
     * 
     * @param configuration
     *            The data base action configuration
     * @return new NRLNCOMAction()
     */
    public NRLNCOMAction createAction(MetocActionConfiguration configuration) {
        try {
            return new NRLNCOMAction(configuration);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    @Override
    public boolean canCreateAction(MetocActionConfiguration configuration) {
        try {
            // absolutize working dir
            String wd = Path.getAbsolutePath(configuration.getWorkingDirectory());
            if (wd != null) {
                configuration.setWorkingDirectory(wd);
                return true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("NRLNCOMGeneratorService::canCreateAction(): "
                                    + "unable to create action, it's not possible to get an absolute working dir.");
            }
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

}