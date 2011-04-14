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
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.flow.event.generator.BaseEventGenerator;
import it.geosolutions.geobatch.flow.event.generator.FlowEventListener;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;

/**
 * 
 * @author AlFa (Alessio Fabiani)
 * @author (r2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class FileBasedEventGenerator<T extends EventObject> extends BaseEventGenerator<T> {

    private static Logger LOGGER = LoggerFactory.getLogger(FileBasedEventGenerator.class.toString());

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

    
    private FileSystemEventType acceptedEvent;

    /**
     * The file extension wildcard.
     * @uml.property  name="wildCard"
     */
    private String wildCard;

    
    private EventListenerList listeners = new EventListenerList();

    
    private FileSystemListener fsListener; // a GBEventListener thread instance
    
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

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(new StringBuilder("Event: ").append(incomingEvent.toString())
                            .append(" ").append(fe.getSource()).toString());
                }

                if (incomingEvent.equals(acceptedEvent)) {
                    FileBasedEventGenerator.this.sendEvent(fe);
                }
// TODO check if this may never happen
//                else if (acceptedEvent == null) {
//                    FileBasedEventGenerator.this.sendEvent(fe);
//                }
            } else {
                if (fe == null) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Null Event delivered ");
                    }
                } else {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Null Event's source ");
                    }
                }
            }
        }
    }


    /**
     * Constructor which gets OS Type, watched dir, extension wildcard and keep files in watched dir
     * flag as parameters.
     * @param osType int OSType (0 - Undefined; 1 - Windows; 2 - Linux)
     * @param type the preferred fs monitor type 
     * @param eventType the accepted event Type (not null)
     * @param watchDirectory the directory to watch 
     * (should be: not null, readable, exists and isDirectory)
     * @param wildcard (if null '*' is applied)
     * @param keepFiles flag used to keep files in watchDirectory
     * when flow is started.
     */
//    public FileBasedEventGenerator(final OsType osType, final FileSystemMonitorType type,
//            final FileSystemEventType eventType, final File dir, final String wildcard,
//            final boolean keepFiles) {
//        
//        initialize(osType, type, eventType, dir, wildcard, keepFiles);
//    }

    /**
     * @param osType int OSType (0 - Undefined; 1 - Windows; 2 - Linux)
     * @param type the preferred fs monitor type 
     * @param eventType the accepted event Type (not null)
     * @param watchDirectory the directory to watch 
     * (should be: not null, readable, exists and isDirectory)
     * @param wildcard (if null '*' is applied)
     * @param keepFiles flag used to keep files in watchDirectory
     * when flow is started.
     */
    private void initialize(final OsType osType, final FileSystemMonitorType type, final FileSystemEventType eventType,
            final File watchDirectory, final String wildcard, final boolean keepFiles) {
        // add myself as listener
        fsListener = new GBEventListener();

        if (wildcard!=null)
            this.wildCard = wildcard;
        else{
            this.wildCard = "*";
            if (LOGGER.isTraceEnabled()){
                LOGGER.trace("Provided wild card is null, using default ( * )");
            }
        }
        
        if (eventType!=null)
            this.acceptedEvent = eventType;
        else
            throw new IllegalArgumentException(
                    "Unable to initialize FileBasedEventGenerator using a null EventType");

        if (watchDirectory!=null)
            this.watchDirectory = watchDirectory;
        else
            throw new IllegalArgumentException(
                "Unable to initialize FileBasedEventGenerator using a null watchingDirectory");
        
        if (this.watchDirectory.isDirectory() && this.watchDirectory.exists() && this.watchDirectory.canRead()) {
            
            final Map<String, Object> params = new HashMap<String, Object>();
            
            // the watched directory
            params.put(FileSystemMonitorSPI.SOURCE, watchDirectory);
            
            // the wildcard to filter file (DirectoryWalker)
            if (this.wildCard != null)
                params.put(FileSystemMonitorSPI.WILDCARD, wildCard);
            
            // the event type to filter
            params.put(FileSystemMonitorSPI.TYPE, acceptedEvent);
            
            this.fsMonitor = FSMSPIFinder.getMonitor(params, osType,type);

            this.fsMonitor.addListener(fsListener);
        } else
            throw new IllegalArgumentException(
                    "Unable to start the GBFileSystemMonitorJob on watchingDirectory:"+watchDirectory);
        
        this.keepFiles = keepFiles;
    }

    /**
     * Constructor
     * @param configuration the FileBasedEventGeneratorConfiguration configuration 
     * @throws IOException see initialize
     * @thrown NullPointerException see initialize
     */
    public FileBasedEventGenerator(FileBasedEventGeneratorConfiguration configuration)
            throws IOException , NullPointerException{
        super(configuration.getId(), configuration.getName(), configuration.getDescription());
        // absolutize file
        final File notifyDir = Path.findLocation(configuration.getWorkingDirectory(), new File(
                ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        // initialize
        initialize(configuration.getOsType(),
                configuration.getMonitorType(),
                configuration.getEventType(),
                notifyDir,
                configuration.getWildCard(),
                configuration.getKeepFiles());
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

        FileBasedEventGenerator<FileSystemEvent> other=null;
        if (obj instanceof FileBasedEventGenerator)     
            other= (FileBasedEventGenerator) obj;
        else
            throw new IllegalArgumentException("FileBasedEventGenerator: The object is not a FileBasedEventGenerator.");

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
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Cleaning up " + watchDirectory.getAbsolutePath().toString());
            }
            Path.emptyDirectory(watchDirectory, true, false);
        } else if (LOGGER.isInfoEnabled()) {
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
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Unable to remove listener list:\n"+t.getLocalizedMessage());
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
                ((FlowEventListener<FileSystemEvent>) listenersArray[index]).eventGenerated(fe);
            }
        }
    }

    /**
     * @return the acceptedEvent
     * @uml.property name="acceptedEvent"
     */
    public FileSystemEventType getEventType() {
        return acceptedEvent;
    }

}
