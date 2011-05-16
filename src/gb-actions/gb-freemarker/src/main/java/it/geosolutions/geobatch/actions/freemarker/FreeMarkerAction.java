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

import freemarker.template.TemplateModel;
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
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action can be used to filter a data structure of type DATA_IN which must be supported by
 * FreeMarker (see its documentation)
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @param <DATA_CONF>
 */
public class FreeMarkerAction extends BaseAction<EventObject> implements
        EventAdapter<TemplateModelEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(FreeMarkerAction.class);

    /**
     * configuration
     */
    final FreeMarkerConfiguration conf;

    // the filter
    final FreeMarkerFilter filter;

    public FreeMarkerAction(FreeMarkerConfiguration configuration) {
        super(configuration);
        conf = configuration;
        filter = new FreeMarkerFilter(Path.getAbsolutePath(conf.getWorkingDirectory()),
                conf.getInput());
    }

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {

        // build the output absolute file name
        final File outputFile;
        try {
            // the output
            outputFile = it.geosolutions.geobatch.tools.file.Path.findLocation(conf.getOutput(),
                    new File(conf.getWorkingDirectory()));
            if (LOGGER.isInfoEnabled())
                LOGGER.info("FreeMarkerAction.execute(): Output file name: "
                        + outputFile.toString());

        } catch (NullPointerException npe) {
            final String message = "FreeMarkerAction.execute(): Unable to get the output file path from :"
                    + conf.getOutput();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this, message);
        }

        // try to open the file to write into
        FileWriter fw = null;
        try {
            fw = new FileWriter(outputFile);
        } catch (IOException ioe) {
            final String message = "FreeMarkerAction.execute(): Unable to build the output file writer: "
                    + ioe.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this, message);
        }

        /*
         * Building/getting the root data structure
         */
        Map<String, Object> root = null;
        if (conf.getRoot() != null)
            root = conf.getRoot();
        else
            root = new HashMap<String, Object>();

        /*
         * while the adapted object (peeked from the queue) is a TemplateModelEvent instance, try to
         * add to it the root data structure using the name of the event.
         */
        TemplateModelEvent ev = null;
        List<TemplateModel> list = new ArrayList<TemplateModel>();
        while (events.size() > 0) {
            try {
                // append the incoming data structure
                if ((ev = adapter(events.remove())) != null) {
                    // try to get a TemplateModel from the adapted object
                    list.add(ev.getModel(filter));
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("FreeMarkerAction.execute(): Unable to append the event: unrecognized format");
                    }
                }
            } catch (TemplateModelException tme) {
                final String message = "FreeMarkerAction.execute(): Unable to wrap the passed object: "
                        + tme.getLocalizedMessage();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this, message);
            } catch (Exception ioe) {
                final String message = "FreeMarkerAction.execute(): Unable to produce the output: "
                        + ioe.getLocalizedMessage();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this, message);
            }
        }

        // append the list of adapted event objects
        root.put(TemplateModelEvent.EVENT_KEY, list);

        /*
         * If available, process the output file using the TemplateModel data structure
         */
        try {
            // process the input template file
            filter.process(filter.wrapRoot(root), fw);

            // flush the buffer
            if (fw != null)
                fw.flush();

        } catch (IOException ioe) {
            final String message = "FreeMarkerAction.execute(): Unable to flush buffer to the output file: "
                    + ioe.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this, message);
        } catch (TemplateModelException tme) {
            final String message = "FreeMarkerAction.execute(): Unable to wrap the passed object: "
                    + tme.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this, message);
        } catch (Exception e) {
            final String message = "FreeMarkerAction.execute(): Unable to process the input file: "
                    + e.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this, message);
        } finally {
            IOUtils.closeQuietly(fw);
        }

        // add the file to the queue
        events.add(new FileSystemEvent(outputFile.getAbsoluteFile(), FileSystemEventType.FILE_ADDED));
        return events;
    }

    /**
     * Used as key into the map for the incoming event. ${event[X].PARENT}
     * 
     * To use it into a template you have to use:<br>
     * ${event[0].PARENT} -> first file into the queue<br>
     * ${event[N-1].PARENT} -> (N)th file into the queue<br>
     * 
     * To compose the entire file name:<br>
     * ${event[N].PARENT}/${event[N].FILENAME}.${event[N].EXTENSION}
     */
    private static final String FILE_EVENT_PARENTFILE_KEY = "PARENT";

    private static final String FILE_EVENT_NAMEFILE_KEY = "FILENAME";

    private static final String FILE_EVENT_EXTENSION_KEY = "EXTENSION";

    /**
     * Act as a Gateway interface (EIP):<br>
     * Try to adapt the effective input EventObject to the expected input a TemplateDataModel
     * 
     * @param ieo
     *            The Event Object to test or to transform
     * @return Adapted data model or null if event cannot be adapted
     */
    public TemplateModelEvent adapter(EventObject ieo) throws ActionException {
        if (ieo instanceof TemplateModelEvent) {
            return (TemplateModelEvent) ieo;
        } else if (ieo instanceof FileSystemEvent) {
            Map<String, Object> map = new HashMap<String, Object>();

            final File file = ((FileSystemEvent) ieo).getSource().getAbsoluteFile();

            map.put(FILE_EVENT_PARENTFILE_KEY, file.getParent());
            map.put(FILE_EVENT_NAMEFILE_KEY, FilenameUtils.getBaseName(file.getName()));
            map.put(FILE_EVENT_EXTENSION_KEY, FilenameUtils.getExtension(file.getName()));

            return new TemplateModelEvent(map);
        } else {
            try {
                return new TemplateModelEvent(filter.wrapRoot(ieo.getSource()));
            } catch (NullPointerException npe) {
                // NullPointerException - if tm is null
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("FreeMarkerAction.adapter(): The passed event object is null");
            } catch (TemplateModelException tme) {
                // TemplateModelException - if defined objectWrapper can't wrap the passed object
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("FreeMarkerAction.adapter(): Default wrapper can't wrap the passed object");
            }
        }
        return null;
    }

}
