/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * 
 * Public class to ingest a geotiff image-mosaic into GeoServer
 * 
 */
public class ImageMosaicConfigurator extends
		ImageMosaicConfiguratorAction<FileSystemMonitorEvent> {

    /**
     * 
     */
	public final static String GEOSERVER_VERSION = "2.x";

	protected ImageMosaicConfigurator(
			ImageMosaicActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	/**
	 * 
	 */
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

				//
				// CREATE REGEX PROPERTIES FILES
				//
				
				if (configuration.getDatastorePropertiesPath() != null) {
					final File dsFile = IOUtils.findLocation(configuration.getDatastorePropertiesPath(), new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
					if (dsFile != null && dsFile.exists() && !dsFile.isDirectory()) {
						LOGGER.info("DataStore file found: " + dsFile.getAbsolutePath());
						FileUtils.copyFileToDirectory(dsFile, inputDir);
					}
				}
				
				final File indexer        = new File(inputDir, "indexer.properties");
				final File timeregex      = new File(inputDir, "timeregex.properties");
				final File elevationregex = new File(inputDir, "elevationregex.properties");

				FileWriter outFile = null;
				PrintWriter out = null;

				// INDEXER
				try {
					outFile = new FileWriter(indexer);
					out = new PrintWriter(outFile);
					
					// Write text to file
					if (configuration.getTimeRegex() != null)
						out.println("TimeAttribute=ingestion");
					
					if (configuration.getElevationRegex() != null)
						out.println("ElevationAttribute=elevation");
					
					out.println("Schema=*the_geom:Polygon,location:String" + 
							(configuration.getTimeRegex() != null ? ",ingestion:java.util.Date" : "") +
							(configuration.getElevationRegex() != null ? ",elevation:Double" : "")
					);
					out.println("PropertyCollectors=" + 
							(configuration.getTimeRegex() != null ? "TimestampFileNameExtractorSPI[timeregex](ingestion)" : "") + 
							(configuration.getElevationRegex() != null ? (configuration.getTimeRegex() != null ? "," : "") + "ElevationFileNameExtractorSPI[elevationregex](elevation)" : "")
					);
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

				// TIME REGEX
				if (configuration.getTimeRegex() != null) {
					try {
						outFile = new FileWriter(timeregex);
						out = new PrintWriter(outFile);
						
						// Write text to file
						out.println("regex=" + configuration.getTimeRegex());
					} catch (IOException e){
						LOGGER.log(Level.SEVERE, "Error occurred while writing timeregex.properties file!", e);
					} finally {
						if (out != null) {
							out.flush();
							out.close();
						}
						
						outFile = null;
						out = null;
					}
				}

				// ELEVATION REGEX
				if (configuration.getElevationRegex() != null) {
					try {
						outFile = new FileWriter(elevationregex);
						out = new PrintWriter(outFile);
						
						// Write text to file
						out.println("regex=" + configuration.getElevationRegex());
					} catch (IOException e){
						LOGGER.log(Level.SEVERE, "Error occurred while writing elevationregex.properties file!", e);
					} finally {
						if (out != null) {
							out.flush();
							out.close();
						}
						
						outFile = null;
						out = null;
					}
				}

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
					String[] firstCvNameParts = FilenameUtils.getBaseName(fileNames[0]).split("_");
					String[] lastCvNameParts  = FilenameUtils.getBaseName(fileNames[fileNames.length-1]).split("_");
					
					if (firstCvNameParts != null && firstCvNameParts.length > 3) {
						// 	Temp workaround to leverages on a coverageStoreId having the same name of the coverage 
						// and the same name of the mosaic folder
						String metocFields =
							firstCvNameParts.length == 9 && firstCvNameParts.length == lastCvNameParts.length?
							new StringBuilder()
							.append(firstCvNameParts[0]).append("_")
							.append(firstCvNameParts[1]).append("_")
							.append(firstCvNameParts[2]).append("_")
							.append(firstCvNameParts[3]).append("_") // Min Z
							.append(lastCvNameParts[3]).append("_") // Max Z
							.append(firstCvNameParts[5]).append("_") // Base Time
							.append(lastCvNameParts[6]).append("_") // Forecast Time
							.append(firstCvNameParts[7]).append("_") // TAU
							.append(firstCvNameParts[8]) // NoDATA
							.toString() : inputDir.getName(); 
						String coverageStoreId = inputDir.getName();
						
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
								inputDir, 
								getConfiguration().getGeoserverURL(), 
								getConfiguration().getGeoserverUID(), 
								getConfiguration().getGeoserverPWD(),
								coverageStoreId, 
								coverageStoreId,
								queryParams, 
								"", 
								"EXTERNAL",
								"imagemosaic",
								GEOSERVER_VERSION, 
								getConfiguration().getStyles(), 
								getConfiguration().getDefaultStyle()
						);
						
						if (layerResponse != null && layerResponse.length > 2) {
							String layer = layerResponse[0];
							LOGGER.info("ImageMosaicConfigurator layer: " + layer);
							
							final String workspace = layerResponse[1];
							final String coverageStore = coverageStoreId;
							final String coverageName = layer;
							queryParams.clear();
							queryParams.put("MaxAllowedTiles", Integer.toString(Integer.MAX_VALUE));
							String noData = (firstCvNameParts.length >= 9 ? firstCvNameParts[8] : firstCvNameParts.length >= 8 ? firstCvNameParts[7] : "-1.0");
							//Actually, the ImageMosaicConfiguration is contained in the flow.xml.
							//therefore, there is no way to set the background values a runtime
							//for the moment, we take the nodata from the file name.
							queryParams.put("BackgroundValues", noData);//NoData
							queryParams.put("OutputTransparentColor", "");
							queryParams.put("InputTransparentColor", "");
							queryParams.put("AllowMultithreading", "false");
							queryParams.put("USE_JAI_IMAGEREAD", "true");
							queryParams.put("SUGGESTED_TILE_SIZE", "512,512");
							
							configureMosaic(queryParams, getConfiguration().getGeoserverURL(), 
									getConfiguration().getGeoserverUID(), 
									getConfiguration().getGeoserverPWD(), workspace, coverageStore, coverageName);
							
							final File layerDescriptor = new File(inputDir, layer + ".layer");
							
							if(layerDescriptor.createNewFile()) {
								try {
									outFile = new FileWriter(layerDescriptor);
									out = new PrintWriter(outFile);
									
									// Write text to file
									out.println("namespace=" + layerResponse[1]);
									out.println("metocFields=" + metocFields);
									out.println("storeid=" + coverageStoreId);
									out.println("layerid=" + inputDir.getName());
									out.println("driver=ImageMosaic");
									out.println("path=" + File.separator);
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
	
	public static void configureMosaic(final Map<String, String> queryParams,  
			final String geoserverBaseURL, final String geoserverUID, final String geoserverPWD,
			final String workspace, final String coverageStore, final String coverageName) 
		throws ParserConfigurationException, IOException, TransformerException {
		Map<String,String> configElements = new HashMap<String, String>(2);
		if (queryParams.containsKey("MaxAllowedTiles")){
			//Configuring wmsPath
			final String maxTiles = queryParams.get("MaxAllowedTiles");
			configElements.put("MaxAllowedTiles", maxTiles);
		}
		else{
			configElements.put("MaxAllowedTiles", "2147483647");
		}
		
		if (queryParams.containsKey("BackgroundValues")){
			//Configuring wmsPath
			final String backgroundValues = queryParams.get("BackgroundValues");
			configElements.put("BackgroundValues", backgroundValues);
		}
		else{
			configElements.put("BackgroundValues", "");
		}
		
		if (queryParams.containsKey("OutputTransparentColor")){
			//Configuring wmsPath
			final String outputTransparentColor = queryParams.get("OutputTransparentColor");
			configElements.put("OutputTransparentColor", outputTransparentColor);
		}
		else{
			configElements.put("OutputTransparentColor", "");
		}
		
		if (queryParams.containsKey("InputTransparentColor")){
			//Configuring wmsPath
			final String inputTransparentColor = queryParams.get("InputTransparentColor");
			configElements.put("InputTransparentColor", inputTransparentColor);
		}
		else{
			configElements.put("InputTransparentColor", "");
		}
		
		if (queryParams.containsKey("AllowMultithreading")){
			//Configuring wmsPath
			final String allowMultithreading = queryParams.get("AllowMultithreading");
			configElements.put("AllowMultithreading", allowMultithreading);
		}
		else{
			configElements.put("AllowMultithreading", "false");
		}
		
		if (queryParams.containsKey("USE_JAI_IMAGEREAD")){
			//Configuring wmsPath
			final String useJaiImageread = queryParams.get("USE_JAI_IMAGEREAD");
			configElements.put("USE_JAI_IMAGEREAD", useJaiImageread);
		}
		else{
			configElements.put("USE_JAI_IMAGEREAD", "false");
		}
		
		if (queryParams.containsKey("SUGGESTED_TILE_SIZE")){
			//Configuring wmsPath
			final String suggestedTileSize = queryParams.get("SUGGESTED_TILE_SIZE");
			configElements.put("SUGGESTED_TILE_SIZE", suggestedTileSize);
		}
		else{
			configElements.put("SUGGESTED_TILE_SIZE", "512,512");
		}
		    
		if (!configElements.isEmpty()){
			GeoServerRESTHelper.sendCoverageConfiguration(configElements, geoserverBaseURL, geoserverUID, geoserverPWD, workspace,
					coverageStore, coverageName);	
		}
		
	}
}