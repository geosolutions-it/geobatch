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

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
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
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to insert NetCDF data file (gliders measurements) into DB
 * 
 */
public class JGSFLoDeSSNCOMFileConfigurator extends
	MetocConfigurationAction<FileSystemMonitorEvent> {

	public static final long startTime;
	public static final long NCOMstartTime;

	static {
		GregorianCalendar calendar = new GregorianCalendar(1980, 00, 01, 00, 00, 00);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		GregorianCalendar NCOMcalendar = new GregorianCalendar(2000, 00, 01, 00, 00, 00);
		NCOMcalendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		startTime = calendar.getTimeInMillis();
		NCOMstartTime = NCOMcalendar.getTimeInMillis();
	}
	protected JGSFLoDeSSNCOMFileConfigurator(
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
		NetcdfFile ncGridAreaDefinitionFile = null;
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
				LOGGER.log(Level.SEVERE,
						"GeoServerDataDirectory is null or does not exist.");
				throw new IllegalStateException(
						"GeoServerDataDirectory is null or does not exist.");
			}

			// ... BUSINESS LOGIC ... //
			String inputFileName = event.getSource().getAbsolutePath();
			final String filePrefix = FilenameUtils.getBaseName(inputFileName);
			final String fileSuffix = FilenameUtils.getExtension(inputFileName);
			final String fileNameFilter = getConfiguration().getStoreFilePrefix();

			String baseFileName = null;

			if (fileNameFilter != null) {
				if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
						&& ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix) || "nc".equalsIgnoreCase(fileSuffix))) {
					// etj: are we missing something here?
					baseFileName = filePrefix;
				}
			} else if ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix) || "nc".equalsIgnoreCase(fileSuffix)) {
				baseFileName = filePrefix;
			}

			if (baseFileName == null) {
				LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
				throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
			}

			inputFileName = FilenameUtils.getName(inputFileName);
			
			final File outDir = Utilities.createTodayDirectory(workingDir, FilenameUtils.getBaseName(inputFileName));
			
			// decompress input file into a temp directory
			final File tempFile = File.createTempFile(inputFileName, ".tmp", outDir);
			final File ncomsDatasetDirectory = 
				("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix)) ? 
						Utilities.decompress("NCOM", event.getSource(), tempFile) :
							Utilities.createTodayPrefixedDirectory("NCOM", outDir);
			
			// move the file if it's not an archive
			if (!("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix)))
				event.getSource().renameTo(new File(ncomsDatasetDirectory, inputFileName));
			
			tempFile.delete();
			
			// ////
			// STEP 1: Looking for grid area definition NetCDF file
			//    - The file contains lat/lon grids describing irregular grid.
			// ////
			File[] NCOMGridAreaFiles = ncomsDatasetDirectory.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					if (!pathname.isDirectory())
						return true;
					
					return false;
				}

			});
			
			if (NCOMGridAreaFiles.length != 1) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Could not find NCOM Grid Area definition file.");
				throw new IOException("Could not find NCOM Grid Area definition file.");
			}
			
			ncGridAreaDefinitionFile = NetcdfFile.open(NCOMGridAreaFiles[0].getAbsolutePath());

			// input dimensions
			final Dimension xIndex = ncGridAreaDefinitionFile.findDimension("X_Index");

			final Dimension yIndex = ncGridAreaDefinitionFile.findDimension("Y_Index");

			Dimension depth = null;

			// input VARIABLES
			final Variable lonOriginalVar = ncGridAreaDefinitionFile.findVariable("Longitude");
			final DataType lonDataType = lonOriginalVar.getDataType();

			final Variable latOriginalVar = ncGridAreaDefinitionFile.findVariable("Latitude");
			final DataType latDataType = latOriginalVar.getDataType();

			final Array latOriginalData = latOriginalVar.read();
			final Array lonOriginalData = lonOriginalVar.read();

			double[] bbox = JGSFLoDeSSIOUtils.computeExtrema(latOriginalData, lonOriginalData, yIndex, xIndex);
			
			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);
			envelope.setRange(0, bbox[0], bbox[2]);
			envelope.setRange(1, bbox[1], bbox[3]);


			// ////
			// STEP 2: Looking for directories containing variables
			//    - Each directory should contain separate NetCDF files for the variables.
			// ////
			File[] NCOMVarFolders = ncomsDatasetDirectory.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					if (pathname.isDirectory())
						return true;
					
					return false;
				}

			});
			
			if (NCOMVarFolders.length == 0) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Could not find NCOM Variables.");
				throw new IOException("Could not find NCOM Variables.");
			}

			// ////
			// ... create the output file data structure
			// "lscv08_MERCATOR-Forecast-T" + new Date().getTime() + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc"
			// ////
            final File outputFile = new File(outDir, "JGSFLoDeSS_NCOM-Forecast-T" + new Date().getTime() + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            
        	List<String> timesFound = new ArrayList<String>();
            
            // counting number of forecasts available
            for (File NCOMVarFolder : NCOMVarFolders) {
            	String[] filesList = NCOMVarFolder.list(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						if (FilenameUtils.getExtension(name).equalsIgnoreCase("nc") ||
								FilenameUtils.getExtension(name).equalsIgnoreCase("netcdf")) {
							return true;
						}
						
						return false;
					}
            		
            	});
            	
            	for (String fileName : filesList) {
            		String baseName = FilenameUtils.getBaseName(fileName);
            		String timeInstant = baseName.substring(baseName.length() - "yyyyMMdd00_hhh".length());
            		
            		if (!timesFound.contains(timeInstant))
            			timesFound.add(timeInstant);
            	}
            }

        	int t0 = Integer.parseInt(timesFound.get(0).substring(timesFound.get(0).lastIndexOf("_") + 1));
        	int t1 = (timesFound.size() > 0 ? Integer.parseInt(timesFound.get(1).substring(timesFound.get(1).lastIndexOf("_") + 1)) : t0);

            // time Variable data
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmm_HHH");
            final SimpleDateFormat fromSdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	fromSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	
        	final Date timeOriginDate = sdf.parse(timesFound.get(0));
        	int TAU = t1 - t0;
        	
            //Grabbing the Variables Dictionary
			JAXBContext context = JAXBContext.newInstance(Metocs.class);
			Unmarshaller um = context.createUnmarshaller();

			File metocDictionaryFile = IOUtils.findLocation(configuration.getMetocDictionaryPath(), new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory())); 
			Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(metocDictionaryFile));

		
            Map<String, Variable> foundVariables        = new HashMap<String, Variable>();
            Map<String, String> foundVariableLongNames  = new HashMap<String, String>();
            Map<String, String> foundVariableBriefNames = new HashMap<String, String>();
            Map<String, String> foundVariableUoM 		= new HashMap<String, String>();
            MetocElementType magnitude = null;
            
            for (File NCOMVarFolder : NCOMVarFolders) {
				for (File NCOMVar : NCOMVarFolder.listFiles()) {
					final String extension = FilenameUtils.getExtension(NCOMVar.getName()); 
					if (extension.equalsIgnoreCase("nc") || extension.equalsIgnoreCase("netcdf")) {
						NetcdfFile ncVarFile = null;
						try {
							ncVarFile = NetcdfFile.open(NCOMVar.getAbsolutePath());
							if (depth == null)
								depth = ncVarFile.findDimension("Depth");
							
							for (Object obj : ncVarFile.getVariables()) {
								final Variable var = (Variable) obj;
								final String varName = var.getName(); 
								if (!varName.equalsIgnoreCase("X_Index") &&
									!varName.equalsIgnoreCase("Y_Index") &&
									!varName.equalsIgnoreCase("Z-Index") &&
									!varName.equalsIgnoreCase("Depth")) {
									
									if (foundVariables.get(varName) == null){
										String longName = null;
				            			String briefName = null;
				            			String uom = null;
				            			
				            			for(MetocElementType m : metocDictionary.getMetoc()) {
				            				if(
				            					(varName.equalsIgnoreCase("U_Velocity") && m.getName().equals("water velocity u-component")) ||
				            					(varName.equalsIgnoreCase("V_Velocity") && m.getName().equals("water velocity v-component"))
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
				            				foundVariables.put(varName, var);
				            				foundVariableLongNames.put(varName, longName);
				            				foundVariableBriefNames.put(varName, briefName);
				            				foundVariableUoM.put(varName, uom);
				            			}
				            		}
								}
							}
							
						}  finally {
							if (ncVarFile != null)
								ncVarFile.close();
						}
					}
				}
            }
            if (packComponents){
	            if (magnitude == null){	
	            	for(MetocElementType m : metocDictionary.getMetoc()) {
    					if (m.getName().equals("water velocity magnitude")){
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
	    				foundVariables.put(magnitude.getName(), null);
	    				foundVariableLongNames.put(magnitude.getName(), longName);
	    				foundVariableBriefNames.put(magnitude.getName(), briefName);
	    				foundVariableUoM.put(magnitude.getName(), uom);
	    			}
	            }
            }
            // defining the file header and structure
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, timesFound.size(),
            		true, depth.getLength(), JGSFLoDeSSIOUtils.DOWN, 
            		true, yIndex.getLength(),
            		true, xIndex.getLength()
            );
            
            
            double noData = Double.NaN;
            double noDataWater = Double.NaN;
            
			// defining output variable
            for (String varName : foundVariables.keySet()) {
            	String variableBrief = foundVariableBriefNames.get(varName);
            	if (variableBrief != null) {
		        	ncFileOut.addVariable(variableBrief, DataType.DOUBLE, outDimensions);
		            ncFileOut.addVariableAttribute(variableBrief, "long_name", foundVariableLongNames.get(varName));
		            ncFileOut.addVariableAttribute(variableBrief, "units", foundVariableUoM.get(varName));
		            
		            if (Double.isNaN(noData)) {
		            	if (varName.equalsIgnoreCase(magnitude.getName())){
		            		continue;
		            	}
		            		
		            	Attribute missingValue = foundVariables.get(varName).findAttribute("missing_value");
		            	if (missingValue != null) {
		            		noData = missingValue.getNumericValue().doubleValue();
		            		ncFileOut.addVariableAttribute(variableBrief, "missing_value", noData);
		            		if (variableBrief.equalsIgnoreCase("watvel-v") || (variableBrief.equalsIgnoreCase("watvel-u"))){
		            			noDataWater = noData;
		            		}
		            	}
		            }
            	}
            }
            if (packComponents && !Double.isNaN(noDataWater)){
            	ncFileOut.addVariableAttribute(magnitude.getBrief(), "missing_value", noDataWater);
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
            
            final double resY = (bbox[3] - bbox[1]) / yIndex.getLength();
            final double resX = (bbox[2] - bbox[0]) / xIndex.getLength();
            
			// lat Variable data
			Array lat1Data = NetCDFConverterUtilities.getArray(yIndex.getLength(), latDataType);
			for (int y = 0; y < yIndex.getLength(); y++)
				lat1Data.setFloat(lat1Data.getIndex().set(y), (float) (bbox[1] + y*resY));
			ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, lat1Data);

			// lon Variable data
			Array lon1Data = NetCDFConverterUtilities.getArray(xIndex.getLength(), lonDataType);
			for (int x = 0; x < xIndex.getLength(); x++)
				lon1Data.setFloat(lon1Data.getIndex().set(x), (float) (bbox[0] + x*resX));
			ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lon1Data);
            
			List<Float> depthValuesFound = new ArrayList<Float>();
			for (File NCOMVarFolder : NCOMVarFolders) {
				for (File NCOMVar : NCOMVarFolder.listFiles()) {
					if (FilenameUtils.getExtension(NCOMVar.getName()).equalsIgnoreCase("nc") ||
							FilenameUtils.getExtension(NCOMVar.getName()).equalsIgnoreCase("netcdf")) {
						NetcdfFile ncVarFile = null;
						try {
							ncVarFile = NetcdfFile.open(NCOMVar.getAbsolutePath());
							
							// input dimensions
							final Dimension Depth = ncVarFile.findDimension("Depth");
							
							// input variables
							final Variable depthOriginalVar = ncVarFile.findVariable("Depth");
							final Array depthOriginalData = depthOriginalVar.read();
							
							for (Object obj : ncVarFile.getVariables()) {
								final Variable var = (Variable) obj;
								Number offset = 0.0;
								Number scale = 1.0;
								final Attribute offsetAtt = var.findAttribute("add_offset");
								final Attribute scaleAtt = var.findAttribute("scale_factor");
								
								if (offsetAtt != null)
									offset = offsetAtt.getNumericValue();
								if (scaleAtt != null)
									scale= scaleAtt.getNumericValue();
								
								final String varName = var.getName(); 
								if (!varName.equalsIgnoreCase("X_Index") &&
									!varName.equalsIgnoreCase("Y_Index") &&
									!varName.equalsIgnoreCase("Z-Index") &&
									!varName.equalsIgnoreCase("Depth")) {
									
									// //
									// defining the SampleModel data type
									// //
									final int dataType;
									final DataType varDataType = var.getDataType();
									if (varDataType == DataType.FLOAT)
										dataType = DataBuffer.TYPE_FLOAT;
									else if (varDataType == DataType.DOUBLE)
										dataType = DataBuffer.TYPE_DOUBLE;
									else if (varDataType == DataType.BYTE)
										dataType = DataBuffer.TYPE_BYTE;
									else if (varDataType == DataType.SHORT)
										dataType = DataBuffer.TYPE_SHORT;
									else if (varDataType == DataType.INT)
										dataType = DataBuffer.TYPE_INT;
									else
										dataType = DataBuffer.TYPE_UNDEFINED;
									
									SampleModel outSampleModel = RasterFactory.createBandedSampleModel(
											dataType, //data type
											xIndex.getLength(), //width
											yIndex.getLength(), //height
											1); //num bands

									Array originalVarArray = var.read();
									
									for (int z = 0; z < Depth.getLength(); z++) {
										
										if (!depthValuesFound.contains(depthOriginalData.getFloat(depthOriginalData.getIndex().set(z)))) {
											depthValuesFound.add(depthOriginalData.getFloat(depthOriginalData.getIndex().set(z)));
										}
										
										WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

										JGSFLoDeSSIOUtils.write2DData(userRaster, var, originalVarArray, false, false, new int[] {z, yIndex.getLength(), xIndex.getLength()}, false);

										// Resampling to a Regular Grid ...
										if (LOGGER.isLoggable(Level.FINE))
											LOGGER.fine("Resampling to a Regular Grid ...");
										userRaster = JGSFLoDeSSIOUtils.warping(
												bbox, 
												lonOriginalData, 
												latOriginalData, 
												xIndex.getLength(), yIndex.getLength(), 
												2, userRaster, (float) noData,
												false);
										
										final Variable outVar = ncFileOut.findVariable(foundVariableBriefNames.get(varName));
										final Array outVarData = outVar.read();

										int tIndex = 0;
										String baseName = FilenameUtils.getBaseName(NCOMVar.getName());
					            		String timeInstant = baseName.substring(baseName.length() - "yyyyMMdd00_hhh".length());
					            		
					            		for (String timeFound : timesFound) {
					            			if (timeFound.equals(timeInstant))
					            				break;
					            			tIndex++;
					            		}
					            		
										for (int y = 0; y < yIndex.getLength(); y++)
											for (int x = 0; x < xIndex.getLength(); x++){
												int originalValue = userRaster.getSample(x,y,0);
												outVarData.setDouble(outVarData.getIndex().set(tIndex, z, y, x), (originalValue != noData ? JGSFLoDeSSIOUtils.rescaleValue(originalValue, scale, offset) : noData));
//												outVarData.setFloat(outVarData.getIndex().set(tIndex, z, y, x), userRaster.getSampleFloat(x, y, 0));
											}
										ncFileOut.write(foundVariableBriefNames.get(varName), outVarData);
									}
								}
							}
						} finally {
							if (ncVarFile != null)
								ncVarFile.close();
						}
					}
				}
			}
			
			
			
			// z level Variable data
            Array zeta1Data = NetCDFConverterUtilities.getArray(depth.getLength(), DataType.FLOAT);
            for (int z = 0; z < depthValuesFound.size(); z++)
            	zeta1Data.setFloat(zeta1Data.getIndex().set(z), depthValuesFound.get(z));
            
            ncFileOut.write(JGSFLoDeSSIOUtils.DEPTH_DIM, zeta1Data);
            if (packComponents){
            	final Variable uVar = ncFileOut.findVariable("watvel-u");
            	final Variable vVar = ncFileOut.findVariable("watvel-v");
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
						for (int z = 0; z < depthValuesFound.size(); z++) {
							for (int y = 0; y < yIndex.getLength(); y++){
								for (int x = 0; x < xIndex.getLength(); x++){
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
				if (ncGridAreaDefinitionFile != null)
					ncGridAreaDefinitionFile.close();

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
