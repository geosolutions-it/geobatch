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

package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.geobatch.tools.time.TimeParser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.jai.JAI;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * An action which is able to create and update a layer into the GeoServer
 * 
 * @author (r1)AlFa
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $ ImageMosaicConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06 $
 *          ImageMosaicAction.java $ Revision: 0.2 $ 25/feb/11 09:00:00
 */

public class ImageMosaicAction extends BaseAction<FileSystemEvent> implements
        Action<FileSystemEvent> {

    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ImageMosaicAction.class.toString());

    protected final ImageMosaicConfiguration configuration;

    /**
     * Constructs a producer. The operation name will be the same than the parameter descriptor
     * name.
     * 
     * @throws IOException
     */
    public ImageMosaicAction(ImageMosaicConfiguration configuration) {
        super(configuration);
        this.configuration = configuration;
        // //
        // data flow configuration and dataStore name must not be null.
        // //
        String message = null;
        if (configuration == null) {
            message = "ImageMosaicAction: DataFlowConfig is null.";
        } else if ((configuration.getGeoserverURL() == null)) {
            message = "GeoServerURL is null.";
        } else if ("".equals(configuration.getGeoserverURL())) {
            message = "GeoServerURL is empty.";
        }

        if (message != null) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, message);
            throw new IllegalStateException(message);
        }
    }

    /**
     * 
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("ImageMosaicAction: Starting with processing...");

        listenerForwarder.started();

        try {
            // looking for file
            if (events.size() == 0)
                throw new IllegalArgumentException(
                        "ImageMosaicAction: Wrong number of elements for this action: "
                                + events.size());

            // data flow configuration must not be null.
            String message = null; // message should ever be null!
            if (configuration == null) {
                message = "ImageMosaicAction: DataFlowConfig is null.";
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, message);
                throw new IllegalStateException(message);
            }

            // working dir
            final File workingDir = Path.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
            if ((workingDir == null)) {
                message = "ImageMosaicAction: GeoServer working Dir is null.";
            } else if (!workingDir.exists() || !workingDir.isDirectory()) {
                message = "ImageMosaicAction: GeoServer working Dir does not exist.";
            }
            if (message != null) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, message);
                throw new IllegalStateException(message);
            }

            /*
             * If here: we can execute the action
             */
            Collection<FileSystemEvent> layers = new ArrayList<FileSystemEvent>();

            /**
             * For each event into the queue
             */
            while (events.size() > 0) {
                FileSystemEvent event = events.remove();

                /**
                 * If the input file exists and it is a file: Check if it is: - A Directory - An XML
                 * -> Serialized ImageMosaicCommand
                 * 
                 * Building accordingly the ImageMosaicCommand command.
                 */
                ImageMosaicCommand cmd;

                /**
                 * The returned file: - one for each event - .layer file - will be added to the
                 * output queue
                 */
                File layerDescriptor;

                /**
                 * a descriptor for the mosaic to handle
                 */
                ImageMosaicGranulesDescriptor mosaicDescriptor;

                /**
                 * the file pointing to the directory which the layer will refer to.
                 */
                File baseDir;

                /*
                 * Checking input files.
                 */
                File input = event.getSource();
                if (input == null) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING,
                                "ImageMosaicAction: The input file event points to a null file object.");
                    // no file is found for this event try with the next one
                    continue;
                }
                // if the input exists
                if (input.exists()) {
                    /**
                     * the file event points to an XML file...
                     * 
                     * @see ImageMosaicCommand
                     */
                    if (input.isFile()
                            && FilenameUtils.getExtension(input.getName()).equalsIgnoreCase("xml")) {
                        // try to deserialize
                        cmd = ImageMosaicCommand.deserialize(input.getAbsoluteFile());
                        if (cmd == null) {
                            if (LOGGER.isLoggable(Level.SEVERE))
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: Unable to deserialize the passed file: "
                                                + input.getAbsolutePath());
                            continue;
                        }

                        /**
                         * If here: the command is ready: - get the base dir file which will be used
                         * as ID.
                         */
                        baseDir = cmd.getBaseDir();

                        // Perform tests on the base dir file
                        if (!baseDir.exists() || !baseDir.isDirectory()) {
                            if (LOGGER.isLoggable(Level.SEVERE))
                                LOGGER.log(Level.SEVERE, "ImageMosaicAction: Unexpected file '"
                                        + baseDir.getAbsolutePath() + "'");
                            continue;
                        }

                        mosaicDescriptor = ImageMosaicGranulesDescriptor.buildDescriptor(baseDir);

                        if (mosaicDescriptor == null) {
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: Unable to build the imageMosaic descriptor"
                                                + input.getAbsolutePath());
                            }
                            continue;
                        }

                        /*
                         * Check if ImageMosaic layer already exists... TODO: check if the Store
                         * exists!!!
                         */
                        boolean layerExists = false;
                        try {
                            layerExists = GeoServerRESTHelper.checkLayerExistence(ImageMosaicREST
                                    .decurtSlash(getConfiguration().getGeoserverURL()),
                                    getConfiguration().getGeoserverUID(), getConfiguration()
                                            .getGeoserverPWD(), mosaicDescriptor
                                            .getCoverageStoreId());
                        } catch (ParserConfigurationException pce) {
                            // unrecoverable error
                            throw pce;
                        } catch (IOException ioe) {
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: " + ioe.getLocalizedMessage());
                            }
                            continue;
                        } catch (TransformerException te) {
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: " + te.getLocalizedMessage());
                            }
                            continue;
                        }

                        /*
                         * CHECKING FOR datastore.properties
                         */
                        final File datastore = ImageMosaicProperties.checkDataStore(configuration,
                                baseDir);
                        if (datastore == null) {
                            // error occurred
                            continue;
                        }

                        final File indexer = new File(baseDir, "indexer.properties");
                        ImageMosaicProperties.buildIndexer(indexer, configuration);

                        if (!layerExists) {

                            // layer do not exists
                            ImageMosaicREST.createNewImageMosaicLayer(baseDir, mosaicDescriptor,
                                    configuration, layers);
                            /*
                             * TODO HERE WE HAVE A 'cmd' COMMAND FILE WHICH MAY HAVE GETADDFILE OR
                             * GETDELFILE !=NULL USING THOSE LIST WE MAY: DEL ->LOG WARNING--- ADD
                             * ->INSERT INTO THE DATASTORE AN IMAGE USING THE ABSOLUTE PATH.
                             */

                        } else {
                            // layer exists
                            /**
                             * If datastore Update ImageMosaic datastore...
                             */
                            if (Utils.checkFileReadable(datastore)) {

                                // read the properties file
                                Properties dataStoreProp = null;
                                try {
                                    dataStoreProp = ImageMosaicProperties.getProperty(datastore);
                                } catch (UnsatisfiedLinkError ule) {
                                    throw new IllegalArgumentException(
                                            "Unable to 'ImageMosaicAction::updateDataStore()': "
                                                    + ule.getLocalizedMessage());
                                }

                                /**
                                 * This file is generated by the GeoServer and we need it to get:
                                 * LocationAttribute -> the name of the attribute indicating the
                                 * file location AbsolutePath -> a boolean indicating if file
                                 * locations (paths) are absolutes
                                 * 
                                 * 20101014T030000_pph.properties
                                 * 
                                 * AbsolutePath=false Name=20101014T030000_pph ExpandToRGB=false
                                 * LocationAttribute=location
                                 */
                                final File mosaicPropFile = new File(baseDir,
                                        mosaicDescriptor.getCoverageStoreId() + ".properties");

                                Properties mosaicProp = null;
                                try {
                                    mosaicProp = ImageMosaicProperties.getProperty(mosaicPropFile);
                                } catch (UnsatisfiedLinkError ule) {
                                    throw new IllegalArgumentException(
                                            "Unable to 'ImageMosaicAction::updateDataStore()': "
                                                    + ule.getLocalizedMessage());
                                }
                                // update
                                if (!updateDataStore(mosaicProp, dataStoreProp, mosaicDescriptor,
                                        cmd)) {
                                    continue;
                                }

                            } // datastore.properties
                            else {
                                /*
                                 * File 'datastore.properties' do not exists. Probably we have a
                                 * ShapeFile as datastore for this layer. Error unable to UPDATE the
                                 * shape file.
                                 */

                                if (LOGGER.isLoggable(Level.SEVERE)) {
                                    LOGGER.log(Level.SEVERE,
                                            "ImageMosaicAction: Error unable to UPDATE a shape file.");
                                }
                                continue;
                            } // shapefile

                        } // layer Exists
                    }
                    // the file event points to a directory
                    else if (input.isDirectory()) {
                        /**
                         * If here: - get the base dir file which will be used as ID.
                         */
                        baseDir = input;

                        mosaicDescriptor = ImageMosaicGranulesDescriptor.buildDescriptor(baseDir);

                        if (mosaicDescriptor == null) {
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: Unable to build the imageMosaic descriptor"
                                                + input.getAbsolutePath());
                            }
                            continue;
                        }

                        /*
                         * Check if ImageMosaic layer already exists...
                         */
                        boolean layerExists = false;
                        try {
                            layerExists = GeoServerRESTHelper.checkLayerExistence(ImageMosaicREST
                                    .decurtSlash(getConfiguration().getGeoserverURL()),
                                    getConfiguration().getGeoserverUID(), getConfiguration()
                                            .getGeoserverPWD(), mosaicDescriptor
                                            .getCoverageStoreId());

                        } catch (ParserConfigurationException pce) {
                            // unrecoverable error
                            throw pce;
                        } catch (IOException ioe) {
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: " + ioe.getLocalizedMessage());
                            }
                            continue;
                        } catch (TransformerException te) {
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: " + te.getLocalizedMessage());
                            }
                            continue;
                        }

                        /*
                         * CHECKING FOR datastore.properties
                         */
                        final File datastore = ImageMosaicProperties.checkDataStore(configuration,
                                baseDir);
                        if (datastore == null) {
                            // error occurred
                            continue;
                        }

                        final File indexer = new File(baseDir, "indexer.properties");
                        ImageMosaicProperties.buildIndexer(indexer, configuration);

                        if (!layerExists) {
                            // create a new ImageMosaic layer... normal case
                            ImageMosaicREST.createNewImageMosaicLayer(baseDir, mosaicDescriptor,
                                    configuration, layers);

                        } else {
                            // layer already exists
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: The Layer referring to the directory: "
                                                + input.getAbsolutePath() + " do not exists!");
                            }
                            continue;
                        } // layer Exists

                    } // input is Directory || xml
                    else {
                        // the file event do not point to a directory nor to an xml file
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE,
                                    "ImageMosaicAction: the file event do not point to a directory nor to an xml file: "
                                            + input.getAbsolutePath());
                        }
                        continue;
                    }
                } // input file event exists
                else {
                    // no file is found for this event try with the next one
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE,
                                "ImageMosaicAction: Unable to handle the passed file event: "
                                        + input.getAbsolutePath());
                    }
                    continue;
                }

                // prepare the return
                layerDescriptor = new File(baseDir, mosaicDescriptor.getCoverageStoreId()
                        + ".layer");
                if (layerDescriptor.exists() && layerDescriptor.isFile())
                    layers.add(new FileSystemEvent(layerDescriptor, FileSystemEventType.FILE_ADDED));

            } // while

            // ... setting up the appropriate event for the next action
            events.addAll(layers);

            listenerForwarder.completed();
            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            JAI.getDefaultInstance().getTileCache().flush();
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        } finally {
            JAI.getDefaultInstance().getTileCache().flush();
        }
    }

    /**
     * @param queryParams
     * @return
     */
    protected static String getQueryString(Map<String, String> queryParams) {
        StringBuilder queryString = new StringBuilder();

        if (queryParams != null)
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (queryString.length() > 0)
                    queryString.append("&");
                queryString.append(entry.getKey()).append("=").append(entry.getValue());
            }

        return queryString.toString();
    }

    public ImageMosaicConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "cfg:" + getConfiguration() + "]";
    }

    /**
     * 
     * @param files
     * @param absolute
     * @param key
     *            optional list of string
     * @return the query string if success, null otherwise.
     * @throws NullPointerException
     * @throws CQLException
     */
    private Filter getQuery(List<File> files, boolean absolute, String... key)
            throws NullPointerException, CQLException {

        if (files == null) { // Optional -> || key==null
            throw new NullPointerException("getQuery(): The passed argument file list is null!");
        }

        // check the size
        final int size = files.size();
        if (size == 0) {
            return null;
        }
        /**
         * TODO probably we may want to change the query if the size is too big to list all of the
         * file into it! Carlo 03 Mar 2011
         */
        // case fileLocation IN ('f1','f2',...,'fn')
        if (key[0] == null) {
            throw new NullPointerException(
                    "getQuery(): The passed argument key list contains a null element!");
        }
        StringBuilder query = new StringBuilder(key[0] + " IN (");

        if (absolute) {
            for (int i = 0; i < size; i++) {
                File file = files.get(i);
                if (file.exists()) {
                    query.append((i == 0) ? "'" : ",'");
                    query.append(file.getAbsolutePath().replace("\\", "\\\\"));
                    query.append("'");
                }
            }
            query.append(")");
        } else {
            for (int i = 0; i < size; i++) {
                File file = files.get(i);
                if (file.exists()) {
                    query.append((i == 0) ? "'" : ",'");
                    query.append(file.getAbsolutePath().replace("\\", "\\\\"));
                    query.append("'");
                }
            }
            query.append(")");
        }

        // filter=ff.equals(ff.property(locationKey), ff.literal());
        /**
         * The "in predicate" was added in ECQL. (Have a look in the bnf
         * http://docs.codehaus.org/display/GEOTOOLS/ECQL+Parser+Design#ECQLParserDesign-
         * INPredicate) this is the rule for the falue list: <in value list> ::= <expression> {","
         * <expression>}
         * 
         * Thus, you could write sentences like: Filter filter =
         * ECQL.toFilter("length IN (4100001,4100002, 4100003 )"); or Filter filter =
         * ECQL.toFilter("name IN ('one','two','three')"); other Filter filter =
         * ECQL.toFilter("length IN ( (1+2), 3-4, [5*6] )");
         */
        return ECQL.toFilter(query.toString());
    }

    private void setFeature(File baseDir, File granule, String geometryName, SimpleFeature feature) {
        // get attributes and copy them over
        try {
            GeoTiffReader reader = new GeoTiffReader(granule);
            GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

            ReferencedEnvelope bb = new ReferencedEnvelope(originalEnvelope);

            WKTReader wktReader = new WKTReader();
            Geometry the_geom = wktReader.read("POLYGON((" + bb.getMinX() + " " + bb.getMinY()
                    + "," + bb.getMinX() + " " + bb.getMaxY() + "," + bb.getMaxX() + " "
                    + bb.getMaxY() + "," + bb.getMaxX() + " " + bb.getMinY() + "," + bb.getMinX()
                    + " " + bb.getMinY() + "))");
            Integer SRID = CRS.lookupEpsgCode(bb.getCoordinateReferenceSystem(), true);
            the_geom.setSRID(SRID);

            feature.setAttribute(geometryName, the_geom);
            feature.setAttribute("location", granule);

            final File indexer = new File(baseDir, "indexer.properties");
            final Properties indexerProps = ImageMosaicProperties.getProperty(indexer);

            if (indexerProps.getProperty("TimeAttribute") != null) {
                // TODO move out of the cycle
                final File timeregex = new File(baseDir, "timeregex.properties");
                final Properties timeProps = ImageMosaicProperties.getProperty(timeregex);
                final Pattern timePattern = Pattern.compile(timeProps.getProperty("regex"));
                // TODO move out of the cycle
                if (timePattern != null) {
                    final Matcher matcher = timePattern.matcher(granule.getName());
                    if (matcher.find()) {
                        TimeParser timeParser = new TimeParser();
                        List<Date> dates = timeParser.parse(matcher.group());
                        if (dates != null && dates.size() > 0) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                            cal.setTime(dates.get(0));

                            feature.setAttribute(indexerProps.getProperty("TimeAttribute"),
                                    cal.getTime());
                        }
                    }
                }
            }

            if (indexerProps.getProperty("ElevationAttribute") != null) {
                // TODO move out of the cycle
                final File elevationRegex = new File(baseDir, "elevationregex.properties");
                final Properties elevProps = ImageMosaicProperties.getProperty(elevationRegex);
                final Pattern elevPattern = Pattern.compile(elevProps.getProperty("regex"));
                // TODO move out of the cycle
                final Matcher matcher = elevPattern.matcher(granule.getName());
                if (matcher.find()) {
                    feature.setAttribute(indexerProps.getProperty("ElevationAttribute"),
                            Double.valueOf(matcher.group()));
                }
            }

            if (indexerProps.getProperty("RuntimeAttribute") != null) {
                // TODO move out of the cycle
                final File runtimeRegex = new File(baseDir, "runtimeregex.properties");
                final Properties runtimeProps = ImageMosaicProperties.getProperty(runtimeRegex);
                final Pattern runtimePattern = Pattern.compile(runtimeProps.getProperty("regex"));
                // TODO move out of the cycle
                final Matcher matcher = runtimePattern.matcher(granule.getName());
                if (matcher.find()) {
                    feature.setAttribute(indexerProps.getProperty("RuntimeAttribute"),
                            Integer.valueOf(matcher.group()));
                }
            }
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

    /**
     * Update datastore
     * 
     * @param dataStore
     * @param mosaicDescriptor
     * @param cmd
     * @param baseDir
     * @return
     * @throws IOException
     * @throws Throwable
     */
    private boolean updateDataStore(Properties mosaicProp, Properties dataStoreProp,
            ImageMosaicGranulesDescriptor mosaicDescriptor, ImageMosaicCommand cmd)
            throws IllegalArgumentException, IOException, NullPointerException {
        if (dataStoreProp == null) {
            throw new IllegalArgumentException(
                    "ImageMosaicAction::updateDataStore(): Unable to get datastore properties.");
        }
        if (mosaicProp == null) {
            throw new IllegalArgumentException(
                    "ImageMosaicAction::updateDataStore(): Unable to get mosaic properties.");
        }

        DataStore dataStore = null;

        // TODO MOVE TO the top or better -> get from GeoTools api
        final String ABSOLUTE_PATH_KEY = "AbsolutePath";
        final String LOCATION_KEY = "LocationAttribute";
        final int WAIT = 10; // seconds to wait for nfs propagation

        try {
            try {
                // SPI
                final String SPIClass = dataStoreProp.getProperty("SPI");
                // create a datastore as instructed
                final DataStoreFactorySpi spi = (DataStoreFactorySpi) Class.forName(SPIClass)
                        .newInstance();
                final Map<String, Serializable> params = Utils
                        .createDataStoreParamsFromPropertiesFile(dataStoreProp, spi);

                // special case for postgis
                if (spi instanceof PostgisNGJNDIDataStoreFactory
                        || spi instanceof PostgisNGDataStoreFactory) {
                    dataStore = spi.createDataStore(params);
                    if (dataStore == null) {
                        throw new NullPointerException(
                                "updateDataStore(): the required resource was not found or if insufficent parameters were given.");
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("updateDataStore(): " + e.getLocalizedMessage());
            } catch (InstantiationException e) {
                throw new IOException("updateDataStore(): " + e.getLocalizedMessage());
            } catch (IllegalAccessException e) {
                throw new IOException("updateDataStore(): " + e.getLocalizedMessage());
            }

            // the layer uses absolute path?
            final boolean absolute;
            if (mosaicProp.get(ABSOLUTE_PATH_KEY).equals("true")) {
                absolute = true;
                // no need to copy files
            } else {
                absolute = false;
                // copy files to the baseDir
                cmd.setAddFiles(ImageMosaicCommand.copyTo(cmd.getAddFiles(), cmd.getBaseDir(), WAIT));
                /*
                 * if we have some absolute path into delFile list we have to skip those files since
                 * the layer is relative and acceptable (to deletion) passed path are to be relative
                 */
                List<File> files = null;
                if ((files = cmd.getDelFiles()) != null) {
                    for (File file : files) {
                        if (file.isAbsolute()) {
                            /*
                             * this file can still be acceptable since it can be child of the layer
                             * baseDir
                             */
                            final String path = file.getAbsolutePath();
                            if (!path.contains(cmd.getBaseDir().getAbsolutePath())) {
                                // the path is absolute AND the file is outside the layer baseDir!
                                files.remove(file); // remove it
                                // log as warning
                                if (LOGGER.isLoggable(Level.WARNING)) {
                                    LOGGER.warning("updateDataStore(): Layer specify a relative pattern for files but the "
                                            + "incoming xml command file has an absolute AND outside the layer baseDir file into the "
                                            + "delFile list! This file will NOT be removed from the layer: "
                                            + file.getAbsolutePath());
                                }
                            }
                        }
                    }
                }
            }
            // the attribute key location
            final String locationKey = (String) mosaicProp.get(LOCATION_KEY);

            // final String[] typeNames = dataStore.getTypeNames();
            // if (typeNames.length <= 0)
            // throw new IllegalArgumentException(
            // "ImageMosaicAction: Problems when opening the index, no typenames for the schema are defined");

            Transaction transaction = null;
            FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;
            final String handle = "ImageMosaic:" + Thread.currentThread().getId();
            final String store = mosaicDescriptor.getCoverageStoreId();

            List<File> delList = cmd.getDelFiles();
            Filter delFilter = null;
            // query
            try {
                delFilter = getQuery(delList, absolute, locationKey);
            } catch (NullPointerException npe) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("updateDataStore():" + npe);
                }
            } catch (CQLException cqle) {
                throw new IllegalArgumentException(
                        "updateDataStore(): Unable to build a query. Message: " + cqle);
            }

            if (delFilter != null) {
                transaction = new DefaultTransaction(handle);
                try {
                    fw = dataStore.getFeatureWriter(store, delFilter, transaction);
                    if (fw == null) {
                        throw new NullPointerException(
                                "UpdateDataStore(): The FeatureWriter is null, it's impossible to get a writer on the dataStore: "
                                        + dataStore.toString());
                    }
                    // get the schema if this feature
                    // final FeatureType schema = fw.getFeatureType();

                    // TODO check needed??? final String geometryPropertyName =
                    // schema.getGeometryDescriptor().getLocalName();

                    while (fw.hasNext()) {
                        fw.remove();
                    }
                    transaction.commit();
                } catch (IOException ioe) {
                    if (transaction != null)
                        transaction.rollback();
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.severe("UpdateDataStore(): the DEL file list is not used to query datastore. Probably it is empty");
                    }
                    throw new IOException("UpdateDataStore(): " + ioe.getLocalizedMessage());
                } finally {
                    if (transaction != null) {
                        transaction.close();
                        transaction = null; // once closed you have to renew the reference
                    }
                    if (fw != null) {
                        fw.close();
                    }
                }
            }// if ! query error
            else {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("UpdateDataStore(): the DEL file list is not used to query datastore. Probably it is empty");
                }
            }

            List<File> addList = cmd.getAddFiles();
            Filter addFilter = null;
            // calculate the query
            try {
                addFilter = getQuery(addList, absolute, locationKey);
            } catch (NullPointerException npe) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("updateDataStore():" + npe);
                }
            } catch (CQLException cqle) {
                throw new IllegalArgumentException(
                        "updateDataStore(): Unable to build a query. Message: " + cqle);
            }

            // once closed you have to renew the reference
            transaction = new DefaultTransaction(handle);
            if (addFilter != null) {
                try {
                    fw = dataStore.getFeatureWriterAppend(store, transaction);
                    for (File file : addList) {
                        SimpleFeature feature = fw.next();
                        // TODO setFeature();
                        feature.setAttribute(locationKey,
                                file.getAbsolutePath().replaceAll("\\", "\\\\"));
                    }
                } catch (IOException ioe) {
                    transaction.rollback();
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.severe("updateDataStore(): unable to access to the datastore in append mode. Message: "
                                + ioe.getLocalizedMessage());
                    }
                } finally {
                    if (transaction != null) {
                        transaction.close();
                        transaction = null; // once closed you have to renew the reference
                    }
                    if (fw != null) {
                        fw.close();
                    }
                }
            }// if ! query error
            else {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("updateDataStore(): the ADD file list is not used to query datastore. Probably it is empty");
                }
            }
//        } catch (Throwable e) {
//
//            if (LOGGER.isLoggable(Level.SEVERE)) {
//                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
//            }
//            return false;
            
        } finally {
            if (dataStore != null)
                dataStore.dispose();
        }
        return true;
    }
}
