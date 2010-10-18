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
package it.geosolutions.geobatch.lamma.extract;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.lamma.base.LammaBaseAction;
import it.geosolutions.geobatch.lamma.base.WgribInventory;
import it.geosolutions.geobatch.lamma.base.WgribRecordDescriptor;
import it.geosolutions.geobatch.lamma.base.WgribInventory.SUPPORTED_PARAMS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions
 */
public class LammaGribMetadataExtractorAction extends LammaBaseAction {

	protected final static Logger LOGGER = Logger
			.getLogger(LammaGribMetadataExtractorAction.class.toString());
	protected final LammaGribMetadataExtractorConfiguration configuration;

	/**
	 * 
	 * @param configuration
	 */
	public LammaGribMetadataExtractorAction(
			LammaGribMetadataExtractorConfiguration configuration)
			throws IOException {
		super(configuration);
		this.configuration = configuration;
	}

	/**
	 * 
	 * @param events
	 * @return
	 * @throws ActionException
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws ActionException {
		try {
			listenerForwarder.started();

			// looking for file
			if (events.size() != 1) {
				throw new IllegalArgumentException(
						"Wrong number of elements for this action: "
								+ events.size());
			}

			// //
			//
			// data flow configuration and dataStore name must not be null.
			//
			// //
			if (configuration == null) {
				throw new IllegalStateException("DataFlowConfig is null.");
			}

			// get the first event
			final FileSystemMonitorEvent event = events.remove();
			final File inputFile = event.getSource();

			final File srcGribFile = new File(getScriptArguments(inputFile
					.getAbsolutePath(), "srcfile"));
			final File gribInventoryFile = new File(getScriptArguments(
					inputFile.getAbsolutePath(), "destination"));

			// Logging to ESB ...
			logMessage.setMessage("Initliazing Grib inventory for file: "
					+ srcGribFile.getName());
			logMessage.setMessageTime(new Date());
			logToESB(logMessage);

			WgribInventory inventory = new WgribInventory(gribInventoryFile);

			if (inventory.isValid() && inventory.isInitialized()) {
				// Logging to ESB ...
				logMessage
						.setMessage("Grib inventory successfully initialized for file: "
								+ srcGribFile.getName());
				logMessage.setMessageTime(new Date());
				logToESB(logMessage);

				events.clear();
				final String run = extractRun(inputFile);

				for (Long rId : inventory.getRecords().keySet()) {
					WgribRecordDescriptor record = inventory.getRecords().get(
							rId);

					final String dirName = SUPPORTED_PARAMS.valueOf(
							record.getParamId()).getParamDescription()
							.replaceAll(" ", "_").replaceAll("\\.", "").trim();
					final File paramDir = createParamBaseDir(configuration
							.getBaseOutputDir(), dirName);

					final File metadataFile = extractGribMetadata(srcGribFile,
							paramDir, run, record);
					// // Logging to ESB ...
					// logMessage.setMessage("Created Metadata file: " +
					// metadataFile.getName());
					// logMessage.setMessageTime(new Date());
					// logToESB(logMessage);
					events.add(new FileSystemMonitorEvent(metadataFile,
							FileSystemMonitorNotifications.FILE_ADDED));
				}
			} else {
				// Logging to ESB ...
				logMessage.setMessage("[ERROR] "
						+ "Grib Inventory could not be initialized.");
				logMessage.setMessageTime(new Date());
				logToESB(logMessage);

				throw new IllegalStateException(
						"Grib Inventory could not be initialized.");
			}

			// Logging to ESB ...
			logMessage
					.setMessage("Successfully created parameters metadata for file: "
							+ srcGribFile.getName());
			logMessage.setMessageTime(new Date());
			logToESB(logMessage);
			listenerForwarder.completed();

			return events;
		} catch (Throwable t) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			}
			// Logging to ESB ...
			logMessage.setMessage("[ERROR] " + t.getLocalizedMessage());
			logMessage.setMessageTime(new Date());
			logToESB(logMessage);

			listenerForwarder.failed(t);
			throw new ActionException(this, t.getMessage(), t);
		}

	}

	/**
	 * 
	 * @param srcGribFile
	 * @param paramDir
	 * @param run
	 * @param record
	 * @return
	 */
	private static File extractGribMetadata(final File srcGribFile,
			File paramDir, final String run, final WgribRecordDescriptor record) {
		SimpleDateFormat runtimedf = new SimpleDateFormat("yyyyMMddHH");
		runtimedf.setTimeZone(LAMMA_TZ);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
		sdf.setTimeZone(LAMMA_TZ);

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeZone(LAMMA_TZ);
		calendar.setTime(record.getBaseTime());
		calendar.add(GregorianCalendar.HOUR, Integer.parseInt(record
				.getForecastTime()));

		StringBuilder metadataFileName = new StringBuilder("lamma").append("_")
				.append("R").append(record.getrId()).append("_").append(
						record.getParamId()).append("_")
				.append(record.getLevel())
				// Z min
				.append("_")
				.append(record.getLevel())
				// Z max
				.append("_").append(runtimedf.format(record.getBaseTime()))
				.append("_").append(sdf.format(calendar.getTime())).append("_")
				.append(run).append("_").append("9.999e20"); // NoDATA

		FileWriter outW = null;
		PrintWriter out = null;

		/**
		 * Writing down metadata info on gribMetadata File...
		 */
		final File gribMetadata = new File(paramDir, metadataFileName
				.toString()
				+ ".info");
		try {
			outW = new FileWriter(gribMetadata);
			out = new PrintWriter(outW);

			// Write text to file
			out.println("srcGribFile=" + srcGribFile.getAbsolutePath());
			out.println("recordId=" + record.getrId());
			out.println("paramId=" + record.getParamId());
			out.println("paramDescription=" + record.getParamDescription());
			out.println("run=" + run);
			out.println("baseTime=" + sdf.format(record.getBaseTime()));
			out.println("forecastTime=" + record.getRawForecastTime());
			out.println("level=" + record.getRawLevel());
		} catch (IOException e) {
			LOGGER.severe(e.getLocalizedMessage());
		} finally {
			if (out != null)
				out.close();

			if (outW != null)
				try {
					outW.close();
				} catch (IOException e) {
					LOGGER.severe(e.getLocalizedMessage());
				}
		}

		/**
		 * Building up taskExecutor XML input file for the next action...
		 */
		final File taskExecutorInput = new File(paramDir, metadataFileName
				.toString()
				+ ".xml");
		try {
			outW = new FileWriter(taskExecutorInput);
			out = new PrintWriter(outW);

			// Write text to file
			out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			out.println("<Wgrib>");
			out.println("  <d>" + record.getrId() + "</d>");
			out.println("  <text/>");
			final File wgribRecordDump = new File(paramDir, metadataFileName
					.toString()
					+ ".dat");
			out.println("  <o>" + wgribRecordDump.getAbsolutePath() + "</o>");
			out.println("  <V/>");
			out.println("  <srcfile>" + srcGribFile.getAbsolutePath()
					+ "</srcfile>");
			out.println("  <destination></destination>");
			out.println("</Wgrib>");
		} catch (IOException e) {
			LOGGER.severe(e.getLocalizedMessage());
		} finally {
			if (out != null)
				out.close();

			if (outW != null)
				try {
					outW.close();
				} catch (IOException e) {
					LOGGER.severe(e.getLocalizedMessage());
				}
		}

		return taskExecutorInput;
	}

	/**
	 * 
	 * @param baseDir
	 * @param dirName
	 * @param run
	 * @return
	 */
	private static File createParamBaseDir(final String baseDir,
			final String dirName) {
		final File paramDir = new File(baseDir, dirName);

		if (paramDir.exists() && paramDir.isFile()) {
			throw new IllegalStateException("Param Dir could not be created.");
		} else if (!paramDir.exists()) {
			if (!paramDir.mkdirs())
				throw new IllegalStateException(
						"Param Dir could not be created.");
		}

		return paramDir;
	}

	/**
	 * 
	 * @param inputFile
	 * @return
	 */
	private static String extractRun(File inputFile) {
		String fileName = FilenameUtils.getBaseName(inputFile.getName());

		if (fileName.lastIndexOf("_") > 0) {
			String run = fileName.substring(fileName.lastIndexOf("_") + 1);

			if (run.startsWith("run"))
				return run;
		}

		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(this.getClass()
				.getSimpleName());
		builder.append(" [");
		if (configuration != null) {
			builder.append("configuration=").append(configuration);
		}
		builder.append("]");
		return builder.toString();
	}
}
