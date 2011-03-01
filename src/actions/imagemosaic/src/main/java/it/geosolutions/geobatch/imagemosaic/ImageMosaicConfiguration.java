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

    protected ImageMosaicConfiguration(String id, String name, String description,
            boolean dirty) {
        super(id, name, description, dirty);
    }

    private String datastorePropertiesPath;

    private String timeRegex;

    private String elevationRegex;

    private String runtimeRegex;

    private String backgroundValue;// NoData
    
//    <metadata>
//    <entry key="timeDimEnabled">true</entry>
//    <entry key="dirName">20101012T210000_wdi_20101012T210000_wdi</entry>
//    <entry key="timePresentationMode">LIST</entry>
//    </metadata>
    private String timeDimEnabled;
    
    
    private String dirName;
    private String timePresentationMode;
    
    private String outputTransparentColor;
    private String inputTransparentColor;
    private boolean allowMultithreading;
    private boolean useJaiImageRead;
    private int tileSizeH;
    private int tileSizeW;

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

    public ImageMosaicConfiguration() {
        super();
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

    @Override
    public ImageMosaicConfiguration clone() { 
        final ImageMosaicConfiguration configuration = (ImageMosaicConfiguration) super
                .clone();


        configuration.setBackgroundValue(getBackgroundValue());
        configuration.setDatastorePropertiesPath(getDatastorePropertiesPath());
        configuration.setTimeRegex(getTimeRegex());
        configuration.setElevationRegex(getElevationRegex());
        configuration.setRuntimeRegex(getRuntimeRegex());

        return configuration;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + " name:" + getName()
                + " srvId:" + getServiceID() + " wkdir:" + getWorkingDirectory() + " GSurl:"
                + getGeoserverURL() + "]";
    }

}
