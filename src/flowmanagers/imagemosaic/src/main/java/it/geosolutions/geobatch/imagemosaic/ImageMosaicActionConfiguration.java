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



package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;

public class ImageMosaicActionConfiguration extends GeoServerActionConfiguration {

    protected ImageMosaicActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
	}

    private String datastorePropertiesPath;
    
    private String timeRegex;

    private String elevationRegex;
    
    private double backgroundValue;

    public double getBackgroundValue() {
		return backgroundValue;
	}

	public void setBackgroundValue(double backgroundValue) {
		this.backgroundValue = backgroundValue;
	}

	public ImageMosaicActionConfiguration() {
        super();
    }

	/**
	 * @param datastorePropertiesPath the datastorePropertiesPath to set
	 */
	public void setDatastorePropertiesPath(String datastorePropertiesPath) {
		this.datastorePropertiesPath = datastorePropertiesPath;
	}

	/**
	 * @return the datastorePropertiesPath
	 */
	public String getDatastorePropertiesPath() {
		return datastorePropertiesPath;
	}

	/**
	 * @param timeRegex the timeRegex to set
	 */
	public void setTimeRegex(String timeRegex) {
		this.timeRegex = timeRegex;
	}

	/**
	 * @return the timeRegex
	 */
	public String getTimeRegex() {
		return timeRegex;
	}

	/**
	 * @param elevationRegex the elevationRegex to set
	 */
	public void setElevationRegex(String elevationRegex) {
		this.elevationRegex = elevationRegex;
	}

	/**
	 * @return the elevationRegex
	 */
	public String getElevationRegex() {
		return elevationRegex;
	}

	@Override
	public ImageMosaicActionConfiguration clone() throws CloneNotSupportedException {
		final ImageMosaicActionConfiguration configuration = 
			new ImageMosaicActionConfiguration(super.getId(),super.getName(),super.getDescription(),super.isDirty());
		configuration.setCrs(getCrs());
		configuration.setDataTransferMethod(getDataTransferMethod());
		configuration.setDatatype(getDatatype());
		configuration.setDefaultNamespace(getDefaultNamespace());
		configuration.setDefaultNamespaceUri(getDefaultNamespaceUri());
		configuration.setDefaultStyle(getDefaultStyle());
		configuration.setEnvelope(getEnvelope());
		configuration.setGeoserverPWD(getGeoserverPWD());
		configuration.setGeoserverUID(getGeoserverUID());
		configuration.setGeoserverURL(getGeoserverURL());
		configuration.setServiceID(getServiceID());
		configuration.setStoreFilePrefix(getStoreFilePrefix());
		configuration.setStyles(getStyles());
		configuration.setWmsPath(getWmsPath());
		configuration.setWorkingDirectory(getWorkingDirectory());
		configuration.setBackgroundValue(getBackgroundValue());
		configuration.setDatastorePropertiesPath(getDatastorePropertiesPath());
		configuration.setTimeRegex(getTimeRegex());
		configuration.setElevationRegex(getElevationRegex());
		
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

}
