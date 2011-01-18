package it.geosolutions.filesystemmonitor.neutral.OLD;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.impl.BaseFileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.thread.AbstractPausableThread;
import it.geosolutions.filesystemmonitor.monitor.thread.AbstractPeriodicThread;

import java.io.File;

import org.quartz.JobExecutionException;

public final class FileSystemWatcher extends BaseFileSystemMonitor {
    
    private final FileSystemScanJob scanner;

    protected Thread worker;
    

    public final static long DEFAULT_POLLING_INTERVAL = 1000;

    /**
     * This is the timer thread which is executed every n milliseconds according to the setting of
     * the file monitor. It investigates the file in question and notify listeners if changed.
     * 
     * @author SImone Giannecchini
     * 
     */
    private class Poller extends AbstractPeriodicThread {

        public Poller(final long pollingInterval) {
            super("PureJavaFSMonitor", pollingInterval);
        }

        public boolean execute() {

            FileSystemWatcher.this.checkFileSystem();

            return true;
        }

        public void dispose() {
        }

    }

    public FileSystemWatcher(File file) {
        this(file, DEFAULT_POLLING_INTERVAL);
    }

    /**
     * Create a file monitor instance with specified polling interval.
     * 
     * @param pollingInterval
     *            Polling interval in milli seconds.extension
     */
    public FileSystemWatcher(File file, long pollingInterval) {
        this(file, null, pollingInterval);
    }

    public FileSystemWatcher(File file, String wildCard) {
        this(file, wildCard, DEFAULT_POLLING_INTERVAL);
    }

    /**
     * Create a file monitor instance with specified polling interval.
     * 
     * @param pollingInterval
     *            Polling interval in milli seconds.
     */
    public FileSystemWatcher(File file, String wildCard, long pollingInterval) {
        super(file, wildCard);
        
        if (this.file.exists() && this.file.canRead()) {

            scanner=new FileSystemScanJob(wildCard,file,consumer);
            
            //TODO FIRST RUN OF THE SCANNER
            try {
                scanner.execute(null);
            } catch (JobExecutionException e) {
// TODO                LOGGER.log(Level.FINER, e.getMessage(), e);
            }

        } else
            throw new IllegalArgumentException("Input File not valid!");

        this.worker =new Poller(pollingInterval) ;
        this.wildCardString = wildCard;
        ((Poller) worker).setDaemon(true);
    }

    public FileSystemMonitorSPI getSPI() {
        return new FileSystemWatcherSPI();
    }

    /**
     * This is the timer thread which is executed every n milliseconds according to the setting of
     * the file monitor. It investigates the file in question and notify listeners if changed.
     * 
     * @author Simone Giannecchini
     * 
     */
    protected void checkFileSystem() {
        try {
            scanner.execute(null); // TODO add the context
        } catch (JobExecutionException e) {
//TODO            LOGGER.log(Level.FINER, e.getMessage(), e);
        }
    }

    public synchronized void dispose() {
        stop();

        ((AbstractPausableThread) worker).requestTermination();
        ((AbstractPausableThread) worker).dispose();
        worker = null;
  


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
        scanner.clear();
        listeners = null;
    }

    /**
	 * 
	 */
    public synchronized boolean isPaused() {
        if (worker != null
                && ((AbstractPausableThread) worker).isPaused()) {
            return true;
        }
        return false;
    }

    /**
     * Check if the watcher is up and running.
     */
    public synchronized boolean isRunning() {
        boolean isAlive = true;
        if (this.worker != null) {
            final Thread observer = ((Thread) worker);
            isAlive = isAlive && observer != null && observer.isAlive();
        } else
            isAlive = false;

        return this.worker != null && isAlive;
    }

    /**
	 * 
	 */
    public synchronized void pause() {
        if (this.worker != null) {
            ((AbstractPausableThread) this.worker).setPauseRequested(true);
        }
    }

    /**
	 * 
	 */
    public synchronized void reset() {
        if (this.worker != null) {
            ((AbstractPausableThread) this.worker).setPaused(false);
            ((AbstractPausableThread) this.worker).setTerminationRequested(false);
            ((AbstractPausableThread) this.worker).setPaused(false);
        }

    }

    /**
     * Starts watching changes in the directory. Note that a worker thread will be started to
     * monitor file system changes, and this method will return immediately. All file system events
     * are fired in the worker thread. The worker thread will run until stop() is invoked if there
     * is no error. In case of error, the worker thread might terminate prematurely. You can use
     * isRunning() to check whether the worker thread is running or not.
     */
    public synchronized void start() {
        if (isRunning())
            return;
        if (worker != null) {
            this.worker.start();
        }

    }

    /**
     * Stops watching changes in the directory.
     */
    public synchronized void stop() {
        if (this.worker != null) {
            ((AbstractPausableThread) this.worker).requestTermination();
        }
    }

}
