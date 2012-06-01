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
package it.geosolutions.geobatch.geoserver.reload;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

public class GeoServerReloadConfiguration extends ActionConfiguration {

	// path of the xml file containing the geoserver list to reload
	protected String geoserverList;
	
	// size of the parallel executions 
	protected Integer executorSize;

    /**
	 * @return the executorSize
	 */
	public final Integer getExecutorSize() {
		return executorSize;
	}

	/**
	 * @param executorSize the executorSize to set
	 */
	public final void setExecutorSize(Integer executorSize) {
		this.executorSize = executorSize;
	}

	/**
	 * @return the geoserverList
	 */
	public final String getGeoserverList() {
		return geoserverList;
	}

	/**
	 * @param geoserverList the geoserverList to set
	 */
	public final void setGeoserverList(String geoserverList) {
		this.geoserverList = geoserverList;
	}

	public GeoServerReloadConfiguration(String id, String name, String description) {
		super(id, name, description);
	}
	
    @Override
    public GeoServerReloadConfiguration clone() { 
        final GeoServerReloadConfiguration configuration = (GeoServerReloadConfiguration) super
                .clone();

        configuration.setGeoserverList(geoserverList);
//        configuration.setServiceID(getServiceID());
        configuration.setConfigDir(getConfigDir());

        return configuration;
    }


}