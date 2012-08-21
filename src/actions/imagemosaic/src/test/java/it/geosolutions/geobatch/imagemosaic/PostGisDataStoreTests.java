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

import it.geosolutions.geobatch.geoserver.test.GeoServerTests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.geotools.test.TestData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * To use datastore set following variables:<br>
 * <ul>
 * <li><b>postgis</b>=true</li>
 * <li><b>datastore_path</b>="${ABSOLUTE_PATH}"</li>
 * </ul>
 * <br>
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class PostGisDataStoreTests {

	/**
	 * check if postgis datastore is enabled
	 */
	private static boolean POSTGIS;
	private static String DATASTORE_PATH;
	
	static {
		POSTGIS = GeoServerTests.getenv("postgis", "false").equalsIgnoreCase(
				"true");
		DATASTORE_PATH = GeoServerTests.getenv("datastore_path",
				"datastore.properties");
	}
	
	public static boolean existsPostgis(){
		return POSTGIS;
	}
	
	public static File getDatastoreProperties(){
		return new File(DATASTORE_PATH);
	}
	
	@Before
	public void setUP() throws FileNotFoundException, IOException{		
		// check for postgis
		File datastoreFile = new File(DATASTORE_PATH);
		if (!datastoreFile.isAbsolute()) {
			DATASTORE_PATH=TestData.file(this,"datastore.properties").getAbsolutePath();
		}

	}
	
	@Test
	public void getPropertiesTest() throws UnsatisfiedLinkError,
			FileNotFoundException {
		final Properties props;
		try {

			props = ImageMosaicProperties.getPropertyFile(PostGisDataStoreTests
					.getDatastoreProperties());
			/**
			 * SPI=org.geotools.data.postgis.PostgisNGDataStoreFactory<br>
			 * port=5432<br>
			 * host=localhost<br>
			 * schema=public<br>
			 * database=db<br>
			 * user=gis<br>
			 * passwd=gis<br>
			 */
			Assert.assertNotNull(props.getProperty("port"));
			Assert.assertNotNull(props.getProperty("host"));
			Assert.assertNotNull(props.getProperty("schema"));
			Assert.assertNotNull(props.getProperty("database"));
			Assert.assertNotNull(props.getProperty("user"));
			Assert.assertNotNull(props.getProperty("passwd"));
			
		} catch (NullPointerException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (IOException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

}
