/*
 */

package it.geosolutions.geobatch.ctd;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */
public class CTDAliasRegistrar extends AliasRegistrar {

     public CTDAliasRegistrar(AliasRegistry registry) {
        LOGGER.info(getClass().getSimpleName() + ": registering alias.");
        registry.putAlias("CTDActionConfiguration", it.geosolutions.geobatch.ctd.configuration.CTDActionConfiguration.class);
     }
 }