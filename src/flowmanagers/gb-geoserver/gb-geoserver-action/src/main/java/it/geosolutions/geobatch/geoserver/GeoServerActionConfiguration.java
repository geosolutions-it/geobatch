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



package it.geosolutions.geobatch.geoserver;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.util.List;

public class GeoServerActionConfiguration extends ActionConfiguration implements Configuration {

    public GeoServerActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
	}

	private String workingDirectory;
	
    private String crs;

    private String envelope;

    private String geoserverPWD;

    private String geoserverUID;

    private String geoserverURL;

    private String storeFilePrefix;

    private String configId;

    private String datatype;

    private String defaultNamespace;

    private String defaultNamespaceUri;

    private String defaultStyle;

    private String wmsPath;

    private List<String> styles;

    private String dataTransferMethod;

    public GeoServerActionConfiguration() {
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
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
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

    public String getStoreFilePrefix() {
        return storeFilePrefix;
    }

    public void setStoreFilePrefix(String storeFilePrefix) {
        this.storeFilePrefix = storeFilePrefix;
    }

    public List<String> getStyles() {
        return styles;
    }

    public void setStyles(List<String> styles) {
        this.styles = styles;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
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

    public String getDataTransferMethod() {
        return dataTransferMethod;
    }

    public void setDataTransferMethod(String dataTransferMethod) {
        this.dataTransferMethod = dataTransferMethod;
    }

	@Override
	public GeoServerActionConfiguration clone() throws CloneNotSupportedException {
		final GeoServerActionConfiguration configuration = 
			new GeoServerActionConfiguration(super.getId(),super.getName(),super.getDescription(),super.isDirty());
		configuration.setCrs(crs);
		configuration.setDataTransferMethod(dataTransferMethod);
		configuration.setDatatype(datatype);
		configuration.setDefaultNamespace(defaultNamespace);
		configuration.setDefaultNamespaceUri(defaultNamespaceUri);
		configuration.setDefaultStyle(defaultStyle);
		configuration.setEnvelope(envelope);
		configuration.setGeoserverPWD(geoserverPWD);
		configuration.setGeoserverUID(geoserverUID);
		configuration.setGeoserverURL(geoserverURL);
		configuration.setServiceID(getServiceID());
		configuration.setStoreFilePrefix(storeFilePrefix);
		configuration.setStyles(styles);
		configuration.setWmsPath(wmsPath);
		configuration.setWorkingDirectory(workingDirectory);
		
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
