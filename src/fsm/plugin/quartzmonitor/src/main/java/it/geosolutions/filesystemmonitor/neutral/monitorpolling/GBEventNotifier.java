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
package it.geosolutions.filesystemmonitor.neutral.monitorpolling;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemListener;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.event.EventListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author (v1) Simone Giannecchini
 * @author (v2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class GBEventNotifier implements Runnable {

    /** Default Logger **/
    private final static Logger LOGGER = LoggerFactory.getLogger(GBEventNotifier.class.toString());

    // private long lockWaitThreshold = IOUtils.MAX_WAITING_TIME_FOR_LOCK;

    // private boolean lockInputFiles;

    public final static ExecutorService threadPool = Executors.newCachedThreadPool();

    private EventListenerList listeners = null;

    // queue of events to pass to the listener list
    /**
     * @uml.property name="eventQueue"
     * @uml.associationEnd multiplicity="(0 -1)"
     *                     elementType="it.geosolutions.filesystemmonitor.monitor.FileSystemEvent"
     */
    private BlockingQueue<FileSystemEvent> eventQueue = new ArrayBlockingQueue<FileSystemEvent>(100); // TODO
                                                                                                      // change

    // the stop element
    private static FileSystemEvent STOP = new FileSystemEvent(new File(""), null);

    // set true to end the thread

    private boolean stop;

    // used to filter the the type of events to be notified
    private FileSystemEventType eventFilter;

    /**
     * used to check the status of this event consumer if this method return true thread is stopped
     * and
     * 
     * @return
     */
    public boolean isStopped() {
        return stop;
    }

    /**
     * used to start the consumer
     */
    public void start() {
        if (this.isStopped()) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Starting the event consumer...");
            threadPool.execute(this);
            stop = false;
        } else if (LOGGER.isWarnEnabled())
            LOGGER.warn("The event consumer is already started");
    }

    /**
     * used to stop the event consumer
     */
    public void stop() {
        try {
            eventQueue.put(STOP);
        } catch (InterruptedException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            stop = true;
        }
    }

    /**
     * Default Constructor
     */
    // public GBEventNotifier(boolean lockInputFiles, long maxLockingWait, EventListenerList list){
    // // this.lockInputFiles=lockInputFiles;
    // // this.lockWaitThreshold=maxLockingWait;
    // this.stop=true; //still not started
    // this.listeners=list;
    // // handle the event
    // this.start();
    // }

    /**
     * Default Constructor
     */
    public GBEventNotifier(EventListenerList list, FileSystemEventType type) {
        // this.lockInputFiles=lockInputFiles;
        // this.lockWaitThreshold=maxLockingWait;
        this.stop = true; // still not started
        this.listeners = list;
        this.eventFilter = type;
        // handle the event
        this.start();
    }

    // ----------------------------------------------- UTILITY METHODS

    /**
     * Sending an event by putting it inside the Swing dispatching thred. This might be useless in
     * command line app but it is important in GUi apps. I might change this though.
     * 
     * @param event
     */
    private void notifyAll(final FileSystemEvent event) {

        /*
         * FileSystemEventType notified= event.getNotification();
         * 
         * if (// if file event is NOT FILE_REMOVED and NOT DIR_REMOVED (notified!=
         * FileSystemEventType.DIR_REMOVED && notified!=FileSystemEventType.FILE_REMOVED) ){ // deal
         * with locking of input files if (lockInputFiles) { final File source = event.getSource();
         * try { //java.nio.channels.FileLock //org.apache.commons.io.FileUtils.
         * IOUtils.acquireLock(this, source, lockWaitThreshold); } catch (InterruptedException e) {
         * if (LOGGER.isErrorEnabled()) LOGGER.error(e.getLocalizedMessage(), e); return; } catch
         * (IOException e) { if (LOGGER.isErrorEnabled()) LOGGER.error(e.getLocalizedMessage(), e);
         * return; } } }
         */
        final Object[] listenersArray = listeners.getListenerList();// return a not-null array
        /*
         * Process the listeners last to first, notifying those that are interested in this event
         */
        final int length = listenersArray.length;
        for (int i = length - 2; i >= 0; i -= 2) {
            final int index = i + 1;
            if (listenersArray[i] == FileSystemListener.class) {
                ((FileSystemListener) listenersArray[index]).onFileSystemEvent(event);
            }
        }
    }

    /**
     * /** Use this method to add events to this consumer the event will be filtered using the
     * FileSystemEventType specified into the EventGeneratorConfiguration.
     * 
     * @param file
     * @param type
     */
    protected void notifyEvent(final File file, final FileSystemEventType type) {
        if (type == eventFilter) {
            if (!eventQueue.offer(new FileSystemEvent(file, type)))
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("GBEventNotifier: Unable to offer a new object to the eventQueue");
        } else if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("GBEventNotifier: FILTERED -> event of type \'" + type.toString()
                    + "\' on file \'" + file.getAbsolutePath() + "\'");
        }
    }

    /**
     * Never call this method manually
     */
    public void run() {
        try {
            FileSystemEvent event = null;
            while ((event = eventQueue.take()) != STOP && !stop) {
                // send event
                notifyAll(event);
            }
            // clean
            eventQueue.clear();
        } catch (InterruptedException uoe) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("GBEventNotifier: " + uoe.getLocalizedMessage(), uoe);
        } catch (UnsupportedOperationException uoe) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("GBEventNotifier: Caught an UnsupportedOperationException: "
                                + uoe.getLocalizedMessage(), uoe);
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("GBEventNotifier: Caught an IOException: " + e.getLocalizedMessage(), e);
        }

    }
}
