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

import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;

/**
 * 
 *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *        emmanuel.blondel@fao.org
 *
 */
public class Ds2dsGeoServerConfiguration extends GeoServerActionConfiguration {
	

	private String operation;
	private boolean createDataStore;
	private boolean createNameSpace;
	private FeatureConfiguration featureConfig;

	/**
	 * Constructs a GeoServerDBLayerConfiguration
	 * 
	 * @param id
	 * @param name
	 * @param description
	 */
	public Ds2dsGeoServerConfiguration(String id, String name,
			String description) {
		super(id, name, description);
	}

	/**
	 * Set the operation (PUBLISH, REMOVE) to perform
	 * 
	 * @param operation
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}

	/**
	 * Get the operation to perform
	 * 
	 * @return
	 */
	public String getOperation() {
		return operation;
	}
	
	/**
	 * Set CreateStore value
	 * 
	 * @param createDataStore
	 */
	public void setCreateDataStore(boolean createDataStore){
		this.createDataStore = createDataStore;
	}
	
	/**
	 * Get CreateStore value
	 * 
	 * @return
	 */
	public boolean getCreateDataStore(){
		return this.createDataStore;
	}
	
	/**
	 * set CreateNameSpace value
	 * 
	 * @param createNameSpace
	 */
	public void setCreateNameSpace(boolean createNameSpace){
		this.createNameSpace = createNameSpace;
	}
	
	/**
	 * Get CreateNameSpace value
	 * 
	 * @return
	 */
	public boolean getCreateNameSpace(){
		return this.createNameSpace;
	}
	
	/**
	 * 
	 * @param featureConfiguration
	 */
	public void setFeatureConfiguration(FeatureConfiguration featureConfiguration){
		this.featureConfig = featureConfiguration;
	}
	
	/**
	 * Get the Feature configuration
	 * 
	 * @return
	 */
	public FeatureConfiguration getFeatureConfiguration(){
		if(featureConfig == null) {
			featureConfig = new FeatureConfiguration();
		}
		return featureConfig;
	}
	
	
	@Override
    public Ds2dsGeoServerConfiguration clone() { 
        final Ds2dsGeoServerConfiguration configuration = (Ds2dsGeoServerConfiguration) super
                .clone();

        configuration.setOperation(operation);
        return configuration;
    }
	
	

}
