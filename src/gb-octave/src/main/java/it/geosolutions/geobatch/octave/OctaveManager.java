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

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
public class OctaveManager{

    private final static int TIME_TO_WAIT = 100*60; // in seconds == 100 min
    
    private static OctaveManager singleton=null;
    
    private final static Lock l=new ReentrantLock();

    private final static Logger LOGGER = Logger.getLogger(OctaveManager.class.toString());
    
    /**
     * blocking execution queue
     */
    private ArrayBlockingQueue<OctaveEnv<OctaveExecutableSheet>> inQueue;
    
    // ID, List<OctaveObject>
    private ConcurrentHashMap<Long,Future<List<OctaveObject>>> out;
    
    private ExecutorService executorService;
    
//    /**
//     * never call this method
//     * @deprecated
//     * @return
//     * @throws Exception
//     */
//    public static OctaveManager getOctaveManager() throws Exception {
//        if (singleton==null){
//            throw new Exception("Unable to pass a not initialized object");
//        }
//        return singleton;
//    }
    
    public static OctaveManager getOctaveManager(OctaveConfiguration configuration) throws Exception {
        if (singleton==null){
            try {
                l.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
                if (singleton==null)
                    singleton=new OctaveManager(configuration);
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
    
    public static OctaveManager getOctaveManager(OctaveConfiguration configuration, ExecutorService es) throws Exception {
        if (singleton==null){
            try {
                l.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
                if (singleton==null)
                    singleton=new OctaveManager(configuration);
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
    
    /**
     * Constructor
     * @param actionConfiguration configuration for this action.
     * @throws Exception 
     */
    private OctaveManager(OctaveConfiguration configuration) throws Exception {
        inQueue=new ArrayBlockingQueue<OctaveEnv<OctaveExecutableSheet>>(configuration.getExecutionQueueSize());
        out=new ConcurrentHashMap<Long,Future<List<OctaveObject>>>(configuration.getExecutionQueueSize());
        executorService = Executors.newFixedThreadPool(configuration.getExecutionQueueSize());
        
//TODO add this thread to the master thread pool
        Thread t=new Thread(new Runnable() {
            public void run() {
                try {
                    startup();
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.log(Level.INFO, e.getMessage(), e);
                }
            }
        });
        t.setDaemon(true);
        t.start();
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
//TODO add this thread to the master thread pool
        Thread t=new Thread(new Runnable() {
            
            public void run() {
                try {
                    startup();
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.log(Level.INFO, e.getMessage(), e);
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    /**
     * Enqueue an Octave environment for execution waiting for the resulting
     * returns
     * @param env the octave environment to use
     * @return the resulting list of object
     * @throws InterruptedException if corresponding thread is interrupted 
     * @throws ExecutionException 
     */
    public List<OctaveObject> enqueue(OctaveEnv<OctaveExecutableSheet> env)
        throws InterruptedException, ExecutionException{
        // add the task to the queue
        inQueue.add(env);

        synchronized (env) {
            env.wait();
        }
        Future<List<OctaveObject>> futRet=out.remove(env.getUniqueID());
        if (futRet!=null)
            return futRet.get();
        else
            return null;
    }
    
    /**
     * Call this method to stop the manager 
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException{
        inQueue.put(null);
    }
    
    // this is the method to call to start
    private void startup() throws Exception {
        OctaveEnv<OctaveExecutableSheet> env=null;
        while ((env=inQueue.take())!=null){
            
            //get Asynch task from scheduler
            OctaveExecutor task = OctaveProcessScheduler.getProcessor(env);
            //new OctaveExecutor(env, new Engine());
            
            //wrap using a future task
            Future<List<OctaveObject>> result = executorService.submit(task);
            
            // put the task into the map
            out.put(env.getUniqueID(), result);
            
            synchronized (env) {
                // notify the waiting thread
                env.notify();
            }
        } // while !shutdown()
/*
 * TODO some notify on the inQueue to make shutdown able to
 * set to null all the members. (something like a C++ dtor()).
 * @note: add a inQueue.wait() into shutdown before set to null all the
 * members! 
 */
        // shutdown was called
        executorService.shutdown();
    }
}
