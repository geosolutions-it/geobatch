/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveFunctionSheet;
import it.geosolutions.geobatch.octave.SheetBuilder;
import it.geosolutions.geobatch.octave.actions.OctaveAction;
import it.geosolutions.tools.compress.file.Extract;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import dk.ange.octave.exception.OctaveEvalException;

@Action(configurationClass=OctaveFreeMarkerConfiguration.class)
public class OctaveFreeMarkerAction extends OctaveAction<FileSystemEvent> {
       
    private final static Logger LOGGER = LoggerFactory.getLogger(OctaveFreeMarkerAction.class);
    
	@Override
	@CheckConfiguration
	public boolean checkConfiguration() {
		// TODO Auto-generated method stub
		return false;
	}
	
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

    /**
     * @deprecated Use TEMPDIR_KEY instead
     */
    @XStreamOmitField
    protected static String WORKINGDIR_KEY="WORKINGDIR";

    /**
     * TODO: string content should be set to "TEMPDIR", but I::ETj guess this would break some templates.
     */
    // to set at runtime
    @XStreamOmitField
    protected static String TEMPDIR_KEY="WORKINGDIR";

    @XStreamOmitField
    protected static String SHEETNAME_KEY="SHEETNAME";
    
    public OctaveFreeMarkerAction(OctaveFreeMarkerConfiguration actionConfiguration) {
        super(actionConfiguration);
        config=actionConfiguration;
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
//                String out_dir_name=config.getOverrideConfigDir()+File.separator+getOutputDir()+File.separator;
                                
                /*
                 * build input file name
                 * uncompress the input file (if needed)
                 */
                String in_name=Extract.extract(ev.getSource().getAbsolutePath());
                
                // build absolute output file name
                String out_name = new File(getTempDir(), ev.getSource().getName()).getAbsolutePath();
                
                Map<String, Object> root = config.getRoot() != null ?
                        config.getRoot() :
                        new HashMap<String, Object>();

//                  root.put(config.FUNCTION_KEY, getFunction());
                root.put(OctaveFreeMarkerAction.IN_FILE_KEY,  in_name);
                root.put(OctaveFreeMarkerAction.OUT_FILE_KEY, out_name);
                root.put(OctaveFreeMarkerAction.TEMPDIR_KEY,  getTempDir().getAbsolutePath());

                if(LOGGER.isInfoEnabled()){
                    LOGGER.info("Preprocessing functions on arguments: ");
                    for (Map.Entry<String, Object> entry : root.entrySet()) {
                        LOGGER.info("Arg '"+entry.getKey()+"' is '"+entry.getValue()+"'");
                    }
                }

                /**
                 * Build the SheetBuilder using a FreeMarkerSheetBuilder which get
                 * two files:
                 * in file (coming from events)
                 * out file build into the extending class  
                 */
                SheetBuilder fb = new FreeMarkerSheetBuilder(getConfigDir(), root);

                String name=null;
 // TODO this is a very bad check! add checks!
                OctaveExecutableSheet es=env.getSheet(0);
                if (es!=null){
                    if(LOGGER.isDebugEnabled()) {
//                        StringWriter sw = new StringWriter();
//                        JAXB.marshal(es, sw);
                        LOGGER.debug("Executable sheet :\n" + es);
                    }

                    if (es instanceof OctaveFunctionSheet){
                        OctaveFunctionSheet fs=(OctaveFunctionSheet) es;
                        name=fs.getFunctions().get(0).getName();
                    } else {
                        throw new ActionException(this, "Unknown sheet type " + es);
                    }
                } else {
                    throw new ActionException(this, "No sheets found");
                }

                //add the SheetBuilder to the preprocessor map
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Adding builder for " + name);
                }
                preprocessor.addBuilder(name, fb);

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
                        +oee.getLocalizedMessage(),oee);
        }
        catch(Exception e){
            throw new ActionException(this,"Unable to run octave script:\n"
                    +e.getLocalizedMessage(),e);
        }
        return events;
    }    
}
