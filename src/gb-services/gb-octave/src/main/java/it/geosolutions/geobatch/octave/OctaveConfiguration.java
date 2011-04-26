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
package it.geosolutions.geobatch.octave;

import it.geosolutions.geobatch.tools.system.Property;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * This class is used to define most of the values needed to handle
 * the octave multiprocess platform.
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
@XStreamAlias("OctaveConfiguration")
@XStreamInclude({OctaveFunctionFile.class})
public class OctaveConfiguration {
    
    private static Lock l=new ReentrantLock(true);
    
    private static OctaveConfiguration singleton=null;
    
    private final static Logger LOGGER = LoggerFactory.getLogger(OctaveConfiguration.class);
    
    private static int timeToWait = 100*60; // in seconds == 100 min
    
    private static int executionQueueSize=100;
    
    // Working directory
    private static String workingDirectory=null;
    
    private static ExecutorService executorService=null;
    
    private static int processors=0;
    
    static {   
        // init statically configured variables
        init();
    }
        
    
    public static OctaveConfiguration getOctaveConfiguration(String workingdir, ExecutorService es){
        if (singleton==null){
            try {
                l.tryLock(OctaveConfiguration.getTimeToWait(),TimeUnit.SECONDS);
                if (singleton==null){
                    singleton=new OctaveConfiguration(workingdir, es);
                }
            } catch (InterruptedException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(e.getLocalizedMessage());
            }
            finally {
                l.unlock();
            }
        }
        return singleton;
    }
    
    private static void init(){
        /*
         * configuring number of processors
         */
        
        Integer p=null;
        String property="OctaveConfiguration.processors";
        try {
            p=Property.getIntProperty(property);
        }
        catch (NullPointerException npe){
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(property+": "+npe.getLocalizedMessage());
        }
        if (p!=null){
            processors=p;
        }
        if (processors<=0){
            Runtime r=Runtime.getRuntime();
            processors=r.availableProcessors();
        }
        
        if (LOGGER.isInfoEnabled())
            LOGGER.info(property+": "+processors);
//System.out.println("OctaveConfiguration.processors: "+processors);
        /*
         * configuring ExecutionQueueSize
         */
        p=null;
        property="OctaveConfiguration.executionQueueSize";
        try {
            p=Property.getIntProperty(property);        
        }
        catch (NullPointerException npe){
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(property+" :"+npe.getLocalizedMessage());
        }
        if (p!=null){
            executionQueueSize=p;
        }
        if (LOGGER.isInfoEnabled())
            LOGGER.info(property+": "+executionQueueSize);
        
//System.out.println("OctaveConfiguration.executionQueueSize: "+executionQueueSize);
        
        /*
         * configuring TIME_TO_WAIT
         * time in seconds used for various lock.tryToLock
         */
        p=null;
        property="OctaveConfiguration.timeToWait";
        try {
            p=Property.getIntProperty(property);        
        }
        catch (NullPointerException npe){
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(property+": "+npe.getLocalizedMessage());
        }
        if (p!=null){
            timeToWait=p;
        }

        if (LOGGER.isInfoEnabled())
            LOGGER.info(property+": "+timeToWait);
        /*
         * configuring workingDirectory
         * time in seconds used for various lock.tryToLock
         */
        property="OctaveConfiguration.workingDirectory";
        String arg=System.getProperty(property);
        if (arg!=null){
                workingDirectory=arg;
        }
        else
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(property+" actually not set.");
        
        if (LOGGER.isInfoEnabled())
            LOGGER.info(property+": "+workingDirectory);
    }

    /**
     * Constr
     * @param workingdir
     * @param es
     */
    private OctaveConfiguration(String workingdir, ExecutorService es) {
        if (workingdir!=null){
            if (workingDirectory!=null){
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("OctaveConfiguration.workingDirectory is set by command line argument\n" +
                    		"this will override the code set.\n No changes will be applied.\n" +
                    		"Working dir: "+workingDirectory);
            }
            else
                setWorkingDirectory(workingdir);
        }
        else {
            if (workingDirectory==null){
                String message="OctaveConfiguration.workingDirectory is not set by command line nor by argument";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new NullPointerException(message);
            }
        }
        if (executorService==null){
            if (es!=null){
                executorService=es;
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("OctaveConfiguration.executorService is set.");
            }
            else {
                String message="OctaveConfiguration.executorService can't be null";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new NullPointerException(message);
            }
        }
        else {
            if (executorService.isShutdown()){
                if (es!=null){
                    executorService=es;
                    if (LOGGER.isInfoEnabled())
                        LOGGER.info("OctaveConfiguration.executorService is set.");
                }
                else {
                    String message="OctaveConfiguration.executorService can't be null";
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(message);
                    throw new NullPointerException(message);
                }
            }
            else
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("OctaveConfiguration.executorService is already set.\nNo modifications are performed.");
        }
    }
    
    public final static int getTimeToWait(){
        return timeToWait;
    }
    
    public final static int getProcessors(){
        return processors;
    }
    
    public final static int getExecutionQueueSize(){
        return executionQueueSize;
    }
    
    public final static String getWorkingDirectory() {
        return workingDirectory;
    }
    
    public final ExecutorService getExecutorService(){
        return executorService;
    }

    public void setWorkingDirectory(String workingDir) {
        if (workingDirectory!=null){
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("OctaveConfiguration.workingDirectory: overriding working dir");
        }
        else {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("OctaveConfiguration.workingDirectory: setting working dir");
        }
        workingDirectory = workingDir;
    }
    
    
}
