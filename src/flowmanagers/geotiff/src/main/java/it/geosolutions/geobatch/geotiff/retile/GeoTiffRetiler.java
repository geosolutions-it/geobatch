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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.RenderedOp;

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
 * Comments here ...
 * 
 * @author Simone Giannechini, GeoSolutions
 * 
 * @version $GeoTIFFOverviewsEmbedder.java $ Revision: x.x $ 23/mar/07 11:42:25
 */
@SuppressWarnings("deprecation")
public class GeoTiffRetiler extends BaseAction<FileSystemMonitorEvent> implements
        Action<FileSystemMonitorEvent> {

    private GeoTiffRetilerConfiguration configuration;

    private final static Logger LOGGER = Logger
            .getLogger(GeoTiffRetiler.class.toString());

    protected GeoTiffRetiler(GeoTiffRetilerConfiguration configuration)
            throws IOException {
        this.configuration = configuration;
    }

	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {

            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            
            // get the first event
            final FileSystemMonitorEvent event = events.peek();
            final File inputFile = event.getSource();
        	final String absolutePath=inputFile.getAbsolutePath();
        	final String name= FilenameUtils.getName(absolutePath);
        	final String extension=FilenameUtils.getExtension(absolutePath); 
        	
            final File tiledInputFile=new File(inputFile.getParent(),name+"_tiled."+extension);
			//do we need to remove the input?
            if(!inputFile.renameTo(tiledInputFile)){
            	IOUtils.copyFile(inputFile, tiledInputFile);
            	IOUtils.deleteFile(inputFile);
            }

            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }

			// /////////////////////////////////////////////////////////////////////
			//
			// ACQUIRING A READER
			//
			// /////////////////////////////////////////////////////////////////////
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER.info("Acquiring a reader for the provided file...");
			// getting a reader for the given input
			final AbstractGridFormat format= (AbstractGridFormat) GridFormatFinder.findFormat(tiledInputFile);
			if(format ==null || format instanceof UnknownFormat)
				throw new IllegalArgumentException("Unable to find a reader for the file:"+tiledInputFile.getAbsolutePath());
			final AbstractGridCoverage2DReader reader= (AbstractGridCoverage2DReader) format.getReader(tiledInputFile, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));

			// /////////////////////////////////////////////////////////////////////
			//
			// ACQUIRING A COVERAGE
			//
			// /////////////////////////////////////////////////////////////////////
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER.info("Acquiring a coverage provided file...");
			final GridCoverage2D inCoverage = (GridCoverage2D) reader.read(null);
			
			// /////////////////////////////////////////////////////////////////////
			//
			// PREPARING A WRITE
			//
			// /////////////////////////////////////////////////////////////////////
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER.info("Writing down the file in the decoded directory...");
			final double compressionRatio=configuration.getCompressionRatio();
			final String compressionType=configuration.getCompressionScheme();
			
			
			final GeoTiffFormat wformat = new GeoTiffFormat();
			final GeoTiffWriteParams wp = new GeoTiffWriteParams();
			if (!Double.isNaN(compressionRatio)&&compressionType!=null) {
				wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
				wp.setCompressionType(compressionType);
				wp.setCompressionQuality((float) compressionRatio);
			}
			wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
			wp.setTiling(configuration.getTileW(), configuration.getTileH());
			final ParameterValueGroup wparams = wformat.getWriteParameters();
			wparams.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

			
			// keep original name
			final File outFile = event.getSource();			
			
			// /////////////////////////////////////////////////////////////////////
			//
			// ACQUIRING A WRITER AND PERFORMING A WRITE
			//
			// /////////////////////////////////////////////////////////////////////
			final AbstractGridCoverageWriter writer = (AbstractGridCoverageWriter) new GeoTiffWriter(outFile);
			writer.write(inCoverage, (GeneralParameterValue[]) wparams.values().toArray(new GeneralParameterValue[1]));

			// /////////////////////////////////////////////////////////////////////
			//
			// PERFORMING FINAL CLEAN UP AFTER THE WRITE PROCESS
			//
			// /////////////////////////////////////////////////////////////////////
			final RenderedOp initImage = (RenderedOp) inCoverage.getRenderedImage();
			ImageReader r = (ImageReader) initImage.getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
			r.dispose();
			Object input = r.getInput();
			if (input instanceof ImageInputStream) 
				((ImageInputStream) input).close();
			initImage.dispose();
			writer.dispose();
			reader.dispose();		
			
			//do we need to remove the input?
            if(!tiledInputFile.renameTo(inputFile)){
            	IOUtils.copyFile(tiledInputFile,inputFile);
            	IOUtils.deleteFile(tiledInputFile);
            }

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }

    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }

}
