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
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;

/**
 * A Conf for the Flow based on xml marshalled files.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 * @author Ivano Picco
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class FileBasedFlowConfiguration extends BaseFlowConfiguration {

    /**
     * workingDirectory: this attribute represents the configuring directory for
     * this flow. It can be relative to the catalog.xml directory or absolute.
     * Attention: the configuring directory should be different from the one
     * containing the configuration files.
     */
    private String workingDirectory;

    /**
     * maximum numbers of stored executed (see Consumer.getStatus()) consumers
     */
    private int maxStoredConsumers;

    /**
     * if true once maxStoredConsumer is reached consumers removal is only
     * manually permitted (using {@link FileBasedFlowManager#disposeConsumer(String)} or via GUI)<br>
     * Default is false.
     */
    private boolean keepConsumers = false;

    /**
     * autorun: this attribute is used to autorun a flow on startup.
     */
    private boolean autorun = false;

    /**
     * @return the keepConsumers
     */
    public final boolean isKeepConsumers() {
        return keepConsumers;
    }

    /**
     * @param keepConsumers the keepConsumers to set
     */
    public final void setKeepConsumers(boolean keepConsumers) {
        this.keepConsumers = keepConsumers;
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
                                      FileBasedEventGeneratorConfiguration eventGeneratorConfiguration,
                                      String description,
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
                                      EventGeneratorConfiguration eventGeneratorConfiguration,
                                      String description,
                                      EventConsumerConfiguration eventConsumerConfiguration,
                                      String workingDirectory) {
        super(id, name, eventGeneratorConfiguration, description, eventConsumerConfiguration);
        this.workingDirectory = workingDirectory;
    }

    /**
     * @return the maxStoredConsumers
     */
    public int getMaxStoredConsumers() {
        return maxStoredConsumers;
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + ", name:" + getName() + ", sid:"
               + getServiceID() + ", wdir:" + getWorkingDirectory() + ", egcfg:"
               + getEventGeneratorConfiguration() + "]";
    }

    /**
     * @return
     */
    public boolean isAutorun() {
        return autorun;
    }

    /**
     * @param autorun
     */
    public void setAutorun(boolean autorun) {
        this.autorun = autorun;
    }
}
