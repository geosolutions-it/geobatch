package it.geosolutions.geobatch.ais.raster;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorService;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AISCoverageGeoServerGeneratorService extends
        GeoServerConfiguratorService<FileSystemEvent, GeoServerActionConfiguration> {

    public AISCoverageGeoServerGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(AISCoverageGeoServerGeneratorService.class);

    public AISCoverageGeoServerGenerator createAction(GeoServerActionConfiguration configuration) {
        try {
            return new AISCoverageGeoServerGenerator(configuration);
        } catch (IOException e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public boolean canCreateAction(GeoServerActionConfiguration configuration) {
        final boolean superRetVal = super.canCreateAction(configuration);
        return superRetVal;
    }

}
