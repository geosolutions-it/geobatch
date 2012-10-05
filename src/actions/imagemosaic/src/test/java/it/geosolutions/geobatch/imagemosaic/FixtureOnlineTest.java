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


import java.io.File;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class FixtureOnlineTest extends GeoBatchBaseTest {

	private final static Logger LOGGER = Logger.getLogger(FixtureOnlineTest.class);
	

    @Override
    protected String getFixtureId() {
        return "geobatch/mosaic/fixturetest";
    }

    @Override
    protected void connect() throws Exception {
        connectToPostgis();        
    }

//    @Override
//    protected boolean isOnline() throws Exception {
//        return super.isOnline();
//    }

    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
    }

    @Override
    protected void tearDownInternal() throws Exception {
        super.tearDownInternal();
    }

    @Override
    protected Properties createExampleFixture() {
        Properties ret = new Properties();

        Properties pgprops = getExamplePostgisProps();
        pgprops.setProperty("pg_host", "this_is_intentionally_bad");

        for (Map.Entry entry : pgprops.entrySet()) {
            ret.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        for (Map.Entry entry : getExampleGeoServerProps().entrySet()) {
            ret.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        return ret;
    }



	@Before
	public void setUp() {
        LOGGER.warn("this is the test class @Before setUp()");
	}

	@AfterClass
	public static void dispose() throws Exception {
        LOGGER.warn("this is the test class @AfterClass dispose()");
	}

	@Test
	public void testFixture() throws Exception {
        System.out.println("*** PROCESSING TEST *** ");
        throw new IllegalStateException("connection is failing, so this test should not be running");
	}


}
