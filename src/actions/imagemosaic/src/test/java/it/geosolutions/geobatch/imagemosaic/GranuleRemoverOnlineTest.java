/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.imagemosaic.granuleutils.GranuleRemover;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GranuleRemoverOnlineTest extends GeoBatchBaseTest {

	private final static Logger LOGGER = Logger.getLogger(GranuleRemoverOnlineTest.class);
	
	private static final String WORKSPACE = "topp";	
	private static final String STORENAME = "granuleTestMosaic";

    //==== FIXTURE STUFF ======================================================

    @Override
    protected String getFixtureId() {
        return "geobatch/mosaic/granuleremover";
    }

    @Override
    protected void connect() throws Exception {
//        new RuntimeException("trace").printStackTrace();
        connectToPostgis();
        connectToGeoserver();
    }

//    @Override
//    protected boolean isOnline() throws Exception {
//        return super.isOnline();
//    }

//    @Override
//    protected void setUpInternal() throws Exception {
//        super.setUpInternal();
//    }
//
//    @Override
//    protected void tearDownInternal() throws Exception {
//        super.tearDownInternal();
//    }

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

    //==== THE REAL TESTS  ======================================================

    @Test
    public void removeGranules() throws Exception {
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
            ImageMosaicAction action = createMosaicAction(createMosaicConfig());
            Queue<EventObject> outputQ = action.execute(inputQ);
        }

        DataStore dataStore = createDatastore();
        assertEquals(5, dataStore.getFeatureSource(STORENAME).getCount(Query.ALL));

        //=== Add another granule
        LOGGER.info(" ***** ADD ONE MORE GRANULE");
        {
            ImageMosaicCommand imc = recreateIMC("20121009");
            // serialize
            imcFile = new File(getTempDir(), "ImageMosaicCommand1.xml");
            LOGGER.info("Creating  " + imcFile);
            ImageMosaicCommand.serialize(imc, imcFile.toString());
        }

        {
            Queue<EventObject> inputQ = new LinkedList<EventObject>();
            inputQ.add(new FileSystemEvent(imcFile, FileSystemEventType.FILE_ADDED));
            ImageMosaicAction action = createMosaicAction(createMosaicConfig());
            Queue<EventObject> outputQ = action.execute(inputQ);
        }

        assertEquals(6, dataStore.getFeatureSource(STORENAME).getCount(Query.ALL));

        //== performs some tests on delete list
        LOGGER.info(" ***** REMOVE OLD GRANULE");
        {
            ImageMosaicCommand imc = recreateIMC("nothing");
            GranuleRemover remover = new GranuleRemover();

            Calendar cal = new GregorianCalendar(2012, 9, 7);
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));

            remover.setBaseDate(cal);
            remover.setDaysAgo(2);
            remover.enrich(imc);
            LOGGER.info("remove " + imc.getDelFiles());
            assertEquals(2, imc.getDelFiles().size());


            imc.setDelFiles(null);
            remover.setDaysAgo(1);
            remover.enrich(imc);
            LOGGER.info("remove " + imc.getDelFiles());
            assertEquals(3, imc.getDelFiles().size());
            assertNotNull(imc.getAddFiles());

            // serialize
            imcFile = new File(getTempDir(), "ImageMosaicCommand2.xml");
            LOGGER.info("Creating  " + imcFile);
            ImageMosaicCommand.serialize(imc, imcFile.toString());
        }

        //== run the action with latest IMC
        {
            Queue<EventObject> inputQ = new LinkedList<EventObject>();
            inputQ.add(new FileSystemEvent(imcFile, FileSystemEventType.FILE_ADDED));
            ImageMosaicConfiguration conf = createMosaicConfig();
//            conf.setFailIgnored(true);
            ImageMosaicAction action = createMosaicAction(conf);
            Queue<EventObject> outputQ = action.execute(inputQ);
        }

        assertEquals(3, dataStore.getFeatureSource(STORENAME).getCount(Query.ALL));

        //== Cleanup
        removeStore();
    }

    /**
     * GranuleRemover used to throw NPE when applied over a still not existent layer.
     */
    @Test
    public void removeFromNewLayer() throws Exception {
        removeStore();

        //=== Add first set of granules
        {
            ImageMosaicCommand imc = recreateIMC("20121004","20121005","20121006","20121007","20121008");
            // serialize

            GranuleRemover remover = new GranuleRemover();

            Calendar cal = new GregorianCalendar(2012, 9, 7);
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));

            remover.setBaseDate(cal);
            remover.setDaysAgo(2);
            remover.enrich(imc);
            LOGGER.info("remove " + imc.getDelFiles());
        }

        //== Cleanup
//        removeStore();
    }

    /**
     * * deletes existing store
     * * create a brand new mosaic dir
     * * create the mosaic on GS
     * 
     */
	protected ImageMosaicConfiguration createMosaicConfig() throws IOException {

        // create datastore file
        Properties datastore = new Properties();
        datastore.putAll(getPostgisParams());
        datastore.remove(PostgisDataStoreFactory.DBTYPE.key);
        datastore.setProperty("SPI", "org.geotools.data.postgis.PostgisNGDataStoreFactory");
        datastore.setProperty(PostgisNGDataStoreFactory.LOOSEBBOX.key, "true");
        datastore.setProperty(PostgisNGDataStoreFactory.ESTIMATED_EXTENTS.key, "false");
        File datastoreFile = new File(getTempDir(), "datastore.properties");
        LOGGER.info("Creating  " + datastoreFile);
        FileOutputStream out = new FileOutputStream(datastoreFile);
        datastore.store(out, "Datastore file created from fixtures");
        out.flush();
        out.close();
//        datastore.store(System.out, "Datastore created from fixtures");
//        datastore.list(System.out);

		// config
		ImageMosaicConfiguration conf = new ImageMosaicConfiguration("", "", "");
		conf.setTimeRegex("(?<=_)\\\\d{8}");
		conf.setTimeDimEnabled("true");
		conf.setTimePresentationMode("LIST");
		conf.setGeoserverURL(getFixture().getProperty("gs_url"));
		conf.setGeoserverUID(getFixture().getProperty("gs_user"));
		conf.setGeoserverPWD(getFixture().getProperty("gs_password"));
        conf.setDatastorePropertiesPath(datastoreFile.getAbsolutePath());
		conf.setDefaultNamespace(WORKSPACE);
		conf.setDefaultStyle("raster");
		conf.setCrs("EPSG:4326");

        return conf;
	}

	protected ImageMosaicAction createMosaicAction(ImageMosaicConfiguration conf) throws IOException {

		ImageMosaicAction action = new ImageMosaicAction(conf);
        action.setTempDir(getTempDir());
		action.setConfigDir(getTempDir()); // is it really used?
        return action;
	}

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

            // previous select did not throw, so the table does exist.
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

}
