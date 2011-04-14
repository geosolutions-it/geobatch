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
package it.geosolutions.geobatch.nurc.tda.jgsflodess;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorService;
import it.geosolutions.geobatch.metocs.commons.MetocActionConfiguration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Public class to generate JGSFLoDeSS Services
 * 
 */
public class JGSFLoDeSSNCOMGeneratorService extends
        GeoServerConfiguratorService<FileSystemEvent, MetocActionConfiguration> {


    private final static Logger LOGGER = LoggerFactory.getLogger(JGSFLoDeSSNCOMGeneratorService.class
            .toString());
    
    public JGSFLoDeSSNCOMGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Action creator
     * 
     * @param configuration
     *            The data base action configuration
     * @return new NRLNCOMFileConfiguratorAction()
     */
    public JGSFLoDeSSNCOMFileConfigurator createAction(MetocActionConfiguration configuration) {
        try {
            return new JGSFLoDeSSNCOMFileConfigurator(configuration);
        } catch (IOException e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public boolean canCreateAction(MetocActionConfiguration configuration) {
        final boolean superRetVal = super.canCreateAction(configuration);
        return superRetVal;
    }

}