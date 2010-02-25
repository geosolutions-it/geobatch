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
package it.geosolutions.geobatch.jgsflodess;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.geobatch.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.metocs.MetocConfigurationAction;
import it.geosolutions.geobatch.metocs.jaxb.model.MetocElementType;
import it.geosolutions.geobatch.metocs.jaxb.model.Metocs;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.utils.io.Utilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;
import it.geosolutions.utils.coamps.data.FlatFileGrid;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to insert NetCDF data file (gliders measurements) into DB
 * 
 */
public class JGSFLoDeSSCOAMPSFileConfigurator extends MetocConfigurationAction<FileSystemMonitorEvent> {

	protected JGSFLoDeSSCOAMPSFileConfigurator(
			MetocActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	/**
	 * 
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Starting with processing...");
		NetcdfFileWriteable ncFileOut = null;
		try {
			// looking for file
			if (events.size() != 1)
				throw new IllegalArgumentException("Wrong number of elements for this action: " + events.size());
			FileSystemMonitorEvent event = events.remove();
			final String configId = configuration.getName();
			
			final boolean packComponents = configuration.isPackComponents();

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
				LOGGER.log(Level.SEVERE, "GeoServerDataDirectory is null or does not exist.");
				throw new IllegalStateException("GeoServerDataDirectory is null or does not exist.");
			}

			// ... BUSINESS LOGIC ... //
			String inputFileName = event.getSource().getAbsolutePath();
			final String filePrefix = FilenameUtils.getBaseName(inputFileName);
			final String fileSuffix = FilenameUtils.getExtension(inputFileName);
			final String fileNameFilter = getConfiguration().getStoreFilePrefix();

			String baseFileName = null;

			if (fileNameFilter != null) {
				if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
						&& ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix))) {
					// etj: are we missing something here?
					baseFileName = filePrefix;
				}
			} else if ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix)) {
				baseFileName = filePrefix;
			}

			if (baseFileName == null) {
				LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
				throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
			}

			final File outDir = Utilities.createTodayDirectory(workingDir, FilenameUtils.getBaseName(inputFileName));
			
			inputFileName = FilenameUtils.getName(inputFileName);
			// decompress input file into a temp directory
			final File tempFile = File.createTempFile(inputFileName, ".tmp");
			final File coampsDatasetDirectory = Utilities.decompress("COAMPS", event.getSource(), tempFile);

			// ////
			// CASE 1: A FlatFileDescriptor exists
			//    - A FlatFileDescriptor is a file endings with the key word "infofld".
			//      It contains all the COMAPS file Nests/Grid infos.
			// ////
			// TODO
			
			// ////
			// CASE 2: A FlatFileDescriptor does not exist
			//    - In such case we need to search for lon/lat grid files.
			// ////
			String[] gridInfoFileNames = coampsDatasetDirectory.list(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (name.startsWith("longit") || name.startsWith("latitu"))
						return true;
					
					return false;
				}
				
			});
			
			if (gridInfoFileNames.length < 2) {
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("COAMPS grid file information could not be found!");
				
				return null;
			}

			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);

			float xmin = Float.POSITIVE_INFINITY;
            float ymin = Float.POSITIVE_INFINITY;
            float xmax = Float.NEGATIVE_INFINITY;
            float ymax = Float.NEGATIVE_INFINITY;
            
			int width = -1;
			int height = -1;
			float[] lonData = null;
			float[] latData = null;
			
			for (String gridInfoFileName : gridInfoFileNames) {
				if (gridInfoFileName.toLowerCase().startsWith("longit")) {
					final FlatFileGrid lonFileGrid = new FlatFileGrid(new File(coampsDatasetDirectory, gridInfoFileName));
					lonData = lonFileGrid.getData();

					for (float lon : lonData) {
						xmin = lon < xmin ? lon : xmin;
						xmax = lon > xmax ? lon : xmax;
					}
					
					width  = width  == -1 ? lonFileGrid.getWidth()  : width;
					height = height == -1 ? lonFileGrid.getHeight() : height;

					envelope.setRange(0, xmin, xmax);
				}
				
				if (gridInfoFileName.toLowerCase().startsWith("latitu")) {
					final FlatFileGrid latFileGrid = new FlatFileGrid(new File(coampsDatasetDirectory, gridInfoFileName));
					latData = latFileGrid.getData();

					for (float lat : latData) {
						ymin = lat < ymin ? lat : ymin;
						ymax = lat > ymax ? lat : ymax;
					}
					
					width  = width  == -1 ? latFileGrid.getWidth()  : width;
					height = height == -1 ? latFileGrid.getHeight() : height;

					envelope.setRange(1, ymin, ymax);
				}

			}
			
			File[] COAMPSFiles = coampsDatasetDirectory.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (!name.startsWith("longit") && !name.startsWith("latitu"))
						return true;
					
					return false;
				}
				
			});
			
			// ////
			// ... create the output file data structure
			// ////
            final File outputFile = new File(outDir, "JGSFLoDeSS_COAMPS-Forecast-T" + new Date().getTime() + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //Grabbing the Variables Dictionary
			JAXBContext context = JAXBContext.newInstance(Metocs.class);
			Unmarshaller um = context.createUnmarshaller();

			File metocDictionaryFile = IOUtils.findLocation(configuration.getMetocDictionaryPath(), new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory())); 
			Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(metocDictionaryFile));

		
            Map<String, String> foundVariableLongNames  = new HashMap<String, String>();
            Map<String, String> foundVariableBriefNames = new HashMap<String, String>();
            Map<String, String> foundVariableUoM 		= new HashMap<String, String>();
            MetocElementType magnitude = null;
            double noData = -9999.0;
            
            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            final List<String> varsFound = new ArrayList<String>();
            final List<String> timesFound = new ArrayList<String>();
            final List<Long> levelsFound = new ArrayList<Long>();
            
            for (File COAMPSFile : COAMPSFiles) {
				final FlatFileGrid COAMPSFileGrid = new FlatFileGrid(COAMPSFile);
				
				if (COAMPSFileGrid != null && COAMPSFileGrid.getWidth() == width && COAMPSFileGrid.getHeight() == height) {
					final String timeInstant = COAMPSFileGrid.getTimeGroup().substring(0, COAMPSFileGrid.getTimeGroup().length() - 2) + "_" + COAMPSFileGrid.getForecastTime().substring(1);
					final Long level = new Long(COAMPSFileGrid.getLevel());

					if (!varsFound.contains(COAMPSFileGrid.getParamName().replaceAll("_", ""))) {
						String varName = COAMPSFileGrid.getParamName().replaceAll("_", "");
						varsFound.add(varName);
						
						String longName = null;
            			String briefName = null;
            			String uom = null;
            			
            			for(MetocElementType m : metocDictionary.getMetoc()) {
            				if(
            					(varName.equalsIgnoreCase("lndsea") && m.getName().equals("land or sea")) ||
            					(varName.equalsIgnoreCase("ustrue") && m.getName().equals("wind stress u-component")) ||
            					(varName.equalsIgnoreCase("vstrue") && m.getName().equals("wind stress v-component"))
            				)
        					{
        						longName = m.getName();
        						briefName = m.getBrief();
        						uom = m.getDefaultUom();
        						uom = uom.indexOf(":") > 0 ? URLDecoder.decode(uom.substring(uom.lastIndexOf(":")+1), "UTF-8") : uom;
        						break;
        					}
        				}
            			
            			if (longName != null && briefName != null) {	
            				foundVariableLongNames.put(varName, longName);
            				foundVariableBriefNames.put(varName, briefName);
            				foundVariableUoM.put(varName, uom);
            			}
					}
					
					if (!timesFound.contains(timeInstant))
						timesFound.add(timeInstant);
					
					if (!levelsFound.contains(level))
						levelsFound.add(level);
				}
            }
            if (packComponents){
	            if (magnitude == null){	
	            	for(MetocElementType m : metocDictionary.getMetoc()) {
    					if (m.getName().equals("wind stress magnitude")){
    						magnitude = m;
    						break;
    					}
	            	}
	            }
            	if (magnitude != null){
	            	String longName = null;
	    			String briefName = null;
	    			String uom = null;
					longName = magnitude.getName();
					briefName = magnitude.getBrief();
					uom = magnitude.getDefaultUom();
					uom = uom.indexOf(":") > 0 ? URLDecoder.decode(uom.substring(uom.lastIndexOf(":")+1), "UTF-8") : uom;
					if (longName != null && briefName != null) {	
	    				foundVariableLongNames.put(magnitude.getName(), longName);
	    				foundVariableBriefNames.put(magnitude.getName(), briefName);
	    				foundVariableUoM.put(magnitude.getName(), uom);
	    			}
	            }
            }
            
            int t0 = Integer.parseInt(timesFound.get(0).substring(timesFound.get(0).lastIndexOf("_") + 1,timesFound.get(0).lastIndexOf("_") + 4 ));
        	int t1 = (timesFound.size() > 0 ? Integer.parseInt(timesFound.get(1).substring(timesFound.get(1).lastIndexOf("_") + 1,timesFound.get(1).lastIndexOf("_") + 4 )) : t0);

            // time Variable data
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHHmmss");
            final SimpleDateFormat fromSdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	fromSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	
        	final Date timeOriginDate = sdf.parse(timesFound.get(0));
        	int TAU = t1 - t0;
        	
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, timesFound.size(),
            		true, levelsFound.size(), JGSFLoDeSSIOUtils.UP, 
            		true, height,
            		true, width
            );
            
			// defining output variable
            for (String varName : varsFound) {
            	String variableBrief = foundVariableBriefNames.get(varName);
            	if (variableBrief != null) {
		        	ncFileOut.addVariable(variableBrief, DataType.DOUBLE, outDimensions);
		            ncFileOut.addVariableAttribute(variableBrief, "long_name", foundVariableLongNames.get(varName));
		            ncFileOut.addVariableAttribute(variableBrief, "units", foundVariableUoM.get(varName));
	            	if (varName.equalsIgnoreCase(magnitude.getName())){
	            		continue;
	            	}
            		ncFileOut.addVariableAttribute(variableBrief, "missing_value", noData);
            	}
            }
            if (packComponents){
            	final String magnitudeName = magnitude.getName();
            	String variableBrief = foundVariableBriefNames.get(magnitudeName);
            	if (variableBrief != null) {
		        	ncFileOut.addVariable(variableBrief, DataType.DOUBLE, outDimensions);
		            ncFileOut.addVariableAttribute(variableBrief, "long_name", foundVariableLongNames.get(magnitudeName));
		            ncFileOut.addVariableAttribute(variableBrief, "units", foundVariableUoM.get(magnitudeName));
            		ncFileOut.addVariableAttribute(variableBrief, "missing_value", noData);
            	}

            }
            
            // Setting up global Attributes ...
        	ncFileOut.addGlobalAttribute("base_time", fromSdf.format(timeOriginDate));
        	ncFileOut.addGlobalAttribute("tau", TAU);
        	ncFileOut.addGlobalAttribute("nodata", noData);
        	
            // writing bin data ...
            ncFileOut.create();
            
            // time Variable data
            Array time1Data = NetCDFConverterUtilities.getArray(timesFound.size(), DataType.FLOAT);
            for (int t = 0; t < timesFound.size(); t++) {
            	Date timeInstant = sdf.parse(timesFound.get(t));
            	float timeValue = (timeInstant.getTime() - JGSFLoDeSSIOUtils.startTime) / 1000.0f;
				time1Data.setFloat(time1Data.getIndex().set(t), timeValue);
            }
            ncFileOut.write(JGSFLoDeSSIOUtils.TIME_DIM, time1Data);
            
            // z level Variable data
            Array zeta1Data = NetCDFConverterUtilities.getArray(levelsFound.size(), DataType.FLOAT);
            for (int z = 0; z < levelsFound.size(); z++)
            	zeta1Data.setFloat(zeta1Data.getIndex().set(z), levelsFound.get(z));
            ncFileOut.write(JGSFLoDeSSIOUtils.HEIGHT_DIM, zeta1Data);
            
            final double resY = (envelope.getMaximum(1) - envelope.getMinimum(1)) / height;
            final double resX = (envelope.getMaximum(0) - envelope.getMinimum(0)) / width;

            // lat Variable data
			Array lat1Data = NetCDFConverterUtilities.getArray(height, DataType.FLOAT);
			for (int y = 0; y < height; y++)
				lat1Data.setFloat(lat1Data.getIndex().set(y), (float) (envelope.getMinimum(1) + y*resY));
			ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, lat1Data);

			// lon Variable data
			Array lon1Data = NetCDFConverterUtilities.getArray(width, DataType.FLOAT);
			for (int x = 0; x < width; x++)
				lon1Data.setFloat(lon1Data.getIndex().set(x), (float) (envelope.getMinimum(0) + x*resX));
			ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lon1Data);
			
			// ////
			// writing out rasters
			// ////
			SampleModel outSampleModel = RasterFactory.createBandedSampleModel(
					DataBuffer.TYPE_DOUBLE, //data type
					width, //width
					height, //height
					1); //num bands
			
			for (File COAMPSFile : COAMPSFiles) {
				final FlatFileGrid COAMPSFileGrid = new FlatFileGrid(COAMPSFile);
				
				if (COAMPSFileGrid != null && COAMPSFileGrid.getWidth() == width && COAMPSFileGrid.getHeight() == height) {
					WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

					JGSFLoDeSSIOUtils.write2DData(userRaster, COAMPSFileGrid, false, false);
					
					// Resampling to a Regular Grid ...
					if (LOGGER.isLoggable(Level.FINE))
						LOGGER.fine("Resampling to a Regular Grid ...");
					userRaster = JGSFLoDeSSIOUtils.warping(
							COAMPSFileGrid, 
							new double[] {xmin, ymin, xmax, ymax}, 
							lonData, 
							latData, 
							width, height, 
							2, userRaster, 0,
							false);
					
					final String varName = COAMPSFileGrid.getParamName().replaceAll("_", "");
					final Variable outVar = ncFileOut.findVariable(foundVariableBriefNames.get(varName));
					final Array outVarData = outVar.read();

					int tIndex = 0;
					final String timeInstant = COAMPSFileGrid.getTimeGroup().substring(0, COAMPSFileGrid.getTimeGroup().length() - 2) + "_" + COAMPSFileGrid.getForecastTime().substring(1);
            		
            		for (String timeFound : timesFound) {
            			if (timeFound.equals(timeInstant))
            				break;
            			tIndex++;
            		}
            		
            		for (int z = 0; z < levelsFound.size(); z++)
            			for (int y = 0; y < height; y++)
            				for (int x = 0; x < width; x++) {
            					float sample = userRaster.getSampleFloat(x, y, 0);
            					sample = (float) (sample == 0.0 ? noData : sample);
								outVarData.setFloat(outVarData.getIndex().set(tIndex, z, y, x), sample);
            				}
					
					ncFileOut.write(foundVariableBriefNames.get(varName), outVarData);
				}
			}
			
            if (packComponents){
            	final Variable uVar = ncFileOut.findVariable("windstress-u");
            	final Variable vVar = ncFileOut.findVariable("windstress-v");
    			Array u = null;
    			Array v = null;
            	if (uVar != null && vVar != null){
            		u = uVar.read();
            		v = vVar.read();
            	}
				if (u != null && v != null){
					final String magnitudeName = foundVariableBriefNames.get(magnitude.getName());
					final Variable magnitudeVar = ncFileOut.findVariable(magnitudeName);
					final Array magnitudeVarData = magnitudeVar.read();
					for (int t = 0; t < timesFound.size(); t++) {
						for (int z = 0; z < levelsFound.size(); z++) {
							for (int y = 0; y < height; y++){
								for (int x = 0; x < width; x++){
									Index index = magnitudeVarData.getIndex().set(t, z, y, x);	
								
									double uValue = u.getDouble(index);
									double vValue = v.getDouble(index);
									double magnitudeValue = (uValue != noData && vValue != noData) ? Math.sqrt(Math.pow(uValue,2)+Math.pow(vValue,2)) : noData; 
									magnitudeVarData.setDouble(index, magnitudeValue);
								}
							}
						}
					}
					ncFileOut.write(magnitudeName, magnitudeVarData);
					u = null;
					v = null;
				}
			}
			// ... setting up the appropriate event for the next action
			events.add(new FileSystemMonitorEvent(outputFile, FileSystemMonitorNotifications.FILE_ADDED));
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			JAI.getDefaultInstance().getTileCache().flush();
			return null;
		} finally {
			try {
				if (ncFileOut != null)
					ncFileOut.close();
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			} finally {
				JAI.getDefaultInstance().getTileCache().flush();
			}
		}
	}
	
}
