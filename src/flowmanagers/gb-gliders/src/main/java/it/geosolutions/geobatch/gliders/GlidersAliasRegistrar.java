/*
 */

package it.geosolutions.geobatch.gliders;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class GlidersAliasRegistrar extends AliasRegistrar {

     public GlidersAliasRegistrar(AliasRegistry registry) {
        LOGGER.info(getClass().getSimpleName() + ": registering alias.");
        registry.putAlias("GlidersActionConfiguration", it.geosolutions.geobatch.gliders.configuration.GlidersActionConfiguration.class);
     }
 }