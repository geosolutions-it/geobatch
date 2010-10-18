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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
 * Public class to transform lscv08::NRL_NCOM Model
 * 
 */
public class NRLNCOMFileConfiguratorAction extends
		METOCSBaseConfiguratorAction {

	public static final long NCOMstartTime;

	static {
		GregorianCalendar NCOMcalendar = new GregorianCalendar(2000, 00, 01,
				00, 00, 00);
		NCOMcalendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		NCOMstartTime = NCOMcalendar.getTimeInMillis();
	}

	protected NRLNCOMFileConfiguratorAction(
			MetocActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	@Override
	protected File unzipMetocArchive(FileSystemMonitorEvent event,
			String fileSuffix, File outDir, File tempFile) throws IOException {
		return ("zip".equalsIgnoreCase(fileSuffix) || "tar"
				.equalsIgnoreCase(fileSuffix)) ? Utilities.decompress("NCOM",
				event.getSource(), tempFile) : Utilities
				.createTodayPrefixedDirectory("NCOM", outDir);
	}

	@Override
	protected void writeDownNetCDF(File outDir, String inputFileName)
			throws IOException, InvalidRangeException, JAXBException,
			ParseException {
		// input dimensions
		final Dimension lon_dim = ncGridFile.findDimension("lon");

		final Dimension lat_dim = ncGridFile.findDimension("lat");

		final Dimension depth_dim = ncGridFile.findDimension("depth");

		final Dimension time_dim = ncGridFile.findDimension("time");

		// input VARIABLES
		final Variable lonOriginalVar = ncGridFile.findVariable("lon");
		@SuppressWarnings("unused")
		final DataType lonDataType = lonOriginalVar.getDataType();

		final Variable latOriginalVar = ncGridFile.findVariable("lat");
		@SuppressWarnings("unused")
		final DataType latDataType = latOriginalVar.getDataType();

		boolean hasDepth = false;
		Variable depthOriginalVar = null;
		@SuppressWarnings("unused")
		DataType depthDataType = null;
		if (ncGridFile.findVariable("depth") != null) {
			hasDepth = true;
			depthOriginalVar = ncGridFile.findVariable("depth");
			depthDataType = depthOriginalVar.getDataType();
		}

		final Variable timeOriginalVar = ncGridFile.findVariable("time");
		@SuppressWarnings("unused")
		final DataType timeDataType = timeOriginalVar.getDataType();

		final Array lonOriginalData = lonOriginalVar.read();
		final Array latOriginalData = latOriginalVar.read();
		final Array depthOriginalData = hasDepth ? depthOriginalVar.read()
				: null;
		final Array timeOriginalData = timeOriginalVar.read();

		double[] bbox = METOCSActionsIOUtils.computeExtrema(latOriginalData,
				lonOriginalData, lat_dim, lon_dim);

		// building Envelope
		final GeneralEnvelope envelope = new GeneralEnvelope(
				METOCSActionsIOUtils.WGS_84);
		envelope.setRange(0, bbox[0], bbox[2]);
		envelope.setRange(1, bbox[1], bbox[3]);

		// ////
		// ... create the output file data structure
		// ////
		outputFile = new File(outDir, "lscv08_NCOM"
				+ (inputFileName.contains("nest") ? "nest"
						+ inputFileName.substring(inputFileName.indexOf("nest")
								+ "nest".length(), inputFileName
								.indexOf("nest")
								+ "nest".length() + 1) : "") + "-Forecast-T"
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
			if (!varName.equalsIgnoreCase("lon")
					&& !varName.equalsIgnoreCase("lat")
					&& !varName.equalsIgnoreCase("depth")
					&& !varName.equalsIgnoreCase("time")) {

				if (foundVariables.get(varName) == null) {
					String longName = null;
					String briefName = null;
					String uom = null;

					for (MetocElementType m : metocDictionary.getMetoc()) {
						if ((varName.equalsIgnoreCase("salinity") && m
								.getName().equals("salinity"))
								|| (varName.equalsIgnoreCase("water_temp") && m
										.getName().equals("water temperature"))
								|| (varName.equalsIgnoreCase("surf_el") && m
										.getName().equals("sea surface height"))
								|| (varName.equalsIgnoreCase("water_u") && m
										.getName().equals(
												"water velocity u-component"))
								|| (varName.equalsIgnoreCase("water_v") && m
										.getName().equals(
												"water velocity v-component"))
								|| (varName.equalsIgnoreCase("water_w") && m
										.getName().equals(
												"vertical water velocity"))) {
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
				.createNetCDFCFGeodeticDimensions(ncFileOut, true, time_dim
						.getLength(), hasDepth, hasDepth ? depth_dim
						.getLength() : 0, METOCSActionsIOUtils.DOWN, true,
						lat_dim.getLength(), true, lon_dim.getLength());

		double noData = Double.NaN;

		// defining output variable
		for (String varName : foundVariables.keySet()) {
			// SIMONE: replaced foundVariables.get(varName).getDataType()
			// with DataType.DOUBLE
			ncFileOut.addVariable(foundVariableBriefNames.get(varName),
					DataType.DOUBLE, outDimensions);
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
		final SimpleDateFormat toSdf = new SimpleDateFormat("yyyyMMdd");
		toSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

		final Date timeOriginDate = toSdf.parse(inputFileName
				.substring(inputFileName.lastIndexOf("_") + 1));
		int TAU = 0;

		Array time1Data = NetCDFConverterUtilities.getArray(time_dim
				.getLength(), DataType.DOUBLE);
		for (int t = 0; t < time_dim.getLength(); t++) {
			// hours since 2000-01-01 00:00 UTC
			long timeValue = timeOriginalData.getLong(timeOriginalData
					.getIndex().set(t));
			if (t == 0 && time_dim.getLength() > 1) {
				TAU = (int) (timeOriginalData.getLong(timeOriginalData
						.getIndex().set(t + 1)) - timeValue);
			} else if (t == 0) {
				TAU = (int) ((timeValue * 3600 * 1000) + NCOMstartTime - timeOriginDate
						.getTime());
			}
			// adding time offset
			timeValue = (NCOMstartTime - startTime) + (timeValue * 3600000);
			// converting back to seconds and storing to data
			time1Data.setLong(time1Data.getIndex().set(t), timeValue / 1000);
		}

		// Setting up global Attributes ...
		settingNCGlobalAttributes(noData, timeOriginDate, TAU);

		// writing bin data ...
		ncFileOut.create();

		// writing time Variable data
		ncFileOut.write(METOCSActionsIOUtils.TIME_DIM, time1Data);

		// writing depth Variable data
		if (hasDepth)
			ncFileOut.write(METOCSActionsIOUtils.DEPTH_DIM, depthOriginalData);

		// writing lat Variable data
		ncFileOut.write(METOCSActionsIOUtils.LAT_DIM, latOriginalData);

		// writing lon Variable data
		ncFileOut.write(METOCSActionsIOUtils.LON_DIM, lonOriginalData);

		for (String varName : foundVariables.keySet()) {
			Variable var = foundVariables.get(varName);
			double offset = 0.0;
			double scale = 1.0;

			Attribute offsetAtt = var.findAttribute("add_offset");
			Attribute scaleAtt = var.findAttribute("scale_factor");

			offset = (offsetAtt != null ? offsetAtt.getNumericValue()
					.doubleValue() : offset);
			scale = (scaleAtt != null ? scaleAtt.getNumericValue()
					.doubleValue() : scale);

			Array originalVarArray = var.read();
			Array destArray = NetCDFConverterUtilities.getArray(
					originalVarArray.getShape(), DataType.DOUBLE);

			for (int t = 0; t < time_dim.getLength(); t++)
				for (int z = 0; z < (hasDepth ? depth_dim.getLength() : 1); z++)
					for (int y = 0; y < lat_dim.getLength(); y++)
						for (int x = 0; x < lon_dim.getLength(); x++) {
							if (!hasDepth) {
								double originalValue = originalVarArray
										.getDouble(originalVarArray.getIndex()
												.set(t, y, x));
								destArray
										.setDouble(
												destArray.getIndex().set(t, y,
														x),
												(originalValue != noData ? (originalValue * scale)
														+ offset
														: noData));
							} else {
								double originalValue = originalVarArray
										.getDouble(originalVarArray.getIndex()
												.set(t, z, y, x));
								destArray
										.setDouble(
												destArray.getIndex().set(t, z,
														y, x),
												(originalValue != noData ? (originalValue * scale)
														+ offset
														: noData));
							}
						}

			ncFileOut.write(foundVariableBriefNames.get(varName), destArray);
		}
	}
}