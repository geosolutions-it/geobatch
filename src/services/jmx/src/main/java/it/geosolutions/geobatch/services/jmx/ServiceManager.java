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
package it.geosolutions.geobatch.services.jmx;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * 
 * GeoBatch JMX service which supports:
 * <ul>
 * <li>
 * - creating JMX flow on the fly</li>
 * <li>
 * - creating and starting consumers with externally configured action</li>
 * <li>
 * - get status of JMX consumer instances</li>
 * <li>
 * - dispose JMX consumer instances</li>
 * <li>
 * - get status of JMX consumer instance</li>
 * </ul>
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
@ManagedResource(objectName = "bean:name=JMXServiceManager", description = "JMX Service Manager to start/monitor/dispose GeoBatch action", log = true, logFile = "jmx.log", persistName = "JMXServiceManager")
public interface ServiceManager {

    public void disposeConsumer(final String uuid) throws Exception;

    /**
     * returns the status of the selected consumer
     * 
     * @param uuid
     * @return {@link ConsumerStatus}
     */
    public ConsumerStatus getStatus(String uuid);

    /**
     * create a consumer with multiple actions
     * @param configs
     * @return
     * @throws Exception
     */
    public String createConsumer(List<Map<String, String>> configs) throws Exception;

    /**
     * create a consumer with single action
     * @param config
     * @return
     * @throws Exception
     */
    public String createConsumer(java.util.Map<String, String> config) throws Exception;

    /**
     * create the configured action on the remote GeoBatch server through the JMX connection
     * 
     * @param config A map containing the list of needed parameters, inputs and outputs used by the action
     * @throws Exception if:
     *         <ul>
     *         <li>the passed map is null</li>
     *         <li>the passed map doesn't contains needed keys</li>
     *         <li>the connection is lost</li>
     *         </ul>
     */
    public void runConsumer(String uuid, Serializable event) throws Exception;

    public <T extends JMXProgressListener> Collection<T> getListeners(String uuid,
            Class<T> type);

}
