/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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

import it.geosolutions.geobatch.tools.system.Property;

import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class Configuration {
    private final static Logger LOGGER = Logger.getLogger(Configuration.class.toString());
    
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
    private static int bufferSize;
    
    static {
        /*
         * Extractor.bufferSize
         */
        String property="Extractor.bufferSize";
        try {
            bufferSize=Property.getIntProperty(property);
        }
        catch (NullPointerException e){
            bufferSize=(1024*8*100); // default value
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info(property+": "+bufferSize);
            
        }
    }
    
    public static int getBufferSize(){
        return bufferSize;
    }

}
