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
package it.geosolutions.geobatch.annotationProcessor;

import it.geosolutions.geobatch.catalog.Resource;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DamianoG
 * This class is a Generalized Service used for create Action. In Geobatch 1.3.x for each action is mandatory to write an own Service.
 * This class take as Constructor inputs an id and a ClassType (relative to an Action) and provide generic methods (using reflection and annotation scanning) for create every type of actions.    
 */
public class GenericActionService implements Resource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericActionService.class);
    private Class<? extends BaseAction<? extends EventObject>> actionType;
    private String id;
    
    public GenericActionService(String id, Class<? extends BaseAction<? extends EventObject>> actionType){
        this.id = id;
        this.actionType = actionType;
    }
    
    public Class<? extends BaseAction<? extends EventObject>> getType(){
        return actionType;
    }
    
    /**
     * The canCreateAction method lookup for a CanCreateAction annotated method and invoke it, the result of that method will be returned to the caller.
     * If no annotated method will be found the result returned will be forced to TRUE (but log a message under WARN level)
     * If an exception occurrs during the method invocation the result will be forced to FALSE
     * Please note that it executes (or try to executes) just the first method found. So annotate more than one method per class does not make sense. 
     * @param actionConfig
     * @return
     */
    public boolean canCreateAction(ActionConfiguration actionConfig){
        Method[] methods = actionType.getDeclaredMethods();
        Boolean canCreateAction = true;
        boolean foundAtLeastOneMethod = false;
        for (Method el : methods) {
            if (el.isAnnotationPresent(CanCreateAction.class)) {
                el.setAccessible(true);
                foundAtLeastOneMethod = true;
                StringBuilder sb = new StringBuilder();
                sb.append("Find an CanCreateAction annotated method called: ").append(el.getName());
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug(sb.toString());
                }
                try {
                    canCreateAction = (Boolean) el.invoke(actionConfig);
                } catch (Exception e) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("An exception has occurred while invoking the canCreateAction").append(". The result will be forced to false, please check and fix this abnormal situation. - ").append(e.getLocalizedMessage());
                    if(LOGGER.isWarnEnabled()){
                        LOGGER.warn(sb2.toString(), e);
                    }
                    canCreateAction = false;
                }
                // It may be present other annotated methods... don't care, execute just the first...
                break;
            }
        }
        if(!foundAtLeastOneMethod){
            if(LOGGER.isWarnEnabled()){
                LOGGER.warn("No CanCreateAction annotated method found. Force the result to TRUE.");
            }
        }
        return canCreateAction;
    }
    
    /**
     * Istantiate an action class from the Class type and the ActionConfig provided
     * @param actionClass
     * @param actionConfig
     * @return
     */
    public BaseAction<? extends EventObject> createAction(Class<? extends BaseAction<? extends EventObject>> actionClass,
            ActionConfiguration actionConfig) {

        Constructor<? extends BaseAction<? extends EventObject>> constructor;
        try {
            constructor = actionClass.getConstructor(actionConfig.getClass());
            return constructor.newInstance(actionConfig);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
            throw new IllegalStateException(
                    "A fatal error occurred while instantiate the action class... "
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

}
