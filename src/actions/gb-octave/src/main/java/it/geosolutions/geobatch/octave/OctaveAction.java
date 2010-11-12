package it.geosolutions.geobatch.octave;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ange.octave.exception.OctaveEvalException;
/**
 * This action is used to handle FileSystemMonitorEvent running Octave.
 * ...
 * @TODO complete description
 * 
 * @param <FileSystemMonitorEvent> the type of events passed to this action.
 * This Class implements actions to take on the FileSystemMonitorEvent events.
 * It also extends BaseAction<> to avoid re-implementing commons methods
 * @see BaseAction
 * @see Action
 * @see EventObject
 * @see FileSystemMonitorEvent
 * 
 * @author Carlo Cancellieri, ccancellieri AT geo-solutions.it, GeoSolutions
 */
public class OctaveAction extends BaseAction<FileSystemMonitorEvent> implements Action<FileSystemMonitorEvent> {

    private final static Logger LOGGER = Logger.getLogger(OctaveAction.class.toString());
    
// TODO: check... should we add this member to the BaseAction?
    private final OctaveActionConfiguration config;
    
    /**
     * Constructor
     * @param actionConfiguration configuration for this action.
     */
    public OctaveAction(OctaveActionConfiguration actionConfiguration) {
        super(actionConfiguration);
        config=actionConfiguration;
    }
    
    /**
// TODO: check... should we add this member to the BaseAction?
     * get configuration
     * @return configuration of this action
    */
    protected final OctaveActionConfiguration getConfig(){
        return config;
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
            
            /**
             * > I'm trying to use octave from java and I need to run netCDF functions
             * > installed by octcdf system package.
             * > Running octave manually all netcdf commands are available but doing
             * > something like:
             * >  octave.eval("nc=netcdf('/home/carlo/test2.nc','c');ncclose(nc);");
             * > I get:
             * > SEVERE: Unable to run octave script:
             * > `netcdf' undefined near line 11 column 8
             * > can you help me?
             * > May I have to set some ENV variables?
             * > thank you, carlo cancellieri
             * 
             * I can see that it is because I start the octave process with the
             * option --no-site-file, I think I need to remove that option in the
             * next release.
             * Until I create a new octave.jar you can source the site file manually
             * doing something like this:
             * octave.eval("source /usr/share/octave/3.2.4/m/startup/octaverc");
             * after that netcdf() should work. You might have to adjust the path to
             * octaverc to fit your local octave installation.
             * Regards,
             * Kim Hansen
             * Vadgårdsvej 3, 2.tv
             * 2860 Søborg
             * Phone: +45 3091 2437
             */
            getConfig().getFunction().eval("source \"/usr/share/octave/3.0.5/m/startup/octaverc\"");
            
            // If configuration contains a new octave path
            // TODO: warning no check on path is performed
            //       anyway 'eval' is safe (read apidoc);
            if (getConfig().getPathList()!=null)
            for (String path : getConfig().getPathList()){
                /**
                 * adding/setting path:
                 * @note absolute path should begin with
                 * double '/'
                 */
                // setting octave engine
                getConfig().getFunction().eval("addPath(\'"+path+"\');");
            }
            
            Iterator<SerializableOctaveObject<?>> i=
                getConfig().getFunction().getArgumentsList().iterator();
            
            Iterator<FileSystemMonitorEvent> e=events.iterator();
            
            while (e.hasNext()){
                FileSystemMonitorEvent ev=e.next();
//todo: check if this condition is needed
                if ((ev)==null && LOGGER.isLoggable(Level.WARNING))
                    LOGGER.warning(
                        "Executing Octave script on empty event queue");
                
                Vector<String> in_files=new Vector<String>();
                
                while (i.hasNext()){
                    SerializableOctaveObject<?> obj=i.next();
                    // input file string
                    if (obj instanceof SerializableOctaveFile){
                        SerializableOctaveFile sof=(SerializableOctaveFile)obj;
                        if (sof.isInput()){
                            // input_file_name
                            sof.reSetVal(ev.getSource().getAbsolutePath());
                            in_files.add(ev.getSource().getAbsolutePath());
                            /**
                             * if it is NOT also an output file
                             * we have to change the name so
                             * here we store into the in_file list
                             * to pass it to a conversion class.
                             */
                            // 
                            if (!sof.isOutput()){
                                in_files.add(ev.getSource().getName());
                            }
                        }
                        if (sof.isOutput()){
                            // TODO: check if firstElement remove the object
                            String out_name=in_files.firstElement();
                            
                            // TODO: ELABORATE THE STRING USING
                            // THE CONFIGURATION to load the right class
                            
                            out_name=ev.getSource().getParent()+
                                File.separator+"out"+out_name;
                            // output_file_name
                            sof.reSetVal(out_name);
                            
                            events.add(new FileSystemMonitorEvent(
                                    new File(out_name),FileSystemMonitorNotifications.FILE_ADDED));
                            
                            
                            
                            /**
                             * 
                             */
                        }
                    }
                }
            }
            
            // script
            String script;

            // setting script
            String data = System.getProperty("GEOBATCH_DATA_DIR");
            if (data!=null){
                script="cd \""+data+File.separator+getConfig().getWorkingDirectory()+"\";";
            }
            else if ((data = System.getenv("GEOBATCH_DATA_DIR"))!=null){
                script="cd \""+data+File.separator+getConfig().getWorkingDirectory()+"\";";
            }
            else
                throw new ActionException(this,"GEOBATCH_DATA_DIR not defined");
            
            System.out.println("DATA3:"+data);
            
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Entering working dir using command: "+script);
            
            // Going to local working dir:
            // `cd /local/working/dir`
            getConfig().getFunction().eval(script);
            
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Evaluating...");
            // executing script
            // replacing input file placeholder with filename
            
//TODO: @note this should be run as thread
            getConfig().getFunction().run();
            
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Evaluating: DONE");
            
            // closing octave script engine
            getConfig().getFunction().close();
            
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
