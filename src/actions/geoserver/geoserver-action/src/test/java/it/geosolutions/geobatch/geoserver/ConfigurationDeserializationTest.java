package it.geosolutions.geobatch.geoserver;

import static org.junit.Assert.assertTrue;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.geoserver.reload.GeoServerReloadConfiguration;
import it.geosolutions.geobatch.geoserver.style.GeoServerStyleConfiguration;
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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"test-context.xml"})

public class ConfigurationDeserializationTest {

	@Autowired
	private AliasRegistry aliasRegistry;

	@Configuration
	static class ContextConfiguration {

	}

	@Test
	public void testGeoServerReloadDeserialization() throws Exception{
		
		XStream xstream = new XStream();
		Alias alias=new Alias();
		alias.setAliasRegistry(aliasRegistry);
		alias.setAliases(xstream);
		File configFile = new File("src/test/resources/reload.xml");
		FileBasedFlowConfiguration configuration = (FileBasedFlowConfiguration)xstream.fromXML(configFile);
		boolean configurationDeserialized = false;
		for(ActionConfiguration actionConfiguration : configuration.getEventConsumerConfiguration().getActions()){
			if(actionConfiguration != null && actionConfiguration instanceof GeoServerReloadConfiguration){
				configurationDeserialized = true;
				break;
			}
		}
		assertTrue(configurationDeserialized);
		
	}

	@Test
	public void testGeoServerStyleDeserialization() throws Exception{
		
		XStream xstream = new XStream();
		Alias alias=new Alias();
		alias.setAliasRegistry(aliasRegistry);
		alias.setAliases(xstream);
		File configFile = new File("src/test/resources/style.xml");
		FileBasedFlowConfiguration configuration = (FileBasedFlowConfiguration)xstream.fromXML(configFile);
		boolean configurationDeserialized = false;
		for(ActionConfiguration actionConfiguration : configuration.getEventConsumerConfiguration().getActions()){
			if(actionConfiguration != null && actionConfiguration instanceof GeoServerStyleConfiguration){
				configurationDeserialized = true;
				break;
			}
		}
		assertTrue(configurationDeserialized);
		

	}

}
