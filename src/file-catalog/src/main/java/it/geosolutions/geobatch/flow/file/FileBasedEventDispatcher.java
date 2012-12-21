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

package it.geosolutions.geobatch.flow.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;

import java.util.EventObject;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch events and feed them to Consumers.
 * 
 * <P>
 * For every incoming event, existing consumers are checked if they are waiting for it. <BR>
 * If the new event is not consumed by any existing consumer, a new consumer will be created.
 * 
 * @author AlFa
 * @author Emanuele Tajariol, GeoSolutions
 */
/* package private */class FileBasedEventDispatcher extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedEventDispatcher.class);

    private final BlockingQueue<FileSystemEvent> eventMailBox;

    private final FileBasedFlowManager flowManager;

    // ----------------------------------------------- PUBLIC METHODS
    /**
     * Default Constructor
     */
    public FileBasedEventDispatcher(FileBasedFlowManager fm,
            BlockingQueue<FileSystemEvent> eventMailBox) {
        super(new StringBuilder("FileBasedEventDispatcher: EventDispatcherThread-").append(
                fm.getId()).toString());

        this.eventMailBox = eventMailBox;
        this.flowManager = fm;

        setDaemon(true); // shut me down when parent shutdown
        // reset interrupted flag
        interrupted();
    }

    /**
     * Shutdown the dispatcher.
     */
    public void shutdown() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Shutting down the dispatcher ... NOW!");
        }
        interrupt();
    }

    /**
     * Listener to notify the end of the consumer process
     * 
     * @author cancellieri
     *
     */
    class DispatcherListener implements IProgressListener {
        
        EventConsumer<EventObject, EventConsumerConfiguration> consumer;
        FileBasedEventDispatcher dispatcher;
        public DispatcherListener(EventConsumer<EventObject, EventConsumerConfiguration> consumer, FileBasedEventDispatcher dispatcher) {
            super();
            this.consumer=consumer;
            this.dispatcher=dispatcher;
        }

        @Override
        public void failed(Throwable exception) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Consumer has failed the job notifying the dispatcher");
            }
            synchronized (dispatcher) {
                dispatcher.notify();
            }
        }
        
        @Override
        public void completed() {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Consumer has completed the job notifying the dispatcher");
            }
            synchronized (dispatcher) {
                dispatcher.notify();
            }
        }
        @Override
        public void terminated() {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Consumer has completed the job notifying the dispatcher");
            }
            synchronized (dispatcher) {
                dispatcher.notify();
            }
        }
        @Override
        public void started() {}
        @Override
        public void setTask(String currentTask) {}
        @Override
        public void setProgress(float progress) {}
        @Override
        public void resumed() {}
        @Override
        public void progressing() {}
        @Override
        public void paused() {}
        @Override
        public String getTask() {return null;}
        @Override
        public float getProgress() {return 0;}
        @Override
        public Identifiable getOwner() {return consumer;}
    }
    
    /**
     * the dispatcher thread
     */
    public void run() {
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Ready to dispatch Events to flow " + flowManager.getId() + "("
                        + flowManager.getName() + ")");
            }

            while (!isInterrupted()) {
              
                // waiting for a new event
                final FileSystemEvent event;
                try {
                    event = eventMailBox.take(); // blocking call
                } catch (InterruptedException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                    this.interrupt();
                    return;
                }
                
                // //
                // if no EventConsumer is found, we need to create a new one
                // //
                final FileBasedFlowConfiguration flowCfg = flowManager.getConfiguration();
                final FileBasedEventConsumerConfiguration consumerCfg = ((FileBasedEventConsumerConfiguration) flowCfg
                        .getEventConsumerConfiguration()).clone();
                final FileBasedEventConsumer consumer = new FileBasedEventConsumer(consumerCfg,
                        flowManager.getFlowConfigDir(), flowManager.getFlowTempDir());
                consumer.setFlowName(flowManager.getName());
                
                // add the listener for notify the end of the process
                consumer.addListener(new DispatcherListener((EventConsumer)consumer, this));
                
                // //
                // We just created a brand new BaseEventConsumer which
                // can handle this event.
                // //
                while (!flowManager.addConsumer(consumer)) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Waiting for free space in consumer map to start consumer " + consumer + ")");
                    }
                    // wait for notify from the flow manager
                    synchronized (this) {
                        wait();
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Dispatcher was notified by the FlowManager.");
                    }
                }

                

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Processing incoming event " + event);
                }
                
                if (consumer.consume(event)) {
                    if (consumer.getStatus() != EventConsumerStatus.EXECUTING) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(consumer + " created on event " + event);
                        }
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(event + " was the only needed event for "
                                    + consumer);
                        }
                        flowManager.execute(consumer);
                    }
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("---------------------------------------------------------------");
                        LOGGER.error("The consumer could serve " + event + " (neither "
                                + consumer + " could)");
                        LOGGER.error("---------------------------------------------------------------");
                    }
                }
            }
        } catch (InterruptedException e) { // may be thrown by the "stop" button
            // on web interface
            LOGGER.error(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

    }
}
