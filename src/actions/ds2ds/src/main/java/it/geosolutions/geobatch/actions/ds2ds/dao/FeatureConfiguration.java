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
import java.util.Map.Entry;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * DAO for the input / output XML file used by the action.
 * 
 * @author Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it
 *
 */
public class FeatureConfiguration {
	
	private static final XStream xstream = new XStream();
	
	static {
		xstream.alias("feature", FeatureConfiguration.class);		
		/*xstream.alias("DataStore", DataStoreConfiguration.class);
		xstream.aliasField("DataStore", FeatureConfiguration.class, "dataStore");
		xstream.aliasField("TypeName", FeatureConfiguration.class, "typeName");
		xstream.aliasField("CRS", FeatureConfiguration.class, "crs");*/
		xstream.omitField(FeatureConfiguration.class, "coordinateReferenceSystem");
		
		/*xstream.registerConverter(new Converter() {

			@Override
			public boolean canConvert(Class arg0) {
				return Map.class.isAssignableFrom(arg0);
			}

			@Override
			public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {

	            Map map = (Map) value;
	            for (Object obj : map.entrySet()) {
	                Entry entry = (Entry) obj;
	                writer.startNode(entry.getKey().toString());
	                writer.setValue(entry.getValue().toString());
	                writer.endNode();
	            }
	        }

			@Override
			public Object unmarshal(HierarchicalStreamReader reader,
					UnmarshallingContext context) {
				Map<String, Serializable> map = new HashMap<String, Serializable>();

			    while(reader.hasMoreChildren()) {
			        reader.moveDown();			        
			        map.put(reader.getNodeName(), reader.getValue());
			        reader.moveUp();
			    }
			    return map;				
			}
			
		});*/
	}
	
	private String typeName;
	private String crs;
	private CoordinateReferenceSystem coordinateReferenceSystem;
	
	private DataStoreConfiguration dataStore = new DataStoreConfiguration();

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
	public DataStoreConfiguration getDataStore() {
		return dataStore;
	}

	/**
	 * Sets DataStore connection parameters.
	 * @param dataStore
	 */
	public void setDataStore(DataStoreConfiguration dataStore) {
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
	
	public static FeatureConfiguration fromXML(InputStream inputXML) {
		return (FeatureConfiguration) xstream.fromXML(inputXML);		
	}
	
	public void toXML(OutputStream outXML) {
		xstream.toXML(this, outXML);
	}
}
