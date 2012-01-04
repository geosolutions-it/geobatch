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

package it.geosolutions.geobatch.octave.actions.templates.freemarker;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveFunctionSheet;
import it.geosolutions.geobatch.octave.SheetBuilder;
import it.geosolutions.geobatch.octave.actions.OctaveAction;
import it.geosolutions.tools.compress.file.Extract;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import dk.ange.octave.exception.OctaveEvalException;


public class OctaveFreeMarkerAction extends OctaveAction<FileSystemEvent> {
       
    private final static Logger LOGGER = LoggerFactory.getLogger(OctaveFreeMarkerAction.class.toString());
    
    private final OctaveFreeMarkerConfiguration config;

    @XStreamOmitField
    protected static String OUT_FILE_KEY="OUT_FILE";

    // to set at runtime
    @XStreamOmitField
    protected static String IN_FILE_KEY="IN_FILE";

    // specific for FileInFileOut!!!
    @XStreamOmitField
    protected static String FUNCTION_KEY="FUNCTION";

    // to update using workingdir
    @XStreamOmitField
    protected static String SOURCEDIR_KEY="SOURCEDIR";

    // to set at runtime
    @XStreamOmitField
    protected static String WORKINGDIR_KEY="WORKINGDIR";

    @XStreamOmitField
    protected static String SHEETNAME_KEY="SHEETNAME";
    
    public OctaveFreeMarkerAction(OctaveFreeMarkerConfiguration actionConfiguration) {
        super(actionConfiguration);
        config=actionConfiguration;
    }
    
    /**
     *  set the output dir of this action
     *  @note this should be relative path as 
     *  it will be a sub dir of the working dir
     */
    protected String getOutputDir(){
        return config.getOutDir();
    }
    
    /**
     * @return a string representing the output file name of the script
     */
    protected String buildFileName(){
        return config.getCruise()+"_"+config.getModel()+"-Forecast-T" + new Date().getTime()+config.getExtension();
    }

    /**
     * Action to execute on the FileSystemEvent event queue.
     * 
     * @param Queue<FileSystemEvent> queue of events to handle in this (and next)
     * action executions.
     * @return Queue<FileSystemEvent> the resulting list of events
     */
    @Override
    public Queue<FileSystemEvent> load(Queue<FileSystemEvent> events, OctaveEnv<OctaveExecutableSheet> env)
            throws ActionException {
        try {
            if(LOGGER.isInfoEnabled())
                LOGGER.info("Running FileInFileOut script...");
            
            FileSystemEvent ev=events.remove();
            
            if ((ev)!=null){

                /**
                 * @note workingdir is an absolute path
                 * @see MARS3DGeneratorService
                 * Build output dir
                 * check if it :
                 * - exists (if not try to create)
                 * - check write permissions
                 */
                String out_dir_name=config.getWorkingDirectory()+File.separator+getOutputDir()+File.separator;
                File out_dir=new File(out_dir_name);
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
                 * build input file name
                 * uncompress the input file (if needed)
                 */
                String in_name=Extract.extract(ev.getSource().getAbsolutePath());
                
                /**
                 * build absolute output file name
                 */
                String out_name=out_dir_name+buildFileName();
                
                Map<String, Object> root=config.getRoot();
                if (root!=null){
//                  root.put(config.FUNCTION_KEY, getFunction());
                    if(LOGGER.isInfoEnabled()){
                        LOGGER.info(
                                "Preprocessing functions on arguents: \nFile_in: "+in_name
                                +" \nFile_out: "+out_name);
                    }
                    root.put(OctaveFreeMarkerAction.IN_FILE_KEY, in_name);
                    root.put(OctaveFreeMarkerAction.OUT_FILE_KEY, out_name);
                    StringBuilder sb=new StringBuilder(Path.getAbsolutePath(config.getWorkingDirectory())+File.separator);
                    if(LOGGER.isInfoEnabled()){
                        LOGGER.info(
                                "WorkingDir: "+sb.toString());
                    }
                    root.put(OctaveFreeMarkerAction.WORKINGDIR_KEY, sb.toString());
                    sb=null;

                    /**
                     * Build the SheetBuilder using a FreeMarkerSheetBuilder which get
                     * two files:
                     * in file (coming from events)
                     * out file build into the extending class  
                     */
                    SheetBuilder fb=new FreeMarkerSheetBuilder(config);

                    String name=null;
 // TODO this is a very bad check! add checks!
                    OctaveExecutableSheet es=env.getSheet(0);
                    if (es!=null){
                        if (es instanceof OctaveFunctionSheet){
                            OctaveFunctionSheet fs=(OctaveFunctionSheet) es;
                            name=fs.getFunctions().get(0).getName();
                        }
                    }

                    //add the SheetBuilder to the preprocessor map
                    preprocessor.addBuilder(name, fb);
                }
                else
                    throw new NullPointerException("The substitution root map cannot be null");
                
                

                /**
                 * add output file to the event queue
                 */
                events.add(new FileSystemEvent(
                        new File(out_name),FileSystemEventType.FILE_ADDED));

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
// DEBUG
e.printStackTrace();
            throw new ActionException(this,"Unable to run octave script:\n"
                    +e.getLocalizedMessage());
        }
        return events;
    }    
}
