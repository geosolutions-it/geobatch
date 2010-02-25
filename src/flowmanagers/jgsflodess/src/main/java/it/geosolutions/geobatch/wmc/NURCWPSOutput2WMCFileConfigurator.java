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
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.utils.io.Utilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to split NetCDF_CF Geodetic to GeoTIFFs and consequently
 * send them to GeoServer along with their basic metadata.
 * 
 * For the NetCDF_CF Geodetic file we assume that it contains georectified
 * geodetic grids and therefore has a maximum set of dimensions as follows:
 * 
 * lat {
 *  lat:long_name = "Latitude"
 *  lat:units = "degrees_north"
 * }
 * 
 * lon {
 *  lon:long_name = "Longitude"
 *  lon:units = "degrees_east"
 * }
 * 
 * time {
 *  time:long_name = "time"
 *  time:units = "seconds since 1980-1-1 0:0:0"
 * }
 * 
 * depth {
 *  depth:long_name = "depth";
 *  depth:units = "m";
 *  depth:positive = "down";
 * }
 * 
 * height {
 *  height:long_name = "height";
 *  height:units = "m";
 *  height:positive = "up";
 * }
 * 
 */
public class NURCWPSOutput2WMCFileConfigurator extends
	GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	/**
	 * GeoTIFF Writer Default Params
	 */
	public final static String GEOSERVER_VERSION = "2.x";
	
	private static final int DEFAULT_TILE_SIZE = 256;

	private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

	private static final String DEFAULT_COMPRESSION_TYPE = "LZW";

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
	private final SimpleDateFormat wpssdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS'Z'");

	public static final long matLabStartTime;
	
	static {
		GregorianCalendar calendar = new GregorianCalendar(0000, 00, 01, 00, 00, 00);
		calendar.setTimeZone(JGSFLoDeSSIOUtils.UTC);
		matLabStartTime = calendar.getTimeInMillis();
	}
	
	protected NURCWPSOutput2WMCFileConfigurator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
		sdf.setTimeZone(JGSFLoDeSSIOUtils.UTC);
		wpssdf.setTimeZone(JGSFLoDeSSIOUtils.UTC);
	}

	/**
	 * EXECUTE METHOD 
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Starting with processing...");
		NetcdfFile ncFileIn = null;
		try {
			// looking for file
			if (events.size() != 1)
				throw new IllegalArgumentException("Wrong number of elements for this action: " + events.size());
			FileSystemMonitorEvent event = events.remove();

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
				LOGGER.log(Level.SEVERE, "WorkingDirectory is null or does not exist.");
				throw new IllegalStateException("WorkingDirectory is null or does not exist.");
			}

			// ... BUSINESS LOGIC ... //
			String inputFileName = event.getSource().getAbsolutePath();
			final String filePrefix = FilenameUtils.getBaseName(inputFileName);
			final String fileSuffix = FilenameUtils.getExtension(inputFileName);
			final String fileNameFilter = getConfiguration().getStoreFilePrefix();

			String baseFileName = null;
			boolean packData = false;
			boolean isTDA = false;

			if (fileNameFilter != null) {
				if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
						&& ("nc".equalsIgnoreCase(fileSuffix) || "netcdf".equalsIgnoreCase(fileSuffix))) {
					baseFileName = filePrefix;
				}
			} else if ("nc".equalsIgnoreCase(fileSuffix) || "netcdf".equalsIgnoreCase(fileSuffix)) {
				baseFileName = filePrefix;
			} else if ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix)) {
				baseFileName = filePrefix;
				packData = true;
			}

			if (baseFileName == null) {
				LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
				throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
			}
			
			String inputBaseName = FilenameUtils.getBaseName(inputFileName); 
			File outDir = null;
			if (packData){
				outDir = Utilities.createTodayDirectory(workingDir, inputBaseName+"packed",true);
			}
			else
				outDir = Utilities.createTodayDirectory(workingDir, inputBaseName,true);
			File outputFile = null;
			
			// //
			//
			// Pack all nectdf files contained within the zip archive to a single netcdf file
			//
			// //
			if (packData){
				// 	decompress input file into a temp directory
				final File tempFile = File.createTempFile(inputBaseName, ".tmp");
				if (inputFileName.contains("TDA"))
					isTDA = true;
				final File wpsDatasetDirectory = Utilities.decompress("WPSOUT", event.getSource(), tempFile);
				
				final File[] wpsFiles = wpsDatasetDirectory.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						if (FilenameUtils.getExtension(name).equalsIgnoreCase("nc") ||
								FilenameUtils.getExtension(name).equalsIgnoreCase("netcdf")) {
							return true;
						}
						return false;
					}});
				
				outputFile = new File(outDir, inputBaseName + ".nc");
	            final NetcdfFileWriteable ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());
	            
	            final int times = wpsFiles.length;
	            boolean initialized = false;
	            List<String> variablesName = new ArrayList<String>();
	            Map<Long,File> timesMap = new TreeMap<Long, File>();
	            
	            //Gathering available times
	            for (File wpsFile : wpsFiles) {
	            	final String wpsFileName = wpsFile.getAbsolutePath();
	            	final String name = FilenameUtils.getBaseName(wpsFileName);
	            	final String time = name.substring(11,name.indexOf("_",12));
	            	final Date timeDate = wpssdf.parse(time);
	            	final Long timeInstant = timeDate.getTime();
	            	timesMap.put(timeInstant, wpsFile);
	            }
	            
	            int timeIndex = 0;
	            int nLat=0;
				int nLon=0;
	            for (Long timeInstant : timesMap.keySet()) {
	            	final File wpsFile = timesMap.get(timeInstant);
					final String extension = FilenameUtils.getExtension(wpsFile.getName());
					final String wpsFileName = wpsFile.getAbsolutePath();
					if (extension.equalsIgnoreCase("nc") || extension.equalsIgnoreCase("netcdf")) {
						NetcdfFile ncVarFile = null;
						try {
							ncVarFile = NetcdfFile.open(wpsFileName);
							if (!initialized){
								//Initialize the output netcdf file
								initialized = true;
								final Dimension lat = ncVarFile.findDimension("lat");
								final Dimension lon = ncVarFile.findDimension("lon");
								nLat = lat.getLength();
								nLon = lon.getLength();
								
								//Initialize dimensions
								final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
					            		ncFileOut, true, times, false, 0, "", true, nLat, true, nLon, DataType.INT);
								for (Object obj : ncVarFile.getVariables()) {
									final Variable var = (Variable) obj;
									final String varName = var.getName(); 
									if (!varName.equalsIgnoreCase("lat") &&
										!varName.equalsIgnoreCase("lon")) {
										variablesName.add(varName);
										final String shortName = varName.replace(" ", "").toLowerCase(); 
										ncFileOut.addVariable(shortName, DataType.DOUBLE, outDimensions);
										ncFileOut.addVariableAttribute(shortName, "long_name", varName);
									    ncFileOut.addVariableAttribute(shortName, "units", "dimensionless");
					            		ncFileOut.addVariableAttribute(shortName, "missing_value", -9999.0);
									}								
								
								}
								final Variable lonOriginalVar = ncVarFile.findVariable(NetCDFUtilities.LON);
								final Variable latOriginalVar = ncVarFile.findVariable(NetCDFUtilities.LAT);

								final Array latOriginalData = latOriginalVar.read();
								final Array lonOriginalData = lonOriginalVar.read();
								
								Array timeData = new ArrayLong(new int[]{times});
								int i=0;
					            for (Long timeI : timesMap.keySet()) {
					            	long timeValue = (timeI - JGSFLoDeSSIOUtils.startTime) / 1000;
									timeData.setLong(timeData.getIndex().set(i++), timeValue);
					            }
					            ncFileOut.create();
					            
								ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, latOriginalData);
								ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lonOriginalData);
								ncFileOut.write(JGSFLoDeSSIOUtils.TIME_DIM, timeData);
							}
							
							//Write variables to the output file
							for (Object obj : ncVarFile.getVariables()) {
								final Variable var = (Variable) obj;
								final String varName = var.getName(); 
								if (!varName.equalsIgnoreCase("lat") &&
									!varName.equalsIgnoreCase("lon")) {
									final String shortName = varName.replace(" ", "").toLowerCase(); 
									final Variable outVar = ncFileOut.findVariable(shortName);
									final Array outData = outVar.read();
									final Array inData = var.read();
									for (int y = 0; y < nLat; y++){
										for (int x = 0; x < nLon; x++){
											Index index = outData.getIndex().set(timeIndex, y, x);	
											Index inIndex = inData.getIndex().set(y,x);
											outData.setDouble(index, inData.getDouble(inIndex));
										}
									}
									ncFileOut.write(shortName, outData);
								}								
							}
							
							timeIndex++;
						}  finally {
							if (ncVarFile != null)
								ncVarFile.close();
						}
					}
	            }
	            ncFileOut.close();
			}
			if (outputFile != null){
				inputFileName = outputFile.getAbsolutePath();
				inputBaseName = FilenameUtils.getBaseName(inputFileName);
				outDir = Utilities.createTodayDirectory(workingDir, inputBaseName ,true);
			}
			ncFileIn = NetcdfFile.open(inputFileName);
			
			// input DIMENSIONS
			final Dimension timeDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.TIME_DIM);
			final boolean timeDimExists = timeDim != null;
			
			final Dimension depthDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.DEPTH_DIM);
			final boolean depthDimExists = depthDim != null;

			final Dimension heightDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.HEIGHT_DIM);
			final boolean heightDimExists = heightDim != null;

			Dimension latDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LAT_DIM);
			if (latDim == null)
				latDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LAT_DIM_LONG);
			final boolean latDimExists = latDim != null;

			Dimension lonDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LON_DIM);
			if (lonDim == null)
				lonDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LON_DIM_LONG);

			final boolean lonDimExists = lonDim != null;

			// dimensions' checks
			final boolean hasZeta = depthDimExists || heightDimExists;
			
			if (!latDimExists || !lonDimExists) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Invalid input NetCDF-CF Geodetic file: longitude and/or latitude dimensions could not be found!");
				throw new IllegalStateException("Invalid input NetCDF-CF Geodetic file: longitude and/or latitude dimensions could not be found!");
			}

			int nTime = timeDimExists ? timeDim.getLength() : 0;
			int nZeta = 0;
			int nLat  = latDim.getLength();
			int nLon  = lonDim.getLength();

			// input VARIABLES
			final Variable timeOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.TIME_DIM);
			final Variable yearOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.TIME_YEAR);
			final Variable monthOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.TIME_MONTH);
			final Variable dayOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.TIME_DAY);
			final Variable hourOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.TIME_HOUR);
			Array yearOriginalData = null;
			Array monthOriginalData = null;
			Array dayOriginalData = null;
			Array hourOriginalData = null;
			
			GregorianCalendar timeCalendar = new GregorianCalendar();
			timeCalendar.setTimeZone(JGSFLoDeSSIOUtils.UTC);

			final Array timeOriginalData;
			final Index timeOriginalIndex; 
			final boolean hasTime; 
			if (timeOriginalVar != null){
				timeOriginalData = timeOriginalVar.read();
				timeOriginalIndex = timeOriginalData.getIndex();
				hasTime = true;
			}
			else{
				timeOriginalData = null;
				timeOriginalIndex = null;
				hasTime = false;
			}
			if (yearOriginalVar != null){
				yearOriginalData = yearOriginalVar.read();
			}
			if (monthOriginalVar != null){
				monthOriginalData = monthOriginalVar.read();
			}
			if (dayOriginalVar != null){
				dayOriginalData = dayOriginalVar.read();
			}
			if (hourOriginalVar != null){
				hourOriginalData = hourOriginalVar.read();
			}

			String baseTime = null;
			if (!hasTime){
				Date dateTime = new Date(System.currentTimeMillis());
				baseTime = sdf.format(dateTime);
			}
			else{
				if (isTDA){
					baseTime = sdf.format(timeOriginalData.getLong(timeOriginalIndex.set(0))*1000 + JGSFLoDeSSIOUtils.startTime);
				}
				else{
					timeCalendar.set(yearOriginalData.getInt(0), monthOriginalData.getInt(0)-1, dayOriginalData.getInt(0), hourOriginalData.getInt(0), 0, 0);
					baseTime = sdf.format(timeCalendar.getTime().getTime());
//					baseTime = sdf.format(matLabStartTime + timeOriginalData.getFloat(timeOriginalIndex.set(0))*86400000L);
				}
					
			}

			Variable lonOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LON_DIM);
			if (lonOriginalVar == null)
				lonOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LON_DIM_LONG);

			Variable latOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LAT_DIM);
			if (latOriginalVar == null)
				latOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LAT_DIM_LONG);

			final Array latOriginalData = latOriginalVar.read();
			final Array lonOriginalData = lonOriginalVar.read();

			// //
			//
			// Depth related variables
			//
			// //
			Variable zetaOriginalVar = null;
			Array zetaOriginalData = null;
			
			if (hasZeta) {
				zetaOriginalVar = ncFileIn.findVariable(depthDimExists ? JGSFLoDeSSIOUtils.DEPTH_DIM : JGSFLoDeSSIOUtils.HEIGHT_DIM);
				if (zetaOriginalVar != null) {
					nZeta = depthDimExists ? depthDim.getLength() : heightDim.getLength();
					zetaOriginalData = zetaOriginalVar.read();
				}
			}

			double[] bbox = JGSFLoDeSSIOUtils.computeExtrema(latOriginalData, lonOriginalData, latDim, lonDim);
			
			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);
			envelope.setRange(0, bbox[0], bbox[2]);
			envelope.setRange(1, bbox[1], bbox[3]);
			
			// Storing variables Variables as GeoTIFFs
			final List<Variable> foundVariables = ncFileIn.getVariables();
			final ArrayList<String> variables = new ArrayList<String>();
			int numVars = 0;

			for (Variable var : foundVariables) {
				if (var != null) {
					String varName = var.getName();
					if (var.getRank()==1 || varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LAT_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LON_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LAT_DIM_LONG)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LON_DIM_LONG)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.TIME_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.HEIGHT_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.DEPTH_DIM))
						continue;
					variables.add(varName);
					
					boolean canProceed = false;
					
					final File gtiffOutputDir = new File(outDir.getAbsolutePath() + File.separator + inputBaseName + "_" + varName.replaceAll("_", ""));
					
					if (!gtiffOutputDir.exists())
						canProceed = gtiffOutputDir.mkdirs();
					
					canProceed = gtiffOutputDir.isDirectory();
					
					if (canProceed) {
						// //
						// defining the SampleModel data type
						// //
						final SampleModel outSampleModel = Utilities.getSampleModel(var.getDataType(), nLon, nLat,1);

						Array originalVarArray = var.read();
						final boolean hasLocalZLevel = NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.DEPTH_DIM)
								|| NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.HEIGHT_DIM);
						final boolean hasLocalTime = NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.TIME_DIM) && hasTime;
						
						double noData = Double.NaN;
						Attribute missingValue = var.findAttribute("missing_value");
                        if (missingValue != null) {
                                noData = missingValue.getNumericValue().doubleValue();
                        }

                        final String variableName = varName.replace("_", "").replace(" ", "");
                        final ArrayList<String> ranges = new ArrayList<String>((hasLocalTime? nTime:1) * (hasLocalZLevel ? nZeta:1));
                        
                        for (int t = 0; t < (hasLocalTime ? nTime : 1); t++) {
                        	for (int z = 0; z < (hasLocalZLevel ? nZeta : 1); z++) {
								WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

								int[] dimArray;
								if (hasLocalZLevel && hasLocalTime)
									dimArray = new int[] {t, z, nLat, nLon} ;
								else if (hasLocalZLevel)
									dimArray = new int[] {z, nLat, nLon} ;
								else if (hasLocalTime)
									dimArray = new int[] {t, nLat, nLon} ;
								else
									dimArray = new int[] {nLat, nLon} ;
								
								//Writing data and looking for min max
								final Array minMaxArray = JGSFLoDeSSIOUtils.write2DData(userRaster, var, originalVarArray, true, true, dimArray, true);
								
								// ////
								// producing the Coverage here...
								// ////
								String refZeta = hasLocalZLevel ? elevLevelFormat(zetaOriginalData.getDouble(zetaOriginalData.getIndex().set(z))) : "0000.000";
								final StringBuilder coverageName = new StringBuilder(inputBaseName)
								              .append("_").append(variableName).append("_").append(refZeta).append("_").append(refZeta).append("_");
								String refTime = null;
								if (!hasTime){
									coverageName.append(baseTime);
									refTime = baseTime;
								}
								else{
									if (isTDA){
										// Seconds since 01-01-1980
										refTime = timeDimExists ? sdf.format(JGSFLoDeSSIOUtils.startTime + timeOriginalData.getLong(timeOriginalIndex.set(t))*1000) : "00000000T000000Z";
										coverageName.append(refTime);
									}
									else {
										coverageName.append(baseTime).append("_");
										// Days since 01-01-0000 (Matlab time)	
										if (timeDimExists)
											timeCalendar.set(yearOriginalData.getInt(t), monthOriginalData.getInt(t)-1, dayOriginalData.getInt(t), hourOriginalData.getInt(t), 0, 0);
										String ftime = timeDimExists ? sdf.format(timeCalendar.getTime().getTime()):"00000000T000000Z";
										coverageName.append(ftime);
										refTime = ftime;
									}
								}
								ranges.add(new StringBuilder(refTime).append(",").append(refZeta).append(",").
										append(minMaxArray.getDouble(0)).append(",").append(minMaxArray.getDouble(1)).append("\n").toString());
								coverageName.append("-T").append(System.currentTimeMillis());
								final String nd = Double.isNaN(noData)?"-9999.0":Double.toString(noData);
								coverageName.append("_").append(nd);

								File gtiffFile = Utilities.storeCoverageAsGeoTIFF(gtiffOutputDir, coverageName.toString(), variableName, userRaster, noData, envelope, DEFAULT_COMPRESSION_TYPE, DEFAULT_COMPRESSION_RATIO, DEFAULT_TILE_SIZE);
							}
						}
                        
                        //Writing statistics file
                        File outInfoFile = null;
                        try{
                        	outInfoFile = new File(gtiffOutputDir, new StringBuilder(inputBaseName)
					              .append("_").append(variableName).toString() + WMCFileConfigurator.INFO_EXTENSION);
                        	BufferedWriter writer = new BufferedWriter(new FileWriter(outInfoFile));
                        	for (String rangeEntry : ranges)
                        		writer.write(rangeEntry);
                        	if(!isTDA){
                        		writer.write(var.getDescription());
                        	}
                        	writer.flush();
                        	writer.close();
                        } finally{
                        	try{
                        		if (outInfoFile != null)
                        			outInfoFile = null;
                        	}catch (Throwable t){
                        		//eat me
                        	}
                        }
						
						
						// ... setting up the appropriate event for the next action
						events.add(new FileSystemMonitorEvent(gtiffOutputDir, FileSystemMonitorNotifications.FILE_ADDED));
					}

					numVars++;
				}
			}
			
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			JAI.getDefaultInstance().getTileCache().flush();
			return null;
		} finally {
			try {
				if (ncFileIn != null)
					ncFileIn.close();
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
	 * @param d
	 * @return
	 */
	private static String elevLevelFormat(double d) {
		String[] parts = String.valueOf(d).split("\\.");
		
		String integerPart = parts[0];
		String decimalPart = parts[1];
		
		while (integerPart.length() % 4 != 0)
			integerPart = "0" + integerPart;
		
		decimalPart = decimalPart.length() > 3 ? decimalPart.substring(0, 3) : decimalPart;
		
		while (decimalPart.length() % 3 != 0)
			decimalPart = decimalPart + "0";
		
		return integerPart + "." + decimalPart;
	}
}