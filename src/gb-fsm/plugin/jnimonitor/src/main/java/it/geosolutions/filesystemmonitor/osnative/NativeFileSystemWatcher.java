package it.geosolutions.filesystemmonitor.osnative;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.impl.BaseFileSystemMonitor;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

/**
 * @author Alessio
 */
public final class NativeFileSystemWatcher extends BaseFileSystemMonitor {
	private final static Logger LOGGER = Logger
			.getLogger("it.geosolutions.filesystemmonitor.osnative");
	
	private final class BaseJNotifyListener implements JNotifyListener {
		public BaseJNotifyListener() {

		}

		public void fileRenamed(int wd, String rootPath,
				String oldName, String newName) {
			// fire event
			if (!isPaused() && isRunning()) {
				sendEvent(new FileSystemMonitorEvent(
						new File(rootPath
								+ File.separatorChar
								+ newName),
						FileSystemMonitorNotifications.FILE_MODIFIED));
			}
		}

		public void fileModified(int wd, String rootPath,
				String name) {
			// fire event
			if (!isPaused() && isRunning()) {
				sendEvent(new FileSystemMonitorEvent(
						new File(rootPath
								+ File.separatorChar + name),
						FileSystemMonitorNotifications.FILE_MODIFIED));
			}
		}

		public void fileDeleted(int wd, String rootPath,
				String name) {
			// fire event
			if (!isPaused() && isRunning()) {
				sendEvent(new FileSystemMonitorEvent(
						new File(rootPath
								+ File.separatorChar + name),
						FileSystemMonitorNotifications.FILE_REMOVED));
			}
		}

		public void fileCreated(int wd, String rootPath,
				String name) {
			// fire event
			if (!isPaused() && isRunning()) {
				sendEvent(new FileSystemMonitorEvent(
						new File(rootPath
								+ File.separatorChar + name),
						FileSystemMonitorNotifications.FILE_ADDED));
			}
		}
	}

	private final JNotifyListener notificationsListener = new BaseJNotifyListener();
	/**
	 * Indicates whether or not this thread is running.
	 * 
	 * @uml.property name="isPaused"
	 */
	private volatile boolean isPaused = false;

	/**
	 * Indicates whether or not this thread is running.
	 * 
	 * @uml.property name="isRunning"
	 */
	private volatile boolean isRunning = false;

	/** Whether sub-directories should be included or not... */
	private boolean includeSubdirectories;

	/** The kind of FS event to listen * */
	private int mask;

	/** Watcher ID * */
	private Long wd = null;



	public NativeFileSystemWatcher(File file) {
		this(file, false);
	}

	/**
	 * Custom constructor.
	 * 
	 * @param mask
	 *            {@link int} Indicating the kind of FS events to watch
	 * @param linkplain
	 *            {@link boolean} Whether sub-directories should be included or
	 *            not
	 */
	public NativeFileSystemWatcher(File file, boolean includeSubdirectories) {
		this(file,  includeSubdirectories,null);
	}
	/**
	 * Create a file monitor instance with specified polling interval.
	 * 
	 * @param pollingInterval
	 *            Polling interval in milli seconds.
	 */
	public NativeFileSystemWatcher(File file,boolean includeSubdirectories,String wildCard) {
		super(file, wildCard);
		this.mask = JNotify.FILE_ANY;
		this.includeSubdirectories = includeSubdirectories;
	
	}
	
	public NativeFileSystemWatcher(File file, String wildCard) {
		this(file, false, wildCard);
	
	}

	public FileSystemMonitorSPI getSPI() {
		return new NativeFileSystemWatcherSPI();
	}

	/**
	 * Starts watching changes in the directory. Note that a worker thread will
	 * be started to monitor file system changes, and this method will return
	 * immediately. All file system events are fired in the worker thread. The
	 * worker thread will run until stop() is invoked if there is no error. In
	 * case of error, the worker thread might terminate prematurely. You can use
	 * isRunning() to check whether the worker thread is running or not.
	 */
	public synchronized void start() {
		// do not bother if I am running
		if(isRunning)
			return;
		// ////
		// if "start" method is invoked after "pause" method or "stop" method,
		// then start watching events again
		// ////
		if (isPaused() ) {
			isPaused = false;
			isRunning = true;
			return;
		}

		if (file == null || !file.exists() || !file.isDirectory()) {
			final IllegalStateException ise= new IllegalStateException("Severe Error. FileSystemWatcher is not properly configured!!");
			throw ise;
		} 
		if (wd != null) {
			final IllegalStateException ise= new IllegalStateException("Severe Error. FileSystemWatcher is not properly configured!!");
			throw ise;
		} 

		// register this worker for change notifications
		try {
			this.wd = new Long(JNotify.addWatch(file.getAbsolutePath(),
					mask, includeSubdirectories,notificationsListener));
			isRunning=true;
		} catch (Throwable e) {
			//rethrow
			isRunning=false;
			throw new RuntimeException(e);
		}
		
	}

	/**
	 * Stops watching changes in the directory.
	 */
	public synchronized void stop() {
		isPaused = false;
		isRunning = false;

		if(wd==null)
			return;
		try {
			JNotify.removeWatch(wd.intValue());
		} catch (JNotifyException e) {
			LOGGER.log(Level.SEVERE,
					"Error occurred when detaching Native Watcher: "
							+ e.getLocalizedMessage());
		} finally {
			wd = null;
		}
	}

	public void dispose() {
		super.dispose();
		//no big deal if we do this outside critical section
		wd=null;
	}

	/**
	 * 
	 */
	public void pause() {
		isPaused = true;
	}

	/**
	 * 
	 */
	public synchronized void reset() {
		stop();
		start();
	}

	/**
	 * Check if the watcher is up and running.
	 * 
	 * @uml.property name="isRunning"
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * @uml.property name="isPaused"
	 */
	public boolean isPaused() {
		return isPaused;
	}

}