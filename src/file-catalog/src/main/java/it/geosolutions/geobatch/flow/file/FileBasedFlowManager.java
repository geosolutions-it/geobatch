/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
import it.geosolutions.geobatch.catalog.impl.BasePersistentResource;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.FlowManager;
import it.geosolutions.geobatch.flow.Job;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.generator.EventGenerator;
import it.geosolutions.geobatch.flow.event.generator.EventGeneratorService;
import it.geosolutions.geobatch.flow.event.generator.FlowEventListener;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.settings.GBSettings;
import it.geosolutions.geobatch.settings.GBSettingsCatalog;
import it.geosolutions.geobatch.settings.flow.FlowSettings;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.collection.UnmodifiableCollection;
import org.apache.commons.collections.set.UnmodifiableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;

/**
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author (rev2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
// @Component("FlowManager")
// @ManagedResource(objectName = "spring:name=FileBasedFlowManager", description
// = "A JMX-managed FileBasedFlowManager")
public class FileBasedFlowManager
    extends BasePersistentResource<FileBasedFlowConfiguration>
    implements FlowManager<FileSystemEvent, FileBasedFlowConfiguration>, Runnable, Job {

    /** Default Logger **/
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowManager.class);

    private String name;
    private String description;

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

    private final ConcurrentMap<String, EventConsumer> eventConsumers = new ConcurrentHashMap<String, EventConsumer>();
    private volatile Lock eventConsumersLock=new ReentrantLock();

    /**
     * maximum numbers of executed see {@link EventConsumer#getStatus()}
     * 
     * @see #purgeConsumers(int)
     */
    private int maxStoredConsumers;
    /**
     * @see {@link FileBasedFlowConfiguration#isKeepConsumers()}
     */
    private boolean keepConsumers;

    private ThreadPoolExecutor executor;

    /**
     * @param configuration the fileBasedFlowConfiguration to use in
     *            initialization
     * @throws IOException
     */
    public FileBasedFlowManager(FileBasedFlowConfiguration configuration) 
            throws IOException, NullPointerException {

        super(configuration.getId());

        name = configuration.getName();
        description = configuration.getDescription();

        initialize(configuration);
        super.setConfiguration(configuration);
    }

    /**
     * Used just before loading the object.
     */
    public FileBasedFlowManager(String baseName) throws NullPointerException, IOException {
        super(baseName);
    }

    /**
     * WARNING this flow manager is not configured nor initialized
     * 
     * @param baseName
     * @param name
     * @param description
     * @throws NullPointerException
     * @throws IOException
     */
    public FileBasedFlowManager(String baseName, String name, String description)
        throws NullPointerException, IOException {

        super(baseName);
        this.name = name;
        this.description = description;

//        LOGGER.error("Creating an unconfigured " + getClass().getSimpleName());
//        if(LOGGER.isDebugEnabled()) {
//            LOGGER.debug("Creating an unconfigured " + getClass().getSimpleName(), new RuntimeException("Trace!"));
//        }

        // warning this flow manager is not configured nor initialized (working
        // dir related problem)
        // initialize(new FileBasedFlowConfiguration(baseName, name, null,
        // description, null));
    }

    @ManagedAttribute
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * @param configuration
     * @throws IOException
     */
    private void initialize(FileBasedFlowConfiguration configuration) throws IOException,
        NullPointerException {
        final GBSettingsCatalog settingsCatalog = CatalogHolder.getSettingsCatalog();
        final GBSettings settings;
        final FlowSettings fs;
        try {
            settings = settingsCatalog.find("FLOW");
            if ((settings != null) && (settings instanceof FlowSettings)) {
                fs = (FlowSettings)settings;
            } else {
                fs = new FlowSettings();
                // store the file for further flow loads
                settingsCatalog.save(fs);
            }

            this.initialized = false;
            this.paused = false;
            this.terminationRequest = false;
            
//            if (configuration.getWorkingDirectory()==null){
//                throw new IllegalArgumentException("Unable to configure a flow without a valid working dir");
//            }
//            this.setWorkingDirectory(new File(configuration.getWorkingDirectory()));

            maxStoredConsumers = configuration.getMaxStoredConsumers();
            if (maxStoredConsumers < 1) {
                maxStoredConsumers = fs.getMaxStoredConsumers(); // default
                                                                 // value
            }
            this.keepConsumers=configuration.isKeepConsumers();

            this.autorun = configuration.isAutorun();

            final int queueSize = (configuration.getWorkQueueSize() > 0)
                ? configuration.getWorkQueueSize() : fs.getWorkQueueSize();
            final int corePoolSize = (configuration.getCorePoolSize() > 0)
                ? configuration.getCorePoolSize() : fs.getCorePoolSize();
            final int maximumPoolSize = (configuration.getMaximumPoolSize() > 0) ? configuration
                .getMaximumPoolSize() : fs.getMaximumPoolSize();
            final long keepAlive = (configuration.getKeepAliveTime() > 0)
                ? configuration.getKeepAliveTime() : fs.getKeepAliveTime(); // seconds

            final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);

            this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAlive,
                                                   TimeUnit.SECONDS, queue);

            if (this.autorun) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Automatic Flow Startup for '" + getName() + "'");
                }
                this.resume();
            }

        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Failed to save the flow settings");
            }

            final IOException ioe = new IOException(e);
            ioe.initCause(e.getCause());
            throw ioe;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#dispose()
     */
    public synchronized void dispose() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("dispose: " + this.getId());
        }
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
     * It should only be used on instances that are not running, i.e. in a
     * COMPLETED or FAILED state.
     * 
     * @param fbec the consumer to be removed.
     * @throws IllegalArgumentException
     */
    public void disposeConsumer(EventConsumer fbec) throws IllegalArgumentException {
        if (fbec == null) {
            throw new IllegalArgumentException("Unable to dispose a null consumer object");
        }
        disposeConsumer(fbec.getId());
    }


    /**
     * 
     * Remove the given consumer instance from the ones handled by this flow.
     * <P>
     * It should only be used on instances that are not running, i.e. in a
     * COMPLETED or FAILED state.
     * 
     * @param fbec the consumer to be removed.
     */
    @Override
    public void disposeConsumer(String uuid) throws IllegalArgumentException {

        if (uuid == null) {
            throw new IllegalArgumentException("Unable to dispose a null consumer object");
        }

        try {
                this.eventConsumersLock.lock();
                final EventConsumer<FileSystemEvent, EventConsumerConfiguration> fbec = eventConsumers.get(uuid);
        
                if (fbec == null) {
                    throw new IllegalArgumentException("This flow is not managing consumer: " + uuid);
                }
        
                if ((fbec.getStatus() != EventConsumerStatus.COMPLETED)
                    && (fbec.getStatus() != EventConsumerStatus.FAILED)) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Goning to dispose and uncompleted consumer " + fbec);
                    }
                    fbec.cancel();
                }
    
                eventConsumers.remove(uuid);
                fbec.dispose();
            
        } finally {
        	this.eventConsumersLock.unlock();
        }

    }

    /**
     * Main thread loop.
     * <ul>
     * <LI>Create and tear down generators when the flow is paused.</LI>
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
                    final String message = "Error on dispatcher initialization: " + e.getLocalizedMessage();
                    LOGGER.error(message);
                    throw new RuntimeException(message, e);
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
                            // (re)Creating the FileBasedEventGenerator, which
                            // waits for
                            // new events
                            try {
                                createGenerator();
                            } catch (Throwable t) {
                                String message = "Error on FS-Monitor initialization: "
                                                 + t.getLocalizedMessage();
                                LOGGER.error(message, t);
                                throw new RuntimeException(message, t);
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
                    LOGGER.error("FlowManager cycle exception: " + e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void createGenerator() {
        final EventGeneratorConfiguration generatorConfig = getConfiguration()
            .getEventGeneratorConfiguration();
        if (generatorConfig==null){
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Unable to create a null event generator. Please configure one.");
            }
            return;
        }
        final String serviceID = generatorConfig.getServiceID();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("EventGeneratorCreationServiceID: " + serviceID);
        }

        final EventGeneratorService<FileSystemEvent, EventGeneratorConfiguration> generatorService = CatalogHolder
            .getCatalog().getResource(serviceID, EventGeneratorService.class);
        if (generatorService != null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("EventGeneratorService found");
            }
            eventGenerator = generatorService.createEventGenerator(generatorConfig);
            if (eventGenerator != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("FileSystemEventGenerator created");
                }
                eventGenerator.addListener(new GeneratorListener());
                eventGenerator.start();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("FileSystemEventGenerator started");
                }
            } else {
                final String message = "Error on EventGenerator creations";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(message);
                }
                throw new RuntimeException(message);
            }
        } else {
            final String message = "Unable to get the " + "generator service as resource from the catalog";
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

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("RESUMING ->" + this.getId());
        }

        if (!started) {
            executor.execute(this);
            this.started = true;
            this.paused = false;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("STARTED ->" + this.getId());
            }
        } else if (!isRunning()) {
            this.paused = false;
            this.notify();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("RESUMED ->" + this.getId());
            }
        }

    }

    /**
     * Implements the {@link Job#pause()} interface.
     * 
     * <P>
     * Pausing is implemented by stopping and removing the EventGenerator so
     * that no events are put into the mailbox.
     * 
     * @see it.geosolutions.geobatch.catalog.FlowManager#stop()
     */
    public synchronized boolean pause() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("PAUSING -> " + this.getId());
        }

        if (isRunning()) {
            this.paused = true;
            this.notify();
        }

        return true;
    }

    public synchronized boolean pause(boolean sub) {
        pause();
        if (sub) {
            final Set<String> keySet = eventConsumers.keySet();
            final Iterator<String> it = keySet.iterator();
            while (it.hasNext()) {
                final String key = it.next();
                final EventConsumer<FileSystemEvent, EventConsumerConfiguration> consumer = eventConsumers
                    .get(key);
                if (consumer != null) {
                    consumer.pause(true);
                } else {
                    eventConsumers.remove(key);
                }
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
    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory the workingDirectory to set
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
        setDescription(getConfiguration().getDescription());
    }

    @Override
    public synchronized boolean remove() throws IOException {
        return super.remove();
    }

    @Override
    public synchronized void persist() throws IOException {
        super.persist();
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    /**
     * returns an unmodifiable list of all the consumers
     * 
     * @return
     */
    public final Collection<EventConsumer> getEventConsumers() {
        try {
            eventConsumersLock.lock();
            return UnmodifiableCollection.decorate(eventConsumers.values());
        } finally {
            eventConsumersLock.unlock();
        }
    }

    /**
     * Returns an unmodifiable set of all the consumers id, if you want to add
     * or remove consumers, use the following methods:<br>
     * {@link FileBasedFlowManager#disposeConsumer(String)}<br>
     * {@link FileBasedFlowManager#addConsumer(EventConsumer)}<br>
     */
    public final Set<String> getEventConsumersId() {
        try {
            eventConsumersLock.lock();
            return UnmodifiableSet.decorate(eventConsumers.keySet());
        } finally {
            eventConsumersLock.unlock();
        }
    }

    @Override
    public final EventConsumer<FileSystemEvent, EventConsumerConfiguration> getConsumer(final String uuid) {

        try {
            eventConsumersLock.lock();
            return eventConsumers.get(uuid);
        } finally {
            eventConsumersLock.unlock();
        }
    }

    /**
     * 
     * Add consumers to this flow
     * 
     * @see #disposeConsumer(EventConsumer)
     * 
     * @param consumer the consumer to add
     * @return true if consumer is successfully added. If the
     *         {@link FileBasedFlowConfiguration#isKeepConsumers()} parameter is
     *         true, once
     *         {@link FileBasedFlowConfiguration#getMaxStoredConsumers()} is
     *         reached this method will return false until a consumer is
     *         manually removed.
     * @throws IllegalArgumentException if consumer is null
     */
    @Override
    public boolean addConsumer(final EventConsumer consumer) throws IllegalArgumentException {
        if (consumer == null)
            throw new IllegalArgumentException("Unable to add a null consumer");

        this.eventConsumersLock.lock();  // block until condition holds
        try {
        	if (eventConsumers.size() >= maxStoredConsumers) {
	            if (purgeConsumers(1) > 0) {
	                eventConsumers.put(consumer.getId(), consumer);
	            } else {
	                return false;
	            }
	        } else {
	            eventConsumers.put(consumer.getId(), consumer);
	        }
        } finally {
        	this.eventConsumersLock.unlock();
        }
        return true;
    }

    /**
     * 
     * @param uuid the uid of the consumer to check for status
     * @return the status of the selected consumer or null if consumer is not
     *         found
     */
    public EventConsumerStatus getStatus(final String uuid) {
        EventConsumer consumer=null;
        try {
            eventConsumersLock.lock();
            consumer = eventConsumers.get(uuid);
        } finally {
            eventConsumersLock.unlock();
        }
        if (consumer != null) {
            return consumer.getStatus();
        } else {
            return null;
        }
    }

    /**
     * Remove from the consumers map at least a 'quantity' of completed, failed
     * or canceled consumers. This method is thread-safe.<br>
     * If keep consumers is true Consumers may be removed manually and this
     * method will return always 0.
     * 
     * @see #disposeConsumer(EventConsumer)
     * @see #addConsumer(EventConsumer)
     * 
     * @return the number of purged of the
     * 
     */
    public int purgeConsumers(int quantity) {
        int size = 0;
        if (keepConsumers)
            return 0;
        
    	try {
    		this.eventConsumersLock.lock();
                final Set<String> keySet = eventConsumers.keySet();
                final Iterator<String> it = keySet.iterator();
                while (it.hasNext() && size < quantity) {
                    final String key = it.next();
                    final EventConsumer<FileSystemEvent, EventConsumerConfiguration> nextConsumer = eventConsumers
                        .get(key);
                    if (nextConsumer == null) {
                        it.remove();
                        ++size;
                        continue;
                    }
                    final EventConsumerStatus status = nextConsumer.getStatus();
                    if ((status == EventConsumerStatus.CANCELED) || (status == EventConsumerStatus.COMPLETED)
                        || (status == EventConsumerStatus.FAILED)) {
                        nextConsumer.dispose();
                        it.remove();
                        ++size;
                    }
                }
        } finally {
        	this.eventConsumersLock.unlock();
        }
        return size;
    }

    /**
     * Run the given consumer into the threadpool.
     * 
     * @param consumer The instance to be executed.
     * @throws IllegalStateException if the consumer is not in the EXECUTING
     *             state.
     * @throws IllegalArgumentException if the consumer is not in the
     *             {@link #eventConsumers} list of this FlowManager.
     */
    Future<Queue<FileSystemEvent>> execute(EventConsumer consumer) {
        if (consumer.getStatus() != EventConsumerStatus.EXECUTING) {
            final String message = "Consumer " + consumer + " is not in an EXECUTING state.";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message);
            }
            throw new IllegalStateException(message);
        }

        if (!eventConsumers.containsKey(consumer.getId())) {
            final String message = "Consumer " + consumer + " is not handled by the current flow manager.";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message);
            }
            throw new IllegalArgumentException(message);
        }
        try {
            return this.executor.submit(consumer);
        } catch (RejectedExecutionException r) {

            /*
             * +
             * "Will be rejected when the Executor has been shut down, and also "
             * +
             * "when the Executor uses finite bounds for both maximum threads and "
             * + "work queue capacity, and is saturated." +
             * " In either case, the execute method invokes the "
             */
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to submit the consumer (id:" + consumer.getId()
                                 + ") to the flow manager (id:" + this.getId() + ") queue.\nMessage is:"
                                 + r.getLocalizedMessage() + "\nThread pool executor info:"
                                 + "\nMaximum allowed number of threads: " + executor.getMaximumPoolSize()
                                 + "\nWorking Queue size: " + executor.getQueue().size()
                                 + "\nWorking Queue remaining capacity: "
                                 + executor.getQueue().remainingCapacity() + "\nCurrent number of threads: "
                                 + executor.getPoolSize()
                                 + "\nApproximate number of threads that are actively executing : "
                                 + executor.getActiveCount() + "\nCore number of threads: "
                                 + executor.getCorePoolSize() + "\nKeepAliveTime [secs]: "
                                 + executor.getKeepAliveTime(TimeUnit.SECONDS), r);
            }
            throw new RuntimeException(r);
        } catch (Throwable t) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to submit the consumer (id:" + consumer.getId()
                                 + ") to the flow manager (id:" + this.getId() + ") queue.\nMessage is:"
                                 + t.getLocalizedMessage(), t);
            }
            throw new RuntimeException(t);
        }
    }

    public void postEvent(FileSystemEvent event) {
        try {
            eventMailBox.put(event);
        } catch (NullPointerException npe) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to add a null event to the flow manager (id:" + this.getId()
                             + ") eventMailBox.\nMessage is:" + npe.getLocalizedMessage(), npe);
            }
            throw npe;
        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER
                    .error("Unable to add event [" + event.toString() + "] to the flow manager (id:"
                               + this.getId() + ") eventMailBox.\nMessage is:" + e.getLocalizedMessage(), e);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Will listen for the eventGenerator events, and put them in the blocking
     * mailbox.
     */
    private class GeneratorListener implements FlowEventListener<FileSystemEvent> {
        public void eventGenerated(FileSystemEvent event) {
            postEvent(event);
        }
    }
}
