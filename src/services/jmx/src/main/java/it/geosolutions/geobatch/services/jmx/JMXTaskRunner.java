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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.remote.JMXConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - GeoSolutions
 *
 * @param <T> implementations of {@link ConsumerManager}
 */
public abstract class JMXTaskRunner<T extends ConsumerManager> {

    protected final static Logger LOGGER = LoggerFactory.getLogger(JMXTaskRunner.class);

    /**
     * return the number of runned tasks
     * 
     * @return
     * @throws Exception
     */
    protected abstract int runTasks(final CompletionService<T> cs) throws Exception;
    
    /**
     * the method which actually runs the runTasks method and fill in the list of success and failures
     * @see {@link JMXTaskRunner#run(JMXTaskRunner, Map, List, List)} 
     * @param retSuccess
     * @param retFail
     * @throws Exception
     */
    public abstract void run(final List<T> retSuccess,
            final List<T> retFail) throws Exception;

    // the jmx connector
    protected JMXConnector jmxConnector;

    // the ActionManager's proxy
    protected ServiceManager serviceManager;

    /**
     * 
     * @param connectionParams connection parameters
     * @throws Exception
     */
    public JMXTaskRunner() {
        super();
    }

    protected void connect(final Map<String, String> connectionParams) throws Exception {
        if (connectionParams == null) {
            throw new IllegalArgumentException("Unable to run using a null environment map");
        }
        try {
            // get the connector using the configured environment
            this.jmxConnector = JMXClientUtils.getConnector(connectionParams);
            // create the proxy
            this.serviceManager = JMXClientUtils.getProxy(connectionParams, jmxConnector);

        } catch (Exception e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            dispose();
            throw e;
        }
    }

    protected void dispose() throws IOException {
        // TODO dispose all the pending consumers?!?
        if (jmxConnector != null) {
            try {
                // close connector's connection
                jmxConnector.close();
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(e.getMessage(), e);
            }
        }
    }

    protected void run(JMXTaskRunner<T> runner, final Map<String, String> commonEnv,
            final List<T> retSuccess, final List<T> retFail) throws Exception {

        try {
            final String connectionPropFileName = commonEnv
                    .get(JMXClientUtils.CONNECTION_PARAMETERS_KEY);

            if (connectionPropFileName != null && !connectionPropFileName.isEmpty()) {
                final Map<String, String> connectionParams = JMXClientUtils
                        .loadEnv(connectionPropFileName);
                runner.connect(connectionParams);
            } else {
                // try using the main prop file
                runner.connect(commonEnv);
            }

            final ExecutorService es = Executors.newFixedThreadPool(10); // TODO
                                                                         // get
            // thread
            // pool
            // size
            final CompletionService<T> cs = new ExecutorCompletionService<T>(es);

            for (int size = runner.runTasks(cs); size > 0; --size) {
                // for each task
                // get the event input flow
                String event = null;
                T consumerManager = null;
                try {
                    // get the first ending task
                    final Future<T> task = cs.take();
                    // get the consumer manager
                    consumerManager = task.get();
                    // get the event input flow
                    event = consumerManager.getConfiguration(0).get(ConsumerManager.INPUT_KEY);
                    // check for the status
                    final ConsumerStatus status = consumerManager.getStatus();
                    // consumer uuid
                    final String uuid = consumerManager.getUuid();
                    if (status == ConsumerStatus.COMPLETED) {
                        // success
                        if (LOGGER.isInfoEnabled())
                            LOGGER.info("Action UUID: " + uuid + " EXIT status is: " + status);
                        if (event != null)
                            retSuccess.add(consumerManager);

                        if (LOGGER.isInfoEnabled()) {
                            Iterator<JMXProgressListener> it = consumerManager.getListeners()
                                    .iterator();
                            while (it.hasNext())
                                LOGGER.info("Listener messages: "
                                        + JMXCumulatorListener
                                                .printMessages(JMXCumulatorListener.class.cast(it
                                                        .next())));
                        }
                    } else if (status == ConsumerStatus.FAILED || status == ConsumerStatus.CANCELED) {
                        // failure
                        if (event != null)
                            retFail.add(consumerManager);
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.info("Action UUID: " + uuid + " EXIT status is: " + status);
                            Iterator<JMXProgressListener> it = consumerManager.getListeners()
                                    .iterator();
                            while (it.hasNext())
                                LOGGER.error("Listener messages: "
                                        + JMXCumulatorListener
                                                .printMessages(JMXCumulatorListener.class.cast(it
                                                        .next())));
                        }
                    } else if (status == ConsumerStatus.UNRECOGNIZED) {
                        // failure
                        if (event != null)
                            retFail.add(consumerManager);
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Action UUID: " + uuid + " EXIT status is: " + status);
                            Iterator<JMXProgressListener> it = consumerManager.getListeners()
                                    .iterator();
                            while (it.hasNext())
                                LOGGER.error("Listener messages: "
                                        + JMXCumulatorListener
                                                .printMessages(JMXCumulatorListener.class.cast(it
                                                        .next())));
                        }
                    } else { // status == null
                        // failure
                        if (event != null)
                            retFail.add(consumerManager);
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Action UUID: " + uuid
                                    + " Unable to submit new actions to the remote GB");
                            Iterator<JMXCumulatorListener> it = consumerManager.getListeners(
                                    JMXCumulatorListener.class).iterator();
                            while (it.hasNext())
                                LOGGER.error("Listener messages: "
                                        + JMXCumulatorListener
                                                .printMessages(JMXCumulatorListener.class.cast(it
                                                        .next())));
                        }
                    }
                } catch (NullPointerException e) {
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(e.getMessage(), e);
                    if (event != null)
                        retFail.add(consumerManager);
                } catch (Exception e) {
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(e.getMessage(), e);
                    if (event != null)
                        retFail.add(consumerManager);
                }
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            else
                LOGGER.error(e.getLocalizedMessage());
            throw e;
        } finally {
            if (runner != null) {
                runner.dispose();
            }
        }

    }

}
