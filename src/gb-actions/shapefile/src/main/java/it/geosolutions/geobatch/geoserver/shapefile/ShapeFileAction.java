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
package it.geosolutions.geobatch.geoserver.shapefile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Collector;
import it.geosolutions.geobatch.tools.file.Compressor;
import it.geosolutions.geobatch.tools.file.Extract;
import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Geoserver ShapeFile action.
 * 
 * Process shapefiles and inject them into a Geoserver instance.
 * 
 * Accept:<br>
 * - a list of mandatory files(ref. to the shape file standard for details) - a compressed archive
 * (ref. to the Extract class to see accepted formats)
 * 
 * Check the content of the input and build a valid ZIP file which represent the output of this
 * action.
 * 
 * The same output is sent to the configured GeoServer using the GS REST api.
 * 
 * TODO: check for store/layer existence
 * 
 * @author AlFa
 * @author ETj
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version 0.1 - date: 11 feb 2007
 * @version 0.2 - date: 25 Apr 2011
 */
public class ShapeFileAction extends BaseAction<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShapeFileAction.class);

    // private ShapeFileConfiguration configuration;
    private GeoServerActionConfiguration configuration;

    public ShapeFileAction(final GeoServerActionConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    /**
     * 
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {

        listenerForwarder.setTask("config");
        listenerForwarder.started();

        try {
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            if (configuration == null) {
                // LOGGER.error("ActionConfig is null."); // we're
                // rethrowing it, so don't log
                throw new IllegalStateException("ActionConfig is null.");
            }

            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            final File workingDir = Path.findLocation(configuration.getWorkingDirectory(),
                    ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory());

            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if (workingDir == null) {
                // LOGGER.error("Working directory is null."); //
                // we're rethrowing it, so don't log
                throw new IllegalStateException("Working directory is null.");
            }

            if (!workingDir.exists() || !workingDir.isDirectory()) {
                // LOGGER.error(//
                // "Working directory does not exist ("+workingDir.getAbsolutePath()+").");
                // // we're rethrowing it, so don't log
                throw new IllegalStateException("Working directory does not exist ("
                        + workingDir.getAbsolutePath() + ").");
            }

            // Fetch the first event in the queue.
            // We may have one in these 2 cases:
            // 1) a single event for a .zip file
            // 2) a list of events for the .shp+.dbf+.shx+ some other optional
            // files

            final FileSystemEvent event = events.peek();

            // the name of the shapefile
            String shapeName = null;

            // the output (to send to the geoserver) file
            File zippedFile = null;

            // list of file to send to the GeoServer
            final File[] files;
            if (events.size() == 1) {

                zippedFile = event.getSource();

                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("Testing for compressed file: " + zippedFile.getAbsolutePath());

                final String tmpDirName = Extract.extract(zippedFile.getAbsolutePath());

                listenerForwarder.progressing(5, "File extracted");

                /*
                 * if the output (Extract) file is not a dir the event was a not compressed file so
                 * we have to throw and error
                 */
                final File tmpDirFile = new File(tmpDirName);
                if (!tmpDirFile.isDirectory()) {
                    throw new IllegalStateException("Not valid input: we need a zip file ");
                }

                // collect extracted files
                final Collector c = new Collector(FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(tmpDirName))); // no filter
                final List<File> fileList = c.collect(tmpDirFile);
                if (fileList != null) {
                    files = fileList.toArray(new File[1]);
                } else {
                    final String message = "Input is not a zipped file nor a valid collection of files";
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(message);
                    throw new IllegalStateException(message);
                }
                
            } else if (events.size() >= 3) {

                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("Checking input collection...");

                listenerForwarder.progressing(5, "Checking input collection...");

                /*
                 * build the collection of files
                 */
                files = new File[events.size()];
                int i = 0;
                for (FileSystemEvent ev : events) {
                    files[i++] = ev.getSource();
                }

            } else {
                throw new IllegalStateException(
                        "Input is not a zipped file nor a valid collection of files");
            }

            // obtain the shape file name and check for mondatory file
            if ((shapeName = acceptable(files)) == null) {
                throw new IllegalStateException("The file list do not contains mondadory files");
            }

//            zippedFile = Compressor.deflate(new File(configuration.getWorkingDirectory()),
//                    shapeName, files);
//            if (zippedFile == null) {
//                throw new IllegalStateException("Unable to create the zip file");
//            }

            listenerForwarder.progressing(10, "In progress");

            // TODO: check if the store do not exists and if so create it

            // TODO: check if a layer with the same name already exists in GS
            // GeoServerRESTReader reader = new GeoServerRESTReader(configuration.getGeoserverURL(),
            // configuration.getGeoserverUID(), configuration.getGeoserverPWD());

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////

            GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
                    configuration.getGeoserverURL(), configuration.getGeoserverUID(),
                    configuration.getGeoserverPWD());
            /*
             * Storename - same as layer. Layername - same as file name.
             */
            if (publisher.publishShp(configuration.getDefaultNamespace(), shapeName, shapeName,
                    zippedFile, configuration.getCrs(), configuration.getDefaultStyle())) {
                final String message = "Shape file SUCCESFULLY sent";
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(message);
                listenerForwarder.progressing(100, message);
            } else {
                final String message = "Shape file FAILED to be sent";
                final ActionException ae = new ActionException(this, message);
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message, ae);
                listenerForwarder.failed(ae);
            }

            // Removing old files...
            events.clear();

            // Adding the zipped file to send...
            events.add(new FileSystemEvent(zippedFile, FileSystemEventType.FILE_ADDED));
            return events;
        } catch (Throwable t) {
            final ActionException ae = new ActionException(this, t.getMessage(), t);
            if (LOGGER.isErrorEnabled())
                LOGGER.error(ae.getLocalizedMessage(), ae);
            LOGGER.error(t.getLocalizedMessage(), t); // we're
            // rethrowing it, so don't log
            listenerForwarder.failed(t); // fails the Action
            throw ae;
        }
    }

    /**
     * check for mandatory files in the passed list: .shp — shape format; the feature geometry
     * itself <br>
     * .shx — shape index format; a positional index of the feature geometry to allow seeking
     * forwards and backwards quickly <br>
     * .dbf — attribute format; columnar attributes for each shape, in dBase IV format
     * 
     * 
     * @param files
     *            a list of file to check for
     * @return null if 'files' do not contain needed files or contain more than 1 shape file, the
     *         name of the shape file otherwise.
     */
    private static String acceptable(final File[] files) {
        if (files == null)
            return null;

        String shapeFileName = null;
        // if ==3 the incoming file list is acceptable
        int acceptable = 0;
        for (File file : files) {
            if (file == null)
                continue;
            final String ext = FilenameUtils.getExtension(file.getAbsolutePath());

            if (ext.equals("shp")) {
                ++acceptable;
                /*
                 * check if there are more than 1 shp file in the list if so an error occur
                 */
                if (shapeFileName == null)
                    shapeFileName = FilenameUtils.getBaseName(file.getName());
                else
                    return null;
            } else if (ext.equals("shx")) {
                ++acceptable;
            } else if (ext.equals("dbf")) {
                ++acceptable;
            }
        }

        if (acceptable == 3) {
            return shapeFileName;
        } else
            return null;
    }

}
