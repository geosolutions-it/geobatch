/*
 */

package it.geosolutions.geobatch.geoserver;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class GeoServerAliasRegistrar extends AliasRegistrar {

     public GeoServerAliasRegistrar(AliasRegistry registry) {
        LOGGER.info(getClass().getSimpleName() + ": registering alias.");
        registry.putAlias("GeoServerActionConfiguration", GeoServerActionConfiguration.class);
     }
}