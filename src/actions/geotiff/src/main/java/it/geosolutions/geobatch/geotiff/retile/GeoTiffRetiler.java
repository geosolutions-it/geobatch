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

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.RenderedOp;

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
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

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
public class GeoTiffRetiler extends BaseAction<FileSystemEvent> {

    private GeoTiffRetilerConfiguration configuration;

    private final static Logger LOGGER = Logger.getLogger(GeoTiffRetiler.class.toString());

    protected GeoTiffRetiler(GeoTiffRetilerConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {
        try {
            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            //
            // look for a valid file that we can read
            //
            File inputFile = null;
            String absolutePath = null;
            String inputFileName =null;   
            AbstractGridFormat format=null;
            FileSystemEventType eventType=null;
            FileSystemEvent event=null;
            
            while(events.size()>0){
                event=events.remove();
                inputFile = event.getSource();
                absolutePath = inputFile.getAbsolutePath();
                inputFileName = FilenameUtils.getName(absolutePath);
                
                // getting a format for the given input
                format = (AbstractGridFormat) GridFormatFinder.findFormat(inputFile);
                if (format != null && !( format instanceof UnknownFormat)) {
                    eventType=event.getEventType();
                    break;
                }
                // bad file
                format=null;
            }
            event=null;
            
            // looking for file
            if (format==null) {
                throw new IllegalArgumentException("Unable to find a reader for the provided events: "
                        + events);
            }
            final File tiledTiffFile = new File(inputFile.getParent(), inputFileName + "_tiled.tif");


            // /////////////////////////////////////////////////////////////////////
            //
            // ACQUIRING A READER
            //
            // /////////////////////////////////////////////////////////////////////
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Acquiring a reader for the provided file...");
            }

            final AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) format
                    .getReader(inputFile, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,Boolean.TRUE));

            // /////////////////////////////////////////////////////////////////////
            //
            // ACQUIRING A COVERAGE
            //
            // /////////////////////////////////////////////////////////////////////
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Acquiring a coverage provided file...");
            }
            final GridCoverage2D inCoverage = (GridCoverage2D) reader.read(null);

            // /////////////////////////////////////////////////////////////////////
            //
            // PREPARING A WRITE
            //
            // /////////////////////////////////////////////////////////////////////
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Writing down the file in the decoded directory...");
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
            wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
            wp.setTiling(configuration.getTileW(), configuration.getTileH());
            final ParameterValueGroup wparams = wformat.getWriteParameters();
            wparams.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())
                    .setValue(wp);

            // /////////////////////////////////////////////////////////////////////
            //
            // ACQUIRING A WRITER AND PERFORMING A WRITE
            //
            // /////////////////////////////////////////////////////////////////////
            final AbstractGridCoverageWriter writer = (AbstractGridCoverageWriter) new GeoTiffWriter(tiledTiffFile);
            writer.write(inCoverage,
                    (GeneralParameterValue[]) wparams.values().toArray(new GeneralParameterValue[1]));

            // /////////////////////////////////////////////////////////////////////
            //
            // PERFORMING FINAL CLEAN UP AFTER THE WRITE PROCESS
            //
            // /////////////////////////////////////////////////////////////////////
            final RenderedOp initImage = (RenderedOp) inCoverage.getRenderedImage();
            ImageReader r = (ImageReader) initImage
                    .getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
            r.dispose();
            Object input = r.getInput();

            if (input instanceof ImageInputStream) {
                ((ImageInputStream) input).close();
            }
            initImage.dispose();
            writer.dispose();
            reader.dispose();

            final String outputFileName=
                FilenameUtils.getFullPath(absolutePath)+FilenameUtils.getBaseName(inputFileName)+".tif";
            final File outputFile=new File(outputFileName);
            // do we need to remove the input?
            FileUtils.copyFile(tiledTiffFile, outputFile);
            FileUtils.deleteQuietly(tiledTiffFile);
            
            // set the output
            events.clear();
            
            events.add(new FileSystemEvent(outputFile, eventType));

            return events;
        } catch (Exception t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            throw new ActionException(this, t.getMessage(), t);
        }

    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }
}
