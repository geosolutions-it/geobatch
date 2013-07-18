/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
import it.geosolutions.geobatch.annotations.ActionService;
import it.geosolutions.geobatch.annotations.CanCreateAction;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.tools.WorkspaceUtils;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
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
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $ GeoTIFFOverviewsEmbedder.java $ Revision: 0.1 $ 23/mar/07 11:42:25
 * @version $ GeoTIFFOverviewsEmbedder.java $ Revision: 0.2 $ 25/Apr/11 11:00:00
 * @version $ GeoTIFFOverviewsEmbedder.java $ Revision: 0.2 $ 09/May/12 12:00:00
 */
@ActionService(serviceId = "GeotiffGeoServerService")
public class GeotiffGeoServerAction extends BaseAction<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeotiffGeoServerAction.class);

    public static final GeoTiffFormat FORMAT = new GeoTiffFormat();

    public GeotiffGeoServerAction(GeoServerActionConfiguration configuration) throws IOException {
        super(configuration);
    }
    
    @CanCreateAction
    private static boolean canCreateActionGeotiffGeoServerAction(){
        LOGGER.info("Calculating if this action could be Created...");
        return true;
    }

    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {
        try {
            listenerForwarder.started();
            final GeoServerActionConfiguration configuration = getConfiguration();
            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                final String message = "DataFlowConfig is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }
            
            if (events == null) {
                final String message = "Incoming events queue is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }
            
            // returning queue
            final Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();

            // for each incoming file
            for (FileSystemEvent event : events) {
                
                
                
                final File inputFile = event.getSource();

                // checks on input file
                if (!inputFile.exists()) {
                    // ERROR or LOG since it does not exists
                    if (!configuration.isFailIgnored())
                        throw new IllegalStateException("File: " + inputFile.getAbsolutePath()
                                                        + " does not exist!");
                    else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("File: " + inputFile.getAbsolutePath() + " does not exist!");
                        }
                    }
                }
                // check if is File
                if (!inputFile.isFile()) {
                    // ERROR or LOG
                    if (!configuration.isFailIgnored())
                        throw new IllegalStateException("File: " + inputFile.getAbsolutePath()
                                                        + " is not a file!");
                    else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("File: " + inputFile.getAbsolutePath() + " is not a file!");
                        }
                    }
                }

                // check if we can read it
                if (!inputFile.canRead()) {
                    // ERROR or LOG
                    if (!configuration.isFailIgnored())
                        throw new IllegalStateException("File: " + inputFile.getAbsolutePath()
                                                        + " is not readable!");
                    else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("File: " + inputFile.getAbsolutePath() + " is not readablet!");
                        }
                    }
                }
                
                // do your magic
                listenerForwarder.setTask("Publishing: "+inputFile);
                // try to publish on geoserver
                if (publishGeoTiff(inputFile, configuration)) {
                    // if success add the geotiff to the output queue
                    ret.add(event);
                }
            }
            listenerForwarder.completed();
            return ret;
        } catch (Throwable t) {
            final String message = "FATAL -> " + t.getLocalizedMessage();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message, t); // no need to
            }
            listenerForwarder.failed(t);
            throw new ActionException(this, message, t);
        }
    }

    /**
     * If configuration.isFailIgnore and publish fails return false otherwise
     * true.<br>
     * If ! configuration.isFailIgnore and publish fails throws ActionException
     * otherwise return true.<br>
     * 
     * @param inputFile the file to publish
     * @param configuration the configuration to use
     * @return true if success, false otherwise.
     * @throws Exception
     */
    private static boolean publishGeoTiff(File inputFile, GeoServerActionConfiguration configuration)
        throws Exception {
        final String inputFileName = inputFile.getName();

        final String filePrefix = FilenameUtils.getBaseName(inputFile.getName());
        final String fileSuffix = FilenameUtils.getExtension(inputFile.getName());

        String baseFileName = null;
        final String fileNameFilter = configuration.getStoreFilePrefix();
        if (fileNameFilter != null) {
            if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
                && ("tif".equalsIgnoreCase(fileSuffix) || "tiff".equalsIgnoreCase(fileSuffix))) {
                // etj: are we missing something here?
                baseFileName = filePrefix;
            }
        } else if ("tif".equalsIgnoreCase(fileSuffix) || "tiff".equalsIgnoreCase(fileSuffix)) {
            baseFileName = filePrefix;
        }

        if (baseFileName == null) {
            final String message = "Unable to find a fileName for '" + inputFileName + "'";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            if (!configuration.isFailIgnored())
                throw new IllegalStateException(message);
        }

        // generate the coveragestore id
        final String coverageStoreId = FilenameUtils.getBaseName(inputFileName);

        // //
        // creating coverageStore
        // //
        GeoTiffReader coverageReader = null;

        // Trying to read the GeoTIFF to make sure it is correct
        CoordinateReferenceSystem crs = null;
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
            coverageReader = (GeoTiffReader)FORMAT.getReader(inputFile);
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
            crs = coverageReader.getCrs();
            if (crs != null) {
                epsgCode = CRS.lookupEpsgCode(crs, false);
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
				configuration.getGeoserverURL(),
				configuration.getGeoserverUID(),
				configuration.getGeoserverPWD());
		GeoServerRESTReader reader = new GeoServerRESTReader(
				configuration.getGeoserverURL(),
				configuration.getGeoserverUID(),
				configuration.getGeoserverPWD());
		WorkspaceUtils.createWorkspace(reader, publisher,
				configuration.getDefaultNamespace(),
				configuration.getDefaultNamespaceUri());

        // check transfer method
        String transferMethod = configuration.getDataTransferMethod();
        if (transferMethod == null) {
            transferMethod = "DIRECT"; // default one
        }

        // decide CRS
        // default crs
        final String defaultCRS = configuration.getCrs();
        String finalEPSGCode = defaultCRS;
        // retain original CRS if the code is there
        if (epsgCode == null) {
            // we do not have a valid EPSG code
            if (defaultCRS == null) {
                final String message = "Input file has no CRS neither the configuration provides a default one";
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(message);
                }
                if (!configuration.isFailIgnored())
                    throw new ActionException(GeotiffGeoServerAction.class, message);
                return false;

            }

        } else {
            finalEPSGCode = "EPSG:" + epsgCode;
        }

        // decide CRS management
        ProjectionPolicy projectionPolicy = ProjectionPolicy.NONE;
        if (crs == null) {
            // we do not have a valid CRS, we use the default one
            projectionPolicy = ProjectionPolicy.FORCE_DECLARED;

        } else
        // we DO have a valid CRS
        if (epsgCode == null) {
            // we do not have a CRS with a valid EPSG code, let's reproject on
            // the fly
            projectionPolicy = ProjectionPolicy.REPROJECT_TO_DECLARED;

        }

        GSCoverageEncoder coverage = new GSCoverageEncoder();
        coverage.setName(configuration.getLayerName() == null
              ? coverageStoreId : configuration.getLayerName());
        coverage.setTitle(configuration.getTitle());
        coverage.setDescription(configuration.getLayerDescription());
        coverage.setSRS(finalEPSGCode);
        coverage.setProjectionPolicy(projectionPolicy);
        coverage.setAbstract(configuration.getLayerAbstract());
        
//        GSWorkspaceEncoder workspace=new GSWorkspaceEncoder(configuration.getDefaultNamespace());
        
        GSLayerEncoder layer=new GSLayerEncoder();
        layer.setDefaultStyle(configuration.getDefaultStyle() != null ? configuration.getDefaultStyle() : "raster");
        layer.setQueryable(configuration.getQueryable());
        
        if ("DIRECT".equalsIgnoreCase(transferMethod)) {
            // Transferring the fil
            sent = publisher.publishGeoTIFF(configuration.getDefaultNamespace(),
                    configuration.getStoreName() == null
                    ? coverageStoreId : configuration.getStoreName(),
                configuration.getLayerName() == null
                    ? coverageStoreId : configuration.getLayerName(),
                inputFile,
                finalEPSGCode,
                projectionPolicy,
                configuration.getDefaultStyle() != null ? configuration.getDefaultStyle() : "raster",
                            null);
        } else if ("EXTERNAL".equalsIgnoreCase(configuration.getDataTransferMethod())) {
            sent = publisher.publishExternalGeoTIFF(configuration.getDefaultNamespace(), configuration.getStoreName() == null
                    ? coverageStoreId : configuration.getStoreName(), inputFile, coverage, layer)!=null?true:false;
//            sent = publisher.publishExternalGeoTIFF(configuration.getDefaultNamespace(),// workspace
//                                                    configuration.getStoreName() == null
//                                                        ? coverageStoreId : configuration.getStoreName(),
//                                                    inputFile,
//                                                    configuration.getLayerName() == null
//                                                        ? coverageStoreId : configuration.getLayerName(),
//                                                    finalEPSGCode,
//                                                    projectionPolicy,
//                                                    configuration.getDefaultStyle() != null ? configuration.getDefaultStyle() : "raster");
        } else {
            final String message = "FATAL -> Unknown transfer method "
                                   + configuration.getDataTransferMethod();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message);
            }
            sent = false;
        }
        if (sent) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Coverage SUCCESSFULLY sent to GeoServer!");
            }
            return true;
        } else {
            final String message = "Coverage was NOT sent to GeoServer due to connection errors!";
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(message);
            }
            if (!configuration.isFailIgnored())
                throw new ActionException(GeotiffGeoServerAction.class, message);
        }
        return false;
    }

}
