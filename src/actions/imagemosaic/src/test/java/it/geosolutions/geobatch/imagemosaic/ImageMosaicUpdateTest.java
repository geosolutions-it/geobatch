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
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.geoserver.test.GeoServerTests;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.test.TestData;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Junit test case which make use of {@link GeoServerTests} class to tests the
 * mosaic functionalities.<br>
 * To use datastore ref {@link PostGisDataStoreTests}.
 * 
 * @see GeoServerTests
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class ImageMosaicUpdateTest {

	private final static Logger LOGGER = Logger
			.getLogger(ImageMosaicUpdateTest.class);
	
	private static File BASE_DIR;

	private static final String WORKSPACE = "topp";
	
	private static final String STORE = "external";

	private ImageMosaicCommand cmd;
	private File imgMscCmdFile;
	private ImageMosaicAction action;

	@Before
	public void setUp() throws Exception {
		if (GeoServerTests.skipTest()) {
			return;
		}
		// create in memory object
		List<File> addList = new ArrayList<File>();
		addList.add(TestData.file(this,
				"time_mosaic/world.200401.3x5400x2700.tiff"));
		addList.add(TestData.file(this,
				"time_mosaic/world.200402.3x5400x2700.tiff"));
		addList.add(TestData.file(this,
				"time_mosaic/world.200403.3x5400x2700.tiff"));
		addList.add(TestData.file(this,
				"time_mosaic/world.200404.3x5400x2700.tiff"));
		addList.add(TestData.file(this,
				"time_mosaic/world.200405.3x5400x2700.tiff"));
		addList.add(TestData.file(this,
				"time_mosaic/world.200406.3x5400x2700.tiff"));

		BASE_DIR = new File(TestData.file(this, null), STORE);

		cmd = new ImageMosaicCommand(BASE_DIR, addList, null);

		imgMscCmdFile = TestData.temp(this, "ImageMosaicCommand.xml");
		
		// serialize
		ImageMosaicCommand.serialize(cmd, imgMscCmdFile.toString());

		// action
		File workingDir = TestData.file(this.getClass(), null);

		// config
		ImageMosaicConfiguration conf = new ImageMosaicConfiguration("", "", "");
		conf.setTimeRegex("[0-9]{6}");
		conf.setTimeDimEnabled("true");
		conf.setTimePresentationMode("LIST");
		conf.setGeoserverURL(GeoServerTests.URL);
		conf.setGeoserverUID(GeoServerTests.UID);
		conf.setGeoserverPWD(GeoServerTests.PWD);

		// check for postgis
		if (PostGisDataStoreTests.existsPostgis()) {
			conf.setDatastorePropertiesPath(PostGisDataStoreTests.getDatastoreProperties().getAbsolutePath());
		}

		conf.setDefaultNamespace(WORKSPACE);
		conf.setDefaultStyle("raster");
		conf.setCrs("EPSG:4326");

		action = new ImageMosaicAction(conf);
		action.setConfigDir(workingDir);
		action.setRunningContext(TestData.file(this, null).getAbsolutePath());
	}

	@AfterClass
	public static void dispose() throws Exception {
		if (GeoServerTests.skipTest()) {
			return;
		}
		GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
				GeoServerTests.URL, GeoServerTests.UID, GeoServerTests.PWD);
		publisher.removeCoverageStore(WORKSPACE, STORE, true);
		// remove created dir
		FileUtils.deleteDirectory(BASE_DIR);
	}

	@Test
	public void create() throws IOException {
		// check if we have to skip tests
		if (GeoServerTests.skipTest()) {
			return;
		}
		// command
		Assert.assertNotNull(imgMscCmdFile);
		Assert.assertNotNull(action);

		// queue
		Queue<EventObject> queue = new LinkedList<EventObject>();
		queue.add(new FileSystemEvent(imgMscCmdFile,
				FileSystemEventType.FILE_ADDED));

		try {
			action.execute(queue);
		} catch (ActionException e) {
			Assert.fail(e.getLocalizedMessage());
		}

	}

	@Test
	public void update() throws IOException {
		if (GeoServerTests.skipTest()) {
			return;
		}
		if (!PostGisDataStoreTests.existsPostgis()){
			// could not update a shape file
			return;
		}
		// update command
		// add
		List<File> addList = new ArrayList<File>();
		addList.add(TestData.file(this,
				"time_mosaic/world.200407.3x5400x2700.tiff"));
		// remove
		List<File> delList = new ArrayList<File>();
		delList.add(TestData.file(this, STORE
				+ "/world.200401.3x5400x2700.tiff"));
		Assert.assertNotNull(cmd);

		cmd.setAddFiles(addList);
		cmd.setDelFiles(delList);

		Assert.assertNotNull(imgMscCmdFile);
		ImageMosaicCommand.serialize(cmd, imgMscCmdFile.getAbsolutePath());
		Assert.assertNotNull(action);

		// queue
		Queue<EventObject> queue = new LinkedList<EventObject>();
		queue.add(new FileSystemEvent(imgMscCmdFile,FileSystemEventType.FILE_ADDED));

		try {
			queue = action.execute(queue);
			if (queue.size() == 0)
				Assert.fail("Action produces no output");
		} catch (ActionException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}
}
