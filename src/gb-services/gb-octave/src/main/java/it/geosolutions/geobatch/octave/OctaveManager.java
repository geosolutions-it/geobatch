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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.ange.octave.type.OctaveObject;
/**
 * The Octave manager used to manage octave processes
 * 
 * @author Carlo Cancellieri, ccancellieri AT geo-solutions.it, GeoSolutions
 */
public final class OctaveManager{
    
    private final static Logger LOGGER = LoggerFactory.getLogger(OctaveManager.class.toString());

    private final static int TIME_TO_WAIT = 100*60; // in seconds == 100 min
    
    private final static Lock l=new ReentrantLock();
    
    private boolean notExit=true;
    
    private boolean initted=false;
    
    private static OctaveManager singleton=null;
    
    /**
     * blocking execution queue
     */
    private static ArrayBlockingQueue<OctaveEnv<OctaveExecutableSheet>> inQueue=null;
    
    // ID, List<OctaveObject>
    private static ConcurrentHashMap<Long,Future<List<OctaveObject>>> out=null;
    
    private static OctaveConfiguration octaveConfiguration=null;
    
    private static OctaveProcessScheduler octaveProcessScheduler=null;
    
//    private OctaveProcessScheduler octaveProcessScheduler=null;
    
    /**
     * Getter for an instance of this Manager
     * @note if it is already initialized, passed paramethers are ignored
     * @param configuration
     * @param es the executor service to use
     * @return The singleton of this OctaveManager
     * @throws Exception
     */
    public static OctaveManager getOctaveManager(OctaveConfiguration configuration){
        try {
            if (singleton==null){
                try {
                    l.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
                    if (singleton==null){
                        singleton=new OctaveManager(configuration);
                    }
                }
                catch(InterruptedException ie){
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(ie.getLocalizedMessage());
                    if (singleton!=null)
                        singleton.shutdown();
                }
                finally{
                    l.unlock();
                }
            }
            return singleton;
        }
        catch (IOException ioe){
            if (LOGGER.isErrorEnabled())
                LOGGER.error(ioe.getLocalizedMessage());
            if (singleton!=null)
                singleton=null;
        }
        catch (Exception e){
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage());
            if (singleton!=null)
                singleton=null;
        }
        return null;
    }
    
    /**
     * Constructor
     * @param actionConfiguration configuration for this action.
     * @throws Exception 
     */
    private OctaveManager(OctaveConfiguration configuration) throws Exception {
        if (inQueue==null)
            inQueue=new ArrayBlockingQueue<OctaveEnv<OctaveExecutableSheet>>(OctaveConfiguration.getExecutionQueueSize());
        if (out==null)
            out=new ConcurrentHashMap<Long,Future<List<OctaveObject>>>(OctaveConfiguration.getExecutionQueueSize());
        
        octaveConfiguration = configuration;
        
        octaveProcessScheduler=OctaveProcessScheduler.getOctaveProcessScheduler(octaveConfiguration.getExecutorService());
        
        initted=true;
        notExit=true;
        
        Runnable r=new Runnable() {
            public void run() {
                try {
                    startup();
                }
                finally{
                    if (initted)
                        shutdown();
                }
            }
        };
//        t.setDaemon(true);
        octaveConfiguration.getExecutorService().submit(r);
    }
    
    /**
     * Enqueue an Octave environment for execution waiting for the resulting
     * return.
     * @param env the octave environment to use
     * @return the resulting list of object
     * @throws Exception 
     */
    public static List<OctaveObject> process(OctaveEnv<OctaveExecutableSheet> env, ExecutorService es)
        throws Exception
        {
        if (env!=null){
            // run the call on the right Engine waiting for the response
            return OctaveExecutor.call(env,
                    OctaveProcessScheduler.getOctaveProcessScheduler(es).getEngine());
        }
        else
            throw new NullPointerException("Passed environment to compute is null!");
    }
    
    /**
     * Enqueue an Octave environment for execution waiting for the resulting
     * return.
     * @param env the octave environment to use
     * @return the resulting list of object
     * @throws Exception 
     */
    public Future<List<OctaveObject>> enqueue(OctaveEnv<OctaveExecutableSheet> env)
        throws Exception{
        // add the task to the queue
        inQueue.add(env);

        synchronized (env) {
            // wait for thread (future) run
            env.wait();
        }
        
        return out.remove(env.getUniqueID());
    }
    
    /**
     * Call this method to stop the manager 
     * @throws InterruptedException
     */
    public void shutdown(){
        if (LOGGER.isInfoEnabled())
            LOGGER.info("OctaveManager is shutting down");
        exit();
        //clear(); this is done if exit
    }
    
    /**
     * clear all the state variables resetting 
     * this singleton state
     */
    private void clear(){
        if (LOGGER.isInfoEnabled())
            LOGGER.info("OctaveManager clear");
        
        // resetting initialization flag to false
        initted=false;
        
        // clear the singleton
        if (singleton!=null) {
            synchronized (singleton) {
                if (singleton!=null)
                    singleton=null;
            }
        }
        
        // clear the incoming queue        
        if (inQueue!=null){
            inQueue.clear();
//            inQueue=null;
        }
        // clear outgoing results
        if (out!=null){
            out.clear();
//            out=null;
        }
        if (octaveProcessScheduler!=null){
            // should automatically goes down after some time
            octaveProcessScheduler=null;
        }
        if (octaveConfiguration!=null){
            octaveConfiguration=null;
        }
    }
    
    /**
     * make the thread exits from the cycle
     */
    private void exit(){

        if (LOGGER.isInfoEnabled())
            LOGGER.info("OctaveManager is exiting");
        
        notExit=false; //exit
        /*
         * add fake empty object to the queue to make it start the check
         */
        OctaveEnv<OctaveExecutableSheet> exit_env=new OctaveEnv<OctaveExecutableSheet>();
        try {
            inQueue.put(exit_env);
        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("OctaveManager engine process exit abnormally\n"
                        +e.getLocalizedMessage());
        }
    }
    
    // this is the method to call to start the executorService
    private void startup(){
        OctaveEnv<OctaveExecutableSheet> env=null;
        
        OctaveProcessScheduler octaveProcessScheduler=
            OctaveProcessScheduler.getOctaveProcessScheduler(octaveConfiguration.getExecutorService());
        
        try{
            if (LOGGER.isInfoEnabled())
                LOGGER.info("OctaveManager starting up...");
            //wait for an new object
            while ((env=inQueue.take())!=null && notExit){
                
                OctaveExecutor task = null;
            
                try{
                    //get Asynch task from scheduler
                    task=octaveProcessScheduler.getProcessor(env);
                }
                catch(InterruptedException ie){
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("OctaveManager engine process exit abnormally.\n"
                                +ie.getLocalizedMessage());
                    // octave engine process exit abnormally
                    shutdown();
                }
                
                Future<List<OctaveObject>> result=null;
                try{
                    //wrap using a future task
                    result = octaveConfiguration.getExecutorService().submit(task);
                }
                catch(RejectedExecutionException ree){
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("OctaveManager task cannot be scheduled for execution.\n"
                                +ree.getLocalizedMessage());
                    //if task cannot be scheduled for execution
                    shutdown();
                }
                catch(NullPointerException npe){
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("OctaveManager passed task is null\n"+npe.getLocalizedMessage());
                    //The passed task is null
                    shutdown();
                }
                try {
                // put the task into the map
                out.put(env.getUniqueID(), result);
                }
                catch(NullPointerException npe){
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("OctaveManager the key or value is null\n"+npe.getLocalizedMessage());
                    //NullPointerException - if the key or value is null.
                    shutdown();
                }
                synchronized (env) {
                    // notify the waiting thread
                    env.notify();
                }
            } // while !shutdown()
        }
        catch(InterruptedException ie){
            if (LOGGER.isErrorEnabled())
                LOGGER.error("OctaveManager interrupted while waiting\n"+ie.getLocalizedMessage());
//InterruptedException - if interrupted while waiting
        }
        finally{
            if (initted)
                clear();
        }
    }
}
