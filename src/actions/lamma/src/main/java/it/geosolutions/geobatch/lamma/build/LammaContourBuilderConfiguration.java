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

package it.geosolutions.geobatch.lamma.build;

import it.geosolutions.geobatch.lamma.base.LammaBaseConfiguration;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;




/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions
 */
public class LammaContourBuilderConfiguration extends LammaBaseConfiguration {

	/**
	 * Custom fields
	 */
    private String geoserverPWD;
    
    private String geoserverUID;
    
    private String geoserverURL;
    
    private String wmsPath;
    
    private String defaultNamespace;
    
    private String defaultStyle;
    
	public LammaContourBuilderConfiguration() {
        super();
    }

    protected LammaContourBuilderConfiguration(String id, String name, String description,
            boolean dirty) {
        super(id, name, description, dirty);
    }

    /**
	 * @param geoserverPWD the geoserverPWD to set
	 */
	public void setGeoserverPWD(String geoserverPWD) {
		this.geoserverPWD = geoserverPWD;
	}

	/**
	 * @return the geoserverPWD
	 */
	public String getGeoserverPWD() {
		return geoserverPWD;
	}

	/**
	 * @param geoserverUID the geoserverUID to set
	 */
	public void setGeoserverUID(String geoserverUID) {
		this.geoserverUID = geoserverUID;
	}

	/**
	 * @return the geoserverUID
	 */
	public String getGeoserverUID() {
		return geoserverUID;
	}

	/**
	 * @param geoserverURL the geoserverURL to set
	 */
	public void setGeoserverURL(String geoserverURL) {
		this.geoserverURL = geoserverURL;
	}

	/**
	 * @return the geoserverURL
	 */
	public String getGeoserverURL() {
		return geoserverURL;
	}

	/**
	 * @param wmsPath the wmsPath to set
	 */
	public void setWmsPath(String wmsPath) {
		this.wmsPath = wmsPath;
	}

	/**
	 * @return the wmsPath
	 */
	public String getWmsPath() {
		return wmsPath;
	}

	/**
	 * @param defaultNamespace the defaultNamespace to set
	 */
	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	/**
	 * @return the defaultNamespace
	 */
	public String getDefaultNamespace() {
		return defaultNamespace;
	}

	/**
	 * @param defaultStyle the defaultStyle to set
	 */
	public void setDefaultStyle(String defaultStyle) {
		this.defaultStyle = defaultStyle;
	}

	/**
	 * @return the defaultStyle
	 */
	public String getDefaultStyle() {
		return defaultStyle;
	}

	@Override
    public LammaContourBuilderConfiguration clone() { // throws CloneNotSupportedException {
    	try {
			return (LammaContourBuilderConfiguration) BeanUtils.cloneBean(this);
		} catch (IllegalAccessException e) {
			final RuntimeException cns= new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (InstantiationException e) {
			final RuntimeException cns= new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (InvocationTargetException e) {
			final RuntimeException cns= new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (NoSuchMethodException e) {
			final RuntimeException cns= new RuntimeException();
			cns.initCause(e);
			throw cns;
		}
    }
}
