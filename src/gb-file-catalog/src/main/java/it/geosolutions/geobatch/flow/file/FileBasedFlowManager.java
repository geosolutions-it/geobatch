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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.catalog.impl.BasePersistentResource;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.FlowManager;
import it.geosolutions.geobatch.flow.Job;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.event.generator.EventGenerator;
import it.geosolutions.geobatch.flow.event.generator.EventGeneratorService;
import it.geosolutions.geobatch.flow.event.generator.FlowEventListener;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author (rev2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
@Component("FlowManager")
@ManagedResource(objectName = "spring:name=FileBasedFlowManager", description = "A JMX-managed FileBasedFlowManager")
public class FileBasedFlowManager extends BasePersistentResource<FileBasedFlowConfiguration>
        implements FlowManager<FileSystemEvent, FileBasedFlowConfiguration>, Runnable, Job {

    /** Default Logger **/
    private final static Logger LOGGER = LoggerFactory.getLogger(FlowManager.class);

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
    private final BlockingQueue<FileSystemEvent> eventMailBox = new LinkedBlockingQueue<FileSystemEvent>();

    /**
     * The FileMonitorEventDispatcher
     */
    private FileBasedEventDispatcher dispatcher;

    /**
     * EventGenerator
     */
    private EventGenerator<FileSystemEvent> eventGenerator; // FileBasedEventGenerator<FileSystemEvent>

    private final List<FileBasedEventConsumer> eventConsumers = new ArrayList<FileBasedEventConsumer>();

    private ThreadPoolExecutor executor;

    @ManagedAttribute
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * @param configuration
     *            the fileBasedFlowConfiguration to use in initialization
     * @throws IOException
     */
    public FileBasedFlowManager(FileBasedFlowConfiguration configuration) throws IOException,
            NullPointerException {
        super(configuration.getId(), configuration.getName(), configuration.getDescription());
        initialize(configuration);
    }

    public FileBasedFlowManager(String baseName, String name, String description) {
        super(baseName, name, description);
    }

    /**
     * @param configuration
     * @throws IOException
     */
    private void initialize(FileBasedFlowConfiguration configuration) throws IOException,
            NullPointerException {
        this.initialized = false;
        this.paused = false;
        this.terminationRequest = false;

        File baseDir = ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory();

        if (baseDir == null)
            throw new NullPointerException(
                    "FileBasedFlowManager:initialize(): Base Working dir is null");

        this.workingDirectory = Path.findLocation(configuration.getWorkingDirectory(), baseDir);

        if (workingDirectory == null)
            throw new IllegalArgumentException(new StringBuilder(
                    "FileBasedFlowManager:initialize(): Working dir is invalid: ").append('>')
                    .append(baseDir).append("< ").append('>')
                    .append(configuration.getWorkingDirectory()).append("< ").toString());

        if (!workingDirectory.canWrite() || !workingDirectory.isDirectory())
            throw new IllegalArgumentException(new StringBuilder(
                    "FileBasedFlowManager:initialize(): Working dir is invalid: ").append('>')
                    .append(baseDir).append("< ").append('>')
                    .append(configuration.getWorkingDirectory()).append("< ").toString());

        this.autorun = configuration.isAutorun();

        final int queueSize = configuration.getWorkQueueSize() > 0 ? configuration
                .getWorkQueueSize() : 100;
        final int corePoolSize = configuration.getCorePoolSize() > 0 ? configuration
                .getCorePoolSize() : 10;
        final int maximumPoolSize = configuration.getMaximumPoolSize() > 0 ? configuration
                .getMaximumPoolSize() : 30;
        final long keepAlive = configuration.getKeepAliveTime() > 0 ? configuration
                .getKeepAliveTime() : 150; // seconds

        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);

        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAlive,
                TimeUnit.SECONDS, queue);

        if (this.autorun) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("FileBasedFlowManager:initialize(): Automatic Flow Startup for '"
                        + getName() + "'");
            this.resume();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#dispose()
     */
    public synchronized void dispose() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("FileBasedFlowManager:dispose(): " + this.getId());
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
            throw new IllegalArgumentException(
                    "FileBasedFlowManager:dispose(): This flow is not managing " + fbec);
        }

        if (fbec.getStatus() != EventConsumerStatus.COMPLETED
                && fbec.getStatus() != EventConsumerStatus.FAILED) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("FileBasedFlowManager:dispose(): Disposing uncompleted consumer "
                        + fbec);
        }

        synchronized (eventConsumers) {
            eventConsumers.remove(fbec);
        }

        // dunno if we should also force a fbec.dispose();
        // it's called at the end of the consumer thread automatically
    }

    /**
     * Main thread loop. 
     * <ul>
     * <LI>Create and tear down generators when the flow is paused. </LI>
     * <LI>Init the dispatcher.</LI>
     * </UL>
     * 
     * TODO the stopping condition is never used...
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

                        eventGenerator.pause();
                    }

                    this.wait();

                    if (terminationRequest) {
                        break;
                    }
                } catch (InterruptedException e) {
                    final String message = "FileBasedFlowManager:run(): Error on dispatcher initialization: "
                            + e.getLocalizedMessage();
                    LOGGER.error(message);
                    throw new RuntimeException(message);
                }
            }

            if (!initialized) {
                // Initialize objects
                this.dispatcher = new FileBasedEventDispatcher(this, eventMailBox);
                dispatcher.start();
                initialized = true;
            }

            while (!paused) {
                try {
                    if (initialized) {
                        if (eventGenerator == null) {
                            // (re)Creating the FileBasedEventGenerator, which waits for
                            // new events
                            try {
                                createGenerator();
                            } catch (Throwable t) {
                                String message = "FileBasedFlowManager:run(): Error on FS-Monitor initialization: "
                                        + t.getLocalizedMessage();
                                LOGGER.error(message, t);
                                throw new RuntimeException(message);
                            }
                        } else {
                            eventGenerator.start();
                        }
                    }

                    this.wait();

                    if (terminationRequest) {
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("FileBasedFlowManager:run(): FlowManager cycle exception: "
                                    + e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void createGenerator() {
        final EventGeneratorConfiguration generatorConfig = getConfiguration().getEventGeneratorConfiguration();
        final String serviceID = generatorConfig.getServiceID();
        if (LOGGER.isInfoEnabled())
            LOGGER.info("FileBasedFlowManager:createGenerator(): EventGeneratorCreationServiceID: "
                    + serviceID);
        final EventGeneratorService<FileSystemEvent, EventGeneratorConfiguration> generatorService = CatalogHolder
                .getCatalog().getResource(serviceID, EventGeneratorService.class);
        if (generatorService != null) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("FileBasedFlowManager:createGenerator(): EventGeneratorService found");
            eventGenerator = generatorService.createEventGenerator(generatorConfig);
            if (eventGenerator != null) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("FileBasedFlowManager:createGenerator(): FileSystemEventGenerator created");
                eventGenerator.addListener(new GeneratorListener());
                eventGenerator.start();
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("FileBasedFlowManager:createGenerator(): FileSystemEventGenerator started");
            } else {
                final String message = "FileBasedFlowManager:createGenerator(): Error on EventGenerator creations";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(message);
                }
                throw new RuntimeException(message);
            }
        } else {
            final String message = "FileBasedFlowManager::createGenerator(): Unable to get the "
                    + "generator service as resource from the catalog";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message);
            }
            final RuntimeException re = new RuntimeException(message);
            throw re;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#start()
     */
    public synchronized void resume() {

        if (LOGGER.isInfoEnabled())
            LOGGER.info("FileBasedFlowManager::resume(): RESUMING ->" + this.getId());

        if (!started) {
            executor.execute(this);
            this.started = true;
            this.paused = false;
            if (LOGGER.isInfoEnabled())
                LOGGER.info("FileBasedFlowManager::resume(): STARTED ->" + this.getId());
        } else if (!isRunning()) {
            this.paused = false;
            this.notify();
            if (LOGGER.isInfoEnabled())
                LOGGER.info("FileBasedFlowManager::resume(): RESUMED ->" + this.getId());
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
        if (LOGGER.isInfoEnabled())
            LOGGER.info("FileBasedFlowManager::pause(): PAUSING -> " + this.getId());

        if (isRunning()) {
            this.paused = true;
            this.notify();
        }
        return true;
    }

    public synchronized boolean pause(boolean sub) {
        pause();

        if (sub) {
            for (FileBasedEventConsumer consumer : eventConsumers) {
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
        LOGGER.info("FileBasedFlowManager: Resetting: " + this.getId());
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
     * @uml.property name="paused"
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
     * @uml.property name="workingDirectory"
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     * @uml.property name="workingDirectory"
     */
    public void setWorkingDirectory(File outputDir) {
        this.workingDirectory = outputDir;
    }

    public EventGenerator<FileSystemEvent> getEventGenerator() {
        return this.eventGenerator;
    }

    public void setEventGenerator(EventGenerator<FileSystemEvent> eventGenerator) {
        this.eventGenerator = eventGenerator;

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
    }

    public List<FileBasedEventConsumer> getEventConsumers() {
        return eventConsumers;
    }

    /**
     * we don't want to manipulate the list externally. please enforce this.
     * 
     * @param consumer
     */
    void add(FileBasedEventConsumer consumer) {
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
    Future<Queue<FileSystemEvent>> execute(FileBasedEventConsumer consumer) {
        if (consumer.getStatus() != EventConsumerStatus.EXECUTING) {
            final String message = "FileBasedFlowManager:execute(): Consumer " + consumer
                    + " is not in an EXECUTING state.";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        if (!eventConsumers.contains(consumer)) {
            final String message = "FileBasedFlowManager:execute(): Consumer " + consumer
                    + " is not handled by the current flow manager.";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
        try {
            return this.executor.submit(consumer);
        } catch (RejectedExecutionException r) {
            /*
             * + "Will be rejected when the Executor has been shut down, and also " +
             * "when the Executor uses finite bounds for both maximum threads and " +
             * "work queue capacity, and is saturated." +
             * " In either case, the execute method invokes the "
             */
            if (LOGGER.isErrorEnabled())
                LOGGER.error("FileBasedFlowManager:execute(): Unable to submit the consumer (id:"
                                + consumer.getId() + ") to the flow manager (id:" + this.getId()
                                + ") queue.\nMessage is:" + r.getLocalizedMessage()
                                + "\nThread pool executor info:"
                                + "\nMaximum allowed number of threads: "
                                + executor.getMaximumPoolSize() + "\nWorking Queue size: "
                                + executor.getQueue().size()
                                + "\nWorking Queue remaining capacity: "
                                + executor.getQueue().remainingCapacity()
                                + "\nCurrent number of threads: " + executor.getPoolSize()
                                + "\nApproximate number of threads that are actively executing : "
                                + executor.getActiveCount() + "\nCore number of threads: "
                                + executor.getCorePoolSize() + "\nKeepAliveTime [secs]: "
                                + executor.getKeepAliveTime(TimeUnit.SECONDS), r);
            throw new RuntimeException(r);
        } catch (Throwable t) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("FileBasedFlowManager:execute(): Unable to submit the consumer (id:"
                                + consumer.getId() + ") to the flow manager (id:" + this.getId()
                                + ") queue.\nMessage is:" + t.getLocalizedMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public void postEvent(FileSystemEvent event) {
        try {
            eventMailBox.put(event);
        } catch (NullPointerException npe) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("FileBasedFlowManager:postEvent(): Unable to add a null event to the flow manager (id:"
                                + this.getId() + ") eventMailBox.\nMessage is:"
                                + npe.getLocalizedMessage(), npe);
            throw npe;
        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("FileBasedFlowManager:postEvent(): Unable to add event ["
                        + event.toString() + "] to the flow manager (id:" + this.getId()
                        + ") eventMailBox.\nMessage is:" + e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Will listen for the eventGenerator events, and put them in the blocking mailbox.
     */
    private class GeneratorListener implements FlowEventListener<FileSystemEvent> {
        public void eventGenerated(FileSystemEvent event) {
            postEvent(event);
        }
    }

}

