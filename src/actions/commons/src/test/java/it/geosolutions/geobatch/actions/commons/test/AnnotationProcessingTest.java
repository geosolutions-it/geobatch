package it.geosolutions.geobatch.actions.commons.test;

import static org.junit.Assert.assertTrue;
import it.geosolutions.geobatch.actions.commons.CollectorAction;
import it.geosolutions.geobatch.actions.commons.CollectorConfiguration;
import it.geosolutions.geobatch.actions.commons.CopyAction;
import it.geosolutions.geobatch.actions.commons.CopyConfiguration;
import it.geosolutions.geobatch.actions.commons.ExtractAction;
import it.geosolutions.geobatch.actions.commons.ExtractConfiguration;
import it.geosolutions.geobatch.actions.commons.MoveAction;
import it.geosolutions.geobatch.actions.commons.MoveConfiguration;
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
	public void testCollectorConfigurationAliased() throws Exception{
		Boolean configurationAliasRegistered = false;
		Class configurationClass = CollectorConfiguration.class;
		Iterator<Entry<String, Class<?>>> it = aliasRegistry.iterator();
		while (it.hasNext()){
			Entry<String, Class<?>> alias = it.next();
			
			Action action = (Action)CollectorAction.class.getAnnotations()[0];
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
	public void testCollectorActionServiceGeneration() throws Exception{
		Boolean actionServiceRegistered = false;
		List<GenericActionService> actionList = ActionServicePostProcessor.getActionList();
		for(GenericActionService action : actionList){
			if(CollectorConfiguration.class.getSimpleName().equals(action.getId())){
				actionServiceRegistered = true;
				break;
			}
		}
		assertTrue(actionServiceRegistered);
	}
	
	@Test
	public void testCopyConfigurationAliased() throws Exception{
		Boolean configurationAliasRegistered = false;
		Class configurationClass = CopyConfiguration.class;
		Iterator<Entry<String, Class<?>>> it = aliasRegistry.iterator();
		while (it.hasNext()){
			Entry<String, Class<?>> alias = it.next();		
			
			Action action = (Action)CopyAction.class.getAnnotations()[0];
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
	public void testCopyActionServiceGeneration() throws Exception{
		Boolean actionServiceRegistered = false;
		List<GenericActionService> actionList = ActionServicePostProcessor.getActionList();
		for(GenericActionService action : actionList){
			if(CopyConfiguration.class.getSimpleName().equals(action.getId())){
				actionServiceRegistered = true;
				break;
			}
		}
		assertTrue(actionServiceRegistered);
	}
	
	@Test
	public void testExtractConfigurationAliased() throws Exception{
		Boolean configurationAliasRegistered = false;
		Class configurationClass = ExtractConfiguration.class;
		Iterator<Entry<String, Class<?>>> it = aliasRegistry.iterator();
		while (it.hasNext()){
			Entry<String, Class<?>> alias = it.next();		
			
			Action action = (Action)ExtractAction.class.getAnnotations()[0];
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
	public void testExtractActionServiceGeneration() throws Exception{
		Boolean actionServiceRegistered = false;
		List<GenericActionService> actionList = ActionServicePostProcessor.getActionList();
		for(GenericActionService action : actionList){
			if(ExtractConfiguration.class.getSimpleName().equals(action.getId())){
				actionServiceRegistered = true;
				break;
			}
		}
		assertTrue(actionServiceRegistered);
	}
	
	@Test
	public void testMoveConfigurationAliased() throws Exception{
		Boolean configurationAliasRegistered = false;
		Class configurationClass = MoveConfiguration.class;
		Iterator<Entry<String, Class<?>>> it = aliasRegistry.iterator();
		while (it.hasNext()){
			Entry<String, Class<?>> alias = it.next();	
			
			Action action = (Action)MoveAction.class.getAnnotations()[0];
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
	public void testMoveActionServiceGeneration() throws Exception{
		Boolean actionServiceRegistered = false;
		List<GenericActionService> actionList = ActionServicePostProcessor.getActionList();
		for(GenericActionService action : actionList){
			if(MoveConfiguration.class.getSimpleName().equals(action.getId())){
				actionServiceRegistered = true;
				break;
			}
		}
		assertTrue(actionServiceRegistered);
	}
	
}
