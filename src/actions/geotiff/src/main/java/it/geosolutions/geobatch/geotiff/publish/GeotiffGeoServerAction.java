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
package it.geosolutions.geobatch.geotiff.publish;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageStore;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;

import java.io.File;
import java.io.IOException;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author AlFa
 * @author (r2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $ GeoTIFFOverviewsEmbedder.java $ Revision: 0.1 $ 23/mar/07 11:42:25
 * @version $ GeoTIFFOverviewsEmbedder.java $ Revision: 0.2 $ 25/Apr/11 11:00:00
 */
public class GeotiffGeoServerAction extends BaseAction<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeotiffGeoServerAction.class);
    
    private static final GeoTiffFormat FORMAT = new GeoTiffFormat();

    private final GeoServerActionConfiguration configuration;

    public GeotiffGeoServerAction(GeoServerActionConfiguration configuration)
            throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    public GeoServerActionConfiguration getConfiguration() {
        return configuration;
    }

    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {
        try {
            listenerForwarder.started();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                final String message = "GeotiffGeoServerAction.execute(): DataFlowConfig is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }

            while (events.size()>0){
	            final FileSystemEvent event = events.remove();
	            final File inputFile = event.getSource();
	            

	            // checks on input file
	            if(!inputFile.exists()){
	            	// ERROR or LOG
	    			if (!configuration.isFailIgnored())
	    				throw new IllegalStateException("File: "+inputFile.getAbsolutePath()+" does not exist!");
	    			else {
	    				if(LOGGER.isWarnEnabled()){
	    					LOGGER.warn("File: "+inputFile.getAbsolutePath()+" does not exist!");
	    				}
	    			}
	            }
            	// check if is File
            	if(!inputFile.isFile()){
	            	// ERROR or LOG
	    			if (!configuration.isFailIgnored())
	    				throw new IllegalStateException("File: "+inputFile.getAbsolutePath()+" is not a file!");
	    			else {
	    				if(LOGGER.isWarnEnabled()){
	    					LOGGER.warn("File: "+inputFile.getAbsolutePath()+" is not a file!");
	    				}
	    			}
            	}	
            	
            	// check if we can read it
            	if(!inputFile.canRead()){
	            	// ERROR or LOG
	    			if (!configuration.isFailIgnored())
	    				throw new IllegalStateException("File: "+inputFile.getAbsolutePath()+" is not readable!");
	    			else {
	    				if(LOGGER.isWarnEnabled()){
	    					LOGGER.warn("File: "+inputFile.getAbsolutePath()+" is not readablet!");
	    				}
	    			}
            	}	            
	            
            	// do your magic
            	publishGeoTiff(inputFile);
            }
            listenerForwarder.completed();
            return events;
        } catch (Throwable t) {
            final String message = "GeotiffGeoServerAction.execute(): FATAL -> "
                    + t.getLocalizedMessage();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message, t); // no need to
            }
            listenerForwarder.failed(t);
            throw new ActionException(this, message, t);
        }
    }


	private void publishGeoTiff(File inputFile) throws Exception {
		final String inputFileName = inputFile.getName();
		listenerForwarder.setTask("Working on: " + inputFileName);

		final String filePrefix = FilenameUtils
				.getBaseName(inputFile.getName());
		final String fileSuffix = FilenameUtils.getExtension(inputFile
				.getName());

		String baseFileName = null;
		final String fileNameFilter = getConfiguration().getStoreFilePrefix();
		if (fileNameFilter != null) {
			if ((filePrefix.equals(fileNameFilter) || filePrefix
					.matches(fileNameFilter))
					&& ("tif".equalsIgnoreCase(fileSuffix) || "tiff"
							.equalsIgnoreCase(fileSuffix))) {
				// etj: are we missing something here?
				baseFileName = filePrefix;
			}
		} else if ("tif".equalsIgnoreCase(fileSuffix)
				|| "tiff".equalsIgnoreCase(fileSuffix)) {
			baseFileName = filePrefix;
		}

		if (baseFileName == null) {
			final String message = "GeotiffGeoServerAction.execute(): Unexpected file '"
					+ inputFileName + "'";
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message);
			if (!configuration.isFailIgnored())
				throw new IllegalStateException(message);
		}

		final String coverageStoreId = FilenameUtils.getBaseName(inputFileName);

		// //
		// creating coverageStore
		// //
		GeoTiffReader coverageReader = null;

		// //
		// Trying to read the GeoTIFF
		// //
		Integer epsgCode = null;
		try {
			if (!FORMAT.accepts(inputFile)) {
				final String message = "No valid GeoTIFF File found for this Data Flow!";
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(message);
				}
				if (!configuration.isFailIgnored())
					throw new IllegalStateException(message);
			}
			coverageReader = (GeoTiffReader) FORMAT.getReader(inputFile);

			if (coverageReader == null) {
				final String message = "No valid GeoTIFF File found for this Data Flow!";
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(message);
				}
				if (!configuration.isFailIgnored())
					throw new IllegalStateException(message);
			}

			// get the CRS or go back to the default one as per the
			// configuration if we cannot find one
			final CoordinateReferenceSystem crs = coverageReader.getCrs();
			if (crs != null) {
				epsgCode = CRS.lookupEpsgCode(crs, true);
			}
		} finally {
			if (coverageReader != null) {
				try {
					coverageReader.dispose();
				} catch (Throwable e) {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace(e.getLocalizedMessage(), e);
					}
				}
			}
		}

		//
		// SENDING data to GeoServer via REST protocol.
		//
		boolean sent = false;
		GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
				getConfiguration().getGeoserverURL(), getConfiguration()
						.getGeoserverUID(), getConfiguration()
						.getGeoserverPWD());

		if ("DIRECT".equalsIgnoreCase(getConfiguration()
				.getDataTransferMethod())) {
			// TODO Deprecated: to be tested
			sent = publisher.publishGeoTIFF(getConfiguration()
					.getDefaultNamespace(), coverageStoreId, getConfiguration().getLayerName(), inputFile);
		} else if ("EXTERNAL".equalsIgnoreCase(getConfiguration()
				.getDataTransferMethod())) {
			sent = publisher.publishExternalGeoTIFF(
					getConfiguration().getDefaultNamespace(),// workspace
					coverageStoreId, //coverageStore
					inputFile,
					getConfiguration().getLayerName(),
					epsgCode != null ? "EPSG:" + Integer.toString(epsgCode): getConfiguration().getCrs(), // retain original CRS if the code is there
					ProjectionPolicy.FORCE_DECLARED,
					getConfiguration().getDefaultStyle());// defaultStyle
		} else {
			final String message = "FATAL -> Unknown transfer method "
					+ getConfiguration().getDataTransferMethod();
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(message);
			}
			sent = false;
		}
		if (sent) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Coverage SUCCESSFULLY sent to GeoServer!");
			}
		} else {
			final String message = "Coverage was NOT sent to GeoServer due to connection errors!";
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(message);
			}
			if (!configuration.isFailIgnored())
				throw new IllegalStateException(message);

		}
	}

}
