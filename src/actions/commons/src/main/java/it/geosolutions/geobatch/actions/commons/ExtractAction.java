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
package it.geosolutions.geobatch.actions.commons;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.tools.compress.file.Extract;

import java.io.File;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class ExtractAction extends BaseAction<EventObject> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExtractAction.class);

    /**
     * configuration
     */
    private final ExtractConfiguration conf;

    /**
     * 
     * @param configuration
     * @throws IllegalAccessException if input template file cannot be resolved
     * 
     */
    public ExtractAction(ExtractConfiguration configuration) throws IllegalArgumentException {
        super(configuration);
        conf = configuration;
        if (conf.getDestination()==null){
            throw new IllegalArgumentException("Unable to work with a null dest dir");
        }
        if (!conf.getDestination().isAbsolute()){
            // TODO LOG
            conf.setConfigDir(new File(conf.getConfigDir(),conf.getDestination().getPath()));
        }
    }

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {

        listenerForwarder.started();
        listenerForwarder.setTask("build the output absolute file name");

        // return
        final Queue<EventObject> ret = new LinkedList<EventObject>();

        listenerForwarder.setTask("Building/getting the root data structure");
        
        boolean extractMultipleFile;
        final int size=events.size();
        if (size==0){
            throw new ActionException(this, "Empty file list");
        } else if (size>1){
            extractMultipleFile=true;
        } else {
            extractMultipleFile=false;
        }
        
        final File dest=conf.getDestination();
        
        if (dest!=null && !dest.isDirectory()){
            if (!dest.mkdirs()){
                throw new ActionException(this, "bad destination (not writeable): "+dest);
            }
        }
        
        
        while (!events.isEmpty()) {
            listenerForwarder.setTask("Generating the output");
            
            final EventObject event=events.remove();
            if (event==null){
                // TODO LOG
                continue;
            }
            if (event instanceof FileSystemEvent){ 
                File source = ((FileSystemEvent) event).getSource();
                
                try {
                    listenerForwarder.setTask("Extracting file: "+source);
                    final File extracted=Extract.extract(source,getTempDir(),false);
                    if (extracted!=null){
                            if (dest!=null){
                                File newDest=new File(dest,extracted.getName());
                                listenerForwarder.setTask("moving \'"+extracted+"\' to \'"+newDest+"\'");
                                FileUtils.moveDirectoryToDirectory(extracted, newDest, true);
                            ret.add(new FileSystemEvent(newDest, FileSystemEventType.DIR_CREATED));
                        } else {
                            throw new ActionException(this, "Unable to extracto file: "+source);
                        }
                    } else{
                        final String message="Unable to extract "+source;
                        if (!getConfiguration().isFailIgnored()){
                            throw new ActionException(this.getClass(), message);
                        } else {
                            LOGGER.warn(message);
                        }
                    }
                } catch (Exception e) {
                    if (!getConfiguration().isFailIgnored()){
                        throw new ActionException(this, e.getLocalizedMessage());
                    } else {
                        LOGGER.warn(e.getLocalizedMessage());
                    }
                    
                }
            } else {
                final String message="Incoming instance is not a FileSystemEvent: "+event;
                if (!getConfiguration().isFailIgnored()){
                    throw new ActionException(this.getClass(), message);
                } else {
                    LOGGER.warn(message);
                }
            }
        } // endwile

        listenerForwarder.completed();
        return ret;
    }

}
