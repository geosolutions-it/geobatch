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
 * Configuration Object for the Ds2ds action.
 * This action copies a feature from a source GeoTools DataStore to an output GeoTools DataStore.
 * 
 * Both source and output features options can be configured.
 * 
 * @author Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it
 * 
 */
public class Ds2dsConfiguration extends ActionConfiguration {

	// source feature configuration object (optional)
	private FeatureConfiguration sourceFeature;
	
	// output feature configuration object
	private FeatureConfiguration outputFeature;
	
	// purge (remove) data from the output feature before importing
	private Boolean purgeData = false;
	
	// optional mappings (renaming) from source feature attributes to
	// output (key is output attribute name, value is source attribute name
	private Map<String,Serializable> attributeMappings; 
	
	// execute a projection on the attribureMappings property, skipping
	// source properties non included in the map
	private Boolean projectOnMappings = false;
	
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
	
	@Override
    public Ds2dsConfiguration clone() {
        final Ds2dsConfiguration ret = (Ds2dsConfiguration)super.clone();

        if (this.sourceFeature != null)
            ret.sourceFeature = this.sourceFeature.clone();
        else
            ret.sourceFeature = null;
        
        if (this.outputFeature != null)
            ret.outputFeature = this.outputFeature.clone();
        else
            ret.outputFeature = null;
        
        ret.purgeData = this.purgeData;
        if(this.attributeMappings != null) {
	    	ret.attributeMappings=new HashMap<String,Serializable>();
	    	for(String key : this.attributeMappings.keySet()) {
	    		ret.attributeMappings.put(key, this.attributeMappings.get(key));
	    	}
        } else {
        	ret.attributeMappings = null;
        }
    	    	
    	ret.projectOnMappings = this.projectOnMappings;

        return ret;
    }
}
