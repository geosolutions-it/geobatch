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
package it.geosolutions.geobatch.flow.event.consumer.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.listeners.cumulator.CumulatingProgressListener;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.tools.io.file.IOUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * @author Emanuele Tajariol, GeoSolutions S.A.S.
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class FileBasedEventConsumer 
    extends BaseEventConsumer<FileSystemEvent, FileBasedEventConsumerConfiguration> {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedEventConsumer.class);

    /**
     * The number of expected mandatory files before the flow can be started.
     * Should be set by configuration, and decremented each time a mandatory
     * file is consumed.
     */
    private long numInputFiles = 0;

    /**
     * Temporary dir for this flow instance.<br>
     * It represents the parent dir of the {@link #flowInstanceTempDir}<br>
     */
    private File flowBaseTempDir;
    private File flowConfigDir;

    /**
     * Temporary folder for a given flow instance.
     * It's created using
     * {@link FileBasedEventConsumer#createFlowInstanceTempDir(File)}
     */
    private File flowInstanceTempDir;

    private FileBasedEventConsumerConfiguration configuration;

    private volatile boolean canceled;

    /**
     * do not remove runtimeDir when consumer is disposed
     */
    private boolean keepTempDir = false;


    /**
     * PUBLIC CONSTRUCTORS: Initialize the consumer using the passed
     * configuration.<br>
     * Note that the id is initialized using UUID.randomUUID()<br>
     * It also try to create a {@link FileBasedEventConsumer#runtimeDir} into
     * the {@link FileBasedEventConsumer#getFlowInstanceTempDir()}
     * 
     * @param configuration
     * @throws InterruptedException
     * @throws IOException
     */

    public FileBasedEventConsumer(FileBasedEventConsumerConfiguration configuration, File flowConfigDir, File flowBaseTempDir)
        throws InterruptedException, IOException {
        super(UUID.randomUUID().toString());
       
        this.flowConfigDir = flowConfigDir;
        
        this.flowInstanceTempDir = createFlowInstanceTempDir(flowBaseTempDir);
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Prepared flowInstanceTempDir " + flowInstanceTempDir);
               
        initialize(configuration);
    }



    /**
     * Called by ctor
     */
    private static File createFlowInstanceTempDir(File flowBaseTempDir) {
        // current directory inside the flow temp dir, specifically created for this execution/thread.
        // Creation is eager
        final File instanceTempDir = new File(flowBaseTempDir, UniqueTimeStampProvider.getTimeStamp());
        instanceTempDir.mkdirs();

        return instanceTempDir;
    }

    /**
     * 
     * FileBasedEventConsumer initialization.
     * 
     * @param configuration
     * @param workingDir
     * @throws InterruptedException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private void initialize(FileBasedEventConsumerConfiguration configuration)
        throws InterruptedException, IllegalArgumentException, IOException {

        this.configuration = configuration;
        this.keepTempDir = configuration.isKeepTempDir();
        this.canceled = false;

        // ////////////////////////////////////////////////////////////////////
        // LISTENER
        // ////////////////////////////////////////////////////////////////////

        for (ProgressListenerConfiguration plConfig : configuration.getListenerConfigurations()) {
            final String serviceID = plConfig.getServiceID();
            final ProgressListenerService progressListenerService =
                    CatalogHolder.getCatalog().getResource(serviceID, ProgressListenerService.class);
            if (progressListenerService != null) {
                ProgressListener progressListener = progressListenerService.createProgressListener(plConfig, this);
                getListenerForwarder().addListener(progressListener);
            } else {
                throw new IllegalArgumentException("Could not find '" + serviceID
                                                   + "' listener, declared in " + configuration.getId()
                                                   + " configuration");
            }
        }

        // ////////////////////////////////////////////////////////////////////
        // ACTIONS
        // ////////////////////////////////////////////////////////////////////

        final List<BaseAction<FileSystemEvent>> loadedActions = new ArrayList<BaseAction<FileSystemEvent>>();

        final Catalog catalog = CatalogHolder.getCatalog();

        for (ActionConfiguration actionConfig : configuration.getActions()) {
            final String actionServiceID = actionConfig.getServiceID();
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Loading actionService " + actionServiceID 
                        + " from " + actionConfig.getClass().getSimpleName()
                        + " " + actionConfig.getId()+":"+actionConfig.getName());
            final ActionService<FileSystemEvent, ActionConfiguration> actionService = catalog.getResource(actionServiceID, ActionService.class);
            if (actionService != null) {
                Action<FileSystemEvent> action = null;
                if (actionService.canCreateAction(actionConfig)) {
                    action = actionService.createAction(actionConfig);
                    if (action == null) {
                        throw new IllegalArgumentException("Action could not be instantiated for config "
                                                           + actionConfig);
                    }
                } else {
                    throw new IllegalArgumentException("Cannot create the action using the service "
                                                       + actionServiceID + " check the configuration.");
                }

                // add default status listener (Used by the GUI to track action
                // stat)
                // TODO

                // attach listeners to actions
                for (ProgressListenerConfiguration plConfig : actionConfig.getListenerConfigurations()) {
                    final String listenerServiceID = plConfig.getServiceID();
                    final ProgressListenerService progressListenerService = CatalogHolder.getCatalog()
                        .getResource(listenerServiceID, ProgressListenerService.class);
                    if (progressListenerService != null) {
                        ProgressListener progressListener = progressListenerService
                            .createProgressListener(plConfig, action);
                        action.addListener(progressListener);
                    } else {
                        throw new IllegalArgumentException("Could not find '" + listenerServiceID
                                                           + "' listener," + " declared in "
                                                           + actionConfig.getId() + " action configuration,"
                                                           + " in " + configuration.getId() + " consumer");
                    }
                }

                loadedActions.add((BaseAction<FileSystemEvent>)action);
            } else {
                throw new IllegalArgumentException("ActionService not found '" + actionServiceID
                                                   + "' for ActionConfig '" + actionConfig.getName() + "'");
            }
        }
        super.addActions(loadedActions);

        if (loadedActions.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(getClass().getSimpleName() + " initialized with " + loadedActions.size()
                            + " actions");
            }
        }
    }

    /***************************************************************************
     * Main Thread cycle.
     * 
     * <LI>Create needed dirs</LI> <LI>Optionally backup files</LI> <LI>Move
     * files into a job-specific working dir</LI> <LI>Run the actions</LI>
     */
    public Queue<FileSystemEvent> call() throws Exception {
        this.canceled = false;

        boolean jobResultSuccessful = false;
        Throwable exceptionOccurred = null;

        getListenerForwarder().setTask("Configuring");
        getListenerForwarder().started();

        try {

            // create live working dir
            getListenerForwarder().progressing(10, "Managing events");

            //
            // Management of current working directory
            //
            // if we work on the input directory, we do not move around
            // anything, unless we want to
            // perform a backup

            if (configuration.isPerformBackup() || !configuration.isPreserveInput()) {
                if ( ! flowInstanceTempDir.exists() && ! flowInstanceTempDir.mkdirs()) {
                    throw new IllegalStateException("Could not create consumer backup directory!");
                }
            }
            // set the consumer running context
            // don't know how this running context will be used in a FileBased* hiererchy, anyway let's force the use of proper methods.
            setRunningContext("DONT_USE_AS_FILEPATH_" + flowInstanceTempDir.getAbsolutePath());

            // create backup dir. Creation is deferred until first usage
            getListenerForwarder().progressing(20, "Creating backup dir");

            final File backupDirectory = new File(flowInstanceTempDir, "backup");
            if (configuration.isPerformBackup()) {
                if (!backupDirectory.exists() && !backupDirectory.mkdirs()) {
                    throw new IllegalStateException("Could not create consumer backup directory!");
                }
            }

            //
            // Cycling on all the input events
            //
            Queue<FileSystemEvent> fileEventList = new LinkedList<FileSystemEvent>();
            int numProcessedFiles = 0;
            for (FileSystemEvent event : this.eventsQueue) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("[" + Thread.currentThread().getName()
                                + "]: new element retrieved from the MailBox.");
                }

                // get info for the input file event
                final File sourceDataFile = event.getSource();
                final String fileBareName;
                if ((sourceDataFile != null) && sourceDataFile.exists()) {
                    fileBareName = FilenameUtils.getName(sourceDataFile.toString());
                    getListenerForwarder()
                        .progressing(30 + (10f / this.eventsQueue.size() * numProcessedFiles++),
                                     "Preprocessing event " + fileBareName);
                    //
                    // copy input file/dir to current working directory
                    //
                    if (IOUtils.acquireLock(this, sourceDataFile)) {

                        //
                        // Backing up inputs?
                        //
                        if (this.configuration.isPerformBackup()) {

                            // Backing up files and delete sources.
                            getListenerForwarder()
                                .progressing(30 + (10f / this.eventsQueue.size() * numProcessedFiles++),
                                             "Creating backup files");

                            // In case we do not work on the input as is, we
                            // move it to our
                            // current working directory
                            final File destDataFile = new File(backupDirectory, fileBareName);
                            if (sourceDataFile.isDirectory()) {
                                FileUtils.copyDirectory(sourceDataFile, destDataFile);
                            } else {
                                FileUtils.copyFile(sourceDataFile, destDataFile);
                            }
                        }

                        //
                        // Working on input events directly without moving to
                        // working dir?
                        //
                        if (!configuration.isPreserveInput()) {

                            // In case we do not work on the input as is, we
                            // move it to our current working directory
                            final File destDataFile = new File(flowInstanceTempDir, fileBareName);
                            if (sourceDataFile.isDirectory()) {
                                FileUtils.moveDirectory(sourceDataFile, destDataFile);
                            } else {
                                FileUtils.moveFile(sourceDataFile, destDataFile);
                            }

                            // adjust event sources since we moved the files
                            // locally
                            fileEventList.offer(new FileSystemEvent(destDataFile, event.getEventType()));
                        } else {
                            // we are going to work directly on the input files
                            fileEventList.offer(event);

                        }
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("[" + Thread.currentThread().getName() + "]: accepted file "
                                        + sourceDataFile);
                        }
                    } else {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error(new StringBuilder("[").append(Thread.currentThread().getName())
                                .append("]: could not lock file ").append(sourceDataFile).toString());
                        }

                        /*
                         * TODO: lock not acquired: what else?
                         */
                    }

                } // event.getSource()!=null && sourceDataFile.exists()
                else {

                    /*
                     * event.getSource()==null || !sourceDataFile.exists() this
                     * could be an empty file representing a POLLING event
                     */
                    fileEventList.offer(event);
                }

            }

            // //
            // TODO if no further processing is necessary or can be
            // done due to some error, set eventConsumerStatus to Finished or
            // Failure. (etj: ???)
            // //
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[" + Thread.currentThread().getName() + "]: new element processed.");
            }

            // // Finally, run the Actions on the files
            getListenerForwarder().progressing(50, "Running actions");

            try {
                // apply actions into the actual context (currentRunDirectory)
                fileEventList = this.applyActions(fileEventList);
                this.setStatus(EventConsumerStatus.COMPLETED);
                jobResultSuccessful = true;
            } catch (ActionException ae) {
                this.setStatus(EventConsumerStatus.FAILED);
                throw ae;
            }

            return fileEventList;
        } catch (ActionException e) {
            String msg = "[" + Thread.currentThread().getName() + "] Error during "
                             + e.getType().getSimpleName() + " execution: " + e.getLocalizedMessage();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(msg, e);
            } else {
                LOGGER.error(msg);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[" + Thread.currentThread().getName()
                                 + "] could not move file " + " due to the following IO error: "
                                 + e.getLocalizedMessage(), e);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[" + Thread.currentThread().getName()
                                 + "] could not move file " + " due to an InterruptedException: "
                                 + e.getLocalizedMessage(), e);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch (RuntimeException e) {
            exceptionOccurred = e;
            throw e;

        } finally {
            getListenerForwarder().progressing(100, "Running actions");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(Thread.currentThread().getName() + " DONE!");
            }
            // this.dispose();

            if (jobResultSuccessful && (exceptionOccurred == null)) {
                getListenerForwarder().completed();
            } else {
                getListenerForwarder().failed(exceptionOccurred);
            }
        }

        return null;
    }

    /**
     * @param configuration
     */
    public void setConfiguration(FileBasedEventConsumerConfiguration configuration) {
        this.configuration = configuration;

    }

    /**
     * @return
     */
    public FileBasedEventConsumerConfiguration getConfiguration() {
        return configuration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.manager.Manager#dispose()
     */
    public void dispose() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(Thread.currentThread().getName() + " DISPOSING!");
        }

        clear();

        super.dispose();
        this.numInputFiles = 0;
        this.configuration = null;
    }

    /**
     * remove all Cumulating progress listener from the Consumer and containing
     * action(s) remove all the actions from the action list remove
     * contextRunningDir
     */
    private void clear() {
        // Progress Logging...
        // remove all Cumulating progress listener from the Consumer and
        // containing action(s)
        final ProgressListenerForwarder lf = this.getListenerForwarder();
        final Collection<? extends IProgressListener> listeners = lf.getListeners();
        if (listeners != null) {
            for (IProgressListener listener : listeners) {

                if (listener instanceof CumulatingProgressListener) {
                    ((CumulatingProgressListener)listener).clearMessages();
                }
            }
        }

        // Current Action Status...
        // remove all the actions from the action list
        if (actions != null) {
            for (Action action : this.actions) {

                if (action instanceof BaseAction<?>) {
                    final BaseAction<?> baseAction = (BaseAction)action;
                    // try the most interesting information holder
                    Collection<IProgressListener> coll = baseAction
                        .getListeners(CumulatingProgressListener.class);
                    for (IProgressListener cpl : coll) {
                        if (cpl != null && cpl instanceof CumulatingProgressListener) {
                            ((CumulatingProgressListener)cpl).clearMessages();
                        }
                    }

                }
            }
            this.actions.clear();
        }

        // remove contextRunningDir
        if (!keepTempDir) {
            // removing running context directory
            try {
                FileUtils.deleteDirectory(getFlowInstanceTempDir());
            } catch (IOException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Problem trying to remove the running context directory: "
                                + getFlowInstanceTempDir() + ".\n " + e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public boolean consume(FileSystemEvent event) {
        if ((getStatus() != EventConsumerStatus.IDLE) && (getStatus() != EventConsumerStatus.WAITING)) {
            return false;
        }
        if (super.consume(event)) {

            // start execution
            if (numInputFiles == 0) {
                setStatus(EventConsumerStatus.EXECUTING);
            }

            // move to waiting
            if (getStatus() == EventConsumerStatus.IDLE) {
                setStatus(EventConsumerStatus.WAITING);
            }

            return true;
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Action execution is rejected. Probably execution queue is full.");
            }
            setStatus(EventConsumerStatus.CANCELED);
            return false;
        }

    }

    public void cancel() {
        this.canceled = true;
    }

    /**
     * @return
     */
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    protected void setStatus(EventConsumerStatus eventConsumerStatus) {
        super.setStatus(eventConsumerStatus);
        // // are we executing? If yes, let's trigger a thread!
        // if (eventConsumerStatus == EventConsumerStatus.EXECUTING)
        // getCatalog().getExecutor().execute(this);
    }

    /**
     * Create a temp dir for an action in a flow.<br/>
     */
    @Override
    protected void setupAction(BaseAction action, int step) throws IllegalStateException {
        // random id
        action.setId(UUID.randomUUID().toString());
        
        // tempDir
        String actionTempDirName = step + "_" + action.getName();
        File actionTempDir = new File(flowInstanceTempDir, actionTempDirName);
        if (!actionTempDir.mkdirs()) {
            throw new IllegalStateException("Unable to create the action temporary dir: " + actionTempDir);
        }
        action.setTempDir(actionTempDir);
        
        File actionConfigDir = initConfigDir(action.getConfiguration(), flowConfigDir);
        action.setConfigDir(actionConfigDir);
    }

    private File initConfigDir(ActionConfiguration actionCfg, File flowConfigDir) {
        File ret = null;
        File ovr = actionCfg.getOverrideConfigDir(); 
        if( ovr != null) {
            if( ! ovr.isAbsolute())
                ret = new File(flowConfigDir, ovr.getPath());
            else 
                ret = ovr;
        } else
            ret = new File(flowConfigDir, actionCfg.getId());
        
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Action config dir set to " + ret);
        return ret;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + " status:" + getStatus() + " actions:" + actions.size()
               + " tempDir:" + getFlowInstanceTempDir() + " events:" + eventsQueue.size()
               + (isPaused() ? " PAUSED" : "")
               + (eventsQueue.isEmpty() ? "" : (" first event:" + eventsQueue.peek().getSource().getName()))
               + "]";
    }

    /**
     * Temporary folder used in the flow instance.
     */
    public final File getFlowInstanceTempDir() {
        return flowInstanceTempDir;
    }

    /**
     * @return the keepTempDir
     */
    public final boolean isKeepTempDir() {
        return keepTempDir;
    }

    /**
     * @param keepTempDir if true the runtime dir is not removed
     */
    public final void setKeepTempDir(boolean keepTempDir) {
        this.keepTempDir = keepTempDir;
    }

    /**
     * Used to create a unique timestamp useful for creating unique temp dir names.  
     */
    static class UniqueTimeStampProvider {
        // Dateformat for creating flow instance temp dirs.
        final static SimpleDateFormat DATEFORMATTER = new SimpleDateFormat("yyyyMMdd'-'HHmmss-SSS");
        static{
            TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
            DATEFORMATTER.setTimeZone(TZ_UTC);
        }
        
        static long lastDate = new Date().getTime(); // arbitrary init
        
        synchronized static public String getTimeStamp() {
            Date now;
            do {
                now = new Date();    
            } while (lastDate == now.getTime());
            lastDate = now.getTime();
                        
            final String timeStamp = DATEFORMATTER.format(now);
            return timeStamp;
        }
        
    }
}
