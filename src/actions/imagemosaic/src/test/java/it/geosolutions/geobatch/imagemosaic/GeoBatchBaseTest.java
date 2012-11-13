/*
 *  Copyright (C) 2007 - 2012 GeoSolutions S.A.S.
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

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.test.OnlineTestSupport;
import org.junit.Before;
import org.junit.Rule;
import static org.junit.Assume.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ETj (etj at geo-solutions.it)
 */
public abstract class GeoBatchBaseTest extends OnlineTestSupport {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeoBatchBaseTest.class);

    @Rule
    public TestName _testName = new TestName();

    private static File testDataDir = null;
    private File classDir;
    private File tempDir;

    public GeoBatchBaseTest() {
    }

//    @BeforeClass
//    public static void setUpClass() {
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }


    @Before
    public void before() throws Exception {
        LOGGER.debug(" BaseTest:: <start_of_before()>");
        super.before(); // FIXME: shouldnt this be already called?
                        // note this will also call the connect() method
        try{
            connect(); // FIXME: shouldnt this be already called by the geotools classes?
        } catch(Exception e) {
            LOGGER.warn("connect() failed, skipping test " + getTestName() );
            assumeTrue(false);
        }

        LOGGER.info("---------- Running Test " + getClass().getSimpleName() + " :: " + _testName.getMethodName());
        String className = this.getClass().getSimpleName();
        classDir = new File(getTestDataDir(), className);
        if (!classDir.exists()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Using test class dir " + classDir);
            }
            classDir.mkdir();
        }

        String testName = _testName.getMethodName();
        tempDir = new File(classDir, testName);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using test case dir " + tempDir);
        }
        LOGGER.debug(" BaseTest:: </ end_of_before()>");
    }

//    @After
//    public void tearDown() {
//    }

    //=========================================================================
    //=== Utility methods
    //=========================================================================

    /**
     * Get the current test method name.
     */
    public String getTestName() {
        return _testName.getMethodName();
    }
    
    /**
     * Get a temp dir owned by the current test method.
     */
    protected synchronized File getTempDir() {
        if( ! tempDir.exists()) // create dir lazily
            tempDir.mkdir();

        return tempDir;
    }

    /**
     * Load a resource from the resource/ directory (either main/ or test/).
     */
    protected File loadFile(String name) {
        try {
            URL url = this.getClass().getClassLoader().getResource(name);
            if (url == null) {
                throw new IllegalArgumentException("Cant get file '" + name + "'");
            }
            File file = new File(url.toURI());
            return file;
        } catch (URISyntaxException e) {
            LOGGER.error("Can't load file " + name + ": " + e.getMessage(), e);
            return null;
        }
    }

    private synchronized File getTestDataDir() {

        if (testDataDir != null) {
            return testDataDir;
        }

        String startDir = System.getProperty("buildDirectory");
        if (startDir == null) {
            LOGGER.warn("Property 'buildDirectory' is not defined");

            File f = loadFile(".");
            if (f == null || !f.exists()) {
                LOGGER.warn("Undefined current directory");

                throw new IllegalStateException("Could not find a valid current dir");
            }

            String fa = f.getParentFile().getAbsolutePath();

            if (!"target".equals(FilenameUtils.getBaseName(fa))) {
                LOGGER.warn("Can't use current dir " + fa);
                throw new IllegalStateException("Could not find a valid current dir");
            }

            startDir = fa;
        }

        testDataDir = new File(startDir, "test-data-" + System.currentTimeMillis());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using test dir " + testDataDir);
        }

        testDataDir.mkdir();
        return testDataDir;
    }

    //=========================================================================
    //=== Fixtures support
    //=========================================================================

//    /**
//     * Return the directory containing GeoTools test fixture configuration files.
//     * This is ".geobatch" in the user home directory.
//     */
//    public static File getFixtureDirectory() {
//        return new File(System.getProperty("user.home"), ".geobatch");
//    }

    protected Properties getExamplePostgisProps() {
        Properties ret = new Properties();
        ret.setProperty("pg_host", "localhost");
        ret.setProperty("pg_port", "5432");
        ret.setProperty("pg_database", "geobatch");
        ret.setProperty("pg_schema", "public");
        ret.setProperty("pg_user", "geobatch");
        ret.setProperty("pg_password", "geobatch");

        return ret;
    }

    protected Properties getExampleGeoServerProps() {
        Properties ret = new Properties();
        ret.setProperty("gs_url", "http://localhost:8888/geoserver");
        ret.setProperty("gs_user", "admin");
        ret.setProperty("gs_password", "geoserver");

        return ret;
    }

    protected void connectToPostgis() throws Exception {
        LOGGER.debug("postgis host is " + getFixture().getProperty("pg_host"));

        createDatastore();
        LOGGER.debug("postgis connection is ok");
        
//         TODO: check if geotoold provides methods to check for PG existence
//
//        Class.forName("org.postgresql.Driver");
//        Connection connection = DriverManager.getConnection(
//                "jdbc:postgresql://"+getFixture().getProperty("pg_host")
//                    +":"+getFixture().getProperty("pg_port")
//                    +"/"+getFixture().getProperty("pg_database"),
//                getFixture().getProperty("pg_user"),
//                getFixture().getProperty("pg_passwd"));
//        connection.close();
    }

    protected Connection createPGConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://"+getFixture().getProperty("pg_host")
                    +":"+getFixture().getProperty("pg_port")
                    +"/"+getFixture().getProperty("pg_database"),
                getFixture().getProperty("pg_user"),
                getFixture().getProperty("pg_password"));
        
        return connection;
    }

    protected GeoServerRESTReader createGSReader() throws MalformedURLException {
        GeoServerRESTReader reader = new GeoServerRESTReader(
                getFixture().getProperty("gs_url"),
                getFixture().getProperty("gs_user"),
                getFixture().getProperty("gs_password"));
        return reader;
    }

    protected GeoServerRESTPublisher createGSPublisher() throws MalformedURLException {
        GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
                getFixture().getProperty("gs_url"),
                getFixture().getProperty("gs_user"),
                getFixture().getProperty("gs_password"));
        return publisher;
    }

    protected void connectToGeoserver() throws Exception {
        LOGGER.debug("geoserver url is " + getFixture().getProperty("gs_url"));

        try {
            GeoServerRESTReader reader = createGSReader();

            if( ! reader.existGeoserver()) {
                LOGGER.error("GeoServer not found at " + getFixture().getProperty("gs_url"));
                throw new ConnectException("GeoServer not found at " + getFixture().getProperty("gs_url"));
            }
        } catch (MalformedURLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw e;
        }
        LOGGER.debug("geoserver connection is ok");
    }

    public Map getPostgisParams() {
        Map params = new HashMap();

        params.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
        params.put(PostgisDataStoreFactory.HOST.key, getFixture().getProperty("pg_host"));
        params.put(PostgisDataStoreFactory.PORT.key, getFixture().getProperty("pg_port"));
        params.put(PostgisDataStoreFactory.SCHEMA.key, getFixture().getProperty("pg_schema"));
        params.put(PostgisDataStoreFactory.DATABASE.key, getFixture().getProperty("pg_database"));
        params.put(PostgisDataStoreFactory.USER.key, getFixture().getProperty("pg_user"));
        params.put(PostgisDataStoreFactory.PASSWD.key, getFixture().getProperty("pg_password"));

        if (getFixture().containsKey("wkbEnabled")) {
            params.put(PostgisDataStoreFactory.WKBENABLED.key, getFixture().getProperty("wkbEnabled"));
        }
        if (getFixture().containsKey("looseBbox")) {
            params.put(PostgisDataStoreFactory.LOOSEBBOX.key, getFixture().getProperty("looseBbox"));
        }
        return params;
    }

    protected DataStore createDatastore() throws IOException {
        Map params = getPostgisParams();
        DataStore dataStore = new PostgisDataStoreFactory().createDataStore(params);
        return dataStore;
    }

}
