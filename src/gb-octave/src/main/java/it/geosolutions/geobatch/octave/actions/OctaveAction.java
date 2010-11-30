package it.geosolutions.geobatch.octave.actions;

import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.octave.Engine;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.SheetPreprocessor;

import java.util.EventObject;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ange.octave.exception.OctaveEvalException;

public abstract class OctaveAction<T extends EventObject> extends BaseAction<T> implements Action<T> {
       
    private final static Logger LOGGER = Logger.getLogger(OctaveAction.class.toString());
    
    private volatile OctaveEnv<OctaveExecutableSheet>  env;
    
//TODO cheange this
    protected final static Engine engine=new Engine();
    
    protected final SheetPreprocessor preprocessor=new SheetPreprocessor();
    
    protected final OctaveActionConfiguration config;
    
    @SuppressWarnings("unchecked")
    public OctaveAction(OctaveActionConfiguration actionConfiguration) {
        super(actionConfiguration);
        env=(OctaveEnv<OctaveExecutableSheet>)actionConfiguration.getEnv().clone();
        config=actionConfiguration;
        
     // TODO implement a M.D.POJO
        //run thread manually
        //if (!oct.isAlive())
        //    oct.start();
    }
    
    /**
     * This method should add all the needed preprocessors to the preprocessors map
     * @param events
     * @return
     */
    public abstract Queue<T> load(Queue<T> events) throws ActionException;
    
    /**
     * Action to execute on the FileSystemMonitorEvent event queue.
     * 
     * @param Queue<FileSystemMonitorEvent> queue of events to handle in this (and next)
     * action executions.
     * @return Queue<FileSystemMonitorEvent> the resulting list of events
     */
    public Queue<T> execute(Queue<T> events)
            throws ActionException {
        try {
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Executing Octave script...");
            
            /**
             * here all the events are processed and preprocess map
             * is build.
             */
            events=load(events);
            
            if (events!=null){
                              
                /**
                 * try to preprocess the OctaveFunctionSheet
                 * this operation should transform all the OctaveFunction stored into the env
                 * into OctaveExecutableSheet which can be freely executed by the Octave Engine.class
                 * @note each sheet is executed atomically so be careful with the 'cd' command
                 * (which change context dir) or other commands like so.    
                 */
                try {
                    preprocessor.preprocess(env);
                }catch (Exception e){
                    String message="Exception during buildFunction:"+e.getMessage();
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.warning(message);
                    throw new ActionException(this,message);
                }
                
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Passing Octave sheet to Octave process... ");
                
// TODO ACTUALLY we temporarily skip the THREAD OR JMPOJO interface.
                int size=env.size();
                int index=0;
                while (index<size){
                    /**
                     * exec is synchronized and can be called
                     * without warnings
                     */
                    engine.exec(env.getSheet(index++), true);
                }

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
            throw new ActionException(this,"Unable to run octave script:\n"
                    +e.getLocalizedMessage());
        }
        return events;
    }
    
}
