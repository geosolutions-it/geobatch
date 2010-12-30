package it.geosolutions.geobatch.ais.raster;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AISCoverageGeoServerGeneratorService
		extends
		GeoServerConfiguratorService<FileSystemMonitorEvent, GeoServerActionConfiguration> {

	private final static Logger LOGGER = Logger
			.getLogger(AISCoverageGeoServerGeneratorService.class.toString());

	public AISCoverageGeoServerGenerator createAction(
			GeoServerActionConfiguration configuration) {
		try {
			return new AISCoverageGeoServerGenerator(configuration);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
			return null;
		}
	}

	@Override
	public boolean canCreateAction(GeoServerActionConfiguration configuration) {
		final boolean superRetVal = super.canCreateAction(configuration);
		return superRetVal;
	}

}
