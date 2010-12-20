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
import it.geosolutions.geobatch.flow.Job;
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
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * 
 * 
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public class FileBasedFlowManager extends BasePersistentResource<FileBasedFlowConfiguration>
        implements FlowManager<FileSystemMonitorEvent, FileBasedFlowConfiguration>, Runnable, Job {

    /** Default Logger **/
    private final static Logger LOGGER = Logger.getLogger(FlowManager.class.toString());

    private boolean autorun = false;

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
    private boolean terminationRequest;

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

    // private final List<BaseEventConsumer<FileSystemMonitorEvent,
    // FileBasedEventConsumerConfiguration>> eventConsumers =
    // new ArrayList<BaseEventConsumer<FileSystemMonitorEvent,
    // FileBasedEventConsumerConfiguration>>();
    private final List<FileBasedEventConsumer> eventConsumers = new ArrayList<FileBasedEventConsumer>();

    private ThreadPoolExecutor executor;

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
        this.terminationRequest = false;

        String baseDir = ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory();
        if (baseDir == null)
            throw new IllegalArgumentException("Working dir is null");

        this.workingDirectory = IOUtils.findLocation(configuration.getWorkingDirectory(), new File(
                baseDir));

        if (workingDirectory == null || !workingDirectory.exists() || !workingDirectory.canWrite()
                || !workingDirectory.isDirectory())
            throw new IllegalArgumentException(new StringBuilder("Working dir is invalid: ")
                    .append('>').append(baseDir).append("< ").append('>').append(
                            configuration.getWorkingDirectory()).append("< ").toString());

        this.autorun = configuration.isAutorun();

        // sensible defaults TODO make me configurable!!!!
        final int queueSize = configuration.getWorkQueueSize() > 0 ? configuration
                .getWorkQueueSize() : 100;
        final int corePoolSize = configuration.getCorePoolSize() > 0 ? configuration
                .getCorePoolSize() : 10;
        final int maximumPoolSize = configuration.getMaximumPoolSize() > 0 ? configuration
                .getMaximumPoolSize() : 30;
        final long keepAlive = configuration.getKeepAliveTime() > 0 ? configuration
                .getKeepAliveTime() : 15000;

        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue(queueSize);
        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAlive,
TimeUnit.MILLISECONDS, queue);

        if (this.autorun) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Automatic Flow Startup for flow '" + getName() + "'");
            this.resume();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#dispose()
     */
    public synchronized void dispose() {
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Disposing: " + this.getId());
        this.terminationRequest = true;
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

    /**
     * Remove the given consumer instance from the ones handled by this flow.
     * <P>
     * It should only be used on instances that are not running, i.e. in a COMPLETED or FAILED
     * state.
     * 
     * @param fbec
     *            the consumer to be removed.
     */
    public void dispose(FileBasedEventConsumer fbec) {
        if (!eventConsumers.contains(fbec)) {
            throw new IllegalArgumentException("This flow is not managing " + fbec);
        }

        if (fbec.getStatus() != EventConsumerStatus.COMPLETED
                && fbec.getStatus() != EventConsumerStatus.FAILED) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Disposing uncompleted consumer " + fbec);
        }

        synchronized (eventConsumers) {
            eventConsumers.remove(fbec);
        }

        // dunno if we should also force a fbec.dispose();
        // it's called at the end of the consumer thread automatically
    }

    /**
     * Main thread loop.
     * 
     * <LI>Create and tear down generators when the flow is paused. <LI>Init the dispatcher.
     */
    public synchronized void run() {
        for (;;) {
            if (terminationRequest) {
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

                    if (terminationRequest) {
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.severe("Error on dispatcher initialization: " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
            }

            if (!initialized) {
                // Initialize objects
                this.dispatcher = new EventDispatcher(this, eventMailBox);
                dispatcher.start();
                initialized = true;
            }

            while (!paused) {
                try {
                    if (initialized && ((eventGenerator == null) || !eventGenerator.isRunning())) {
                        // Creating the FileBasedEventGenerator, which waits for
                        // new events
                        try {
                            createGenerator();
                        } catch (Throwable t) {
                            LOGGER.log(Level.SEVERE, "Error on FS-Monitor initialization: "
                                    + t.getLocalizedMessage(), t);
                            throw new RuntimeException(t);
                        }
                    }

                    this.wait();

                    if (terminationRequest) {
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.severe("FlowManager cycle exception: " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void createGenerator() {
        // LOGGER.info("EventGeneratorCreationStart");
        final EventGeneratorConfiguration generatorConfig = getConfiguration()
                .getEventGeneratorConfiguration();
        final String serviceID = generatorConfig.getServiceID();
        LOGGER.info("EventGeneratorCreationServiceID: " + serviceID);
        final EventGeneratorService<EventObject, EventGeneratorConfiguration> generatorService = CatalogHolder
                .getCatalog().getResource(serviceID, EventGeneratorService.class);
        if (generatorService != null) {
            LOGGER.info("EventGeneratorCreationFound!");
            eventGenerator = generatorService.createEventGenerator(generatorConfig);
            if (eventGenerator != null) {
                LOGGER.info("EventGeneratorCreationCreated!");
                eventGenerator.addListener(new GeneratorListener());
                eventGenerator.start();
                LOGGER.info("EventGeneratorCreationStarted!");
            } else {
                throw new RuntimeException("Error on EventGenerator creations");
            }
        }
        // LOGGER.info("EventGeneratorCreationEnd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#start()
     */
    public synchronized void resume() {
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Resuming: " + this.getId());

        if (!started) {
            executor.execute(this);
            this.started = true;
            this.paused = false;
        } else if (!isRunning()) {
            this.paused = false;
            this.notify();
        }
    }

    /**
     * Implements the {@link Job#pause()} interface.
     * 
     * <P>
     * Pausing is implemented by stopping and removing the EventGenerator so that no events are put
     * into the mailbox.
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#stop()
     */
    public synchronized boolean pause() {
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Pausing: " + this.getId());

        if (isRunning()) {
            this.paused = true;
            this.notify();
        }

        return true;
    }

    public synchronized boolean pause(boolean sub) {
        pause();

        if (sub) {
            for (BaseEventConsumer consumer : eventConsumers) {
                consumer.pause(true);
            }
        }

        return true;
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
        return terminationRequest;
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

    @Override
    public synchronized void setConfiguration(FileBasedFlowConfiguration configuration) {
        super.setConfiguration(configuration);
        try {
            initialize(configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void load() throws IOException {
        super.load();
        setName(getConfiguration().getName());
    }

    @Override
    public synchronized boolean remove() throws IOException {
        return super.remove();
    }

    @Override
    public synchronized void persist() throws IOException {
        super.persist();
    }

    public boolean isAutorun() {
        return autorun;
    }

    public void setAutorun(boolean autorun) {
        this.autorun = autorun;
    }

    public List<FileBasedEventConsumer> getEventConsumers() {
        return eventConsumers;
    }

    /**
     * we don't want to manipulate the list externally. please enforce this.
     * 
     * @param consumer
     */
    /* package private */void add(FileBasedEventConsumer consumer) {
        eventConsumers.add(consumer);
    }

    /**
     * Run the given consumer into the threadpool.
     * 
     * @param consumer
     *            The instance to be executed.
     * @throws IllegalStateException
     *             if the consumer is not in the EXECUTING state.
     * @throws IllegalArgumentException
     *             if the consumer is not in the {@link #eventConsumers} list of this FlowManager.
     */
    /* package private */void execute(FileBasedEventConsumer consumer) {
        if (consumer.getStatus() != EventConsumerStatus.EXECUTING)
            throw new IllegalStateException("Consumer " + consumer
                    + " is not in an EXECUTING state.");

        if (!eventConsumers.contains(consumer))
            throw new IllegalArgumentException("Consumer " + consumer
                    + " is not handled by the current flowmanager.");

        this.executor.execute(consumer);
    }

    public void postEvent(FileSystemMonitorEvent event) {
        try {
            eventMailBox.put(event);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Will listen for the eventGenerator events, and put them in the blocking mailbox.
     */
    private class GeneratorListener implements FlowEventListener<FileSystemMonitorEvent> {
        public void eventGenerated(FileSystemMonitorEvent event) {
            postEvent(event);
        }
    }

}

/**
 * Fetch events and feed them to Consumers.
 * 
 * <P>
 * For every incoming event, existing consumers are checked if they are waiting for it. <BR>
 * If the new event is not consumed by any existing consumer, a new consumer will be created.
 * 
 * @author AlFa
 */
final class EventDispatcher extends Thread {
    private final static Logger LOGGER = Logger.getLogger(EventDispatcher.class.getName());

    private final BlockingQueue<FileSystemMonitorEvent> eventMailBox;

    private final FileBasedFlowManager fm;

    // ----------------------------------------------- PUBLIC METHODS
    /**
     * Default Constructor
     */
    public EventDispatcher(FileBasedFlowManager fm,
            BlockingQueue<FileSystemMonitorEvent> eventMailBox) {
        super(new StringBuilder("EventDispatcherThread-").append(fm.getId()).toString());

        this.eventMailBox = eventMailBox;
        this.fm = fm;

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
                try {
                    event = eventMailBox.take(); // blocking call
                } catch (InterruptedException e) {
                    this.interrupt();
                    return;
                }

                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("FileMonitorEventDispatcher: processing incoming event " + event);

                // //
                // is there any BaseEventConsumer waiting for this particular
                // event?
                // //
                boolean eventServed = false;

                for (FileBasedEventConsumer consumer : fm.getEventConsumers()) {

                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Checking consumer " + consumer + " for " + event);

                    if (consumer.consume(event)) {
                        // //
                        // we have found an Event BaseEventConsumer waiting for
                        // this event, if
                        // we have changed state we remove it from the list
                        // //
                        if (consumer.getStatus() == EventConsumerStatus.EXECUTING) {
                            if (LOGGER.isLoggable(Level.FINE))
                                LOGGER.fine(event + " was the last needed event for " + consumer);

                            // are we executing? If we are, let's trigger a
                            // thread!
                            fm.execute(consumer);
                        } else if (LOGGER.isLoggable(Level.FINE))
                            LOGGER.fine(event + " was consumed by " + consumer);

                        // event served
                        eventServed = true;
                        break;
                    }
                }

                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("FileMonitorEventDispatcher: " + event
                            + (eventServed ? "" : " not") + " served");

                if (!eventServed) {
                    // //
                    // if no EventConsumer is found, we need to create a new one
                    // //
                    final FileBasedEventConsumerConfiguration configuration = ((FileBasedEventConsumerConfiguration) fm
                            .getConfiguration().getEventConsumerConfiguration()).clone();
                    final FileBasedEventConsumer brandNewConsumer = new FileBasedEventConsumer(
                            configuration);

                    if (brandNewConsumer.consume(event)) {
                        // //
                        // We just created a brand new BaseEventConsumer which
                        // can handle this event.
                        // If it needs some other events to complete, we'll put
                        // it in the EventConsumers
                        // waiting list.
                        // //
                        if (brandNewConsumer.getStatus() != EventConsumerStatus.EXECUTING) {
                            if (LOGGER.isLoggable(Level.FINE))
                                LOGGER.fine(brandNewConsumer + " created on event " + event);
                            fm.add(brandNewConsumer);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE))
                                LOGGER.fine(event + " was the only needed event for "
                                        + brandNewConsumer);

                            // etj: shouldn't we call
                            // executor.execute(consumer); here?
                            fm.add(brandNewConsumer);
                            fm.execute(brandNewConsumer);
                        }

                        eventServed = true;
                    } else
                        LOGGER.warning("!!! No consumer could serve " + event + " (neither "
                                + brandNewConsumer + " could)");
                }
            }
        } catch (InterruptedException e) { // may be thrown by the "stop" button
            // on web interface
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            // } catch (CloneNotSupportedException e) {
            // LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

    }
}
