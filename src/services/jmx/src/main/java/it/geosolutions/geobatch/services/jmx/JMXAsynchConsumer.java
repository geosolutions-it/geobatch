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

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.management.remote.JMXConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implements a Callable interface running a remote GeoBatch action and
 * returning a {@link JMXConsumerManager}
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class JMXAsynchConsumer implements Callable<ConsumerManager> {
    private final Logger LOGGER = LoggerFactory.getLogger(JMXAsynchConsumer.class);

    // the consumer
    private final List<Map<String, String>> consumerConfig;
    // the jmx connector
    private final JMXConnector jmxConnector;
    // the ActionManager's proxy
    private final ServiceManager serviceManager;
    // extract the delay for the polling interval
    private Long pollingDelay = null;

//  /**
//  * 
//  * @param env
//  * @deprecated create connector externally use
//  *             {@link #JMXAsynchConsumer(JMXConnector, Map)}
//  */
// public JMXAsynchConsumer(Map<String, String> env) {
//         if (env == null) {
//                 throw new IllegalArgumentException("Unable to create a "
//                                 + getClass() + "using a null environment map");
//         }
//         consumerConfig = env;
//         try {
//                 // get the connector using the configured environment
//                 jmxConnector = JMXClientUtils.getConnector(consumerConfig);
//                 // create the proxy
//                 serviceManager = JMXClientUtils.getProxy(consumerConfig, jmxConnector);
//
//                 // Process delay secs
//                 final String delay = env.get(JMXClientUtils.PROCESS_DELAY_KEY);
//                 if (delay != null) {
//                         try {
//                                 pollingDelay = Math.round(1000 * Double.parseDouble(delay));
//                         } catch (NumberFormatException nfe) {
//                                 pollingDelay = JMXClientUtils.PROCESS_DELAY_DEFAULT;
//                         }
//                 } else {
//                         pollingDelay = JMXClientUtils.PROCESS_DELAY_DEFAULT;
//                 }
//         } catch (Exception e) {
//                 if (LOGGER.isErrorEnabled())
//                         LOGGER.error(e.getLocalizedMessage(), e);
//                 if (jmxConnector != null) {
//                         try {
//                                 // close connector's connection
//                                 jmxConnector.close();
//                         } catch (IOException e1) {
//                                 if (LOGGER.isErrorEnabled())
//                                         LOGGER.error(e.getMessage(), e);
//                         }
//                 }
//         }
// }
    
    /**
     * 
     * @param connector
     * @param serviceManagerProxy (can be null)
     * @param connParams
     * @param consumerConfig
     * @throws Exception
     */
    public JMXAsynchConsumer(JMXConnector connector, ServiceManager serviceManagerProxy, Map<String, String> consumerConfig, final long pollingDelay)
        throws Exception {
        if (serviceManagerProxy == null || consumerConfig == null || connector == null) {
            throw new IllegalArgumentException("Unable to create a " + getClass() + "using a null argument");
        }
        // connection
        this.jmxConnector = connector;

        this.consumerConfig = Collections.singletonList(consumerConfig);
        // create the proxy
        this.serviceManager = serviceManagerProxy;
        
        this.pollingDelay=pollingDelay;
    }
    
    /**
     * 
     * @param connector
     * @param serviceManagerProxy (can be null)
     * @param connParams
     * @param consumerConfig
     * @throws Exception
     */
    public JMXAsynchConsumer(JMXConnector connector, ServiceManager serviceManagerProxy, List<Map<String, String>> consumerConfig, final long pollingDelay)
        throws Exception {
        if (serviceManagerProxy == null || consumerConfig == null || connector == null) {
            throw new IllegalArgumentException("Unable to create a " + getClass() + "using a null argument");
        }
        // connection
        this.jmxConnector = connector;

        this.consumerConfig = consumerConfig;
        // create the proxy
        this.serviceManager = serviceManagerProxy;
        
        this.pollingDelay=pollingDelay;
    }


    public static Serializable getEvent(Map<String, String> config) throws IllegalArgumentException {
        // ////////////////////////////
        final String input = config.get(ConsumerManager.INPUT_KEY);
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Unable to locate the key " + ConsumerManager.INPUT_KEY
                                               + " matching input in the passed paramether table.");
        }
        return new File(input);
    }

    @Override
    public ConsumerManager call() throws Exception {
        ConsumerManager consumer = null;

        String uuid = null;
        try {
            /**
             * @see {@link ActionManager#callAction(java.util.Map arg0)}
             */
            consumer = new JMXConsumerManager(consumerConfig, serviceManager);

            uuid = consumer.getUuid();

            // RUN
            consumer.run(getEvent(consumerConfig.get(0)));

            ConsumerStatus status = ConsumerStatus.EXECUTING;
            do {

                // pending
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Consumer: " + uuid + " is WAITING while status is: " + status);

                // sleep for some time
                Thread.sleep(pollingDelay);

                /**
                 * @see {@link ActionManager#getStatus(java.lang.String)}
                 */
                // check result
                status = consumer.getStatus();

            } while (status == ConsumerStatus.EXECUTING || status == ConsumerStatus.IDLE
                     || status == ConsumerStatus.PAUSED || status == ConsumerStatus.WAITING);
        } finally {

            if (serviceManager != null && consumer != null) {
                try {
                    // get the listeners
                    consumer.getListeners();
                    // dispose remote GeoBatch consumer instance
                    consumer.dispose();
                } catch (Exception e) {
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(e.getMessage(), e);
                }
            } else {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Proxy is: " + serviceManager + " Consumer UUID is:" + uuid);
            }
        }
        return consumer;
    }

}
