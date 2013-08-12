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
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
@Action(configurationClass=CopyConfiguration.class)
public class CopyAction extends BaseAction<EventObject> {

    private final static Logger LOGGER = LoggerFactory.getLogger(CopyAction.class);

    @Override
	@CheckConfiguration
	public boolean checkConfiguration(){
	    LOGGER.info("Calculating if this action could be Created...");
	    return true;
	}
    
    /**
     * configuration
     */
    private final CopyConfiguration conf;

    /**
     * 
     * @param configuration
     * @throws IllegalAccessException if input template file cannot be resolved
     * 
     */
    public CopyAction(CopyConfiguration configuration) throws IllegalArgumentException {
        super(configuration);
        conf = configuration;

        if (!conf.getDestination().isAbsolute()) {
            // TODO LOG
            conf.setConfigDir(new File(conf.getConfigDir(), conf.getDestination().getPath()));
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

        boolean copyMultipleFile;
        final int size = events.size();
        if (size == 0) {
            throw new ActionException(this, "Empty file list");
        } else if (size > 1) {
            copyMultipleFile = true;
        } else {
            copyMultipleFile = false;
        }

        boolean copyToDir;
        if (!conf.getDestination().isDirectory()) {
            // TODO LOG
            copyToDir = false;
            if (copyMultipleFile) {
                throw new ActionException(this,
                                          "Unable to run on multiple file with an output file, use directory instead");
            }
        } else {
            copyToDir = true;
        }

        while (!events.isEmpty()) {
            listenerForwarder.setTask("Generating the output");

            final EventObject event = events.remove();
            if (event == null) {
                // TODO LOG
                continue;
            }
            if (event instanceof FileSystemEvent) {
                File source = ((FileSystemEvent)event).getSource();
                File dest;
                if (copyToDir) {
                    dest = new File(conf.getDestination(), source.getName());
                } else if (copyMultipleFile) {
                    dest = conf.getDestination();
                } else {
                    // LOG continue
                    continue;
                }

                File out = it.geosolutions.tools.io.file.Copy.copyFileToNFS(source, dest, conf.getTimeout());
                if (out != null) {
                    // add the file to the return
                    ret.add(new FileSystemEvent(out, FileSystemEventType.FILE_ADDED));
                } else {
                    // TODO LOG
                }
            }
        }

        listenerForwarder.completed();
        return ret;
    }

}
