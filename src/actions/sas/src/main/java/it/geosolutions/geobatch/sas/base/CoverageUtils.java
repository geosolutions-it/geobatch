/*
 */

package it.geosolutions.geobatch.sas.base;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicReader;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class CoverageUtils {
    private final static Logger LOGGER = Logger.getLogger(CoverageUtils.class.getName());

    public static void checkGeotiff(final File tiffFile) throws IllegalStateException {
            if ( tiffFile == null || ! tiffFile.exists() ) {
                throw new IllegalStateException("Bad geotiff file.");
            }

        final GeoTiffFormat format = new GeoTiffFormat();
        GeoTiffReader coverageReader = null;
        // //
        // Trying to read the geotiff
        // //
        try {
            coverageReader = (GeoTiffReader) format.getReader(tiffFile);
            if (coverageReader == null) {
                LOGGER.log(Level.SEVERE, "No valid geotiff: " + tiffFile);
                throw new IllegalStateException("No valid geotiff: " + tiffFile);
            }
        } finally {
            if (coverageReader != null) {
                try {
                    coverageReader.dispose();
                } catch (Throwable e) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    public static void checkMosaic(final File mosaicDir) throws IllegalStateException {
        if ( mosaicDir == null ||
                ! mosaicDir.exists() ||
                ! mosaicDir.isDirectory()) {
            throw new IllegalStateException("Bad mosaicDir.");
        }

        final ImageMosaicFormat format = new ImageMosaicFormat();
        ImageMosaicReader coverageReader = null;
        // //
        // Trying to read the mosaic
        // //
        try {
            coverageReader = (ImageMosaicReader) format.getReader(mosaicDir);
            if (coverageReader == null) {
                LOGGER.log(Level.SEVERE, "No valid Mosaic found for this Data Flow!");
                throw new IllegalStateException("No valid Mosaic found for this Data Flow!");
            }
        } finally {
            if (coverageReader != null) {
                try {
                    coverageReader.dispose();
                } catch (Throwable e) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

}
