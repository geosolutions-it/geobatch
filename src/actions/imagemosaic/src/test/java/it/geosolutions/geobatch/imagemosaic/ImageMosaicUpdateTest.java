package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Junit test case which make use of {@link GeoServerTests} class to tests the
 * mosaic functionalities.<br> To use datastore set following variables:<br>
 * <ul>
 * <li><b>postgis</b>=true</li>
 * <li><b>datastore_path</b>="${ABSOLUTE_PATH}"</li>
 * </ul>
 * <br>
 * 
 * @see GeoServerTests
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class ImageMosaicUpdateTest {

	private final static Logger LOGGER = Logger
			.getLogger(ImageMosaicUpdateTest.class);

	private ImageMosaicCommand cmd;
	private File imgMscCmdFile;
	private File imgMscCmdBaseDir;
	private ImageMosaicAction action;

	private static String workspace = "topp";
	private static String store = "external";

	/**
	 * check if postgis datastore is enabled
	 */
	private static boolean postgis;
	private static String datastore_path;
	static {
		postgis = GeoServerTests.getenv("postgis", "false").equalsIgnoreCase(
				"true");
		datastore_path = GeoServerTests.getenv("datastore_path",
				"time_mosaic/datastore.properties");
	}

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

		imgMscCmdBaseDir = new File(TestData.file(this, null), store);

		cmd = new ImageMosaicCommand(imgMscCmdBaseDir, addList, null);

		imgMscCmdFile = TestData.temp(this, "ImageMosaicCommand.xml");
		// serialize
		ImageMosaicCommand.serialize(cmd, imgMscCmdFile.toString());

		// action

		File workingDir = TestData.file(this.getClass(), null);

		// config
		ImageMosaicConfiguration conf = new ImageMosaicConfiguration("", "", "");
		conf.setWorkingDirectory(workingDir.getAbsolutePath());
		conf.setTimeRegex("[0-9]{6}");
		conf.setTimeDimEnabled("true");
		conf.setTimePresentationMode("LIST");
		conf.setGeoserverURL(GeoServerTests.URL);
		conf.setGeoserverUID(GeoServerTests.UID);
		conf.setGeoserverPWD(GeoServerTests.PWD);

		// check for postgis
		if (postgis) {
			File datastoreFile = new File(datastore_path);
			if (datastoreFile.isAbsolute()) {
				conf.setDatastorePropertiesPath(datastore_path);
			} else {
				conf.setDatastorePropertiesPath(TestData.file(this,
						"time_mosaic/datastore.properties").getAbsolutePath());
			}
		}

		conf.setDefaultNamespace("topp");
		conf.setDefaultStyle("raster");
		conf.setCrs("EPSG:4326");
		// conf.addListenerConfiguration(TesD);

		action = new ImageMosaicAction(conf);
		action.setRunningContext(TestData.file(this, null).getAbsolutePath());
	}

	@After
	public void tearDown() throws Exception {
		if (GeoServerTests.skipTest()) {
			return;
		}
		GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
				GeoServerTests.URL, GeoServerTests.UID, GeoServerTests.PWD);
		publisher.removeCoverageStore(workspace, store, true);
		// remove created dir
		FileUtils.deleteDirectory(imgMscCmdBaseDir);
	}

	@Test
	public void createTest() throws IOException {
		// check if we have to skip tests
		if (GeoServerTests.skipTest()) {
			return;
		}
		// command
		Assert.assertNotNull(imgMscCmdFile);
		Assert.assertNotNull(action);

		// queue
		Queue<FileSystemEvent> queue = new LinkedList<FileSystemEvent>();
		queue.add(new FileSystemEvent(imgMscCmdFile,
				FileSystemEventType.FILE_ADDED));

		try {
			action.execute(queue);
		} catch (ActionException e) {
			Assert.fail(e.getLocalizedMessage());
		}

	}

	@Test
	public void updateTest() throws IOException {
		if (GeoServerTests.skipTest()) {
			return;
		}
		// update command
		// add
		List<File> addList = new ArrayList<File>();
		addList.add(TestData.file(this,
				"time_mosaic/world.200407.3x5400x2700.tiff"));
		// remove
		List<File> delList = new ArrayList<File>();
		delList.add(TestData.file(this, store
				+ "/world.200401.3x5400x2700.tiff"));
		Assert.assertNotNull(cmd);

		cmd.setAddFiles(addList);
		cmd.setDelFiles(delList);

		Assert.assertNotNull(imgMscCmdFile);
		ImageMosaicCommand.serialize(cmd, imgMscCmdFile.getAbsolutePath());

		// check if we have to skip tests
		if (GeoServerTests.skipTest())
			return;

		Assert.assertNotNull(action);

		// queue
		Queue<FileSystemEvent> queue = new LinkedList<FileSystemEvent>();
		queue.add(new FileSystemEvent(imgMscCmdFile,
				FileSystemEventType.FILE_ADDED));

		try {
			queue = action.execute(queue);
			if (queue.size() == 0)
				Assert.fail("Action produces no output");
		} catch (ActionException e) {
			Assert.fail(e.getLocalizedMessage());
		}

	}

}
