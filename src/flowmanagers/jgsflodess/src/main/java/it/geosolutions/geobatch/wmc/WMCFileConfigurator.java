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
package it.geosolutions.geobatch.wmc;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.wmc.model.GeneralWMCConfiguration;
import it.geosolutions.geobatch.wmc.model.OLDimension;
import it.geosolutions.geobatch.wmc.model.OLDisplayInLayerSwitcher;
import it.geosolutions.geobatch.wmc.model.OLIsBaseLayer;
import it.geosolutions.geobatch.wmc.model.OLLayerID;
import it.geosolutions.geobatch.wmc.model.OLMaxExtent;
import it.geosolutions.geobatch.wmc.model.OLSingleTile;
import it.geosolutions.geobatch.wmc.model.OLStyleClassNumber;
import it.geosolutions.geobatch.wmc.model.OLStyleColorRamps;
import it.geosolutions.geobatch.wmc.model.OLStyleMaxValue;
import it.geosolutions.geobatch.wmc.model.OLStyleMinValue;
import it.geosolutions.geobatch.wmc.model.OLStyleRestService;
import it.geosolutions.geobatch.wmc.model.OLTransparent;
import it.geosolutions.geobatch.wmc.model.ViewContext;
import it.geosolutions.geobatch.wmc.model.WMCBoundingBox;
import it.geosolutions.geobatch.wmc.model.WMCExtension;
import it.geosolutions.geobatch.wmc.model.WMCFormat;
import it.geosolutions.geobatch.wmc.model.WMCLayer;
import it.geosolutions.geobatch.wmc.model.WMCOnlineResource;
import it.geosolutions.geobatch.wmc.model.WMCServer;
import it.geosolutions.geobatch.wmc.model.WMCWindow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.opengis.coverage.grid.Format;

public class WMCFileConfigurator extends BaseAction<FileSystemMonitorEvent>
		implements Action<FileSystemMonitorEvent> {

	private final static Logger LOGGER = Logger
			.getLogger(WMCFileConfigurator.class.toString());

	private WMCActionConfiguration configuration;

	private String sessionId;

	public final static String GEOSERVER_VERSION = "2.x";
	
	public final static String INFO_EXTENSION = ".info";

	protected WMCFileConfigurator(WMCActionConfiguration configuration)
			throws IOException {
		this.configuration = configuration;
	}

	/**
	 * EXECUTE METHOD
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		try {
			// looking for file
			if (events.size() == 0)
				throw new IllegalArgumentException(
						"Wrong number of elements for this action: " + events.size());

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
				LOGGER.log(Level.SEVERE, "WorkingDirectory is null or does not exist.");
				throw new IllegalStateException("WorkingDirectory is null or does not exist.");
			}

			final List<WMCEntry> entryList = new ArrayList<WMCEntry>();
			final Map<String,String> infoFileMap = new HashMap<String,String>();
			
			LOGGER.info("WMCFileConfigurator ... fetching events...");
			while (events.size() > 0) {
				FileSystemMonitorEvent event = events.remove();

				// //
				// data flow configuration must not be null.
				// //
				if (configuration == null) {
					throw new IllegalStateException("DataFlowConfig is null.");
				}

				// ... BUSINESS LOGIC ... //
				final File inputFile = event.getSource();
				String inputFileName = inputFile.getAbsolutePath();
				final String filePrefix = FilenameUtils.getBaseName(inputFileName);
				final String fileSuffix = FilenameUtils.getExtension(inputFileName);

				String baseFileName = null;

				if ("layer".equalsIgnoreCase(fileSuffix)) {
					baseFileName = filePrefix;
				}

				if (baseFileName == null) {
					LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
					throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
				}

				if (this.sessionId == null) {
					this.sessionId = getSessionId(filePrefix);
				}
				
				Properties props = new Properties();

				//try retrieve data from file
				try {
					props.load(new FileInputStream(inputFile));
				}

				//catch exception in case properties file does not exist
				catch(IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}

				final String namespace = props.getProperty("namespace");
				final String storeid = props.getProperty("storeid");
				final String layerid = props.getProperty("layerid");
				final String metocFields = props.getProperty("metocFields");
				final String driver = props.getProperty("driver");
				final String path = new File(inputFile.getParentFile(), props.getProperty("path")).getAbsolutePath();
				final String infoFile = new StringBuilder(inputFileName.substring(0,inputFileName.length()-6)).append(INFO_EXTENSION).toString();
				
				final AbstractGridCoverage2DReader reader = ((AbstractGridFormat) acquireFormat(driver)).getReader(new File(path).toURI().toURL());
				
				WMCEntry entry = new WMCEntry(namespace, layerid);
				entry.setLayerTitle(getVariableName(metocFields));
				
				final String[] metadataNames = reader.getMetadataNames();
	            

	            String timeMetadata = null;
	            String elevationMetadata=null;
	            if (metadataNames != null && metadataNames.length > 0) {
	                // TIME DIMENSION
	                timeMetadata = reader.getMetadataValue("TIME_DOMAIN");

	                // ELEVATION DIMENSION
	                elevationMetadata = reader.getMetadataValue("ELEVATION_DOMAIN");                   
	            }
	            
	            if (timeMetadata != null) {
	                final String[] timePositions = timeMetadata.split(",");
	                Map<String, String> time = new HashMap<String, String>();
	                time.put("default", timePositions[0]);
	                time.put("values", timeMetadata);
	                entry.getDimensions().put("TIME", time);
	            }
	            
	            if (elevationMetadata != null) {
	                final String[] elevationLevels = elevationMetadata.split(",");
	                Map<String, String> elevation = new HashMap<String, String>();
	                elevation.put("default", elevationLevels[0]);
	                elevation.put("values", elevationMetadata);
	                entry.getDimensions().put("ELEVATION", elevation);
	            }
	            
				entryList.add(entry);
				infoFileMap.put(layerid,infoFile);
			}

			//
			//
			// Write down the WMC file ...
			//
			//
			final List<WMCLayer> layerList = new ArrayList<WMCLayer>();

			final String crs = configuration.getCrs();

			final String boundingBox = configuration.getBoundingBox();
			
			final int width = Integer.parseInt(configuration.getWidth());

			final int height = Integer.parseInt(configuration.getHeight());
			
			String geoserverUrl = configuration.getGeoserverURL();
				   geoserverUrl = (geoserverUrl.contains("wms") ? geoserverUrl : geoserverUrl + (geoserverUrl.endsWith("/") ? "wms" : "/wms"));

			// //
			// GENERAL CONFIG ...
			// //
			ViewContext viewContext = new ViewContext("WMC", "2Beta");
			WMCWindow window = new WMCWindow(height, width);
			GeneralWMCConfiguration generalConfig = new GeneralWMCConfiguration(window, "WMC", "WMC");
			String[] cfgbbox = boundingBox.split(",");
			WMCBoundingBox bbox = new WMCBoundingBox(crs, Double
					.valueOf(cfgbbox[0]), Double.valueOf(cfgbbox[1]), Double
					.valueOf(cfgbbox[2]), Double.valueOf(cfgbbox[3]));

			// //
			// BASE LAYER ...
			// //
			if (configuration.getBaseLayerId() != null) {
				final String baseLayerName = configuration.getBaseLayerId();
				final String baseLayerTitle = configuration.getBaseLayerTitle();
				final String baseLayerURL = configuration.getBaseLayerURL();
				final String baseLayerFormat = configuration.getBaseLayerFormat();

				WMCLayer baseLayer = new WMCLayer("0", "0", baseLayerName, baseLayerTitle, crs);
				WMCServer server = new WMCServer("wms", "1.1.1", "wms");
				List<WMCFormat> formatList = new ArrayList<WMCFormat>();
				// List<WMCStyle> styleList = new ArrayList<WMCStyle>();
				WMCExtension extension = new WMCExtension();
				extension.setId(new OLLayerID(baseLayerName));
				extension.setMaxExtent(new OLMaxExtent(null));
				extension.setIsBaseLayer(new OLIsBaseLayer("true"));
				extension.setSingleTile(new OLSingleTile("true"));
				extension.setTransparent(new OLTransparent("false"));

				formatList.add(new WMCFormat("1", baseLayerFormat));

				server.setOnlineResource(new WMCOnlineResource("simple", baseLayerURL));
				baseLayer.setServer(server);
				baseLayer.setFormatList(formatList);
				baseLayer.setExtension(extension);

				layerList.add(baseLayer);
			}
			
			// //
			//
			// Write layers pages
			//
			// //
			for (WMCEntry entry : entryList) {
				final String nameSpace = entry.getNameSpace();
				final String layerName = entry.getLayerName();
				final String layerTitle = entry.getLayerTitle();
				final String infoFile = infoFileMap.get(layerName);

				WMCLayer newLayer = new WMCLayer("0", "1", nameSpace + ":" + layerName, layerTitle, crs);
				WMCServer server = new WMCServer("wms", "1.1.1", "wms");
				List<WMCFormat> formatList = new ArrayList<WMCFormat>();
				// List<WMCStyle> styleList = new ArrayList<WMCStyle>();
				WMCExtension extension = new WMCExtension();
				extension.setId(new OLLayerID(layerName));
				extension.setMaxExtent(new OLMaxExtent(null));
				extension.setIsBaseLayer(new OLIsBaseLayer("false"));
				extension.setSingleTile(new OLSingleTile("false"));
				extension.setTransparent(new OLTransparent("true"));
				extension.setDisplayInLayerSwitcher(new OLDisplayInLayerSwitcher("false"));
				OLStyleColorRamps ramp = new OLStyleColorRamps("jet,red,blue,gray");
				ramp.setDefaultRamp("jet");
				extension.setStyleColorRamps(ramp);
				setAdditionalInfo(infoFile,extension,newLayer);
				extension.setStyleClassNumber(new OLStyleClassNumber("100"));
				extension.setStyleRestService(new OLStyleRestService(configuration.getGeoserverURL()+"/rest/sldservice/"+nameSpace+":"+layerName+"/rasterize.sld"));
				
				if (entry.getDimensions() != null) {
					for (String dim : entry.getDimensions().keySet()) {
						final String values = entry.getDimensions().get(dim).get("values");
						final String defaultValue = entry.getDimensions().get(dim).get("default");

						if ("TIME".equals(dim)){
							extension.setTime(new OLDimension(values, dim, defaultValue));
						} else if ("ELEVATION".equals(dim)) {
							extension.setElevation(new OLDimension(values, dim, defaultValue));
						}
					}
				}

				formatList.add(new WMCFormat("1", "image/png"));
				// styleList.add(new WMCStyle("1", new WMCSLD(new WMCOnlineResource("simple", "http://localhost:8081/NurcCruises/resources/xml/SLDDefault.xml"))));

				server.setOnlineResource(new WMCOnlineResource("simple", geoserverUrl));
				newLayer.setServer(server);
				newLayer.setFormatList(formatList);
				// testLayer.setStyleList(styleList);
				newLayer.setExtension(extension);

				layerList.add(newLayer);
			}

			// //
			//
			// Finalize
			//
			// //
			window.setBbox(bbox);
			viewContext.setGeneral(generalConfig);
			viewContext.setLayerList(layerList);

			final File outputDir = configuration.getOutputDirectory() != null ? new File(configuration.getOutputDirectory()) : workingDir;
			
			if (outputDir != null && outputDir.exists() && outputDir.isDirectory()) {
				FileWriter outFile = null;
				PrintWriter out = null;
				try {
					outFile = new FileWriter(new File(outputDir, "WMC_" + sessionId + ".xml"));
					out = new PrintWriter(outFile);
				
					new WMCStream().toXML(viewContext, out);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				} finally {
					if (out != null) {
						out.flush();
						out.close();
					}
					
					outFile = null;
					out = null;
				}
			}
			
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		}
	}

	private void setAdditionalInfo(final String statisticFile, final WMCExtension extension, final WMCLayer newLayer) throws IOException {
		final File file = new File(statisticFile);
		final StringBuilder mins = new StringBuilder();
		final StringBuilder maxs = new StringBuilder();
		boolean minMaxSet = false;
		if (file.exists()){
			BufferedReader reader = null; 
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				String minDef = null;
				String maxDef = null;
				boolean init = false;
				while ((line = reader.readLine()) != null){
					String entries[] = line.split(",");
					final int nEntries = entries.length;
					if (nEntries < 2){
						newLayer.setTitle(entries[0]);
					}
					else{
						String min = entries[nEntries-2];
						String max = entries[nEntries-1];
						if (!init){
							minDef = min;
							maxDef = max;
							init = true;
						}
						mins.append(min).append(",");
						maxs.append(max).append(",");
					}
				}
				
				final String minStyle = mins.toString();
				final String maxStyle = maxs.toString();
				extension.setStyleMinValue(new OLStyleMinValue(minStyle.substring(0,minStyle.length()-1), minDef));
				extension.setStyleMaxValue(new OLStyleMaxValue(maxStyle.substring(0,maxStyle.length()-1), maxDef));
				minMaxSet = true;
			} finally {
				if (reader!=null){
					try{
						reader.close();
						reader = null;
					}catch (Throwable te){
						
					}
				}
			}
		}
			
		if (!minMaxSet){
			extension.setStyleMinValue(new OLStyleMinValue("0.0", "0.0"));
			extension.setStyleMaxValue(new OLStyleMaxValue("100.0", "100.0"));
		}
		
	}

	/**
	 * 
	 * @param filePrefix
	 * @return
	 */
	private String getSessionId(String filePrefix) {
		String sessionId = "-1"; 
		
		String[] fileParts = filePrefix.split("_");
		if (fileParts != null && fileParts.length > 0) {
			for (String part : fileParts) {
				try {
					Long.parseLong(part);
					sessionId = part;
					break;
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
		
		return sessionId;
	}

	/**
	 * 
	 * @param metocs
	 * @return
	 */
	private String getVariableName(String metocs) {
		String[] fileParts = metocs.split("_");
		int p=0;
		if (fileParts != null && fileParts.length > 0) {
			for (String part : fileParts) {
				try {
					Long.parseLong(part);
					break;
				} catch (NumberFormatException e) {
					p++;
					continue;
				}
			}
		}
		
		return fileParts[p+1];
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static Format acquireFormat(String type) throws IOException {
		Format[] formats = GridFormatFinder.getFormatArray();
		Format format = null;
		final int length = formats.length;

		for (int i = 0; i < length; i++) {
			if (formats[i].getName().equals(type)) {
				format = formats[i];

				break;
			}
		}

		if (format == null) {
			throw new IOException("Cannot handle format: " + type);
		} else {
			return format;
		}
	}
}