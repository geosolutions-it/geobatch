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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ange.octave.type.OctaveObject;
/**
 * The Octave manager used to manage octave processes
 * 
 * @author Carlo Cancellieri, ccancellieri AT geo-solutions.it, GeoSolutions
 */
public final class OctaveManager{
    
    private final static Logger LOGGER = Logger.getLogger(OctaveManager.class.toString());

    private final static int TIME_TO_WAIT = 100*60; // in seconds == 100 min
    
    private final static Lock l=new ReentrantLock();
    
    private static boolean notExit=true;
    
    private static boolean initted=false;
    
    private static OctaveManager singleton=null;

    
    
    /**
     * blocking execution queue
     */
    private static ArrayBlockingQueue<OctaveEnv<OctaveExecutableSheet>> inQueue=null;
    
    // ID, List<OctaveObject>
    private static ConcurrentHashMap<Long,Future<List<OctaveObject>>> out;
    
    private static ExecutorService executorService;
    
    // decide if the executorService should be handled (true) or not
    private static boolean manageService;
    
    
    public static OctaveManager getOctaveManager(OctaveConfiguration configuration) throws Exception {
        try {
            if (singleton==null){
                try {
                    l.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
                    if (singleton==null){
                        singleton=new OctaveManager(configuration);
                        // init the scheduler
                        OctaveProcessScheduler.getOctaveProcessScheduler(null);
                    }
                }
                catch(InterruptedException ie){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe(ie.getLocalizedMessage());
                }
                finally{
                    l.unlock();
                }
            }
            return singleton;
        }
        catch (IOException ioe){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(ioe.getLocalizedMessage());
            if (singleton!=null)
                singleton=null;
        }
        catch (Exception e){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(e.getLocalizedMessage());
            if (singleton!=null)
                singleton=null;
        }
        return null;
    }
    
    /**
     * Getter for an instance of this Manager
     * @note if it is already initialized, passed paramethers are ignored
     * @param configuration
     * @param es the executor service to use
     * @return The singleton of this OctaveManager
     * @throws Exception
     */
    public static OctaveManager getOctaveManager(OctaveConfiguration configuration, ExecutorService es){
        try {
            if (singleton==null){
                try {
                    l.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
                    if (singleton==null){
                        singleton=new OctaveManager(configuration);
                        // init the scheduler
                        OctaveProcessScheduler.getOctaveProcessScheduler(es);
                    }
                }
                catch(InterruptedException ie){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe(ie.getLocalizedMessage());
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
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(ioe.getLocalizedMessage());
            if (singleton!=null)
                singleton=null;
        }
        catch (Exception e){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(e.getLocalizedMessage());
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
        inQueue=new ArrayBlockingQueue<OctaveEnv<OctaveExecutableSheet>>(configuration.getExecutionQueueSize());
        out=new ConcurrentHashMap<Long,Future<List<OctaveObject>>>(configuration.getExecutionQueueSize());
        executorService = Executors.newFixedThreadPool(configuration.getExecutionQueueSize());
        // the service is owned by this object so we should manage it
        manageService=true;
        
        initted=true;
        notExit=true;
        
        Thread t=new Thread(new Runnable() {
            public void run() {
                try {
                    startup();
                }
                finally{
                    if (initted)
                        shutdown();
                }
            }
        });
        t.setDaemon(true);
        executorService.submit(t);
        //t.start();
    }
    
    /**
     * Constructor
     * @param actionConfiguration configuration for this action.
     * @throws Exception 
     */
    private OctaveManager(OctaveConfiguration configuration, ExecutorService es) throws Exception {
        inQueue=new ArrayBlockingQueue<OctaveEnv<OctaveExecutableSheet>>(configuration.getExecutionQueueSize());
        out=new ConcurrentHashMap<Long,Future<List<OctaveObject>>>(configuration.getExecutionQueueSize());
        executorService = es;
        // the service is created externally 
        manageService=false;
        
        initted=true;
        notExit=true;
        
        Thread t=new Thread(new Runnable() {
            public void run() {
                try {
                    startup();
                }
                finally{
                    if (initted)
                        shutdown();
                }
            }
        });
        t.setDaemon(true);
        executorService.submit(t);
    }
    
    /**
     * Enqueue an Octave environment for execution waiting for the resulting
     * return.
     * @param env the octave environment to use
     * @return the resulting list of object
     * @throws Exception 
     */
    public static List<OctaveObject> process(OctaveEnv<OctaveExecutableSheet> env)
        throws Exception{
        // run the call on the right Engine waiting for the response
        return OctaveExecutor.call(env, OctaveProcessScheduler.getEngine());
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
        exit();
        clear();
    }
    
    /**
     * clear all the state variables resetting 
     * this singleton state
     */
    private void clear(){
        initted=false;
        
        if (manageService){
            synchronized (executorService) {
                executorService.shutdown();
                executorService=null;
            }  
        }
        
        synchronized (singleton) {
            singleton=null;
        }
        
        inQueue.clear();
        out.clear();
    }
    
    /**
     * make the thread exits from the cycle
     */
    private void exit(){
        notExit=false; //exit
        /*
         * add fake empty object to the queue to make it start the check
         */
        OctaveEnv<OctaveExecutableSheet> exit_env=new OctaveEnv<OctaveExecutableSheet>();
        try {
            inQueue.put(exit_env);
        } catch (InterruptedException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("OctaveManager engine process exit abnormally\n"
                        +e.getLocalizedMessage());
        }
    }
    
    // this is the method to call to start the executorService
    private void startup(){
        OctaveEnv<OctaveExecutableSheet> env=null;
        try{
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("OctaveManager starting up...");
            //wait for an new object
            while ((env=inQueue.take())!=null && notExit){
                
                OctaveExecutor task = null;
            
                try{
                    //get Asynch task from scheduler
                    task=OctaveProcessScheduler.getProcessor(env);
                }
                catch(InterruptedException ie){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe("OctaveManager engine process exit abnormally\n"
                                +ie.getLocalizedMessage());
                    // octave engine process exit abnormally
                    shutdown();
                }
                
                Future<List<OctaveObject>> result=null;
                try{
                    //wrap using a future task
                    result = executorService.submit(task);
                }
                catch(RejectedExecutionException ree){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe("OctaveManager task cannot be scheduled for execution\n"
                                +ree.getLocalizedMessage());
                    //if task cannot be scheduled for execution
                    shutdown();
                }
                catch(NullPointerException npe){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe("OctaveManager passed task is null\n"+npe.getLocalizedMessage());
                    //The passed task is null
                    shutdown();
                }
                try {
                // put the task into the map
                out.put(env.getUniqueID(), result);
                }
                catch(NullPointerException npe){
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe("OctaveManager the key or value is null\n"+npe.getLocalizedMessage());
                    //NullPointerException - if the key or value is null.
                    shutdown();
                }
                synchronized (env) {
                    // notify the waiting thread
                    env.notify();
                }
            } // while !shutdown()
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("OctaveManager is exiting");
        }
        catch(InterruptedException ie){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("OctaveManager interrupted while waiting\n"
                        +ie.getLocalizedMessage());
//InterruptedException - if interrupted while waiting
        }
        finally{
            clear();
        }
    }
}
