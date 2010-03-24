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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

/**
 *
 * @author etj
 */
public class GeoserverRESTPublisherTest extends TestCase {

	public static final String RESTURL  = "http://localhost:8080/geoserver";
	public static final String RESTUSER = "admin";
	public static final String RESTPW   = "geoserver";

	public static final GeoServerRESTPublisher publisher
			= new GeoServerRESTPublisher(RESTURL, RESTUSER, RESTPW);

	public static final URL URL;
	public static final GeoServerRESTReader reader;

	static {
		URL lurl = null;
		try {
			lurl = new URL(RESTURL);
		} catch (MalformedURLException ex) {
		}

		URL = lurl;
		reader = new GeoServerRESTReader(lurl);
	}

    public GeoserverRESTPublisherTest(String testName) {
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


	public void testInsertDelete() throws FileNotFoundException {
		String layerName = "";
/*
		// dry run delete to work in a known state
		if(reader.getLayer(layerName) != null) {
			publisher.deleteLayer(layerName);
		}

		// known state?
		assertFalse("Cleanup failed", existsLayer(layerName));

		// test insert
		publisher.publishExternalGeoTIFF(layerName, layerName, null);
		assertTrue(existsLayer(layerName));

		//test delete
		publisher.deleteLayer(layerName);
		assertFalse(existsLayer(layerName));
*/
	}

	private boolean existsLayer(String layername) {
		return reader.getLayer(layername) != null;
	}

}