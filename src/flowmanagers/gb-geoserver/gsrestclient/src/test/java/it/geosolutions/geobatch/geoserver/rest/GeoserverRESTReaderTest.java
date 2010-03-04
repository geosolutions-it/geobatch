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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import junit.framework.TestCase;
import org.jdom.Element;

/**
 *
 * @author etj
 */
public class GeoserverRESTReaderTest extends TestCase {

	public static final String RESTURL = "http://localhost:8080/geoserver";
	public static final URL URL;
	public static final GeoserverRESTReader grr;

	static {
		URL lurl = null;
		try {
			lurl = new URL("http://localhost:8080/geoserver");
		} catch (MalformedURLException ex) {			
		}

		URL = lurl;
		grr = new GeoserverRESTReader(lurl);
	}

    public GeoserverRESTReaderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

	/**
	 * Test of getLayers method, of class GeoserverRESTReader.
	 */
	public void testGetLayers() {
//		System.out.println("getLayers");
//
//		Element result = grr.getLayers();
//		assertNotNull(result);
//
//		for (Element layer : (List<Element>)result.getChildren("layer")) {
//			System.out.println("Layer: " + layer.getChildText("name"));
//		}
	}

	/**
	 * Test of getDatastores method, of class GeoserverRESTReader.
	 */
	public void testGetDatastores() {
//		System.out.println("getDatastores");
//
//		List<String> wslist = grr.getWorkspaceNames();
//		assertNotNull(wslist);
//		for (String wsname : wslist) {
//			List<String> result = grr.getDatastoresNames(wsname);
//			assertNotNull(result);
//			for (String dsname : result) {
//				System.out.println("datastore -> " + wsname + " : " + dsname);
//			}
//		}
	}

	/**
	 * Test of getDatastore method, of class GeoserverRESTReader.
	 */
	public void testGetDatastore() {
	}

	/**
	 * Test of getLayer method, of class GeoserverRESTReader.
	 */
	public void testGetLayer() {
	}

	/**
	 * Test of getNamespaceNames method, of class GeoserverRESTReader.
	 */
	public void testGetNamespaceNames() {
//		System.out.println("getNamespaceNames");
//		List<String> result = grr.getNamespaceNames();
//		assertNotNull(result);
//		for (String name : result) {
//			System.out.println("namespace: " + name);
//		}
	}


	/**
	 * Test of getWorkspaceNames method, of class GeoserverRESTReader.
	 */
	public void testGetWorkspaceNames() {
//		System.out.println("getWorkspaceNames");
//		List<String> result = grr.getWorkspaceNames();
//		assertNotNull(result);
//		for (String name : result) {
//			System.out.println("workspace: " + name);
//		}
	}

}
