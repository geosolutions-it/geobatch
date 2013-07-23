package it.geosolutions.geobatch.actions.geonetwork;

import static org.junit.Assert.assertTrue;

import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkDeleteConfiguration;
import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkInsertConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"test-context.xml"})

public class ConfigurationDeserializationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDeserializationTest.class);

	@Autowired
	private AliasRegistry aliasRegistry;

	@Configuration
	static class ContextConfiguration {

	}

	@Test
	public void testGeonetworkDeleteDeserialization() throws Exception{
		XStream xstream = new XStream();
		Alias alias=new Alias();
		alias.setAliasRegistry(aliasRegistry);
		alias.setAliases(xstream);
		File configFile = new File("src/test/resources/geonetworkDeleteFlow.xml");
		FileBasedFlowConfiguration configuration = (FileBasedFlowConfiguration)xstream.fromXML(configFile);
		boolean configurationDeserialized = false;
		for(ActionConfiguration actionConfiguration : configuration.getEventConsumerConfiguration().getActions()){
			if(actionConfiguration != null && actionConfiguration instanceof GeonetworkDeleteConfiguration){
				configurationDeserialized = true;
				break;
			} else {
                LOGGER.info("Rejecting config as " + actionConfiguration.getClass());
            }
		}
		assertTrue(configurationDeserialized);

	}

	@Test
	public void testGeonetworkInsertDeserialization() throws Exception{
		XStream xstream = new XStream();
		Alias alias=new Alias();
		alias.setAliasRegistry(aliasRegistry);
		alias.setAliases(xstream);
		File configFile = new File("src/test/resources/geonetworkInsertFlow.xml");
		FileBasedFlowConfiguration configuration = (FileBasedFlowConfiguration)xstream.fromXML(configFile);
		boolean configurationDeserialized = false;
		for(ActionConfiguration actionConfiguration : configuration.getEventConsumerConfiguration().getActions()){
			if(actionConfiguration != null && actionConfiguration instanceof GeonetworkInsertConfiguration){
				configurationDeserialized = true;

				break;
			}
		}
		assertTrue(configurationDeserialized);

	}

}
