/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.imagemosaic.granuleutils;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.gce.imagemosaic.Utils;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides the locations for mosaic entries.
 *
 * @author ETj
 */
public class GranuleSelector {
	protected final static Logger LOGGER = LoggerFactory.getLogger(GranuleSelector.class);

	/** {@link ShapefileDataStoreFactory} singleton for later usage.*/
    private static final ShapefileDataStoreFactory SHAPEFILE_DATA_STORE_FACTORY = new ShapefileDataStoreFactory();

    private String  locationAttributeName = "location";
    private String  datastoreFileName = "datastore.properties";
    private String  typeName = null;
    private Filter  filter = null;

    public GranuleSelector() {
    }

    /**
     * Customize the locationAttributeName file name.
     * Default is "<code>location</code>".
     */
    public void setLocationAttributeName(String locationAttributeName) {
        this.locationAttributeName = locationAttributeName;
    }

    /**
     * Customize the datastore file name.
     * Default is "<code>datastore.properties</code>".
     */
    public void setDatastoreFileName(String datastoreFileName) {
        this.datastoreFileName = datastoreFileName;
    }

    /**
     * Set the feature type name.
     * 
     * <p/>If not set the base name of the mosaicDir will be used.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Set the granules selection filter.
     * 
     * <p/>This is a <b>mandatory</b> parameter.
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * @return the locations strings as provided by the mosaic index
     */
    public Set<String> getLocations(File mosaicDir) throws IOException, IllegalStateException {

        String localTypeName = typeName;
        if(localTypeName == null) {
            localTypeName = mosaicDir.getName();
            if(LOGGER.isInfoEnabled())
                LOGGER.info("typeName is null, will be automatically set to " + localTypeName);            
        }

        if(filter == null)
            throw new IllegalStateException("Filter is not defined");

        File datastoreProps = new File(mosaicDir, datastoreFileName);
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Looking for datastore property file " + datastoreProps);

        if( ! datastoreProps.exists()) {
            if(LOGGER.isErrorEnabled()) {
                LOGGER.error("Datastore file could not be found. The mosaic may be still not initialized. Skipping.");
                LOGGER.info("Datastore file: " + datastoreProps );
            }
            return null;
        }

        DataStore dataStore = openDataStore(datastoreProps.toURI().toURL());
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Got datastore " + dataStore);

        return selectGranules(dataStore, localTypeName, filter);
    }

    /**
     * @return the locations Files, made absolute using the mosaicDir as starting root.
     */
    public Set<File> getFiles(File mosaicDir) throws IOException, IllegalStateException {
        Set<String> filesToBeRemoved = getLocations(mosaicDir);
        return absolutizeFiles(mosaicDir, filesToBeRemoved);
    }

    protected static Set<File> absolutizeFiles(File mosaicDir, Set<String> locations) {

        Set<File> ret = new HashSet<File>(locations.size());
        for (String location : locations) {
            File file = new File(location);
            if(file.isAbsolute())
                ret.add(file);
            else
                ret.add(new File(mosaicDir, location));
        }
        
        return ret;
    }

    private DataStore openDataStore(final URL propsURL) throws IOException {
        // TODO!!!
        // datastore params should be read from the datastore file in the mosaic dir
        if(propsURL == null)
            throw new NullPointerException("Datastore URL is null");
        
        // load the datastore.properties file
        final Properties properties = Utils.loadPropertiesFromURL(propsURL);
        if(properties!=null){
        	
    		// SPI
    		final String SPIClass = properties.getProperty("SPI");
    		try {
    			// create a datastore as instructed
    			final DataStoreFactorySpi spi = (DataStoreFactorySpi) Class.forName(SPIClass).newInstance();
    			return spi.createDataStore(Utils.createDataStoreParamsFromPropertiesFile(properties, spi));
    		} catch (Exception e) {
    			throw new IOException(e);
    		}
        } else {
        	// try for a shapefile store
        	if(SHAPEFILE_DATA_STORE_FACTORY.canProcess(propsURL)){
        		return SHAPEFILE_DATA_STORE_FACTORY.createDataStore(propsURL);
        	}
        }
        return null;
    }

    /**
     * 
     * @throws IOException
     */
    protected Set<String> selectGranules(DataStore granuleDataStore, String typeName, Filter filter) throws IOException {

        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Filtering " + typeName + " using filter \""+filter +"\"");

    	SimpleFeatureIterator iterator=null;
    	final Set<String> retValue= new HashSet<String>();
    	try {            
			final SimpleFeatureCollection features = granuleDataStore.getFeatureSource(typeName).getFeatures(filter);
			iterator = features.features();
			while(iterator.hasNext()){
				
				// get feature
				SimpleFeature granule = iterator.next();

				// get attribute location
				// TODO make the attribute parametric by inspecting the mosaic properties file
				String location = (String) granule.getAttribute(locationAttributeName);
				retValue.add(location);
			}
		} finally {
			// release resources
			if(iterator!=null){
				iterator.close();
			}
		}
        if(LOGGER.isInfoEnabled())
            LOGGER.info("Found " + retValue.size() + " granules.");

        return retValue;
    }

    //=========================================================================

}