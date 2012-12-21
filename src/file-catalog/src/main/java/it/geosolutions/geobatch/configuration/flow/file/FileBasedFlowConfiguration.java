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

package it.geosolutions.geobatch.configuration.flow.file;

import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.flow.BaseFlowConfiguration;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;

import java.io.File;

/**
 * A Conf for the Flow based on xml marshalled files.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 * @author Ivano Picco
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * @author Emanuele Tajariol, GeoSolutions
 * 
 */
public class FileBasedFlowConfiguration extends BaseFlowConfiguration {

    /**
     * configDir: this attribute represents the configuring directory for
     * this flow. It can be relative to the {@link #DataDirHandler} GEOBATCH_CONFIG_DIR directory or absolute.
     * Attention: the configuring directory should be different from the one
     * containing temporary files.
     */
    private File overrideConfigDir;

    /**
     * May be different than null if the temp dir should be mapped in a directory outside the configured GEOBATCH_TEMP_DIR.
     * It represents the "Flow Base Temp Dir", i.e. the base direcotry where the
     * temp dir for the flow instances will be created.
     * <p/>
     * <B>It must be an absolute path.</B>
     */
    private File overrideTempDir;

    /**
     * maximum numbers of stored executed (see Consumer.getStatus()) consumers
     */
    private Integer maxStoredConsumers;

    /**
     * if true once maxStoredConsumer is reached consumers removal is only
     * manually permitted (using {@link FileBasedFlowManager#disposeConsumer(String)} or via GUI)<br>
     * Default is false.
     */
    private Boolean keepConsumers;

    /**
     * autorun: this attribute is used to autorun a flow on startup.
     */
    private boolean autorun = false;


    /**
     * @return the configDir
     */
    public File getOverrideConfigDir() {
        return overrideConfigDir;
    }

    /**
     * @param configDir the configDir to set
     */
    public void setOverrideConfigDir(File configDir) {
        this.overrideConfigDir = configDir;
        setDirty(true);
    }

    /**
     * @param maxStoredConsumers the maxStoredConsumers to set
     */
    public void setMaxStoredConsumers(int maxStoredConsumers) {
        this.maxStoredConsumers = maxStoredConsumers;
        setDirty(true);
    }

    /**
     * 
     * @param id
     * @param name
     * @param eventGeneratorConfiguration
     * @param description
     * @param eventConsumerConfiguration
     */
    public FileBasedFlowConfiguration(String id, String name, String description,
                                      FileBasedEventGeneratorConfiguration eventGeneratorConfiguration,
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
    public FileBasedFlowConfiguration(String id, String name, String description,
                                      EventGeneratorConfiguration eventGeneratorConfiguration,                                      
                                      EventConsumerConfiguration eventConsumerConfiguration,
                                      File ovrTempDir) {
        super(id, name, eventGeneratorConfiguration, description, eventConsumerConfiguration);
        this.overrideTempDir = ovrTempDir;
    }

    /**
     * @return the maxStoredConsumers
     */
    public Integer getMaxStoredConsumers() {
        return maxStoredConsumers;
    }

    public File getOverrideTempDir() {
        return overrideTempDir;
    }

    public void setOverrideTempDir(File overrideTempDir) {
        this.overrideTempDir = overrideTempDir;
        setDirty(true);
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
        setDirty(true);
    }

    /**
     * @return the keepConsumers
     */
    public final Boolean isKeepConsumers() {
        return keepConsumers;
    }

    /**
     * @param keepConsumers the keepConsumers to set
     */
    public final void setKeepConsumers(boolean keepConsumers) {
        this.keepConsumers = keepConsumers;
        setDirty(true);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "["
                + "id:" + getId()
                + ", name:" + getName()
                + ", sid:" + getServiceID()
                + ", configDir:" + getOverrideConfigDir()
                + ", ovrTDir:" + getOverrideTempDir()
                + ", egcfg:" + getEventGeneratorConfiguration()
                + " auto:" + autorun
                + " maxCons:" + maxStoredConsumers
                + " keepCons:" + keepConsumers
                + "]";
    }

}
