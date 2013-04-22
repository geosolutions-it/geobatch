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
package it.geosolutions.geobatch.actions.ds2ds.geoserver;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 *
 */
public class Ds2dsGeoServerGeneratorService extends /**AutoregisteringService*/ BaseService implements ActionService<EventObject, Ds2dsGeoServerConfiguration>{

	private final static Logger LOGGER = LoggerFactory.getLogger(Ds2dsGeoServerGeneratorService.class);
	
	public Ds2dsGeoServerGeneratorService(String id){
		super(id);
	}

	@Override
	public Action<EventObject> createAction(
			Ds2dsGeoServerConfiguration configuration) {
		try {
			return new Ds2dsGeoServerAction(configuration);
		} catch (Exception e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(
						"Error occurred creating scripting Action... "
								+ e.getLocalizedMessage(), e);
		}

		return null;
	}

	@Override
	public boolean canCreateAction(Ds2dsGeoServerConfiguration configuration) {
		return true;
	}
	
}
