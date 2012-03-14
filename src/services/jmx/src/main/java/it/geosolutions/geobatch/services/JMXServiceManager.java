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
package it.geosolutions.geobatch.services;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class JMXServiceManager implements ApplicationContextAware {
    private final static Logger LOGGER = LoggerFactory.getLogger(JMXServiceManager.class);

    public final String FlowManagerID = "JMX_FLOW_MANAGER";

    private static Catalog catalog;

    private static FileBasedFlowManager flowManager;
    final FileBasedFlowConfiguration flowManagerConfig;
    
    private static File configDirFile;

    private static List<FileBasedEventConsumer> consumers;

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public JMXServiceManager() throws NullPointerException, IOException {
        catalog = CatalogHolder.getCatalog();

        flowManager = catalog.getResource(FlowManagerID,
                                     it.geosolutions.geobatch.flow.file.FileBasedFlowManager.class);
        if (flowManager == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("The flow id \'" + FlowManagerID
                            + "\' does not exists into catalog... -> going to create it");
            }
            flowManagerConfig = new FileBasedFlowConfiguration(FlowManagerID, FlowManagerID,null,  "Auto generated " + FlowManagerID, null);
            configDirFile = new File(((FileBaseCatalog)catalog).getBaseDirectory(), FlowManagerID);
            if (!configDirFile.exists()) {
                if (!(configDirFile.getParentFile().canWrite() && configDirFile.mkdir())) {
                    throw new IllegalArgumentException("Unable to automatically create the " + FlowManagerID
                                                       + " working dir into:"
                                                       + configDirFile.getAbsolutePath().toString());
                }
            }
            flowManagerConfig.setWorkingDirectory(configDirFile.getAbsolutePath());
            
            flowManager = new FileBasedFlowManager(flowManagerConfig);
            
            consumers = flowManager.getEventConsumers();
            
            catalog.add(flowManager);
//            catalog.save(parent);
//            parent.persist();
            
        } else {
            configDirFile = flowManager.getWorkingDirectory();
            consumers = flowManager.getEventConsumers();
            flowManagerConfig = flowManager.getConfiguration();
        }
        
        if (!configDirFile.exists()) {
            if (!(configDirFile.getParentFile().canWrite() && configDirFile.mkdir())) {
                throw new IllegalArgumentException("Unable to automatically create the " + FlowManagerID
                                                   + " working dir into:"
                                                   + configDirFile.getAbsolutePath().toString());
            }
        }
        
        // listener config
//        if ()
        //flowManagerConfig.getProgressListenerConfigurations();
        
    }
    
    static ActionService getActionService(String serviceId){
        final ActionService service = (ActionService)context.getBean(serviceId);
        return service;
    }
    
    static int getStatus(String uuid) {
        synchronized (consumers) {
            for (FileBasedEventConsumer consumer : consumers) {
                if (consumer.getId().equals(uuid)) {
                    final EventConsumerStatus status = consumer.getStatus();
                    switch (status) {
                    case IDLE:
                        return 4;
                    case WAITING:
                        return 3;
                    case PAUSED:
                        return 2;
                    case EXECUTING:
                        return 1;
                    case COMPLETED:
                        return 0;
                    case CANCELED:
                        return -1;
                    case FAILED:
                        return -2;
                    }
                }
            }
        }

        return -3; // consumer UUID not found
    }

    static String callAction(String serviceId, Map<String, String> config, Queue<FileSystemEvent> events) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, InterruptedException, IOException{
        
        final ActionService service = JMXServiceManager.getActionService(serviceId);
        final Class serviceClass = service.getClass();
        
        ActionConfiguration actionConfig = null;
        for (Method method : serviceClass.getMethods()) {
            if (method.getName().equals("canCreateAction")) {
                final Class[] classes = method.getParameterTypes();
                final Constructor constructor = classes[0].getConstructor(new Class[]{String.class, String.class, String.class});
                actionConfig = (ActionConfiguration)constructor.newInstance(UUID.randomUUID().toString(),
                                                                            "NAME", "DESC");
                actionConfig.setServiceID(serviceId);
                final Set<String> keys = config.keySet();
                final Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    org.apache.commons.beanutils.BeanUtils.copyProperty(actionConfig, key, config.get(key));
                }
                actionConfig.setConfigDir(configDirFile);
                actionConfig.setWorkingDirectory(configDirFile.getAbsolutePath());
                
                if (actionConfig != null)
                    break;
                // BeanUtils.instantiate(clazz)
            }
        }
        if (actionConfig == null)
            throw new IllegalArgumentException("Unable to locate the configuration");

        final FileBasedEventConsumerConfiguration consumerConfig = new FileBasedEventConsumerConfiguration("JMX_Consumer_id",
                                                                                                           "JMX_Consumer_name",
                                                                                                           "JMX_Consumer description");
        // TODO Status progress listener
//        final StatusProgressListenerConfiguration statusProgressListenerConfig=new StatusProgressListenerConfiguration("status_listener", "status_listener", "status_listener");
//        statusProgressListenerConfig.setServiceID("StatusProgressListener");
//        actionConfig.addListenerConfiguration(statusProgressListenerConfig);
        
        final List<ActionConfiguration> actions = new ArrayList<ActionConfiguration>();
        actions.add(actionConfig);
        
        consumerConfig.setActions(actions);
        consumerConfig.setWorkingDirectory(configDirFile.getAbsolutePath());
        
        // TODO logging progress listener
//        final LoggingProgressListenerConfiguration loggingProgressListenerConfig=new LoggingProgressListenerConfiguration("logging_listener", "logging_listener", "logging_listener");
//        loggingProgressListenerConfig.setServiceID("LoggingProgressListener");        
//        loggingProgressListenerConfig.setLoggerName("it.geosolutions.geobatch.services");
//        consumerConfig.addListenerConfiguration(loggingProgressListenerConfig);
        
        final FileBasedEventConsumer consumer = new FileBasedEventConsumer(consumerConfig);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("INIT injecting consumer to the parent flow. UUID: "+consumer.getId());
        }
        synchronized (consumers) {
            consumers.add(consumer);
        }
        for (FileSystemEvent event :events){
            consumer.consume(event);   
        }
        
        // execute
        flowManager.getExecutor().submit(consumer);
        
        return consumer.getId();
    }
    
}
