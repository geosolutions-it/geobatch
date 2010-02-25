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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
 * Public class to transform lscv08::INGV Model
 * 
 */
public class INGVFileConfigurator extends
			MetocConfigurationAction<FileSystemMonitorEvent> {

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmm_HHH");
	
	public static final long startTime;

	static {
		GregorianCalendar calendar = new GregorianCalendar(1980, 00, 01, 00, 00, 00);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		startTime = calendar.getTimeInMillis();
	}

	private static String[] depthNames = new String[] { "depth", "deptht",
			"depthu", "depthv", "depthw" };

	public final String NAV_LON = "nav_lon";

	public final String NAV_LAT = "nav_lat";

	protected INGVFileConfigurator(
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
						Utilities.decompress("INGV-MFS", event.getSource(), tempFile) :
							Utilities.createTodayPrefixedDirectory("INGV-MFS", outDir);
			
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

            boolean hasDepth = false;
            // input dimensions
            String timeName = "time_counter";
            Dimension timeOriginalDim = ncGridFile.findDimension(timeName);
            if (timeOriginalDim == null) {
                timeOriginalDim = ncGridFile.findDimension("time");
                timeName = "time";
            }
            final Dimension yDim = ncGridFile.findDimension("y");
            final Dimension xDim = ncGridFile.findDimension("x");

            // input VARIABLES
            final Variable timeOriginalVar = ncGridFile.findVariable(timeName);
            final Array timeOriginalData = timeOriginalVar.read();
            final DataType timeDataType = timeOriginalVar.getDataType();

            final Variable navLat = ncGridFile.findVariable(NAV_LAT);
            final DataType navLatDataType = navLat.getDataType();

            final Variable navLon = ncGridFile.findVariable(NAV_LON);
            final DataType navLonDataType = navLon.getDataType();

            final int nLat = yDim.getLength();
            final int nLon = xDim.getLength();
            final int nTimes = timeOriginalDim.getLength();

            final Array latOriginalData = navLat.read("0:" + (nLat - 1) + ":1, 0:0:1").reduce();

            final Array lonOriginalData = navLon.read("0:0:1, 0:" + (nLon - 1) + ":1").reduce();

            // //
            //
            // Depth related vars
            //
            // //
            Array depthOriginalData = null;
            DataType depthDataType = null;
            int nDepths = 0;
            Array depthDestData = null;
            Dimension depthDim = null;
            String depthName = "depth";

            Variable depthOriginalVar = null;
            int dName = 0;
            while (depthOriginalVar == null) {
                if (dName == depthNames.length)
                    break;
                String name = depthNames[dName++];
                depthOriginalVar = ncGridFile.findVariable(name); // Depth
            }
            if (depthOriginalVar != null) {
                depthName = depthNames[dName - 1];
                nDepths = depthOriginalVar.getDimension(0).getLength();
                depthOriginalData = depthOriginalVar.read();
                hasDepth = true;
            }

			double[] bbox = JGSFLoDeSSIOUtils.computeExtrema(latOriginalData, lonOriginalData, yDim, xDim);

			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);
			envelope.setRange(0, bbox[0], bbox[2]);
			envelope.setRange(1, bbox[1], bbox[3]);

			// ////
			// ... create the output file data structure
			// ////
            outputFile = new File(outDir, "lscv08_INGVMFS-Forecast-T" + new Date().getTime() + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            
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
            	boolean isDepth = false;
            	for (String dptName : depthNames) {
            		if (dptName.equalsIgnoreCase(varName)) {
            			isDepth = true;
            			break;
            		}
            	}
            	
            	if (!varName.equalsIgnoreCase(NAV_LAT) &&
            		!varName.equalsIgnoreCase(NAV_LON) &&
            		!varName.contains("time") &&
            		!isDepth) {

            		if (foundVariables.get(varName) == null) {
            			String longName = null;
            			String briefName = null;
            			String uom = null;
            			
            			for(MetocElementType m : metocDictionary.getMetoc()) {
        					if (
        						(varName.equalsIgnoreCase("sohefldo") && m.getName().equals("net downward heat flux")) ||
        						(varName.equalsIgnoreCase("soshfldo") && m.getName().equals("short wave radiation")) ||
        						(varName.equalsIgnoreCase("sossheig") && m.getName().equals("sea surface height")) ||
        						(varName.equalsIgnoreCase("sowaflup") && m.getName().equals("net upward water flux")) ||
        						(varName.equalsIgnoreCase("vosaline") && m.getName().equals("salinity")) ||
        						(varName.equalsIgnoreCase("votemper") && m.getName().equals("water temperature")) ||
        						(varName.equalsIgnoreCase("vozocrtx") && m.getName().equals("water velocity u-component")) ||
        						(varName.equalsIgnoreCase("vomecrty") && m.getName().equals("water velocity v-component")) ||
        						(varName.equalsIgnoreCase("sozotaux") && m.getName().equals("wind stress u-component")) ||
        						(varName.equalsIgnoreCase("sometauy") && m.getName().equals("wind stress v-component"))
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

            // defining the file header and structure
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, nTimes,
            		hasDepth, nDepths, JGSFLoDeSSIOUtils.DOWN, 
            		true, nLat,
            		true, nLon
            );
            
            double noData = Double.NaN;
            
			// defining output variables
            for (String varName : foundVariables.keySet()) {
            	boolean hasLocalDepth = NetCDFConverterUtilities.hasThisDimension(foundVariables.get(varName), depthName);
            	List<Dimension> localOutDimensions = new ArrayList<Dimension>();
            	if (hasLocalDepth)
            		localOutDimensions = outDimensions;
            	else if (hasDepth) {
            		for (Dimension dim : outDimensions) {
            			if (!dim.getName().equals(JGSFLoDeSSIOUtils.DEPTH_DIM))
            				localOutDimensions.add(dim);
            		}
            	}
            	
            	ncFileOut.addVariable(foundVariableBriefNames.get(varName), foundVariables.get(varName).getDataType(), localOutDimensions);
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
            final SimpleDateFormat toSdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.ENGLISH);
        	final SimpleDateFormat fromSdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
        	toSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	fromSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        	final Date timeOriginDate = toSdf.parse(timeOriginalVar.findAttribute("time_origin").getStringValue().trim().toLowerCase());
        	int TAU = 0;
        	
        	Array timeData = NetCDFConverterUtilities.getArray(nTimes, timeDataType);
            for (int t=0; t<nTimes; t++) {
            	double seconds = 0;
            	if (timeDataType == DataType.FLOAT || timeDataType == DataType.DOUBLE) {
            		double originalSecs = timeOriginalData.getDouble(timeOriginalData.getIndex().set(t));
            		seconds = originalSecs + (timeOriginDate.getTime() / 1000) - (startTime / 1000);
            		if (t == 0) {
            			TAU = (int)(originalSecs / 3600);
            		}
            	} else {
            		long originalSecs = timeOriginalData.getLong(timeOriginalData.getIndex().set(t));
            		seconds = originalSecs + (timeOriginDate.getTime() / 1000) - (startTime / 1000);
            		if (t == 0) {
            			TAU = (int)(originalSecs / 3600);
            		}
            	}
            	timeData.setDouble(timeData.getIndex().set(t), seconds);
            }
        	
            // Setting up global Attributes ...
        	ncFileOut.addGlobalAttribute("base_time", fromSdf.format(timeOriginDate));
        	ncFileOut.addGlobalAttribute("tau", TAU);
        	ncFileOut.addGlobalAttribute("nodata", noData);

            // writing bin data ...
            ncFileOut.create();

            // writing time Variable data
            ncFileOut.write(JGSFLoDeSSIOUtils.TIME_DIM, timeData);

            // writing depth Variable data
            if (hasDepth) {
            	depthDataType = depthOriginalVar.getDataType();
            	depthDestData = NetCDFConverterUtilities.getArray(nDepths, depthDataType);
            	NetCDFConverterUtilities.setData1D(depthOriginalData, depthDestData, depthDataType, nDepths, false);
                ncFileOut.write(JGSFLoDeSSIOUtils.DEPTH_DIM, depthDestData);
            }

			// writing lat Variable data
            Array lat1Data = NetCDFConverterUtilities.getArray(nLat, navLatDataType);
            NetCDFConverterUtilities.setData1D(latOriginalData, lat1Data, navLatDataType, nLat, true);
			ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, lat1Data);

			// writing lon Variable data
            Array lon1Data = NetCDFConverterUtilities.getArray(nLon, navLonDataType);
            NetCDFConverterUtilities.setData1D(lonOriginalData, lon1Data, navLonDataType, nLon, false);
			ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lon1Data);
            
			// Writing data ...
			for (String varName : foundVariables.keySet()) {
				final Variable var = foundVariables.get(varName);
				final boolean hasLocalDepth = NetCDFConverterUtilities.hasThisDimension(var, depthName);
				
				Array originalVarArray = var.read();
                DataType varDataType = var.getDataType();
                Array destArray = null;
                int[] dimensions = null;
                final boolean setDepth = hasDepth && hasLocalDepth;
                if (setDepth) {
                    dimensions = new int[] { nTimes, nDepths, nLat, nLon };

                } else {
                    dimensions = new int[] { nTimes, nLat, nLon };
                }
                destArray = NetCDFConverterUtilities.getArray(dimensions, varDataType);

                final int[] loopLengths;
                if (setDepth)
                    loopLengths = new int[] { nTimes, nDepths, nLat, nLon };
                else
                    loopLengths = new int[] { nTimes, nLat, nLon };
                
                NetCDFConverterUtilities.writeData(
                		ncFileOut, foundVariableBriefNames.get(varName), var,
                        originalVarArray, destArray, false,
                        false, loopLengths, false);
                destArray = null;
                originalVarArray = null;
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