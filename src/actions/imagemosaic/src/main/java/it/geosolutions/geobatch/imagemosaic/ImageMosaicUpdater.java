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
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
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

    private static void setFeature(File baseDir, File granule, SimpleFeature feature, String geometryName, String locationKey) {
            // get attributes and copy them over
            try {
                AbstractGridFormat format=null;
                format = (AbstractGridFormat) GridFormatFinder.findFormat(granule);
                if (format == null || ( format instanceof UnknownFormat)) {
                    throw new IllegalArgumentException(
                            "ImageMosaic:setFeature(): Unable to find a reader for the provided file: "+granule.getAbsolutePath());
                }
                // can throw UnsupportedOperationsException
                final AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) format
                        .getReader(granule, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,Boolean.TRUE));
                
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
    //TODO absolute
                feature.setAttribute(locationKey, granule.getName());
                        //granule.getName().replaceAll("\\", "\\\\"));
    
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
                if (ImageMosaicAction.LOGGER.isLoggable(Level.SEVERE))
                    ImageMosaicAction.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
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
        protected static boolean updateDataStore(Properties mosaicProp, Properties dataStoreProp,
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
                                    if (ImageMosaicAction.LOGGER.isLoggable(Level.WARNING)) {
                                        ImageMosaicAction.LOGGER.warning("updateDataStore(): Layer specify a relative pattern for files but the "
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
                    if (ImageMosaicAction.LOGGER.isLoggable(Level.INFO)) {
                        ImageMosaicAction.LOGGER.info("updateDataStore():" + npe);
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
                        if (ImageMosaicAction.LOGGER.isLoggable(Level.SEVERE)) {
                            ImageMosaicAction.LOGGER.severe("UpdateDataStore(): the DEL file list is not used to query datastore. Probably it is empty");
                        }
                        throw new IOException("UpdateDataStore(): " + ioe.getLocalizedMessage());
                    } catch (RuntimeException re) {
                        if (transaction != null)
                            transaction.rollback();
                        if (ImageMosaicAction.LOGGER.isLoggable(Level.SEVERE)) {
                            ImageMosaicAction.LOGGER.severe("UpdateDataStore(): problem with connection: "+re.getLocalizedMessage());
                        }
                        throw new IOException("UpdateDataStore(): " + re.getLocalizedMessage());
                    } finally {
                        if (transaction != null) {
                            transaction.commit();
    //                        transaction.close();
    //                        transaction = null; // once closed you have to renew the reference
                        }
                        if (fw != null) {
                            fw.close();
                            fw=null;
                        }
                    }
                }// if ! query error
                else {
                    if (ImageMosaicAction.LOGGER.isLoggable(Level.INFO)) {
                        ImageMosaicAction.LOGGER.info("UpdateDataStore(): the DEL file list is not used to query datastore. Probably it is empty");
                    }
                }
    
                List<File> addList = cmd.getAddFiles();
                Filter addFilter = null;
                // calculate the query
                try {
                    addFilter = getQuery(addList, absolute, locationKey);
                } catch (NullPointerException npe) {
                    if (ImageMosaicAction.LOGGER.isLoggable(Level.WARNING)) {
                        ImageMosaicAction.LOGGER.warning("updateDataStore():" + npe);
                    }
                } catch (CQLException cqle) {
                    throw new IllegalArgumentException(
                            "updateDataStore(): Unable to build a query. Message: " + cqle);
                }
                // once closed you have to renew the reference
                if (transaction == null)
                    transaction = new DefaultTransaction(handle);
                /*
                 * CHECK IF ADD FILES ARE ALREADY INTO THE LAYER
                 */
                if (addFilter != null) {
                    FeatureReader<SimpleFeatureType, SimpleFeature> fr=null;
                    try {
                        // get the schema if this feature
                        final SimpleFeatureType schema = dataStore.getSchema(store); 
                        /*
                         * TODO to save time we could use the store name which should be
                         * the same
                         */
                        
                        Query q=new Query(schema.getTypeName(),addFilter);
                        fr = dataStore.getFeatureReader(q, transaction);
                        if (fr == null) {
                            throw new NullPointerException(
                                    "UpdateDataStore(): The FeatureReader is null, it's impossible to get a reader on the dataStore: "
                                            + dataStore.toString());
                        }
                        while (fr.hasNext()){
                            SimpleFeature feature=fr.next();
                            if (feature!=null){
                                String path=(String)feature.getAttribute(locationKey);
                                
                                // remove from the list the image which is already into the layer
                                if (absolute){
                                    File added=new File(cmd.getBaseDir(),path);
                                    cmd.getAddFiles().remove(added);
                                    if (ImageMosaicAction.LOGGER.isLoggable(Level.WARNING)) {
                                        ImageMosaicAction.LOGGER.warning("UpdateDataStore(): the file: "+path+
                                                " is removed from the addFiles list because it is already present into the layer");
                                    }
                                }
                                else {
                                    // check relative paths
                                    Iterator<File> it=cmd.getAddFiles().iterator();
                                    while (it.hasNext()){
                                        File file=it.next();
                                        if (file.getName().equals(path)){
                                            it.remove();
                                            if (ImageMosaicAction.LOGGER.isLoggable(Level.WARNING)) {
                                                ImageMosaicAction.LOGGER.warning("UpdateDataStore(): the file: "+path+
                                                " is removed from the addFiles list because it is already present into the layer");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException ioe) {
                        if (transaction != null)
                            transaction.rollback();
                        if (ImageMosaicAction.LOGGER.isLoggable(Level.SEVERE)) {
                            ImageMosaicAction.LOGGER.severe("updateDataStore(): unable to access to the datastore in append mode. Message: "
                                    + ioe.getLocalizedMessage());
                        }
                    } catch (RuntimeException re) {
                        String message="UpdateDataStore(): problem with connection: "+re.getLocalizedMessage();
                        if (transaction != null)
                            transaction.rollback();
                        if (ImageMosaicAction.LOGGER.isLoggable(Level.SEVERE)) {
                            ImageMosaicAction.LOGGER.severe(message);
                        }
                        throw new IOException(message);
                    } finally {
                        if (transaction != null) {
                            transaction.commit();
    //                        transaction.close();
    //                        transaction = null; // once closed you have to renew the reference
                        }
                        if (fr != null) {
                            fr.close();
                            fr=null;
                        }
                    }
                }// if ! query error
                else {
                    if (ImageMosaicAction.LOGGER.isLoggable(Level.INFO)) {
                        ImageMosaicAction.LOGGER.info("updateDataStore(): the ADD file list is not used to query datastore. Probably it is empty");
                    }
                }
                addFilter = null;
                
                /*
                 * copy purged addFiles list of files to the baseDir 
                 * and replace addFiles list with the new copied file list
                 */
                if (!absolute){
                    cmd.setAddFiles(Path.copyListFileToNFS(cmd.getAddFiles(), cmd.getBaseDir(), ImageMosaicAction.WAIT));
                }
                if (cmd.getAddFiles()==null){
                    String message="UpdateDataStore(): problem with copy copyTo files. addFiles list is null here";
                    if (ImageMosaicAction.LOGGER.isLoggable(Level.SEVERE)) {
                        ImageMosaicAction.LOGGER.severe(message);
                    }
                    throw new IOException(message);
                }
                else if (cmd.getAddFiles().size() > 0){
                    // once closed you have to renew the reference
    //                transaction = new DefaultTransaction(handle);
                    /*
                     * ADD FILES TO THE LAYER
                     */
                    try {
                        fw = dataStore.getFeatureWriterAppend(store, transaction);
                        if (fw == null) {
                            throw new NullPointerException(
                                    "UpdateDataStore(): The FeatureWriter is null, it's impossible to get a writer on the dataStore: "
                                            + dataStore.toString());
                        }
                        // get the schema if this feature
                        final FeatureType schema = fw.getFeatureType();
        
                        // TODO check needed??? 
                        final String geometryPropertyName = schema.getGeometryDescriptor().getLocalName();
                        
                        for (File file : addList) {
                            // get the next feature to append
                            SimpleFeature feature = fw.next();
                            if (feature!=null){
                                setFeature(cmd.getBaseDir(), file, feature,geometryPropertyName,locationKey);
                                fw.write();
                            }
                        }
                    } catch (IOException ioe) {
                        transaction.rollback();
                        if (ImageMosaicAction.LOGGER.isLoggable(Level.SEVERE)) {
                            ImageMosaicAction.LOGGER.severe("updateDataStore(): unable to access to the datastore in append mode. Message: "
                                    + ioe.getLocalizedMessage());
                        }
                    } catch (RuntimeException re) {
                        if (transaction != null)
                            transaction.rollback();
                        if (ImageMosaicAction.LOGGER.isLoggable(Level.SEVERE)) {
                            ImageMosaicAction.LOGGER.severe("UpdateDataStore(): problem with connection: "+re.getLocalizedMessage());
                        }
                        throw new IOException("UpdateDataStore(): " + re.getLocalizedMessage());
                    } finally {
                        if (transaction != null) {
                            transaction.commit();
                            transaction.close();
                            transaction = null; // once closed you have to renew the reference
                        }
                        if (fw != null) {
                            fw.close();
                        }
                    }
                } // addFiles size > 0
                
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
