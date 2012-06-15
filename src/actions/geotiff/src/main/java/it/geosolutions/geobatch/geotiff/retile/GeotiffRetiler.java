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
package it.geosolutions.geobatch.geotiff.retile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ReTile the passed geotiff image. NOTE: accept only one image per run
 * 
 * @author Simone Giannechini, GeoSolutions
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $GeoTIFFOverviewsEmbedder.java Revision: 0.1 $ 23/mar/07 11:42:25 Revision: 0.2 $
 *          15/Feb/11 14:40:00
 */
public class GeotiffRetiler extends BaseAction<FileSystemEvent> {

    private GeotiffRetilerConfiguration configuration;

    final static Logger LOGGER = LoggerFactory.getLogger(GeotiffRetiler.class);

    public GeotiffRetiler(GeotiffRetilerConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {
        try {

            if (configuration == null) {
                final String message = "GeotiffRetiler::execute(): flow configuration is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this, message);
            }
            if (events.size() == 0) {
                throw new ActionException(this,
                        "GeotiffRetiler::execute(): Unable to process an empty events queue.");
            }

            if (LOGGER.isInfoEnabled())
                LOGGER.info("GeotiffRetiler::execute(): Starting with processing...");

            listenerForwarder.started();

            // The return
            final Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();

            while (events.size() > 0) {

                FileSystemEvent event = events.remove();

                File eventFile = event.getSource();
                FileSystemEventType eventType = event.getEventType();

                if (eventFile.exists() && eventFile.canRead() && eventFile.canWrite()) {
                    /*
                     * If here: we can start retiler actions on the incoming file event
                     */

                    if (eventFile.isDirectory()) {
                    	

                        File[] fileList = eventFile.listFiles();
                        int size = fileList.length;
                        for (int progress = 0; progress < size; progress++) {

                            File inFile = fileList[progress];

                            final String absolutePath = inFile.getAbsolutePath();
                            final String inputFileName = FilenameUtils.getName(absolutePath);

                            if (LOGGER.isInfoEnabled())
                                LOGGER.info("is going to retile: " + inputFileName);

                            

                            try {

                                listenerForwarder.setTask("GeotiffRetiler");
                                GeoTiffRetilerUtils.reTile(inFile, configuration, getTempDir());

                                
                                // set the output
                                /*
                                 * COMMENTED OUT 21 Feb 2011: simone: If the event represents a Dir
                                 * we have to return a Dir. Do not matter failing files.
                                 * 
                                 * carlo: we may also want to check if a file is already tiled!
                                 * 
                                 * File outputFile=reTile(inFile); if (outputFile!=null){ //TODO:
                                 * here we use the same event for each file in the ret.add(new
                                 * FileSystemEvent(outputFile, eventType)); }
                                 */
                                

                            } catch (UnsupportedOperationException uoe) {
                                listenerForwarder.failed(uoe);
                                if (LOGGER.isWarnEnabled())
                                    LOGGER.warn(uoe.getLocalizedMessage(), uoe);
                                continue;
                            } catch (IOException ioe) {
                                listenerForwarder.failed(ioe);
                                if (LOGGER.isWarnEnabled())
                                    LOGGER.warn(ioe.getLocalizedMessage(), ioe);
                                continue;
                            } catch (IllegalArgumentException iae) {
                                listenerForwarder.failed(iae);
                                if (LOGGER.isWarnEnabled())
                                    LOGGER.warn(iae.getLocalizedMessage(), iae);
                                continue;
                            } finally {
                                listenerForwarder.setProgress((progress * 100)
                                        / ((size != 0) ? size : 1));
                                listenerForwarder.progressing();
                            }
                        }

                        if (LOGGER.isInfoEnabled())
                            LOGGER.info("SUCCESSFULLY completed work on: "+ event.getSource());
                        
                        // add the directory to the return
                        ret.add(event);
                    } else {
                        // file is not a directory
                        try {
                            listenerForwarder.setTask("GeotiffRetiler");
                            final File outputFile=GeoTiffRetilerUtils.reTile(eventFile, configuration, getTempDir());
                            
                            if (LOGGER.isInfoEnabled())
                                LOGGER.info("SUCCESSFULLY completed work on: "
                                        + event.getSource());
                            listenerForwarder.setProgress(100);
                            ret.add(new FileSystemEvent(outputFile, eventType));
                        
                        } catch (UnsupportedOperationException uoe) {
                            listenerForwarder.failed(uoe);
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn(uoe.getLocalizedMessage(), uoe);
                            continue;
                        } catch (IOException ioe) {
                            listenerForwarder.failed(ioe);
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn(ioe.getLocalizedMessage(),ioe);
                            continue;
                        } catch (IllegalArgumentException iae) {
                            listenerForwarder.failed(iae);
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn(iae.getLocalizedMessage(),iae);
                            continue;
                        } finally {

                            listenerForwarder.setProgress((100) / ((events.size() != 0) ? events
                                    .size() : 1));
                            listenerForwarder.progressing();
                        }
                    }
                } else {
                    final String message = "The passed file event refers to a not existent "
                            + "or not readable/writeable file! File: "
                            + eventFile.getAbsolutePath();
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn(message);
                    final IllegalArgumentException iae = new IllegalArgumentException(message);
                    listenerForwarder.failed(iae);
                }
            } // endwile
            listenerForwarder.completed();

            // return
            if (ret.size() > 0) {
                events.clear();
                return ret;
            } else {
                /*
                 * If here: we got an error no file are set to be returned the input queue is
                 * returned
                 */
                return events;
            }
        } catch (Exception t) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(t.getLocalizedMessage(), t);
            final ActionException exc = new ActionException(this, t.getLocalizedMessage(), t);
            listenerForwarder.failed(exc);
            throw exc;
        }
    }
}
