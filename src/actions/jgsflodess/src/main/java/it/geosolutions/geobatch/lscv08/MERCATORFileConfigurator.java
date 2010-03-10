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

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
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
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to transform lscv08::MERCATOR Model
 * 
 */
public class MERCATORFileConfigurator extends
			MetocConfigurationAction<FileSystemMonitorEvent> {

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmm_HHH");
	
	public static final long startTime;

	static {
		GregorianCalendar calendar = new GregorianCalendar(1980, 00, 01, 00, 00, 00);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		startTime = calendar.getTimeInMillis();
	}

	
	protected MERCATORFileConfigurator(
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
						Utilities.decompress("MERCATOR", event.getSource(), tempFile) :
							Utilities.createTodayPrefixedDirectory("MERCATOR", outDir);
			
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
			final Dimension lon_dim = ncGridFile.findDimension("longitude");

			final Dimension lat_dim = ncGridFile.findDimension("latitude");

			final Dimension depth_dim = ncGridFile.findDimension("depth");

			// input VARIABLES
			final Variable lonOriginalVar = ncGridFile.findVariable("longitude");
			final DataType lonDataType = lonOriginalVar.getDataType();

			final Variable latOriginalVar = ncGridFile.findVariable("latitude");
			final DataType latDataType = latOriginalVar.getDataType();

			final Variable depthOriginalVar = ncGridFile.findVariable("depth");
			final DataType depthDataType = depthOriginalVar.getDataType();

			final Array lonOriginalData = lonOriginalVar.read();
			final Array latOriginalData = latOriginalVar.read();
			final Array depthOriginalData = depthOriginalVar.read();

			double[] bbox = JGSFLoDeSSIOUtils.computeExtrema(latOriginalData, lonOriginalData, lat_dim, lon_dim);

			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);
			envelope.setRange(0, bbox[0], bbox[2]);
			envelope.setRange(1, bbox[1], bbox[3]);

			// ////
			// ... create the output file data structure
			// ////
            outputFile = new File(outDir, "lscv08_MERCATOR-Forecast-T" + new Date().getTime() + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            
            // defining the file header and structure
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, 1,
            		true, depth_dim.getLength(), JGSFLoDeSSIOUtils.DOWN, 
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
            	if (!varName.equalsIgnoreCase("longitude") &&
            		!varName.equalsIgnoreCase("latitude") &&
            		!varName.equalsIgnoreCase("depth")) {

            		if (foundVariables.get(varName) == null) {
            			String longName = null;
            			String briefName = null;
            			String uom = null;
            			
            			for(MetocElementType m : metocDictionary.getMetoc()) {
            				if(
            					(varName.equalsIgnoreCase("salinity") && m.getName().equals("salinity")) ||
            					(varName.equalsIgnoreCase("temperature") && m.getName().equals("water temperature")) ||
            					(varName.equalsIgnoreCase("u") && m.getName().equals("water velocity u-component")) ||
            					(varName.equalsIgnoreCase("v") && m.getName().equals("water velocity v-component"))
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
                ncFileOut.addVariable(foundVariableBriefNames.get(varName), foundVariables.get(varName).getDataType(), outDimensions);
                //NetCDFConverterUtilities.setVariableAttributes(foundVariables.get(varName), ncFileOut, foundVariableBriefNames.get(varName), new String[] { "positions" });
                ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "long_name", foundVariableLongNames.get(varName));
                ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "units", foundVariableUoM.get(varName));
                
                if (Double.isNaN(noData)) {
                	Attribute missingValue = foundVariables.get(varName).findAttribute("_FillValue");
                	if (missingValue != null) {
                		noData = missingValue.getNumericValue().doubleValue();
                		ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "missing_value", noData);
                	}
                }
            }
            
            // MERCATOR OCEAN MODEL Global Attributes
            Attribute referenceTime = ncGridFile.findGlobalAttributeIgnoreCase("bulletin_date");
            Attribute forecastDate  = ncGridFile.findGlobalAttributeIgnoreCase("forecast_range");

            final SimpleDateFormat toSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final SimpleDateFormat fromSdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
        	toSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	fromSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	
        	final Date timeOriginDate = toSdf.parse(referenceTime.getStringValue().trim().toLowerCase());
        	int TAU = 0;
        	
        	final String forecastDays = forecastDate.getStringValue();
        	if (forecastDays != null) {
                final int index = forecastDays.indexOf("-day_forecast");
                if (index != -1) {
                    int numDay = Integer.parseInt(forecastDays.substring(0, index));
                    TAU = numDay * 24;
                }
        	}
        	
        	// Setting up global Attributes ...
        	ncFileOut.addGlobalAttribute("base_time", fromSdf.format(timeOriginDate));
        	ncFileOut.addGlobalAttribute("tau", TAU);
        	ncFileOut.addGlobalAttribute("nodata", noData);

            // writing bin data ...
            ncFileOut.create();

            // writing time Variable data
            setTime(ncFileOut, referenceTime, forecastDate);

            // writing depth Variable data
            ncFileOut.write(JGSFLoDeSSIOUtils.DEPTH_DIM, depthOriginalData);

			// writing lat Variable data
			ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, latOriginalData);

			// writing lon Variable data
			ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lonOriginalData);
            
			for (String varName : foundVariables.keySet()) {
				final Variable var = foundVariables.get(varName);
				
				// //
				// defining the SampleModel data type
				// //
				final SampleModel outSampleModel = Utilities.getSampleModel(var.getDataType(), 
						lon_dim.getLength(), lat_dim.getLength(),1); 

				Array originalVarArray = var.read();
				
				for (int z = 0; z < depth_dim.getLength(); z++) {
					
					WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

					JGSFLoDeSSIOUtils.write2DData(userRaster, var, originalVarArray, false, false, new int[] {z, lat_dim.getLength(), lon_dim.getLength()}, false);

					// Resampling to a Regular Grid ...
//					if (LOGGER.isLoggable(Level.INFO))
//						LOGGER.info("Resampling to a Regular Grid ...");
//					userRaster = JGSFLoDeSSIOUtils.warping(
//							bbox, 
//							lonOriginalData, 
//							latOriginalData, 
//							lon_dim.getLength(), lat_dim.getLength(), 
//							2, userRaster, 0,
//							false);
					
					final Variable outVar = ncFileOut.findVariable(foundVariableBriefNames.get(varName));
					final Array outVarData = outVar.read();

					for (int y = 0; y < lat_dim.getLength(); y++)
						for (int x = 0; x < lon_dim.getLength(); x++)
							outVarData.setFloat(outVarData.getIndex().set(0, z, y, x), userRaster.getSampleFloat(x, y, 0));
					
					ncFileOut.write(foundVariableBriefNames.get(varName), outVarData);
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

	/**
	 * 
	 * @param ncFileOut
	 * @param referenceTime
	 * @param forecastDate
	 * @return
	 */
	private static void setTime(NetcdfFileWriteable ncFileOut, final Attribute referenceTime, final Attribute forecastDate) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //sdf.setDateFormatSymbols(new DateFormatSymbols(Locale.CANADA));
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        
        long millisFromStartDate = 0;
        if (referenceTime != null && forecastDate != null) {
            final String timeOrigin = referenceTime.getStringValue();
            final String forecastDays = forecastDate.getStringValue();
            
            Date startDate = null;
            try {
				startDate = sdf.parse(timeOrigin);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Unable to parse time origin");
			}
            
        	if (forecastDays != null) {
                final int index = forecastDays.indexOf("-day_forecast");
                if (index != -1) {
                    int numDay = Integer.parseInt(forecastDays.substring(0, index));
                    
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                    calendar.setTime(startDate);
                    calendar.add(GregorianCalendar.DATE, numDay);
                    
                    millisFromStartDate = calendar.getTimeInMillis() - startTime;
                }
            } else
                throw new IllegalArgumentException("Unable to find forecast day");
        }

        // writing time variable data
        ArrayFloat timeData = new ArrayFloat(new int[] { 1 });
        Index timeIndex = timeData.getIndex();
        timeData.setFloat(timeIndex.set(0), millisFromStartDate / 1000.0f);
        try {
			ncFileOut.write(JGSFLoDeSSIOUtils.TIME_DIM, timeData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			throw new IllegalArgumentException("Unable to store time data to the output NetCDF file.");
		}
    }
}