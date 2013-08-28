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

import it.geosolutions.geobatch.catalog.Resource;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

/**
 * @author DamianoG
 * This class is a Generalized Service used for create Action. In Geobatch 1.3.x for each action is mandatory to write an own Service.
 * This class take as Constructor inputs an id and a ClassType (relative to an Action) and provide generic methods (using reflection and annotation scanning) for create every type of actions.    
 */
public class GenericActionService implements Resource {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericActionService.class);
	private Class<? extends BaseAction<? extends EventObject>> actionType;
	private String id;

    private ApplicationContext applicationContext;

	public GenericActionService(String id, Class<? extends BaseAction<? extends EventObject>> actionType){
		this.id = id;
		this.actionType = actionType;
	}

	public Class<? extends BaseAction<? extends EventObject>> getType(){
		return actionType;
	}

	/**
	 * The canCreateAction method lookup for a checkConfiguration annotated method and invoke it, the result of that method will be returned to the caller.
	 * If no annotated method will be found the result returned will be forced to TRUE (but log a message under WARN level)
	 * If an exception occurrs during the method invocation the result will be forced to FALSE
	 * Please note that it executes (or try to executes) just the first method found. So annotate more than one method per class does not make sense. 
	 * @param actionConfig
	 * @return
	 */
	public boolean checkConfiguration(ActionConfiguration actionConfig){
		Boolean isConfigurationOk = true;
		Method el = null;
		for (Method method : actionType.getDeclaredMethods()){
			if( method.isAnnotationPresent(CheckConfiguration.class)){
				el = method;
				break;
			}
		}
		if(el != null){
			el.setAccessible(true);
			StringBuilder sb = new StringBuilder();
			sb.append("Found a @CheckConfiguration annotated method called: ").append(el.getName());
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(sb.toString());
			}
			try {			
				isConfigurationOk = (Boolean) el.invoke(actionType.getDeclaredConstructor(actionConfig.getClass()).newInstance(actionConfig));
			} catch (Exception e) {
				if(LOGGER.isWarnEnabled()){
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("An exception has occurred while invoking the CheckConfiguration")
                            .append(". The result will be forced to false, please check and fix this abnormal situation. ");
                    if(e.getMessage() != null)
                            sb2.append(e.getMessage());
					LOGGER.warn(sb2.toString(), e);
				}
				isConfigurationOk = false;
			}
		}
		return isConfigurationOk;
	}

	/**
	 * Istantiate an action class from the Class type and the ActionConfig provided.
     * <p/>
     * Once the class is instantiated: <ol>
     * <li>{@code @autowire} fields are autowired</li>
     * <li>{@code afterPropertiesSet()} is called if {@code InitializingBean} is declared</li>
     * </ol>
	 * @param actionClass
	 * @param actionConfig
	 * @return
	 */
	public <T extends BaseAction<? extends EventObject>> T createAction(
            Class<T> actionClass,
			ActionConfiguration actionConfig) {
		
		try {
            LOGGER.info("Instantiating action " + actionClass.getSimpleName());
			Constructor<T> constructor =
                    actionClass.getConstructor(actionConfig.getClass());
            T newInstance = constructor.newInstance(actionConfig);

            ((AbstractRefreshableApplicationContext)applicationContext).getBeanFactory().autowireBean(newInstance);
            if(InitializingBean.class.isAssignableFrom(actionClass)) {
                ((InitializingBean)newInstance).afterPropertiesSet();
            }

            return newInstance;
		} catch (Exception e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}
			throw new IllegalStateException(
					"A fatal error occurred while instantiate the action class " + actionClass.getSimpleName() +": "
							+ e.getLocalizedMessage());
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;

	}

	@Override
	public void dispose() {
		// TODO wich implementation goes here???
	}

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
