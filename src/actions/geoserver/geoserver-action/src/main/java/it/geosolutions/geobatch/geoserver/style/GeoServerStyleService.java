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
package it.geosolutions.geobatch.geoserver.style;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class GeoServerStyleService extends /**AutoregisteringService*/ BaseService implements ActionService<EventObject, GeoServerStyleConfiguration> {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(GeoServerStyleService.class);
	
	public GeoServerStyleAction createAction(GeoServerStyleConfiguration configuration) {
        try {
            return new GeoServerStyleAction(configuration);
        } catch (Exception e) {
        	if (LOGGER.isErrorEnabled())
        		LOGGER.error("Error occurred creating scripting Action... " + e.getLocalizedMessage(),
                    e);
        }

        return null;
    }
	
    public GeoServerStyleService(String id, String name, String description) {
        super(id, name, description);

        if (LOGGER.isInfoEnabled())
            LOGGER.info(getClass().getSimpleName() + ": registering alias.");       
    }

    public boolean canCreateAction(GeoServerStyleConfiguration configuration) {
        return true;
    }

}
