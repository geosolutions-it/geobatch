/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geobatch.annotations;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistry;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Process Spring Beans after initialization looking for {@link AliasRegistry} to dynamically inject configuration alias into registry and associate action class to configuration class.
 * <p/>
 * All classes annotated with {@link Action} will be processed, the class specified in "configurationClass" parameter will be aliased.
 * <br/>
 * The Action class is associated with the name of configuration class to retrieve it after XML deserialization.
 * <br/>
 * If annotation parameter configurationAlias is empty, the Configuration class name will be used as alias; 
 * <br/>
 * For the follow example of XML configuration the alias should be GeotiffOverviewsEmbedderConfiguration, or a GeotiffOverviewsEmbedderConfiguration string must be set as configurationAlias
 * <pre>
 * {@code
 * <GeotiffOverviewsEmbedderConfiguration>
 * <id>GeoTiffOverviewsEmbedder</id>
 * ...
 * </GeotiffOverviewsEmbedderConfiguration>
 * }
 * </pre>
 * @author	Sandro Salari
 * @see     AbstractActionServicePostProcessor
 * @see     BeanPostProcessor
 */

public class ActionServicePostProcessor extends AbstractActionServicePostProcessor implements BeanPostProcessor, ApplicationContextAware {

	private static List<GenericActionService> actionList = new ArrayList<GenericActionService>();

    private ApplicationContext applicationContext;

	public static List<GenericActionService> getActionList() {
		return actionList;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if(bean.getClass().equals(AliasRegistry.class)){
			AliasRegistry aliasRegistry = (AliasRegistry)bean;
			ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
			scanner.addIncludeFilter(new AnnotationTypeFilter(Action.class));
			for (BeanDefinition bd : scanner.findCandidateComponents("it.geosolutions")){
				try {
					Class actionClass = Class.forName(bd.getBeanClassName());
					Action annotation = (Action) actionClass.getAnnotation(Action.class);
					if(annotation != null){
						Class<? extends ActionConfiguration> configurationClass = annotation.configurationClass();
						
						String alias = configurationClass.getSimpleName();
						if(annotation.configurationAlias() != null && !annotation.configurationAlias().isEmpty()){
							alias = annotation.configurationAlias();
						}
						aliasRegistry.putAlias(alias, configurationClass);

						GenericActionService asr = new GenericActionService(annotation.configurationClass().getSimpleName(),actionClass);
                        asr.setApplicationContext(applicationContext);
						actionList.add(asr); 
					}
				} catch (Exception e) {
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
