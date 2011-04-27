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
package it.geosolutions.geobatch.actions.freemarker;

import freemarker.template.TemplateModelException;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.tools.adapter.EventAdapter;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.tools.filter.FreeMarkerFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action can be used to filter a data structure of type DATA_IN which
 * must be supported by FreeMarker (see its documentation)
 *  
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 * @param <DATA_CONF>
 */
public class FreeMarkerAction 
        extends BaseAction<EventObject> 
        implements EventAdapter<TemplateModelEvent>
    {
    private final static Logger LOGGER = LoggerFactory.getLogger(FreeMarkerAction.class.toString());
    
    FreeMarkerConfiguration conf;
    
    public FreeMarkerAction(FreeMarkerConfiguration configuration) {
        super(configuration);
        conf=configuration;
    }

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
        // the filter
        FreeMarkerFilter f=new FreeMarkerFilter(Path.getAbsolutePath(conf.getWorkingDirectory()),conf.getInput());
        
        // build the output absolute file name
        StringBuilder output=null;
        try{
            output=new StringBuilder(Path.getAbsolutePath(conf.getWorkingDirectory()));
            output.append(File.separatorChar+conf.getOutput());
        }
        catch(NullPointerException npe){
            String message="Unable to build the output file name";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this,message);
        }
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Output file name: "+output.toString());

        // the output
        File out=new File(output.toString());
        // try to open the file to write into
        FileWriter fw=null;
        try{
            fw=new FileWriter(out);
        }
        catch (IOException ioe){
            String message="Unable to build the output file writer: "+ioe.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this,message);
        }
        
        /*
         * Building/getting the root data structure
         */
        Map<String,Object> root=null;
        if (conf.getRoot()!=null)
            root=conf.getRoot();
        else
            root=new HashMap<String, Object>();
        
        /*
         * while the adapted object (peeked from the queue) is a TemplateModelEvent 
         * instance, try to add to it the root data structure using the name of the event.
         */
        TemplateModelEvent ev=null;
        while ((ev=adapter(events.peek()))!=null){
            try {
                // append the incoming data structure
                root.put(ev.getName(), ev.getModel(f));
            }
            catch (TemplateModelException tme){
                String message="Unable to wrap the passed object: "+tme.getLocalizedMessage();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this,message);
            }
            catch(Exception ioe){
                String message="Unable to produce the output: "+ioe.getLocalizedMessage();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this,message);
            }
            
            // remove the processed element
            events.remove(ev);
        }
        
        /*
         * If available, process the output file using 
         * the TemplateModel data structure
         */
        try {
            // process the input template file
            if (root!=null)
                f.process(f.wrapRoot(root), fw);
            else
                throw new NullPointerException("Unable to process a null root data structure");
            // flush the buffer
            fw.flush();
        }
        catch (IOException ioe){
            String message="Unable to flush buffer to the output file: "+ioe.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this,message);
        }
        catch (TemplateModelException tme){
            String message="Unable to wrap the passed object: "+tme.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this,message);
        }
        catch (Exception e){
            String message="Unable to process the input file: "+e.getLocalizedMessage();
            e.printStackTrace();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this,message);
        }
        
        // add the file to the queue
        events.add(new FileSystemEvent(out.getAbsoluteFile(), FileSystemEventType.FILE_ADDED));
        return events;
    }

    public TemplateModelEvent adapter(EventObject ieo) throws ActionException {
        if (ieo instanceof TemplateModelEvent)
            return (TemplateModelEvent)ieo;
        else if (ieo instanceof FileSystemEvent){
            Map<String,Object> map=new HashMap<String, Object>();
            map.put("FILE", ieo.getSource());
            return new TemplateModelEvent(map);
        }
        else
            return null;
    }

}
