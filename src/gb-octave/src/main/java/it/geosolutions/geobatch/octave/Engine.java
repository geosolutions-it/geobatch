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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveObject;

/**
 * This is a primitive Octave Engine Layer
 * 
 * @author carlo cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class Engine{
    
    private final static int TIME_TO_WAIT = 100*60; // in seconds == 100 min
    
    private final static Logger LOGGER = Logger.getLogger(Engine.class.toString());

    private Lock lock=null;
    
    private OctaveEngine engine=null;
    
    // load of this engine
    private AtomicInteger load=null;
    
    // list of object returned by the sheets executed by this engine
    private List<OctaveObject> env=null;

    /**
     * Constructor
     */
    public Engine()throws InterruptedException{
        /*
         * The constructor for this class accepts an optional fairness parameter.
         * When set true, under contention, locks favor granting access to the 
         * longest-waiting thread. Otherwise this lock does not guarantee any 
         * particular access order.
         */
        lock=new ReentrantLock(true);
        
        /*
         * try to start the engine
         */
        if (init()<0){
            throw new InterruptedException("Unable to initialize the Octave Egine");
        }
        else {
            /*
             * initialize env
             */
            env=new ArrayList<OctaveObject>();
        }
        // setting load
        load=new AtomicInteger(0);
        
    }
    
    /**
     * try to (re)start the engine
     * @note this will use the 'dk.ange' package to run a new Octave process.
     */
    protected int init() throws InterruptedException{
        if (engine==null){
            try{
                lock.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
                if (engine==null){
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("Starting octave engine");
                    engine=new OctaveEngineFactory().getScriptEngine();
                }
            }catch (InterruptedException ie){
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe(ie.getLocalizedMessage());
                throw ie;
            }
            finally{
                lock.unlock();
            }
        }
        return 1;
    }
    
    protected void close(){
        if (engine!=null){
            try{
                lock.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
                if (engine!=null) {
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("Stopping octave engine");
                    engine.close();
                    engine=null;
                }
                
            }catch (InterruptedException ie){
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe(ie.getLocalizedMessage());
            }
            finally{
                lock.unlock();
            }
        }
    }
    
    /**
     * Execute the 'run' command into the engine
     * if it is up.
     * @param run
     */
    private void eval(String run){
        if (engine!=null){
            try{
                lock.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
                if (engine!=null)
                    engine.eval(run);
                else
                    throw new OctaveEvalException(
                            "Unable to use this engine, try to run start() before use it");
            }catch (InterruptedException ie){
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe(ie.getLocalizedMessage());
            }
            finally{
                lock.unlock();
            }
        }
        else {
            throw new OctaveEvalException(
                    "Unable to use this engine, try to run start() before use it");
        }
    }
    
    public int getLoad(){
        return load.get();
    }
    
    /**
     * 
     * @param sheet the Executable sheet to execute.
     * @param clear if true all the definitions are cleared from 
     * the octave environment after commands execution
     * @return an integer:
     * 	==0 exit is called
     * 	>0 normal execution
     * 	<0 error occurred
     * @throws Exception 
     * @note It will not be modified so you can:
     * -get returns variables using sheet.getReturns()<br>
     * -check/clear definitions using sheet.getDefinitions()<br>
     * -check execution status sheet.getCommands().get(X).isExecuted()<br>
     * 
     * @note: this method runs the sheet in one shot... 
     * this is due to the possibility to change dir of the context using the 'cd' command.  
     */
    public int exec(OctaveExecutableSheet sheet, boolean clear) throws Exception{
        try{
            if (engine==null)
                init();
            lock.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
            // increase the load of this engine
            load.incrementAndGet();
/*
 * USEFUL FOR DEBUG
if (sheet.getDefinitions().size()==2){
    List<SerializableOctaveObject<?>> ret=sheet.getDefinitions();
    System.out.println(((OctaveString) ret.get(0).getOctObj()).getString());
    System.out.println(((OctaveString) ret.get(1).getOctObj()).getString());}
 */          
         // index
            int index=0;
            // list of commands to execute
            List<OctaveCommand> oc=sheet.getCommands();
            // check commands existence
            if (oc==null){
/*
 * TODO: check
 * NOTE this may result in an undesirable behavior since we may want to
 * use a sheet to set octave environment variable without executing commands:
 * f.e.: A=zeros(1:10);
 */
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.finer("Octave sheet do not contains commands...");
                return -1;
            }
            
            // put definitions into octave environment
            if (sheet.hasDefinitions()){
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.finer("Octave transferring definitions to Octave environment...");
                this.put(sheet.getDefinitions());
/*
 * USEFUL FOR DEBUG
if (sheet.getDefinitions().size()==2){
    List<SerializableOctaveObject<?>> ret=sheet.getDefinitions();
    this.get(ret);
    System.out.println(((OctaveString) ret.get(0).getOctObj()).getString());
    System.out.println(((OctaveString) ret.get(1).getOctObj()).getString());}
    */
            }
            
            // run commands
            while (index<oc.size()){
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Octave extracting new command to execute...");
                
                // extract
                OctaveCommand comm=oc.get(index++);
                
                // check command existence
                if (comm!=null){
                    if (!comm.isExecuted()){
                        String command=comm.getCommand();
                        // check command string
                        if (command.equalsIgnoreCase("")){
                            if (LOGGER.isLoggable(Level.WARNING))
                                LOGGER.warning("Octave cannot execute an empty or a \'quit\' command...");
                            continue;
                        }
                        else if  (command.equalsIgnoreCase("quit")){
                            this.eval(command);
                            comm.set();
                        	return 0;
                        }
                        else {
                            // evaluate commands (f.e.: source files)                        
                            if (LOGGER.isLoggable(Level.INFO)){
                                LOGGER.info(
                                        "Octave process is running command: \""+command+
                                        "\" from sheet: "+sheet.getName());
                            }
                            
                            // evaluate command  
                            this.eval(command);
/*
 * USEFUL FOR DEBUG
    if (sheet.getDefinitions().size()==2){
    List<SerializableOctaveObject<?>> ret=sheet.getDefinitions();
    this.get(ret);
    System.out.println(((OctaveString) ret.get(0).getOctObj()).getString());
    System.out.println(((OctaveString) ret.get(1).getOctObj()).getString());}
*/
                            // set command as executed
                            comm.set();
                        } // else
                    } // if !isExecuted
                } // if comm!=null
            }
            
            //all the command in this sheet are executed
            sheet.setExecuted(true);
            
            /*
             * get returning values and put results
             * into the (local to java) engine env
             */
            if (sheet.hasReturns()){
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.finer(
                        "Extracting returning values");
                env.addAll(this.get(sheet.getReturns()));
            }
            
            // clear sheet environment
            if (clear && sheet.hasDefinitions()){
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.finer(
                        "Clear definitions from the Octave transferring definitions to Octave environment...");
                this.clear(sheet.getDefinitions());
            }
            
        }catch (InterruptedException ie){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(ie.getLocalizedMessage());
        }
        finally{
            load.decrementAndGet();
            lock.unlock();
        }
        return 1; // normal condition
    }
    
    /**
     * Return the env contents erasing it.
     * @return the env contents erasing it.
     */
    protected List<OctaveObject> getResults(){
        synchronized (env) {
            List<OctaveObject> results=null;
            if (this.env!=null){
                results=new ArrayList<OctaveObject>();
                int size=env.size();
                while (size>0){
                    OctaveObject o=env.remove(size--);
                    results.add(o);
                }
            }
            return results;   
        }
    }
    
    /**
     * Check existance of a function into the octave environment
     * @note this may depend on the actual engine environment path
     * and may vary depending on which addpath() command is added
     * to the execution chain
     * @param _f a string representing the function to check
     * @return true if function is found (usable), false otherwise
     */
    protected boolean isRunnable(String _f){
        OctaveDouble r=null;
        try {
            if (engine==null)
                init();
            lock.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.finer(
                    "Checking existance of function \""+_f+"\" in the octaev environment...");
            /**
             * Built-in Function: exist (function)
             * Return:
             * 1 - if the function exists as a variable
             * 2 - if the function (after appending `.m') exists in the path.
             * 0 - Otherwise.
             */
            engine.eval("ret=exist(\'"+_f+"\');");
            r=engine.get(OctaveDouble.class,"ret");
        }
        catch (InterruptedException ie){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(ie.getLocalizedMessage());
            return false;
        }
        finally{
            lock.unlock();
        }
        if (r!=null){
            if (r.get(1)==1){
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.finer("Checked function is a variable");
                return false;
            }
            else if (r.get(1)==2){
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.finer("Checked function exists");
                return true;
            }
            else {
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.finer("Checked function NOT exists");
                return false;
            }
        }
        else {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Unable to get returning value from octave engine.");
            return false;
        }
    }
    
    /**
     * clear passed vector of definitions from environment
     * @param defs
     * @throws Exception 
     */
    protected void clear(List<SerializableOctaveObject<?>> defs) throws Exception{
        int size=defs.size();
        for (int i=0; i<size;i++){
            clear(defs.get(i));
        }
    }
    
    /**
     * clear passed definition from environment
     * @param def
     * @throws Exception 
     */
    protected void clear(SerializableOctaveObject<?> def) throws Exception{
        if (engine==null)
            throw new Exception("Engine is not up");
        try{
            lock.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
            this.eval("clear \'"+def.getName()+"\';");
        }
        catch (InterruptedException ie){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(ie.getLocalizedMessage());
        }
        finally{
            lock.unlock();
        }
            
        /*
        synchronized (def) {
// TODO sync the local variable???
            def.setOctObj(null);
            def.setVal();
        }*/
    }
    
    /**
     * (variable definition)
     * @param list
     */
    protected void put(List<SerializableOctaveObject<?>> list){
        try{
            lock.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
            if (engine==null)
                init();
            // fill in serialized values into octave (variable definition)
            int size =list.size();
            int i=0;
            while (i<size){
                SerializableOctaveObject<?> soo=list.get(i++);
                synchronized (soo) {
                    soo.setVal();   
                }
                    engine.put(soo.getName(),soo.getOctObj());
            }
        }
        catch (InterruptedException ie){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(ie.getLocalizedMessage());
        }
        finally{
            lock.unlock();
        }
    }
    
    /**
     * reading variables from octave env
     * @param list
     * @throws Exception 
     */
    protected List<OctaveObject> get(List<SerializableOctaveObject<?>> list) throws Exception{
        if (engine==null)
            throw new Exception("Engine is not running");
        // returning
        List<OctaveObject> ret=new ArrayList<OctaveObject>();
        try{
            lock.tryLock(TIME_TO_WAIT, TimeUnit.SECONDS);
            int size =list.size();
            int i=0;
            // store results
            while (i<size){
                SerializableOctaveObject<?> soo=list.get(i++);
                ret.add(soo.get(engine));
            }
        }
        catch (InterruptedException ie){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(ie.getLocalizedMessage());
        }
        finally{
            lock.unlock();
        }
        return ret;
    }
}
