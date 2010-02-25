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
package it.geosolutions.geobatch.wmc;


import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

/**
 * A Base Configuration class sharing common configuration's parameters
 *
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public class WMCActionConfiguration extends ActionConfiguration implements Configuration {

	private String workingDirectory;
	
    private String crs;

    private String boundingBox;

    private String geoserverURL;

    private String width;

    private String height;
    
    private String baseLayerId;

    private String baseLayerURL;

    private String baseLayerTitle;

    private String baseLayerFormat;
    
    private String outputDirectory;

	public WMCActionConfiguration(String id, String name, String description, boolean dirty) {
		super(id, name, description, dirty);
	}

	public WMCActionConfiguration() {
		super();
	}

	/**
	 * @return the workingDirectory
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	/**
	 * @param workingDirectory
	 *            the workingDirectory to set
	 */
	public void setWorkingDirectory(final String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public String getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(String boundingBox) {
		this.boundingBox = boundingBox;
	}


	public String getGeoserverURL() {
		return geoserverURL;
	}

	public void setGeoserverURL(String geoserverURL) {
		this.geoserverURL = geoserverURL;
	}

	@Override
	public ActionConfiguration clone() throws CloneNotSupportedException {
		 final WMCActionConfiguration configuration = new WMCActionConfiguration(getId(), getName(), getDescription(), isDirty());
	        configuration.setServiceID(getServiceID());
	        configuration.setWorkingDirectory(workingDirectory);
	        configuration.setBoundingBox(boundingBox);
	        configuration.setCrs(crs);
	        configuration.setGeoserverURL(geoserverURL);
	        configuration.setWidth(width);
	        configuration.setHeight(height);
	        configuration.setBaseLayerFormat(baseLayerFormat);
	        configuration.setBaseLayerId(baseLayerId);
	        configuration.setBaseLayerTitle(baseLayerTitle);
	        configuration.setBaseLayerURL(baseLayerURL);
	        configuration.setOutputDirectory(outputDirectory);
	        return configuration;
	    }

	/**
	 * @param width the width to set
	 */
	public void setWidth(String width) {
		this.width = width;
	}

	/**
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(String height) {
		this.height = height;
	}

	/**
	 * @return the height
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * @param baseLayerId the baseLayerId to set
	 */
	public void setBaseLayerId(String baseLayerId) {
		this.baseLayerId = baseLayerId;
	}

	/**
	 * @return the baseLayerId
	 */
	public String getBaseLayerId() {
		return baseLayerId;
	}

	/**
	 * @param baseLayerURL the baseLayerURL to set
	 */
	public void setBaseLayerURL(String baseLayerURL) {
		this.baseLayerURL = baseLayerURL;
	}

	/**
	 * @return the baseLayerURL
	 */
	public String getBaseLayerURL() {
		return baseLayerURL;
	}

	/**
	 * @param baseLayerTitle the baseLayerTitle to set
	 */
	public void setBaseLayerTitle(String baseLayerTitle) {
		this.baseLayerTitle = baseLayerTitle;
	}

	/**
	 * @return the baseLayerTitle
	 */
	public String getBaseLayerTitle() {
		return baseLayerTitle;
	}

	/**
	 * @param baseLayerFormat the baseLayerFormat to set
	 */
	public void setBaseLayerFormat(String baseLayerFormat) {
		this.baseLayerFormat = baseLayerFormat;
	}

	/**
	 * @return the baseLayerFormat
	 */
	public String getBaseLayerFormat() {
		return baseLayerFormat;
	}

	/**
	 * @param outputDirectory the outputDirectory to set
	 */
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/**
	 * @return the outputDirectory
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}

}