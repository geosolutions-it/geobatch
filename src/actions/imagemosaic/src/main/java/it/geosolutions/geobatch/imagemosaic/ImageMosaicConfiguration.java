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

public class ImageMosaicConfiguration extends GeoServerActionConfiguration {

	public ImageMosaicConfiguration(String id, String name,
			String description) {
		super(id, name, description);
	}


    /**
     * Only used when updating.
     * We'll assume the layer exists, and the reset will not be performed.
     */
    private boolean ignoreGeoServer = false;
	/**
	 * Try to use COARDS file name convention to parse file name. This is used
	 * to get ImageMosaicGranulesDescription info
	 */
	private boolean COARDS;

	private String datastorePropertiesPath;

	private String timeRegex;

	private String elevationRegex;

	private String runtimeRegex;

	private String backgroundValue;// NoData

	private String projectionPolicy;// NONE, REPROJECT_TO_DECLARED,
									// FORCE_DECLARED

	private Double NativeMinBoundingBoxX;// BoundingBox

	private Double NativeMinBoundingBoxY;// BoundingBox

	private Double NativeMaxBoundingBoxX;// BoundingBox

	private Double NativeMaxBoundingBoxY;// BoundingBox

	public String getProjectionPolicy() {
		return projectionPolicy;
	}

	public void setProjectionPolicy(String projectionPolicy) {
		this.projectionPolicy = projectionPolicy;
	}

	private Double latLonMinBoundingBoxX;// BoundingBox

	private Double latLonMinBoundingBoxY;// BoundingBox

	private Double latLonMaxBoundingBoxX;// BoundingBox

	private Double latLonMaxBoundingBoxY;// BoundingBox

	// <metadata>
	// <entry key="timeDimEnabled">true</entry>
	// <entry key="dirName">20101012T210000_wdi_20101012T210000_wdi</entry>
	// <entry key="timePresentationMode">LIST</entry>
	// </metadata>
	private String timeDimEnabled;

	private String elevDimEnabled;

	public String getElevDimEnabled() {
		return elevDimEnabled;
	}

	public void setElevDimEnabled(String elevationDimEnabled) {
		this.elevDimEnabled = elevationDimEnabled;
	}

	private String timePresentationMode;

	private String elevationPresentationMode;

	public String getElevationPresentationMode() {
		return elevationPresentationMode;
	}

	public void setElevationPresentationMode(String elevationPresentationMode) {
		this.elevationPresentationMode = elevationPresentationMode;
	}

	// TODO removeme
	private String dirName;

	private String outputTransparentColor;

	private String inputTransparentColor;

	private boolean allowMultithreading;

	private boolean useJaiImageRead;

	private int tileSizeH;

	private int tileSizeW;

	public boolean isCOARDS() {
		return COARDS;
	}

	public void setCOARDS(boolean cOARDS) {
		COARDS = cOARDS;
	}

	public Double getNativeMinBoundingBoxX() {
		return NativeMinBoundingBoxX;
	}

	public void setNativeMinBoundingBoxX(Double nativeMinBoundingBoxX) {
		NativeMinBoundingBoxX = nativeMinBoundingBoxX;
	}

	public Double getNativeMinBoundingBoxY() {
		return NativeMinBoundingBoxY;
	}

	public void setNativeMinBoundingBoxY(Double nativeMinBoundingBoxY) {
		NativeMinBoundingBoxY = nativeMinBoundingBoxY;
	}

	public Double getNativeMaxBoundingBoxX() {
		return NativeMaxBoundingBoxX;
	}

	public void setNativeMaxBoundingBoxX(Double nativeMaxBoundingBoxX) {
		NativeMaxBoundingBoxX = nativeMaxBoundingBoxX;
	}

	public Double getNativeMaxBoundingBoxY() {
		return NativeMaxBoundingBoxY;
	}

	public void setNativeMaxBoundingBoxY(Double nativeMaxBoundingBoxY) {
		NativeMaxBoundingBoxY = nativeMaxBoundingBoxY;
	}

	public Double getLatLonMinBoundingBoxX() {
		return latLonMinBoundingBoxX;
	}

	public void setLatLonMinBoundingBoxX(Double latLonMinBoundingBoxX) {
		this.latLonMinBoundingBoxX = latLonMinBoundingBoxX;
	}

	public Double getLatLonMinBoundingBoxY() {
		return latLonMinBoundingBoxY;
	}

	public void setLatLonMinBoundingBoxY(Double latLonMinBoundingBoxY) {
		this.latLonMinBoundingBoxY = latLonMinBoundingBoxY;
	}

	public Double getLatLonMaxBoundingBoxX() {
		return latLonMaxBoundingBoxX;
	}

	public void setLatLonMaxBoundingBoxX(Double latLonMaxBoundingBoxX) {
		this.latLonMaxBoundingBoxX = latLonMaxBoundingBoxX;
	}

	public Double getLatLonMaxBoundingBoxY() {
		return latLonMaxBoundingBoxY;
	}

	public void setLatLonMaxBoundingBoxY(Double latLonMaxBoundingBoxY) {
		this.latLonMaxBoundingBoxY = latLonMaxBoundingBoxY;
	}

	public String getTimeDimEnabled() {
		return timeDimEnabled;
	}

	public void setTimeDimEnabled(String timeDimEnabled) {
		this.timeDimEnabled = timeDimEnabled;
	}

	public String getDirName() {
		return dirName;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	public String getTimePresentationMode() {
		return timePresentationMode;
	}

	public void setTimePresentationMode(String timePresentationMode) {
		this.timePresentationMode = timePresentationMode;
	}

	public String getOutputTransparentColor() {
		return outputTransparentColor;
	}

	public void setOutputTransparentColor(String outputTransparentColor) {
		this.outputTransparentColor = outputTransparentColor;
	}

	public String getInputTransparentColor() {
		return inputTransparentColor;
	}

	public void setInputTransparentColor(String inputTransparentColor) {
		this.inputTransparentColor = inputTransparentColor;
	}

	public boolean isAllowMultithreading() {
		return allowMultithreading;
	}

	public void setAllowMultithreading(boolean allowMultithreading) {
		this.allowMultithreading = allowMultithreading;
	}

	public boolean isUseJaiImageRead() {
		return useJaiImageRead;
	}

	public void setUseJaiImageRead(boolean useJaiImageRead) {
		this.useJaiImageRead = useJaiImageRead;
	}

	public int getTileSizeH() {
		return tileSizeH;
	}

	public void setTileSizeH(int tileSizeH) {
		this.tileSizeH = tileSizeH;
	}

	public int getTileSizeW() {
		return tileSizeW;
	}

	public void setTileSizeW(int tileSizeW) {
		this.tileSizeW = tileSizeW;
	}

	public String getBackgroundValue() {
		return backgroundValue;
	}

	public void setBackgroundValue(String backgroundValue) {
		this.backgroundValue = backgroundValue;
	}

	/**
	 * @param datastorePropertiesPath
	 *            the datastorePropertiesPath to set
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
	 * @param timeRegex
	 *            the timeRegex to set
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
	 * @param elevationRegex
	 *            the elevationRegex to set
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

	/**
	 * @param runtimeRegex
	 *            the runtimeRegex to set
	 */
	public void setRuntimeRegex(String runtimeRegex) {
		this.runtimeRegex = runtimeRegex;
	}

	/**
	 * @return the runtimeRegex
	 */
	public String getRuntimeRegex() {
		return runtimeRegex;
	}

    /**
     * Only used when updating.
     * We'll assume the layer exists, and the reset will not be performed.
     */
    public boolean getIgnoreGeoServer() {
        return ignoreGeoServer;
    }

    public void setIgnoreGeoServer(boolean ignoreGeoServer) {
        this.ignoreGeoServer = ignoreGeoServer;
    }

	@Override
	public ImageMosaicConfiguration clone() {
		return (ImageMosaicConfiguration)super.clone();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() 
                + "["
                + "id:" + getId()
                + " name:" + getName()
                + " srvId:" + getServiceID()
                + " GS:" + getGeoserverUID() + "@"+ getGeoserverURL()
                + " ignoreGS:" + ignoreGeoServer
                + "]";
	}

}
