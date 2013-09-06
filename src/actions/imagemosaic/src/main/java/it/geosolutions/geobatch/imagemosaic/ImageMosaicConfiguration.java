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

package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.imagemosaic.config.DomainAttribute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageMosaicConfiguration extends GeoServerActionConfiguration {

    protected final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicConfiguration.class);

    private final static Boolean DEFAULT_IGNORE_GEOSERVER = Boolean.FALSE;

    /**
     * Only used when updating. We'll assume the layer exists, and the reset
     * will not be performed.
     */
    private Boolean ignoreGeoServer = DEFAULT_IGNORE_GEOSERVER;
    /**
     * Try to use COARDS file name convention to parse file name. This is used
     * to get ImageMosaicGranulesDescription info
     */
    private Boolean COARDS;
    
    private String datastorePropertiesPath;


    private String backgroundValue;// NoData

    private String projectionPolicy;// NONE, REPROJECT_TO_DECLARED,
                                    // FORCE_DECLARED

    List<DomainAttribute> domainAttributes;

    private Double NativeMinBoundingBoxX;// BoundingBox
    private Double NativeMinBoundingBoxY;// BoundingBox
    private Double NativeMaxBoundingBoxX;// BoundingBox
    private Double NativeMaxBoundingBoxY;// BoundingBox

    private Double latLonMinBoundingBoxX;// BoundingBox
    private Double latLonMinBoundingBoxY;// BoundingBox
    private Double latLonMaxBoundingBoxX;// BoundingBox
    private Double latLonMaxBoundingBoxY;// BoundingBox

    // <metadata>
    // <entry key="timeDimEnabled">true</entry>
    // <entry key="dirName">20101012T210000_wdi_20101012T210000_wdi</entry>
    // <entry key="timePresentationMode">LIST</entry>
    // </metadata>

    /** @deprecated use {@link #domainAttributes} */
    private String runtimeRegex;

    /** @deprecated use {@link #domainAttributes} */
    private String timeRegex;
    /** @deprecated use {@link #domainAttributes} */
    private String timeDimEnabled;
    /** @deprecated use {@link #domainAttributes} */
    private String timeAttribute;
    /** @deprecated use {@link #domainAttributes} */
    private String timePresentationMode;
    /** @deprecated use {@link #domainAttributes} */
    private BigDecimal timeDiscreteInterval;

    /** @deprecated use {@link #domainAttributes} */
    private String elevationRegex;
    /** @deprecated use {@link #domainAttributes} */
    private String elevDimEnabled;
    /** @deprecated use {@link #domainAttributes} */
    private String elevationAttribute;
    /** @deprecated use {@link #domainAttributes} */
    private String elevationPresentationMode;
    /** @deprecated use {@link #domainAttributes} */
    private BigDecimal elevationDiscreteInterval;

    private String granuleFormat;

    // TODO remove me
    /** @deprecated */
    private String dirName;

    private String outputTransparentColor;
    private String inputTransparentColor;

    private Boolean allowMultithreading;

    private Boolean useJaiImageRead;

    private Integer tileSizeH;
    private Integer tileSizeW;

    private boolean ignoreEmptyAddList = true;

	public ImageMosaicConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    public String getProjectionPolicy() {
        return projectionPolicy;
    }

    public void setProjectionPolicy(String projectionPolicy) {
        this.projectionPolicy = projectionPolicy;
    }

    public boolean isCOARDS() {
        if (COARDS==null)
            return false;
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


    /** @deprecated */
    public String getDirName() {
        return dirName;
    }

    /** @deprecated */
    public void setDirName(String dirName) {
        this.dirName = dirName;
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
        if (allowMultithreading==null)
            return false;
        return allowMultithreading;
    }

    public void setAllowMultithreading(boolean allowMultithreading) {
        this.allowMultithreading = allowMultithreading;
    }

    public boolean isUseJaiImageRead() {
        if (useJaiImageRead==null)
            return false;
        return useJaiImageRead;
    }

    public void setUseJaiImageRead(boolean useJaiImageRead) {
        this.useJaiImageRead = useJaiImageRead;
    }

    public Integer getTileSizeH() {
        if (tileSizeH==null)
            return 0;
        return tileSizeH;
    }

    public void setTileSizeH(int tileSizeH) {
        this.tileSizeH = tileSizeH;
    }

    public Integer getTileSizeW() {
        if (tileSizeW==null)
            return 0;
        return tileSizeW;
    }

    public void setTileSizeW(int tileSizeW) {
        this.tileSizeW = tileSizeW;
    }


    public void setTileSizeH(Integer tileSizeH) {
        this.tileSizeH = tileSizeH;
    }

    public void setTileSizeW(Integer tileSizeW) {
        this.tileSizeW = tileSizeW;
    }
    
    public String getBackgroundValue() {
        return backgroundValue;
    }

    public void setBackgroundValue(String backgroundValue) {
        this.backgroundValue = backgroundValue;
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

    private String getElevDimEnabled() {
        return elevDimEnabled;
    }

    public void setElevDimEnabled(String elevationDimEnabled) {
        this.elevDimEnabled = elevationDimEnabled;
    }

    private String getElevationPresentationMode() {
        return elevationPresentationMode;
    }

    public void setElevationPresentationMode(String elevationPresentationMode) {
        this.elevationPresentationMode = elevationPresentationMode;
    }

    private BigDecimal getTimeDiscreteInterval() {
        return timeDiscreteInterval;
    }

    public void setTimeDiscreteInterval(BigDecimal timeDiscreteInterval) {
        this.timeDiscreteInterval = timeDiscreteInterval;
    }

    private BigDecimal getElevationDiscreteInterval() {
        return elevationDiscreteInterval;
    }

    public void setElevationDiscreteInterval(BigDecimal elevationDiscreteInterval) {
        this.elevationDiscreteInterval = elevationDiscreteInterval;
    }
    private String getTimeDimEnabled() {
        return timeDimEnabled;
    }

    public void setTimeDimEnabled(String timeDimEnabled) {
        this.timeDimEnabled = timeDimEnabled;
    }
    private String getTimePresentationMode() {
        return timePresentationMode;
    }

    public void setTimePresentationMode(String timePresentationMode) {
        this.timePresentationMode = timePresentationMode;
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
    private String getTimeRegex() {
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
    private String getElevationRegex() {
        return elevationRegex;
    }

    /**
     * @param runtimeRegex the runtimeRegex to set
     */
    public void setRuntimeRegex(String runtimeRegex) {
        this.runtimeRegex = runtimeRegex;
    }

    /**
     * @return the runtimeRegex
     */
    private String getRuntimeRegex() {
        return runtimeRegex;
    }

    private String getTimeAttribute() {
        return timeAttribute;
    }

    public void setTimeAttribute(String timeAttribute) {
        this.timeAttribute = timeAttribute;
    }

    private String getElevationAttribute() {
        return elevationAttribute;
    }

    public void setElevationAttribute(String elevationAttribute) {
        this.elevationAttribute = elevationAttribute;
    }

    /**
     * Only used when updating. We'll assume the layer exists, and the reset
     * will not be performed.
     */
    public Boolean getIgnoreGeoServer() {
        return ignoreGeoServer == null? DEFAULT_IGNORE_GEOSERVER : ignoreGeoServer;
    }

    public void setIgnoreGeoServer(Boolean ignoreGeoServer) {
        this.ignoreGeoServer = ignoreGeoServer;
    }

    public Boolean getCOARDS() {
        return COARDS;
    }

    public Boolean getAllowMultithreading() {
        return allowMultithreading;
    }

    public Boolean getUseJaiImageRead() {
        return useJaiImageRead;
    }
    
    /**
     * @return the granuleFormat
     */
    public String getGranuleFormat() {
        return granuleFormat;
    }

    /**
     * @param granuleFormat the granuleFormat to set
     */
    public void setGranuleFormat(String granuleFormat) {
        this.granuleFormat = granuleFormat;
    }

    public boolean isIgnoreEmptyAddList() {
        return ignoreEmptyAddList;
    }

    public void setIgnoreEmptyAddList(boolean ignoreEmptyAddList) {
        this.ignoreEmptyAddList = ignoreEmptyAddList;
    }

    public List<DomainAttribute> getDomainAttributes() {
        return domainAttributes;
    }

    public void setDomainAttributes(List<DomainAttribute> domainAttributes) {
        this.domainAttributes = domainAttributes;
    }

    public void addDomainAttribute(DomainAttribute attr) {
        if(domainAttributes == null)
            domainAttributes = new ArrayList<DomainAttribute>();

        domainAttributes.add(attr);
    }



    @Override
    public ImageMosaicConfiguration clone() {
        ImageMosaicConfiguration clone = (ImageMosaicConfiguration)super.clone();
        // deep copy
        if(domainAttributes != null) {
            clone.domainAttributes = new ArrayList<DomainAttribute>();
            for (DomainAttribute attr : domainAttributes) {
                clone.domainAttributes.add(attr.clone());
            }
        }

        return clone;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + " name:" + getName() + " srvId:"
               + getServiceID() + " GS:" + getGeoserverUID() + "@" + getGeoserverURL() + " ignoreGS:"
               + ignoreGeoServer + "]";
    }

    public void fixObsoleteConfig() {

        if (!CollectionUtils.isEmpty(getDomainAttributes())) {
            return;
        }

        setDomainAttributes(new ArrayList<DomainAttribute>());

        if ( getTimeRegex() != null ) {
            LOGGER.warn("ImageMosaicCommand contains deprecated TIME dim configuration. PLEASE FIX IT.");

            DomainAttribute attr = new DomainAttribute();
            attr.setDimensionName(DomainAttribute.DIM_TIME);
            if(getTimeAttribute() != null)
                attr.setAttribName(getTimeAttribute());
            else
                attr.setAttribName(DomainAttribute.DIM_TIME);

            attr.setRegEx(getTimeRegex());
            attr.setType(DomainAttribute.TYPE.DATE);
            attr.setPresentationMode(getTimePresentationMode());
            attr.setDiscreteInterval(getTimeDiscreteInterval());
            domainAttributes.add(attr);
        }

        if ( getElevationRegex() != null ) {
            LOGGER.warn("ImageMosaicCommand contains deprecated ELEVATION dim configuration. PLEASE FIX IT.");

            DomainAttribute attr = new DomainAttribute();
            attr.setDimensionName(DomainAttribute.DIM_ELEV);
            if(getElevationAttribute() != null)
                attr.setAttribName(getElevationAttribute());
            else
                attr.setAttribName(DomainAttribute.DIM_ELEV);

            attr.setRegEx(getElevationRegex());
            attr.setType(DomainAttribute.TYPE.DOUBLE);
            attr.setPresentationMode(getElevationPresentationMode());
            attr.setDiscreteInterval(getElevationDiscreteInterval());
            domainAttributes.add(attr);
        }

        if ( getRuntimeRegex() != null ) {
            LOGGER.warn("ImageMosaicCommand contains deprecated RUNTIME dim configuration. PLEASE FIX IT.");

            DomainAttribute attr = new DomainAttribute();
            attr.setDimensionName(DomainAttribute.DIM_RUNTIME);
            attr.setAttribName(DomainAttribute.DIM_RUNTIME);
            attr.setRegEx(getRuntimeRegex());
            attr.setType(DomainAttribute.TYPE.DATE);
            domainAttributes.add(attr);
        }
    }

}
