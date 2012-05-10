/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

public class ImageMosaicOutput {
    
    public static String NATIVE_LOWER_CORNER_FIRST_KEY = "NATIVE_LOWER_CORNER_FIRST";

    public static String NATIVE_LOWER_CORNER_SECOND_KEY = "NATIVE_LOWER_CORNER_SECOND";

    public static String NATIVE_UPPER_CORNER_FIRST_KEY = "NATIVE_UPPER_CORNER_FIRST";

    public static String NATIVE_UPPER_CORNER_SECOND_KEY = "NATIVE_UPPER_CORNER_SECOND";

    public static String LONLAT_LOWER_CORNER_FIRST_KEY = "LONLAT_LOWER_CORNER_FIRST";

    public static String LONLAT_LOWER_CORNER_SECOND_KEY = "LONLAT_LOWER_CORNER_SECOND";

    public static String LONLAT_UPPER_CORNER_FIRST_KEY = "LONLAT_UPPER_CORNER_FIRST";

    public static String LONLAT_UPPER_CORNER_SECOND_KEY = "LONLAT_UPPER_CORNER_SECOND";

    public static String CRS_KEY = "CRS";

    public static String STORENAME = "STORENAME";

    public static String WORKSPACE = "WORKSPACE";

    public static String LAYERNAME = "LAYERNAME";
    
    public static String DEFAULT_STYLE = "DEFAULT_STYLE";
    

    private final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicOutput.class);

    /*
     * used to read properties from an already created imagemosaic.
     */
    private static final AbstractGridFormat IMAGEMOSAIC_FORMAT = new ImageMosaicFormat();
    
    

    /**
     * /**
     * Write the ImageMosaic output to an XML file using XStream
     * 
     * @param baseDir
     * @param outDir
     *            directory where to place the output
     * @param storename
     * @param workspace
     * @param layername
     * @return
     */
    protected static File writeReturn(final File baseDir, final File outDir, final ImageMosaicCommand cmd) {

        final String layerName=cmd.getLayerName();
        final File layerDescriptor = new File(outDir, layerName + ".xml");
        FileWriter outFile=null;
        try {
            final XStream xstream = new XStream();
            outFile = new FileWriter(layerDescriptor);
            // the output structure
            Map<String, Object> outMap = new HashMap<String, Object>();

            outMap.put(STORENAME, cmd.getStoreName());
            outMap.put(WORKSPACE, cmd.getDefaultNamespace());
            outMap.put(LAYERNAME, layerName);
            outMap.put(DEFAULT_STYLE, cmd.getDefaultStyle());
            // TODO ADD AS NEEDED ...

            setReaderData(baseDir, outMap);

            xstream.toXML(outMap, outFile);
        } catch (XStreamException e) {
            // XStreamException - if the object cannot be serialized
            if (LOGGER.isErrorEnabled())
                LOGGER.error("setReturn the object cannot be serialized");
        } catch (IOException e) {
            // XStreamException - if the object cannot be serialized
            if (LOGGER.isErrorEnabled())
                LOGGER.error("setReturn the object cannot be serialized");
        } finally {
            IOUtils.closeQuietly(outFile);
        }
        return layerDescriptor;
    }
    

    /**
     * using ImageMosaic reader extract needed data from the mosaic
     */
    private static boolean setReaderData(final File directory, final Map<String, Object> map)
            throws IOException {
        AbstractGridCoverage2DReader reader = null;

        final String absolutePath = directory.getAbsolutePath();
        final String inputFileName = FilenameUtils.getName(absolutePath);
        try {

            // /////////////////////////////////////////////////////////////////////
            //
            // ACQUIRING A READER
            //
            // /////////////////////////////////////////////////////////////////////
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Acquiring a reader for the provided directory...");
            }

            if (!IMAGEMOSAIC_FORMAT.accepts(directory)) {
                final String message = "IMAGEMOSAIC_FORMAT do not accept the directory: "
                        + directory.getAbsolutePath();
                final IOException ioe = new IOException(message);
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message, ioe);
                throw ioe;
            }
            reader = (AbstractGridCoverage2DReader) IMAGEMOSAIC_FORMAT.getReader(directory,
                    new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
            if (reader == null) {
                final String message = "Unable to find a reader for the provided file: "
                        + inputFileName;
                final IOException ioe = new IOException(message);
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message, ioe);
                throw ioe;
            }
            
            // HAS_TIME_DOMAIN this is a boolean String with values true|false. Meaning is obvious.
            // TIME_DOMAIN this returns a String that is a comma separated list of time values in
            // ZULU time using the ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss.SSS)
            if (reader.getMetadataValue(AbstractGridCoverage2DReader.HAS_TIME_DOMAIN) == "true") {
                map.put(AbstractGridCoverage2DReader.HAS_TIME_DOMAIN, Boolean.TRUE);
                final String times = reader.getMetadataValue(AbstractGridCoverage2DReader.TIME_DOMAIN);
                final Set<String> timesList = new TreeSet<String>();
                for (String time : times.split(",")) {
                    timesList.add(time);
                }
//                Collections.sort(timesList);
                map.put(AbstractGridCoverage2DReader.TIME_DOMAIN, timesList);
            } else {
                map.put(AbstractGridCoverage2DReader.HAS_TIME_DOMAIN, Boolean.FALSE);
            }
            
            // ELEVATION
            if (reader.getMetadataValue(AbstractGridCoverage2DReader.HAS_ELEVATION_DOMAIN) == "true") {
                map.put(AbstractGridCoverage2DReader.HAS_ELEVATION_DOMAIN, Boolean.TRUE);
                final String elevations = reader.getMetadataValue(AbstractGridCoverage2DReader.ELEVATION_DOMAIN);
                final Set<String> elevList = new TreeSet<String>();
                for (String time : elevations.split(",")) {
                    elevList.add(time);
                }
                map.put(AbstractGridCoverage2DReader.ELEVATION_DOMAIN, elevList);
            } else {
                map.put(AbstractGridCoverage2DReader.HAS_ELEVATION_DOMAIN, Boolean.FALSE);
            }

            final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
            // Setting BoundingBox
            DirectPosition position = originalEnvelope.getLowerCorner();
            double[] lowerCorner = position.getCoordinate();
            map.put(NATIVE_LOWER_CORNER_FIRST_KEY, (Double) lowerCorner[0]);
            map.put(NATIVE_LOWER_CORNER_SECOND_KEY, (Double) lowerCorner[1]);
            position = originalEnvelope.getUpperCorner();
            double[] upperCorner = position.getCoordinate();
            map.put(NATIVE_UPPER_CORNER_FIRST_KEY, (Double) upperCorner[0]);
            map.put(NATIVE_UPPER_CORNER_SECOND_KEY, (Double) upperCorner[1]);

            // Setting crs
            map.put(CRS_KEY, reader.getCrs());

            // computing lon/lat bbox
            final CoordinateReferenceSystem wgs84;
            try {
                wgs84 = CRS.decode("EPSG:4326", true);
                final GeneralEnvelope lonLatBBOX = (GeneralEnvelope) CRS.transform(
                        originalEnvelope, wgs84);
                // Setting BoundingBox
                position = lonLatBBOX.getLowerCorner();
                lowerCorner = position.getCoordinate();
                map.put(LONLAT_LOWER_CORNER_FIRST_KEY, (Double) lowerCorner[0]);
                map.put(LONLAT_LOWER_CORNER_SECOND_KEY, (Double) lowerCorner[1]);
                position = lonLatBBOX.getUpperCorner();
                upperCorner = position.getCoordinate();
                map.put(LONLAT_UPPER_CORNER_FIRST_KEY, (Double) upperCorner[0]);
                map.put(LONLAT_UPPER_CORNER_SECOND_KEY, (Double) upperCorner[1]);
            } catch (NoSuchAuthorityCodeException e) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn(e.getLocalizedMessage(), e);
            } catch (FactoryException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(e.getLocalizedMessage(), e);
            } catch (TransformException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(e.getLocalizedMessage(), e);
            }

        } finally {
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Exception e) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn(e.getLocalizedMessage(), e);
                }

            }
        }
        return true;
    }
}
