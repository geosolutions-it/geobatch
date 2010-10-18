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
package it.geosolutions.geobatch.nurc.sem.lscv08;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.metocs.base.METOCSBaseConfiguratorAction;
import it.geosolutions.geobatch.metocs.jaxb.model.MetocElementType;
import it.geosolutions.geobatch.metocs.jaxb.model.Metocs;
import it.geosolutions.geobatch.metocs.utils.io.METOCSActionsIOUtils;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to transform lscv08::INGV Model
 * 
 */
public class INGVFileConfiguratorAction extends METOCSBaseConfiguratorAction {

	private static String[] depthNames = new String[] { "depth", "deptht",
			"depthu", "depthv", "depthw" };

	public final String NAV_LON = "nav_lon";

	public final String NAV_LAT = "nav_lat";

	protected INGVFileConfiguratorAction(MetocActionConfiguration configuration)
			throws IOException {
		super(configuration);
	}

	@Override
	protected File unzipMetocArchive(FileSystemMonitorEvent event,
			String fileSuffix, File outDir, File tempFile) throws IOException {
		return ("zip".equalsIgnoreCase(fileSuffix) || "tar"
				.equalsIgnoreCase(fileSuffix)) ? Utilities.decompress(
				"INGV-MFS", event.getSource(), tempFile) : Utilities
				.createTodayPrefixedDirectory("INGV-MFS", outDir);
	}

	@Override
	protected void writeDownNetCDF(File outDir, String inputFileName)
			throws IOException, InvalidRangeException, JAXBException,
			ParseException {
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

		final Array latOriginalData = navLat.read(
				"0:" + (nLat - 1) + ":1, 0:0:1").reduce();

		final Array lonOriginalData = navLon.read(
				"0:0:1, 0:" + (nLon - 1) + ":1").reduce();

		// //
		//
		// Depth related vars
		//
		// //
		Array depthOriginalData = null;
		DataType depthDataType = null;
		int nDepths = 0;
		Array depthDestData = null;
		@SuppressWarnings("unused")
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

		double[] bbox = METOCSActionsIOUtils.computeExtrema(latOriginalData,
				lonOriginalData, yDim, xDim);

		// building Envelope
		final GeneralEnvelope envelope = new GeneralEnvelope(
				METOCSActionsIOUtils.WGS_84);
		envelope.setRange(0, bbox[0], bbox[2]);
		envelope.setRange(1, bbox[1], bbox[3]);

		// ////
		// ... create the output file data structure
		// ////
		outputFile = new File(outDir, "lscv08_INGVMFS-Forecast-T"
				+ new Date().getTime()
				+ FilenameUtils.getBaseName(inputFileName).replaceAll("-", "")
				+ ".nc");
		ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

		// NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut,
		// ncFileIn.getGlobalAttributes());

		// Grabbing the Variables Dictionary
		JAXBContext context = JAXBContext.newInstance(Metocs.class);
		Unmarshaller um = context.createUnmarshaller();

		File metocDictionaryFile = IOUtils.findLocation(configuration
				.getMetocDictionaryPath(), new File(
				((FileBaseCatalog) CatalogHolder.getCatalog())
						.getBaseDirectory()));
		Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(
				metocDictionaryFile));

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

			if (!varName.equalsIgnoreCase(NAV_LAT)
					&& !varName.equalsIgnoreCase(NAV_LON)
					&& !varName.contains("time") && !isDepth) {

				if (foundVariables.get(varName) == null) {
					String longName = null;
					String briefName = null;
					String uom = null;

					for (MetocElementType m : metocDictionary.getMetoc()) {
						if ((varName.equalsIgnoreCase("sohefldo") && m
								.getName().equals("net downward heat flux"))
								|| (varName.equalsIgnoreCase("soshfldo") && m
										.getName().equals(
												"short wave radiation"))
								|| (varName.equalsIgnoreCase("sossheig") && m
										.getName().equals("sea surface height"))
								|| (varName.equalsIgnoreCase("sowaflup") && m
										.getName().equals(
												"net upward water flux"))
								|| (varName.equalsIgnoreCase("vosaline") && m
										.getName().equals("salinity"))
								|| (varName.equalsIgnoreCase("votemper") && m
										.getName().equals("water temperature"))
								|| (varName.equalsIgnoreCase("vozocrtx") && m
										.getName().equals(
												"water velocity u-component"))
								|| (varName.equalsIgnoreCase("vomecrty") && m
										.getName().equals(
												"water velocity v-component"))
								|| (varName.equalsIgnoreCase("sozotaux") && m
										.getName().equals(
												"wind stress u-component"))
								|| (varName.equalsIgnoreCase("sometauy") && m
										.getName().equals(
												"wind stress v-component"))) {
							longName = m.getName();
							briefName = m.getBrief();
							uom = m.getDefaultUom();
							uom = uom.indexOf(":") > 0 ? URLDecoder.decode(uom
									.substring(uom.lastIndexOf(":") + 1),
									"UTF-8") : uom;
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
		final List<Dimension> outDimensions = METOCSActionsIOUtils
				.createNetCDFCFGeodeticDimensions(ncFileOut, true, nTimes,
						hasDepth, nDepths, METOCSActionsIOUtils.DOWN, true, nLat,
						true, nLon);

		double noData = Double.NaN;

		// defining output variables
		for (String varName : foundVariables.keySet()) {
			boolean hasLocalDepth = NetCDFConverterUtilities.hasThisDimension(
					foundVariables.get(varName), depthName);
			List<Dimension> localOutDimensions = new ArrayList<Dimension>();
			if (hasLocalDepth)
				localOutDimensions = outDimensions;
			else if (hasDepth) {
				for (Dimension dim : outDimensions) {
					if (!dim.getName().equals(METOCSActionsIOUtils.DEPTH_DIM))
						localOutDimensions.add(dim);
				}
			}

			ncFileOut.addVariable(foundVariableBriefNames.get(varName),
					foundVariables.get(varName).getDataType(),
					localOutDimensions);
			// NetCDFConverterUtilities.setVariableAttributes(foundVariables.get(varName),
			// ncFileOut, foundVariableBriefNames.get(varName), new String[]
			// { "positions" });
			ncFileOut.addVariableAttribute(
					foundVariableBriefNames.get(varName), "long_name",
					foundVariableLongNames.get(varName));
			ncFileOut.addVariableAttribute(
					foundVariableBriefNames.get(varName), "units",
					foundVariableUoM.get(varName));

			if (Double.isNaN(noData)) {
				Attribute missingValue = foundVariables.get(varName)
						.findAttribute("missing_value");
				if (missingValue != null) {
					noData = missingValue.getNumericValue().doubleValue();
					ncFileOut.addVariableAttribute(foundVariableBriefNames
							.get(varName), "missing_value", noData);
				}
			}
		}

		// time Variable data
		final SimpleDateFormat toSdf = new SimpleDateFormat(
				"yyyy-MMM-dd HH:mm:ss", Locale.ENGLISH);
		toSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

		final Date timeOriginDate = toSdf.parse(timeOriginalVar.findAttribute(
				"time_origin").getStringValue().trim().toLowerCase());
		int TAU = 0;

		Array timeData = NetCDFConverterUtilities
				.getArray(nTimes, timeDataType);
		for (int t = 0; t < nTimes; t++) {
			double seconds = 0;
			if (timeDataType == DataType.FLOAT
					|| timeDataType == DataType.DOUBLE) {
				double originalSecs = timeOriginalData
						.getDouble(timeOriginalData.getIndex().set(t));
				seconds = originalSecs + (timeOriginDate.getTime() / 1000)
						- (startTime / 1000);
				if (t == 0) {
					TAU = (int) (originalSecs / 3600);
				}
			} else {
				long originalSecs = timeOriginalData.getLong(timeOriginalData
						.getIndex().set(t));
				seconds = originalSecs + (timeOriginDate.getTime() / 1000)
						- (startTime / 1000);
				if (t == 0) {
					TAU = (int) (originalSecs / 3600);
				}
			}
			timeData.setDouble(timeData.getIndex().set(t), seconds);
		}

		// Setting up global Attributes ...
		settingNCGlobalAttributes(noData, timeOriginDate, TAU);

		// writing bin data ...
		ncFileOut.create();

		// writing time Variable data
		ncFileOut.write(METOCSActionsIOUtils.TIME_DIM, timeData);

		// writing depth Variable data
		if (hasDepth) {
			depthDataType = depthOriginalVar.getDataType();
			depthDestData = NetCDFConverterUtilities.getArray(nDepths,
					depthDataType);
			NetCDFConverterUtilities.setData1D(depthOriginalData,
					depthDestData, depthDataType, nDepths, false);
			ncFileOut.write(METOCSActionsIOUtils.DEPTH_DIM, depthDestData);
		}

		// writing lat Variable data
		Array lat1Data = NetCDFConverterUtilities
				.getArray(nLat, navLatDataType);
		NetCDFConverterUtilities.setData1D(latOriginalData, lat1Data,
				navLatDataType, nLat, true);
		ncFileOut.write(METOCSActionsIOUtils.LAT_DIM, lat1Data);

		// writing lon Variable data
		Array lon1Data = NetCDFConverterUtilities
				.getArray(nLon, navLonDataType);
		NetCDFConverterUtilities.setData1D(lonOriginalData, lon1Data,
				navLonDataType, nLon, false);
		ncFileOut.write(METOCSActionsIOUtils.LON_DIM, lon1Data);

		// Writing data ...
		for (String varName : foundVariables.keySet()) {
			final Variable var = foundVariables.get(varName);
			final boolean hasLocalDepth = NetCDFConverterUtilities
					.hasThisDimension(var, depthName);

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
			destArray = NetCDFConverterUtilities.getArray(dimensions,
					varDataType);

			final int[] loopLengths;
			if (setDepth)
				loopLengths = new int[] { nTimes, nDepths, nLat, nLon };
			else
				loopLengths = new int[] { nTimes, nLat, nLon };

			NetCDFConverterUtilities.writeData(ncFileOut,
					foundVariableBriefNames.get(varName), var,
					originalVarArray, destArray, false, false, loopLengths,
					false);
			destArray = null;
			originalVarArray = null;
		}
	}
}