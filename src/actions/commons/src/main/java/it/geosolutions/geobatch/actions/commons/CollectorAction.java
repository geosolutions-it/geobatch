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
import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class CollectorAction extends BaseAction<EventObject> {

    private final static Logger LOGGER = LoggerFactory.getLogger(CollectorAction.class);

    /**
     * configuration
     */
    private final CollectorConfiguration conf;

    /**
     * 
     * @param configuration
     * @throws IllegalAccessException if input template file cannot be resolved
     * 
     */
    public CollectorAction(CollectorConfiguration configuration) throws IllegalArgumentException {
        super(configuration);
        conf = configuration;
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

        if (conf.getWildcard() == null) {
            LOGGER.warn("Null wildcard: using default\'*\'");
            conf.setWildcard("*");
        }

        Collector collector = new Collector(new WildcardFileFilter(conf.getWildcard(), IOCase.INSENSITIVE),
                                            conf.getDeep());
        while (!events.isEmpty()) {

            final EventObject event = events.remove();
            if (event == null) {
                // TODO LOG
                continue;
            }
            File source = null;
            if (event.getSource() instanceof File) {
                source = ((File)event.getSource());
            }

            if (source == null || !source.exists()) {
                // LOG
                continue;
            }
            listenerForwarder.setTask("Collecting from" + source);
            
            List<File> files=collector.collect(source);
            if (files==null){
                return ret;
            }
            for (File file: files){
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Collected file: " + file);
                }
                ret.add(new FileSystemEvent(file, FileSystemEventType.FILE_ADDED));
            }
            
        }
        listenerForwarder.completed();
        return ret;
    }

}
