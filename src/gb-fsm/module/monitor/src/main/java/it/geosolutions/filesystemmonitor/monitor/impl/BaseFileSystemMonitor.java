/**
 * 
 */
package it.geosolutions.filesystemmonitor.monitor.impl;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;


/**
 * @author   Alessio Fabiani, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public abstract class BaseFileSystemMonitor implements
		FileSystemMonitor {

	public final static ExecutorService threadPool=Executors.newCachedThreadPool();


	/** Default Logger **/
	private final static Logger LOGGER = Logger.getLogger(BaseFileSystemMonitor.class.toString());
	
	private class EventManager implements Runnable{
		 private FileSystemMonitorEvent event;

		// ----------------------------------------------- PUBLIC METHODS
        /**
         * Default Constructor
         */
        public EventManager(final FileSystemMonitorEvent fe) {
            this.event=fe;
            
            
            //handle the event
            threadPool.execute(this);
        }


        // ----------------------------------------------- UTILITY METHODS

        /**
         * Sending an event by putting it inside the Swing dispatching thred. This
         * might be useless in command line app but it is important in GUi apps. I
         * might change this though.
         * 
         * @param file
         */
        private void handleEvent() {
        	//deal with locking of input files
        	if(lockInputFiles)
        	{
        		final File source=event.getSource();
        		try {
					IOUtils.acquireLock(this, source,lockWaitThreshold);
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
					return;
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
					return;
				}
        	}
        	
        	
        	
            // Guaranteed to return a non-null array
            final Object[] listenersArray = listeners.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            final int length = listenersArray.length;
            for (int i = length - 2; i >= 0; i -= 2) {
                    final int index = i + 1;
                    if (listenersArray[i] == FileSystemMonitorListener.class) {
                            // Lazily create the event inside the dispatching thread in
                            // order to avoid problems if we run this inside a GUI app.
                            SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                            ((FileSystemMonitorListener) listenersArray[index])
                                                            .fileMonitorEventDelivered(event);

                                    }
                            });

                    }
            }

        }

        /**
    	 *
    	 */
        public void run() {
            try {
         
            	//send event
            	handleEvent();
                
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, new StringBuffer("Caught an IOException Exception: ")
                        .append(e.getLocalizedMessage()).toString(), e);
            }

        }
	}
	
	

	protected BaseFileSystemMonitor(File file, String wildCard) {
		this(file, wildCard, false,-1);
		
	}
	
	protected BaseFileSystemMonitor(File file, String wildCard,final boolean lockInputFiles) {

		this(file, wildCard, lockInputFiles, IOUtils.MAX_WAITING_TIME_FOR_LOCK);
	}
	
	
	protected BaseFileSystemMonitor(File file, String wildCard,final boolean lockInputFiles, final long maxLockingWait) {
		if (file==null)
			throw new IllegalArgumentException("Null file provided to this FileSystemMonitor");
		if(wildCard!=null&&wildCard.length()<=0)
			throw new IllegalArgumentException("Empty wild card provided");
		if(wildCard!=null&&wildCard.length()>0&&(file.exists()&&!file.isDirectory()))
			throw new IllegalArgumentException("Wild card provided, while monitoring a singl file");
		this.file = file;
		this.wildCardString = wildCard;
		this.lockInputFiles=lockInputFiles;
		this.lockWaitThreshold=maxLockingWait;
		
	}
	
	public File getFile() {
		return this.file;
	}


	public synchronized String getWildCard() {
		return this.wildCardString;
	}


	protected EventListenerList listeners = new EventListenerList();
	
	protected File file = null;
	protected String wildCardString = null;
	
	protected long lockWaitThreshold=IOUtils.MAX_WAITING_TIME_FOR_LOCK;
	
	protected boolean lockInputFiles;



	public abstract FileSystemMonitorSPI getSPI();

	protected void finalize() throws Throwable {
		dispose();
	}

	/**
	 * Add listener to this file monitor.
	 * 
	 * @param fileListener
	 *            Listener to add.
	 */
	public synchronized void addListener(FileSystemMonitorListener fileListener) {
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

		listeners.add(FileSystemMonitorListener.class, fileListener);
	}

	/**
	 * Remove listener from this file monitor.
	 * 
	 * @param fileListener
	 *            Listener to remove.
	 */
	public synchronized void removeListener(FileSystemMonitorListener fileListener) {
		listeners.remove(FileSystemMonitorListener.class, fileListener);

	}

	public synchronized void dispose() {
		stop();
		if (listeners != null) {
			Object[] listenerArray = listeners.getListenerList();
			final int length = listenerArray.length;
			for (int i = length - 2; i >= 0; i -= 2) {
				if (listenerArray[i] == FileSystemMonitorListener.class) {
					listeners.remove(FileSystemMonitorListener.class,
							(FileSystemMonitorListener) listenerArray[i + 1]);
				}
			}
		}
		listeners = null;
	}


	/**
	 * Sending an event by putting it inside the Swing dispatching thred. This
	 * might be useless in command line app but it is important in GUi apps. I
	 * might change this though.
	 * 
	 * @param file
	 */
	protected void sendEvent(final FileSystemMonitorEvent fe) {
		new EventManager(fe);

	}

    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = (PRIME * result) +
            ((file == null) ? 0 : file.hashCode());
        result = (PRIME * result) +
            ((wildCardString == null) ? 0 : wildCardString.hashCode());

        return result;
    }
    
    
    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(
                "FileSystemMonitor {");
        sb.append(this.file.getAbsolutePath()).append("; ");
        sb.append(this.wildCardString.toString()).append("; ");

        sb.append("}");

        return sb.toString();
    }
	
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

        final BaseFileSystemMonitor other = (BaseFileSystemMonitor) obj;


        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }

        if (wildCardString == null) {
            if (other.wildCardString != null) {
                return false;
            }
        } else if (!wildCardString.equals(other.wildCardString)) {
            return false;
        }

        return true;
    }
    
}