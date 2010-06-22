/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
package it.geosolutions.geobatch.geoserver.rest;

import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;

public class GeoServerRESTActionConfiguration extends GeoServerActionConfiguration {

    public GeoServerRESTActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
	}

    private String queryString;
    
    private String geoserverVersion;
    
    private String storeType;
    
    private String storeId;
    
    private Boolean vectorialLayer;

    public GeoServerRESTActionConfiguration() {
        super();
    }

    public GeoServerRESTActionConfiguration(GeoServerRESTActionConfiguration baseConfiguration) {
        super();

        setCrs(baseConfiguration.getCrs());
		setDataTransferMethod(baseConfiguration.getDataTransferMethod());
		setDatatype(baseConfiguration.getDatatype());
		setDefaultNamespace(baseConfiguration.getDefaultNamespace());
		setDefaultNamespaceUri(baseConfiguration.getDefaultNamespaceUri());
		setDefaultStyle(baseConfiguration.getDefaultStyle());
		setEnvelope(baseConfiguration.getEnvelope());
		setGeoserverPWD(baseConfiguration.getGeoserverPWD());
		setGeoserverUID(baseConfiguration.getGeoserverUID());
		setGeoserverURL(baseConfiguration.getGeoserverURL());
		setGeoserverVersion(baseConfiguration.getGeoserverVersion());
		setServiceID(baseConfiguration.getServiceID());
		setStoreId(baseConfiguration.getStoreId());
		setStoreFilePrefix(baseConfiguration.getStoreFilePrefix());
		setStoreType(baseConfiguration.getStoreType());
		setStyles(baseConfiguration.getStyles());
		setWmsPath(baseConfiguration.getWmsPath());
		setQueryString(baseConfiguration.getQueryString());
		setWorkingDirectory(baseConfiguration.getWorkingDirectory());
		setVectorialLayer(baseConfiguration.isVectorialLayer());
    }
    
	/**
	 * @param geoserverVersion the geoserverVersion to set
	 */
	public void setGeoserverVersion(String geoserverVersion) {
		this.geoserverVersion = geoserverVersion;
	}


	/**
	 * @return the geoserverVersion
	 */
	public String getGeoserverVersion() {
		return geoserverVersion;
	}

	@Override
	public GeoServerRESTActionConfiguration clone() { // throws CloneNotSupportedException {
//		final GeoServerRESTActionConfiguration configuration =
//			new GeoServerRESTActionConfiguration(super.getId(),super.getName(),super.getDescription(),super.isDirty());
		final GeoServerRESTActionConfiguration configuration =
                (GeoServerRESTActionConfiguration)super.clone();

		configuration.setGeoserverVersion(geoserverVersion);
		configuration.setQueryString(queryString);
		
		return configuration;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +"["
				+ "id:" + getId()
				+ " name:" + getName()
				+ " srvId:" + getServiceID()
				+ " wkdir:" + getWorkingDirectory()
				+ " GSurl:" + getGeoserverURL()
				+"]";
	}

	/**
	 * @param storeType the storeType to set
	 */
	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}

	/**
	 * @return the storeType
	 */
	public String getStoreType() {
		return storeType;
	}

	/**
	 * @param storeId the storeId to set
	 */
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	/**
	 * @return the storeId
	 */
	public String getStoreId() {
		return storeId;
	}

	/**
	 * @param vectorialLayer the vectorialLayer to set
	 */
	public void setVectorialLayer(Boolean vectorialLayer) {
		this.vectorialLayer = vectorialLayer;
	}

	/**
	 * @return the vectorialLayer
	 */
	public Boolean isVectorialLayer() {
		return vectorialLayer;
	}

	/**
	 * @param queryString the queryString to set
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		return queryString;
	}
}
