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

package it.geosolutions.geobatch.gwc;

import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.util.EventObject;
import java.util.logging.Logger;



public abstract class GeoWebCacheConfiguratorAction<T extends EventObject>
extends BaseAction<T> {
	
	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(GeoWebCacheConfiguratorAction.class.toString());

    protected final GeoWebCacheActionConfiguration configuration;
    
    
    /**
     * Constructs a producer.
	 * The operation name will be the same than the parameter descriptor name.
	 * 
     */
    public GeoWebCacheConfiguratorAction(GeoWebCacheActionConfiguration configuration) {
        this.configuration = configuration;
        
        // //////////////////////////
        // get required parameters
        // //////////////////////////
        
		if ((configuration.getGeoserverUrl() == null) || configuration.getGwcUrl() == null 
				|| (configuration.getWorkingDirectory() == null)) {
			throw new IllegalStateException("Some configuration parameters is null!");
		}

    }
    
    public GeoWebCacheActionConfiguration getConfiguration() {
        return configuration;
    }
}
