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

package it.geosolutions.geobatch.imagemosaic.config;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An attribute to be extracted for the mosaic.
 *
 * For instance, these attributes
 * <pre>
 * {@code
 * TimeAttribute=time;endtime
 * ElevationAttribute=lowz;highz
 * AdditionalDomainAttributes=date,wavelength(loww;highw)
 * Schema= the_geom:Polygon,location:String,time:java.util.Date,endtime:java.util.Date,date:String,lowz:Integer,highz:Integer,loww:Integer,highw:Integer
 * PropertyCollectors=TimestampFileNameExtractorSPI[timeregex](time),TimestampFileNameExtractorSPI[timeregexend](endtime),StringFileNameExtractorSPI[dateregex](date),IntegerFileNameExtractorSPI[elevationregex](lowz),IntegerFileNameExtractorSPI[elevationregexhigh](highz),StringFileNameExtractorSPI[wavelengthregex](loww),StringFileNameExtractorSPI[wavelengthregexhigh](highw)
 * }
 * </pre>
 *
 * will be encoded this way:
 * <UL>
 * <LI>dimName=time, attribname=time</LI>
 * <LI>dimName=time, attribname=endtime</LI>
 * <LI>dimName=elev, attribname=lowz</LI>
 * <LI>dimName=elev, attribname=highz</LI>
 * <LI>dimName=date, attribname=date</LI>
 * <LI>dimName=wavelenght, attribname=loww</LI>
 * <LI>dimName=wavelenght, attribname=highw</LI>
 * </UL>
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class DomainAttribute implements Cloneable, Serializable{

    public final static String DIM_TIME = "time";
    public final static String DIM_ELEV = "elevation";

    public static enum TYPE {DATE, INTEGER, DOUBLE, STRING};

    /**
     * This is not a geoserver specifi dimension but is used in some custom flows
     * @deprecated it's a custom dimension name
     */
    public final static String DIM_RUNTIME = "runtime";

    private String dimensionName;

    private String attribName;
    private String regEx;

    private String endRangeAttribName;
    private String endRangeRegEx;

    private TYPE type;
    
    private String presentationMode;
    private BigDecimal discreteInterval;

    /**
     * The dimension name.
     * It is used to group pairs of attributes to define a range.
     * <p/>
     * For <b>time</b> or <b>elevation</b> please use the constants {@link #DIM_TIME DIM_TIME} and {@link #DIM_ELEV DIM_ELEV}.
     * <p/>
     * Should be left equal to the attrName if the attribute is not a range.
     *
     * @return
     */
    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }


    /**
     * The feature attribute name.
     */
    public String getAttribName() {
        return attribName;
    }

    public void setAttribName(String attribName) {
        this.attribName = attribName;
    }

    /**
     * The regex used to extract the value from the filename.
     */
    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regex) {
        this.regEx = regex;
    }

    /**
     * Presentation mode.
     *
     * Information for the mosaic publication in GeoServer.
     * <p/>
     * To be left null in ranged dimensions.
     */
    public String getPresentationMode() {
        return presentationMode;
    }

    public void setPresentationMode(String presentationMode) {
        this.presentationMode = presentationMode;
    }

    /**
     * Used when presentationMode is DISCRETE_INTERVAL.
     *
     * Information for the mosaic publication in GeoServer.
     */
    public BigDecimal getDiscreteInterval() {
        return discreteInterval;
    }

    public void setDiscreteInterval(BigDecimal discreteInterval) {
        this.discreteInterval = discreteInterval;
    }

    public String getEndRangeAttribName() {
        return endRangeAttribName;
    }

    public void setEndRangeAttribName(String endRangeAttribName) {
        this.endRangeAttribName = endRangeAttribName;
    }

    public String getEndRangeRegEx() {
        return endRangeRegEx;
    }

    public void setEndRangeRegEx(String endRangeRegEx) {
        this.endRangeRegEx = endRangeRegEx;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = TYPE.valueOf(type);
    }

    @Override
    public DomainAttribute clone() {
        try {
            return (DomainAttribute)super.clone();
        } catch (CloneNotSupportedException ex) { // should not happen
            Logger.getLogger(DomainAttribute.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.dimensionName != null ? this.dimensionName.hashCode() : 0);
        hash = 53 * hash + (this.attribName != null ? this.attribName.hashCode() : 0);
        hash = 53 * hash + (this.regEx != null ? this.regEx.hashCode() : 0);
        hash = 53 * hash + (this.endRangeAttribName != null ? this.endRangeAttribName.hashCode() : 0);
        hash = 53 * hash + (this.endRangeRegEx != null ? this.endRangeRegEx.hashCode() : 0);
        hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 53 * hash + (this.presentationMode != null ? this.presentationMode.hashCode() : 0);
        hash = 53 * hash + (this.discreteInterval != null ? this.discreteInterval.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DomainAttribute other = (DomainAttribute) obj;
        if ((this.dimensionName == null) ? (other.dimensionName != null) : !this.dimensionName.equals(other.dimensionName)) {
            return false;
        }
        if ((this.attribName == null) ? (other.attribName != null) : !this.attribName.equals(other.attribName)) {
            return false;
        }
        if ((this.regEx == null) ? (other.regEx != null) : !this.regEx.equals(other.regEx)) {
            return false;
        }
        if ((this.endRangeAttribName == null) ? (other.endRangeAttribName != null) : !this.endRangeAttribName.equals(other.endRangeAttribName)) {
            return false;
        }
        if ((this.endRangeRegEx == null) ? (other.endRangeRegEx != null) : !this.endRangeRegEx.equals(other.endRangeRegEx)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if ((this.presentationMode == null) ? (other.presentationMode != null) : !this.presentationMode.equals(other.presentationMode)) {
            return false;
        }
        if (this.discreteInterval != other.discreteInterval && (this.discreteInterval == null || !this.discreteInterval.equals(other.discreteInterval))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append('[');

        if(dimensionName != null)
            sb.append("dimName:").append(dimensionName);
        if(attribName != null)
            sb.append(" attrName:").append(attribName);
        if(regEx != null)
            sb.append(" regEx:").append(regEx);
        if(endRangeAttribName != null)
            sb.append(" endName:").append(endRangeAttribName);
        if(endRangeRegEx != null)
            sb.append(" endRegEx:").append(endRangeRegEx);
        if(type != null)
            sb.append(" type:").append(type);
        if(presentationMode != null)
            sb.append(" pres:").append(presentationMode);
        if(discreteInterval != null)
            sb.append(" discrInt:").append(discreteInterval);

        sb.append(']');
        return sb.toString();
    }


}
