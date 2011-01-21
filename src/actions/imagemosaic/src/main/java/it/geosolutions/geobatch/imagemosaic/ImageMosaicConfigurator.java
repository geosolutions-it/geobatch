/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.geobatch.tools.time.TimeParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.JAI;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * Public class to ingest a geotiff image-mosaic into GeoServer
 * 
 */
public class ImageMosaicConfigurator extends ImageMosaicConfiguratorAction<FileSystemEvent> {

    /**
     * 
     */
    public final static String GEOSERVER_VERSION = "2.x";

    private static final TimeZone LAMMA_TZ = TimeZone.getTimeZone("GMT+2");

    protected ImageMosaicConfigurator(ImageMosaicActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    /**
	 * 
	 */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
            throws ActionException {

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Starting with processing...");

        listenerForwarder.started();

        try {
            // looking for file
            if (events.size() == 0)
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());

            Collection<FileSystemEvent> layers = new ArrayList<FileSystemEvent>();

            while (events.size() > 0) {
                FileSystemEvent event = events.remove();
                // final String configId = configuration.getName();

                // //
                // data flow configuration and dataStore name must not be null.
                // //
                if (configuration == null) {
                    LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                    throw new IllegalStateException("DataFlowConfig is null.");
                }
                // ////////////////////////////////////////////////////////////////////
                //
                // Initializing input variables
                //
                // ////////////////////////////////////////////////////////////////////
                final File workingDir = Path
                        .findLocation(configuration.getWorkingDirectory(), new File(
                                ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

                // ////////////////////////////////////////////////////////////////////
                //
                // Checking input files.
                //
                // ////////////////////////////////////////////////////////////////////
                if ((workingDir == null) || !workingDir.exists() || !workingDir.isDirectory()) {
                    LOGGER.log(Level.SEVERE, "GeoServerDataDirectory is null or does not exist.");
                    throw new IllegalStateException(
                            "GeoServerDataDirectory is null or does not exist.");
                }

                // ... BUSINESS LOGIC ... //
                File input = event.getSource();

                if (input.exists() && input.isFile()
                        && FilenameUtils.getExtension(input.getName()).equalsIgnoreCase("xml")) {
                    input = parseImageMosaicDirectory(input);
                }

                File inputDir = input;

                if (inputDir == null || !inputDir.exists() || !inputDir.isDirectory()) {
                    LOGGER
                            .log(Level.SEVERE, "Unexpected file '" + inputDir.getAbsolutePath()
                                    + "'");
                    throw new IllegalStateException("Unexpected file '"
                            + inputDir.getAbsolutePath() + "'");
                }

                // ////
                //
                // CREATE REGEX PROPERTIES FILES
                //
                // ////
                final File indexer = new File(inputDir, "indexer.properties");
                final File timeregex = new File(inputDir, "timeregex.properties");
                final File elevationregex = new File(inputDir, "elevationregex.properties");
                final File runtimeregex = new File(inputDir, "runtimeregex.properties");

                final File datastore = new File(inputDir, "datastore.properties");

                FileWriter outFile = null;
                PrintWriter out = null;

                // ////
                // INDEXER
                // ////
                if (!indexer.exists()) {
                    try {
                        outFile = new FileWriter(indexer);
                        out = new PrintWriter(outFile);

                        // Write text to file
                        if (configuration.getTimeRegex() != null)
                            out.println("TimeAttribute=ingestion");

                        if (configuration.getElevationRegex() != null)
                            out.println("ElevationAttribute=elevation");

                        if (configuration.getRuntimeRegex() != null)
                            out.println("RuntimeAttribute=runtime");

                        out
                                .println("Schema=*the_geom:Polygon,location:String"
                                        + (configuration.getTimeRegex() != null ? ",ingestion:java.util.Date"
                                                : "")
                                        + (configuration.getElevationRegex() != null ? ",elevation:Double"
                                                : "")
                                        + (configuration.getRuntimeRegex() != null ? ",runtime:Integer"
                                                : ""));
                        out
                                .println("PropertyCollectors="
                                        + (configuration.getTimeRegex() != null ? "TimestampFileNameExtractorSPI[timeregex](ingestion)"
                                                : "")
                                        + (configuration.getElevationRegex() != null ? (configuration
                                                .getTimeRegex() != null ? "," : "")
                                                + "ElevationFileNameExtractorSPI[elevationregex](elevation)"
                                                : "")
                                        + (configuration.getRuntimeRegex() != null ? (configuration
                                                .getTimeRegex() != null
                                                || configuration.getElevationRegex() != null ? ","
                                                : "")
                                                + "RuntimeFileNameExtractorSPI[runtimeregex](runtime)"
                                                : ""));
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,
                                "Error occurred while writing indexer.properties file!", e);
                    } finally {
                        if (out != null) {
                            out.flush();
                            out.close();
                        }

                        outFile = null;
                        out = null;
                    }
                }

                // ////
                // TIME REGEX
                // ////
                if (!timeregex.exists()) {
                    if (configuration.getTimeRegex() != null) {
                        try {
                            outFile = new FileWriter(timeregex);
                            out = new PrintWriter(outFile);

                            // Write text to file
                            out.println("regex=" + configuration.getTimeRegex());
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE,
                                    "Error occurred while writing timeregex.properties file!", e);
                        } finally {
                            if (out != null) {
                                out.flush();
                                out.close();
                            }

                            outFile = null;
                            out = null;
                        }
                    }
                }

                // ////
                // ELEVATION REGEX
                // ////
                if (!elevationregex.exists()) {
                    if (configuration.getElevationRegex() != null) {
                        try {
                            outFile = new FileWriter(elevationregex);
                            out = new PrintWriter(outFile);

                            // Write text to file
                            out.println("regex=" + configuration.getElevationRegex());
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE,
                                    "Error occurred while writing elevationregex.properties file!",
                                    e);
                        } finally {
                            if (out != null) {
                                out.flush();
                                out.close();
                            }

                            outFile = null;
                            out = null;
                        }
                    }
                }

                // ////
                // RUNTIME REGEX
                // ////
                if (!runtimeregex.exists()) {
                    if (configuration.getRuntimeRegex() != null) {
                        try {
                            outFile = new FileWriter(runtimeregex);
                            out = new PrintWriter(outFile);

                            // Write text to file
                            out.println("regex=" + configuration.getRuntimeRegex());
                        } catch (IOException e) {
                            LOGGER
                                    .log(
                                            Level.SEVERE,
                                            "Error occurred while writing runtimeregex.properties file!",
                                            e);
                        } finally {
                            if (out != null) {
                                out.flush();
                                out.close();
                            }

                            outFile = null;
                            out = null;
                        }
                    }
                }

                // ////
                // CHECKING FOR datastore.properties
                // ////
                if (configuration.getDatastorePropertiesPath() != null) {
                    final File dsFile = Path.findLocation(configuration
                            .getDatastorePropertiesPath(), new File(
                            ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
                    if (dsFile != null && dsFile.exists() && !dsFile.isDirectory()) {
                        LOGGER.info("DataStore file found: " + dsFile.getAbsolutePath());
                        if (!datastore.exists())
                            FileUtils.copyFileToDirectory(dsFile, inputDir);
                    }
                }

                // ////////////////////////////////////////////////////////////
                //
                // IMAGEMOSAIC LAYER CREATION AND CONFIGURATION
                //
                // ////////////////////////////////////////////////////////////
                ImageMosaicGranulesDescriptor mosaicDescriptor = createImageMosaicDescriptor(inputDir);
                /**
                 * CASE 0: check if ImageMosaic layer already exists...
                 */
                boolean exists = checkIfImageMosaicLayerExists(mosaicDescriptor.coverageStoreId);

                if (!exists) {
                    /**
                     * CASE 1: create a new ImageMosaic layer... normal case
                     */
                    if (mosaicDescriptor != null)
                        createNewImageMosaicLayer(inputDir, mosaicDescriptor, layers);
                } else {
                    /**
                     * CASE 2: check if datastore.properties exists...
                     */
                    if (datastore.exists()) {
                        /**
                         * CASE 2.a: update ImageMosaic datastore...
                         */
                        if (Utils.checkFileReadable(datastore)) {
                            // read the properties file
                            Properties properties = Utils.loadPropertiesFromURL(DataUtilities
                                    .fileToURL(datastore));
                            if (properties == null)
                                throw new IOException();

                            // SPI
                            final String SPIClass = properties.getProperty("SPI");
                            try {
                                // create a datastore as instructed
                                final DataStoreFactorySpi spi = (DataStoreFactorySpi) Class
                                        .forName(SPIClass).newInstance();
                                final Map<String, Serializable> params = Utils
                                        .createDataStoreParamsFromPropertiesFile(properties, spi);

                                // special case for postgis
                                if (spi instanceof PostgisNGJNDIDataStoreFactory
                                        || spi instanceof PostgisNGDataStoreFactory) {
                                    DataStore tileIndexStore = spi.createDataStore(params);
                                    final String[] typeNames = tileIndexStore.getTypeNames();
                                    if (typeNames.length <= 0)
                                        throw new IllegalArgumentException(
                                                "Problems when opening the index, no typenames for the schema are defined");

                                    // loading all the features into memory to
                                    // build an in-memory index.
                                    String typeName = mosaicDescriptor.coverageStoreId;

                                    SimpleFeatureSource featureSource = tileIndexStore
                                            .getFeatureSource(typeName);
                                    if (featureSource == null)
                                        throw new NullPointerException(
                                                "The provided SimpleFeatureSource is null, it's impossible to create an index!");

                                    final FeatureType schema = featureSource.getSchema();

                                    FeatureIterator<SimpleFeature> it = null;
                                    SimpleFeatureCollection features = null;
                                    final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
                                    final Lock lock = rwLock.readLock();
                                    try {
                                        lock.lock();

                                        features = featureSource.getFeatures();

                                        if (features == null)
                                            throw new NullPointerException(
                                                    "The provided SimpleFeatureCollection is null, it's impossible to create an index!");

                                        if (LOGGER.isLoggable(Level.FINE))
                                            LOGGER.fine("Index Loaded");

                                        // load the feature from the shapefile
                                        // and create JTS index
                                        it = features.features();
                                        if (!it.hasNext())
                                            throw new IllegalArgumentException(
                                                    "The provided SimpleFeatureCollection  or empty, it's impossible to create an index!");

                                        String[] fileNames = inputDir.list(new SuffixFileFilter(
                                                new String[] { ".tif", ".tiff" },
                                                IOCase.INSENSITIVE));
                                        List<String> fileNameList = Arrays.asList(fileNames);
                                        Collections.sort(fileNameList);

                                        List<String> newGranules = new ArrayList<String>(
                                                fileNameList);
                                        while (it.hasNext()) {
                                            // get the feature
                                            final SimpleFeature sf = it.next();
                                            String location = (String) sf.getAttribute("location");

                                            int index = Collections.binarySearch(fileNameList,
                                                    location);
                                            if (index >= 0) {
                                                newGranules.remove(location);
                                            }
                                        }

                                        FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;
                                        try {
                                            if (newGranules.size() > 0) {
                                                // ////////////////////////////////
                                                // WRITE NEW GRANULES TO THE DB...
                                                // ////////////////////////////////
                                                String geometryPropertyName = schema
                                                        .getGeometryDescriptor().getLocalName();
                                                ReferencedEnvelope bounds = featureSource
                                                        .getBounds();
                                                WKTReader wktReader = new WKTReader();
                                                Geometry the_geom = wktReader.read("POLYGON(("
                                                        + bounds.getMinX() + " " + bounds.getMinY()
                                                        + "," + bounds.getMinX() + " "
                                                        + bounds.getMaxY() + "," + bounds.getMaxX()
                                                        + " " + bounds.getMaxY() + ","
                                                        + bounds.getMaxX() + " " + bounds.getMinY()
                                                        + "," + bounds.getMinX() + " "
                                                        + bounds.getMinY() + "))");
                                                Integer SRID = CRS.lookupEpsgCode(bounds
                                                        .getCoordinateReferenceSystem(), true);
                                                the_geom.setSRID(SRID);

                                                Properties ftProps = Utils
                                                        .loadPropertiesFromURL(DataUtilities
                                                                .fileToURL(indexer));

                                                Pattern timePattern = null, elevPattern = null, runtimePattern = null;
                                                if (timeregex.exists()) {
                                                    Properties timeProps = Utils
                                                            .loadPropertiesFromURL(DataUtilities
                                                                    .fileToURL(timeregex));
                                                    timePattern = Pattern.compile(timeProps
                                                            .getProperty("regex"));
                                                }

                                                if (elevationregex.exists()) {
                                                    Properties elevProps = Utils
                                                            .loadPropertiesFromURL(DataUtilities
                                                                    .fileToURL(elevationregex));
                                                    elevPattern = Pattern.compile(elevProps
                                                            .getProperty("regex"));
                                                }

                                                if (runtimeregex.exists()) {
                                                    Properties runtimeProps = Utils
                                                            .loadPropertiesFromURL(DataUtilities
                                                                    .fileToURL(runtimeregex));
                                                    runtimePattern = Pattern.compile(runtimeProps
                                                            .getProperty("regex"));
                                                }

                                                // create a writer that appends this
                                                // features
                                                fw = tileIndexStore.getFeatureWriterAppend(
                                                        typeName, Transaction.AUTO_COMMIT);

                                                for (String granuleLocation : newGranules) {
                                                    granuleLocation = granuleLocation.replace("\\",
                                                            "\\\\");

                                                    // create a new feature
                                                    final SimpleFeature feature = fw.next();

                                                    // get attributes and copy them
                                                    // over

                                                    feature.setAttribute(geometryPropertyName,
                                                            the_geom);
                                                    feature.setAttribute("location",
                                                            granuleLocation);

                                                    if (ftProps.getProperty("TimeAttribute") != null
                                                            && timePattern != null) {
                                                        final Matcher matcher = timePattern
                                                                .matcher(granuleLocation);
                                                        if (matcher.find()) {
                                                            TimeParser timeParser = new TimeParser();
                                                            List<Date> dates = timeParser
                                                                    .parse(matcher.group());
                                                            if (dates != null && dates.size() > 0) {
                                                                Calendar cal = Calendar
                                                                        .getInstance();
                                                                cal.setTimeZone(TimeZone
                                                                        .getTimeZone("UTC"));
                                                                cal.setTime(dates.get(0));
                                                                cal.setTimeZone(LAMMA_TZ);

                                                                feature
                                                                        .setAttribute(
                                                                                ftProps
                                                                                        .getProperty("TimeAttribute"),
                                                                                cal.getTime());
                                                            }
                                                        }
                                                    }

                                                    if (ftProps.getProperty("ElevationAttribute") != null) {
                                                        final Matcher matcher = elevPattern
                                                                .matcher(granuleLocation);
                                                        if (matcher.find()) {
                                                            feature
                                                                    .setAttribute(
                                                                            ftProps
                                                                                    .getProperty("ElevationAttribute"),
                                                                            Double.valueOf(matcher
                                                                                    .group()));
                                                        }
                                                    }

                                                    if (ftProps.getProperty("RuntimeAttribute") != null) {
                                                        final Matcher matcher = runtimePattern
                                                                .matcher(granuleLocation);
                                                        if (matcher.find()) {
                                                            feature
                                                                    .setAttribute(
                                                                            ftProps
                                                                                    .getProperty("RuntimeAttribute"),
                                                                            Integer.valueOf(matcher
                                                                                    .group()));
                                                        }
                                                    }

                                                    // write down
                                                    fw.write();
                                                }
                                            }
                                        } finally {
                                            fw.close();
                                        }

                                        File layerDescriptor = new File(inputDir,
                                                mosaicDescriptor.coverageStoreId + ".layer");
                                        if (layerDescriptor.exists() && layerDescriptor.isFile())
                                            layers.add(new FileSystemEvent(layerDescriptor,
                                                    FileSystemEventType.FILE_ADDED));
                                    } catch (Throwable e) {
                                        LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
                                        throw e;
                                    } finally {
                                        lock.unlock();
                                        tileIndexStore.dispose();
                                    }
                                }
                            } catch (ClassNotFoundException e) {
                                final IOException ioe = new IOException();
                                throw (IOException) ioe.initCause(e);
                            } catch (InstantiationException e) {
                                final IOException ioe = new IOException();
                                throw (IOException) ioe.initCause(e);
                            } catch (IllegalAccessException e) {
                                final IOException ioe = new IOException();
                                throw (IOException) ioe.initCause(e);
                            }
                        }
                    } else {
                        /**
                         * CASE 2.b: ... do nothing!
                         */
                        File layerDescriptor = new File(inputDir, mosaicDescriptor.coverageStoreId
                                + ".layer");
                        if (layerDescriptor.exists() && layerDescriptor.isFile())
                            layers.add(new FileSystemEvent(layerDescriptor,
                                    FileSystemEventType.FILE_ADDED));
                    }
                }
            }

            // ... setting up the appropriate event for the next action
            events.addAll(layers);

            listenerForwarder.completed();
            return events;
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // no need to
            // log,
            // rethrowing
            JAI.getDefaultInstance().getTileCache().flush();
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        } finally {
            JAI.getDefaultInstance().getTileCache().flush();
        }
    }

    /**
     * 
     * @param xmlFile
     * @return
     */
    private static File parseImageMosaicDirectory(File xmlFile) {
        // TODO: Improve me, leveraging on real XML (DOM)
        String startTag = "<mosaicLocation>";
        String endTag = "</mosaicLocation>";

        String dataDir = null;
        if (xmlFile != null) {
            try {
                final FileImageInputStream fis = new FileImageInputStream(xmlFile);
                String location = null;
                while ((location = fis.readLine()) != null) {
                    if (location.startsWith(startTag)) {
                        if (location.endsWith(endTag)) {
                            dataDir = location.substring(
                                    location.indexOf(startTag) + startTag.length(),
                                    location.length() - (endTag.length())).trim();
                        } else {
                            String next = fis.readLine();
                            if (next != null) {
                                if (next.endsWith(endTag)) {
                                    dataDir = next.substring(0, next.length() - (endTag.length()))
                                            .trim();
                                } else {
                                    String nextLine = fis.readLine();
                                    if (nextLine != null) {
                                        dataDir = next.trim();
                                    } else {
                                        LOGGER.warning("Unable to find missions");
                                        return null;
                                    }
                                }
                            } else {
                                LOGGER.warning("Unable to find missions");
                                return null;
                            }

                        }
                    }
                }
            } catch (FileNotFoundException e) {
                LOGGER.warning("Unable to find the specified file: " + xmlFile);
            } catch (IOException e) {
                LOGGER.warning(new StringBuilder("Problems occurred while reading: ").append(
                        xmlFile).append("due to ").append(e.getLocalizedMessage()).toString());
            }
        }

        return new File(dataDir);
    }

    /**
     * 
     * @param coverageStoreId
     * @return
     * @throws TransformerException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private boolean checkIfImageMosaicLayerExists(String coverageStoreId)
            throws ParserConfigurationException, IOException, TransformerException {
        return GeoServerRESTHelper.checkLayerExistence(getConfiguration().getGeoserverURL(),
                getConfiguration().getGeoserverUID(), getConfiguration().getGeoserverPWD(),
                coverageStoreId);
    }

    /**
     * Create Mosaic Method
     * 
     * @param layers
     * @param inputDir
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    private void createNewImageMosaicLayer(File inputDir,
            ImageMosaicGranulesDescriptor mosaicDescriptor,
            Collection<FileSystemEvent> layers) throws ParserConfigurationException,
            IOException, TransformerException {
        FileWriter outFile;

        // ////////////////////////////////////////////////////////////////////
        //
        // SENDING data to GeoServer via REST protocol.
        //
        // ////////////////////////////////////////////////////////////////////
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("namespace", getConfiguration().getDefaultNamespace());
        queryParams.put("wmspath", getConfiguration().getWmsPath());
        final String[] layerResponse = GeoServerRESTHelper.sendCoverage(inputDir, inputDir,
                getConfiguration().getGeoserverURL(), getConfiguration().getGeoserverUID(),
                getConfiguration().getGeoserverPWD(), mosaicDescriptor.coverageStoreId,
                mosaicDescriptor.coverageStoreId, queryParams, "", "EXTERNAL", "imagemosaic",
                GEOSERVER_VERSION, getConfiguration().getStyles(), getConfiguration()
                        .getDefaultStyle());

        if (layerResponse != null && layerResponse.length > 2) {
            String layer = layerResponse[0];
            LOGGER.info("ImageMosaicConfigurator layer: " + layer);

            final String workspace = layerResponse[1];
            final String coverageStore = mosaicDescriptor.coverageStoreId;
            final String coverageName = layer;
            queryParams.clear();
            queryParams.put("MaxAllowedTiles", Integer.toString(Integer.MAX_VALUE));
            String noData = (mosaicDescriptor.firstCvNameParts.length >= 9 ? mosaicDescriptor.firstCvNameParts[8]
                    : mosaicDescriptor.firstCvNameParts.length >= 8 ? mosaicDescriptor.firstCvNameParts[7]
                            : "-1.0");
            // Actually, the ImageMosaicConfiguration is contained in the
            // flow.xml.
            // therefore, there is no way to set the background values a runtime
            // for the moment, we take the nodata from the file name.
            queryParams.put("BackgroundValues", noData);// NoData
            queryParams.put("OutputTransparentColor", "");
            queryParams.put("InputTransparentColor", "");
            queryParams.put("AllowMultithreading", "false");
            queryParams.put("USE_JAI_IMAGEREAD", "false");
            queryParams.put("SUGGESTED_TILE_SIZE", "512,512");

            configureMosaic(queryParams, getConfiguration().getGeoserverURL(), getConfiguration()
                    .getGeoserverUID(), getConfiguration().getGeoserverPWD(), workspace,
                    coverageStore, coverageName);

            final File layerDescriptor = new File(inputDir, layer + ".layer");

            if (layerDescriptor.createNewFile()) {
                PrintWriter out = null;
                try {
                    outFile = new FileWriter(layerDescriptor);
                    out = new PrintWriter(outFile);

                    // Write text to file
                    out.println("namespace=" + layerResponse[1]);
                    out.println("metocFields=" + mosaicDescriptor.metocFields);
                    out.println("storeid=" + mosaicDescriptor.coverageStoreId);
                    out.println("layerid=" + inputDir.getName());
                    out.println("driver=ImageMosaic");
                    out.println("path=" + File.separator);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,
                            "Error occurred while writing indexer.properties file!", e);
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }

                    outFile = null;
                    out = null;
                }

                layers.add(new FileSystemEvent(layerDescriptor,
                        FileSystemEventType.FILE_ADDED));
            }
        }
    }

    /**
     * Create Mosaic Descriptor Method
     * 
     * @param inputDir
     * @param mosaicDescriptor
     * @return
     */
    private ImageMosaicGranulesDescriptor createImageMosaicDescriptor(File inputDir) {
        ImageMosaicGranulesDescriptor mosaicDescriptor = null;

        String[] fileNames = inputDir.list(new SuffixFileFilter(new String[] { ".tif", ".tiff" },
                IOCase.INSENSITIVE));
        List<String> fileNameList = Arrays.asList(fileNames);
        Collections.sort(fileNameList);
        fileNames = fileNameList.toArray(new String[1]);

        if (fileNames != null && fileNames.length > 0) {
            String[] firstCvNameParts = FilenameUtils.getBaseName(fileNames[0]).split("_");
            String[] lastCvNameParts = FilenameUtils.getBaseName(fileNames[fileNames.length - 1])
                    .split("_");

            if (firstCvNameParts != null && firstCvNameParts.length > 3) {
                // Temp workaround to leverages on a coverageStoreId having the
                // same name of the coverage
                // and the same name of the mosaic folder
                String metocFields = firstCvNameParts.length == 9
                        && firstCvNameParts.length == lastCvNameParts.length ? new StringBuilder()
                        .append(firstCvNameParts[0]).append("_").append(firstCvNameParts[1])
                        .append("_").append(firstCvNameParts[2]).append("_").append(
                                firstCvNameParts[3]).append("_") // Min Z
                        .append(lastCvNameParts[3]).append("_") // Max Z
                        .append(firstCvNameParts[5]).append("_") // Base Time
                        .append(lastCvNameParts[6]).append("_") // Forecast Time
                        .append(firstCvNameParts[7]).append("_") // TAU
                        .append(firstCvNameParts[8]) // NoDATA
                        .toString() : inputDir.getName();
                String coverageStoreId = inputDir.getName();

                LOGGER.info("Coverage Store ID: " + coverageStoreId);

                mosaicDescriptor = new ImageMosaicGranulesDescriptor(coverageStoreId, metocFields,
                        firstCvNameParts, lastCvNameParts);
            }
        }

        return mosaicDescriptor;
    }

    /**
     * Configure Mosaic Method
     * 
     * @param queryParams
     * @param geoserverBaseURL
     * @param geoserverUID
     * @param geoserverPWD
     * @param workspace
     * @param coverageStore
     * @param coverageName
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public static void configureMosaic(final Map<String, String> queryParams,
            final String geoserverBaseURL, final String geoserverUID, final String geoserverPWD,
            final String workspace, final String coverageStore, final String coverageName)
            throws ParserConfigurationException, IOException, TransformerException {
        Map<String, String> configElements = new HashMap<String, String>(2);
        if (queryParams.containsKey("MaxAllowedTiles")) {
            // Configuring wmsPath
            final String maxTiles = queryParams.get("MaxAllowedTiles");
            configElements.put("MaxAllowedTiles", maxTiles);
        } else {
            configElements.put("MaxAllowedTiles", "2147483647");
        }

        if (queryParams.containsKey("BackgroundValues")) {
            // Configuring wmsPath
            final String backgroundValues = queryParams.get("BackgroundValues");
            configElements.put("BackgroundValues", backgroundValues);
        } else {
            configElements.put("BackgroundValues", "");
        }

        if (queryParams.containsKey("OutputTransparentColor")) {
            // Configuring wmsPath
            final String outputTransparentColor = queryParams.get("OutputTransparentColor");
            configElements.put("OutputTransparentColor", outputTransparentColor);
        } else {
            configElements.put("OutputTransparentColor", "");
        }

        if (queryParams.containsKey("InputTransparentColor")) {
            // Configuring wmsPath
            final String inputTransparentColor = queryParams.get("InputTransparentColor");
            configElements.put("InputTransparentColor", inputTransparentColor);
        } else {
            configElements.put("InputTransparentColor", "");
        }

        if (queryParams.containsKey("AllowMultithreading")) {
            // Configuring wmsPath
            final String allowMultithreading = queryParams.get("AllowMultithreading");
            configElements.put("AllowMultithreading", allowMultithreading);
        } else {
            configElements.put("AllowMultithreading", "false");
        }

        if (queryParams.containsKey("USE_JAI_IMAGEREAD")) {
            // Configuring wmsPath
            final String useJaiImageread = queryParams.get("USE_JAI_IMAGEREAD");
            configElements.put("USE_JAI_IMAGEREAD", useJaiImageread);
        } else {
            configElements.put("USE_JAI_IMAGEREAD", "false");
        }

        if (queryParams.containsKey("SUGGESTED_TILE_SIZE")) {
            // Configuring wmsPath
            final String suggestedTileSize = queryParams.get("SUGGESTED_TILE_SIZE");
            configElements.put("SUGGESTED_TILE_SIZE", suggestedTileSize);
        } else {
            configElements.put("SUGGESTED_TILE_SIZE", "512,512");
        }

        if (!configElements.isEmpty()) {
            GeoServerRESTHelper.sendCoverageConfiguration(configElements, geoserverBaseURL,
                    geoserverUID, geoserverPWD, workspace, coverageStore, coverageName);
        }

    }

    /**
     * 
     * @author afabiani
     * 
     */
    final class ImageMosaicGranulesDescriptor {
        private String coverageStoreId = null;

        private String metocFields = null;

        private String[] firstCvNameParts = null;

        private String[] lastCvNameParts = null;

        /**
         * @param coverageStoreId
         * @param metocFields
         * @param firstCvNameParts
         * @param lastCvNameParts
         */
        public ImageMosaicGranulesDescriptor(String coverageStoreId, String metocFields,
                String[] firstCvNameParts, String[] lastCvNameParts) {
            this.coverageStoreId = coverageStoreId;
            this.metocFields = metocFields;
            this.firstCvNameParts = firstCvNameParts;
            this.lastCvNameParts = lastCvNameParts;
        }

        /**
         * @return the coverageStoreId
         */
        public String getCoverageStoreId() {
            return coverageStoreId;
        }

        /**
         * @return the metocFields
         */
        public String getMetocFields() {
            return metocFields;
        }

        /**
         * @return the firstCvNameParts
         */
        public String[] getFirstCvNameParts() {
            return firstCvNameParts;
        }

        /**
         * @return the lastCvNameParts
         */
        public String[] getLastCvNameParts() {
            return lastCvNameParts;
        }

    }
}