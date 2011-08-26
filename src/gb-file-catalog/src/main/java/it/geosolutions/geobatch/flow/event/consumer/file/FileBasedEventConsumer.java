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
package it.geosolutions.geobatch.flow.event.consumer.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
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
import it.geosolutions.geobatch.tools.file.IOUtils;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class FileBasedEventConsumer 
    extends BaseEventConsumer<FileSystemEvent, FileBasedEventConsumerConfiguration> {

    /**
     * Common file prefix (unless the rule specify another one)
     */
    private String commonPrefixRegex;

    /**
     * The number of expected mandatory files before the flow can be started.
     * Should be set by configuration, and decremented each time a mandatory file
     * is consumed.
     */
    private long numInputFiles = 0;

    /**
     * Storing mandatory rules and the times they will occur.
     */
    private final List<FileEventRule> mandatoryRules = new ArrayList<FileEventRule>();

    /**
     * Storing optional rules and the times they will occur.
     */
    private final List<FileEventRule> optionalRules = new ArrayList<FileEventRule>();

    
    private File workingDir;

    
    private FileBasedEventConsumerConfiguration configuration;

    
    private volatile boolean canceled;

    /**
     * Default logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(FileBasedEventConsumer.class.toString());

    // ----------------------------------------------- PUBLIC CONSTRUCTORS
    public FileBasedEventConsumer(FileBasedEventConsumerConfiguration configuration)
            throws InterruptedException, IOException {

        super(configuration.getId(), configuration.getName(), configuration.getDescription());

        final File catalogFile = ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory();

        final File workingDir = Path.findLocation(configuration.getWorkingDirectory(), catalogFile);

        if (workingDir == null){
            throw new IllegalArgumentException("Invalid configuring directory");
        }

        if (workingDir.exists() && workingDir.isDirectory() & workingDir.canRead()) {
            initialize(configuration, workingDir);
            return;
        }

    }

    // -------------------------------------------------------------------------
    /**
     * This method allows the BaseDispatcher to check if an Event can be processed by the current
     * BaseEventConsumer.
     * 
     * @return boolean true if the event can be accepted, i.e. this BaseEventConsumer was waiting
     *         for it.
     * @throws InterruptedException
     */
    private boolean canConsume(FileSystemEvent event) {
        final String path = event.getSource().getAbsolutePath();
        final String fileName = FilenameUtils.getName(path);
        final String filePrefix = FilenameUtils.getBaseName(fileName);
        // final String fullPath = FilenameUtils.getFullPath(path);

        // check mandatory rules
        boolean res = this.checkRuleConsistency(event.getEventType(), filePrefix, fileName, true);

        // check optional rules if needed
        if (!res) {
            res = this.checkRuleConsistency(event.getEventType(), filePrefix, fileName, false);
        }

        /*
         * COMMENTED OUT 19 Jan 2011 Carlo Cancellieri This is a not needed limitation to the fluxes
         */
        // res &= this.checkSamePath(fullPath);

        return res;
    }

    // ----------------------------------------------------------------------------
    /**
     * Helper method to check for mandatory rules consistency.
     * 
     * @param fileName
     * 
     * @return boolean
     * 
     */
    private boolean checkRuleConsistency(final FileSystemEventType eventType, final String prefix,
            final String fileName, final boolean mandatory) {

        int occurrencies;

        final List<FileEventRule> rules = (mandatory ? this.mandatoryRules : this.optionalRules);
        if(rules==null||rules.isEmpty()) {
        	return true;
        }
        for (FileEventRule rule : rules) {

            // check event type
            final List<FileSystemEventType> eventTypes = rule.getAcceptableNotifications();
            if (!checkEvent(eventType, eventTypes)) {
                return false;
            }

            // check occurrences for this file in case we have multiple
            // Occurrences
            occurrencies = rule.getActualOccurrencies();
            final int originalOccurrences = rule.getOriginalOccurrencies();
            final Pattern p = Pattern.compile(rule.getRegex());
            if (p.matcher(fileName).matches()) {
                // we cannot exceed the number of needed occurrences!
                if (occurrencies > originalOccurrences) {
                    return false;
                }

                if (this.commonPrefixRegex == null) {
                    this.commonPrefixRegex = prefix;
                    rule.setActualOccurrencies(occurrencies + 1);
                    if (mandatory) {
                        this.numInputFiles--; // WARNING!!!! the numinputfiles should be decreased only when the event is actually consumed, not when checked (TODO)
                    }
                    return true;
                } else if (prefix.startsWith(this.commonPrefixRegex)) {
                    rule.setActualOccurrencies(occurrencies + 1);
                    if (mandatory) {
                        this.numInputFiles--; // WARNING!!!! the numinputfiles should be decreased only when the event is actually consumed, not when checked (TODO)
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkEvent(FileSystemEventType eventType,
            List<FileSystemEventType> eventTypes) {
        if (eventTypes == null) {
            return true;
        }
        for (FileSystemEventType notification : eventTypes) {
            if (notification.equals(eventType)) {
                return true;
            }

        }
        return false;
    }

    /**
     * FileBasedEventConsumer initialization.
     * 
     * @throws InterruptedException
     */
    private void initialize(FileBasedEventConsumerConfiguration configuration, File workingDir)
            throws InterruptedException {
        this.configuration = configuration;
        this.workingDir = workingDir;
        this.commonPrefixRegex = null;
        this.mandatoryRules.clear();
        this.optionalRules.clear();
        this.canceled = false;

        // set the same name of the configuration
        setName(configuration.getName());

        // ////////////////////////////////////////////////////////////////////
        // LISTENER
        // ////////////////////////////////////////////////////////////////////

        for (ProgressListenerConfiguration plConfig : configuration.getListenerConfigurations()) {
            final String serviceID = plConfig.getServiceID();
            final ProgressListenerService progressListenerService = CatalogHolder.getCatalog()
                    .getResource(serviceID, ProgressListenerService.class);
            if (progressListenerService != null) {
                ProgressListener progressListener = progressListenerService.createProgressListener(
                        plConfig, this);
                getListenerForwarder().addListener(progressListener);
            } else {
                throw new IllegalArgumentException("Could not find '" + serviceID
                        + "' listener, declared in " + configuration.getId() + " configuration");
            }
        }

        // ////////////////////////////////////////////////////////////////////
        // RULES
        // ////////////////////////////////////////////////////////////////////

        numInputFiles = 0;
        if(configuration.getRules()!=null){
            for (FileEventRule rule : configuration.getRules()) {
                if (!rule.isOptional()) {
                    this.mandatoryRules.add(rule);
                    numInputFiles += rule.getOriginalOccurrencies();
                } else {
                    this.optionalRules.add(rule);
                }
            }
        }


        // ////////////////////////////////////////////////////////////////////
        // ACTIONS
        // ////////////////////////////////////////////////////////////////////

        final List<BaseAction<FileSystemEvent>> loadedActions = new ArrayList<BaseAction<FileSystemEvent>>();
        for (ActionConfiguration actionConfig : configuration.getActions()) {
            final String actionServiceID = actionConfig.getServiceID();
            final ActionService<FileSystemEvent, ActionConfiguration> actionService = CatalogHolder.getCatalog().getResource(actionServiceID, ActionService.class);
            if (actionService != null) {
                Action<FileSystemEvent> action = null;
                if (actionService.canCreateAction(actionConfig)) {
                    action = actionService.createAction(actionConfig);
                    if (action == null) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Unable to load the action using the service "
                                    + actionServiceID);
                        }
                        throw new IllegalArgumentException(
                                "Action could not be instantiated for config "
                                        + actionConfig);
                    }
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Cannot create the action using the service "
                                + actionServiceID + " check the configuration.");
                    }
                    throw new IllegalArgumentException("Action could not be created for config "
                                    + actionConfig);
                }

                // attach listeners to actions
                for (ProgressListenerConfiguration plConfig : actionConfig
                        .getListenerConfigurations()) {
                    final String listenerServiceID = plConfig.getServiceID();
                    final ProgressListenerService progressListenerService = CatalogHolder
                            .getCatalog().getResource(listenerServiceID,
                                    ProgressListenerService.class);
                    if (progressListenerService != null) {
                        ProgressListener progressListener = progressListenerService
                                .createProgressListener(plConfig, action);
                        action.addListener(progressListener);
                    } else {
                        final String message="Could not find '" + listenerServiceID
                        + "' listener," + " declared in " + actionConfig.getId()
                        + " action configuration," + " in " + configuration.getId()
                        + " consumer";
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error(message);
                        }
                        throw new IllegalArgumentException(message);
                    }
                }

                loadedActions.add((BaseAction<FileSystemEvent>)action);
            } else {
                final String message="ActionService not found '" + actionServiceID
                + "' for ActionConfig '" + actionConfig.getName() + "'";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(message);
                }
                throw new IllegalArgumentException(message);
            }
        }
        super.addActions(loadedActions);

        if (loadedActions.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(getClass().getSimpleName() + " initialized with "
                        + mandatoryRules.size() + " mandatory rules, " + optionalRules.size()
                        + " optional rules, " + loadedActions.size() + " actions");
            }
        }
    }

    /***************************************************************************
     * Main Thread cycle.
     * 
     * <LI>Create needed dirs</LI> 
     * <LI>Optionally backup files</LI> 
     * <LI>Move files into a job-specific working dir</LI> 
     * <LI>Run the actions</LI>
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
            // if we work on the input directory, we do not move around anything, unless we want to
            // perform
            // a backup

            // Dateformat for creating working dirs.
            final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSSz");
            TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
            dateFormatter.setTimeZone(TZ_UTC);
            final String timeStamp = dateFormatter.format(new Date());

            // current directory inside working dir, specifically created for this execution.
            // Creation is deferred until first usage
            final File currentRunDirectory = new File(this.workingDir, timeStamp);
            if (configuration.isPerformBackup() || !configuration.isPreserveInput())
                if (!currentRunDirectory.exists() && !currentRunDirectory.mkdirs())
                    throw new IllegalStateException("Could not create consumer backup directory!");
            
			// set the consumer running context
			setRunningContext(currentRunDirectory.getAbsolutePath());

            // create backup dir. Creation is deferred until first usage
            getListenerForwarder().progressing(20, "Creating backup dir");
            final File backupDirectory = new File(currentRunDirectory, "backup");
            if (configuration.isPerformBackup())
                if (!backupDirectory.exists() && !backupDirectory.mkdirs())
                    throw new IllegalStateException("Could not create consumer backup directory!");

            //
            // Cycling on all the input events
            //
            Queue<FileSystemEvent> fileEventList = new LinkedList<FileSystemEvent>();
            int numProcessedFiles = 0;
            for (FileSystemEvent event : this.eventsQueue) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("[" + Thread.currentThread().getName()
                            + "]: new element retrieved from the MailBox.");

                // get info for the input file event
                final File sourceDataFile = event.getSource();
                final String fileBareName;
                if (sourceDataFile!=null && sourceDataFile.exists()){
                	fileBareName = FilenameUtils.getName(sourceDataFile.toString());
                	getListenerForwarder().progressing(
                        30 + (10f / this.eventsQueue.size() * numProcessedFiles++),
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
	                        getListenerForwarder().progressing(
	                                30 + (10f / this.eventsQueue.size() * numProcessedFiles++),
	                                "Creating backup files");
	
	                        // In case we do not work on the input as is, we move it to our
	                        // current working directory
	                        final File destDataFile = new File(backupDirectory, fileBareName);
	                        if (sourceDataFile.isDirectory())
	                            FileUtils.copyDirectory(sourceDataFile, destDataFile);
	                        else
	                            FileUtils.copyFile(sourceDataFile, destDataFile);
	                    }
	
	                    //
	                    // Working on input events directly without moving to working dir?
	                    //
	                    if (!configuration.isPreserveInput()) {
	
	                        // In case we do not work on the input as is, we move it to our
	                        // current working directory
	                        final File destDataFile = new File(currentRunDirectory, fileBareName);
	                        if (sourceDataFile.isDirectory())
	                            FileUtils.moveDirectory(sourceDataFile, destDataFile);
	                        else
	                            FileUtils.moveFile(sourceDataFile, destDataFile);
	
	                        // adjust event sources since we moved the files locally
	                        fileEventList
	                                .offer(new FileSystemEvent(destDataFile, event.getEventType()));
	                    } else {
	                        // we are going to work directly on the input files
	                        fileEventList.offer(event);
	
	                    }
	                    if (LOGGER.isInfoEnabled()){
	                        LOGGER.info("[" + Thread.currentThread().getName()
	                            + "]: accepted file " + sourceDataFile);
	                    }
	                } else {
	                    if (LOGGER.isErrorEnabled()){
	                        LOGGER.error(new StringBuilder("[")
	                                .append(Thread.currentThread().getName())
	                                .append("]: could not lock file ").append(sourceDataFile).toString());
	                    }
	
	                    /*
	                     * TODO: lock not acquired: what else?
	                     */
	                }
	                
                } // event.getSource()!=null && sourceDataFile.exists()
                else {
                	/*
                	 * event.getSource()==null || !sourceDataFile.exists()
                     * this could be an empty file representing a POLLING event
                     */
                    fileEventList.offer(event);
                }

            }

            // //
            // TODO if no further processing is necessary or can be
            // done due to some error, set eventConsumerStatus to Finished or
            // Failure. (etj: ???)
            // //
            if (LOGGER.isInfoEnabled()){
                LOGGER.info("[" + Thread.currentThread().getName()
                    + "]: new element processed.");
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
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("FileBasedEventConsumer " + Thread.currentThread().getName()
                                + " Error during " + e.getAction().getClass().getSimpleName()
                                + " execution: " + e.getLocalizedMessage(), e);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("FileBasedEventConsumer "
                        + Thread.currentThread().getName() + " could not move file "
                        + " due to the following IO error: " + e.getLocalizedMessage(), e);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("FileBasedEventConsumer "
                        + Thread.currentThread().getName() + " could not move file "
                        + " due to an InterruptedException: " + e.getLocalizedMessage(), e);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch (RuntimeException e) {
            exceptionOccurred = e;
            throw e;

        } finally {
            getListenerForwarder().progressing(100, "Running actions");
            if (LOGGER.isInfoEnabled())
                LOGGER.info(Thread.currentThread().getName() + " DONE!");
            this.dispose();

            if (jobResultSuccessful && exceptionOccurred == null)
                getListenerForwarder().completed();
            else
                getListenerForwarder().failed(exceptionOccurred);
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
     * @return the workingDirectory
     */
    public File getWorkingDir() {
        return workingDir;
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
        LOGGER.info(Thread.currentThread().getName() + " DISPOSING!");

        super.dispose();
        this.numInputFiles = 0;
        this.configuration = null;
        this.commonPrefixRegex = null;
        this.mandatoryRules.clear();
        this.optionalRules.clear();
        
    }

    /**
     * 
     * remove all Cumulating progress listener from the Consumer and containing action(s)
     * remove all the actions from the action list
     * 
     */
    public void clear(){

        // Progress Logging...
   		final ProgressListenerForwarder lf= this.getListenerForwarder();
   		final List <? extends IProgressListener> listeners=lf.getListeners();
   		if (listeners!=null){
   			for (IProgressListener listener : listeners){
   				
   				if (listener instanceof CumulatingProgressListener){
   					((CumulatingProgressListener)listener).clearMessages();
   				}
   			}
   		}
   		
   		// Current Action Status...
   		if (actions != null) {
   			for (Action action : this.actions) {
   				
   				if (action instanceof BaseAction<?>){
   					final BaseAction<?> baseAction=(BaseAction)action;
   					// try the most interesting information holder
   					final CumulatingProgressListener cpl= (CumulatingProgressListener) baseAction
   							.getProgressListener(CumulatingProgressListener.class);
   					if (cpl != null)
   						cpl.clearMessages();

   				}
   			}
   			this.actions.clear();
   		}
    }

    @Override
    public boolean consume(FileSystemEvent event) {
        if (getStatus() != EventConsumerStatus.IDLE && getStatus() != EventConsumerStatus.WAITING) {
            return false;
        }
        if (!canConsume(event)) {
            return false;
        }
        super.consume(event);

        // start execution
        if (numInputFiles == 0) {
            setStatus(EventConsumerStatus.EXECUTING);
        }

        // move to waiting
        if (getStatus() == EventConsumerStatus.IDLE) {
            setStatus(EventConsumerStatus.WAITING);
        }

        return true;
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

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "["
                + "name:" + getName()
                + " status:"+ getStatus()
                + " actions:"+ actions.size()
                + " events:" + eventsQueue.size()
                + " still missing:"+ numInputFiles
                + (isPaused() ? " PAUSED" : "")
                + (eventsQueue.isEmpty() ? "" : (" first event:" + eventsQueue.peek().getSource()
                        .getName())) + "]";
    }
}
