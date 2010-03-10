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



package it.geosolutions.geobatch.geoserver.ascii;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.arcgrid.ArcGridFormat;
import org.geotools.gce.arcgrid.ArcGridReader;

/**
 * @author fabiania
 * 
 */
public class AsciiGeoServerGenerator extends GeoServerConfiguratorAction<FileSystemMonitorEvent> {

    protected AsciiGeoServerGenerator(GeoServerActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    /**
     * @param inputDataDir
     * @param geoserverBaseURL
     * @param storeFilePrefix
     * @param timeStamp
     * @param dataStyles
     * @param configId
     * @param defaultStyle
     * @param coverageStoreId
     * @param dataTransferMethod
     * @param files
     * @throws MalformedURLException
     * @throws FileNotFoundException
     */
    public void send(final File inputDataDir, final File data, final String geoserverBaseURL,
            final String timeStamp, final String coverageStoreId, final String storeFilePrefix,
            final List<String> dataStyles, final String configId, final String defaultStyle,
            final Map<String, String> queryParams, String dataTransferMethod)
            throws MalformedURLException, FileNotFoundException {
        URL geoserverREST_URL = null;
        boolean sent = false;

        String coverageNamePath = "";
    	if(queryParams.get("coverageName") != null){
    		coverageNamePath = "&coverageName="+queryParams.get("coverageName");
    	}
    	
        if ("DIRECT".equals(getConfiguration().getDataTransferMethod())) {
			geoserverREST_URL = new URL(geoserverBaseURL + "/rest/workspaces/"
					+ queryParams.get("namespace") + "/coveragestores/"
					+ coverageStoreId + "/file.arcgrid?" + "style="
					+ queryParams.get("style") + "&" + "wmspath="
					+ queryParams.get("wmspath") + coverageNamePath);
			sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL,
					new FileInputStream(data), getConfiguration()
							.getGeoserverUID(), getConfiguration()
							.getGeoserverPWD());
		} else if ("URL".equals(getConfiguration().getDataTransferMethod())) {
			geoserverREST_URL = new URL(geoserverBaseURL + "/rest/workspaces/"
					+ queryParams.get("namespace") + "/coveragestores/"
					+ coverageStoreId + "/url.arcgrid?" + "style="
					+ queryParams.get("style") + "&" + "wmspath="
					+ queryParams.get("wmspath") + coverageNamePath);
			sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data
					.toURI().toURL().toExternalForm(), getConfiguration()
					.getGeoserverUID(), getConfiguration().getGeoserverPWD());
		} else if ("EXTERNAL"
				.equals(getConfiguration().getDataTransferMethod())) {
			geoserverREST_URL = new URL(geoserverBaseURL + "/rest/workspaces/"
					+ queryParams.get("namespace") + "/coveragestores/"
					+ coverageStoreId + "/external.arcgrid?" + "style="
					+ queryParams.get("style") + "&" + "wmspath="
					+ queryParams.get("wmspath") + coverageNamePath);
			System.out.println(geoserverREST_URL);
			sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data
					.toURI().toURL().toExternalForm(), getConfiguration()
					.getGeoserverUID(), getConfiguration().getGeoserverPWD());
		}

        if (sent) {
            LOGGER.info("ArcGrid GeoServerConfiguratorAction: coverage SUCCESSFULLY sent to GeoServer!");

            // //
            // Storing SLDs
            // //
            boolean sldsCreatedOK = true;

            for (String styleName : dataStyles) {
                File sldFile = new File(inputDataDir, "/" + configId + "/" + timeStamp + "/"
                        + styleName + ".sld");
                geoserverREST_URL = new URL(geoserverBaseURL + "/rest/styles/" + styleName);

                if (GeoServerRESTHelper.putTextFileTo(geoserverREST_URL, 
												new FileInputStream(sldFile),
												getConfiguration().getGeoserverUID(),
												getConfiguration().getGeoserverPWD())) {
                    geoserverREST_URL = new URL(geoserverBaseURL + "/rest/sldservice/updateLayer/"
												+ storeFilePrefix);
                    GeoServerRESTHelper.putContent(geoserverREST_URL,
													"<LayerConfig><Style>"
													+ styleName
													+ "</Style></LayerConfig>",
													getConfiguration().getGeoserverUID(),
													getConfiguration().getGeoserverPWD());


                    LOGGER.info("ArcGrid GeoServerConfiguratorAction: SLD SUCCESSFULLY sent to GeoServer!");
                } else {
                    LOGGER.info("ArcGrid GeoServerConfiguratorAction: SLD was NOT sent to GeoServer!");
                    sldsCreatedOK = false;
                }
            }

            // //
            // if it's all OK, set the Default SLD
            // //
            if (sldsCreatedOK) {
                geoserverREST_URL = new URL(geoserverBaseURL + "/rest/sldservice/updateLayer/"
                        + storeFilePrefix);
                GeoServerRESTHelper.putContent(geoserverREST_URL,
												"<LayerConfig><DefaultStyle>"
												+ defaultStyle
												+ "</DefaultStyle></LayerConfig>",
												getConfiguration().getGeoserverUID(),
												getConfiguration().getGeoserverPWD());
            }
        } else {
            LOGGER.info("ArcGrid GeoServerConfiguratorAction: coverage was NOT sent to GeoServer due to connection errors!");
        }
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            FileSystemMonitorEvent event = events.remove();
            final String configId = configuration.getName();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if ((workingDir == null) || !workingDir.exists() || !workingDir.isDirectory()) {
                LOGGER.log(Level.SEVERE, "GeoServerDataDirectory is null or does not exist.");
                throw new IllegalStateException("GeoServerDataDirectory is null or does not exist.");
            }

            if ((getConfiguration().getGeoserverURL() == null)
					|| "".equals(getConfiguration().getGeoserverURL())) {
                LOGGER.log(Level.SEVERE, "GeoServerCatalogServiceURL is null.");
                throw new IllegalStateException("GeoServerCatalogServiceURL is null.");
            }

            //
            // // //
            // // looking for optional parameters.
            // // //
            // if ((destinationCrs == null) && (configCRS != null)) {
            // final String authority = configCRS.getAuthority();
            // final BigInteger code = configCRS.getCode();
            //
            // if ((authority != null) && (code != null)) {
            // try {
            // destinationCrs = org.geotools.referencing.CRS.decode(authority + ":" + code, true);
            // } catch (NoSuchAuthorityCodeException e) {
            // LOGGER.info("No right CRS ('AUTH','CODE') specified ... using the native one!");
            // destinationCrs = null;
            // } catch (FactoryException e) {
            // LOGGER.info("No right CRS ('AUTH','CODE') specified ... using the native one!");
            // destinationCrs = null;
            // }
            // } else {
            // final String WKT = configCRS.getStringValue();
            //
            // try {
            // destinationCrs = org.geotools.referencing.CRS.parseWKT(WKT);
            // } catch (FactoryException e) {
            // LOGGER.info("No right CRS ('WKT') specified ... using the native one!");
            // destinationCrs = null;
            // }
            // }
            // }
            //
            // // //
            // // the destination Envelope is acceptable only if the CRS was specified.
            // // //
            // if ((destinationEnvelope == null) && (destinationCrs != null) && (configEvnelope !=
            // null)) {
            // final int dim = configEvnelope.getDimension().intValue();
            //
            // if (dim != 2) {
            // LOGGER.info("Only 2D Envelopes are supported!");
            // LOGGER.info("No right ENVELOPE specified ... using the native one!");
            // }
            //
            // final double[] minCP = new double[2];
            // final double[] maxCP = new double[2];
            //
            // String[] pos_0 = configEvnelope.getPosArray(0).split(" ");
            // String[] pos_1 = configEvnelope.getPosArray(1).split(" ");
            //
            // try {
            // minCP[0] = Double.parseDouble(pos_0[0]);
            // minCP[1] = Double.parseDouble(pos_0[1]);
            // maxCP[0] = Double.parseDouble(pos_1[0]);
            // maxCP[1] = Double.parseDouble(pos_1[1]);
            //
            // destinationEnvelope = new GeneralEnvelope(minCP, maxCP);
            // destinationEnvelope.setCoordinateReferenceSystem(destinationCrs);
            // } catch (NumberFormatException e) {
            // LOGGER.info("No right ENVELOPE specified ... using the native one!");
            // destinationEnvelope = null;
            // }
            // }

            // ////////////////////////////////////////////////////////////////////
            //
            // Creating ArcGrid coverageStore.
            //
            // ////////////////////////////////////////////////////////////////////

            // //
            // looking for file
            // //
            String inputFileName = event.getSource().getAbsolutePath();
            final String filePrefix = FilenameUtils.getBaseName(inputFileName);
            final String fileSuffix = FilenameUtils.getExtension(inputFileName);
			String storeFilePrefix = getConfiguration().getStoreFilePrefix();

            if (storeFilePrefix != null) {
                if ((filePrefix.equals(storeFilePrefix) || filePrefix.matches(storeFilePrefix))
                        && ("tif".equalsIgnoreCase(fileSuffix) || "tiff"
                                .equalsIgnoreCase(fileSuffix))) {
                }
            } else if ("tif".equalsIgnoreCase(fileSuffix) || "tiff".equalsIgnoreCase(fileSuffix)) {
                storeFilePrefix = filePrefix;
            }

            inputFileName = FilenameUtils.getName(inputFileName);
            final String coverageStoreId = FilenameUtils.getBaseName(inputFileName);

            // //
            // creating coverageStore
            // //
            final ArcGridFormat format = new ArcGridFormat();
            ArcGridReader coverageReader;

            // //
            // Trying to read the ArcGrid
            // //
            /**
             * GeoServer url: "file:data/" + coverageStoreId + "/" + ascFileName
             */
            try {
                coverageReader = (ArcGridReader) format.getReader(event.getSource());

                if (coverageReader == null) {
                    LOGGER.log(Level.SEVERE, "No valid ArcGrid File found for this Data Flow!");
                    throw new IllegalStateException(
                            "No valid ArcGrid File found for this Data Flow!");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "No valid ArcGrid File found for this Data Flow: "
                        + e.getLocalizedMessage());
                throw new IllegalStateException("No valid ArcGrid File found for this Data Flow: "
                        + e.getLocalizedMessage());
            }

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////
            // http://localhost:8080/geoserver/rest/coveragestores/test_cv_store/test/file.tiff
            this.sendToGeoServer(workingDir, event, coverageStoreId, storeFilePrefix, configId);
            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }

    }

	public void sendToGeoServer(File workingDir, FileSystemMonitorEvent event, String coverageStoreId, String storeFilePrefix, String configId) throws MalformedURLException, FileNotFoundException {
		LOGGER.info("Sending ArcGrid to GeoServer ... " + getConfiguration().getGeoserverURL());
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("namespace", getConfiguration().getDefaultNamespace());
        queryParams.put("wmspath", getConfiguration().getWmsPath());
        send(workingDir, 
				event.getSource(),
				getConfiguration().getGeoserverURL(),
				new Long(event.getTimestamp()).toString(),
				coverageStoreId,
				storeFilePrefix,
				getConfiguration().getStyles(),
				configId,
				getConfiguration().getDefaultStyle(),
                queryParams,
				getConfiguration().getDataTransferMethod());
		
	}
}
