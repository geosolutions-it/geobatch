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
	// If the configuration specify an ecqlFilter only the features selected by the filter will be purged
	// If no filter are provided this flag remove all feature as forcePurgeAllData
	private Boolean purgeData = false;
	
	// purge (remove) ALL data from the output feature before importing although a filter is specified
	// if this flag is set to TRUE the flag purgeData has no effect.
	private Boolean forcePurgeAllData = false;
	
	// optional mappings (renaming) from source feature attributes to
	// output (key is output attribute name, value is source attribute name
	private Map<String,Serializable> attributeMappings; 
	
	// execute a projection on the attribureMappings property, skipping
	// source properties non included in the map
	private Boolean projectOnMappings = false;
	
	//optional ecql filter in order to filter the source store
	private String ecqlFilter;
	
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
		return purgeData == null ? false : purgeData;
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
		return projectOnMappings == null ? false : projectOnMappings;
	}

	/**
	 * 
	 * @param projectOnMappings projection on mapping attributes to be executed 
	 */
	public void setProjectOnMappings(boolean projectOnMappings) {
		this.projectOnMappings = projectOnMappings;
	}	
	
    	/**
         * @return the ecqlFilter
         */
        public String getEcqlFilter() {
            return ecqlFilter;
        }
    
        /**
         * @param ecqlFilter the ecqlFilter to set
         */
        public void setEcqlFilter(String ecqlFilter) {
            this.ecqlFilter = ecqlFilter;
        }
        
        /**
         * @return the forcePurgeAllData
         */
        public Boolean isForcePurgeAllData() {
            return (forcePurgeAllData == null) ? false : forcePurgeAllData;
        }

        /**
         * @param forcePurgeAllData the forcePurgeAllData to set
         */
        public void setForcePurgeAllData(Boolean forcePurgeAllData) {
            this.forcePurgeAllData = forcePurgeAllData;
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

        ret.sourceFeature = (this.sourceFeature != null)?this.sourceFeature.clone():null;
        ret.outputFeature = (this.outputFeature != null)?this.outputFeature.clone():null;
        ret.ecqlFilter = (this.ecqlFilter != null)?this.ecqlFilter:null;
        ret.purgeData = this.purgeData;
        ret.projectOnMappings = this.projectOnMappings;
        
        if(this.attributeMappings != null) {
	    	ret.attributeMappings=new HashMap<String,Serializable>();
	    	for(String key : this.attributeMappings.keySet()) {
	    		ret.attributeMappings.put(key, this.attributeMappings.get(key));
	    	}
        } else {
        	ret.attributeMappings = null;
        }

        return ret;
    }
}
