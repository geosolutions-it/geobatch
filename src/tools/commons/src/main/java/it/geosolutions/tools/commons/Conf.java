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
package it.geosolutions.tools.commons;

import it.geosolutions.tools.commons.system.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class Conf {
    private final static Logger LOGGER = LoggerFactory.getLogger(Conf.class.toString());
    
    /**
     * Variables used by FileRemover
     */
// TODO: check (ask to author Simone)
    // Default size of element for {@link FileChannel} based copy method. 
    public static final int DEFAULT_SIZE = 10 * 1024 * 1024;
 // TODO: check (ask to author Simone)    
    public static final long ATOMIC_WAIT = 5000;
// TODO: check (ask to author Simone)
    // 30 seconds is the default period between two checks.
    public final static long DEFAULT_PERIOD = 5L;
// TODO: check (ask to author Simone)
     // The default number of attempts is 50
    public final static int DEF_MAX_ATTEMPTS = 50;
    
    /**
     * Variables used by reader and Extractor classes
     */
    private static int timeToWait = 10*60; // in seconds = 10 min
    
    /**
     * Variables used by reader and Action.tools classes
     */
    private static int bufferSize = (1024*8*100); // default value
    
    static {
        /*
         * Action.tools.bufferSize
         */
        String property="tools.bufferSize";
        try {
            bufferSize=Property.getIntProperty(property);
        }
        catch (NullPointerException e){
            bufferSize=(1024*8*100); // default value
            if (LOGGER.isInfoEnabled())
                LOGGER.info(property+": "+bufferSize);
        }
        
        /*
         * Action.tools.bufferSize
         */
        property="tools.timeToWait";
        try {
            timeToWait=Property.getIntProperty(property);
        }
        catch (NullPointerException e){
            if (LOGGER.isInfoEnabled())
                LOGGER.info(property+": "+timeToWait);   
        }
    }
    
    public static final int getBufferSize(){
        return bufferSize;
    }
    
    public static final int getTimeToWait(){
        return timeToWait;
    }


}
