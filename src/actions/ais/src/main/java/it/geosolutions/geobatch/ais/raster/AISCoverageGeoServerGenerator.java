package it.geosolutions.geobatch.ais.raster;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.ascii.AsciiGeoServerGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class AISCoverageGeoServerGenerator extends AsciiGeoServerGenerator {

	protected AISCoverageGeoServerGenerator(GeoServerActionConfiguration configuration)
			throws IOException {
		super(configuration);
	}
	
	public void sendToGeoServer(File workingDir, FileSystemMonitorEvent event, String coverageStoreId, String storeFilePrefix, String configId) throws MalformedURLException, FileNotFoundException {
		// ////////////////////////////////////////////////////////////////////
		//
		// SENDING data to GeoServer via REST protocol.
		//
		// ////////////////////////////////////////////////////////////////////
		// http://localhost:8080/geoserver/rest/coveragestores/test_cv_store/test/file.tiff
		LOGGER.info("Sending ArcGrid to GeoServer ... "
				+ getConfiguration().getGeoserverURL());
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("namespace", getConfiguration()
				.getDefaultNamespace());
		queryParams.put("wmspath", getConfiguration().getWmsPath());
		queryParams.put("style", getConfiguration().getDefaultStyle());

		send(workingDir, event.getSource(), getConfiguration()
				.getGeoserverURL(), new Long(event.getTimestamp())
				.toString(), coverageStoreId, storeFilePrefix,
				getConfiguration().getStyles(), configId,
				getConfiguration().getDefaultStyle(), queryParams,
				getConfiguration().getDataTransferMethod());

		LOGGER.info("Update Last" + getConfiguration().getId()
				+ " GeoServer layer  ... "
				+ getConfiguration().getGeoserverURL());

		queryParams.put("coverageName", "Last"+getConfiguration().getId());
		
		send(workingDir, event.getSource(), getConfiguration()
				.getGeoserverURL(), new Long(event.getTimestamp())
				.toString(), "Last" + getConfiguration().getId(),
				storeFilePrefix, getConfiguration().getStyles(), configId,
				getConfiguration().getDefaultStyle(), queryParams,
				getConfiguration().getDataTransferMethod());
	}
	
	

}
