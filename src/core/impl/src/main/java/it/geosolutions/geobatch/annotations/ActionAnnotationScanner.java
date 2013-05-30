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
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.registry.AliasRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * @author DamianoG
 * 
 * TODO check log level
 * TODO better avoid use of static method? using static method is very usefull register this bean in appContext! Initialize the Action list could be usefull?
 * 
 */
public class ActionAnnotationScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionAnnotationScanner.class);
    
    private List<GenericActionService> actionList;
    private AliasRegistry aliasRegistry;
    
    public void init() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(ActionAnnotationScanner.class.getCanonicalName() + " INIT!");
        }
        actionList = scanForActionService();
        scanForManageAlias();
    }
    
    public List<GenericActionService> getActionList(){
        return actionList;
    }
    
    public void setAliasRegistry(AliasRegistry aliasRegistry){
        this.aliasRegistry = aliasRegistry;
    }

    /**
     * Search for Class annotated by ActionService in the whole classpath 
     * TODO Search only classes that extends BaseAction ???
     * 
     * @return
     * @throws ClassNotFoundException
     */
    private List<GenericActionService> scanForActionService() {

        StringBuilder sb = new StringBuilder();
        List<GenericActionService> actionServiceList = new ArrayList<GenericActionService>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(ActionService.class));
        // TODO How AND this 2 filter ??? scanner.addIncludeFilter(new AssignableTypeFilter(BaseAction.class));
        
        // TODO provide a method for provide from outside the base package
        Set<BeanDefinition> annotatedClasses = scanner.findCandidateComponents("it.geosolutions.geobatch");
        if (LOGGER.isInfoEnabled()) {
            sb = new StringBuilder();
            sb.append("Found ").append(annotatedClasses.size()).append(" ActionService annotated Classes");
            LOGGER.info(sb.toString());
        }
        for (BeanDefinition bd : annotatedClasses) {
            String className = bd.getBeanClassName();
            sb = new StringBuilder();
            sb.append("found Action: ").append(className);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(sb.toString());
            }
            try {
                Class c = Class.forName(className);
                ActionService as = (ActionService) c.getAnnotation(ActionService.class);
                // check if c is a subclass of BaseAction, otherwise the asSubclass method throws ClassCastException
                c.asSubclass(BaseAction.class);
                GenericActionService asr = new GenericActionService(as.serviceId(),c);
                actionServiceList.add(asr);
            } catch (ClassNotFoundException e) {
                if(LOGGER.isWarnEnabled()){
                    sb = new StringBuilder();
                    sb.append("The Action annotated class '").append(className)
                            .append("' has caused and error, exception message is: ")
                            .append(e.getLocalizedMessage());
                    LOGGER.warn(sb.toString());
                }
            }
        }
        return actionServiceList;
    }
    
    private void scanForManageAlias() {

        StringBuilder sb = new StringBuilder();
        List<GenericActionService> actionServiceList = new ArrayList<GenericActionService>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(ManageAlias.class));
        // TODO How AND this 2 filter ??? scanner.addIncludeFilter(new AssignableTypeFilter(BaseAction.class));
        
        // TODO provide a method for provide from outside the base package
        Set<BeanDefinition> annotatedClasses = scanner.findCandidateComponents("it.geosolutions.geobatch");
        if (LOGGER.isInfoEnabled()) {
            sb = new StringBuilder();
            sb.append("Found ").append(annotatedClasses.size()).append(" ManageAlis annotated Classes");
            LOGGER.info(sb.toString());
        }
        for (BeanDefinition bd : annotatedClasses) {
            String className = bd.getBeanClassName();
            sb = new StringBuilder();
            sb.append("found annotated Configuration: ").append(className);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(sb.toString());
            }
            Class configurationClass = null;
            try {
                configurationClass = Class.forName(className);
                // check if c is a subclass of ActionConfiguration, otherwise the asSubclass method throws ClassCastException
                configurationClass.asSubclass(ActionConfiguration.class);
                // Put the class in AliasRegistry
                aliasRegistry.putProcessAnnotations(className, configurationClass);
            } catch (ClassNotFoundException e) {
                if(LOGGER.isWarnEnabled()){
                    sb = new StringBuilder();
                    sb.append("The configuration annotated class '").append(className)
                            .append("' has caused and error, exception message is: ")
                            .append(e.getLocalizedMessage());
                    LOGGER.warn(sb.toString());
                }
            }
        }
    }

    public void dispose() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(ActionAnnotationScanner.class.getCanonicalName() + " DISPOSING!");
        }
    }
}
