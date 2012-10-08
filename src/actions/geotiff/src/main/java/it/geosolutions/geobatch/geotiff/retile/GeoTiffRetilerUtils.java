package it.geosolutions.geobatch.geotiff.retile;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

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

final class GeoTiffRetilerUtils {

	final static Logger LOGGER = LoggerFactory.getLogger(GeoTiffRetilerUtils.class);
	
	public static File reTile(final File inputFile, final GeotiffRetilerConfiguration configuration, final File tempDirectory) throws IOException{
        final String absolutePath = inputFile.getAbsolutePath();
        final String inputFileName = FilenameUtils.getName(absolutePath);		
		File tiledTiffFile = File.createTempFile(inputFile.getName(), "_tiled.tif", tempDirectory);
        if (tiledTiffFile.exists()) {
            // file already exists
            // check write permission
            if (!tiledTiffFile.canWrite()) {
                final String message = "Unable to over-write the temporary file called: "
                        + tiledTiffFile.getAbsolutePath() + "\nCheck permissions.";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(message);
                }
                throw new IllegalArgumentException(message);
            }
        } else if (!tiledTiffFile.createNewFile()) {
            final String message = "Unable to create temporary file called: "
                    + tiledTiffFile.getAbsolutePath();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message);
            }
            throw new IllegalArgumentException(message);
        }
        final double compressionRatio = configuration.getCompressionRatio();
        final String compressionType = configuration.getCompressionScheme();
        
        GeoTiffRetilerUtils.reTileInternal(inputFile,tiledTiffFile,compressionRatio,compressionType,configuration.getTileW(), configuration.getTileH(),configuration.isForceToBigTiff());

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

	private static void reTileInternal(File inFile, File tiledTiffFile, double compressionRatio, String compressionType, int tileW, int tileH, boolean forceBigTiff) throws IOException {
	    //
	    // look for a valid file that we can read
	    //
	
	    AbstractGridFormat format = null;
	    AbstractGridCoverage2DReader reader = null;
	    GridCoverage2D inCoverage = null;
	    AbstractGridCoverageWriter writer = null;
	
	    // getting a format for the given input
	    format = (AbstractGridFormat) GridFormatFinder.findFormat(inFile);
	    if (format == null || (format instanceof UnknownFormat)) {
	        throw new IllegalArgumentException("Unable to find the GridFormat for the provided file: "+ inFile);
	    }
	    
	    try {
	        //
	        // ACQUIRING A READER
	        //
	        if (LOGGER.isInfoEnabled()) {
	            LOGGER.info("Acquiring a reader for the provided file...");
	        }
	
	        // can throw UnsupportedOperationsException
	        reader = (AbstractGridCoverage2DReader) format.getReader(inFile, new Hints(
	                Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
	        
	        if (reader == null) {
	            final IOException ioe = new IOException("Unable to find a reader for the provided file: "
	                            + inFile);
	            throw ioe;
	        }
	
	        //
	        // ACQUIRING A COVERAGE
	        //
	        if (LOGGER.isInfoEnabled()) {
	            LOGGER.info("Acquiring a coverage for the provided file...");
	        }
	        inCoverage = (GridCoverage2D) reader.read(null);
	        if (inCoverage == null) {
	            final IOException ioe = new IOException("inCoverage == null");
	            throw ioe;
	        }
	        
	        //
	        // PREPARING A WRITE
	        //
	        if (LOGGER.isInfoEnabled()) {
	            LOGGER.info("Writing down the file in the decoded directory...");
	        }
	
	        final GeoTiffWriteParams wp = new GeoTiffWriteParams();
	        // compression
	        if (!Double.isNaN(compressionRatio) && compressionType != null) {
	            wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
	            wp.setCompressionType(compressionType);
	            wp.setCompressionQuality((float) compressionRatio);
	        }
	        //is bigtiff
	        wp.setForceToBigTIFF(forceBigTiff);        
	        // control tiling
	        wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
	        wp.setTiling(tileW, tileH);
	        final ParameterValueGroup wparams = GEOTIFF_FORMAT.getWriteParameters();
	        wparams.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
	
	        //
	        // ACQUIRING A WRITER AND PERFORMING A WRITE
	        //
	        writer = (AbstractGridCoverageWriter) new GeoTiffWriter(tiledTiffFile);
	        writer.write(inCoverage,
	                (GeneralParameterValue[]) wparams.values()
	                        .toArray(new GeneralParameterValue[1]));
	
	    } finally {
	        //
	        // PERFORMING FINAL CLEAN UP AFTER THE WRITE PROCESS
	        //
	        if (reader != null) {
	            try {
	                reader.dispose();
	            } catch (Exception e) {
	                if (LOGGER.isWarnEnabled())
	                    LOGGER.warn(e.getLocalizedMessage(), e);
	            }
	
	        }
	
	        if (writer != null) {
	            try {
	                writer.dispose();
	            } catch (Exception e) {
	                if (LOGGER.isWarnEnabled())
	                    LOGGER.warn(e.getLocalizedMessage(), e);
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
	
	}

	public static final GeoTiffFormat GEOTIFF_FORMAT = new GeoTiffFormat();


}
