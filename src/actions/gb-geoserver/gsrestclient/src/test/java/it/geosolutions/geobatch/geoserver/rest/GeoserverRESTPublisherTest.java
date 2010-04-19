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
package it.geosolutions.geobatch.geoserver.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.springframework.core.io.ClassPathResource;

/**
 * Testcase for publishing layers on geoserver.
 * We need a running GeoServer to properly run the tests. 
 * Login credentials are hardcoded at the moment (localhost:8080 admin/geoserver).
 * If such geoserver instance cannot be contacted, tests will be skipped.
 *
 *
 * @author etj
 */
public class GeoserverRESTPublisherTest extends TestCase {
    private final static Logger LOGGER = Logger.getLogger(GeoserverRESTPublisherTest.class.getName());

	public static final String RESTURL  = "http://localhost:8080/geoserver";
	public static final String RESTUSER = "admin";
	public static final String RESTPW   = "geoserver";

	public static final GeoServerRESTPublisher publisher
			= new GeoServerRESTPublisher(RESTURL, RESTUSER, RESTPW);

	public static final URL URL;
	public static final GeoServerRESTReader reader;

    public static boolean enabled = true;
    
	static {
		URL lurl = null;
		try {
			lurl = new URL(RESTURL);
		} catch (MalformedURLException ex) {
		}

		URL = lurl;
		reader = new GeoServerRESTReader(lurl, RESTUSER, RESTPW);
	}

    public GeoserverRESTPublisherTest(String testName) {
        super(testName);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        if(enabled()) {
            if( ! reader.existGeoserver()) {
                System.out.println(getClass().getSimpleName() + ": TESTS WILL BE SKIPPED SINCE NO GEOSERVER WAS FOUND AT " + RESTURL);
                enabled = false;
            }
        }

        if(enabled)
            System.out.println("-------------------> RUNNING TEST " + this.getName());
        else
            System.out.println("Skipping test " + this.getClass().getSimpleName() + "::" + this.getName());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected boolean enabled() {
        if(!enabled) {
//            System.out.println("Skipping test in " + getClass().getSimpleName());
        }
        return enabled;
    }

	public void testPublishDeleteExternalGeotiff() throws FileNotFoundException, IOException {
        if(!enabled()) return;
//        Assume.assumeTrue(enabled);

		String wsName = "it.geosolutions";
		String storeName = "testRESTStoreGeotiff";
		String layerName = "resttestdem";

        File geotiff = new ClassPathResource("testdata/resttestdem.tif").getFile();

		// dry run delete to work in a known state
        Element testLayer = reader.getLayer(layerName);
		if( testLayer != null) {
            LOGGER.info("Clearing stale test layer " + layerName);
			boolean ok = publisher.unpublishCoverage(wsName, storeName, layerName);
            if(! ok) {
                fail("Could not unpublish layer " + layerName);
            }
		}

		// known state?
		assertFalse("Cleanup failed", existsLayer(layerName));

		// test insert
		boolean published = publisher.publishExternalGeoTIFF(wsName, storeName, geotiff);
        assertTrue("publish() failed", published);
		assertTrue(existsLayer(layerName));

		//test delete
		boolean ok = publisher.unpublishCoverage(wsName, storeName, layerName);
        assertTrue("Unpublish() failed", ok);
		assertFalse(existsLayer(layerName));

	}

	public void testPublishDeleteShapeZip() throws FileNotFoundException, IOException {
        if(!enabled()) return;
//        Assume.assumeTrue(enabled);

		String ns = "it.geosolutions";
		String storeName = "resttestshp";
		String layerName = "cities";

        File zipFile = new ClassPathResource("testdata/resttestshp.zip").getFile();

		// dry run delete to work in a known state
        Element testLayer = reader.getLayer(layerName);
		if( testLayer != null) {
            LOGGER.info("Clearing stale test layer " + layerName);
			boolean ok = publisher.unpublishFeatureType(ns, storeName, layerName);
            if(! ok) {
                fail("Could not unpublish layer " + layerName);
            }
		}
        // the test ds may exist
		if(publisher.removeDatastore(ns, storeName)) {
            LOGGER.info("Cleared stale datastore " + storeName);
        }

		// known state?
		assertFalse("Cleanup failed", existsLayer(layerName));

		// test insert
		boolean published = publisher.publishShp(ns, storeName, layerName, zipFile);
        assertTrue("publish() failed", published);
		assertTrue(existsLayer(layerName));

		//test delete
		boolean ok = publisher.unpublishFeatureType(ns, storeName, layerName);
        assertTrue("Unpublish() failed", ok);
		assertFalse(existsLayer(layerName));

        // remove also datastore
		boolean dsRemoved = publisher.removeDatastore(ns, storeName);
        assertTrue("removeDatastore() failed", dsRemoved);

	}


	public void testPublishDeleteStyleFile() throws FileNotFoundException, IOException {
        if(!enabled()) return;
//        Assume.assumeTrue(enabled);
        final String styleName = "restteststyle";

        File sldFile = new ClassPathResource("testdata/restteststyle.sld").getFile();

		// dry run delete to work in a known state
		if( reader.existsStyle(styleName)) {
            LOGGER.info("Clearing stale test style " + styleName);
			boolean ok = publisher.removeStyle(styleName);
            if(! ok) {
                fail("Could not unpublish style " + styleName);
            }
		}
        
		// known state?
		assertFalse("Cleanup failed", reader.existsStyle(styleName));

		// test insert
		boolean published = publisher.publishStyle(sldFile); // Will take the name from sld contents
        assertTrue("publish() failed", published);
		assertTrue(reader.existsStyle(styleName));

		//test delete
		boolean ok = publisher.removeStyle(styleName);
        assertTrue("Unpublish() failed", ok);
		assertFalse(reader.existsStyle(styleName));
	}

	public void testPublishDeleteStyleString() throws FileNotFoundException, IOException {
        if(!enabled()) return;
//        Assume.assumeTrue(enabled);
        final String styleName = "restteststyle";

        File sldFile = new ClassPathResource("testdata/restteststyle.sld").getFile();

		// dry run delete to work in a known state
		if( reader.existsStyle(styleName)) {
            LOGGER.info("Clearing stale test style " + styleName);
			boolean ok = publisher.removeStyle(styleName);
            if(! ok) {
                fail("Could not unpublish style " + styleName);
            }
		}

		// known state?
		assertFalse("Cleanup failed", reader.existsStyle(styleName));

		// test insert
        String sldContent = IOUtils.toString(new FileInputStream(sldFile));

		boolean published = publisher.publishStyle(sldContent);  // Will take the name from sld contents
        assertTrue("publish() failed", published);
		assertTrue(reader.existsStyle(styleName));

		//test delete
		boolean ok = publisher.removeStyle(styleName);
        assertTrue("Unpublish() failed", ok);
		assertFalse(reader.existsStyle(styleName));
	}

	public void testDeleteUnexistingCoverage() throws FileNotFoundException, IOException {
        if(!enabled()) return;
//        Assume.assumeTrue(enabled);

		String wsName = "this_ws_does_not_exist";
		String storeName = "this_store_does_not_exist";
		String layerName = "this_layer_does_not_exist";

		boolean ok = publisher.unpublishCoverage(wsName, storeName, layerName);
		assertFalse("unpublished not existing layer", ok);
	}

	public void testDeleteUnexistingFeatureType() throws FileNotFoundException, IOException {
        if(!enabled()) return;
//        Assume.assumeTrue(enabled);

		String wsName = "this_ws_does_not_exist";
		String storeName = "this_store_does_not_exist";
		String layerName = "this_layer_does_not_exist";

		boolean ok = publisher.unpublishFeatureType(wsName, storeName, layerName);
		assertFalse("unpublished not existing layer", ok);
	}

	public void testDeleteUnexistingDatastore() throws FileNotFoundException, IOException {
        if(!enabled()) return;
//        Assume.assumeTrue(enabled);

		String wsName = "this_ws_does_not_exist";
		String storeName = "this_store_does_not_exist";

		boolean ok = publisher.removeDatastore(wsName, storeName);
		assertFalse("removed not existing datastore", ok);
	}

//	public void testDeleteUnexistingFT() throws FileNotFoundException, IOException {
//		String wsName = "this_ws_does_not_exist";
//		String storeName = "this_store_does_not_exist";
//		String layerName = "this_layer_does_not_exist";
//
//		boolean ok = publisher.unpublishFT(wsName, storeName, layerName);
//		assertFalse("unpublished not existing layer", ok);
//	}

	private boolean existsLayer(String layername) {
		return reader.getLayer(layername) != null;
	}

}