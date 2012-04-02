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
package it.geosolutions.geobatch.services.jmx;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.tools.commons.file.Path;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
 * JMX service which supports:<ul>
 * <li>
 * - creating JMX flow on the fly
 * </li>
 * <li>
 * - creating and starting consumers with externally configured action
 * </li>
 * <li>
 * - get status of JMX consumer instances 
 * </li>
 * <li>
 * - dispose JMX consumer instances 
 * </li>
 * <li>
 * - get status of JMX consumer instance 
 * </li>
 * </ul>
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
//currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200, persistLocation = "foo",
@ManagedResource(objectName = "bean:name=JMXServiceManager", description = "JMX Service Manager to start/monitor/dispose GeoBatch action", log = true, logFile = "jmx.log", persistName = "JMXServiceManager")
public class JMXServiceManager implements ActionManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(JMXServiceManager.class);

    public final static String FlowManagerID = "JMX_FLOW_MANAGER";

    private static Catalog catalog;

    private static FileBasedFlowManager flowManager;

    final FileBasedFlowConfiguration flowManagerConfig;

    private static File configDirFile;

    @Resource(type = org.springframework.context.ApplicationContext.class)
    private ApplicationContext context;

    public JMXServiceManager() throws Exception {
        catalog = CatalogHolder.getCatalog();

        flowManager = catalog.getResource(FlowManagerID,
                                          it.geosolutions.geobatch.flow.file.FileBasedFlowManager.class);
        if (flowManager == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("The flow id \'" + FlowManagerID
                            + "\' does not exists into catalog... -> going to create it");
            }
            
            flowManagerConfig = new FileBasedFlowConfiguration(FlowManagerID, FlowManagerID, null,
                                                               "Auto generated " + FlowManagerID, null);
            configDirFile = new File(((FileBaseCatalog)catalog).getBaseDirectory(), FlowManagerID);
            if (!configDirFile.exists()) {
                if (!(configDirFile.getParentFile().canWrite() && configDirFile.mkdir())) {
                    throw new IllegalArgumentException("Unable to automatically create the " + FlowManagerID
                                                       + " working dir into:"
                                                       + configDirFile.getAbsolutePath().toString());
                }
            }
            flowManagerConfig.setWorkingDirectory(configDirFile.getAbsolutePath());
            
            // keep consumer until disposeAction is called
            flowManagerConfig.setKeepConsumers(true);
            //flowManagerConfig.setMaximumPoolSize(flowManagerConfig.getMaximumPoolSize()); // TODO create a spec param

            flowManager = new FileBasedFlowManager(flowManagerConfig);

            catalog.add(flowManager);
            // TODO persistence (throws NullPointerException)
            // catalog.save(parent);
            // parent.persist();

            if (!configDirFile.exists()) {
                if (!(configDirFile.getParentFile().canWrite() && configDirFile.mkdir())) {
                    throw new IllegalArgumentException("Unable to automatically create the " + FlowManagerID
                                                       + " working dir into:"
                                                       + configDirFile.getAbsolutePath().toString());
                }
            }
        } else {
            flowManagerConfig = flowManager.getConfiguration();
            
            if (flowManagerConfig.getWorkingDirectory()==null)
                throw new IllegalArgumentException("Please set the flow working dir");
            
            configDirFile = new File(flowManagerConfig.getWorkingDirectory());
            
        }


        // TODO listener config
        // if ()
        // flowManagerConfig.getProgressListenerConfigurations();

    }

    /**
     * returns the status of the selected consumer
     * 
     * @param uuid
     * @return {@link ConsumerStatus}
     */
    @Override
    @org.springframework.jmx.export.annotation.ManagedOperation(description = "disposeAction - used to dispose the consumer instance from the consumer registry")
    @ManagedOperationParameters({@ManagedOperationParameter(name = "uuid", description = "The uuid of the consumer")})
    public void disposeAction(String uuid) throws Exception {
        flowManager.disposeConsumer(uuid);
    }

    /**
     * returns the status of the selected consumer
     * 
     * @param uuid
     * @return {@link ConsumerStatus}
     */
    @Override
    @org.springframework.jmx.export.annotation.ManagedOperation(description = "get the status of the selected consumer")
    @ManagedOperationParameters({@ManagedOperationParameter(name = "uuid", description = "The uuid of the consumer")})
    public ConsumerStatus getStatus(String uuid) {
        return ConsumerStatus.getStatus(flowManager.getStatus(uuid));
    }

    @Override
    @org.springframework.jmx.export.annotation.ManagedOperation(description = "callAction - used to run a consumer")
    @ManagedOperationParameters({@ManagedOperationParameter(name = "config", description = "A map containing the list of needed paramethers, inputs and outputs used by the action")})
    public String callAction(java.util.Map<String, String> config) throws Exception {
        final String serviceId = config.remove(SERVICE_ID_KEY);
        if (serviceId == null || serviceId.isEmpty())
            throw new IllegalArgumentException(
                                               "Unable to locate the key "
                                                   + SERVICE_ID_KEY
                                                   + " matching the serviceId action in the passed paramether table");

        final String input = config.remove(INPUT_KEY);
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Unable to locate the key " + INPUT_KEY
                                               + " matching input in the passed paramether table.");
        }
        FileSystemEvent event = new FileSystemEvent(new File(input), FileSystemEventType.FILE_ADDED);
        Queue<FileSystemEvent> events = new java.util.LinkedList<FileSystemEvent>();
        events.add(event);

        // TODO remove all 'NOT configuration' param

        return callAction(serviceId, config, events);
    };

    private String callAction(String serviceId, Map<String, String> config, Queue<FileSystemEvent> events)
        throws Exception {

        final ActionService service = (ActionService)context.getBean(serviceId);
        final Class serviceClass = service.getClass();

        ActionConfiguration actionConfig = null;
        for (Method method : serviceClass.getMethods()) {
            if (method.getName().equals("canCreateAction")) {
                final Class[] classes = method.getParameterTypes();
                
                Constructor constructor;
                try {
                    constructor= classes[0].getConstructor(new Class[]{});
                    actionConfig = (ActionConfiguration)constructor.newInstance();
                }catch (NoSuchMethodException e){
                    constructor= classes[0].getConstructor(new Class[]{String.class,String.class,String.class});
                    actionConfig = (ActionConfiguration)constructor.newInstance(serviceId,serviceId,serviceId);
                }
                actionConfig.setServiceID(serviceId);
                final Set<String> keys = config.keySet();
                final Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    try {
                        smartCopy(actionConfig, key, config.get(key));
                    } catch (Exception e) {
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(e.getLocalizedMessage(), e);
                        // TODO something else?
                    }
                }
                
                actionConfig.setConfigDir(configDirFile);

                if (actionConfig != null)
                    break;
                // BeanUtils.instantiate(clazz)
            }
        }
        if (actionConfig == null)
            throw new IllegalArgumentException("Unable to locate the configuration");

        final FileBasedEventConsumerConfiguration consumerConfig = new FileBasedEventConsumerConfiguration(
                                                                                                           "JMX_Consumer_id",
                                                                                                           "JMX_Consumer_name",
                                                                                                           "JMX_Consumer description");
        // TODO Status progress listener
        // final StatusProgressListenerConfiguration
        // statusProgressListenerConfig=new
        // StatusProgressListenerConfiguration("status_listener",
        // "status_listener", "status_listener");
        // statusProgressListenerConfig.setServiceID("StatusProgressListener");
        // actionConfig.addListenerConfiguration(statusProgressListenerConfig);

        final List<ActionConfiguration> actions = new ArrayList<ActionConfiguration>();
        actions.add(actionConfig);

        consumerConfig.setActions(actions);
        consumerConfig.setWorkingDirectory(flowManagerConfig.getWorkingDirectory());
        // TODO may we want to remove only when getStatus is remotely called???
        // consumerConfig.setKeepContextDir(true);

        // if you want to move the input you may call the action move!
        consumerConfig.setPreserveInput(true);

        // TODO logging progress listener
        // final LoggingProgressListenerConfiguration
        // loggingProgressListenerConfig=new
        // LoggingProgressListenerConfiguration("logging_listener",
        // "logging_listener", "logging_listener");
        // loggingProgressListenerConfig.setServiceID("LoggingProgressListener");
        // loggingProgressListenerConfig.setLoggerName("it.geosolutions.geobatch.services");
        // consumerConfig.addListenerConfiguration(loggingProgressListenerConfig);
        

        final FileBasedEventConsumer consumer = new FileBasedEventConsumer(consumerConfig);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("INIT injecting consumer to the parent flow. UUID: " + consumer.getId());
        }
        
        for (FileSystemEvent event : events) {
            consumer.consume(event);
        }

        // following ops are atomic
        if (!flowManager.addConsumer(consumer)){
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Unable to add another consumer, consumer queue is full. " +
                		"Please dispose some completed consumer before submit a new one.");
            }
            return null;
        }
        // execute
        flowManager.getExecutor().submit(consumer);

        return consumer.getId();
    }

    private static <T> void smartCopy(final T bean, final String propertyName, final String value)
        throws Exception {
    	// try quick way
    	try {
    		BeanUtils.copyProperty(bean, propertyName, value);
    		return;
    	} catch (Exception e){
        	if (LOGGER.isWarnEnabled())
        		LOGGER.warn("Error using ");
    	}
    	// special cases
        PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, propertyName);
        // return null if there is no such descriptor
        if (pd == null) {
            return;
        }
        final Class<?> type=pd.getPropertyType();
        if (type.isAssignableFrom(value.getClass())){
	        // try using setter
	        if (pd.getWriteMethod()!=null){
	        	PropertyUtils.setProperty(bean, propertyName, value);
	        } else {

	        	// T interface doesn't declare setter method for this property
	            // lets use getter methods to get the property reference
	            Object property = PropertyUtils.getProperty(bean, propertyName);
	            if (property!=null){
	            	if (!adapt(type,property,value)){
	            		// fail
	            		if (LOGGER.isErrorEnabled())
	            			LOGGER.error("Skipping unwritable property " + propertyName + " unable to find the adapter for type "
	                             + type);
	            	}
	            } else {
	            	if (LOGGER.isErrorEnabled())
            			LOGGER.error("Skipping unwritable property " + propertyName + " with property type "
                             + type);
	            }
		    }
        }
    }
    
    private static boolean adapt(Class<?> type, Object property, Object value){
        // check type of property to apply new value
    	if (Collection.class.isAssignableFrom(type)) {

            final Collection<Object> liveCollection;
            if (property != null) {
                liveCollection = (Collection<Object>)property;
                liveCollection.clear();
            } else {
                liveCollection = new LinkedList<Object>();
            }
            if (String.class.isAssignableFrom(value.getClass())){
	            // value should be a list of string ',' separated
	            String[] listString = ((String)value).split(",");
	            for (String s : listString) {
	                liveCollection.add(s);
	            }
            }

        } else if (Map.class.isAssignableFrom(type)) {

            final Map<Object, Object> liveMap;
            if (property != null) {
                liveMap = (Map<Object, Object>)property;
                liveMap.clear();
            } else {
                liveMap = new HashMap<Object, Object>();
            }
            if (String.class.isAssignableFrom(value.getClass())){
	            // value should be a list of key=value string ';' separated
	            String[] listString = ((String)value).split(";");
	            for (String kvString : listString) {
	                String kv[] = kvString.split("=");
	                liveMap.put(kv[0], kv[1]);
	            }
            } else {
            	return false;
            }
        } else {
        	return false;
        }
        return true;
    }

}
