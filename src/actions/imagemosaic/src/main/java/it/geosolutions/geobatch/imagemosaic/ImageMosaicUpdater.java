package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.geobatch.tools.time.TimeParser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.Hints;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Abstract class to provide update functions to the ImageMosaic action
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
abstract class ImageMosaicUpdater {

    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ImageMosaicAction.class.toString());

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
    private static Filter getQuery(List<File> files, boolean absolute, String... key)
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
                    query.append(file.getAbsolutePath().replaceAll("\\", "\\\\"));
                    query.append("'");
                } else if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("ImageMosaicAction::getQuery(): Unable to use the following "
                            + "file to build the query, it does not exists.\nFile"
                            + file.getAbsolutePath());
                }
            }
            query.append(")");
        } else {
            for (int i = 0; i < size; i++) {
                File file = files.get(i);
                if (file.exists()) {
                    query.append((i == 0) ? "'" : ",'");
                    query.append(file.getName());
                    query.append("'");
                } else if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("ImageMosaicAction::getQuery(): Unable to use the following "
                            + "file to build the query, it does not exists.\nFile"
                            + file.getAbsolutePath());
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

    private static boolean setFeature(File baseDir, File granule, SimpleFeature feature,
            String geometryName, String locationKey) {
        // get attributes and copy them over
        try {
            AbstractGridFormat format = null;
            format = (AbstractGridFormat) GridFormatFinder.findFormat(granule);
            if (format == null || (format instanceof UnknownFormat)) {
                throw new IllegalArgumentException(
                        "ImageMosaic:setFeature(): Unable to find a reader for the provided file: "
                                + granule.getAbsolutePath());
            }
            // can throw UnsupportedOperationsException
            final AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) format
                    .getReader(granule, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
                            Boolean.TRUE));

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
            // TODO absolute
            feature.setAttribute(locationKey, granule.getName());
            // granule.getName().replaceAll("\\", "\\\\"));

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

            return true;

        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return false;
        }

    }

    /**
     * Try to update the datastore using the passed command and the mosaicDescriptor as data and
     * 
     * @param mosaicProp
     * @param dataStoreProp
     * @param mosaicDescriptor
     * @param cmd
     * @return boolean representing the operation success (true) or failure (false)
     */
    protected static boolean updateDataStore(Properties mosaicProp, Properties dataStoreProp,
            ImageMosaicGranulesDescriptor mosaicDescriptor, ImageMosaicCommand cmd) {
        if (dataStoreProp == null) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE,
                        "ImageMosaicAction::updateDataStore(): Unable to get datastore properties.");
            }
            return false;
        }
        if (mosaicProp == null) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE,
                        "ImageMosaicAction::updateDataStore(): Unable to get mosaic properties.");
            }
            return false;
        }

        DataStore dataStore = null;

        // TODO MOVE TO the top or better -> get from GeoTools api
        final String ABSOLUTE_PATH_KEY = "AbsolutePath";
        final String LOCATION_KEY = "LocationAttribute";

        try {
            // SPI
            final String SPIClass = dataStoreProp.getProperty("SPI");
            try {
                DataStoreFactorySpi spi = (DataStoreFactorySpi) Class.forName(SPIClass)
                        .newInstance();

                final Map<String, Serializable> params = Utils
                        .createDataStoreParamsFromPropertiesFile(dataStoreProp, spi);

                // datastore creation
                dataStore = spi.createDataStore(params);

            } catch (IOException ioe) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(
                            Level.SEVERE,
                            "ImageMosaicAction::updateDataStore(): "
                                    + "problems setting up (creating or connecting) the datasource. The message is: "
                                    + ioe.getLocalizedMessage(), ioe);
                }
                return false;
            } catch (ClassNotFoundException cnfe) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE,
                            "ImageMosaicAction::updateDataStore(): " + cnfe.getLocalizedMessage(),
                            cnfe);
                }
                return false;
            }

            if (dataStore == null) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(
                            Level.SEVERE,
                            "ImageMosaicAction::updateDataStore(): "
                                    + "the required resource was not found or if insufficent parameters were given.");
                }
                return false;
            }

            // does the layer use absolute path?
            final boolean absolute;
            if (mosaicProp.get(ABSOLUTE_PATH_KEY).equals("true")) {
                absolute = true;
                // no need to copy files
            } else {
                absolute = false;
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
// TODO move into a recoverable path to rollback!
                                // log as warning
                                if (LOGGER.isLoggable(Level.WARNING)) {
                                    LOGGER.warning("ImageMosaicAction::updateDataStore(): Layer specify a relative pattern for files but the "
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

            FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;
            final String handle = "ImageMosaic:" + Thread.currentThread().getId();
            final String store = mosaicDescriptor.getCoverageStoreId();

            List<File> delList = cmd.getDelFiles();
            Filter delFilter = null;
            // query
            try {
                delFilter = getQuery(delList, absolute, locationKey);
            } catch (NullPointerException npe) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(
                            Level.WARNING,
                            "ImageMosaicAction::updateDataStore(): The command contain a null delFile list.\nSKIPPING deletion list."
                                    + npe.getLocalizedMessage());
                }
            } catch (CQLException cqle) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE,
                            "ImageMosaicAction::updateDataStore(): Unable to build a query. Message: "
                                    + cqle.getLocalizedMessage(), cqle);
                }
                return false;
            }

            Transaction transaction = null;
            if (delFilter != null) {

                try {
                    // once closed you have to renew the reference
                    if (transaction == null)
                        transaction = new DefaultTransaction(handle);
                    fw = dataStore.getFeatureWriter(store, delFilter, transaction);
                    if (fw == null) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(
                                    Level.SEVERE,
                                    "ImageMosaicAction::updateDataStore(): The FeatureWriter is null, it's impossible to get a writer on the dataStore: "
                                            + dataStore.toString());
                        }
                        return false;
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
// TODO recover removed file to rollback!
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning("UpdateDataStore(): the DEL file list is not used to query datastore. Probably it is empty");
                    }
                    return false;
                } catch (RuntimeException re) {
                    if (transaction != null)
                        transaction.rollback();
// TODO recover removed file to rollback!
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE,
                                "ImageMosaicAction::updateDataStore(): problem with connection: "
                                        + re.getLocalizedMessage(), re);
                    }
                    return false;
                } finally {
                    if (transaction != null) {
                        transaction.commit();
                        transaction.close();
                        transaction = null; // once closed you have to renew the reference
                    }
                    if (fw != null) {
                        fw.close();
                        fw = null;
                    }
                }
            }// if ! query error
            else {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("ImageMosaicAction::updateDataStore(): the DEL file list is not used to query datastore. Probably it is empty");
                }
            }

            List<File> addList = cmd.getAddFiles();
            Filter addFilter = null;
            // calculate the query
            try {
                addFilter = getQuery(addList, absolute, locationKey);
            } catch (NullPointerException npe) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                            "ImageMosaicAction::updateDataStore():" + npe.getLocalizedMessage(),
                            npe);
                }
            } catch (CQLException cqle) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE,
                            "ImageMosaicAction::updateDataStore(): Unable to build a query. Message: "
                                    + cqle, cqle);
                }
                return false;
            }

            /*
             * CHECK IF ADD FILES ARE ALREADY INTO THE LAYER
             */
            if (addFilter != null) {
                FeatureReader<SimpleFeatureType, SimpleFeature> fr = null;
                try {
                    // once closed you have to renew the reference
                    if (transaction == null)
                        transaction = new DefaultTransaction(handle);

                    // get the schema if this feature
                    final SimpleFeatureType schema = dataStore.getSchema(store);
                    /*
                     * TODO to save time we could use the store name which should be the same
                     */

                    Query q = new Query(schema.getTypeName(), addFilter);
                    fr = dataStore.getFeatureReader(q, transaction);
                    if (fr == null) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(
                                    Level.SEVERE,
                                    "ImageMosaicAction::updateDataStore(): The FeatureReader is null, it's impossible to get a reader on the dataStore: "
                                            + dataStore.toString());
                        }
                        return false;
                    }
                    while (fr.hasNext()) {
                        SimpleFeature feature = fr.next();
                        if (feature != null) {
                            String path = (String) feature.getAttribute(locationKey);

                            // remove from the list the image which is already into the layer
                            if (absolute) {
                                File added = new File(cmd.getBaseDir(), path);
                                cmd.getAddFiles().remove(added);
                                if (LOGGER.isLoggable(Level.WARNING)) {
                                    LOGGER.warning("ImageMosaicAction::updateDataStore(): the file: "
                                            + path
                                            + " is removed from the addFiles list because it is already present into the layer");
                                }
                            } else {
                                // check relative paths
                                Iterator<File> it = cmd.getAddFiles().iterator();
                                while (it.hasNext()) {
                                    File file = it.next();
                                    if (file.getName().equals(path)) {
                                        it.remove();
                                        if (LOGGER.isLoggable(Level.WARNING)) {
                                            LOGGER.warning("ImageMosaicAction::updateDataStore(): the file: "
                                                    + path
                                                    + " is removed from the addFiles list because it is already present into the layer");
                                        }
                                    }
                                }
                            }
                        } else {
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.severe("UpdateDataStore(): problem getting the next feature: it is null!");
                            }
                        }
                    }
                } catch (IOException ioe) {
                    try {
                        if (transaction != null) {
                            transaction.rollback();
                            transaction.close();
                            transaction = null;
                        }
                    } catch (Throwable t) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING,
                                    "ImageMosaicAction::updateDataStore(): problem closing transaction: "
                                            + t.getLocalizedMessage(), t);
                        }
                    }
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE,
                                "ImageMosaicAction::updateDataStore(): unable to access to the datastore in append mode. Message: "
                                        + ioe.getLocalizedMessage(), ioe);
                    }
                    return false;
                } catch (RuntimeException re) {
                    String message = "ImageMosaicAction::updateDataStore(): problem with connection: "
                            + re.getLocalizedMessage();
                    if (transaction != null)
                        transaction.rollback();
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, message, re);
                    }
                    return false;
                } finally {
                    try {
                        if (transaction != null) {
                            transaction.commit();
                            transaction.close();
                            transaction = null; // once closed you have to renew the reference
                        }
                        if (fr != null) {
                            fr.close();
                            fr = null;
                        }
                    } catch (Throwable t) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING,
                                    "ImageMosaicAction::updateDataStore(): problem closing transaction: "
                                            + t.getLocalizedMessage(), t);
                        }
                    }
                }
            }// if ! query error
            else {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("ImageMosaicAction::updateDataStore(): the ADD file list is not used to query datastore. Probably it is empty");
                }
            }
            addFilter = null;

            /*
             * copy purged addFiles list of files to the baseDir and replace addFiles list with the
             * new copied file list
             */
            // store copied file for rollback purpose
            List<File> addedFile=null;
            if (!absolute) {
                addedFile=Path.copyListFileToNFS(cmd.getAddFiles(), cmd.getBaseDir(), false,
                        ImageMosaicAction.WAIT);
            }

            if (cmd.getAddFiles() == null) {
                final String message = "UpdateDataStore(): addFiles list is null Here.";

                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning(message);
                }
                return false;
            } else if (cmd.getAddFiles().size() == 0) {
                final String message = "ImageMosaicAction::updateDataStore(): no more images to add to the layer were found, please check the command.";
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning(message);
                }
                return false;
            } else if (cmd.getAddFiles().size() > 0) {
                /*
                 * ADD FILES TO THE LAYER
                 */
                try {
                    // once closed you have to renew the reference
                    if (transaction == null)
                        transaction = new DefaultTransaction(handle);
                    try {
                        fw = dataStore.getFeatureWriterAppend(store, transaction);
                    } catch (IOException ioe) {
                        try {
                            if (LOGGER.isLoggable(Level.SEVERE))
                                LOGGER.severe("ImageMosaicAction:updateDataStore(): unable to update the new layer, removing copied files...");
                            // if fails rollback the copied files
                            if (addedFile!=null){
                                for (File file : addedFile){
                                    if (LOGGER.isLoggable(Level.WARNING))
                                        LOGGER.warning("ImageMosaicAction: DELETING -> "+file.getAbsolutePath());
                                    // this is done since addedFiles are copied not moved
                                    file.delete();
                                }
                            }
                            if (transaction != null) {
                                transaction.rollback();
                                transaction.close();
                                transaction = null;
                            }
                        } catch (Throwable t) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.log(Level.WARNING,
                                        "ImageMosaicAction::updateDataStore(): problem closing transaction: "
                                                + t.getLocalizedMessage(), t);
                            }
                        }
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.severe("ImageMosaicAction::updateDataStore(): unable to access to the datastore in append mode. Message: "
                                    + ioe.getLocalizedMessage());
                        }
                        return false;
                    }
                    if (fw == null) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(
                                    Level.SEVERE,
                                    "ImageMosaicAction::updateDataStore(): The FeatureWriter is null, it's impossible"
                                            + " to get a writer on the dataStore: "
                                            + dataStore.toString());
                        }
                        return false;
                    }

                    // get the schema if this feature
                    final FeatureType schema = fw.getFeatureType();

                    // TODO check needed???
                    final String geometryPropertyName = schema.getGeometryDescriptor()
                            .getLocalName();

                    for (File file : addList) {
                        // get the next feature to append
                        SimpleFeature feature = fw.next();
                        if (feature != null) {
                            setFeature(cmd.getBaseDir(), file, feature, geometryPropertyName,
                                    locationKey);
                            fw.write();
                        }
                    }

                } catch (RuntimeException re) {
                    try {
                        if (LOGGER.isLoggable(Level.SEVERE))
                            LOGGER.severe("ImageMosaicAction:updateDataStore(): unable to update the new layer, removing copied files...");
                        // if fails rollback the copied files
                        if (addedFile!=null){
                            for (File file : addedFile){
                                if (LOGGER.isLoggable(Level.WARNING))
                                    LOGGER.warning("ImageMosaicAction: DELETING -> "+file.getAbsolutePath());
                                // this is done since addedFiles are copied not moved
                                file.delete();
                            }
                        }
                        if (transaction != null) {
                            transaction.rollback();
                            transaction.close();
                            transaction = null;
                        }
                    } catch (Throwable t) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING,
                                    "ImageMosaicAction::updateDataStore(): problem closing transaction: "
                                            + t.getLocalizedMessage(), t);
                        }
                    }
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.severe("ImageMosaicAction::updateDataStore(): problem with connection: "
                                + re.getLocalizedMessage());
                    }
                    return false;
                } finally {
                    try {
                        if (transaction != null) {
                            transaction.commit();
                            transaction.close();
                            transaction = null; // once closed you have to renew the reference
                        }
                        if (fw != null) {
                            fw.close();
                        }
                    } catch (Throwable t) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING,
                                    "ImageMosaicAction::updateDataStore(): problem closing transaction: "
                                            + t.getLocalizedMessage(), t);
                        }
                    }
                }
            } // addFiles size > 0

        } catch (Throwable e) {

            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }

            return false;

        } finally {
            try {
                if (dataStore != null) {
                    dataStore.dispose();
                }
            } catch (Throwable t) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                            "ImageMosaicAction::updateDataStore(): " + t.getLocalizedMessage(), t);
                }
                /*
                 * return false; TODO: check is this formally correct? if the datastore failed to be
                 * disposed...
                 */
            }
        }
        return true;
    }
}
