package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thoughtworks.xstream.XStream;
import it.geosolutions.geobatch.imagemosaic.config.DomainAttribute;
import it.geosolutions.geobatch.imagemosaic.utils.ConfigUtil;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"test-context.xml"})

public class ConfigurationDeserializationTest {

	@Autowired
	private AliasRegistry aliasRegistry;

	@Configuration
	static class ContextConfiguration {

	}

	@Test
	public void testImageMosaicConfigurationDeserialization() throws Exception{
		XStream xstream = new XStream();
		Alias alias=new Alias();
		alias.setAliasRegistry(aliasRegistry);
		alias.setAliases(xstream);
		File configFile = new File("src/test/resources/imagemosaic.xml");
		FileBasedFlowConfiguration configuration = (FileBasedFlowConfiguration)xstream.fromXML(configFile);
		boolean configurationDeserialized = false;
		for(ActionConfiguration actionConfiguration : configuration.getEventConsumerConfiguration().getActions()){
			if(actionConfiguration != null && actionConfiguration instanceof ImageMosaicConfiguration){
				configurationDeserialized = true;
				break;
			}
		}
		assertTrue(configurationDeserialized);
	
        ImageMosaicConfiguration cfg = (ImageMosaicConfiguration)configuration.getEventConsumerConfiguration().getActions().get(0);
        cfg.fixObsoleteConfig();
        
        DomainAttribute attr = ConfigUtil.getTimeAttribute(cfg);
        assertNotNull(attr);
        assertEquals("LIST", attr.getPresentationMode());

	}

	@Test
	public void testImageMosaicNewConfigurationDeserialization() throws Exception{

		XStream xstream = new XStream();
		Alias alias=new Alias();
		alias.setAliasRegistry(aliasRegistry);
		alias.setAliases(xstream);
		File configFile = new File("src/test/resources/imagemosaic_new.xml");
		FileBasedFlowConfiguration configuration = (FileBasedFlowConfiguration)xstream.fromXML(configFile);
		boolean configurationDeserialized = false;
		for(ActionConfiguration actionConfiguration : configuration.getEventConsumerConfiguration().getActions()){
			if(actionConfiguration != null && actionConfiguration instanceof ImageMosaicConfiguration){
				configurationDeserialized = true;
				break;
			}
		}
		assertTrue(configurationDeserialized);

        ImageMosaicConfiguration cfg = (ImageMosaicConfiguration)configuration.getEventConsumerConfiguration().getActions().get(0);

        {
            DomainAttribute attr = ConfigUtil.getTimeAttribute(cfg);
            assertNotNull(attr);
            assertEquals("LIST", attr.getPresentationMode());
            assertEquals("[0-9]{8}T[0-9]{9}Z(\\?!.\\*[0-9]{8}T[0-9]{9}Z.\\*)", attr.getRegEx());
        }

        cfg.fixObsoleteConfig();

        {
            DomainAttribute attr = ConfigUtil.getTimeAttribute(cfg);
            assertNotNull(attr);
            assertEquals("LIST", attr.getPresentationMode());
            assertEquals("[0-9]{8}T[0-9]{9}Z(\\?!.\\*[0-9]{8}T[0-9]{9}Z.\\*)", attr.getRegEx());
        }

	}

    @Test
	public void testImageMosaicNewConfigurationSerialization() throws Exception{

		XStream xstream = new XStream();
		Alias alias=new Alias();
		alias.setAliasRegistry(aliasRegistry);
		alias.setAliases(xstream);

        ImageMosaicConfiguration cfg = new ImageMosaicConfiguration("id", "name", "descr");
        cfg.setGeoserverUID("user");
        cfg.setGeoserverPWD("password");
        cfg.setGeoserverURL("http://geoserver.org");

        {
            DomainAttribute att = new DomainAttribute();
            att.setDimensionName(DomainAttribute.DIM_TIME);
            att.setRegEx(".*");
            cfg.addDomainAttribute(att);
        }

        {
            DomainAttribute att = new DomainAttribute();
            att.setDimensionName(DomainAttribute.DIM_ELEV);
            att.setAttribName("minz");
            att.setRegEx(".*");
            att.setEndRangeAttribName("maxz");
            att.setEndRangeRegEx(".*");
            att.setType(DomainAttribute.TYPE.DOUBLE);
            cfg.addDomainAttribute(att);
        }
        {
            DomainAttribute att = new DomainAttribute();
            att.setDimensionName("date");
            att.setRegEx("regexDATE");
            att.setType(DomainAttribute.TYPE.STRING);
            cfg.addDomainAttribute(att);
        }
        {
            DomainAttribute att = new DomainAttribute();
            att.setDimensionName("wavelenght");
            att.setAttribName("loww");
            att.setRegEx("regexLO");
            att.setEndRangeAttribName("highw");
            att.setEndRangeRegEx("regexHI");
            att.setType(DomainAttribute.TYPE.DOUBLE);
            cfg.addDomainAttribute(att);
        }

        xstream.toXML(cfg, System.out);
        System.out.println();

        cfg = cfg.clone();
        ConfigUtil.sanitize(cfg);

        System.out.println("\n\n Sanitized config:\n\n");
        xstream.toXML(cfg, System.out);
        System.out.println();

        System.out.println();
        System.out.println(ImageMosaicProperties.createIndexer(cfg));
    }
}
