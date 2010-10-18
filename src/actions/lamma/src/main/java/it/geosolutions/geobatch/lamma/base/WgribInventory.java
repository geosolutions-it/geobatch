/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
package it.geosolutions.geobatch.lamma.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions S.A.S.
 * 
 */
public class WgribInventory {

	/**
	 * Default logger
	 */
	private final static Logger LOGGER = Logger.getLogger(WgribInventory.class
			.toString());

	private boolean valid = false;

	private boolean initialized = false;

	private Map<Long, WgribRecordDescriptor> records = new HashMap<Long, WgribRecordDescriptor>();

	public enum SUPPORTED_PARAMS {
		PRMSL("Pressure reduced to MSL"), VIS("Visibility"),

		HGT("Geopotential height"), TMP("Temp"), RH("Relative humidity"), DPT(
				"Dew point temp"), UGRD("u wind"), VGRD("v wind"), PRES(
				"Pressure"),

		APCP("Total precipitation"), ACPCP("Convective precipitation"), LFTX(
				"Surface lifted index"), CAPE("Convective Avail Pot Energy"), CIN(
				"Convective inhibition"), PWAT("Precipitable water"), LCDC(
				"Low level cloud cover"), MCDC("Mid level cloud cover"), HCDC(
				"High level cloud cover"), TCDC("Total cloud cover"), HLCY(
				"Storm relative helicity"), GUST("Surface wind gust"), WTMP(
				"Water temp");

		private final String paramDescription;

		SUPPORTED_PARAMS(String description) {
			this.paramDescription = description;
		}

		public String getParamDescription() {
			return this.paramDescription;
		}
	};

	/**
	 * 
	 * @param source
	 */
	public WgribInventory(final File source) {
		this.valid = checkValidSource(source);

		if (isValid()) {
			this.initialized = initializeInventory(source);
		}
	}

	/**
	 * @param initialized
	 *            the initialized to set
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param valid
	 *            the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @return the records
	 */
	public Map<Long, WgribRecordDescriptor> getRecords() {
		return records;
	}

	/**
	 * 
	 * @param source
	 * @return
	 */
	private static boolean checkValidSource(File source) {
		boolean isValid = false;

		Scanner scanner = null;
		try {
			scanner = new Scanner(source, "UTF-8");
			if (scanner.hasNextLine()) {
				final String line = scanner.nextLine();

				final String[] fields = line.split(":");

				if (fields.length == 9) {
					isValid = true;
				}

				if (isValid && fields[0].equals("1")) {
					isValid = true;
				} else {
					isValid = false;
				}
			}
		} catch (FileNotFoundException e) {
			LOGGER.warning("checkValidSource::FileNotFoundException -> "
					+ e.getLocalizedMessage());
			isValid = false;
		} finally {
			if (scanner != null)
				scanner.close();
		}

		return isValid;
	}

	/**
	 * 
	 * @param source
	 * @return
	 */
	private boolean initializeInventory(File source) {
		boolean isInitialized = false;

		Scanner scanner = null;
		try {
			scanner = new Scanner(source, "UTF-8");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();

				final String[] fields = line.split(":");

				try {
					SUPPORTED_PARAMS Param = SUPPORTED_PARAMS
							.valueOf(fields[3]);

					WgribRecordDescriptor record = new WgribRecordDescriptor();
					record.setrId(Long.parseLong(fields[0]));
					record.setParamId(Param.name());
					record.setParamDescription(fields[8].substring(1));

					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
					// sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					record.setBaseTime(sdf.parse(fields[2].substring("D="
							.length())));
					record.setForecastTime(fields[6]);

					record.setLevel(fields[4]);

					this.records.put(record.getrId(), record);
				} catch (IllegalArgumentException e) {
					// continue...
				}
			}
			isInitialized = true;
		} catch (FileNotFoundException e) {
			LOGGER.warning("initializeInventory::FileNotFoundException -> "
					+ e.getLocalizedMessage());
			isInitialized = false;
		} catch (ParseException e) {
			LOGGER.warning("initializeInventory::ParseException -> "
					+ e.getLocalizedMessage());
			isInitialized = false;
		} finally {
			if (scanner != null)
				scanner.close();
		}

		return isInitialized;
	}

}