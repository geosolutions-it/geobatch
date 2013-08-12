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


import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.thoughtworks.xstream.XStream;

/**
 * DAO for the input / output XML file used by the action.
 * 
 * @author Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it
 *
 */
public class FeatureConfiguration implements Cloneable {
	
	private static final XStream xstream = new XStream();
	
	static {
		xstream.alias("feature", FeatureConfiguration.class);				
		xstream.omitField(FeatureConfiguration.class, "coordinateReferenceSystem");				
	}
	
	// feature type (schema) name
	private String typeName;
	// feature coordinate system (EPSG code): if not defined it will be read from
	// the input feature 
	private String crs;
	
	// feature coordinate system (cached after decoding crs)
	private transient CoordinateReferenceSystem coordinateReferenceSystem;
	
	// feature DataStore connection parameters
	private Map<String,Serializable> dataStore;

	/**
	 * 
	 * @return the typeName of the feature.
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Sets typeName for the feature.
	 * @param typeName
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * 
	 * @return the DataStore connection parameters.
	 */
	public Map<String, Serializable> getDataStore() {
		if(this.dataStore == null) {
			this.dataStore = new HashMap<String, Serializable>();
		}
		return dataStore;
	}

	/**
	 * Sets DataStore connection parameters.
	 * @param dataStore
	 */
	public void setDataStore(Map<String, Serializable> dataStore) {
		
		this.dataStore = dataStore;
	}

	/**
	 * 
	 * @return the CRS to use for the input feature
	 */
	public String getCrs() {
		return crs;
	}

	/**
	 * Sets the CRS to use for the input feature
	 * @param crs
	 */
	public void setCrs(String crs) {
		this.crs = crs;		
	}
	
	
	
	public void setCoordinateReferenceSystem(
			CoordinateReferenceSystem coordinateReferenceSystem) {
		this.coordinateReferenceSystem = coordinateReferenceSystem;
	}

	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		if(coordinateReferenceSystem == null && crs != null) {
			try {
				coordinateReferenceSystem = CRS.decode(crs);
			} catch (NoSuchAuthorityCodeException e) {
				throw new IllegalArgumentException("Invalid crs: "+crs);
			} catch (FactoryException e) {
				throw new IllegalArgumentException("Invalid crs: "+crs);
			}
		}
		return coordinateReferenceSystem;
	}
	
	/**
	 * Read a FeatureConfiguration xml from the given stream.
	 * 
	 * @param inputXML
	 * @return
	 */
	public static FeatureConfiguration fromXML(InputStream inputXML) {
		return (FeatureConfiguration) xstream.fromXML(inputXML);		
	}
	
	/**
	 * Outputs the FeatureConfiguration in XML to the given stream.
	 * 
	 * @param outXML
	 */
	public void toXML(OutputStream outXML) {
		xstream.toXML(this, outXML);
	}
	
	public FeatureConfiguration clone() {
        try {
        	FeatureConfiguration fc = (FeatureConfiguration) super.clone();
//        	fc.typeName = this.typeName ;
//        	fc.crs = this.crs;
//        	fc.coordinateReferenceSystem = this.coordinateReferenceSystem;
        	if(this.dataStore != null) {
	        	fc.dataStore = new HashMap<String,Serializable>();
                for (Map.Entry<String, Serializable> entry : dataStore.entrySet()) {
	        		fc.dataStore.put(entry.getKey(), entry.getValue());
                }
        	} else {
        		fc.dataStore = null;
        	}

            return fc;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
}
