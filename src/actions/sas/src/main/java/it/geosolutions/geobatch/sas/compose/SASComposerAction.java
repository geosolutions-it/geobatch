/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
package it.geosolutions.geobatch.sas.compose;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.sas.base.SASDirNameParser;
import it.geosolutions.geobatch.sas.base.SASUtils;
import it.geosolutions.geobatch.sas.base.SASUtils.FolderContentType;
import it.geosolutions.geobatch.sas.event.SASMosaicEvent;
import it.geosolutions.geobatch.sas.event.SASTileEvent;
import it.geosolutions.geobatch.sas.mosaic.MosaicerAction;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.opensdi.sas.model.Layer;
import it.geosolutions.opensdi.sas.model.Type;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class SASComposerAction
        extends BaseAction<FileSystemMonitorEvent>
        implements Action<FileSystemMonitorEvent> {

    protected final static Logger LOGGER = Logger.getLogger(SASComposerAction.class.toString());
    protected final SASComposerConfiguration configuration;

    /**
     *
     * @param configuration
     */
    public SASComposerAction(SASComposerConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    /**
     * 
     * @param events
     * @return
     * @throws ActionException
     */
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws ActionException {
        try {
            listenerForwarder.started();

            // looking for file
            if (events.size() != 1) {
                throw new IllegalArgumentException("Wrong number of elements for this action: " + events.size());
            }

            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            Queue<FileSystemMonitorEvent> sasEvents = new LinkedList<FileSystemMonitorEvent>();

            SASComposerUtils.setJAIHints(configuration);

            // get the first event
            final FileSystemMonitorEvent event = events.remove();
            final File inputFile = event.getSource();

            // //
            // Get the directory containing the data from the specified
            // XML file
            // //
            final List<String> missionDirs = SASUtils.getDataDirectories(inputFile, FolderContentType.LEGS);

            if (missionDirs == null || missionDirs.isEmpty()) {
                LOGGER.warning("Unable to find LegData location from the specified file: " + inputFile.getAbsolutePath());
                return events;
            }
            final int nMissions = missionDirs.size();
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(new StringBuilder("Found ").append(nMissions).append(" mission").append(nMissions > 1 ? "s" : "").toString());
            }

            for (String sMissionDir : missionDirs) {
                String initTime = null;
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Processing Mission: " + sMissionDir);
                }

                // Preparing parameters
                final String inputFormats = configuration.getInputFormats();
                final String outputFormat = configuration.getOutputFormat();
                final String baseDir = configuration.getOutputBaseFolder();

                //TODO: Refactor this search to leverage on a PATH_DEPTH parameter.
                //Actually is looking for specifiedDir/dirdepth1/dirdepth2/

                // //
                //
                // Checking LEGS for the current MISSION
                //
                // //
                ArrayList<File> missionSubDirs = null;
                final File missionDir = new File(sMissionDir); //Mission dir
                if (missionDir != null && missionDir.isDirectory()) {
                    final File[] foundDirs = missionDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
                    if (foundDirs != null && foundDirs.length > 0) {
                        missionSubDirs = new ArrayList<File>();
                        for (File foundDir : foundDirs) {
//	                        if (file.exists() && file.isDirectory()){
                            missionSubDirs.add(foundDir);
//	                        }
                        }
                    }
                } else {
                    LOGGER.warning("Skipping bad mission dir " + missionDir);
                }

                // //
                //
                // Mission Scan: Looking for LEGS
                //
                // //
                if (missionSubDirs != null && !missionSubDirs.isEmpty()) {
                    Collections.sort(missionSubDirs);

                    final Set<String> leavesSet = new HashSet<String>(Arrays.asList("port","stbd"));

                    // //
                    //
                    // Leg Scan
                    //
                    // //
                    for (File legDir : missionSubDirs) {
                        if (legDir.isDirectory()) {
                            final File subFolders[] = legDir.listFiles();
                            if (subFolders != null) {

                                // //
                                //
                                // Channel scan (leaves)
                                //
                                // //
                                for (File leaf : subFolders) {
                                    final String leafName = leaf.getName();
                                    if (leavesSet.contains(leafName)) {

                                        final String leafPath = leaf.getAbsolutePath();

                                        // Initialize time
                                        if (initTime == null) {
                                            initTime = SASUtils.setInitTime(leafPath, 2);
                                        }

                                        //Build the output directory path
                                        final StringBuilder outputDir = new StringBuilder(baseDir)
                                                .append(SASUtils.SEPARATOR).append(initTime)
                                                .append(SASUtils.SEPARATOR).append(missionDir.getName())
                                                .append(SASUtils.SEPARATOR).append(legDir.getName())
                                                .append(SASUtils.SEPARATOR).append(leafName);

                                        // //
                                        // 1) PROCESS SINGLE TILES
                                        //

                                        Queue<FileSystemMonitorEvent> tileEvents = SASComposerUtils.processTiles(events,
                                                configuration,
                                                leafPath, outputDir.toString(),
                                                inputFormats, outputFormat,
                                                configuration.getRawScaleAlgorithm());

                                        if (tileEvents == null) {
                                            throw new ActionException(this, "Unable to proceed with the mosaic composition due to problems occurred during conversion");
                                        }
                                        
                                        for (FileSystemMonitorEvent tile : tileEvents) {
                                        	((SASTileEvent) tile).getLegNames().add(legDir.getName() + "_" + leafName.toUpperCase());
                                        	((SASTileEvent) tile).setType(Type.valueOf(leafName.toUpperCase()));
                                        }
                                        sasEvents.addAll(tileEvents);

                                        // //
                                        // 2) MOSAIC COMPOSITION
                                        //

                                        final Layer mosaicTobeIngested = SASComposerUtils.composeMosaic(events,
                                                configuration,
                                                outputDir.toString(),
                                                configuration.getMosaicScaleAlgorithm());

                                        // //
                                        // 3) MOSAIC INGESTION: prepare next events
                                        //
                                        
                                        if (mosaicTobeIngested != null) {
//                                                final String style = SasMosaicGeoServerAction.SAS_STYLE;
                                            final int index = mosaicTobeIngested.getFileURL().lastIndexOf(MosaicerAction.MOSAIC_PREFIX);

                                            //Setting up the wmspath.
                                            //Actually it is set by simply changing mosaic's name underscores to slashes.
                                            //TODO: can be improved
                                            final String path = mosaicTobeIngested.getFileURL().substring(index + MosaicerAction.MOSAIC_PREFIX.length());
                                            final String wmsPath = SASUtils.buildWmsPath(path);

                                            final File realDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                                                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
                                        	SASMosaicEvent mosaicEvent = new SASMosaicEvent(realDir);
                                            mosaicEvent.setWmsPath(wmsPath);
                                            mosaicEvent.setFormat("imagemosaic");
                                            mosaicEvent.getLegNames().add(legDir.getName() + "_" + leafName.toUpperCase());
                                            mosaicEvent.setType(Type.valueOf(leafName.toUpperCase()));
                                            mosaicEvent.setLayer(mosaicTobeIngested);
                                            if (LOGGER.isLoggable(Level.FINE)) {
                                                LOGGER.fine("Adding " + mosaicEvent);
                                            }

                                            SASDirNameParser nameParser = SASDirNameParser.parse(path);
                                            if(nameParser != null) {
                                                //mosaicEvent.setMissionName(FilenameUtils.getBaseName(new File(mosaicTobeIngested.getFileURL()).getParentFile().getParentFile().getParent()));
                                            	mosaicEvent.setMissionName(nameParser.getMission());
                                            } else {
                                            	LOGGER.severe("ATTENTION: Unparsable Mission name for Mosaic Event!");
                                            }
                                            sasEvents.add(mosaicEvent);
                                        } else {
                                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                                LOGGER.severe("unable to build a mosaic for the following dataset:" + leafPath);
                                            }
                                        }
                                    } else {
                                        LOGGER.info("Skipping unknown leaf dir " + leafName);
                                    }
                                }
                            }
                        }
                    }

                } else {
                    LOGGER.info("No mission subdirs for" + missionDir);
                }
            }
            listenerForwarder.completed();
            return sasEvents;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            }
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
        builder.append(" [");
        if (configuration != null) {
            builder.append("configuration=").append(configuration);
        }
        builder.append("]");
        return builder.toString();
    }
}
