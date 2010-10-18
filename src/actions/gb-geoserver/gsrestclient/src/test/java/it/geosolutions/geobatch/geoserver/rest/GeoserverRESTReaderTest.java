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
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import org.jdom.Element;

/**
 * 
 * @author etj
 */
public class GeoserverRESTReaderTest extends TestCase {

	public static final String RESTURL = "http://localhost:8080/geoserver";
	public static final String USERNAME = "admin";
	public static final String PASSWORD = "geoserver";
	public static final URL URL;
	public static final GeoServerRESTReader reader;

	public static boolean enabled = true;

	static {
		URL lurl = null;
		try {
			lurl = new URL("http://localhost:8080/geoserver");
		} catch (MalformedURLException ex) {
		}

		URL = lurl;
		reader = new GeoServerRESTReader(lurl, USERNAME, PASSWORD);
	}

	public GeoserverRESTReaderTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (enabled()) {
			if (!reader.existGeoserver()) {
				System.out
						.println(getClass().getSimpleName()
								+ ": TESTS WILL BE SKIPPED SINCE NO GEOSERVER WAS FOUND AT "
								+ RESTURL);
				enabled = false;
			}
		}

		if (enabled)
			System.out.println("-------------------> RUNNING TEST "
					+ this.getName());
		else
			System.out.println("Skipping test "
					+ this.getClass().getSimpleName() + "::" + this.getName());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected boolean enabled() {
		if (!enabled) {
			// System.out.println("Skipping test in " +
			// getClass().getSimpleName());
		}
		return enabled;
	}

	/**
	 * Test of getLayers method, of class GeoServerRESTReader.
	 */
	public void testGetLayers() {
		if (!enabled())
			return;

		Element result = reader.getLayers();
		assertNotNull(result);
		assertEquals(/* CHANGEME */19, result.getChildren("layer").size()); // value
																			// in
																			// default
																			// gs
																			// installation

		System.out.println("Layers:" + result.getChildren("layer").size());
		System.out.print("Layers:");
		for (Element layer : (List<Element>) result.getChildren("layer")) {
			System.out.print(layer.getChildText("name") + " ");
		}
		System.out.println();
	}

	/**
	 * Test of getDatastores method, of class GeoServerRESTReader.
	 */
	public void testGetDatastores() {
		if (!enabled())
			return;

		List<String> wslist = reader.getWorkspaceNames();
		assertNotNull(wslist);
		assertEquals(7, wslist.size()); // value in default gs installation

		System.out.println("Workspaces:" + wslist.size());
		int dsnum = 0;
		for (String wsname : wslist) {
			System.out.print("Workspace " + wsname + " : ");
			List<String> result = reader.getDatastoresNames(wsname);
			assertNotNull(result);
			dsnum += result.size();
			for (String dsname : result) {
				System.out.print(dsname + " ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("Datastores:" + dsnum); // value in default gs
													// installation
		assertEquals(4, dsnum); // value in default gs installation

	}

	/**
	 * Test of getDatastore method, of class GeoServerRESTReader.
	 */
	public void testGetDatastore() {
	}

	/**
	 * Test of getLayer method, of class GeoServerRESTReader.
	 */
	public void testGetLayer() {
	}

	/**
	 * Test of getNamespaceNames method, of class GeoServerRESTReader.
	 */
	public void testGetNamespaceNames() {
		if (!enabled())
			return;

		List<String> result = reader.getNamespaceNames();
		assertNotNull(result);
		assertEquals(7, result.size()); // value in default gs installation

		System.out.println("Namespaces:" + result.size());
		System.out.print("Namespaces:");
		for (String name : result) {
			System.out.print(name + " ");
		}
		System.out.println();
	}

	/**
	 * Test of getWorkspaceNames method, of class GeoServerRESTReader.
	 */
	public void testGetWorkspaceNames() {
		if (!enabled())
			return;

		List<String> result = reader.getWorkspaceNames();
		assertNotNull(result);
		assertEquals(7, result.size()); // value in default gs installation

		System.out.println("Workspaces:" + result.size());
		System.out.print("Workspaces:");
		for (String name : result) {
			System.out.print(name + " ");
		}
		System.out.println();
	}

}
