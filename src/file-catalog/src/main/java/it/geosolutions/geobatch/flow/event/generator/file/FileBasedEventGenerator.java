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
package it.geosolutions.geobatch.flow.event.generator.file;

import it.geosolutions.filesystemmonitor.FSMSPIFinder;
import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.impl.BaseFileSystemMonitor;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.flow.event.generator.BaseEventGenerator;
import it.geosolutions.geobatch.flow.event.generator.EventGenerator;
import it.geosolutions.geobatch.flow.event.generator.FlowEventListener;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;


/**
 * Comments here ...
 * 
 * @author AlFa (Alessio Fabiani)
 * @author Ivano Picco
 */
public class FileBasedEventGenerator<T extends EventObject> extends BaseEventGenerator<T> implements EventGenerator<T> {
    // ----------------------------------------------- PRIVATE ATTRIBUTES

    /**
     * Private Logger
     */
    private static Logger LOGGER = Logger.getLogger(FileBasedEventGenerator.class.toString());

    /**
     * Helper class implementing an event listener for the FileSystem Monitor.
     */
    private final class EventListener implements FileSystemMonitorListener {
        /*
         * (non-Javadoc)
         * 
         * @see
         * it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener#fileMonitorEventDelivered
         * (it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent)
         */

        public void fileMonitorEventDelivered(final FileSystemMonitorEvent fe) {
            if (fe != null && fe.getSource() != null) {


                final FileSystemMonitorNotifications acceptedNotification =
                        FileBasedEventGenerator.this.getEventType();
                final FileSystemMonitorNotifications notification = fe.getNotification();

                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info(new StringBuilder("Event: ").append(notification.toString()).append(" ").append(fe.getSource()).toString());
                }

                if (acceptedNotification != null && notification.equals(acceptedNotification)) {
                    FileBasedEventGenerator.this.sendEvent(fe);
                } else if (acceptedNotification == null) {
                    FileBasedEventGenerator.this.sendEvent(fe);
                }
            } else {
                if (fe == null) {
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Null Event delivered ");
                    }
                } else {
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Null Event's source ");
                    }
                }
            }
        }
    }
    /**
     * The File-System Monitor thread.
     * 
     * @uml.property name="fsMonitor"
     */
    private FileSystemMonitor fsMonitor;
    /**
     * The directory to watch.
     * 
     * @uml.property name="watchDirectory"
     */
    private File watchDirectory;
    /**
     *
     * A flag used to keep files in watchDirectory when flow is started.
     *
     * @uml.property name="keepFiles"
     */
    private boolean keepFiles;
    /**
     * 
     */
    private FileSystemMonitorNotifications eventType;
    /**
     * The file extension wildcard.
     * 
     * @uml.property name="wildCard"
     */
    private String wildCard;
    private EventListenerList listeners = new EventListenerList();
    private EventListener fsListener;

    // ----------------------------------------------- PUBLIC CONSTRUCTORS
    /**
     * Constructor which gets OS Type and watched dir as parameters.
     * 
     * @param osType
     *            int OSType (0 - Undefined; 1 - Windows; 2 - Linux)
     * @param dir
     *            File directory to watch
     * @throws NotSupportedException
     */
    public FileBasedEventGenerator(final OsType osType,
            final FileSystemMonitorNotifications eventType, final File dir) {
        this(osType, eventType, dir, null, false);
    }

    /**
     * Constructor which gets OS Type, watched dir and extension wildcard as parameters.
     *
     * @param osType
     *            int OSType (0 - Undefined; 1 - Windows; 2 - Linux)
     * @param dir
     *            File directory to watch
     * @param wildcard
     *            String file extension wildcard
     * @throws NotSupportedException
     */
    public FileBasedEventGenerator(final OsType osType,
            final FileSystemMonitorNotifications eventType, final File dir, final String wildcard){
        this(osType, eventType, dir, wildcard, false);
    }

    /**
     * Constructor which gets OS Type, watched dir and keep files in watched dir flag as parameters.
     *
     * @param osType
     *            int OSType (0 - Undefined; 1 - Windows; 2 - Linux)
     * @param sensedDir
     *            File directory to watch
     * @param keepFiles
     *            Flag used to keep file in watched directory when flow is started
     * @throws NotSupportedException
     */
    FileBasedEventGenerator(OsType osType, FileSystemMonitorNotifications eventType, File sensedDir, boolean keepFiles){
        this(osType, eventType, sensedDir, null, keepFiles);
    }

    /**
     * Constructor which gets OS Type, watched dir, extension wildcard and keep files in watched dir flag as parameters.
     *
     * @param osType
     *            int OSType (0 - Undefined; 1 - Windows; 2 - Linux)
     * @param dir
     *            File directory to watch
     * @param wildcard
     *            String file extension wildcard
     * @param keepFiles
     *            Flag used to keep file in watched directory when flow is started
     * @throws NotSupportedException
     */
    public FileBasedEventGenerator(final OsType osType,
            final FileSystemMonitorNotifications eventType, final File dir, final String wildcard, final boolean keepFiles){

        initialize(osType, eventType, dir, wildcard, keepFiles);
    }

    /**
     * @param osType
     * @param eventType
     * @param dir
     * @param wildcard
     * @param keepFiles 
     * @throws NotSupportedException
     */
    private void initialize(
    		final OsType osType, 
    		final FileSystemMonitorNotifications eventType,
            final File dir, 
            final String wildcard, 
            final boolean keepFiles) {
        // add myself as listener
        fsListener = new EventListener();

        this.watchDirectory = dir;
        this.wildCard = wildcard;
        this.eventType = eventType;
        this.keepFiles = keepFiles;

        if ((this.watchDirectory != null) && this.watchDirectory.isDirectory() && this.watchDirectory.exists()) 
        {
            	
    		final Map<String,Object>params= new HashMap<String, Object>();
    		params.put(FileSystemMonitorSPI.SOURCE, dir);
    		if (this.wildCard != null)
    			params.put(FileSystemMonitorSPI.WILDCARD, wildCard);	
            this.fsMonitor = (BaseFileSystemMonitor) FSMSPIFinder.getMonitor(params,osType);
            this.fsMonitor.addListener(fsListener);
        }
        else
        	throw new IllegalArgumentException("Unable to start the FileSystemMonitor for directory:"+dir.getAbsolutePath());
    }

    // ----------------------------------------------- PUBLIC ACCESS METHODS
    public FileBasedEventGenerator(FileBasedEventGeneratorConfiguration configuration)
            throws IOException {
        OsType osType = configuration.getOsType();
        FileSystemMonitorNotifications eventType = configuration.getEventType();
        final File notifyDir = IOUtils.findLocation(configuration.getWorkingDirectory(), new File(
                ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        if (notifyDir == null || !(notifyDir.exists() && notifyDir.isDirectory() & notifyDir.canRead())) {
            throw new IOException("Invalid notify directory");
        }
        boolean keepFiles = configuration.getKeepFiles();
        String wildCard = configuration.getWildCard();
        initialize(osType, eventType, notifyDir, wildCard, keepFiles);
    }

    /**
     * @return the watchDirectory
     * @uml.property name="watchDirectory"
     */
    public File getWatchDirectory() {
        return watchDirectory;
    }

    /**
     * @return the wildCard
     * @uml.property name="wildCard"
     */
    public String getWildCard() {
        return wildCard;
    }

    // ----------------------------------------------- OVERRIDE METHODS

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final FileBasedEventGenerator other = (FileBasedEventGenerator) obj;

        if (fsMonitor == null) {
            if (other.fsMonitor != null) {
                return false;
            }
        } else if (!fsMonitor.equals(other.fsMonitor)) {
            return false;
        }

        if (watchDirectory == null) {
            if (other.watchDirectory != null) {
                return false;
            }
        } else if (!watchDirectory.equals(other.watchDirectory)) {
            return false;
        }

        if (wildCard == null) {
            if (other.wildCard != null) {
                return false;
            }
        } else if (!wildCard.equals(other.wildCard)) {
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = (PRIME * result) + ((fsMonitor == null) ? 0 : fsMonitor.hashCode());
        result = (PRIME * result) + ((watchDirectory == null) ? 0 : watchDirectory.hashCode());
        result = (PRIME * result) + ((wildCard == null) ? 0 : wildCard.hashCode());

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileBasedEventGenerator {");

        sb.append(this.fsMonitor.toString()).append("; ");
        sb.append(this.watchDirectory.getAbsolutePath()).append("; ");
        sb.append(this.wildCard.toString()).append("; ");

        sb.append("}");

        return sb.toString();
    }

    // ----------------------------------------------- DELEGATE METHODS
    /**
     * 
     * @see it.geosolutions.filesystemmonitor.monitor.Monitor#dispose()
     */
    public synchronized void dispose() {
        fsMonitor.removeListener(fsListener);
        fsMonitor.dispose();
    }

    /**
     * @return
     * @see it.geosolutions.filesystemmonitor.monitor.Monitor#isRunning()
     */
    public synchronized boolean isRunning() {
        return fsMonitor.isRunning();
    }

    /**
     * 
     * @see it.geosolutions.filesystemmonitor.monitor.Monitor#resume()
     */
    public synchronized void start() {
        if (!keepFiles) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Cleaning up " + watchDirectory.getAbsolutePath().toString());
            }
            IOUtils.emptyDirectory(watchDirectory, true, false);
        } else if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Keep existing files in " + watchDirectory.getAbsolutePath().toString());
        }

        fsMonitor.start();
    }

    /**
     * 
     * @see it.geosolutions.filesystemmonitor.monitor.Monitor#pause()
     */
    public synchronized void stop() {
        fsMonitor.stop();
    }

    /**
     * Add listener to this file monitor.
     * 
     * @param fileListener
     *            Listener to add.
     */
    public synchronized void addListener(FlowEventListener<T> fileListener) {
        // Don't add if its already there

        // Guaranteed to return a non-null array
        final Object[] listenerArray = listeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        final int length = listenerArray.length;
        for (int i = length - 2; i >= 0; i -= 2) {
            if (listenerArray[i].equals(fileListener)) {
                return;

            }
        }

        listeners.add(FlowEventListener.class, fileListener);
    }

    /**
     * Remove listener from this file monitor.
     * 
     * @param fileListener
     *            Listener to remove.
     */
    public synchronized void removeListener(FlowEventListener<T> fileListener) {
        listeners.remove(FlowEventListener.class, fileListener);

    }

    /**
     * Sending an event by putting it inside the Swing dispatching thred. This might be useless in
     * command line app but it is important in GUi apps. I might change this though.
     * 
     * @param file
     */
    private void sendEvent(final FileSystemMonitorEvent fe) {
        // Guaranteed to return a non-null array
        final Object[] listenersArray = listeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        final int length = listenersArray.length;
        for (int i = length - 2; i >= 0; i -= 2) {
            final int index = i + 1;
            if (listenersArray[i] == FlowEventListener.class) {
                // Lazily create the event inside the dispatching thread in
                // order to avoid problems if we run this inside a GUI app.
                SwingUtilities.invokeLater(new Runnable() {

                    @SuppressWarnings("unchecked")
                    public void run() {
                        ((FlowEventListener<FileSystemMonitorEvent>) listenersArray[index]).eventGenerated(fe);
                    }
                });

            }
        }

    }

    /**
     * @return the eventType
     */
    public FileSystemMonitorNotifications getEventType() {
        return eventType;
    }
}
