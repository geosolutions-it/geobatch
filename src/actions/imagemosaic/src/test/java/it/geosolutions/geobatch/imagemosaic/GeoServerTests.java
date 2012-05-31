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

import it.geosolutions.geoserver.rest.GeoServerRESTReader;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Abstract class to perform tests on a GeoServer targhet. Use the method
 * skipTest() function to check if the GeoServer can be used to perform tests.<br>
 * 
 * Available variables are:<br>
 * <ul>
 * <li><b>gs_url</b> - string - the url of the target geoserver</li>
 * <li><b>gs_uid</b> - string - the user for the target geoserver</li>
 * <li><b>gs_pwd</b> - string - the password for the target geoserver</li>
 * <li><b>gs_test</b> - string{true|false} - to enable|disable the geoserver tests</li>
 * </ul>
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public abstract class GeoServerTests {
	private final static Logger LOGGER = Logger.getLogger(GeoServerTests.class);

	public static final String URL;
	public static final String UID;
	public static final String PWD;

	private static boolean enabled = false;
	private static boolean existgs = false;

	static {
		URL = getenv("gs_url", "http://localhost:8080/geoserver");
		UID = getenv("gs_uid", "admin");
		PWD = getenv("gs_pwd", "geoserver");

		// These tests will destroy data, so let's make sure we do want to run
		// them
		enabled = getenv("gs_test", "false").equalsIgnoreCase("true");
		if (!enabled) {
			LOGGER.warn("Tests on GeoServer are disabled. Please read the documentation to enable them.");
		} else if (!geoServerExists(URL, UID, PWD)) {
			LOGGER.warn("Tests are enabled but GeoServer but could not access to the target GeoServer.");
		}
	}

	private static boolean geoServerExists(final String url, final String user,
			final String pass) {
		if (enabled) {
			GeoServerRESTReader reader;
			try {
				if (!existgs) {
					reader = new GeoServerRESTReader(new URL(URL), UID, PWD);
					existgs = reader.existGeoserver();
				}
			} catch (MalformedURLException e) {
				LOGGER.error(e.getLocalizedMessage(), e);
				return false;
			}
			if (!existgs) {
				LOGGER.error("Failing geoserver not found @:" + URL);
			}
		}
		return existgs;
	}

	public static String getenv(String envName, String envDefault) {
		String env = System.getenv(envName);
		String ret = System.getProperty(envName, env);
		LOGGER.debug("env var " + envName + " is " + ret);
		return ret != null ? ret : envDefault;
	}

	/**
	 * @return true if tests are disabled or geoserver is NOT reachable
	 */
	public static boolean skipTest() {
		return (!enabled || !existgs);
	}

}
