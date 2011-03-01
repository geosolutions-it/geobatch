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
import java.util.Collections;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLockFactory;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * An action which is able to create and update a layer into the GeoServer
 * 
 * @author (r1)AlFa
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version 
 * $ ImageMosaicConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06
 * $ ImageMosaicAction.java $ Revision: 0.2 $ 25/feb/11 09:00:00
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
        String message=null;
        if (configuration == null) {
            message = "ImageMosaicAction: DataFlowConfig is null.";
        }
        else if ((configuration.getGeoserverURL() == null)) {
            message = "GeoServerURL is null.";
        }
        else if ("".equals(configuration.getGeoserverURL())) {
            message = "GeoServerURL is empty.";
        }
        
        if (message!=null){
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
                throw new IllegalArgumentException("ImageMosaicAction: Wrong number of elements for this action: "
                        + events.size());
            
            // data flow configuration must not be null.
            String message=null; // message should ever be null!
            if (configuration == null) {
                message="ImageMosaicAction: DataFlowConfig is null.";
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE,message);
                throw new IllegalStateException(message);
            }
            
            // working dir
            final File workingDir = 
                Path.findLocation(configuration.getWorkingDirectory(), new File(
                        ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
            if ((workingDir == null)) {
                message="ImageMosaicAction: GeoServer working Dir is null.";
            }
            else if (!workingDir.exists() || !workingDir.isDirectory()){
                message="ImageMosaicAction: GeoServer working Dir does not exist.";
            }
            if (message!=null){
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE,message);
                throw new IllegalStateException(message);
            }

            /*
             * If here:
             *  we can execute the action
             */
            
            Collection<FileSystemEvent> layers = new ArrayList<FileSystemEvent>();

            /**
             * For each event into the queue
             */
            while (events.size() > 0) {
                FileSystemEvent event = events.remove();
                
                
                /**
                 * If the input file exists and it is a file:
                 * Check if it is:
                 *  - A Dyrectory
                 *  - An XML -> Serialized ImageMosaicTransaction
                 * 
                 * Building accordingly the ImageMosaicTransaction command.
                 */
                ImageMosaicTransaction cmd;
                
                /**
                 * The returned file:
                 *  - one for each event
                 *  - .layer file
                 *  - will be added to the output queue
                 */
                File layerDescriptor;
                
                /**
                 * a descriptor for the mosaic to handle
                 */
                ImageMosaicGranulesDescriptor mosaicDescriptor;
                
                /**
                 * the file pointing to the directory
                 * which the layer will refer to.
                 */
                File baseDir;
                
                /*
                 * Checking input files.
                 */
                File input = event.getSource();
                if (input==null){
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING,"ImageMosaicAction: The input file event points to a null file object.");
                    // no file is found for this event try with the next one
                    continue;
                }
                // if the input exists
                if (input.exists()){
                    /**
                     * the file event points to an XML file...
                     * @see ImageMosaicTransaction
                     */
                    if (input.isFile() && FilenameUtils.getExtension(input.getName()).equalsIgnoreCase("xml")) {
                        // try to deserialize
                        cmd = ImageMosaicTransaction.deserialize(input.getAbsoluteFile());
                        if (cmd == null) {
                            if (LOGGER.isLoggable(Level.SEVERE))
                                LOGGER.log(Level.SEVERE, "ImageMosaicAction: Unable to deserialize the passed file: "+
                                        input.getAbsolutePath());
                            continue;
                        }
                        
                        /**
                         * If here:
                         *  the command is ready:
                         *  - get the base dir file which will be used as ID. 
                         */
                        baseDir=cmd.getBaseDir();
                        
                        // Perform tests on the base dir file
                        if (!baseDir.exists() || !baseDir.isDirectory()) {
                            if (LOGGER.isLoggable(Level.SEVERE))
                                LOGGER.log(Level.SEVERE, "ImageMosaicAction: Unexpected file '" + baseDir.getAbsolutePath()+ "'");
                            continue;
                        }
                        
                        mosaicDescriptor =ImageMosaicGranulesDescriptor.buildDescriptor(baseDir);
                        
                        if (mosaicDescriptor == null){
                            if (LOGGER.isLoggable(Level.SEVERE)){
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: Unable to build the imageMosaic descriptor"+
                                            input.getAbsolutePath());
                            }
                            continue;
                        }

                        /*
                         * Check if ImageMosaic layer already exists...
 * TODO: check if the Store exists!!!
                         */
                        boolean layerExists=false;
                        try{
                            layerExists = GeoServerRESTHelper.checkLayerExistence( ImageMosaicREST.decurtSlash(getConfiguration()
                                    .getGeoserverURL()), getConfiguration().getGeoserverUID(), getConfiguration()
                                    .getGeoserverPWD(), mosaicDescriptor.getCoverageStoreId());
                        } catch(ParserConfigurationException pce){
                            // unrecoverable error
                            throw pce;
                        }
                        catch(IOException ioe){
                            if (LOGGER.isLoggable(Level.SEVERE)){
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: "+ioe.getLocalizedMessage());
                            }
                            continue;
                        }
                        catch(TransformerException te){
                            if (LOGGER.isLoggable(Level.SEVERE)){
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: "+te.getLocalizedMessage());
                            }
                            continue;
                        }
                        
                        /*
                         * CHECKING FOR datastore.properties
                         */
                        final File datastore = checkDataStore(baseDir);
                        if (datastore==null){
                            // error occurred
                            continue;
                        }
                        
final File indexer = new File(baseDir, "indexer.properties");
ImageMosaicProperties.buildIndexer(indexer, configuration);

                        if (!layerExists) {
                            
                            // layer do not exists
                            ImageMosaicREST.createNewImageMosaicLayer(baseDir, mosaicDescriptor, configuration, layers);
/*
 * TODO
 * HERE WE HAVE A 'cmd' COMMAND FILE WHICH MAY HAVE GETADDFILE OR GETDELFILE !=NULL
 * USING THOSE LIST WE MAY:
 * DEL ->LOG WARNING---
 * ADD ->INSERT INTO THE DATASTORE AN IMAGE USING THE ABSOLUTE PATH.
 */
                            
                        } else {
                            // layer exists
                            /**
                             * If datastore 
                             * Update ImageMosaic datastore...
                             */
                            if (Utils.checkFileReadable(datastore)){
                                // read the properties file
                                Properties properties = Utils.loadPropertiesFromURL(DataUtilities.fileToURL(datastore));
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
                                            || 
                                            spi instanceof PostgisNGDataStoreFactory)
                                    {
                                        DataStore dataStore = spi.createDataStore(params);
                                        // update
                                        if (!updateDataStore(dataStore, mosaicDescriptor, cmd, baseDir)){
                                            continue;
                                        }
                                        dataStore.dispose();
                                        
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
                            } // datastore.properties
                            else {
                                /*
                                 * File 'datastore.properties' do not exists. 
                                 * Probably we have a ShapeFile as datastore for this
                                 * layer.
                                 * Error unable to UPDATE the shape file.
                                 */

                                if (LOGGER.isLoggable(Level.SEVERE)){
                                    LOGGER.log(Level.SEVERE, "ImageMosaicAction: Error unable to UPDATE a shape file.");
                                }
                                continue;
                            } // shapefile

                        } // layer Exists                        
                    }
                    // the file event points to a directory
                    else if (input.isDirectory()){
                        /**
                         * If here:
                         *  - get the base dir file which will be used as ID.
                         */
                        baseDir=input;

                        mosaicDescriptor = ImageMosaicGranulesDescriptor.buildDescriptor(baseDir);
                        
                        if (mosaicDescriptor == null){
                            if (LOGGER.isLoggable(Level.SEVERE)){
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: Unable to build the imageMosaic descriptor"+
                                            input.getAbsolutePath());
                            }
                            continue;
                        }
                        
                        /*
                         * Check if ImageMosaic layer already exists...
                         */
                        boolean layerExists=false;
                        try{
                            layerExists = GeoServerRESTHelper.checkLayerExistence( ImageMosaicREST.decurtSlash(getConfiguration()
                                    .getGeoserverURL()), getConfiguration().getGeoserverUID(), getConfiguration()
                                    .getGeoserverPWD(), mosaicDescriptor.getCoverageStoreId());
                            
                        } catch(ParserConfigurationException pce){
                            // unrecoverable error
                            throw pce;
                        }
                        catch(IOException ioe){
                            if (LOGGER.isLoggable(Level.SEVERE)){
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: "+ioe.getLocalizedMessage());
                            }
                            continue;
                        }
                        catch(TransformerException te){
                            if (LOGGER.isLoggable(Level.SEVERE)){
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: "+te.getLocalizedMessage());
                            }
                            continue;
                        }

                        /*
                         * CHECKING FOR datastore.properties
                         */
                        final File datastore = checkDataStore(baseDir);
                        if (datastore==null){
                            // error occurred
                            continue;
                        }
                        
final File indexer = new File(baseDir, "indexer.properties");
ImageMosaicProperties.buildIndexer(indexer, configuration);
                        
                        if (!layerExists) {
                            // create a new ImageMosaic layer... normal case
                            ImageMosaicREST.createNewImageMosaicLayer(baseDir, mosaicDescriptor, configuration, layers);

                            
                        } else {
                            // layer already exists
                            if (LOGGER.isLoggable(Level.SEVERE)){
                                LOGGER.log(Level.SEVERE,
                                        "ImageMosaicAction: the file event do not point to a directory nor to an xml file: "+
                                            input.getAbsolutePath());
                            }
                            continue;
                        } // layer Exists

                    } // input is Directory || xml
                    else {
                        // the file event do not point to a directory nor to an xml file 
                        if (LOGGER.isLoggable(Level.SEVERE)){
                            LOGGER.log(Level.SEVERE,
                                    "ImageMosaicAction: the file event do not point to a directory nor to an xml file: "+
                                        input.getAbsolutePath());
                        }
                        continue;
                    }
                } // input file event exists
                else {
                    // no file is found for this event try with the next one
                    if (LOGGER.isLoggable(Level.SEVERE)){
                        LOGGER.log(Level.SEVERE, "ImageMosaicAction: Unable to handle the passed file event: "+input.getAbsolutePath());
                    }
                    continue;
                }
                
                // prepare the return
                layerDescriptor = new File(baseDir, mosaicDescriptor.getCoverageStoreId()+ ".layer");
                if (layerDescriptor.exists() && layerDescriptor.isFile())
                    layers.add(new FileSystemEvent(layerDescriptor,FileSystemEventType.FILE_ADDED));

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
     */
    private Filter getInKeyFilter(String key,List<File> files, boolean absolute){
        try{
//          FilterFactory ff =CommonFactoryFinder.getFilterFactory( null );
          StringBuilder query=new StringBuilder(key+" IN ('");
          
          if (absolute){
              for (File file : files) {
                  if (file.exists()){
                      query.append("'");
                      query.append(file.getAbsolutePath().replace("\\","\\\\"));
                      query.append("'");
                  }
              }
//              filter=ff.equals(ff.property(locationKey), ff.literal());
          }
          else {
              for (File file : files) {
                  if (file.exists()){
                      query.append("'");
                      query.append(file.getAbsolutePath().replace("\\","\\\\"));
                      query.append("'");
                  }
              }
//              filter=ff.equals(ff.property(locationKey), ff.literal());
          }
          /**
           * The "in predicate" was added in ECQL. (Have a look in the bnf
              http://docs.codehaus.org/display/GEOTOOLS/ECQL+Parser+Design#ECQLParserDesign-
              INPredicate)
              this is the rule for the falue list:
              <in value list> ::=   <expression> {"," <expression>}
              
              Thus, you could write sentences like:
              
              Filter filter = ECQL.toFilter("length IN (4100001,4100002, 4100003 )");
              
              or
              
              Filter filter = ECQL.toFilter("name IN ('one','two','three')");
              
              other
              
              Filter filter = ECQL.toFilter("length IN ( (1+2), 3-4, [5*6] )"); 
           */          
          return ECQL.toFilter(query.toString());   
      } catch (CQLException cqle){
//TODO LOG
          // do not apply the filter
          return null;
      }
    }
    
    private void setFeature(File baseDir, File granule, String geometryName, SimpleFeature feature){
        // get attributes and copy them over
        try{
            GeoTiffReader reader = new GeoTiffReader(granule);
            GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
    
            ReferencedEnvelope bb = new ReferencedEnvelope(originalEnvelope);
    
            WKTReader wktReader = new WKTReader();
            Geometry the_geom = wktReader.read("POLYGON(("
                    + bb.getMinX() + " " + bb.getMinY()
                    + "," + bb.getMinX() + " "
                    + bb.getMaxY() + "," + bb.getMaxX()
                    + " " + bb.getMaxY() + ","
                    + bb.getMaxX() + " " + bb.getMinY()
                    + "," + bb.getMinX() + " "
                    + bb.getMinY() + "))");
            Integer SRID = CRS
                    .lookupEpsgCode(
                            bb.getCoordinateReferenceSystem(),
                            true);
            the_geom.setSRID(SRID);
    
            feature.setAttribute(geometryName,the_geom);
            feature.setAttribute("location",
                    granule);
    
            final File indexer = new File(baseDir, "indexer.properties");
            final Properties indexerProps = ImageMosaicProperties.getProperty(indexer);
    
            
    
            if (indexerProps.getProperty("TimeAttribute") != null) {
                final File timeregex = new File(baseDir, "timeregex.properties");
                final Properties timeProps = ImageMosaicProperties.getProperty(timeregex);
                final Pattern timePattern = Pattern.compile(timeProps.getProperty("regex"));

                if (timePattern != null){
                    final Matcher matcher = timePattern.matcher(granule.getName());
                    if (matcher.find()) {
                        TimeParser timeParser = new TimeParser();
                        List<Date> dates = timeParser.parse(matcher.group());
                        if (dates != null && dates.size() > 0) {
                            Calendar cal = Calendar
                                    .getInstance();
                            cal.setTimeZone(TimeZone
                                    .getTimeZone("UTC"));
                            cal.setTime(dates.get(0));
        
                            feature.setAttribute(
                                    indexerProps.getProperty("TimeAttribute"),
                                    cal.getTime());
                        }
                    }
                }
            }
    
            if (indexerProps.getProperty("ElevationAttribute") != null) {
                final File elevationRegex = new File(baseDir, "elevationregex.properties");
                final Properties elevProps = ImageMosaicProperties.getProperty(elevationRegex);
                final Pattern elevPattern = Pattern.compile(elevProps.getProperty("regex"));
                final Matcher matcher = elevPattern.matcher(granule.getName());
                if (matcher.find()) {
                    feature.setAttribute(
                            indexerProps.getProperty("ElevationAttribute"),
                            Double.valueOf(matcher.group()));
                }
            }
    
            if (indexerProps.getProperty("RuntimeAttribute") != null) {
                final File runtimeRegex = new File(baseDir, "runtimeregex.properties");
                final Properties runtimeProps = ImageMosaicProperties.getProperty(runtimeRegex);
                final Pattern runtimePattern = Pattern.compile(runtimeProps.getProperty("regex"));
                final Matcher matcher = runtimePattern.matcher(granule.getName());
                if (matcher.find()) {
                    feature.setAttribute(
                            indexerProps.getProperty("RuntimeAttribute"),
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
    private boolean updateDataStore(DataStore dataStore,
                                    ImageMosaicGranulesDescriptor mosaicDescriptor,
                                    ImageMosaicTransaction cmd, File baseDir)
        throws IllegalArgumentException, IOException, NullPointerException
    {
// TODO MOVE TO the top or better -> get from GeoTools api
        final String ABSOLUTE_PATH_KEY="AbsolutePath";
        final String LOCATION_KEY="LocationAttribute";
        try {
            /**
             * This file is generated by the GeoServer and we need it
             * to get:
             *  - LocationAttribute ->
             *      the name of the attribute indicating the file location
             *  - AbsolutePath      ->
             *      a boolean indicating if file locations (paths) are absolutes
             *  
             * 20101014T030000_pph.properties
             * 
             * AbsolutePath=false
                Name=20101014T030000_pph
                ExpandToRGB=false
                LocationAttribute=location
             */
            final File mosaicPropFile=new File(baseDir,mosaicDescriptor.getCoverageStoreId()+".properties");
            Properties mosaicProp;
            try{
                mosaicProp=ImageMosaicProperties.getProperty(mosaicPropFile);
            }catch (UnsatisfiedLinkError ule){
                throw new IllegalArgumentException("Unable to 'ImageMosaicAction::updateDataStore()': "+ule.getLocalizedMessage());
            }
            // the layer uses absolute path?
            final boolean absolute=(Boolean)mosaicProp.get(ABSOLUTE_PATH_KEY);
            // the attribute key location
            final String locationKey=(String)mosaicProp.get(LOCATION_KEY);
            
    //        final String[] typeNames = dataStore.getTypeNames();
    //        if (typeNames.length <= 0)
    //            throw new IllegalArgumentException(
    //                    "ImageMosaicAction: Problems when opening the index, no typenames for the schema are defined");
    
            Transaction transaction=null;
            FeatureWriter<SimpleFeatureType, SimpleFeature> fw=null;
            final String handle="ImageMosaic:"+Thread.currentThread().getId();
            final String store = mosaicDescriptor.getCoverageStoreId();
            
            List<File> delList=cmd.getDelFiles();
            Filter delFilter=null;
            if (delList!=null) {
                delFilter=getInKeyFilter(locationKey, delList, absolute);
            }
            if (delFilter!=null){
                transaction=new DefaultTransaction();
                try {
                    fw=dataStore.getFeatureWriter(store, delFilter, transaction);
                    if (fw == null)
                        throw new NullPointerException(
                            "ImageMosaicAction: The FeatureWriter is null, it's impossible to get a writer on the dataStore: "+dataStore.toString());
        
                    // get the schema if this feature
                    final FeatureType schema = fw.getFeatureType();
                    final String geometryPropertyName = schema.getGeometryDescriptor().getLocalName();
                    while (fw.hasNext()){
                        fw.remove();
                    }
                    transaction.commit();
                }
                catch (IOException ioe){
                    if (transaction!=null)
                        transaction.rollback();
                }
                finally {
                    if (transaction!=null){
                        transaction.close();
                        transaction=null; //once closed you have to renew the reference
                    }
                    if (fw!=null){
                        fw.close();
                    }
                }
            }
            
            List<File> addList=cmd.getAddFiles();
            //once closed you have to renew the reference
            transaction=new DefaultTransaction(handle);
            Filter addFilter=null;
            if (addList!=null) {
                addFilter=getInKeyFilter(locationKey, addList, absolute);
            }
            if (addFilter!=null){
                try {
                    fw=dataStore.getFeatureWriterAppend(store, transaction);
                    for (File file:addList){
                        SimpleFeature feature=fw.next();
                        feature.setAttribute(locationKey, file.getAbsolutePath().replaceAll("\\", "\\\\"));
                    }
                }
                catch (IOException ioe){
                    transaction.rollback();
                }
                finally {
                    if (transaction!=null){
                        transaction.close();
                        transaction=null; //once closed you have to renew the reference
                    }
                    if (fw!=null){
                        fw.close();
                    }
                }
            }
            
            /*
             * READ existing data:
             *  - get the source of data
             */

            return true;
            
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            return false;
        } finally {
            dataStore.dispose();
        }
    }
    
    /**
     * CHECKING FOR datastore.properties
     * If the 'datastore.properties' do not exists into the baseDir, 
     * try to use the configured one.
     * If not found a shape file will be used (done by the geoserver).
     * 
     * @param baseDir the directory of the layer
     * @return File (unchecked) datastore.properties if succes or
     * null if some error occurred. 
     */
    private File checkDataStore(File baseDir){    
        final File datastore = new File(baseDir, "datastore.properties");
        if (!datastore.exists()){
            if (configuration.getDatastorePropertiesPath() != null) {
                File dsFile;
                try {
                    dsFile = Path.findLocation(
                            configuration.getDatastorePropertiesPath(),
                            new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
                } catch (IOException e) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.warning("ImageMosaicAction:checkDataStore() "+e.getMessage());
                    return null;
                }
                if (dsFile != null){
                    if (!dsFile.isDirectory()) {
                        if (LOGGER.isLoggable(Level.INFO)){
                            LOGGER.info("ImageMosaicAction:checkDataStore() Configuration DataStore file found: '"
                                    + dsFile.getAbsolutePath()+"'.");
                        }
                        try {
                            FileUtils.copyFileToDirectory(dsFile, baseDir);
                        } catch (IOException e) {
                            if (LOGGER.isLoggable(Level.WARNING))
                                LOGGER.warning("ImageMosaicAction:checkDataStore() "+e.getMessage());
                            return null;
                        }
                    }
                    else {
                        if (LOGGER.isLoggable(Level.WARNING)){
                            LOGGER.log(Level.WARNING, 
                                    "ImageMosaicAction:checkDataStore() DataStoreProperties file points to a directory! "
                                    + dsFile.getAbsolutePath()+ "'. Skipping event");
                        }
                        return null;
                    }
                }
                else {
                    if (LOGGER.isLoggable(Level.WARNING)){
                        LOGGER.log(Level.WARNING, "ImageMosaicAction: DataStoreProperties file not found"
                                + configuration.getDatastorePropertiesPath()+ "'. Skipping event");
                    }
                    return null;
                }
            }
            else {
                if (LOGGER.isLoggable(Level.WARNING)){
                    LOGGER.log(Level.WARNING, "ImageMosaicAction: DataStoreProperties file not configured " +
                                "nor found into destination dir. A shape file will be used.");
                }
            }
        }
        return datastore;
    }
}
