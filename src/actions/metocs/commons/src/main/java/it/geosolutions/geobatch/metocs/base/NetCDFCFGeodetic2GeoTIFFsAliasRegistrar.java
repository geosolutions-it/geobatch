package it.geosolutions.geobatch.metocs.base;

import java.util.logging.Level;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

public class NetCDFCFGeodetic2GeoTIFFsAliasRegistrar extends AliasRegistrar {

    public NetCDFCFGeodetic2GeoTIFFsAliasRegistrar(AliasRegistry registry) {
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info(getClass().getSimpleName() + ": registering alias.");
        registry.putAlias("NetcdfGeodetic2GeoTiff", NetCDFCFGeodetic2GeoTIFFsConfiguration.class);
    }
    
}