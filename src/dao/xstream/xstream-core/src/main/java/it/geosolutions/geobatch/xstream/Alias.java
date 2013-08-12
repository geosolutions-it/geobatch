/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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
package it.geosolutions.geobatch.xstream;

import it.geosolutions.geobatch.registry.AliasRegistry;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;

/**
 * TODO: We need to have one (or more) XML file and to bind aliases dynamically.
 * 
 * @author etj
 */
public class Alias {

    private final static Logger LOGGER = LoggerFactory.getLogger(Alias.class.getName());

    private AliasRegistry aliasRegistry;

    public AliasRegistry getAliasRegistry() {
        return aliasRegistry;
    }

    public void setAliasRegistry(AliasRegistry registry) {
        aliasRegistry = registry;
    }

    public void setAliases(XStream xstream) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Setting aliases.");
        }
        xstream.alias( "CatalogConfiguration",
                        it.geosolutions.geobatch.configuration.flow.file.FileBasedCatalogConfiguration.class);

        xstream.alias( "JAISettings",
                        it.geosolutions.geobatch.settings.jai.JAISettings.class);

        xstream.alias( "FlowConfiguration",
                it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class);

        xstream.alias( "EventConsumerConfiguration",
                        it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration.class,
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class);

        xstream.alias( "EventGeneratorConfiguration",
                        it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration.class,
                        it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration.class);

        xstream.aliasField("EventConsumerConfiguration",
                it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class,
                "eventConsumerConfiguration");

        xstream.aliasField("EventGeneratorConfiguration",
        		it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class,
                "eventGeneratorConfiguration");

        xstream.aliasField("ListenerConfigurations",
                it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class,
                "progressListenerConfigurations");

        xstream.addImplicitCollection(
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class,
                        "actions",
                        it.geosolutions.geobatch.configuration.event.action.ActionConfiguration.class);

        xstream.addImplicitCollection(
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class,
                        "listenerIds", "listenerId", String.class);

        xstream.addImplicitCollection(
                it.geosolutions.geobatch.configuration.event.action.ActionConfiguration.class,
                "listenerIds", "listenerId", String.class);


        // Back compatibility hack:
        // In these classes name and description fields are omitted
        // TODO: to be removed when all config files are corrected
        for (Class class1 : new Class[]{EventConsumerConfiguration.class, FileBasedEventConsumerConfiguration.class, EventGeneratorConfiguration.class}) {
            xstream.omitField(class1, "description");
            xstream.omitField(class1, "name");
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Workaround for older configuration files: omitting name and descr from " + class1.getSimpleName());
        }

        // Back compatibility hack:
        // In these classes workingDir field is omitted
        // TODO: to be removed when all config files are corrected
        for (Class class1 : new Class[]{FileBasedEventConsumerConfiguration.class, FileBasedFlowConfiguration.class}) {
            xstream.omitField(class1, "workingDirectory");
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Workaround for older configuration files: omitting workingDir from " + class1.getSimpleName());
        }

        // adding registered alias
        if (aliasRegistry != null) {
            for (Entry<String, Class<?>> entry : aliasRegistry) {
                xstream.processAnnotations(entry.getValue());   // added to process annotations in loaded classes
                xstream.alias(entry.getKey(), entry.getValue());
            }
        }

        // adding registered implicit collections
        if (aliasRegistry != null) {
            for (Entry<String, Class<?>> entry : aliasRegistry.implicitCollectionIterator()) {
                xstream.addImplicitCollection(entry.getValue(), entry.getKey());
            }
        }
        
        // adding class for XStream alias Annotation Process
        if (aliasRegistry != null) {
            for (Entry<String, Class<?>> entry : aliasRegistry.processAnnotationsIterator()) {
                xstream.processAnnotations(entry.getValue());
            }
        }
    }
    
}
