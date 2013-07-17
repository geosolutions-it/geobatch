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
package it.geosolutions.geobatch.beam;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTStructuredGridCoverageReaderManager;
import it.geosolutions.tools.io.file.Copy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Harvest action. 
 * 
 * @author AlFa
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * @author Daniele Romagnoli
 * 
 * @TODO: we can probably think about moving this action on gb-action-geoserver
 * 
 */
public class HarvestGeoServerAction extends BaseAction<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(HarvestGeoServerAction.class);

    public HarvestGeoServerAction(HarvestGeoServerActionConfiguration configuration) throws IOException {
        super(configuration);
    }

    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {
        try {
            listenerForwarder.started();
            final HarvestGeoServerActionConfiguration configuration = getConfiguration();
            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                final String message = "DataFlowConfig is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }
            
            if (events == null) {
                final String message = "Incoming events queue is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }
            
            // returning queue
            final Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();

            // for each incoming file
            while (events.size() > 0) {
                final FileSystemEvent event = events.remove();
                final File inputFile = event.getSource();

                // checks on input file
                if (!inputFile.exists()) {
                    // ERROR or LOG since it does not exists
                    if (!configuration.isFailIgnored())
                        throw new IllegalStateException("File: " + inputFile.getAbsolutePath()
                                                        + " does not exist!");
                    else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("File: " + inputFile.getAbsolutePath() + " does not exist!");
                        }
                    }
                }
                // check if is File
                if (!inputFile.isFile()) {
                    // ERROR or LOG
                    if (!configuration.isFailIgnored())
                        throw new IllegalStateException("File: " + inputFile.getAbsolutePath()
                                                        + " is not a file!");
                    else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("File: " + inputFile.getAbsolutePath() + " is not a file!");
                        }
                    }
                }

                // check if we can read it
                if (!inputFile.canRead()) {
                    // ERROR or LOG
                    if (!configuration.isFailIgnored())
                        throw new IllegalStateException("File: " + inputFile.getAbsolutePath()
                                                        + " is not readable!");
                    else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("File: " + inputFile.getAbsolutePath() + " is not readablet!");
                        }
                    }
                }

                File fileToBeHarvested = inputFile;
                final String outputFolderPath = configuration.getOutputFolder();

                // Check if we need to copy the file into an outputFolder before doing the harvesting.
                if (outputFolderPath != null) {
                    final File outputDir = new File(outputFolderPath);
                    if (!outputDir.exists()) {
                        outputDir.mkdir();
                    }
                    fileToBeHarvested = new File(outputDir.getAbsolutePath(), FilenameUtils.getName(inputFile.getAbsolutePath()));
                    final String copiedFilePath = fileToBeHarvested.getAbsolutePath();
                    fileToBeHarvested = Copy.copyFileToNFS(inputFile, fileToBeHarvested, 5);
                    if (fileToBeHarvested == null) {
                        LOGGER.warn("Unable to copy file " + inputFile + " to " + copiedFilePath);
                    }

                }
                if (fileToBeHarvested != null) {
                    listenerForwarder.setTask("Publishing: " + fileToBeHarvested);

                    // try to harvest on geoserver
                    if (harvestFile(fileToBeHarvested, configuration)) {
                        listenerForwarder.progressing();
                    } else {
                        
                    }
                }
            }
            listenerForwarder.completed();
            return ret;
        } catch (Throwable t) {
            final String message = "FATAL -> " + t.getLocalizedMessage();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message, t); // no need to
            }
            listenerForwarder.failed(t);
            throw new ActionException(this, message, t);
        }
    }

    /**
     * If configuration.isFailIgnore and publish fails return false otherwise
     * true.<br>
     * If ! configuration.isFailIgnore and publish fails throws ActionException
     * otherwise return true.<br>
     * 
     * @param inputFile the file to publish
     * @param configuration the configuration to use
     * @return true if success, false otherwise.
     * @throws Exception
     */
    private static boolean harvestFile(File inputFile, GeoServerActionConfiguration configuration)
        throws Exception {
        //
        // SENDING data to GeoServer via REST protocol.
        //
        final GeoServerRESTStructuredGridCoverageReaderManager manager = new GeoServerRESTStructuredGridCoverageReaderManager(
                new URL(configuration.getGeoserverURL()), configuration.getGeoserverUID(), configuration.getGeoserverPWD());

        // check transfer method
        String transferMethod = configuration.getDataTransferMethod();
        if (transferMethod == null) {
            transferMethod = "EXTERNAL"; // default one
        }

        boolean result = manager.harvestExternal(configuration.getDefaultNamespace(), configuration.getStoreName(), "imagemosaic", inputFile.getAbsolutePath());
        
        if (result) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("file " + inputFile.getAbsolutePath() + " successfully harvested");
            }
            return true;
        } else {
            final String message = "file " + inputFile.getAbsolutePath() + " was NOT successfully harvested!";
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(message);
            }
            if (!configuration.isFailIgnored())
                throw new ActionException(HarvestGeoServerAction.class, message);
        }
        return false;
    }

}
