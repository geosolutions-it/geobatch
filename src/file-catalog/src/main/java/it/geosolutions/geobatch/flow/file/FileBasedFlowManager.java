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



package it.geosolutions.geobatch.flow.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.catalog.impl.BasePersistentResource;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.FlowManager;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.event.generator.EventGenerator;
import it.geosolutions.geobatch.flow.event.generator.EventGeneratorService;
import it.geosolutions.geobatch.flow.event.generator.FlowEventListener;
import it.geosolutions.geobatch.flow.event.generator.file.FileBasedEventGenerator;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public class FileBasedFlowManager
		extends BasePersistentResource<FileBasedFlowConfiguration>
        implements	FlowManager<FileSystemMonitorEvent, FileBasedFlowConfiguration>,
					FlowEventListener<FileSystemMonitorEvent>,
					Runnable {

    /** Default Logger **/
    private final static Logger LOGGER = Logger.getLogger(FlowManager.class.toString());

    private boolean autorun=false;

    /**
     * Base class for dispatchers.
     * 
     * @author AlFa
     * @version $ EventDispatcher.java $ Revision: 0.1 $ 22/gen/07 19:36:25
     */
    private final class EventDispatcher extends Thread {

        // ----------------------------------------------- PUBLIC METHODS
        /**
         * Default Constructor
         */
        public EventDispatcher() {
            super(new StringBuilder("EventDispatcherThread-").append(
                    FileBasedFlowManager.this.getId()).toString());
            setDaemon(true);// shut me down when parent shutdown
            // reset interrupted flag
            interrupted();
        }

        /**
         * Shutdown the dispatcher.
         */
        public void shutdown() {
            if (LOGGER.isLoggable(Level.INFO))
				LOGGER.info("Shutting down the dispatcher ... NOW!");
			interrupt();

        }

        // ----------------------------------------------- UTILITY METHODS

        /**
    	 *
    	 */
        public void run() {
            try {
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("FileMonitorEventDispatcher is ready to dispatch Events.");

                while (!isInterrupted()) {

                    // //
                    // waiting for a new event
                    // //
                	final FileSystemMonitorEvent event;
                	try{
                		event = FileBasedFlowManager.this.eventMailBox.take();
                	}catch (InterruptedException e) {
                		this.interrupt();
                		return;
					}

					if(LOGGER.isLoggable(Level.FINE))
						LOGGER.fine("FileMonitorEventDispatcher: processing incoming event " + event);

                    // //
                    // is there any BaseEventConsumer waiting for this particular event?
                    // //
                    boolean eventServed = false;
                    final Iterator<BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration>> it =
							FileBasedFlowManager.this.collectingEventConsumers.iterator();
                    while (it.hasNext()) {
                        final BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration> 
								consumer = it.next();

						if(LOGGER.isLoggable(Level.FINE))
							LOGGER.fine("Checking consumer " + consumer +" for " + event);

                        if (consumer.consume(event)) {
                            // //
                            // we have found an Event BaseEventConsumer waiting for this event, if
                            // we have changed state we remove it from the list
                            // //
                            if (consumer.getStatus() == EventConsumerStatus.EXECUTING) {
								if(LOGGER.isLoggable(Level.FINE))
									LOGGER.fine(event + " was the last needed event for " + consumer);
                                it.remove();
							} else
								if(LOGGER.isLoggable(Level.FINE))
									LOGGER.fine(event + " was consumed by " + consumer);
                            
                            //event served
                            eventServed = true;
                            break;
                        }
                    }

					if(LOGGER.isLoggable(Level.FINE))
						LOGGER.fine("FileMonitorEventDispatcher: " + event + (eventServed?"":" not") + " served");

                    if (!eventServed) {
                        // //
                        // if no EventConsumer is found, we need to create a new one
                        // //
						final FileBasedEventConsumerConfiguration configuration =
								((FileBasedEventConsumerConfiguration) FileBasedFlowManager.this.getConfiguration().getEventConsumerConfiguration()).clone();
                        final FileBasedEventConsumer brandNewConsumer =
								new FileBasedEventConsumer( getCatalog(), configuration);

                        if (brandNewConsumer.consume(event)) {
                            // //
                            // We just created a brand new BaseEventConsumer which can handle this event.
                            // If it needs some other events to complete, we'll put it in the EventConsumers
							// waiting list.
                            // //
                            if ( brandNewConsumer.getStatus() != EventConsumerStatus.EXECUTING) {
								if(LOGGER.isLoggable(Level.FINE))
									LOGGER.fine(brandNewConsumer + " created on event " + event);
								FileBasedFlowManager.this.collectingEventConsumers.add(brandNewConsumer);
							}
							else
								if(LOGGER.isLoggable(Level.FINE))
									LOGGER.fine(event + " was the only needed event for " + brandNewConsumer);

                            eventServed = true;
                        } else
							LOGGER.warning("!!! No consumer could serve " + event + " (neither "+brandNewConsumer+" could)");
                    }
                }
            } catch (InterruptedException e) { // may be thrown by the "stop" button on web interface
                LOGGER.log(Level.SEVERE, "Caught an Interrupted Exception: "+ e.getLocalizedMessage(), e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Caught an IOException: "+ e.getLocalizedMessage(), e);
            } catch (CloneNotSupportedException e) {
                LOGGER.log(Level.SEVERE, new StringBuilder("Caught a CloneNotSupportedException Exception: ")
                .append(e.getLocalizedMessage()).toString(), e);
			}

        }
    }

    private File workingDirectory;

    /**
     * initialized flag
     */
    private boolean initialized;

    /**
     * started flag
     */
    private boolean started = false;

    /**
     * paused flag
     */
    private boolean paused;

    /**
     * termination flag
     */
    private boolean termination;

    /**
     * The MailBox
     */
    private final BlockingQueue<FileSystemMonitorEvent> eventMailBox = new LinkedBlockingQueue<FileSystemMonitorEvent>();

    /**
     * The FileMonitorEventDispatcher
     */
    private EventDispatcher dispatcher;

    /**
     * EventGenerator
     */
    private EventGenerator eventGenerator;

    private final List<BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration>> collectingEventConsumers = new ArrayList<BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration>>();

    /**
     * @param configuration
     * @throws IOException
     */
    public FileBasedFlowManager() throws IOException {
    }

    /**
     * @param configuration
     * @throws IOException
     */
    private void initialize(FileBasedFlowConfiguration configuration) throws IOException {
        this.initialized = false;
        this.paused = false;
        this.termination = false;

		String baseDir = ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory();
		if(baseDir == null)
            throw new IllegalArgumentException("Working dir is null");

        this.workingDirectory = IOUtils.findLocation(configuration.getWorkingDirectory(), 
													 new File(baseDir));

        if (workingDirectory == null || !workingDirectory.exists() || !workingDirectory.canWrite()
                || !workingDirectory.isDirectory())
            throw new IllegalArgumentException(new StringBuilder("Working dir is invalid: ")
                    .append(">").append(baseDir).append("< ")
                    .append(">").append(configuration.getWorkingDirectory()).append("< ").toString()
					);
       
       this.autorun = configuration.autorun();

       if(this.autorun) {
           if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Automatic Flow Startup");
           this.resume();
       }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#dispose()
     */
    public synchronized void dispose() {
        if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Disposing: " + this.getId());
        this.termination = true;
        this.notify();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#isRunning()
     */
    public boolean isRunning() {
        return !paused && started;
    }

    public synchronized void run() {
        do {
            if (termination) {
                if (initialized) {
                    dispatcher.shutdown();
                    eventGenerator.dispose();
                    initialized = false;
                }

                paused = true;

                break;
            }

            while (paused) {
                try {
                    if (initialized && ((eventGenerator != null) && eventGenerator.isRunning())) {
                        eventGenerator.stop();
                        eventGenerator.dispose();
                        eventGenerator = null;
                    }

                    this.wait();

                    if (termination) {
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.severe("Error on dispatcher initialization: " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
            }

            if (!initialized) {
                // //
                // Initialize objects
                // //

                this.dispatcher = new EventDispatcher();
                dispatcher.start();
                initialized = true;
            }

            while (!paused) {
                try {
                    if (initialized && ((eventGenerator == null) || !eventGenerator.isRunning())) {
                        // //
                        // Creating the FileBasedEventGenerator, which waits for new events
                        // //
                        try {
                            LOGGER.info("EventGeneratorCreationStart");
                            final EventGeneratorConfiguration generatorConfig = getConfiguration().getEventGeneratorConfiguration();
                            final String serviceID = generatorConfig.getServiceID();
                            LOGGER.info("EventGeneratorCreationServiceID: "+ serviceID);
                            final EventGeneratorService<EventObject, EventGeneratorConfiguration> generatorService = getCatalog().getResource(serviceID, EventGeneratorService.class);
                            if (generatorService != null) {
                                LOGGER.info("EventGeneratorCreationFound!");
                                eventGenerator = generatorService.createEventGenerator(generatorConfig);
                                if (eventGenerator!=null){
                                	LOGGER.info("EventGeneratorCreationCreated!");
                                	eventGenerator.addListener(this);
                                	LOGGER.info("EventGeneratorCreationAdded!");
                                	eventGenerator.start();
                                	LOGGER.info("EventGeneratorCreationStarted!");
                                } else {
                                	throw new RuntimeException("Error on EventGenerator creations");
                                }

                            }
                            LOGGER.info("EventGeneratorCreationEnd");
                        } catch (Throwable t) {
                            LOGGER.log(Level.SEVERE, "Error on FS-Monitor initialization: " + t.getLocalizedMessage(), t);
                            throw new RuntimeException(t);
                        }
                    }

                    this.wait();

                    if (termination) {
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.severe("FlowManager cycle exception: " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
            }
        } while (true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#start()
     */
    public synchronized void resume() {
    	if(LOGGER.isLoggable(Level.INFO))
        	LOGGER.info("Resuming: " + this.getId());

        if (!started) {
            getCatalog().getExecutor().execute(this);
            this.started = true;
            this.paused = false;
        } else if (!isRunning()) {
            this.paused = false;
            this.notify();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#stop()
     */
    public synchronized void pause() {
    	if(LOGGER.isLoggable(Level.INFO))
        	LOGGER.info("Pausing: " + this.getId());

        if (isRunning()) {
            this.paused = true;
            this.notify();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#reset()
     */
    public void reset() {
        LOGGER.info("Resetting: " + this.getId());
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
     * @return the initialized
     */
    public boolean isInited() {
        return initialized;
    }

    /**
     * @return the paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * @return the termination
     */
    public boolean isTermination() {
        return termination;
    }
    
    /**
     * @return the workingDirectory
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory(File outputDir) {
        this.workingDirectory = outputDir;
    }


    public EventGenerator<FileSystemMonitorEvent> getEventGenerator() {
        return this.eventGenerator;
    }

    public void setEventGenerator(EventGenerator<FileSystemMonitorEvent> eventGenerator) {
        this.eventGenerator = (FileBasedEventGenerator) eventGenerator;

    }

    public void eventGenerated(FileSystemMonitorEvent event) {
        try {
            this.eventMailBox.put(event);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public synchronized void setConfiguration(FileBasedFlowConfiguration configuration) {
        super.setConfiguration(configuration);
        try {
            initialize(configuration);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

    }

    @Override
    public synchronized void load() throws IOException{
        super.load();
    }

    @Override
    public synchronized boolean remove() throws IOException{
        return super.remove();
    }

    @Override
    public synchronized void persist()throws IOException {
        super.persist();
    }

}
