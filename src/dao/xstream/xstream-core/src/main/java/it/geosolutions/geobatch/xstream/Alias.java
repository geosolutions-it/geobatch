/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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

import com.thoughtworks.xstream.XStream;
import it.geosolutions.geobatch.registry.AliasRegistry;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: We XStreamFlowConfigurationDAOneed to have one (or more) XML file and to bind aliases dynamically.
 * 
 * @author etj
 */
public class Alias {

    private final static Logger LOGGER = Logger.getLogger(Alias.class.getName());

    private AliasRegistry aliasRegistry;

    public AliasRegistry getAliasRegistry() {
        return aliasRegistry;
    }
    
    public void setAliasRegistry(AliasRegistry registry) {
        aliasRegistry = registry;
    }


    public void setAliases(XStream xstream) {

    	if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("Setting aliases.");
        xstream.alias("CatalogConfiguration",
						it.geosolutions.geobatch.configuration.flow.file.FileBasedCatalogConfiguration.class);

        xstream.alias("FlowConfiguration",
						it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class);
        xstream.alias("FileEventRule",
						it.geosolutions.geobatch.flow.event.consumer.file.FileEventRule.class);

        xstream.alias("EventConsumerConfiguration",
                        it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration.class,
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class);
        
        xstream.alias("EventGeneratorConfiguration",
                it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration.class,
                it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration.class);

        xstream.aliasField("EventConsumerConfiguration",
						it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class,
						"eventConsumerConfiguration");
        
        xstream.aliasField("EventGeneratorConfiguration",
        		it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class,
				"eventGeneratorConfiguration");
        

        xstream.addImplicitCollection(
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class,
                        "rules",
                        it.geosolutions.geobatch.flow.event.consumer.file.FileEventRule.class);

        xstream.addImplicitCollection(
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class,
                        "actions",
                        it.geosolutions.geobatch.configuration.event.action.ActionConfiguration.class);


        // adding registered alias
        if (aliasRegistry != null) {
            for (Entry<String, Class<?>> entry : aliasRegistry) {
                xstream.alias(entry.getKey(), entry.getValue());
            }
        }
    }

}
