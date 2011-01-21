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
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.flow.event.generator.BaseEventGenerator;
import it.geosolutions.geobatch.flow.event.generator.EventGenerator;
import it.geosolutions.geobatch.flow.event.generator.FlowEventListener;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;
import javax.transaction.NotSupportedException;

/**
 * Comments here ...
 * 
 * @author AlFa (Alessio Fabiani)
 */
public class FileBasedEventGenerator<T extends EventObject> extends BaseEventGenerator<T> implements
        EventGenerator<T> {
    // ----------------------------------------------- PRIVATE ATTRIBUTES

    /**
     * Private Logger
     */
    private static Logger LOGGER = Logger.getLogger(FileBasedEventGenerator.class.toString());

    /**
     * Helper class implementing an event listener for the FileSystem Monitor.
     */
    private final class GBEventListener implements FileSystemListener {
        /*
         * (non-Javadoc)
         * 
         * @see it.geosolutions.filesystemmonitor.monitor.FileSystemListener
         * #fileMonitorEventDelivered
         * (it.geosolutions.filesystemmonitor.monitor.FileSystemEvent)
         */
        public void onFileSystemEvent(final FileSystemEvent fe) {
            if (fe != null && fe.getSource() != null) {

                final FileSystemEventType acceptedEvent = 
                                                        FileBasedEventGenerator.this.getEventType();
                
                final FileSystemEventType incomingEvent = fe.getEventType();

                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info(new StringBuilder("Event: ").append(incomingEvent.toString())
                            .append(" ").append(fe.getSource()).toString());
                }

                if (incomingEvent.equals(acceptedEvent)) {
                    FileBasedEventGenerator.this.sendEvent(fe);
                }
//                else if (acceptedEvent == null) {
//                    FileBasedEventGenerator.this.sendEvent(fe);
//                }
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
     * @uml.property  name="fsMonitor"
     */
    private FileSystemMonitor fsMonitor;

    /**
     * The directory to watch.
     * @uml.property  name="watchDirectory"
     */
    private File watchDirectory;

    /**
     * A flag used to keep files in watchDirectory when flow is started.
     * @uml.property  name="keepFiles"
     */
    private boolean keepFiles;

    /**
     * @uml.property  name="acceptedEvent"
     * @uml.associationEnd  
     */
    private FileSystemEventType acceptedEvent;

    /**
     * The file extension wildcard.
     * @uml.property  name="wildCard"
     */
    private String wildCard;

    /**
     * @uml.property  name="listeners"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private EventListenerList listeners = new EventListenerList();

    /**
     * @uml.property  name="fsListener"
     * @uml.associationEnd  
     */
    private FileSystemListener fsListener;

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
    public FileBasedEventGenerator(final OsType osType, final FileSystemMonitorType type,
            final FileSystemEventType eventType, final File dir) {
        this(osType, type, eventType, dir, null, false);
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
    public FileBasedEventGenerator(final OsType osType, final FileSystemMonitorType type,
            final FileSystemEventType eventType, final File dir, final String wildcard) {
        this(osType, type, eventType, dir, wildcard, false);
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
    FileBasedEventGenerator(OsType osType, final FileSystemMonitorType type, FileSystemEventType eventType,
            File sensedDir, boolean keepFiles) {
        this(osType, type, eventType, sensedDir, null, keepFiles);
    }

    /**
     * Constructor which gets OS Type, watched dir, extension wildcard and keep files in watched dir
     * flag as parameters.
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
    public FileBasedEventGenerator(final OsType osType, final FileSystemMonitorType type,
            final FileSystemEventType eventType, final File dir, final String wildcard,
            final boolean keepFiles) {

        initialize(osType, type, eventType, dir, wildcard, keepFiles);
    }

    /**
     * @param osType
     * @param type the preferred fs monitor type 
     * @param acceptedEvent
     * @param dir
     * @param wildcard
     * @param keepFiles
     * @throws NotSupportedException
     */
    private void initialize(final OsType osType, final FileSystemMonitorType type, final FileSystemEventType eventType,
            final File watchDirectory, final String wildcard, final boolean keepFiles) {
        // add myself as listener
        fsListener = new GBEventListener();

        if (watchDirectory!=null)
            this.watchDirectory = watchDirectory;
        else
            throw new IllegalArgumentException(
                "Unable to initialize FileBasedEventGenerator using a null watchingDirectory");
        
        if (wildCard!=null)
            this.wildCard = wildcard;
        else
            this.wildCard = "*";
        
        if (eventType!=null)
            this.acceptedEvent = eventType;
        else
            throw new IllegalArgumentException(
                    "Unable to initialize FileBasedEventGenerator using a null EventType");
        
        this.keepFiles = keepFiles;

        if ((this.watchDirectory != null) && this.watchDirectory.isDirectory()
                && this.watchDirectory.exists()) {

            final Map<String, Object> params = new HashMap<String, Object>();
            params.put(FileSystemMonitorSPI.SOURCE, watchDirectory);
            if (this.wildCard != null)
                params.put(FileSystemMonitorSPI.WILDCARD, wildCard);
            this.fsMonitor = FSMSPIFinder.getMonitor(params, osType,type);

            this.fsMonitor.addListener(fsListener);
        } else
            throw new IllegalArgumentException(
                    "Unable to start the GBFileSystemMonitorJob for directory:"+watchDirectory);
    }

    // ----------------------------------------------- PUBLIC ACCESS METHODS
    public FileBasedEventGenerator(FileBasedEventGeneratorConfiguration configuration)
            throws IOException {
        OsType osType = configuration.getOsType();
        FileSystemEventType eventType = configuration.getEventType();
        final File notifyDir = Path.findLocation(configuration.getWorkingDirectory(), new File(
                ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        if (notifyDir == null
                || !(notifyDir.exists() && notifyDir.isDirectory() & notifyDir.canRead())) {
            throw new IOException("Invalid notify directory");
        }
        boolean keepFiles = configuration.getKeepFiles();
        String wildCard = configuration.getWildCard();
        initialize(osType, configuration.getMonitorType(), eventType, notifyDir, wildCard, keepFiles);
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
     * @see it.geosolutions.filesystemmonitor.monitor.Monitor#destroy()
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
            Path.emptyDirectory(watchDirectory, true, false);
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
    

    public void pause() {
//TODO check do we need to pause any other components?
        fsMonitor.pause();
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
        try{
            if (fileListener!=null)
                listeners.remove(FlowEventListener.class, fileListener);
            else
                throw new NullPointerException("Unable to remove a NULL listener list.");
        }
        catch (Throwable t){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Unable to remove listener list:\n"+t.getLocalizedMessage());
        }
    }

    /**
     * Sending an event by putting it inside the Swing dispatching thred. This might be useless in
     * command line app but it is important in GUi apps. I might change this though.
     * 
     * @param file
     */
    private void sendEvent(final FileSystemEvent fe) {
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
                ((FlowEventListener<FileSystemEvent>) listenersArray[index])
                                .eventGenerated(fe);

            }
        }

    }

    /**
     * @return  the acceptedEvent
     * @uml.property  name="acceptedEvent"
     */
    public FileSystemEventType getEventType() {
        return acceptedEvent;
    }

}
