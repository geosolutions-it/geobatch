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
package it.geosolutions.geobatch.metocs;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

public class MetocActionConfiguration extends ActionConfiguration implements Configuration {

    protected MetocActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
	}

    private boolean packComponents;
    
	private String workingDirectory;
	
    private String crs;

    private String envelope;

    private String storeFilePrefix;

    private String configId;
    
    private String metocDictionaryPath;

	private String metocHarvesterXMLTemplatePath;

    /**
	 * @return the metocDictionaryPath
	 */
	public String getMetocDictionaryPath() {
		return metocDictionaryPath;
	}

	/**
	 * @param metocDictionaryPath the metocDictionaryPath to set
	 */
	public void setMetocDictionaryPath(String metocDictionaryPath) {
		this.metocDictionaryPath = metocDictionaryPath;
	}

	/**
	 * @return the metocHarvesterXMLTemplatePath
	 */
	public String getMetocHarvesterXMLTemplatePath() {
		return metocHarvesterXMLTemplatePath;
	}

	/**
	 * @param metocHarvesterXMLTemplatePath the metocHarvesterXMLTemplatePath to set
	 */
	public void setMetocHarvesterXMLTemplatePath(
			String metocHarvesterXMLTemplatePath) {
		this.metocHarvesterXMLTemplatePath = metocHarvesterXMLTemplatePath;
	}

    public MetocActionConfiguration() {
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

    public boolean isPackComponents() {
		return packComponents;
	}

	public void setPackComponents(boolean packComponents) {
		this.packComponents = packComponents;
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

	@Override
	public MetocActionConfiguration clone() throws CloneNotSupportedException {
		final MetocActionConfiguration configuration = 
			new MetocActionConfiguration(super.getId(),super.getName(),super.getDescription(),super.isDirty());
		configuration.setCrs(crs);
		configuration.setEnvelope(envelope);
		configuration.setServiceID(getServiceID());
		configuration.setStoreFilePrefix(storeFilePrefix);
		configuration.setWorkingDirectory(workingDirectory);
		configuration.setMetocDictionaryPath(metocDictionaryPath);
		configuration.setMetocHarvesterXMLTemplatePath(metocHarvesterXMLTemplatePath);
		configuration.setPackComponents(packComponents);
		
		return configuration;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +"["
				+ "id:" + getId()
				+ " name:" + getName()
				+ " srvId:" + getServiceID()
				+ " wkdir:" + getWorkingDirectory()
				+"]";
	}

}
