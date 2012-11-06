/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
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
package it.geosolutions.geobatch.imagemosaic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
import it.geosolutions.geoserver.rest.decoder.RESTDimensionInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.junit.Test;

import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;

/**
 * @author DamianoG
 *
 */
public class MetadataPresentationOnlineTest extends GeoBatchBaseTest {

    private final static Logger LOGGER = Logger.getLogger(MetadataPresentationOnlineTest.class);
    
    private static final String WORKSPACE = "topp"; 
    private static final String STORENAME = "granuleTestMosaic";
    
    /* (non-Javadoc)
     * @see org.geotools.test.OnlineTestSupport#getFixtureId()
     */
    @Override
    protected String getFixtureId() {
        return "geobatch/mosaic/metadataPresentation";
    }
    
    @Override
    protected void connect() throws Exception {
        connectToPostgis();
        connectToGeoserver();
    }
    
    @Override
    protected Properties createExampleFixture() {
        Properties ret = new Properties();
        for (Map.Entry entry : getExamplePostgisProps().entrySet()) {
            ret.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        for (Map.Entry entry : getExampleGeoServerProps().entrySet()) {
            ret.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        return ret;
    }
    
    @Test
    public void testConfiguration1() throws Exception{
        
        TestConfiguration conf = new TestConfiguration();
        
        conf.setTimeEnabled(true);
        conf.setTimePresentation("DISCRETE_INTERVAL");
        conf.setTimeResolution("3");
        
        conf.setElevationEnabled(true);
        conf.setElevationPresentation("LIST");
        
        RESTDimensionInfo time = null;
        RESTDimensionInfo elevation = null;
        
        RESTCoverage coverageInfo = createAndRunAction(conf);
        
        assertEquals(coverageInfo.getDimensionInfo().size(),2);
        
        for(RESTDimensionInfo el : coverageInfo.getDimensionInfo()){
            if(el.getKey().equals(RESTDimensionInfo.TIME)){
                time = el;
            }else if (el.getKey().equals(RESTDimensionInfo.ELEVATION)){
                elevation = el;
            }
        }
        
        assertEquals(time.isEnabled(),conf.isTimeEnabled());
        assertEquals(time.getPresentation(),conf.getTimePresentation());
        assertEquals(time.getKey(),RESTDimensionInfo.TIME);
        assertEquals(time.getResolution(),conf.getTimeResolution());
        
        assertEquals(elevation.isEnabled(),conf.isElevationEnabled());
        assertEquals(elevation.getPresentation(),conf.getElevationPresentation());
        assertEquals(elevation.getKey(),RESTDimensionInfo.ELEVATION);
        assertEquals(elevation.getResolution(),null);
        
    }
    
    @Test
    public void testConfiguration2() throws Exception{
        
        TestConfiguration conf = new TestConfiguration();
        
        conf.setTimeEnabled(true);
        conf.setTimePresentation("CONTINUOUS_INTERVAL");
        
        conf.setElevationEnabled(true);
        conf.setElevationPresentation("DISCRETE_INTERVAL");
        conf.setElevationResolution("3");
        
        RESTDimensionInfo time = null;
        RESTDimensionInfo elevation = null;
        
        RESTCoverage coverageInfo = createAndRunAction(conf);
        
        assertEquals(coverageInfo.getDimensionInfo().size(),2);
        
        for(RESTDimensionInfo el : coverageInfo.getDimensionInfo()){
            if(el.getKey().equals(RESTDimensionInfo.TIME)){
                time = el;
            }else if (el.getKey().equals(RESTDimensionInfo.ELEVATION)){
                elevation = el;
            }
        }
        
        assertEquals(time.isEnabled(),conf.isTimeEnabled());
        assertEquals(time.getPresentation(),conf.getTimePresentation());
        assertEquals(time.getKey(),RESTDimensionInfo.TIME);
        assertEquals(time.getResolution(),null);
        
        assertEquals(elevation.isEnabled(),conf.isElevationEnabled());
        assertEquals(elevation.getPresentation(),conf.getElevationPresentation());
        assertEquals(elevation.getKey(),RESTDimensionInfo.ELEVATION);
        assertEquals(elevation.getResolution(),conf.getElevationResolution());
    }
    
    @Test
    public void testConfiguration3() throws Exception{
        
        TestConfiguration conf = new TestConfiguration();
        
        conf.setTimeEnabled(true);
        conf.setTimePresentation("DISCRETE_INTERVAL");
        conf.setTimeResolution("-1");
        
        conf.setElevationEnabled(false);
        
        
        RESTDimensionInfo time = null;
        RESTDimensionInfo elevation = null;
        
        RESTCoverage coverageInfo = createAndRunAction(conf);
        
        assertEquals(coverageInfo.getDimensionInfo().size(),2);
        
        for(RESTDimensionInfo el : coverageInfo.getDimensionInfo()){
            if(el.getKey().equals(RESTDimensionInfo.TIME)){
                time = el;
            }else if (el.getKey().equals(RESTDimensionInfo.ELEVATION)){
                elevation = el;
            }
        }
        
        assertEquals(time.isEnabled(),conf.isTimeEnabled());
        assertEquals(time.getPresentation(),conf.getTimePresentation());
        assertEquals(time.getKey(),RESTDimensionInfo.TIME);
        assertEquals(time.getResolution(),"1");
        
        assertEquals(elevation.isEnabled(),false);
        assertEquals(elevation.getPresentation(),null);
        assertEquals(elevation.getKey(),RESTDimensionInfo.ELEVATION);
        assertEquals(elevation.getResolution(),null);
    }
    
    protected RESTCoverage createAndRunAction(TestConfiguration testConfig) throws Exception {
        removeStore();
        
        File imcFile;

        //=== Add first set of granules
        LOGGER.info(" ***** CREATING FIRST BATCH OF GRANULES");
        {
            ImageMosaicCommand imc = recreateIMC("20121004","20121005","20121006","20121007","20121008");
            // serialize
            imcFile = new File(getTempDir(), "ImageMosaicCommand0.xml");
            LOGGER.info("Creating  " + imcFile);
            ImageMosaicCommand.serialize(imc, imcFile.toString());
        }

        {
            Queue<EventObject> inputQ = new LinkedList<EventObject>();            
            inputQ.add(new FileSystemEvent(imcFile, FileSystemEventType.FILE_ADDED));
            ImageMosaicAction action = createMosaicAction(testConfig);
            Queue<EventObject> outputQ = action.execute(inputQ);
        }
        
        DataStore dataStore = createDatastore();
        assertEquals(5, dataStore.getFeatureSource(STORENAME).getCount(Query.ALL));
        
        GeoServerRESTReader reader = new GeoServerRESTReader(getFixture().getProperty("gs_url"), getFixture().getProperty("gs_user"), getFixture().getProperty("gs_password"));
        
        RESTCoverage coverageInfo = reader.getCoverage(WORKSPACE, STORENAME, STORENAME);
        
        removeStore();
        
        return coverageInfo;
    }
    
    /**
     * Replicated from GranuleRemoverOnlineTest... put it in a Utility class?
     * 
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    protected void removeStore() throws MalformedURLException, ClassNotFoundException, SQLException {
        // remove existing store from GeoServer
        GeoServerRESTReader gsReader = createGSReader();
        GeoServerRESTPublisher publisher = createGSPublisher();

        if( null != gsReader.getCoverageStore(WORKSPACE, STORENAME)) {
            LOGGER.info("Removing existing store");
            publisher.removeCoverageStore(WORKSPACE, STORENAME, true);
        }

        // remove table from PG
        Connection connection = createPGConnection();
        try {
            LOGGER.info("Checking if PG table '"+STORENAME+"' exists");
            Statement st = connection.createStatement();
            st.execute("SELECT count(*) FROM "+getFixture().getProperty("pg_schema")+".\""+STORENAME+"\"");
            st.close();

            // previous select did not issue an exception, so the table does exist.
            try {
                LOGGER.info("Removing PG table '"+STORENAME+"'");

                st = connection.createStatement();
                st.executeUpdate("DROP TABLE " + getFixture().getProperty("pg_schema") + ".\"" + STORENAME + "\"");
                st.close();
            } catch (SQLException ex) {
                LOGGER.warn("Error while dropping table");
            }
        } catch(Exception e) {
            LOGGER.info("The store " + STORENAME + " probably does not exist: " + e.getMessage());
        } finally {
            connection.close();
        }
    }
    
    protected ImageMosaicCommand recreateIMC(String... names) throws Exception {

        // copy brand new mosaic dir
        File initialData = loadFile("time_mosaic");
        assertNotNull(initialData);
        assertTrue(initialData.isDirectory());
        
        File mosaicDir = new File(getTempDir(), STORENAME);
        LOGGER.info("Copying tiff files into " + mosaicDir);
        
                List<File> addList = new ArrayList<File>();
        for(File srcTiff : FileUtils.listFiles(initialData, new String[]{"tiff"}, false)) {
            for (String name : names) {
                if(srcTiff.getName().contains(name)) {
                    LOGGER.info("Adding " + srcTiff + " to ImageMosaicCommand");
                    addList.add(srcTiff);
                    break;
                }
            }
        }
        
        ImageMosaicCommand imc = new ImageMosaicCommand();
        imc.setBaseDir(mosaicDir);
        imc.setAddFiles(addList);
        return imc;
    }
    
    protected File createDatastorePropFromFixtures() throws IOException{
        // create datastore file
        Properties datastore = new Properties();
        datastore.setProperty("SPI","org.geotools.data.postgis.PostgisNGDataStoreFactory");
        datastore.setProperty("port", getFixture().getProperty("pg_port"));
        datastore.setProperty("host", getFixture().getProperty("pg_host"));
        datastore.setProperty("schema", getFixture().getProperty("pg_schema"));
        datastore.setProperty("database", getFixture().getProperty("pg_database"));
        datastore.setProperty("user", getFixture().getProperty("pg_user"));
        datastore.setProperty("passwd", getFixture().getProperty("pg_password"));
        datastore.setProperty("Loose bbox","true");
        datastore.setProperty("Estimated extends","false");
        File datastoreFile = new File(getTempDir(), "datastore.properties");
        LOGGER.info("Creating  " + datastoreFile);
        FileOutputStream out = new FileOutputStream(datastoreFile);
        datastore.store(out, "Datastore file created from fixtures");
        out.flush();
        out.close();
        return datastoreFile;
    }
    
    protected ImageMosaicAction createMosaicAction(TestConfiguration testConfig) throws IOException {

        File datastoreFile = createDatastorePropFromFixtures();

        // config
        ImageMosaicConfiguration conf = new ImageMosaicConfiguration("", "", "");
        
        conf.setTimeRegex("(?<=_)\\\\d{8}");
        conf.setTimeDimEnabled(String.valueOf(testConfig.isTimeEnabled()));
        conf.setTimePresentationMode(testConfig.getTimePresentation());
        
        conf.setElevationRegex("(?<=_)\\\\d{8}");
        conf.setElevDimEnabled(String.valueOf(testConfig.isElevationEnabled()));
        conf.setElevationPresentationMode(testConfig.getElevationPresentation());
        
        BigDecimalConverter conv = new BigDecimalConverter();
        try{
        conf.setTimeDiscreteInterval((BigDecimal)conv.fromString(testConfig.getTimeResolution()));
        }
        catch(Exception e){}
        try{
        conf.setElevationDiscreteInterval((BigDecimal)conv.fromString(testConfig.getElevationResolution()));
        }
        catch(Exception e){}
    
        conf.setGeoserverURL(getFixture().getProperty("gs_url"));
        conf.setGeoserverUID(getFixture().getProperty("gs_user"));
        conf.setGeoserverPWD(getFixture().getProperty("gs_password"));
        conf.setDatastorePropertiesPath(((File) datastoreFile).getAbsolutePath());
        conf.setDefaultNamespace(WORKSPACE);
        conf.setDefaultStyle("raster");
        conf.setCrs("EPSG:4326");

        ImageMosaicAction action = new ImageMosaicAction(conf);
        action.setTempDir(getTempDir());
        action.setConfigDir(getTempDir());
        return action;
    }
    
    private class TestConfiguration{
        
        private boolean timeEnabled = true;
        private String timePresentation = "";
        private String timeKey = RESTDimensionInfo.TIME;
        private boolean elevationEnabled = true;
        private String elevationPresentation = "";
        private String elevationKey = RESTDimensionInfo.ELEVATION;
        private String timeResolution = "";
        private String elevationResolution = "";
        
        public boolean isTimeEnabled() {
            return timeEnabled;
        }
        public void setTimeEnabled(boolean timeEnabled) {
            this.timeEnabled = timeEnabled;
        }
        public String getTimePresentation() {
            return timePresentation;
        }
        public void setTimePresentation(String timePresentation) {
            this.timePresentation = timePresentation;
        }
        public String getTimeKey() {
            return timeKey;
        }
        public void setTimeKey(String timeKey) {
            this.timeKey = timeKey;
        }
        public boolean isElevationEnabled() {
            return elevationEnabled;
        }
        public void setElevationEnabled(boolean elevationEnabled) {
            this.elevationEnabled = elevationEnabled;
        }
        public String getElevationPresentation() {
            return elevationPresentation;
        }
        public void setElevationPresentation(String elevationPresentation) {
            this.elevationPresentation = elevationPresentation;
        }
        public String getElevationKey() {
            return elevationKey;
        }
        public void setElevationKey(String elevationKey) {
            this.elevationKey = elevationKey;
        }
        public String getTimeResolution() {
            return timeResolution;
        }
        public void setTimeResolution(String timeResolution) {
            this.timeResolution = timeResolution;
        }
        public String getElevationResolution() {
            return elevationResolution;
        }
        public void setElevationResolution(String elevationResolution) {
            this.elevationResolution = elevationResolution;
        }
        
        
        
        
    }

}
