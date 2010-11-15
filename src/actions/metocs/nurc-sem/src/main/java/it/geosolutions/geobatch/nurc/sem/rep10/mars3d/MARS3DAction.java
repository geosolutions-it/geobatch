package it.geosolutions.geobatch.nurc.sem.rep10.mars3d;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.OctaveObject;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.octave.*;


public class MARS3DAction extends BaseAction<FileSystemMonitorEvent> implements Action<FileSystemMonitorEvent> {
    
    private final static Logger LOGGER = Logger.getLogger(MARS3DAction.class.toString());
    // 
    // TODO: check... should we add this member to the BaseAction?
    private final MARS3DActionConfiguration config;
    
    public MARS3DAction(MARS3DActionConfiguration actionConfiguration) {
        super(actionConfiguration);
        
        config=actionConfiguration;
    }
    
    // TODO: FIX ME!!!
    private String buildFileName(){
        // TODO add ABSOLUTE PATH+/workingdir/out/
        return "lscv08_NCOM-Forecast-T" + new Date().getTime()+".nc";
    }
    
    /**
     * Action to execute on the FileSystemMonitorEvent event queue.
     * 
     * @param Queue<FileSystemMonitorEvent> queue of events to handle in this (and next)
     * action executions.
     * @return Queue<FileSystemMonitorEvent> the resulting list of events
     */
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws ActionException {
        try {
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Executing Octave script...");
            

// TODO set as ENVIRONMENT VAR
            
            Iterator<FileSystemMonitorEvent> e=events.iterator();
            
            while (e.hasNext()){
                FileSystemMonitorEvent ev=e.next();
//TODO check if this condition is needed
                if ((ev)==null && LOGGER.isLoggable(Level.WARNING))
                    LOGGER.warning(
                        "Executing Octave script on empty event queue");
                
                /**
                 * getting reference to the first executable sheet
                 * this is supposed to be to only executed sheet
                 * in the configuration.
                 */
                OctaveExecutableSheet es = config.getEnv().getEnv(0);
                /**
                 * get the first variable definition which is supposed
                 * to be the first argument of the function
                 * mars3d(file_in,file_out)
                 */
                SerializableOctaveObject<?> soo=es.getDefinitions().get(0);
                /**
                 * set its name to the incoming event referring file
                 * This will be used by the MARS3DFunctionBuilder to
                 * build the command string to execute
                 */
                soo.setName(ev.getSource().getAbsolutePath());
                /**
                 * get the second variable definition which is supposed
                 * to be the second argument of the function
                 * mars3d(file_in,file_out)
                 */
                soo=es.getDefinitions().get(1);
                /**
                 * set its name to the conventional string obtained by 
                 * buildFileName() method
                 * This will be used by the MARS3DFunctionBuilder to
                 * build the command string to execute
                 */
                String out_name=buildFileName();
                soo.setName(out_name);

                events.add(new FileSystemMonitorEvent(
                new File(out_name),FileSystemMonitorNotifications.FILE_ADDED));

            }
            // script
            String script;
            
            /**
 *TODO: change this cast in the FileBaseCatalog!
             *
             * following is substituted by Catalog.getBaseDirectory();
             * 
             * String data = System.getProperty("GEOBATCH_DATA_DIR");
             * if (data!=null){
             * script="cd \""+data+File.separator+getConfig().getWorkingDirectory()+"\";";
             * }
             * else if ((data = System.getenv("GEOBATCH_DATA_DIR"))!=null){
             * script="cd \""+data+File.separator+getConfig().getWorkingDirectory()+"\";";
             * }
             * else
             * throw new ActionException(this,"GEOBATCH_DATA_DIR not defined");
             * 
TODO             
FileBaseCatalog c=(FileBaseCatalog) CatalogHolder.getCatalog();
c.getBaseDirectory();
            
            // setting script
            script="cd \""+
                    c.getBaseDirectory()+
                    File.separator+
                    getConfig().getWorkingDirectory()+"\";";
            
            
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Entering working dir using command: "+script);
            */
            // Going to local working dir:
            // `cd /local/working/dir`
//            getConfig().getFunction().eval(script);
            
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Evaluating...");
            // executing script
            // replacing input file placeholder with filename
            
//TODO: @note this should be run as thread
//            getConfig().getFunction().run();
            
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Evaluating: DONE");
            
            // closing octave script engine
//            getConfig().getFunction().close();
            
// TODO change this.... how we get output file name?
//            String _of=ev.getSource().getAbsolutePath();
//            if (!ev.getSource().renameTo(new File(_of)))
//                throw new ActionException(this,"Unable to create file "+_of);
            
        }
        catch (OctaveEvalException oee){
            /*
            if(LOGGER.isLoggable(Level.SEVERE)){
                LOGGER.severe("Error executing octave script:\n"
                        +oee.getLocalizedMessage());
            }
            */
            throw new ActionException(this,"Unable to run octave script:\n"
                        +oee.getLocalizedMessage());
        }/*
        catch (Exception e){
// 1 - queue is empty... do what???
// TODO
throw new ActionException(this,e.getLocalizedMessage());
        }*/
        return events;
    }

    
}
