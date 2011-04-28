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
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageStore;

import java.io.File;
import java.io.IOException;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(GeoServerAction.class);

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

            // looking for file
            if (events.size() != 1) {
                final String message = "GeotiffGeoServerAction.execute(): Wrong number of elements for this action: "
                        + events.size();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalArgumentException(message);
            }

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                final String message = "GeotiffGeoServerAction.execute(): DataFlowConfig is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            final File workingDir = Path.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if ((workingDir == null) || !workingDir.exists() || !workingDir.isDirectory()) {
                final String message = "GeotiffGeoServerAction.execute(): GeoServerDataDirectory is null or does not exist.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }

            final FileSystemEvent event = events.remove();
            final File inputFile = event.getSource();
            // TODO checks on input file
            final String inputFileName = inputFile.getName();
            final String filePrefix = FilenameUtils.getBaseName(inputFile.getName());
            final String fileSuffix = FilenameUtils.getExtension(inputFile.getName());

            String baseFileName = null;
            final String fileNameFilter = getConfiguration().getStoreFilePrefix();
            if (fileNameFilter != null) {
                if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
                        && ("tif".equalsIgnoreCase(fileSuffix) || "tiff"
                                .equalsIgnoreCase(fileSuffix))) {
                    // etj: are we missing something here?
                    baseFileName = filePrefix;
                }
            } else if ("tif".equalsIgnoreCase(fileSuffix) || "tiff".equalsIgnoreCase(fileSuffix)) {
                baseFileName = filePrefix;
            }

            if (baseFileName == null) {
                final String message = "GeotiffGeoServerAction.execute(): Unexpected file '"
                        + inputFileName + "'";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }

            final String coverageStoreId = FilenameUtils.getBaseName(inputFileName);

            // //
            // creating coverageStore
            // //
            final GeoTiffFormat format = new GeoTiffFormat();
            GeoTiffReader coverageReader = null;

            // //
            // Trying to read the GeoTIFF
            // //
            /**
             * GeoServer url: "file:data/" + coverageStoreId + "/" + geoTIFFFileName
             */
            try {
                coverageReader = (GeoTiffReader) format.getReader(inputFile);

                if (coverageReader == null) {
                    final String message = "GeotiffGeoServerAction.execute(): No valid GeoTIFF File found for this Data Flow!";
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(message);
                    }
                    throw new IllegalStateException(message);
                }
            } finally {
                if (coverageReader != null) {
                    try {
                        coverageReader.dispose();
                    } catch (Throwable e) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace(
                                    "GeotiffGeoServerAction.execute(): "
                                            + e.getLocalizedMessage(), e);
                        }
                    }
                }
            }

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////
            boolean sent = false;
            GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(getConfiguration()
                    .getGeoserverURL(), getConfiguration().getGeoserverUID(), getConfiguration()
                    .getGeoserverPWD());

            if ("DIRECT".equals(getConfiguration().getDataTransferMethod())) {
                // TODO Deprecated: to be tested
                sent = publisher.publishGeoTIFF(getConfiguration().getDefaultNamespace(),
                        coverageStoreId, inputFile);
            } else if ("EXTERNAL".equals(getConfiguration().getDataTransferMethod())) {
                // String workspace, String coverageStore, File geotiff, String srs, String
                // defaultStyle
                RESTCoverageStore store = publisher.publishExternalGeoTIFF(getConfiguration()
                        .getDefaultNamespace(), coverageStoreId, inputFile, getConfiguration()
                        .getCrs(), getConfiguration().getDefaultStyle());
                sent = store != null;
            } else {
                final String message = "GeotiffGeoServerAction.execute(): FATAL -> Unknown transfer method "
                        + getConfiguration().getDataTransferMethod();
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(message);
                }

                if (sent) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("GeotiffGeoServerAction.execute(): coverage SUCCESSFULLY sent to GeoServer!");
                    }
                } else {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("GeotiffGeoServerAction.execute(): coverage was NOT sent to GeoServer due to connection errors!");
                    }
                }
            }

            listenerForwarder.completed();
            return events;
        } catch (Exception t) {
            final String message = "GeotiffGeoServerAction.execute(): FATAL -> "
                    + t.getLocalizedMessage();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message, t); // no need to
            }
            listenerForwarder.failed(t);
            throw new ActionException(this, message, t);
        }
    }

}
