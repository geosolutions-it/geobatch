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

package it.geosolutions.geobatch.octave.actions.templates.FileInFileOut;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.octave.DefaultSheetBuilder;
import it.geosolutions.geobatch.octave.Engine;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;

import java.io.File;
import java.util.Date;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.servicetag.UnauthorizedAccessException;

import dk.ange.octave.exception.OctaveEvalException;


public abstract class FileInFileOutAction extends BaseAction<FileSystemMonitorEvent> implements Action<FileSystemMonitorEvent> {
       
    private final static Logger LOGGER = Logger.getLogger(FileInFileOutAction.class.toString());
    
    //private final static String OUTPUT_DIR="nc";
    
//todo: remove me (this env should be owned by the OctaveThread and
// objects are pushed in it:
    private volatile OctaveEnv<OctaveExecutableSheet>  env;
    //private final static Thread oct=new Thread(new OctaveThread(bus));
    private final static Engine engine=new Engine();
    
    // 
    // TODO: check... should we add this member to the BaseAction?
    private final FileInFileOutActionConfiguration config;
    
    @SuppressWarnings("unchecked")
    public FileInFileOutAction(FileInFileOutActionConfiguration actionConfiguration) {
        super(actionConfiguration);
        env=(OctaveEnv<OctaveExecutableSheet>)actionConfiguration.getEnv().clone();
        config=actionConfiguration;
        
     // TODO implement a M.D.POJO
        //run thread manually
        //if (!oct.isAlive())
        //    oct.start();
    }
    
    /**
     *  set the output dir of this action
     *  @note this should be relative path as 
     *  it will be a sub dir of the working dir
     */
    protected abstract String getOutputDir();
    
    /**
     * @return a string representing the output file name of the script
     */
    protected abstract String buildFileName();
    
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
            
            FileSystemMonitorEvent ev=events.remove();
            
            if ((ev)!=null){

                /**
                 * @note workingdir is an absolute path
                 * @see MARS3DGeneratorService
                 * Build output dir
                 * check if it :
                 * - exists (if not try to create)
                 * - check write permissions
                 */
                
                File out_dir=new File(config.getWorkingDirectory()+File.separator+getOutputDir()+File.separator);
                if (!out_dir.exists()){
                    if (!out_dir.mkdir()){
                        throw new UnauthorizedAccessException("Unable to create the output dir: "+out_dir);
                    }
                    else{
                        if (!out_dir.canWrite())
                            throw new UnauthorizedAccessException("" +
                                    "Can't write to the output dir: "+out_dir+" check permissions");
                    }
                }
                
                /**
                 * build absolute output file name
                 */
                String out_name=out_dir.toString()+buildFileName();
                
                
                /**
                 * Build the MARS3DFunctionBuilder
                 */
                DefaultSheetBuilder fb=new FileInFileOutSheetBuilder(ev.getSource().getAbsolutePath(),out_name);
                
                if(LOGGER.isLoggable(Level.INFO)){
                    LOGGER.info(
                            "Preprocessing functions on arguents: \nFile_in: "+ev.getSource().getAbsolutePath()
                            +" \nFile_out: "+out_name);
                }
                
                /**
                 * try to preprocess the OctaveFunctionSheet
                 * this operation should transform all the OctaveFunction stored into the env
                 * into OctaveExecutableSheet which can be freely executed by the Octave Engine.class
                 * @note each sheet is executed atomically so be careful with the 'cd' command
                 * (which change context dir) or other commands like so.    
                 */
                try {
                    fb.preprocess(env);
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
                
                /**
                 * add output file to the event queue
                 */
                events.add(new FileSystemMonitorEvent(
                        new File(out_name),FileSystemMonitorNotifications.FILE_ADDED));

            } // ev==null
            else if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning(
                    "Executing Octave script on empty event queue");
            
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
