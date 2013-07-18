package it.geosolutions.geobatch.annotations;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistry;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class ActionServicePostProcessor extends AbstractActionServicePostProcessor implements BeanPostProcessor{

	private static List<GenericActionService> actionList = new ArrayList<GenericActionService>();
	
	public static List<GenericActionService> getActionList() {
		return actionList;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if(bean.getClass().equals(AliasRegistry.class)){
			AliasRegistry aliasRegistry = (AliasRegistry)bean;
			ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
			scanner.addIncludeFilter(new AnnotationTypeFilter(ActionService.class));
			for (BeanDefinition bd : scanner.findCandidateComponents("it.geosolutions")){
				try {
					Class serviceClass = Class.forName(bd.getBeanClassName());
					
					//Find costructor with ActionConfiguration argument type or use annotation
					Constructor costr = serviceClass.getConstructors()[0];
					Class<? extends ActionConfiguration> configurationClass = costr.getParameterTypes()[0];
					
					//ActionService annotation = (ActionService) serviceClass.getAnnotation(ActionService.class);
					//Class<? extends ActionConfiguration> configurationClass = annotation.configurationClass();
					
					String standardAlias = configurationClass.getSimpleName();
					aliasRegistry.putAlias(standardAlias, configurationClass);
					
					//Register service to classpath
					GenericActionService asr = new GenericActionService(serviceClass.getSimpleName()+"Service",serviceClass);
					actionList.add(asr); 

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}

		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}


}
