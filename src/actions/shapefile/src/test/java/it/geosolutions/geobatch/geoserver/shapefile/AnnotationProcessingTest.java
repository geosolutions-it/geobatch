package it.geosolutions.geobatch.geoserver.shapefile;

import static org.junit.Assert.assertTrue;
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.ActionServicePostProcessor;
import it.geosolutions.geobatch.annotations.GenericActionService;
import it.geosolutions.geobatch.registry.AliasRegistry;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"test-context.xml"})

public class AnnotationProcessingTest {

	@Autowired
	private AliasRegistry aliasRegistry;

	@Configuration
	static class ContextConfiguration {}

	@Test
	public void testGeoServerShapeActionConfigurationAliased() throws Exception{

		Boolean configurationAliasRegistered = false;
		Class configurationClass = GeoServerShapeActionConfiguration.class;
		Iterator<Entry<String, Class<?>>> it = aliasRegistry.iterator();
		while (it.hasNext()){
			Entry<String, Class<?>> alias = it.next();	

			Action action = (Action)ShapeFileAction.class.getAnnotations()[0];
			String configurationAlias = action.configurationAlias();
			if(configurationAlias == null || configurationAlias.isEmpty()){
				configurationAlias = configurationClass.getSimpleName();
			}

			if(alias.getKey().equals(configurationAlias) && alias.getValue().equals(configurationClass)){
				configurationAliasRegistered = true;
				break;
			}
		}
		assertTrue(configurationAliasRegistered);

	}

	@Test
	public void testGeoServerShapeActionServiceGeneration() throws Exception{

		Boolean actionServiceRegistered = false;
		List<GenericActionService> actionList = ActionServicePostProcessor.getActionList();
		for(GenericActionService action : actionList){
			if(GeoServerShapeActionConfiguration.class.getSimpleName().equals(action.getId())){
				actionServiceRegistered = true;
				break;
			}
		}
		assertTrue(actionServiceRegistered);

	}

}
