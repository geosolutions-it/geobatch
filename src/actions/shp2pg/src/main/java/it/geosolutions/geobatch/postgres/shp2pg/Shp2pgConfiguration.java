/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.postgres.shp2pg;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.util.List;

/**
 * 
 * @author Andrea Di Nora - andrea.dinora@sinergis.it
 * 
 */

public class Shp2pgConfiguration extends ActionConfiguration {

    private String workingDirectory;

    private String storeFilePrefix;

    private String configId;

    // Postgis params
    private String dbPWD;

    private String dbUID;

    private String dbServerIp;

    private String dbPort;

    private String dbName;

    private String dbType;

    // Geoserver params

    private String crs;

    private String envelope;

    private String geoserverPWD;

    private String geoserverUID;

    private String geoserverURL;

    private String datatype;

    private String defaultNamespace;

    private String defaultNamespaceUri;

    private String defaultStyle;

    private String wmsPath;

    private List styles;

    private String dataTransferMethod;

    public Shp2pgConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getDbPWD() {
        return dbPWD;
    }

    public void setDbPWD(String dbPWD) {
        this.dbPWD = dbPWD;
    }

    public String getDbUID() {
        return dbUID;
    }

    public void setDbUID(String dbUID) {
        this.dbUID = dbUID;
    }

    public String getDbServerIp() {
        return dbServerIp;
    }

    public void setDbServerIp(String dbServerIp) {
        this.dbServerIp = dbServerIp;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getStoreFilePrefix() {
        return storeFilePrefix;
    }

    public void setStoreFilePrefix(String storeFilePrefix) {
        this.storeFilePrefix = storeFilePrefix;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getEnvelope() {
        return envelope;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }

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

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public String getDefaultNamespaceUri() {
        return defaultNamespaceUri;
    }

    public void setDefaultNamespaceUri(String defaultNamespaceUri) {
        this.defaultNamespaceUri = defaultNamespaceUri;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public String getWmsPath() {
        return wmsPath;
    }

    public void setWmsPath(String wmsPath) {
        this.wmsPath = wmsPath;
    }

    public List getStyles() {
        return styles;
    }

    public void setStyles(List styles) {
        this.styles = styles;
    }

    public String getDataTransferMethod() {
        return dataTransferMethod;
    }

    public void setDataTransferMethod(String dataTransferMethod) {
        this.dataTransferMethod = dataTransferMethod;
    }

    public Shp2pgConfiguration clone() {
        Shp2pgConfiguration configuration = (Shp2pgConfiguration) super.clone();
        configuration.setConfigId(configId);
        configuration.setDbName(dbName);
        configuration.setDbPort(dbPort);
        configuration.setDbPWD(dbPWD);
        configuration.setDbServerIp(dbServerIp);
        configuration.setDbType(dbType);
        configuration.setDbUID(dbUID);
        configuration.setStoreFilePrefix(storeFilePrefix);
        configuration.setWorkingDirectory(workingDirectory);
        return configuration;
    }
}
