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

package it.geosolutions.geobatch.geoserver.geotiff;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ GeoTIFFFolderGeoServerConfigurator.java $ Revision: x.x $ 23/mar/07 11:42:25
 */
public class GeoTIFFFolderGeoServerConfigurator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {
	
	public final static String GEOSERVER_VERSION = "2.X";

	protected GeoTIFFFolderGeoServerConfigurator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {
		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Starting with processing...");

		try {
			// looking for file
			if (events.size() == 0)
				throw new IllegalArgumentException("Wrong number of elements for this action: " + events.size());
			
			Collection<FileSystemMonitorEvent> layers = new ArrayList<FileSystemMonitorEvent>();
			
			while (events.size() > 0) {
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
				final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(), new File(
						((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

				// ////////////////////////////////////////////////////////////////////
				//
				// Checking input files.
				//
				// ////////////////////////////////////////////////////////////////////
				if ((workingDir == null) || !workingDir.exists()
						|| !workingDir.isDirectory()) {
					LOGGER.log(Level.SEVERE,
							"GeoServerDataDirectory is null or does not exist.");
					throw new IllegalStateException(
							"GeoServerDataDirectory is null or does not exist.");
				}

				// ... BUSINESS LOGIC ... //
				File inputDir = new File(event.getSource().getAbsolutePath());

				if (inputDir == null || !inputDir.exists() || !inputDir.isDirectory()) {
					LOGGER.log(Level.SEVERE, "Unexpected file '" + inputDir.getAbsolutePath() + "'");
					throw new IllegalStateException("Unexpected file '" + inputDir.getAbsolutePath() + "'");
				}

				FileWriter outFile = null;
				PrintWriter out = null;
				
				String[] fileNames = inputDir.list(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						if (FilenameUtils.getExtension(name).equalsIgnoreCase("tiff") || 
								FilenameUtils.getExtension(name).equalsIgnoreCase("tif"))
							return true;
						
						return false;
					}
					
				});
				
				List<String> fileNameList = Arrays.asList(fileNames);
				Collections.sort(fileNameList);
				fileNames = fileNameList.toArray(new String[1]);
				
				if (fileNames != null && fileNames.length > 0) {
					for (String fileName : fileNames) {
						String coverageStoreId = FilenameUtils.getBaseName(fileName);
							
						LOGGER.info("Coverage Store ID: " + coverageStoreId);
						// ////////////////////////////////////////////////////////////////////
						//
						// SENDING data to GeoServer via REST protocol.
						//
						// ////////////////////////////////////////////////////////////////////
						Map<String, String> queryParams = new HashMap<String, String>();
						queryParams.put("namespace", getConfiguration().getDefaultNamespace());
						queryParams.put("wmspath", getConfiguration().getWmsPath());
						final String[] layerResponse = GeoServerRESTHelper.send(
								inputDir,
								new File(inputDir, fileName),
								getConfiguration().getGeoserverURL(), 
								getConfiguration().getGeoserverUID(),
								getConfiguration().getGeoserverPWD(),
								coverageStoreId,
								coverageStoreId,
								queryParams,
								"",
								getConfiguration().getDataTransferMethod(),
								"geotiff",
								GEOSERVER_VERSION,
								getConfiguration().getStyles(),
								getConfiguration().getDefaultStyle()
						);
						
						if (layerResponse != null && layerResponse.length > 2) {
							String layer = layerResponse[0];
							LOGGER.info("ImageMosaicConfigurator layer: " + layer);
							
							final File layerDescriptor = new File(inputDir, layer + ".layer");
							
							if(layerDescriptor.createNewFile()) {
								try {
									outFile = new FileWriter(layerDescriptor);
									out = new PrintWriter(outFile);
									
									// Write text to file
									out.println("namespace=" + layerResponse[1]);
									out.println("storeid=" + layerResponse[2]);
									out.println("layerid=" + layer);
									out.println("driver=GeoTIFF");
									out.println("path=" + File.separator + FilenameUtils.getName(new File(inputDir, fileName).getAbsolutePath()));
								} catch (IOException e){
									LOGGER.log(Level.SEVERE, "Error occurred while writing indexer.properties file!", e);
								} finally {
									if (out != null) {
										out.flush();
										out.close();
									}
									
									outFile = null;
									out = null;
								}
								
								layers.add(new FileSystemMonitorEvent(layerDescriptor, FileSystemMonitorNotifications.FILE_ADDED));
							}
						}
					}
				}
			}
            
			// ... setting up the appropriate event for the next action
			events.addAll(layers);
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			JAI.getDefaultInstance().getTileCache().flush();
			return null;
		} finally {
			JAI.getDefaultInstance().getTileCache().flush();
		}
	}
}
