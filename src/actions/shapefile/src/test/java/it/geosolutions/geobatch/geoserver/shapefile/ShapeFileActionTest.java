package it.geosolutions.geobatch.geoserver.shapefile;

import java.io.File;
import java.net.URI;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.test.GeoServerTests;

public class ShapeFileActionTest extends GeoServerTests {

	static final Logger LOGGER = Logger.getLogger(ShapeFileActionTest.class);
	
	static final String FLOW_XML = "shapefile.xml";
	static final String MULTI_SHP_ZIP = "multipleshapefiles.zip";
	
	GeoServerActionConfiguration configuration;
	
	@Before
	public void setUp() throws Exception {
		XStream stream = new XStream();
        stream.alias("GeoServerShapeActionConfiguration",
        		it.geosolutions.geobatch.geoserver.shapefile.GeoServerShapeActionConfiguration.class);
		configuration = (GeoServerActionConfiguration)stream.fromXML(
				getClass().getResourceAsStream(FLOW_XML));
	}

	@Test
	public void testExecuteMultiShpZip() throws Exception {
		if (skipTest()) {
			LOGGER.warn("Skipping test.");
			return;
		}
		configuration.setGeoserverPWD(getPwd());
		configuration.setGeoserverUID(getUid());
		configuration.setGeoserverURL(getUrl());
		
		File zipFile = new File(getClass().getResource(MULTI_SHP_ZIP).toURI());
		
		Queue<EventObject> events = new LinkedList<EventObject>();
		events.add(new FileSystemEvent(zipFile, FileSystemEventType.FILE_ADDED));
		
		ShapeFileAction action = new ShapeFileAction(configuration);
		action.setTempDir(new File("/tmp"));
		action.execute(events);
	}
	
}
