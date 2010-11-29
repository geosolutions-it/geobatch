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
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.octave.SheetBuilder;
import it.geosolutions.geobatch.octave.actions.OctaveAction;
import it.geosolutions.geobatch.octave.actions.OctaveActionConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ange.octave.exception.OctaveEvalException;


public abstract class FileInFileOutAction extends OctaveAction<FileSystemMonitorEvent> {
       
    private final static Logger LOGGER = Logger.getLogger(FileInFileOutAction.class.toString());
    
    public FileInFileOutAction(OctaveActionConfiguration actionConfiguration) {
        super(actionConfiguration);
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
     * @return a string representing the function name to map with this
     * preprocessor template
     */
    protected abstract String getFunction();
    
    /**
     * Action to execute on the FileSystemMonitorEvent event queue.
     * 
     * @param Queue<FileSystemMonitorEvent> queue of events to handle in this (and next)
     * action executions.
     * @return Queue<FileSystemMonitorEvent> the resulting list of events
     */
    public Queue<FileSystemMonitorEvent> load(Queue<FileSystemMonitorEvent> events)
            throws ActionException {
        try {
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Running FileInFileOut script...");
            
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
                        throw new IOException("Unable to create the output dir: "+out_dir);
                    }
                    else{
                        if (!out_dir.canWrite())
                            throw new IOException("" +
                                    "Can't write to the output dir: "+out_dir+" check permissions");
                    }
                }
                
                /**
                 * build absolute output file name
                 */
                String out_name=out_dir.toString()+buildFileName();
                
                /**
                 * Build the SheetBuilder using a FileInFileOutSheetBuilder which get
                 * two files:
                 * in file (coming from events)
                 * out file build into the extending class  
                 */
                SheetBuilder fb=new FileInFileOutSheetBuilder(ev.getSource().getAbsolutePath(),out_name);
                
                if(LOGGER.isLoggable(Level.INFO)){
                    LOGGER.info(
                            "Preprocessing functions on arguents: \nFile_in: "+ev.getSource().getAbsolutePath()
                            +" \nFile_out: "+out_name);
                }
                
                /**
                 * add the SheetBuilder to the preprocessor map
                 */
                preprocessor.addBuilder(getFunction(), fb);

                /**
                 * add output file to the event queue
                 */
                events.add(new FileSystemMonitorEvent(
                        new File(out_name),FileSystemMonitorNotifications.FILE_ADDED));

            } // ev==null
            else {
                throw new ActionException(this, "Empty event recived");
            }
            
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
