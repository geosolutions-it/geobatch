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
package it.geosolutions.geobatch.actions.xstream;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * This action can be used to filter a data structure of type DATA_IN which must be supported by
 * FreeMarker (see its documentation)
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @param <DATA_CONF>
 */
public class XstreamAction extends BaseAction<EventObject> {
    private final static Logger LOGGER = LoggerFactory.getLogger(XstreamAction.class);

    /**
     * configuration
     */
    private final XstreamConfiguration conf;

    private final XStream xstream;

    public XstreamAction(XstreamConfiguration configuration) {
        super(configuration);
        conf = configuration;
        xstream = new XStream(); // TODO set the reflection provider
    }

    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {

        // the output
        final Queue<EventObject> ret = new LinkedList<EventObject>();

        while (events.size() > 0) {
            final EventObject event = events.remove();
            if (event == null) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("XstreamAction.adapter(): The passed event object is null");
                continue;
            }
            try {

                if (event instanceof FileSystemEvent) {
                    // generate an object
                    final File sourceFile = File.class.cast(event.getSource());
                    if (!sourceFile.exists() || !sourceFile.canRead()) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("XstreamAction.adapter(): The passed FileSystemEvent "
                                    + "reference to a not readable or not existent file: "
                                    + sourceFile.getAbsolutePath());
                        continue;
                    }
                    final FileInputStream inputStream = new FileInputStream(sourceFile);
                    try {
                        final Map<String, String> aliases = conf.getAlias();
                        if (aliases != null && aliases.size() > 0) {
                            for (String alias : aliases.keySet()) {
                                final Class<?> clazz = Class.forName(aliases.get(alias));
                                xstream.alias(alias, clazz);
                            }
                        }
                        // deserialize
                        final Object res = xstream.fromXML(inputStream);
                        // generate event
                        final EventObject eo = new EventObject(res);
                        // append to the output
                        ret.add(eo);

                    } catch (XStreamException e) {
                        // the object cannot be deserialized
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(
                                    "XstreamAction.adapter(): The passed FileSystemEvent "
                                            + "reference to a not deserializable file: "
                                            + sourceFile.getAbsolutePath(), e);
                        continue;
                    } catch (Throwable e) {
                        // the object cannot be deserialized
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(
                                    "XstreamAction.adapter(): " + e.getLocalizedMessage(), e);
                        continue;
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                    }

                } else {
                    // try to serialize
                    // build the output absolute file name
                    final File outputDir;
                    try {
                        // the output
                        outputDir = it.geosolutions.geobatch.tools.file.Path.findLocation(
                                conf.getOutput(), new File(conf.getWorkingDirectory()));

                        if (!outputDir.exists()) {
                            if (!outputDir.mkdirs()) {
                                if (LOGGER.isInfoEnabled())
                                    LOGGER.info("XstreamAction.execute(): Unable to create the ouptut dir named: "
                                            + outputDir.toString());
                                continue;
                            }
                        }
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("XstreamAction.execute(): Output dir name: "
                                    + outputDir.toString());
                        }

                    } catch (NullPointerException npe) {
                        final String message = "XstreamAction.execute(): Unable to get the output file path from :"
                                + conf.getOutput();
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(message, npe);
                        continue;
                    }

                    final File outputFile = new File(outputDir, conf.getOutput());

                    // try to open the file to write into
                    FileWriter fw = null;
                    try {
                        fw = new FileWriter(outputFile);
                    } catch (IOException ioe) {
                        final String message = "XstreamAction.execute(): Unable to build the output file writer: "
                                + ioe.getLocalizedMessage();
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(message, ioe);
                        continue;
                    }

                    try {
                        final Map<String, String> aliases = conf.getAlias();
                        if (aliases != null && aliases.size() > 0) {
                            for (String alias : aliases.keySet()) {
                                final Class<?> clazz = Class.forName(aliases.get(alias));
                                xstream.alias(alias, clazz);
                            }
                        }
                        xstream.toXML(event.getSource(), fw);
                    } catch (XStreamException e) {
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(
                                    "XstreamAction.adapter(): The passed event object cannot be serialized to: "
                                            + outputFile.getAbsolutePath(), e);
                        continue;
                    } catch (Throwable e) {
                        // the object cannot be deserialized
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(
                                    "XstreamAction.adapter(): " + e.getLocalizedMessage(), e);
                        continue;
                    } finally {
                        IOUtils.closeQuietly(fw);
                    }

                    // add the file to the queue
                    ret.add(new FileSystemEvent(outputFile.getAbsoluteFile(),
                            FileSystemEventType.FILE_ADDED));

                }

            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("XstreamAction.execute(): " + e.getLocalizedMessage(), e);
                }
            }
        }

        return ret;
    }
}
