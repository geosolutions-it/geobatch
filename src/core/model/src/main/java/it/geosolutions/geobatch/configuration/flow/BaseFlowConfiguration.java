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

package it.geosolutions.geobatch.configuration.flow;

import it.geosolutions.geobatch.catalog.impl.BaseConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alessio Fabiani, GeoSolutions
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public abstract class BaseFlowConfiguration extends BaseConfiguration implements FlowConfiguration {

    
    private EventGeneratorConfiguration eventGeneratorConfiguration;

    
    private EventConsumerConfiguration eventConsumerConfiguration;

    /**
     * These configurations can be recalled by name by any Action or Consumer that will need them.
     */
    private List<ProgressListenerConfiguration> progressListenerConfigurations = new ArrayList<ProgressListenerConfiguration>();

    
    private int corePoolSize;

    
    private int maximumPoolSize;

    
    private long keepAliveTime;

    
    private int workQueueSize;

    /**
     * @return
     */
    public int getWorkQueueSize() {
        return workQueueSize;
    }

    /**
     * @param workQueueSize
     */
    public void setWorkQueueSize(int workQueueSize) {
        this.workQueueSize = workQueueSize;
    }

    public BaseFlowConfiguration(String id, String name,
            EventGeneratorConfiguration eventGeneratorConfiguration, String description,
            EventConsumerConfiguration eventConsumerConfiguration) {
        super(id, name, description);
        this.eventGeneratorConfiguration = eventGeneratorConfiguration;
        this.eventConsumerConfiguration = eventConsumerConfiguration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManagerDescriptor#getRuleSet()
     */
    /**
     * @return
     */
    public EventGeneratorConfiguration getEventGeneratorConfiguration() {
        return eventGeneratorConfiguration;
    }

    /**
     * @param eventGeneratorConfiguration  the eventGeneratorConfiguration to set
     */
    public void setEventGeneratorConfiguration(
            EventGeneratorConfiguration eventGeneratorConfiguration) {
        this.eventGeneratorConfiguration = eventGeneratorConfiguration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getId();
    }

    /**
     * @return
     */
    public EventConsumerConfiguration getEventConsumerConfiguration() {
        return eventConsumerConfiguration;
    }

    /**
     * @param eventConsumerConfiguration
     */
    public void setEventConsumerConfiguration(EventConsumerConfiguration eventConsumerConfiguration) {
        this.eventConsumerConfiguration = eventConsumerConfiguration;

    }

    public List<ProgressListenerConfiguration> getProgressListenerConfigurations() {
        return progressListenerConfigurations;
    }

    public void setProgressListenerConfigurations(
            List<ProgressListenerConfiguration> progressListenerConfigurations) {
        this.progressListenerConfigurations = progressListenerConfigurations;
    }

    public ProgressListenerConfiguration getProgressListenerConfiguration(String id) {
        for (ProgressListenerConfiguration progressListenerConfiguration : progressListenerConfigurations) {
            if (id.equals(progressListenerConfiguration.getId()))
                return progressListenerConfiguration;
        }
        return null;
    }

    /**
     * @return
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * @param corePoolSize
     */
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * @return
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * @param maximumPoolSize
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * @return
     */
    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    /**
     * @param keepAliveTime
     */
    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
}
