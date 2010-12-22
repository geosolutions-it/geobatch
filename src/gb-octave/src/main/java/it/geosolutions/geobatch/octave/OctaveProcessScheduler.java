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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class OctaveProcessScheduler implements Runnable{
    private final static Logger LOGGER = Logger.getLogger(OctaveProcessScheduler.class.toString());
    
    private static Lock l=new ReentrantLock(true);
    
    private static OctaveProcessScheduler singleton=null;
    
    private List<Engine> engineList=null;
    
    private static OctaveConfiguration octaveConfiguration=null;
    
//    private static boolean initted=false;
    
    /**
     * Private constructor needed to implement
     * singleton pattern
     */
    private OctaveProcessScheduler(){
        /**
         * this is here to make you able to set the OctaveConfiguration.processors
         * (by code) before instantiate the ProcessScheduler
         */
        engineList=new ArrayList<Engine>(OctaveConfiguration.getProcessors());
    }
    
    /**
     * get the process scheduler.
     * @param es the executorService to use, can be null.
     * @return
     */
    protected static OctaveProcessScheduler getOctaveProcessScheduler(ExecutorService es)
        throws NullPointerException
        {
        if (singleton==null){
            try {
                l.tryLock(OctaveConfiguration.getTimeToWait(),TimeUnit.SECONDS);
                if (singleton==null){
                    singleton=new OctaveProcessScheduler();
                    if (es!=null){
                        es.submit(singleton);
                    }
                    else {
                        String message="OctaveProcessScheduler: unable to start using a null ExecutorService";
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.info(message);
                        throw new NullPointerException(message);
//                        Thread t=new Thread(singleton);
//System.out.println("OctaveProcessScheduler STARTING THREAD_ID:"+t.getId());
//                        t.setDaemon(true);
//                        t.start();
                    }
//                    initted=true;
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("OctaveProcessScheduler is up and running");
// DEBUG
//System.out.println("OctaveProcessScheduler is up and running");
                }
            } catch (InterruptedException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe(e.getLocalizedMessage());
            }
            finally {
                l.unlock();
            }
        }
        return singleton;
    }
    

    protected Engine getEngine()
        throws InterruptedException
    {
        if (singleton==null) //initted
            getOctaveProcessScheduler(octaveConfiguration.getExecutorService());
        
        Engine eng=null;
        synchronized (singleton.engineList) {
            if (singleton.engineList.isEmpty()){
                eng=new Engine();
                singleton.engineList.add(eng);
            }
            else {
                Iterator<Engine> i=singleton.engineList.iterator();
                int load=0,minLoad=Integer.MAX_VALUE;
                while (i.hasNext()){
                    Engine next_eng=i.next();
                    load=next_eng.getLoad();
                    if (minLoad>load){
                        eng=next_eng;
                        minLoad=load=eng.getLoad();
                    }
                }
                /*
                 * If the load is >0 try to add a new engine
                 */
                if (load>0 && 
                        singleton.engineList.size()<OctaveConfiguration.getProcessors()){
                    eng=new Engine();
                    singleton.engineList.add(eng);
                }
            }   
        } // synchronized
        return eng;
    }
    
    protected OctaveExecutor getProcessor(OctaveEnv<OctaveExecutableSheet> env)
            throws InterruptedException
    {
        return new OctaveExecutor(env,getEngine());
    }


    public void run() {
        boolean notExit=true;
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("OctaveProcessScheduler starting up...");
        while (notExit){
            try {
// DEBUG
//System.out.println("Sleeping");
                Thread.sleep(10000);
// DEBUG
//System.out.println("Starting");
            } catch (InterruptedException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe(e.getLocalizedMessage());
            }

            synchronized (engineList) {
//                Iterator<Engine> i=engineList.iterator();
                int load=0;
                int size=engineList.size();
                while (size>0){//i.hasNext()){
                    Engine next_eng=engineList.get(--size);//i.next();
                    load=next_eng.getLoad();
                    if (load==0){
                        engineList.remove(next_eng);
                        next_eng.close();
// DEBUG
//System.out.println("PS_Removing engine");
                        next_eng=null;
                    }
                }
                /*
                 * TODO if we found a load >1
                 * should be better to migrate the job
                 * to a lower load process...
                 */
                if (engineList.size()==0){
// DEBUG
//System.out.println("PS_starting Shutdown");
                    notExit=false;
//                    initted=false;
                    singleton=null;
                }
            } // sync on engineList
        } // while noExit
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("OctaveProcessScheduler shutdown...");
// DEBUG
//System.out.println("PS_Shutdown done, bye!");
    }

}
