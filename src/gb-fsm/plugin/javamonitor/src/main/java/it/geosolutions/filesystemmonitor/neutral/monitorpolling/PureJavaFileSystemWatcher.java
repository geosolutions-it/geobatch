package it.geosolutions.filesystemmonitor.neutral.monitorpolling;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.impl.BaseFileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.thread.AbstractPausableThread;
import it.geosolutions.filesystemmonitor.monitor.thread.AbstractPeriodicThread;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public final class PureJavaFileSystemWatcher extends BaseFileSystemMonitor {
	
	public final static long DEFAULT_POLLING_INTERVAL=1000;

	/**
	 * This is the timer thread which is executed every n milliseconds according
	 * to the setting of the file monitor. It investigates the file in question
	 * and notify listeners if changed.
	 * 
	 * @author SImone Giannecchini
	 * 
	 */
	private class PureJavaMonitorNotifier extends AbstractPeriodicThread {

		public PureJavaMonitorNotifier(final long pollingInterval) {
			super("PureJavaFSMonitor", pollingInterval);

		}

		public boolean execute() {

			PureJavaFileSystemWatcher.this.checkFileSystem();

			return true;
		}

		public void dispose() {

		}

	}

	protected final Map<String, Long> filesMap = Collections.synchronizedMap(new TreeMap<String, Long>());
	protected long lastModifiedTime = -1;
	protected Thread workers[];

	public PureJavaFileSystemWatcher(File file) {
		this(file, DEFAULT_POLLING_INTERVAL);
	}
	/**
	 * Create a file monitor instance with specified polling interval.
	 * 
	 * @param pollingInterval
	 *            Polling interval in milli seconds.extension
	 */
	public PureJavaFileSystemWatcher(File file, long pollingInterval) {
		this(file, null,pollingInterval);
	}
	public PureJavaFileSystemWatcher(File file, String wildCard) {
		this(file, wildCard, DEFAULT_POLLING_INTERVAL);
	}
	/**
	 * Create a file monitor instance with specified polling interval.
	 * 
	 * @param pollingInterval
	 *            Polling interval in milli seconds.
	 */
	public PureJavaFileSystemWatcher(File file, String wildCard,
			long pollingInterval) {
		super(file, wildCard);
		long modifiedTime = file.exists() ? file.lastModified() : -1;
		lastModifiedTime = modifiedTime;
		if (this.file.exists()&&file.canRead()) {
			final File[] files;
			if(wildCard==null){
				if(file.isDirectory())
					files =this.file.listFiles();
				else
					files = new File[]{file};
			}else
				if(file.isDirectory())
					files =this.file.listFiles((FileFilter) new WildcardFileFilter(wildCardString,IOCase.INSENSITIVE));
				else
					throw new IllegalStateException("Cannot use a wildcard with aplain file");

			for (int i = 0; i < files.length; i++) {
				filesMap.put(files[i].getName(), new Long(files[i]
						.lastModified()));// name -- last modified

			}
		}
		else
			throw new IllegalArgumentException("Input File not valid!");

		this.workers = new Thread[] { new PureJavaMonitorNotifier(
				pollingInterval) };
		this.wildCardString = wildCard;
		((PureJavaMonitorNotifier) this.workers[0]).setDaemon(true);
	}

	public FileSystemMonitorSPI getSPI() {
		return new PureJavaFileSystemWatcherSPI();
	}

	/**
	 * This is the timer thread which is executed every n milliseconds according
	 * to the setting of the file monitor. It investigates the file in question
	 * and notify listeners if changed.
	 * 
	 * @author Simone Giannecchini
	 * 
	 */
	protected void checkFileSystem() {
	
		synchronized (filesMap) {
	
			// //
			//
			// If we do not have anything to watch let's proceed.
			//
			// //
			if (file == null)
				return;
	
			// //
			//
			// Get the newest modified time for this directoy.
			//
			// //
			final long newModifiedTime = file.exists() ? file.lastModified(): -1;
	
			// //
			//
			// In case I had some changes in the modified time for the
			// watched directory I start all the needed controls.
			//
			// Either the directory has been removed or it has been added or
			// same changes happened below it. We start with looking for the
			// first two conditions that are, the directory has been removed
			// or created.
			//
			//
			// In the rest we look at all the children of this directory to
			// see if something changed
			//
			// //
			if (newModifiedTime != lastModifiedTime) {
	
				// //
				//
				// Directory removed
				//
				// //
				if (newModifiedTime == -1)
					sendEvent(new FileSystemMonitorEvent(file,
							FileSystemMonitorNotifications.DIR_REMOVED));
				// //
				//
				// Directory created
				//
				// //
				else if (lastModifiedTime == -1)
					sendEvent(new FileSystemMonitorEvent(file,
							FileSystemMonitorNotifications.DIR_CREATED));
				else {
					// //
					//
					// Directory modified, let's look for changes in the
					// children
					//
					// //
					// prefix for the files
					final String prefix = new StringBuffer(file
							.getAbsolutePath().trim()).append(File.separator)
							.toString();
	
					// get updated file list
					final String[] newFiles = wildCardString != null ? file
							.list((FilenameFilter) new WildcardFileFilter(wildCardString,IOCase.INSENSITIVE)) : file.list();
					// converting to a list
					final List<String> newFilesList = new ArrayList<String>();
					newFilesList.addAll(Arrays.asList(newFiles));
	
					// //
					//
					// First look for removed and modified by looping
					// through the old list. If I do not find some of the
					// old ones in the newest I assume they have been
					// removed, if I find them I have to check if they have
					// been modified by looking at the last modified.
					//
					// It is important to remove the found files from the
					// updated list in order to have at the end the list of
					// newly created files.
					//
					// //
					String oldFileName;
					// this has to be used in a synch environment
					final Set<String> keySet = filesMap.keySet();
					Iterator<String> it = keySet.iterator();
					File file;
					long newLastModified;
					// this is going to old the values to be added to the
					// back end list.
					final TreeMap<String, Long> newValues = new TreeMap<String, Long>();
					while (it.hasNext()) {
						// modified ar removed
						oldFileName = it.next();
						file = new File(new StringBuffer(prefix).append(oldFileName).toString());
	
						// //
						//
						// If the new list does not contain the old file
						// name it means that the file has been removed,
						// hence we have to remove it from the old list and
						// notify the listeners.
						//
						// If the new list contains the old file name I have
						// to check if it has been modified. In such case I
						// have to update the modified time.
						//
						// //
						if (newFilesList.contains(oldFileName)) {
							// remove it from the new files list
							newFilesList.remove(oldFileName);
	
							// change modification time if it has
							// changed. It is worth to note that I CANNOT
							// modify the list while the Iterator is
							// iterating through the key set therefore I
							// have to remove the key,value pair and add it
							// to the new files list.
							newLastModified = file.lastModified();
							if (newLastModified != filesMap.get(oldFileName).longValue()) {
	
								it.remove();
								newValues.put(oldFileName, new Long(newLastModified));
	
								// event
								sendEvent(new FileSystemMonitorEvent(file,FileSystemMonitorNotifications.FILE_MODIFIED));
							}
						} else {
							// the file has been removed
							// remove it
							it.remove();
	
							// removed
							sendEvent(new FileSystemMonitorEvent(file,FileSystemMonitorNotifications.FILE_REMOVED));
						}
	
					}
	
					// //
					//
					// Now the files that are still in the new list have
					// been created
					//
					// //
					it = newFilesList.iterator();
					String newFileName;
					while (it.hasNext()) {
	
						// get the name
						newFileName = it.next();
						// create a file
						file = new File(new StringBuffer(prefix).append(newFileName).toString());
	
						// add it
						newValues.put(newFileName,new Long(file.lastModified()));
	
						// fire event
						sendEvent(new FileSystemMonitorEvent(file,FileSystemMonitorNotifications.FILE_ADDED));
					}
					// add them all
					filesMap.putAll(newValues);
	
				}
				// Register new modified time for the enclosing dir
				lastModifiedTime = newModifiedTime;
			}
		}
	}

	public synchronized void dispose() {
		stop();
		for (int i = 0; i < workers.length; i++) {
			((AbstractPausableThread) this.workers[i]).requestTermination();
			((AbstractPausableThread) this.workers[i]).dispose();
			this.workers[i] = null;
		}
	
		this.workers = null;
	
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
		filesMap.clear();
		listeners = null;
	}

	/**
	 * 
	 */
	public synchronized boolean isPaused() {
		if (this.workers != null) {
			for (int i = 0; i < workers.length; i++) {
				if (this.workers[i] != null
						&& ((AbstractPausableThread) this.workers[i]).isPaused()) {
					return true;
				}
			}
	
		}
		return false;
	}

	/**
	 * Check if the watcher is up and running.
	 */
	public synchronized boolean isRunning() {
		boolean isAlive = true;
		if (this.workers != null) {
			for (int i = 0; i < workers.length; i++) {
				final Thread observer = ((Thread) this.workers[i]);
				isAlive = isAlive && observer != null && observer.isAlive();
			}
		} else
			isAlive = false;
	
		return this.workers != null && isAlive;
	}

	/**
	 * 
	 */
	public synchronized void pause() {
		if (this.workers != null) {
			for (int i = 0; i < workers.length; i++) {
				if (this.workers[i] != null) {
					((AbstractPausableThread) this.workers[i])
							.setPauseRequested(true);
				}
			}
	
		}
	
	}

	/**
	 * 
	 */
	public synchronized void reset() {
		if (this.workers != null) {
			for (int i = 0; i < workers.length; i++) {
				if (this.workers[i] != null) {
					((AbstractPausableThread) this.workers[i]).setPaused(false);
					((AbstractPausableThread) this.workers[i])
							.setTerminationRequested(false);
					((AbstractPausableThread) this.workers[i]).setPaused(false);
				}
			}
	
		}
	
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
		if(isRunning())
			return ;
		for (int i = 0; i < workers.length; i++) {
			if (workers[i] != null) {
				this.workers[i].start();
			}
		}
	
	}

	/**
	 * Stops watching changes in the directory.
	 */
	public synchronized void stop() {
		if (this.workers != null) {
			for (int i = 0; i < workers.length; i++) {
				((AbstractPausableThread) this.workers[i]).requestTermination();
			}
	
		}
	}


}
