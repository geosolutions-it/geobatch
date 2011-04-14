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
package it.geosolutions.geobatch.metocs.remsens;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.metocs.registry.RegistryConfiguratorService;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 * Public class to generate OSTIA-2-GeoTIFFs Services
 * 
 */
public class OSTIA2GeoTIFFsGeneratorService extends
        RegistryConfiguratorService<FileSystemEvent, GeoServerActionConfiguration> {

    private final static Logger LOGGER = LoggerFactory.getLogger(OSTIA2GeoTIFFsGeneratorService.class
            .toString());

    public OSTIA2GeoTIFFsGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }
    /**
     * Action creator
     * 
     * @param configuration
     *            The Ostia action configuration
     * @return new OSTIA2GeoTIFFsFileConfiguratorAction()
     */
    public OSTIA2GeoTIFFsFileConfiguratorAction createAction(
            GeoServerActionConfiguration configuration) {
        try {
            return new OSTIA2GeoTIFFsFileConfiguratorAction(configuration);
        } catch (IOException e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        } catch (NoSuchAuthorityCodeException e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        } catch (FactoryException e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public boolean canCreateAction(GeoServerActionConfiguration configuration) {
        return super.canCreateAction(configuration);
    }

}