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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import com.thoughtworks.xstream.XStream.InitializationException;

/**
 * Comments here ...
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * 
 */
public class FileBasedEventConsumer
        extends BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration>
        implements EventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration> {

    /**
     * Common file prefix (unless the rule specify another one)
     */
    private String commonPrefixRegex;
    /**
     * Stream Transfer control
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
    /**
     *
     */
    private File workingDir;
    private FileBasedEventConsumerConfiguration configuration;
    private volatile boolean canceled;
    /** Dateformat for creating working dirs. */
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSSz");

    static {
        TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
        DATEFORMAT.setTimeZone(TZ_UTC);
    }
    /**
     * Default logger
     */
    private final static Logger LOGGER = Logger.getLogger(FileBasedEventConsumer.class.toString());

    // ----------------------------------------------- PUBLIC CONSTRUCTORS
    public FileBasedEventConsumer(FileBasedEventConsumerConfiguration configuration)
            throws InterruptedException, IOException {

        final File catalogFile = new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory());
        final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(), catalogFile);

        if (workingDir == null)
            throw new IllegalArgumentException("Invalid configuring directory");

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
    private boolean canConsume(FileSystemMonitorEvent event) {
        final String path = event.getSource().getAbsolutePath();
        final String fileName = FilenameUtils.getName(path);
        final String filePrefix = FilenameUtils.getBaseName(fileName);
        final String fullPath = FilenameUtils.getFullPath(path);

        //check mandatory rules
        boolean res = this.checkRuleConsistency(event.getNotification(), filePrefix, fileName, true);

        //check optinal rules if needed
        if (!res) {
            res = this.checkRuleConsistency(event.getNotification(), filePrefix, fileName, false);
        }

        res &= this.checkSamePath(fullPath);

        return res;
    }

    /**
     * Check if all queued events refer to files in the same dir.
     * CHECKME: is it really needed? will it not break flows that need scattered files?
     */
    private boolean checkSamePath(String fullpath) {
        for (FileSystemMonitorEvent event : eventsQueue) {
            String existingFP = FilenameUtils.getFullPath(event.getSource().getAbsolutePath());
            if( ! fullpath.equals(existingFP)) {
                return false;
            }
        }
        return true;
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
    private boolean checkRuleConsistency(final FileSystemMonitorNotifications eventType,
            final String prefix, final String fileName, final boolean mandatory) {

        int occurrencies;

        final List<FileEventRule> rules = (mandatory ? this.mandatoryRules : this.optionalRules);
        for (FileEventRule rule : rules) {

            // check event type
            final List<FileSystemMonitorNotifications> eventTypes = rule.getAcceptableNotifications();
            if (!checkEvent(eventType, eventTypes)) {
                return false;
            }

            //check occurencies for this file in case we have multiple occurrencies
            occurrencies = rule.getActualOccurrencies();
            final int originalOccurrences = rule.getOriginalOccurrencies();
            final Pattern p = Pattern.compile(rule.getRegex());
            if (p.matcher(fileName).matches()) {
                //we cannot exceed the number of needed occurrences!
                if (occurrencies > originalOccurrences) {
                    return false;
                }

                if (this.commonPrefixRegex == null) {
                    this.commonPrefixRegex = prefix;
                    rule.setActualOccurrencies(occurrencies + 1);
                    if (mandatory) {
                        this.numInputFiles--;
                    }
                    return true;
                } else if (prefix.startsWith(this.commonPrefixRegex)) {
                    rule.setActualOccurrencies(occurrencies + 1);
                    if (mandatory) {
                        this.numInputFiles--;
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean checkEvent(FileSystemMonitorNotifications eventType,
            List<FileSystemMonitorNotifications> eventTypes) {
        if (eventTypes == null) {
            return true;
        }
        for (FileSystemMonitorNotifications notification : eventTypes) {
            if (notification.equals(eventType)) {
                return true;
            }

        }
        return false;
    }

//    /**
//     * Helper method to check for optional rules consistency.
//     * 
//     * @param fileName
//     * 
//     * @return boolean
//     */
//    private boolean checkOptionalRuleConsistency(final FileSystemMonitorNotifications eventType,
//            final String prefix, final String fileName) {
//        int occurrencies;
//
//        for (FileEventRule rule : this.optionalRules) {
//            // check event type, incase we have a filter on that
//            final List<FileSystemMonitorNotifications> eventTypes = rule
//                    .getAcceptableNotifications();
//            if (!checkEvent(eventType, eventTypes))
//                return false;
//            
//            //check occurencies for this file in case we have multiple occurrencies
//            occurrencies = rule.getActualOccurrencies();
//            final int originalOccurrencies = rule.getOriginalOccurrencies();
//            //we cannot exceed the number of needed occurrencies!
//            if(occurrencies>=originalOccurrencies)
//            	return false;
//            final Pattern p = Pattern.compile(rule.getRegex());
//            if (p.matcher(fileName).matches()) {
//                if (this.commonPrefixRegex == null) {
//                    this.commonPrefixRegex = prefix;
//                    rule.setActualOccurrencies(occurrencies+ 1);
//
//                    return true;
//                } else if (prefix.startsWith(this.commonPrefixRegex)) {
//                    rule.setActualOccurrencies(occurrencies + 1);
//
//                    return true;
//                }
//            }
//            
//            
//        }
//
//        return false;
//    }
    /**
     * FileBasedEventConsumer initialization.
     * 
     * @throws InitializationException
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
            final ProgressListenerService progressListenerService =
                    CatalogHolder.getCatalog().getResource(serviceID, ProgressListenerService.class);
            if (progressListenerService != null) {
                ProgressListener progressListener = progressListenerService.createProgressListener(plConfig);
                getListenerForwarder().addListener(progressListener);
            } else {
                throw new IllegalArgumentException("Could not find '"+serviceID+"' listener, declared in " + configuration.getId() + " configuration");
            }
        }

        // ////////////////////////////////////////////////////////////////////
        // RULES
        // ////////////////////////////////////////////////////////////////////

        numInputFiles = 0;
        for (FileEventRule rule : configuration.getRules()) {
            if (!rule.isOptional()) {
                this.mandatoryRules.add(rule);
                numInputFiles += rule.getOriginalOccurrencies();
            } else {
                this.optionalRules.add(rule);
            }
        }

        // ////////////////////////////////////////////////////////////////////
        // ACTIONS
        // ////////////////////////////////////////////////////////////////////

        final List<Action<FileSystemMonitorEvent>> loadedActions = new ArrayList<Action<FileSystemMonitorEvent>>();
        for (ActionConfiguration actionConfig : configuration.getActions()) {
            final String actionServiceID = actionConfig.getServiceID();
            final ActionService<FileSystemMonitorEvent, ActionConfiguration> actionService =
                    CatalogHolder.getCatalog().getResource(actionServiceID, ActionService.class);
            if (actionService != null) {
                Action<FileSystemMonitorEvent> action = actionService.createAction(actionConfig);

                // attach listeners to actions
                for (ProgressListenerConfiguration plConfig : actionConfig.getListenerConfigurations()) {
                    final String listenerServiceID = plConfig.getServiceID();
                    final ProgressListenerService progressListenerService =
                            CatalogHolder.getCatalog().getResource(listenerServiceID, ProgressListenerService.class);
                    if (progressListenerService != null) {
                        ProgressListener progressListener = progressListenerService.createProgressListener(plConfig);
                        action.addListener(progressListener);
                    } else {
                        throw new IllegalArgumentException("Could not find '"+listenerServiceID+"' listener," +
                                " declared in " + actionConfig.getId() + " action configuration," +
                                " in " + configuration.getId() + " consumer");
                    }
                }

                loadedActions.add(action);
            }
        }
        super.addActions(loadedActions);

        if (loadedActions.isEmpty()) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(getClass().getSimpleName() + " initialized with "
                        + mandatoryRules.size() + " mandatory rules, "
                        + optionalRules.size() + " optional rules, "
                        + loadedActions.size() + " actions");
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
    public void run() {
        this.canceled = false;
        boolean jobResultSuccessful = false;
        Throwable exceptionOccurred = null;

        getListenerForwarder().setTask("Configuring");
        getListenerForwarder().started();

        try {

            // create live working dir
            getListenerForwarder().progressing(10, "Creating working dir");
            final String timeStamp = DATEFORMAT.format(new Date());
            final File currentRunDirectory = new File(this.workingDir, timeStamp);
            currentRunDirectory.mkdir();
            if ((currentRunDirectory == null) || !currentRunDirectory.exists() || !currentRunDirectory.isDirectory()) {
                throw new IllegalStateException("Could not create consumer data directories!");
            }

            // create backup dir
            getListenerForwarder().progressing(20, "Creating backup dir");
            File backup = null;
            if (this.configuration.isPerformBackup()) {
                backup = new File(currentRunDirectory, "backup");
                if (!backup.exists()) {
                    backup.mkdirs();
                }
            }

            final Queue<FileSystemMonitorEvent> fileEventList = new LinkedList<FileSystemMonitorEvent>();
            int numProcessedFiles = 0;
            for (FileSystemMonitorEvent ev : this.eventsQueue) {
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info(new StringBuilder("FileBasedEventConsumer [").append(Thread.currentThread().getName()).append("]: new element retrieved from the MailBox.").toString());

                // get info for the input file event
                final File sourceDataFile = ev.getSource();
                final String fileBareName = FilenameUtils.getName(sourceDataFile.toString());

                getListenerForwarder().progressing(30 + (10f / this.eventsQueue.size() * numProcessedFiles++ ), "Preprocessing event " + fileBareName);

                final File destDataFile = new File(currentRunDirectory, fileBareName);
                destDataFile.createNewFile();

                if (IOUtils.acquireLock(this, sourceDataFile)) {
                    IOUtils.copyFile(sourceDataFile, destDataFile);
                    LOGGER.info(new StringBuilder("FileBasedEventConsumer [").append(Thread.currentThread().getName()).append("]: accepted file ").append(sourceDataFile).toString());
                } else {
                    LOGGER.severe(new StringBuilder("FileBasedEventConsumer [").append(Thread.currentThread().getName()).append("]: could not lock file ").append(sourceDataFile).toString());

                    // TODO: lock not aquired: what else?
                }

                //
                fileEventList.offer(new FileSystemMonitorEvent(destDataFile, FileSystemMonitorNotifications.FILE_ADDED));

                // Backing up files and delete sources.
                if (this.configuration.isPerformBackup()) {
                    getListenerForwarder().progressing(30 + (10f / this.eventsQueue.size() * numProcessedFiles++ ), "Creating backup files");
                    performBackup(sourceDataFile, backup, fileBareName);
                } else { // schedule for removal
                    IOUtils.deleteFile(sourceDataFile);
                }
            }

            // //
            // TODO if no further processing is necessary or can be
            // done due to some error, set eventConsumerStatus to Finished or
            // Failure. (etj: ???)
            // //
            LOGGER.info(new StringBuilder("FileBasedEventConsumer [").append(Thread.currentThread().getName()).append("]: new element processed.").toString());

            // // Finally, run the Actions on the files
            getListenerForwarder().progressing(50, "Running actions");

            if (this.applyActions(fileEventList)) {
                this.setStatus(EventConsumerStatus.COMPLETED);
                jobResultSuccessful = true;
            } else {
                this.setStatus(EventConsumerStatus.FAILED);
            }
        } catch (ActionException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "FileBasedEventConsumer "
                        + Thread.currentThread().getName() 
                        + " Error during "+e.getAction().getClass().getSimpleName()+" execution: "
                        + e.getLocalizedMessage(), e);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "FileBasedEventConsumer "
                        + Thread.currentThread().getName() + " could not move file "
                        + " due to the following IO error: " + e.getLocalizedMessage(), e);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch (InterruptedException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "FileBasedEventConsumer "
                        + Thread.currentThread().getName() + " could not move file "
                        + " due to an InterruptedException: " + e.getLocalizedMessage(), e);
            }
            this.setStatus(EventConsumerStatus.FAILED);
            exceptionOccurred = e;

        } catch(RuntimeException e) {
            exceptionOccurred = e;
            throw e;

        } finally {
        	getListenerForwarder().progressing(100, "Running actions");
            LOGGER.info(Thread.currentThread().getName() + " DONE!");
            this.dispose();

            if(jobResultSuccessful && exceptionOccurred==null)
                getListenerForwarder().completed();
            else
                getListenerForwarder().failed(exceptionOccurred);
        }
    }

    protected void performBackup(final File sourceDataFile, File backup, final String fileName) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(getClass().getSimpleName() + " " + Thread.currentThread().getName() + " --- Performing BackUp of input files");
        }
        try {
            if (IOUtils.acquireLock(this, sourceDataFile)) {
                File destFile = new File(backup, fileName);
                if (destFile.exists()) {
                    throw new IOException("Back up file already existent!");
                }
                IOUtils.moveFileTo(sourceDataFile, backup, true);
            } else {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.severe(Thread.currentThread().getName() + " - Can't lock file " + sourceDataFile);
                }
            }
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, getClass().getSimpleName() + "[" + Thread.currentThread().getName() + "]:"
                        + " could not backup file " + fileName
                        + " due to the following IO error: " + e.getLocalizedMessage(), e);
            }
        } catch (InterruptedException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, getClass().getSimpleName() + "[" + Thread.currentThread().getName() + "]:"
                        + " could not backup file " + fileName
                        + " due to the following IO error: " + e.getLocalizedMessage(), e);
            }
        }
    }

    public void setConfiguration(FileBasedEventConsumerConfiguration configuration) {
        this.configuration = configuration;

    }

    /**
     * @return the workingDirectory
     */
    public File getWorkingDir() {
        return workingDir;
    }

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

    @Override
    public boolean consume(FileSystemMonitorEvent event) {
        if (getStatus() != EventConsumerStatus.IDLE && getStatus() != EventConsumerStatus.WAITING) {
            return false;
        }
        if (!canConsume(event)) {
            return false;
        }
        super.consume(event);

        //start execution
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

    public boolean isCanceled() {
        return canceled;
    }

    @Override
    protected void setStatus(EventConsumerStatus eventConsumerStatus) {
        super.setStatus(eventConsumerStatus);
//        // are we executing? If yes, let's trigger a thread!
//        if (eventConsumerStatus == EventConsumerStatus.EXECUTING)
//            getCatalog().getExecutor().execute(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "["
                + "name:" + getName()
                + " status:" + getStatus()
                + " actions:" + actions.size()
                + " events:" + eventsQueue.size()
                + " still missing:" + numInputFiles
                + (isPaused() ? " PAUSED" : "")
                + (eventsQueue.isEmpty() ? "" : (" first event:" + eventsQueue.peek().getSource().getName()))
                + "]";
    }
}
