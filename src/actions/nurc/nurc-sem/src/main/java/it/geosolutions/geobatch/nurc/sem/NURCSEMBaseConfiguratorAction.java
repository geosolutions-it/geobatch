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
package it.geosolutions.geobatch.nurc.sem;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.nurc.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.nurc.metocs.MetocConfigurationAction;
import it.geosolutions.geobatch.nurc.utils.io.Utilities;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.media.jai.JAI;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * Comments here ...
 *
 * @author Alessio Fabiani, GeoSolutions S.A.S.
 *
 */
public abstract class NURCSEMBaseConfiguratorAction extends MetocConfigurationAction {

	public NURCSEMBaseConfiguratorAction(MetocActionConfiguration configuration) {
		super(configuration);
	}

	protected NetcdfFile ncGridFile = null;
	protected NetcdfFileWriteable ncFileOut = null;
	protected File outputFile = null;

	protected Map<String, Variable> foundVariables = new HashMap<String, Variable>();
	protected Map<String, String> foundVariableLongNames = new HashMap<String, String>();
	protected Map<String, String> foundVariableBriefNames = new HashMap<String, String>();
	protected Map<String, String> foundVariableUoM = new HashMap<String, String>();

	/**
	 * 
	 */
	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events) throws ActionException {

		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Starting with processing...");
		try {
			// looking for file
			if (events.size() != 1)
				throw new IllegalArgumentException("Wrong number of elements for this action: " + events.size());
			FileSystemMonitorEvent event = events.remove();
			@SuppressWarnings("unused")
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
			final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(), new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

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
				if ((filePrefix.equals(fileNameFilter) || filePrefix
						.matches(fileNameFilter))
						&& ("zip".equalsIgnoreCase(fileSuffix)
								|| "tar".equalsIgnoreCase(fileSuffix) || "nc"
								.equalsIgnoreCase(fileSuffix))) {
					// etj: are we missing something here?
					baseFileName = filePrefix;
				}
			} else if ("zip".equalsIgnoreCase(fileSuffix)
					|| "tar".equalsIgnoreCase(fileSuffix)
					|| "nc".equalsIgnoreCase(fileSuffix)) {
				baseFileName = filePrefix;
			}

			if (baseFileName == null) {
				LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName
						+ "'");
				throw new IllegalStateException("Unexpected file '"
						+ inputFileName + "'");
			}

			inputFileName = FilenameUtils.getName(inputFileName);

			final File outDir = Utilities.createTodayDirectory(workingDir,
					FilenameUtils.getBaseName(inputFileName));

			// decompress input file into a temp directory
			final File tempFile = File.createTempFile(inputFileName, ".tmp", outDir);
			final File metocsDatasetDirectory = unzipMetocArchive(event, fileSuffix, outDir, tempFile);

			// move the file if it's not an archive
			if (!("zip".equalsIgnoreCase(fileSuffix) || "tar"
					.equalsIgnoreCase(fileSuffix)))
				event.getSource().renameTo(
						new File(metocsDatasetDirectory, inputFileName));

			tempFile.delete();

			// ////
			// STEP 1: Looking for grid NetCDF files
			// - The files are already NetCDF-CF and regular. The time has to be
			// translated.
			// ////
			File[] metocsGridFiles = metocsDatasetDirectory
					.listFiles(new FilenameFilter() {

						public boolean accept(File dir, String name) {
							if (FilenameUtils.getExtension(name)
									.equalsIgnoreCase("nc")
									|| FilenameUtils.getExtension(name)
											.equalsIgnoreCase("netcdf")) {
								return true;
							}

							return false;
						}

					});

			if (metocsGridFiles.length != 1) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Could not find any NCOM Grid file.");
				throw new IOException("Could not find any NCOM Grid file.");
			}

			ncGridFile = NetcdfFile.open(metocsGridFiles[0].getAbsolutePath());

			writeDownNetCDF(outDir, inputFileName);
			
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
	 * @param noData
	 * @param fromSdf
	 * @param timeOriginDate
	 * @param TAU
	 */
	protected void settingNCGlobalAttributes(double noData, final Date timeOriginDate, int TAU) {
		final SimpleDateFormat fromSdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
		fromSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		
		ncFileOut.addGlobalAttribute("base_time", fromSdf.format(timeOriginDate));
		ncFileOut.addGlobalAttribute("tau", TAU);
		ncFileOut.addGlobalAttribute("nodata", noData);
	}
	
	/**
	 * @throws IOException 
	 * @throws InvalidRangeException 
	 * @throws JAXBException 
	 * @throws ParseException 
	 * 
	 */
	protected abstract void writeDownNetCDF(File outDir, String inputFileName) throws IOException, InvalidRangeException, JAXBException, ParseException;

	/**
	 * @param event
	 * @param fileSuffix
	 * @param outDir
	 * @param tempFile
	 * @return
	 * @throws IOException
	 */
	protected abstract File unzipMetocArchive(FileSystemMonitorEvent event, final String fileSuffix, final File outDir, final File tempFile) throws IOException;	
}
