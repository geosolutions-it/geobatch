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


package it.geosolutions.geobatch.actions.ds2ds.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DataStore connection parameters DAO for the input / output XML file used by the Action.
 * 
 * @author Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it
 *
 */
public class DataStoreConfiguration {
	private Map<String, Serializable> parameters = new HashMap<String, Serializable>();

	/**
	 * 
	 * @return connection parameters for the DataStore
	 */
	public Map<String, Serializable> getParameters() {
		return parameters;
	}

	/**
	 * Sets connection parameters for the DataStore
	 * @param parameters
	 */
	public void setParameters(Map<String, Serializable> parameters) {
		this.parameters = parameters;
	}
	
	
}
