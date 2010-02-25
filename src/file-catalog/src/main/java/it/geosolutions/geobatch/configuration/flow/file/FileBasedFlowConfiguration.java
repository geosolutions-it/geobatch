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



package it.geosolutions.geobatch.configuration.flow.file;

import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.flow.BaseFlowConfiguration;
import it.geosolutions.geobatch.configuration.flow.FlowConfiguration;

/**
 * <p>
 * A Configuration for the Flow based on xml marshalled files.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 * @author Ivano Picco
 */
public class FileBasedFlowConfiguration extends BaseFlowConfiguration implements FlowConfiguration {

    /**
     * workingDirectory: this attribute represents the configuring directory for this flow. It can
     * be relative to the catalog.xml directory or absolute.
     * 
     * Attention: the configuring directory should be different from the one containing the
     * configuration files.
     */
    private String workingDirectory;

    /**
     * autorun: this attribute is used to autorun a flow on startup.
     *
     */
    private boolean autorun = false;

    /**
     * Default Constructor.
     */
    public FileBasedFlowConfiguration() {
        super();
    }

    /**
     * 
     * @param id
     * @param name
     * @param eventGeneratorConfiguration
     * @param description
     * @param eventConsumerConfiguration
     */
    public FileBasedFlowConfiguration(String id, String name,
            FileBasedEventGeneratorConfiguration eventGeneratorConfiguration, String description,
            FileBasedEventConsumerConfiguration eventConsumerConfiguration) {
        super(id, name, eventGeneratorConfiguration, description, eventConsumerConfiguration);
    }

    /**
     * 
     * @param id
     * @param name
     * @param eventGeneratorConfiguration
     * @param description
     * @param eventConsumerConfiguration
     * @param workingDirectory
     */
    public FileBasedFlowConfiguration(String id, String name,
            EventGeneratorConfiguration eventGeneratorConfiguration, String description,
            EventConsumerConfiguration eventConsumerConfiguration, String workingDirectory) {
        super(id, name, eventGeneratorConfiguration, description, eventConsumerConfiguration);
        this.workingDirectory = workingDirectory;
    }

    /**
     * Getter for the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Setter for the workingDirectory.
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        setDirty(true);
    }

    /**
     * Returns true if flow must be started on startup
     * 
     */

    public boolean autorun() {
        return autorun;
    }

    /**
     * Set autorun flag.
     *
     * @param autorun
     */
    public void setautorun(boolean autorun) {
        this.autorun = autorun;
    }


	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" +
				"id:" + getId()
				+ ", name:" + getName()
				+ ", sid:" + getServiceID()
				+ ", wdir:" + getWorkingDirectory()
				+ ", egcfg:" + getEventGeneratorConfiguration()
				+ "]";
	}


}
