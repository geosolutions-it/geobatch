package it.geosolutions.geobatch.tools.file;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileCleaningTracker;

public class FileGarbageCollector {
    private static Logger LOGGER=Logger.getAnonymousLogger(FileGarbageCollector.class.toString());
    private static int LOCK_WAIT_TIME=1000;
    private static Lock lock =new ReentrantLock();
    private static FileCleaningTracker singleton=null;
    
    /**
     * Return a initialized instance of this singleton
     * @return initialized instance of this singleton
     * @throws InterruptedException - if Unable to get the lock
     */
    public FileCleaningTracker getFileGarbageCollector() throws InterruptedException{
        if (singleton==null){
            try{
                lock.tryLock(LOCK_WAIT_TIME, TimeUnit.MILLISECONDS);
                build(singleton);
            }
            catch (InterruptedException ie){
                StringBuilder message=new StringBuilder("Unable to get lock on the ");
                message.append(FileGarbageCollector.class.toString());
                message.append(" message: "+ie.getLocalizedMessage());
                if (LOGGER.isLoggable(Level.SEVERE)){
                    LOGGER.severe(message.toString());
                }
                throw ie;
            }
            finally{
                lock.unlock();
            }
        }
        return singleton;
    }

    private void build(FileCleaningTracker s){
        s=new FileCleaningTracker();
    }
}
