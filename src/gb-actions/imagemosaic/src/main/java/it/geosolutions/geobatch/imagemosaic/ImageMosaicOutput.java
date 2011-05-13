package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

class ImageMosaicOutput {

    private final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicOutput.class);

    /*
     * used to read properties from an already created imagemosaic.
     */
    private static final AbstractGridFormat IMAGEMOSAIC_FORMAT = new ImageMosaicFormat();

    /**
     * Write the ImageMosaic output to an XML file using XStream
     * 
     * @param outputDir
     *            directory where to place the output
     * @param layerResponse
     *            must not be null and should contain the following elements:<br>
     *            result[0]: the storename<br>
     *            result[1]: the namespace<br>
     *            result[2]: the layername<br>
     * @param mosaicDescriptor
     * @param cmd
     *            the image mosaic command
     * @return
     */
    protected static File writeReturn(File outputDir, String[] layerResponse,
            ImageMosaicGranulesDescriptor mosaicDescriptor, ImageMosaicCommand cmd) {

        final String storename = layerResponse[0];
        final String workspace = layerResponse[1];
        final String layername = layerResponse[2];

        final File layerDescriptor = new File(outputDir, layername + ".xml");

        FileWriter outFile = null;
        try {
            if (layerDescriptor.createNewFile()) {

                try {
                    final XStream xstream = new XStream();
                    outFile = new FileWriter(layerDescriptor);
                    // the output structure
                    Map<String, Object> outMap = new HashMap<String, Object>();

                    outMap.put(STORENAME_KEY, storename);
                    outMap.put(WORKSPACE_KEY, workspace);
                    outMap.put(LAYERNAME_KEY, layername);

                    setReaderData(cmd.getBaseDir(), outMap);

                    xstream.toXML(outMap, outFile);

                } catch (XStreamException e) {
                    // XStreamException - if the object cannot be serialized
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("ImageMosaicAction.writeReturn(): setReturn the object cannot be serialized");
                } finally {
                    IOUtils.closeQuietly(outFile);
                }

                // PrintWriter out = null;
                // try {
                //
                // /*
                // * F.E. a layer called 'data' will result in: namespace=topp metocFields=data
                // * storeid=data layerid=data driver=ImageMosaic path=/
                // */
                // outFile = new FileWriter(layerDescriptor);
                // out = new PrintWriter(outFile);
                // // Write text to file
                // // out.println("namespace=" + layerResponse[1]);
                // // out.println("metocFields=" + mosaicDescriptor.getMetocFields());
                // // out.println("storeid=" + mosaicDescriptor.getCoverageStoreId());
                // // out.println("layerid=" + inputDir.getName());
                // // out.println("driver=ImageMosaic");
                // // out.println("path=" + File.separator);
                // } catch (IOException e) {
                // if (LOGGER.isErrorEnabled())
                // LOGGER.error("Error occurred while writing indexer.properties file!", e);
                // } finally {
                // if (out != null) {
                // out.flush();
                // out.close();
                // }
                //
                // outFile = null;
                // out = null;
                // }
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("ImageMosaic:setReturn(): unable to create the output file: "
                            + layerDescriptor.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("ImageMosaic:setReturn(): " + e.getMessage(), e);
            }
        }
        return layerDescriptor;
    }

    private static String HAS_TIME_DOMAIN_KEY = "HAS_TIME_DOMAIN";

    private static String TIME_DOMAIN_KEY = "TIME_DOMAIN";

    private static String NATIVE_LOWER_CORNER_FIRST_KEY = "NATIVE_LOWER_CORNER_FIRST";

    private static String NATIVE_LOWER_CORNER_SECOND_KEY = "NATIVE_LOWER_CORNER_SECOND";

    private static String NATIVE_UPPER_CORNER_FIRST_KEY = "NATIVE_UPPER_CORNER_FIRST";

    private static String NATIVE_UPPER_CORNER_SECOND_KEY = "NATIVE_UPPER_CORNER_SECOND";

    private static String LONLAT_LOWER_CORNER_FIRST_KEY = "LONLAT_LOWER_CORNER_FIRST";

    private static String LONLAT_LOWER_CORNER_SECOND_KEY = "LONLAT_LOWER_CORNER_SECOND";

    private static String LONLAT_UPPER_CORNER_FIRST_KEY = "LONLAT_UPPER_CORNER_FIRST";

    private static String LONLAT_UPPER_CORNER_SECOND_KEY = "LONLAT_UPPER_CORNER_SECOND";

    private static String CRS_KEY = "CRS";

    private static String STORENAME_KEY = "STORENAME";

    private static String WORKSPACE_KEY = "WORKSPACE";

    private static String LAYERNAME_KEY = "LAYERNAME";

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

            if (!IMAGEMOSAIC_FORMAT.accepts(directory))
                throw new IOException("PUPPA");
            reader = (AbstractGridCoverage2DReader) IMAGEMOSAIC_FORMAT.getReader(directory,
                    new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
            if (reader == null) {
                final IOException ioe = new IOException(
                        "Unable to find a reader for the provided file: " + inputFileName);
                throw ioe;
            }

            // HAS_TIME_DOMAIN this is a boolean String with values true|false. Meaning is obvious.
            // TIME_DOMAIN this returns a String that is a comma separated list of time values in
            // ZULU time using the ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss.SSS)
            if (reader.getMetadataValue(HAS_TIME_DOMAIN_KEY) == "true") {
                map.put(HAS_TIME_DOMAIN_KEY, Boolean.TRUE);
                final String times = reader.getMetadataValue(TIME_DOMAIN_KEY);
                final List<String> timesList = new ArrayList<String>();
                for (String time : times.split(",")) {
                    timesList.add(time);
                }
                map.put(TIME_DOMAIN_KEY, timesList);
            } else {
                map.put(HAS_TIME_DOMAIN_KEY, Boolean.FALSE);
            }

            final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
            // Setting BoundingBox
            DirectPosition position=originalEnvelope.getLowerCorner();
            double[] lowerCorner = position.getCoordinate();
            map.put(NATIVE_LOWER_CORNER_FIRST_KEY, (Double)lowerCorner[0]);
            map.put(NATIVE_LOWER_CORNER_SECOND_KEY,(Double)lowerCorner[1]);
            position=originalEnvelope.getUpperCorner();
            double[] upperCorner = position.getCoordinate();
            map.put(NATIVE_UPPER_CORNER_FIRST_KEY, (Double)upperCorner[0]);
            map.put(NATIVE_UPPER_CORNER_SECOND_KEY, (Double)upperCorner[1]);

            // Setting crs
            map.put(CRS_KEY, reader.getCrs());

            // computing lon/lat bbox
            final CoordinateReferenceSystem wgs84;
            try {
                wgs84 = CRS.decode("EPSG:4326", true);
                final GeneralEnvelope lonLatBBOX = (GeneralEnvelope) CRS.transform(
                        originalEnvelope, wgs84);
                // Setting BoundingBox
                position=lonLatBBOX.getLowerCorner();
                lowerCorner = position.getCoordinate();
                map.put(LONLAT_LOWER_CORNER_FIRST_KEY, (Double)lowerCorner[0]);
                map.put(LONLAT_LOWER_CORNER_SECOND_KEY, (Double)lowerCorner[1]);
                position=lonLatBBOX.getUpperCorner();
                upperCorner = position.getCoordinate();
                map.put(LONLAT_UPPER_CORNER_FIRST_KEY, (Double)upperCorner[0]);
                map.put(LONLAT_UPPER_CORNER_SECOND_KEY, (Double)upperCorner[1]);
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
