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
    public static FileCleaningTracker getFileCleaningTracker(){
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
            }
            finally{
                lock.unlock();
            }
        }
        return singleton;
    }

    private static void build(FileCleaningTracker s){
        s=new FileCleaningTracker();
    }
}
