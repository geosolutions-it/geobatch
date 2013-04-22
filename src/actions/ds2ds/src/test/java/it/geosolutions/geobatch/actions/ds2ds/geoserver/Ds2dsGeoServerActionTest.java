package it.geosolutions.geobatch.actions.ds2ds.geoserver;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.geoserver.test.GeoServerTests;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import com.thoughtworks.xstream.XStream;

/**
 * Ds2dsGeoServerAction test class
 * The test is based on a postgis instance whose parameters
 * are specified in the input states.xml (see test-data/inputs/states.xml)
 * 
 * @author Emmanuel Blondel (FAO)
 *
 */
public class Ds2dsGeoServerActionTest extends GeoServerTests{


		static final Logger LOGGER = Logger.getLogger(Ds2dsGeoServerActionTest.class);
		static final String FLOW_XML = "ds2dsGeoserver.xml";
		
		Ds2dsGeoServerConfiguration configuration;
		
		@Before
		public void setUp() throws Exception {
		XStream stream = new XStream();
		stream.alias("Ds2dsGeoServerConfiguration",
				Ds2dsGeoServerConfiguration.class);
		configuration = (Ds2dsGeoServerConfiguration) stream.fromXML(this
				.getResourceFile(FLOW_XML));

		configuration.setGeoserverPWD(getPwd());
		configuration.setGeoserverUID(getUid());
		configuration.setGeoserverURL(getUrl());
		
		}
		
		@Test
		public void testExecuteXml() throws Exception {

			configuration.setFeatureConfiguration(FeatureConfiguration.fromXML(
					new FileInputStream(getResourceFile("states.xml"))));
			
			Queue<EventObject> events = new LinkedList<EventObject>();
			events.add(new FileSystemEvent(getResourceFile("states.xml"),
					FileSystemEventType.FILE_ADDED));

			Ds2dsGeoServerAction action = new Ds2dsGeoServerAction(configuration);
			action.setTempDir(new File("/tmp"));
			action.execute(events);
		}
		
		private File getResourceFile(String resource) throws URISyntaxException {
			return new File(this.getClass().getResource("/test-data/inputs/"+resource).toURI());
		}
		
	}