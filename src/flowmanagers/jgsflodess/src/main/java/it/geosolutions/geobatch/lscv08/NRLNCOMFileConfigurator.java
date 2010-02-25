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
package it.geosolutions.geobatch.lscv08;

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

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.media.jai.JAI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to transform lscv08::NRL_NCOM Model
 * 
 */
public class NRLNCOMFileConfigurator extends
		MetocConfigurationAction<FileSystemMonitorEvent> {

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmm_HHH");
	
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

	
	protected NRLNCOMFileConfigurator(
			MetocActionConfiguration configuration) throws IOException {
		super(configuration);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
	}

	/**
	 * 
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Starting with processing...");
		NetcdfFile ncGridFile = null;
		NetcdfFileWriteable ncFileOut = null;
		File outputFile = null;
		try {
			// looking for file
			if (events.size() != 1)
				throw new IllegalArgumentException("Wrong number of elements for this action: " + events.size());
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
			// STEP 1: Looking for grid NetCDF files
			//    - The files are already NetCDF-CF and regular. The time has to be translated. 
			// ////
			File[] NCOMGridFiles = ncomsDatasetDirectory.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (FilenameUtils.getExtension(name).equalsIgnoreCase("nc") ||
							FilenameUtils.getExtension(name).equalsIgnoreCase("netcdf")) {
						return true;
					}
					
					return false;
				}

			});
			
			if (NCOMGridFiles.length != 1) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Could not find any NCOM Grid file.");
				throw new IOException("Could not find any NCOM Grid file.");
			}

			ncGridFile = NetcdfFile.open(NCOMGridFiles[0].getAbsolutePath());

			// input dimensions
			final Dimension lon_dim = ncGridFile.findDimension("lon");

			final Dimension lat_dim = ncGridFile.findDimension("lat");

			final Dimension depth_dim = ncGridFile.findDimension("depth");

			final Dimension time_dim = ncGridFile.findDimension("time");

			// input VARIABLES
			final Variable lonOriginalVar = ncGridFile.findVariable("lon");
			final DataType lonDataType = lonOriginalVar.getDataType();

			final Variable latOriginalVar = ncGridFile.findVariable("lat");
			final DataType latDataType = latOriginalVar.getDataType();

			boolean hasDepth = false;
			Variable depthOriginalVar = null;
			DataType depthDataType = null;
			if(ncGridFile.findVariable("depth") != null) {
				hasDepth = true;
				depthOriginalVar = ncGridFile.findVariable("depth");
				depthDataType = depthOriginalVar.getDataType();
			}

			final Variable timeOriginalVar = ncGridFile.findVariable("time");
			final DataType timeDataType = timeOriginalVar.getDataType();

			final Array lonOriginalData = lonOriginalVar.read();
			final Array latOriginalData = latOriginalVar.read();
			final Array depthOriginalData = hasDepth ? depthOriginalVar.read() : null;
			final Array timeOriginalData = timeOriginalVar.read();

			double[] bbox = JGSFLoDeSSIOUtils.computeExtrema(latOriginalData, lonOriginalData, lat_dim, lon_dim);

			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);
			envelope.setRange(0, bbox[0], bbox[2]);
			envelope.setRange(1, bbox[1], bbox[3]);

			// ////
			// ... create the output file data structure
			// ////
            outputFile = new File(outDir, "lscv08_NCOM" + (inputFileName.contains("nest") ? "nest"+inputFileName.substring(inputFileName.indexOf("nest")+"nest".length(), inputFileName.indexOf("nest")+"nest".length()+1) : "") + "-Forecast-T" + new Date().getTime() + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            
            // defining the file header and structure
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, time_dim.getLength(),
            		hasDepth, hasDepth ? depth_dim.getLength() : 0, JGSFLoDeSSIOUtils.DOWN,
            		true, lat_dim.getLength(),
            		true, lon_dim.getLength()
            );

            //Grabbing the Variables Dictionary
			JAXBContext context = JAXBContext.newInstance(Metocs.class);
			Unmarshaller um = context.createUnmarshaller();

			File metocDictionaryFile = IOUtils.findLocation(configuration.getMetocDictionaryPath(), new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory())); 
			Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(metocDictionaryFile));

		
            Map<String, Variable> foundVariables        = new HashMap<String, Variable>();
            Map<String, String> foundVariableLongNames  = new HashMap<String, String>();
            Map<String, String> foundVariableBriefNames = new HashMap<String, String>();
            Map<String, String> foundVariableUoM 		= new HashMap<String, String>();
            
            for (Object obj : ncGridFile.getVariables()) {
            	final Variable var = (Variable) obj;
            	final String varName = var.getName(); 
            	if (!varName.equalsIgnoreCase("lon") &&
            		!varName.equalsIgnoreCase("lat") &&
            		!varName.equalsIgnoreCase("depth") &&
            		!varName.equalsIgnoreCase("time")) {

            		if (foundVariables.get(varName) == null) {
            			String longName = null;
            			String briefName = null;
            			String uom = null;
            			
            			for(MetocElementType m : metocDictionary.getMetoc()) {
            				if(
            					(varName.equalsIgnoreCase("salinity") && m.getName().equals("salinity")) ||
            					(varName.equalsIgnoreCase("water_temp") && m.getName().equals("water temperature")) ||
            					(varName.equalsIgnoreCase("surf_el") && m.getName().equals("sea surface height")) ||
            					(varName.equalsIgnoreCase("water_u") && m.getName().equals("water velocity u-component")) ||
            					(varName.equalsIgnoreCase("water_v") && m.getName().equals("water velocity v-component")) ||
            					(varName.equalsIgnoreCase("water_w") && m.getName().equals("vertical water velocity"))
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

            double noData = Double.NaN;
            
			// defining output variable
            for (String varName : foundVariables.keySet()) {
            	// SIMONE: replaced foundVariables.get(varName).getDataType() with DataType.DOUBLE
            	ncFileOut.addVariable(foundVariableBriefNames.get(varName), DataType.DOUBLE, outDimensions);
                //NetCDFConverterUtilities.setVariableAttributes(foundVariables.get(varName), ncFileOut, foundVariableBriefNames.get(varName), new String[] { "positions" });
                ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "long_name", foundVariableLongNames.get(varName));
                ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "units", foundVariableUoM.get(varName));
                
                if (Double.isNaN(noData)) {
                	Attribute missingValue = foundVariables.get(varName).findAttribute("missing_value");
                	if (missingValue != null) {
                		noData = missingValue.getNumericValue().doubleValue();
                		ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "missing_value", noData);
                	}
                }
            }
            
            // time Variable data
            final SimpleDateFormat toSdf = new SimpleDateFormat("yyyyMMdd");
            final SimpleDateFormat fromSdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
        	toSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	fromSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	
        	final Date timeOriginDate = toSdf.parse(inputFileName.substring(inputFileName.lastIndexOf("_")+1));
        	int TAU = 0;
        	
        	Array time1Data = NetCDFConverterUtilities.getArray(time_dim.getLength(), DataType.DOUBLE);
            for (int t=0; t<time_dim.getLength(); t++) {
            	// hours since 2000-01-01 00:00 UTC
            	long timeValue = timeOriginalData.getLong(timeOriginalData.getIndex().set(t));
            	if (t == 0 && time_dim.getLength() > 1) {
        			TAU = (int) (timeOriginalData.getLong(timeOriginalData.getIndex().set(t+1)) - timeValue);
        		} else if (t == 0) {
        			TAU = (int) ((timeValue * 3600 * 1000) + NCOMstartTime - timeOriginDate.getTime());
        		}
            	// adding time offset
            	  timeValue = (NCOMstartTime - startTime) + (timeValue * 3600000);
            	// converting back to seconds and storing to data
				  time1Data.setLong(time1Data.getIndex().set(t), timeValue / 1000 );
            }
            
        	// Setting up global Attributes ...
        	ncFileOut.addGlobalAttribute("base_time", fromSdf.format(timeOriginDate));
        	ncFileOut.addGlobalAttribute("tau", TAU);
        	ncFileOut.addGlobalAttribute("nodata", noData);
        	
            // writing bin data ...
            ncFileOut.create();

            // writing time Variable data
            ncFileOut.write(JGSFLoDeSSIOUtils.TIME_DIM, time1Data);

            // writing depth Variable data
            if (hasDepth)
            	ncFileOut.write(JGSFLoDeSSIOUtils.DEPTH_DIM, depthOriginalData);

			// writing lat Variable data
			ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, latOriginalData);

			// writing lon Variable data
			ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lonOriginalData);
            
			for (String varName : foundVariables.keySet()) {
				Variable var = foundVariables.get(varName);
				double offset = 0.0;
				double scale = 1.0;
				
				Attribute offsetAtt = var.findAttribute("add_offset");
				Attribute scaleAtt = var.findAttribute("scale_factor");
				
				offset = (offsetAtt != null ? offsetAtt.getNumericValue().doubleValue() : offset);
				scale  = (scaleAtt != null ? scaleAtt.getNumericValue().doubleValue() : scale); 
				
				Array originalVarArray = var.read();
				Array destArray = NetCDFConverterUtilities.getArray(originalVarArray.getShape(), DataType.DOUBLE);
				
				for (int t = 0; t < time_dim.getLength(); t++)
					for (int z = 0; z < (hasDepth ? depth_dim.getLength() : 1); z++)
						for (int y = 0; y < lat_dim.getLength(); y++)
							for (int x = 0; x < lon_dim.getLength(); x++) {
								if (!hasDepth) {
									double originalValue = originalVarArray.getDouble(originalVarArray.getIndex().set(t, y, x));
									destArray.setDouble(destArray.getIndex().set(t, y, x), (originalValue != noData ? (originalValue * scale) + offset : noData));
								} else {
									double originalValue = originalVarArray.getDouble(originalVarArray.getIndex().set(t, z, y, x));
									destArray.setDouble(destArray.getIndex().set(t, z, y, x), (originalValue != noData ? (originalValue * scale) + offset : noData));
								}
							}
				
				ncFileOut.write(foundVariableBriefNames.get(varName), destArray);
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
				if (ncGridFile != null) {
					ncGridFile.close();
				}

				if (ncFileOut != null) {
					ncFileOut.close();
				}
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			} finally {
				JAI.getDefaultInstance().getTileCache().flush();
			}
		}
	}
}
