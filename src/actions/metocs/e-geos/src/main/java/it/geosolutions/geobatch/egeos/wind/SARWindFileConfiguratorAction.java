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
package it.geosolutions.geobatch.egeos.wind;

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
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to transform E-GEOS::SARWind Derived Data
 * 
 */
public class SARWindFileConfiguratorAction extends
		METOCSBaseConfiguratorAction {

	protected SARWindFileConfiguratorAction(
			MetocActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	@Override
	protected File unzipMetocArchive(FileSystemMonitorEvent event,
			String fileSuffix, File outDir, File tempFile) throws IOException {
		return ("zip".equalsIgnoreCase(fileSuffix) || "tar"
				.equalsIgnoreCase(fileSuffix)) ? Utilities.decompress(
				"E-GEOS_SARWind", event.getSource(), tempFile) : Utilities
				.createTodayPrefixedDirectory("E-GEOS_SARWind", outDir);
	}

	@Override
	protected void writeDownNetCDF(File outDir, String inputFileName)
			throws IOException, InvalidRangeException, JAXBException,
			ParseException {
		// input dimensions
		final Dimension ra_size = ncGridFile.findDimension("ra_size");

		final Dimension az_size = ncGridFile.findDimension("az_size");

		// input VARIABLES
		final Variable lonOriginalVar = ncGridFile.findVariable("longitude");
		final DataType lonDataType = lonOriginalVar.getDataType();

		final Variable latOriginalVar = ncGridFile.findVariable("latitude");
		final DataType latDataType = latOriginalVar.getDataType();

		final Variable maskOriginalVar = ncGridFile.findVariable("mask");
		@SuppressWarnings("unused")
		final DataType maskDataType = maskOriginalVar.getDataType();

		final Array lonOriginalData  = lonOriginalVar.read();
		final Array latOriginalData  = latOriginalVar.read();
		final Array maskOriginalData = maskOriginalVar.read();

		double[] bbox = METOCSActionsIOUtils.computeExtrema(latOriginalData,
				lonOriginalData, az_size, ra_size);

		// building Envelope
		final GeneralEnvelope envelope = new GeneralEnvelope(
				METOCSActionsIOUtils.WGS_84);
		envelope.setRange(0, bbox[0], bbox[2]);
		envelope.setRange(1, bbox[1], bbox[3]);

		// ////
		// ... create the output file data structure
		// ////
		outputFile = new File(outDir, "EGEOS-SARWind-T"
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
			if (!varName.equalsIgnoreCase("longitude")
					&& !varName.equalsIgnoreCase("latitude")
					&& !varName.equalsIgnoreCase("mask")) {

				if (foundVariables.get(varName) == null) {
					String longName = null;
					String briefName = null;
					String uom = null;

					for (MetocElementType m : metocDictionary.getMetoc()) {
						if ((varName.equalsIgnoreCase("wind_speed") && m
								.getName().equals("wind speed"))
								|| (varName.equalsIgnoreCase("wind_direction") && m
										.getName().equals("wind direction"))) {
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
		/** 
		 * createNetCDFCFGeodeticDimensions(
			NetcdfFileWriteable ncFileOut, 
			final boolean hasTimeDim, final int tDimLength, 
			final boolean hasZetaDim, final int zDimLength, final String zOrder, 
			final boolean hasLatDim, final int latDimLength, 
			final boolean hasLonDim, final int lonDimLength)
		 */
		final List<Dimension> outDimensions = METOCSActionsIOUtils
				.createNetCDFCFGeodeticDimensions(
						ncFileOut,
						true, 1,
						false, 0, METOCSActionsIOUtils.UP,
						true, az_size.getLength(), 
						true, ra_size.getLength());

		double noData = Double.NaN;

		// defining output variable
		for (String varName : foundVariables.keySet()) {
			// SIMONE: replaced foundVariables.get(varName).getDataType()
			// with DataType.DOUBLE
			ncFileOut.addVariable(foundVariableBriefNames.get(varName),
					foundVariables.get(varName).getDataType(), outDimensions);
			ncFileOut.addVariableAttribute(
					foundVariableBriefNames.get(varName), "long_name",
					foundVariableLongNames.get(varName));
			ncFileOut.addVariableAttribute(
					foundVariableBriefNames.get(varName), "units",
					foundVariableUoM.get(varName));
			ncFileOut.addVariableAttribute(
					foundVariableBriefNames.get(varName), 
					NetCDFUtilities.DatasetAttribs.MISSING_VALUE, 
					noData);
		}

		// MERCATOR OCEAN MODEL Global Attributes
		Attribute referenceTime = ncGridFile
				.findGlobalAttributeIgnoreCase("SOURCE_ACQUISITION_UTC_TIME");

		// e.g. 20100902211637.870628
		final SimpleDateFormat toSdf = new SimpleDateFormat(
				"yyyyMMddHHmmss");
		
		toSdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		final Date timeOriginDate = toSdf.parse(referenceTime.getStringValue()
				.trim().toLowerCase());
		long timeInMillis = timeOriginDate.getTime();
		
		long ncMillis = Long.parseLong(
				referenceTime.getStringValue().substring(referenceTime.getStringValue().indexOf(".")+1)) / 1000;
		
		timeOriginDate.setTime(timeInMillis + ncMillis);
		
		final int TAU = 0;

		// Setting up global Attributes ...
		settingNCGlobalAttributes(noData, timeOriginDate, TAU);

		// writing bin data ...
		ncFileOut.create();

		// writing time Variable data
		setTime(ncFileOut, referenceTime);

		// lat Variable data
		Array lat1Data = NetCDFConverterUtilities.getArray(az_size.getLength(),
				latDataType);
		final double resY = (bbox[3] - bbox[1]) / az_size.getLength();
		for (int lat = 0; lat < az_size.getLength(); lat++) {
			lat1Data.setDouble(lat, bbox[1] + resY*lat);
		}
		ncFileOut.write(METOCSActionsIOUtils.LAT_DIM, lat1Data);

		// lon Variable data
		Array lon1Data = NetCDFConverterUtilities.getArray(ra_size.getLength(),
				lonDataType);
		final double resX = (bbox[2] - bbox[0]) / ra_size.getLength();
		for (int lon = 0; lon < ra_size.getLength(); lon++) {
			lon1Data.setDouble(lon, bbox[0] + resX*lon);
		}
		ncFileOut.write(METOCSActionsIOUtils.LON_DIM, lon1Data);


		for (String varName : foundVariables.keySet()) {
			final Variable var = foundVariables.get(varName);

			// //
			// defining the SampleModel data type
			// //
			final SampleModel outSampleModel = Utilities
					.getSampleModel(var.getDataType(), ra_size.getLength(),
							az_size.getLength(), 1);

			Array originalVarArray = var.read();

			WritableRaster userRaster = Raster.createWritableRaster(
					outSampleModel, null);

			METOCSActionsIOUtils.write2DData(userRaster, var,
					originalVarArray, false, true, new int[] {
					az_size.getLength(), ra_size.getLength()},
					false, maskOriginalData, false);

			// Resampling to a Regular Grid ...
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER.info("Resampling to a Regular Grid ...");
			userRaster = METOCSActionsIOUtils.warping(
					bbox,
					lonOriginalData, latOriginalData, 
					ra_size.getLength(), az_size.getLength(), 
					2, userRaster, (float) noData, false);

			final Variable outVar = ncFileOut
			.findVariable(foundVariableBriefNames.get(varName));
			final Array outVarData = outVar.read();

			for (int y = 0; y < az_size.getLength(); y++)
				for (int x = 0; x < ra_size.getLength(); x++)
					outVarData.setFloat(outVarData.getIndex().set(0, y,
							x), userRaster.getSampleFloat(x, y, 0));

			ncFileOut.write(foundVariableBriefNames.get(varName),
					outVarData);
		}
	}

	/**
	 * 
	 * @param ncFileOut
	 * @param referenceTime
	 * @return
	 */
	private static void setTime(NetcdfFileWriteable ncFileOut,
			final Attribute referenceTime) {
		// e.g. 20100902211637.870628
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyyMMddHHmmss");
		
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		long millisFromStartDate = 0;
		if (referenceTime != null) {
			Date startDate = null;
			try {
				startDate = sdf.parse(referenceTime.getStringValue()
						.trim().toLowerCase());
				long timeInMillis = startDate.getTime();
				
				long ncMillis = Long.parseLong(
						referenceTime.getStringValue().substring(referenceTime.getStringValue().indexOf(".")+1)) / 1000;
				
				millisFromStartDate = (timeInMillis + ncMillis) - startTime;
			} catch (ParseException e) {
				throw new IllegalArgumentException(
						"Unable to parse time origin");
			}
		}

		// writing time variable data
		ArrayFloat timeData = new ArrayFloat(new int[] { 1 });
		Index timeIndex = timeData.getIndex();
		timeData.setFloat(timeIndex.set(0), millisFromStartDate / 1000.0f);
		try {
			ncFileOut.write(METOCSActionsIOUtils.TIME_DIM, timeData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			throw new IllegalArgumentException(
					"Unable to store time data to the output NetCDF file.");
		}
	}
}