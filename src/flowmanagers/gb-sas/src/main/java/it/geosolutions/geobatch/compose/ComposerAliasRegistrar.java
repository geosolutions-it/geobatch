/*
 */

package it.geosolutions.geobatch.compose;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class ComposerAliasRegistrar extends AliasRegistrar {

     public ComposerAliasRegistrar(AliasRegistry registry) {
         LOGGER.info(getClass().getSimpleName() + ": registering alias.");
         registry.putAlias("ComposerConfiguration", it.geosolutions.geobatch.compose.ComposerConfiguration.class);
     }
 }
