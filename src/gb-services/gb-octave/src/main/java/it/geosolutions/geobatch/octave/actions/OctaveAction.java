package it.geosolutions.geobatch.octave.actions;

import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.octave.OctaveConfiguration;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveManager;
import it.geosolutions.geobatch.octave.SheetPreprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.EventObject;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import dk.ange.octave.exception.OctaveEvalException;

public abstract class OctaveAction<T extends EventObject> extends BaseAction<T> implements Action<T> {
       
    private final static Logger LOGGER = Logger.getLogger(OctaveAction.class.toString());
    
    protected final SheetPreprocessor preprocessor=new SheetPreprocessor();
    
    protected final OctaveActionConfiguration config;
    
    public OctaveAction(OctaveActionConfiguration actionConfiguration) {
        super(actionConfiguration);
        /*
        if ((env=actionConfiguration.getEnv())!=null)
            env=(OctaveEnv<OctaveExecutableSheet>)env.clone();
        else
        {
            if(LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Bad configuration, cannot find Octave environment");
        }
        */
        config=actionConfiguration;
    }
    
    /**
     * This method should add all the needed preprocessors to the preprocessors map
     * modifying as needed the event queue.
     * @param events
     * @return events
     */
    public abstract Queue<T> load(Queue<T> events, OctaveEnv<OctaveExecutableSheet> env) throws ActionException;
    
    /**
     * Action to execute on the FileSystemEvent event queue.
     * 
     * @param Queue<FileSystemEvent> queue of events to handle in this (and next)
     * action executions.
     * @return Queue<FileSystemEvent> the resulting list of events
     */
    public Queue<T> execute(Queue<T> events)
            throws ActionException {
        try {
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Executing Octave script...");
            
            if (events!=null){
                XStream stream=null;
                try{
                    // unmarshall the environment to the env
                    stream=new XStream();
                    stream.processAnnotations(OctaveEnv.class);
                }
                catch(InitializationException ie){
                    //InitializationException - in case of an initialization problem
                    String message="InitializationException: Could not initialize the XStream object.\n"
                        +ie.getLocalizedMessage();
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe(message);
                    throw new ActionException(this,message);
                }
                File in_file=null;
                try{
                    in_file=new File(Path.getAbsolutePath(config.getWorkingDirectory())+File.separator+config.getEnv());
                }
                catch(NullPointerException npe){
                    // NullPointerException - If the pathname argument is null
                    String message="NullPointerException: You have to set the execution string in the config file. "
                        +npe.getLocalizedMessage();
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe(message);
                    throw new ActionException(this,message);
                    
                }
                FileReader env_reader=null;
                try {
                    env_reader=new FileReader(in_file);
                }
                catch (FileNotFoundException fnfe){
                    /* 
                     * FileNotFoundException - if the file does not exist, 
                     * is a directory rather than a regular file, 
                     * or for some other reason cannot be opened for reading.
                     */
                    String message="Unable to find the OctaveEnv file: "+fnfe.getMessage();
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe(message);
                    throw new ActionException(this,message);
                }
                OctaveEnv<OctaveExecutableSheet> env=null;
                Object o=null;
                try {
                    if (env_reader!=null && stream!=null){
                        o=stream.fromXML(env_reader);
                        if (o instanceof OctaveEnv<?>)
                            env=(OctaveEnv<OctaveExecutableSheet>) o;
                        else {
                            String message="ClassCastException: Serialized object is not an OctaveEnv object";
                            if (LOGGER.isLoggable(Level.SEVERE))
                                LOGGER.severe(message);
                            throw new ActionException(this,message);
                        }
                    }
                    else {
                        String message="Exception during execute: stream object:"+stream+" env_reader:"+env_reader;
                        if (LOGGER.isLoggable(Level.SEVERE))
                            LOGGER.severe(message);
                        throw new ActionException(this,message);
                    }
                }
                catch(XStreamException xse){
                    // XStreamException - if the object cannot be deserialized
                    String message="XStreamException: Serialized object is not an OctaveEnv object:\n"+xse.getLocalizedMessage();
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe(message);
                    throw new ActionException(this,message);
                }
                catch(ClassCastException cce){
                    // ClassCastException - if the execute string do not point to a OctaveEnv serialized object
                    String message="ClassCastException: Serialized object is not an OctaveEnv object:\n"+cce.getLocalizedMessage();
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe(message);
                    throw new ActionException(this,message);
                }
                
                /**
                 * here all the events are processed and preprocess map
                 * is build.
                 */
                events=load(events,env);
                
                /*
                 * try to preprocess the OctaveFunctionSheet
                 * this operation should transform all the OctaveFunction stored into the env
                 * into OctaveExecutableSheet which can be freely executed by the Octave Engine.class
                 * @note each sheet is executed atomically so be careful with the 'cd' command
                 * (which change context dir) or other commands like so.
                 */
                try {
                    preprocessor.preprocess(env);
                }
                catch (Exception e){
                    String message="Exception during buildFunction:\n"+e.getLocalizedMessage();
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.severe(message);
                    throw new ActionException(this,message);
                }
                
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Passing Octave sheet to Octave process... ");
//TODO set number of the Thread pool or use the Catalog thread pool
                ExecutorService es=Executors.newFixedThreadPool(OctaveConfiguration.getExecutionQueueSize());
                
                // pass to the octave manager a new environment to process
                OctaveManager.process(env,es);

            } // ev==null
            else {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.warning("Resulting a null event queue");
                throw new ActionException(this,"Resulting a null event queue");
            }
            
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Evaluating: DONE");
            
        }
        catch (OctaveEvalException oee){
            throw new ActionException(this,"Unable to run octave script:\n"
                        +oee.getLocalizedMessage());
        }
        catch(Exception e){
// DEBUG
//e.printStackTrace();
            throw new ActionException(this,"Unable to run octave script:\n"
                    +e.getLocalizedMessage());
        }
        return events;
    }
    
}
