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
package it.geosolutions.geobatch.geoserver.shapefile;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions S.a.S.
 */
public class ShapeFileConfiguration extends ActionConfiguration implements Configuration {

    private String workingDirectory;

<<<<<<< HEAD
    private String namespace;

    public ShapeFileConfiguration(String id, String name, String description) {
        super(id, name, description);
=======
    private String defaultWorkspaceName;

    private String defaultStoreName;

//    private String layerName;

//    private String nativeCrs;

    private String defaultStyle;

    private String geoserverURL;

    private String geoserverPWD;

    private String geoserverUID;

    public String getGeoserverPWD() {
        return geoserverPWD;
    }

    public void setGeoserverPWD(String geoserverPWD) {
        this.geoserverPWD = geoserverPWD;
    }

    public String getGeoserverUID() {
        return geoserverUID;
    }

    public void setGeoserverUID(String geoserverUID) {
        this.geoserverUID = geoserverUID;
    }

    public String getGeoserverURL() {
        return geoserverURL;
    }

    public void setGeoserverURL(String geoserverURL) {
        this.geoserverURL = geoserverURL;
    }

    public String getWorkspace() {
        return defaultWorkspaceName;
    }

    public void setWorkspace(String workspace) {
        this.defaultWorkspaceName = workspace;
    }

    public String getStorename() {
        return defaultStoreName;
    }

    public void setStorename(String storename) {
        this.defaultStoreName = storename;
    }

//    public String getLayerName() {
//        return layerName;
//    }

//    public void setLayerName(String layerName) {
//        this.layerName = layerName;
//    }

//    public String getNativeCrs() {
//        return nativeCrs;
//    }
//
//    public void setNativeCrs(String nativeCrs) {
//        this.nativeCrs = nativeCrs;
//    }

    public String getDefaultStyle() {
        return defaultStyle;
>>>>>>> 15f5136... Fixed Octave test and some internal function. Added new geoservermanager dependency
    }
    
    /**
     * @param namespace
     *            the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
<<<<<<< HEAD
	 * 
	 */
    public ShapeFileConfiguration clone() { // throws CloneNotSupportedException
        // {
        try {
            return (ShapeFileConfiguration) BeanUtils.cloneBean(this);
        } catch (IllegalAccessException e) {
            final RuntimeException cns = new RuntimeException();
            cns.initCause(e);
            throw cns;
        } catch (InstantiationException e) {
            final RuntimeException cns = new RuntimeException();
            cns.initCause(e);
            throw cns;
        } catch (InvocationTargetException e) {
            final RuntimeException cns = new RuntimeException();
            cns.initCause(e);
            throw cns;
        } catch (NoSuchMethodException e) {
            final RuntimeException cns = new RuntimeException();
            cns.initCause(e);
            throw cns;
        }
=======
     * 
     */
    public ShapeFileConfiguration clone() {
        final ShapeFileConfiguration conf=new ShapeFileConfiguration(this.getId(), this.getName(), this.getDescription());
        conf.setGeoserverPWD(this.getGeoserverPWD());
        conf.setGeoserverUID(this.getGeoserverUID());
        conf.setGeoserverURL(this.getGeoserverURL());
//        conf.setLayerName(this.getLayerName());
//        conf.setNativeCrs(this.getNativeCrs());
        conf.setServiceID(this.getServiceID());
        conf.setStorename(this.getStorename());
        conf.setWorkingDirectory(this.getWorkingDirectory());
        conf.setWorkspace(this.getWorkspace());
        return conf;
//        try {
//            return (ShapeFileConfiguration) BeanUtils.cloneBean(this);
//        } catch (IllegalAccessException e) {
//            final RuntimeException cns = new RuntimeException();
//            cns.initCause(e);
//            throw cns;
//        } catch (InstantiationException e) {
//            final RuntimeException cns = new RuntimeException();
//            cns.initCause(e);
//            throw cns;
//        } catch (InvocationTargetException e) {
//            final RuntimeException cns = new RuntimeException();
//            cns.initCause(e);
//            throw cns;
//        } catch (NoSuchMethodException e) {
//            final RuntimeException cns = new RuntimeException();
//            cns.initCause(e);
//            throw cns;
//        }
>>>>>>> 15f5136... Fixed Octave test and some internal function. Added new geoservermanager dependency
    }

}
