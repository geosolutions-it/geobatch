package it.geosolutions.geobatch.actions.ds2ds.geoserver;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
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
 * Environment variable to set:
 * <b>ds2ds_test</b> - string{true|false} - to enable|disable the tests
 *
 * @author Emmanuel Blondel (FAO)
 *
 */
public class Ds2dsGeoServerActionTest{

		private static boolean enabled = false;

		static final Logger LOGGER = Logger.getLogger(Ds2dsGeoServerActionTest.class);
		static final String FLOW_XML = "ds2dsGeoserver.xml";
		
		Ds2dsGeoServerConfiguration configuration;
		
		static{
			enabled = getenv("ds2ds_test", "false").equalsIgnoreCase("true");
	        if (!enabled) {
	            LOGGER.warn("Tests on GeoServer are disabled. Please read the documentation to enable them.");
	        } 
		}
		
		public static String getenv(String envName, String envDefault) {
	        String env = System.getenv(envName);
	        String ret = System.getProperty(envName, env);
	        LOGGER.debug("env var " + envName + " is " + ret);
	        return ret != null ? ret : envDefault;
	    }
		
		@Before
		public void setUp() throws Exception {
		XStream stream = new XStream();
		stream.alias("Ds2dsGeoServerConfiguration",
				Ds2dsGeoServerConfiguration.class);
		configuration = (Ds2dsGeoServerConfiguration) stream.fromXML(this
				.getResourceFile(FLOW_XML));
		
		}
		
		@Test
		public void testExecuteXml() throws Exception {

			if(enabled){
				configuration.setFeatureConfiguration(FeatureConfiguration.fromXML(
						new FileInputStream(getResourceFile("states.xml"))));
				
				Queue<EventObject> events = new LinkedList<EventObject>();
				events.add(new FileSystemEvent(getResourceFile("states.xml"),
						FileSystemEventType.FILE_ADDED));

				Ds2dsGeoServerAction action = new Ds2dsGeoServerAction(configuration);
				action.setTempDir(new File("/tmp"));
				action.execute(events);
			}
			
		}
		
		private File getResourceFile(String resource) throws URISyntaxException {
			return new File(this.getClass().getResource("/test-data/inputs/"+resource).toURI());
		}
		
	}