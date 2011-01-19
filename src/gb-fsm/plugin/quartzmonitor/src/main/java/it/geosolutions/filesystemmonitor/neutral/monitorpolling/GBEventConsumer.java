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
package it.geosolutions.filesystemmonitor.neutral.monitorpolling;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.tools.file.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

/**
 * 
 * @author (v1) Simone Giannecchini
 * @author (v2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class GBEventConsumer implements Runnable {

    /** Default Logger **/
    private final static Logger LOGGER = Logger.getLogger(GBEventConsumer.class.toString());
    
    private long lockWaitThreshold = IOUtils.MAX_WAITING_TIME_FOR_LOCK;

    private boolean lockInputFiles;
    
    public final static ExecutorService threadPool = Executors.newCachedThreadPool();
    
    private EventListenerList listeners =null;
    
    // queue of events to pass to the listener list
    private BlockingQueue<FileSystemMonitorEvent> eventQueue=new ArrayBlockingQueue<FileSystemMonitorEvent>(100); //TODO change
    // the stop element
    private static FileSystemMonitorEvent STOP=new FileSystemMonitorEvent(new File(""), null);
    
    // set true to end the thread
    private boolean stop;
    
    /**
     * used to check the status of this event consumer
     * if this method return true thread is stopped and 
     * @return
     */
    public boolean isStopped(){
        return stop;
    }
    
    /**
     * used to start the consumer
     */
    public void start(){
        if (this.isStopped()){
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Starting the event consumer...");
            threadPool.execute(this);
            stop=false;
        }
        else
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("The event consumer is already started");
    }

    /**
     * used to stop the event consumer
     */
    public void stop(){
        try {
            eventQueue.put(STOP);
        } catch (InterruptedException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning(e.getLocalizedMessage());
        }
        finally {
            stop=true;
        }
    }
    /**
     * Default Constructor
     */
    public GBEventConsumer(boolean lockInputFiles, long maxLockingWait, EventListenerList list){
        this.lockInputFiles=lockInputFiles;
        this.lockWaitThreshold=maxLockingWait;
        this.stop=true; //still not started
        this.listeners=list;
        // handle the event
        this.start();
    }

    // ----------------------------------------------- UTILITY METHODS

    /**
     * Sending an event by putting it inside the Swing dispatching thred. This might be useless
     * in command line app but it is important in GUi apps. I might change this though.
     * 
     * @param file
     */
    private void handleEvent(final FileSystemMonitorEvent event) {
/*
        FileSystemMonitorNotifications notified= event.getNotification();
        
        if (// if file event is NOT FILE_REMOVED and NOT DIR_REMOVED
            (notified!= FileSystemMonitorNotifications.DIR_REMOVED &&
                notified!=FileSystemMonitorNotifications.FILE_REMOVED) ){
            // deal with locking of input files
            if (lockInputFiles) {
                final File source = event.getSource();
                try {
                    //java.nio.channels.FileLock
                    //org.apache.commons.io.FileUtils.
                    IOUtils.acquireLock(this, source, lockWaitThreshold);
                } catch (InterruptedException e) {
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    return;
                } catch (IOException e) {
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    return;
                }
            }
        }
*/

        final Object[] listenersArray = listeners.getListenerList();//return a not-null array
        /*
         * Process the listeners last to first, 
         * notifying those that are interested in this event
         */
        final int length = listenersArray.length;
        for (int i = length - 2; i >= 0; i -= 2) {
            final int index = i + 1;
            if (listenersArray[i] == FileSystemMonitorListener.class) {
                ((FileSystemMonitorListener) listenersArray[index]).fileMonitorEventDelivered(event);
            }
        }
    }

    /**
     * Use this method to add events to this consumer
     * 
     * @param o - The fileSystemMonitorEvent to add
     */
    public void add(FileSystemMonitorEvent o){
        eventQueue.add(o);
    }
    
    /**
     * Never call this method manually
     *
     */
    public void run() {
        try {
            FileSystemMonitorEvent event=null;
            while ((event=eventQueue.take())!=STOP && !stop){
                // send event
                handleEvent(event);
            }
            // clean
            eventQueue.clear();
        }
        catch (UnsupportedOperationException uoe){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, new StringBuilder("Caught an UnsupportedOperationException: ")
                    .append(uoe.getLocalizedMessage()).toString(), uoe);
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, new StringBuilder("Caught an IOException: ")
                    .append(e.getLocalizedMessage()).toString(), e);
        }

    }
}
