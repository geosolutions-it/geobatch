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
package it.geosolutions.geobatch.geotiff.retile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageReader;
import javax.media.jai.PlanarImage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * ReTile the passed geotif image. NOTE: accept only one image per run
 * 
 * @author Simone Giannechini, GeoSolutions
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $GeoTIFFOverviewsEmbedder.java Revision: 0.1 $ 23/mar/07 11:42:25 Revision: 0.2 $
 *          15/Feb/11 14:40:00
 */
public class GeotiffRetiler extends BaseAction<FileSystemEvent> {

    private GeotiffRetilerConfiguration configuration;

    private final static Logger LOGGER = LoggerFactory.getLogger(GeotiffRetiler.class);

    public GeotiffRetiler(GeotiffRetilerConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    private File reTile(File inFile) throws IOException {
        //
        // look for a valid file that we can read
        //

        AbstractGridFormat format = null;
        AbstractGridCoverage2DReader reader = null;
        GridCoverage2D inCoverage = null;
        AbstractGridCoverageWriter writer = null;

        final String absolutePath = inFile.getAbsolutePath();
        final String inputFileName = FilenameUtils.getName(absolutePath);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("GeotiffRetiler: is going to retile: " + inputFileName);

        listenerForwarder.setTask("GeotiffRetiler");

        // getting a format for the given input
        format = (AbstractGridFormat) GridFormatFinder.findFormat(inFile);
        if (format == null || (format instanceof UnknownFormat)) {
            throw new IllegalArgumentException(
                    "GeotiffRetiler::reTile(): Unable to find the GridFormat for the provided file: "
                            + inputFileName);
        }
        
        // throws IOException
        final File tiledTiffFile = File.createTempFile(inputFileName, "_tiled.tif", inFile.getParentFile());

        try {
            if (tiledTiffFile.exists()) {
                // file already exists
                // check write permission
                if (!tiledTiffFile.canWrite()) {
                    final String message = "GeotiffRetiler::reTile(): Unable to over-write the temporary file called: "
                            + tiledTiffFile.getAbsolutePath() + "\nCheck permissions.";
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(message);
                    }
                    throw new IllegalArgumentException(message);
                }
            } else if (!tiledTiffFile.createNewFile()) {
                final String message = "GeotiffRetiler.reTile(): Unable to create temporary file called: "
                        + tiledTiffFile.getAbsolutePath();
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(message);
                }
                throw new IllegalArgumentException(message);
            }

            // /////////////////////////////////////////////////////////////////////
            //
            // ACQUIRING A READER
            //
            // /////////////////////////////////////////////////////////////////////
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("GeotiffRetiler.reTile(): Acquiring a reader for the provided file...");
            }

            // can throw UnsupportedOperationsException
            reader = (AbstractGridCoverage2DReader) format.getReader(inFile, new Hints(
                    Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
            
            if (reader == null) {
                final IOException ioe = new IOException(
                        "GeotiffRetiler::reTile(): Unable to find a reader for the provided file: "
                                + inputFileName);
                throw ioe;
            }

            // /////////////////////////////////////////////////////////////////////
            //
            // ACQUIRING A COVERAGE
            //
            // /////////////////////////////////////////////////////////////////////
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("GeotiffRetiler::reTile(): Acquiring a coverage provided file...");
            }
            inCoverage = (GridCoverage2D) reader.read(null);
            if (inCoverage == null) {
                final IOException ioe = new IOException(
                        "GeotiffRetiler::reTile(): inCoverage == null");
                throw ioe;
            }
            // /////////////////////////////////////////////////////////////////////
            //
            // PREPARING A WRITE
            //
            // /////////////////////////////////////////////////////////////////////
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("GeotiffRetiler::reTile(): Writing down the file in the decoded directory...");
            }
            final double compressionRatio = configuration.getCompressionRatio();
            final String compressionType = configuration.getCompressionScheme();

            final GeoTiffFormat wformat = new GeoTiffFormat();
            final GeoTiffWriteParams wp = new GeoTiffWriteParams();
            if (!Double.isNaN(compressionRatio) && compressionType != null) {
                wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
                wp.setCompressionType(compressionType);
                wp.setCompressionQuality((float) compressionRatio);
            }
            wp.setForceToBigTIFF(configuration.isForceToBigTiff());            
            wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
            wp.setTiling(configuration.getTileW(), configuration.getTileH());
            final ParameterValueGroup wparams = wformat.getWriteParameters();
            wparams.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

            // /////////////////////////////////////////////////////////////////////
            //
            // ACQUIRING A WRITER AND PERFORMING A WRITE
            //
            // /////////////////////////////////////////////////////////////////////
            writer = (AbstractGridCoverageWriter) new GeoTiffWriter(tiledTiffFile);
            writer.write(inCoverage,
                    (GeneralParameterValue[]) wparams.values()
                            .toArray(new GeneralParameterValue[1]));

        } finally {
            // /////////////////////////////////////////////////////////////////////
            //
            // PERFORMING FINAL CLEAN UP AFTER THE WRITE PROCESS
            //
            // /////////////////////////////////////////////////////////////////////
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Exception e) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("GeotiffRetiler::reTile(): " + e.getLocalizedMessage(), e);
                }

            }

            if (writer != null) {
                try {
                    writer.dispose();
                } catch (Exception e) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("GeotiffRetiler::reTile(): " + e.getLocalizedMessage(), e);
                }

            }

            if (inCoverage != null) {
                final RenderedImage initImage = inCoverage.getRenderedImage();
                ImageReader r = (ImageReader) initImage
                        .getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
                try {
                    r.dispose();
                } catch (Exception e) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("GeotiffRetiler::reTile(): " + e.getLocalizedMessage(), e);
                }

                // dispose
                ImageUtilities.disposePlanarImageChain(PlanarImage.wrapRenderedImage(initImage));

            }
        }

        String extension = FilenameUtils.getExtension(inputFileName);
        if (!extension.contains("tif")) {
            extension = "tif";
        }
        final String outputFileName = FilenameUtils.getFullPath(absolutePath)
                + FilenameUtils.getBaseName(inputFileName) + "." + extension;
        final File outputFile = new File(outputFileName);
        // do we need to remove the input?
        FileUtils.copyFile(tiledTiffFile, outputFile);
        FileUtils.deleteQuietly(tiledTiffFile);

        return outputFile;
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

                            try {

                                reTile(inFile);

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
                                    LOGGER.warn(
                                            "GeotiffRetiler::execute(): "
                                                    + uoe.getLocalizedMessage(), uoe);
                                continue;
                            } catch (IOException ioe) {
                                listenerForwarder.failed(ioe);
                                if (LOGGER.isWarnEnabled())
                                    LOGGER.warn(
                                            "GeotiffRetiler::execute(): "
                                                    + ioe.getLocalizedMessage(), ioe);
                                continue;
                            } catch (IllegalArgumentException iae) {
                                listenerForwarder.failed(iae);
                                if (LOGGER.isWarnEnabled())
                                    LOGGER.warn(
                                            "GeotiffRetiler::execute(): "
                                                    + iae.getLocalizedMessage(), iae);
                                continue;
                            } finally {
                                listenerForwarder.setProgress((progress * 100)
                                        / ((size != 0) ? size : 1));
                                listenerForwarder.progressing();
                            }
                        }

                        // add the directory to the return
                        ret.add(event);
                    } else {
                        // file is not a directory
                        File outFile = null;
                        try {
                            outFile = reTile(eventFile);
                            if (outFile != null) {
                                if (LOGGER.isInfoEnabled())
                                    LOGGER.info("GeotiffRetiler::execute(): SUCCESSFULLY completed work on: "
                                            + event.getSource());
                                listenerForwarder.setProgress(100);
                                ret.add(new FileSystemEvent(outFile, eventType));
                            } else {
                                ret.add(new FileSystemEvent(eventFile, eventType));
                                final String message = "GeotiffRetiler::execute(): retiler FAILED to return the output file: "
                                        + event.getSource();
                                if (LOGGER.isErrorEnabled())
                                    LOGGER.error(message);
                                throw new NullPointerException(message);
                            }
                        } catch (UnsupportedOperationException uoe) {
                            listenerForwarder.failed(uoe);
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn(
                                        "GeotiffRetiler::execute(): " + uoe.getLocalizedMessage(),
                                        uoe);
                            continue;
                        } catch (IOException ioe) {
                            listenerForwarder.failed(ioe);
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn(
                                        "GeotiffRetiler::execute(): " + ioe.getLocalizedMessage(),
                                        ioe);
                            continue;
                        } catch (IllegalArgumentException iae) {
                            listenerForwarder.failed(iae);
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn(
                                        "GeotiffRetiler::execute(): " + iae.getLocalizedMessage(),
                                        iae);
                            continue;
                        } finally {

                            listenerForwarder.setProgress((100) / ((events.size() != 0) ? events
                                    .size() : 1));
                            listenerForwarder.progressing();
                        }
                    }
                } else {
                    final String message = "GeotiffRetiler::execute(): The passed file event refers to a not existent "
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
            final String message = "GeotiffRetiler::execute(): " + t.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message, t);
            final ActionException exc = new ActionException(this, message, t);
            listenerForwarder.failed(exc);
            throw exc;
        }
    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }
}
