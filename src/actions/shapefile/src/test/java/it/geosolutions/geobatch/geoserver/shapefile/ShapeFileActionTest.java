package it.geosolutions.geobatch.geoserver.shapefile;

import java.io.File;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.test.GeoServerTests;

public class ShapeFileActionTest extends GeoServerTests {

	static final String FLOW_XML = "shapefile.xml";
	static final String MULTI_SHP_ZIP = "";
	static final String MULTI_SHP_DIR = "";
	
	ShapeFileAction action;
	
	@Before
	public void setUp() throws Exception {
		XStream stream =new XStream();
		GeoServerActionConfiguration configuration = (GeoServerActionConfiguration)stream.fromXML(
				getClass().getResourceAsStream(FLOW_XML));
		action = new ShapeFileAction(configuration);
	}

	@Test
	public void testExecuteMultiShpZip() throws Exception {
		if (skipTest())
			return;
		
		File zipFile = new File(getClass().getResource(MULTI_SHP_ZIP).toURI());
		
		Queue<EventObject> events = new LinkedList<EventObject>();
		events.add(new FileSystemEvent(zipFile, FileSystemEventType.FILE_ADDED));
		
		//action.execute(events);
	}	
}
