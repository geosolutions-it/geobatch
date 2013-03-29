/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.io.file;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileCleaningTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FileCleaningTracker}
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public final class FileGarbageCollector {
    private static final Logger LOGGER=LoggerFactory.getLogger(FileGarbageCollector.class);
    private static final int LOCK_WAIT_TIME=1000;
    private static final Lock lock =new ReentrantLock();
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
                if (LOGGER.isErrorEnabled()){
                    LOGGER.error(message.toString());
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
