/*
 */

package it.geosolutions.geobatch.gwc;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class GWCAliasRegistrar extends AliasRegistrar {

     public GWCAliasRegistrar(AliasRegistry registry) {
        LOGGER.info(getClass().getSimpleName() + ": registering alias.");
        registry.putAlias("GeoWebCacheActionConfiguration", it.geosolutions.geobatch.gwc.GeoWebCacheActionConfiguration.class);
    }
}