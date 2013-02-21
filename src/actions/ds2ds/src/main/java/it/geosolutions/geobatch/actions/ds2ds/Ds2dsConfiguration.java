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
package it.geosolutions.geobatch.actions.ds2ds;

import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it
 * 
 */
public class Ds2dsConfiguration extends ActionConfiguration {

	private FeatureConfiguration sourceFeature;
	private FeatureConfiguration outputFeature;
	
	private boolean purgeData = false;
	
	
	private Map<String,Serializable> attributeMappings; 
	
	private boolean projectOnMappings = false;
	
	public Ds2dsConfiguration(String id, String name, String description) {
		super(id, name, description);		
	}
	
	/**
	 * 
	 * @return the source feature configuration
	 */
	public FeatureConfiguration getSourceFeature() {
		if(sourceFeature == null) {
			sourceFeature = new FeatureConfiguration();
		}
		return sourceFeature;
	}

	/**
	 * 
	 * @param sourceFeature sets the source feature configuration
	 */
	public void setSourceFeature(FeatureConfiguration sourceFeature) {
		this.sourceFeature = sourceFeature;
	}

	/**
	 * 
	 * @return the output feature configuration
	 */
	public FeatureConfiguration getOutputFeature() {
		if(outputFeature == null) {
			outputFeature = new FeatureConfiguration();
		}
		return outputFeature;
	}

	/**
	 * 
	 * @param outputFeature sets the output feature configuration
	 */
	public void setOutputFeature(FeatureConfiguration outputFeature) {
		this.outputFeature = outputFeature;
	}

	/**
	 * 
	 * @return purge data on output feature
	 */
	public boolean isPurgeData() {
		return purgeData;
	}

	/**
	 * 
	 * @param purgeData sets data purging on output feature
	 */
	public void setPurgeData(boolean purgeData) {
		this.purgeData = purgeData;
	}

	/**
	 * 
	 * @return attribute mappings
	 */
	public Map<String, Serializable> getAttributeMappings() {
		if(attributeMappings == null) {
			attributeMappings = new HashMap<String,Serializable>();
		}
		return attributeMappings;
	}

	/**
	 * 
	 * @param attributes attribute mappings from source to destination
	 * 		currently renaming is the only supported mapping
	 * 		(output attribute -> source attribute)
	 */
	public void setAttributeMappings(Map<String, Serializable> attributes) {
		if(attributes != null) {
			this.attributeMappings = attributes;
		}
	}

	/**
	 * 
	 * @return projection on mapping attributes executed 
	 */
	public boolean isProjectOnMappings() {
		return projectOnMappings;
	}

	/**
	 * 
	 * @param projectOnMappings projection on mapping attributes to be executed 
	 */
	public void setProjectOnMappings(boolean projectOnMappings) {
		this.projectOnMappings = projectOnMappings;
	}	
	
	/**
	 * Shortcut to set projection on source attributes.
	 * @param attributes
	 */
	public void setProjection(List<String> attributes) {
		 Map<String,Serializable> mappings = new HashMap<String,Serializable>();
		 for(String attribute : attributes) {
			 mappings.put(attribute, attribute);
		 }
		 setAttributeMappings(mappings);
		 setProjectOnMappings(true);
	}
}
