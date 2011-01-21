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
package it.geosolutions.geobatch.lamma.build;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.imagemosaic.Utils;
import it.geosolutions.geobatch.lamma.base.LammaBaseAction;
import it.geosolutions.geobatch.tools.file.Compressor;
import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.geobatch.tools.time.TimeParser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.MultiLineString;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions
 */
public class LammaContourBuilderAction extends LammaBaseAction {

	protected final static Logger LOGGER = Logger
			.getLogger(LammaContourBuilderAction.class.toString());
	protected final LammaContourBuilderConfiguration configuration;

	/**
	 * 
	 * @param configuration
	 */
	public LammaContourBuilderAction(
			LammaContourBuilderConfiguration configuration) throws IOException {
		super(configuration);
		this.configuration = configuration;
	}

	/**
	 * 
	 * @param events
	 * @return
	 * @throws ActionException
	 */
	public Queue<FileSystemEvent> execute(
			Queue<FileSystemEvent> events) throws ActionException {
		try {
			listenerForwarder.started();

			// //
			//
			// data flow configuration and dataStore name must not be null.
			//
			// //
			if (configuration == null) {
				throw new IllegalStateException("DataFlowConfig is null.");
			}

			Queue<FileSystemEvent> outEvents = new LinkedList<FileSystemEvent>();

			// Logging to ESB ...
			logMessage.setMessage("Building contour shapefiles...");
			logMessage.setMessageTime(new Date());
			logToESB(logMessage);

			while (events.size() > 0) {
				final FileSystemEvent event = events.remove();
				final File inputFile = event.getSource();
				final File inputDir = inputFile.getParentFile();

				final File timeregex = new File(inputDir,
						"timeregex.properties");
				final File elevationregex = new File(inputDir,
						"elevationregex.properties");
				final File runtimeregex = new File(inputDir,
						"runtimeregex.properties");

				Pattern timePattern = null, elevPattern = null, runtimePattern = null;
				if (timeregex.exists()) {
					Properties timeProps = Utils
							.loadPropertiesFromURL(DataUtilities
									.fileToURL(timeregex));
					timePattern = Pattern.compile(timeProps
							.getProperty("regex"));
				}

				if (elevationregex.exists()) {
					Properties elevProps = Utils
							.loadPropertiesFromURL(DataUtilities
									.fileToURL(elevationregex));
					elevPattern = Pattern.compile(elevProps
							.getProperty("regex"));
				}

				if (runtimeregex.exists()) {
					Properties runtimeProps = Utils
							.loadPropertiesFromURL(DataUtilities
									.fileToURL(runtimeregex));
					runtimePattern = Pattern.compile(runtimeProps
							.getProperty("regex"));
				}

				final File contourShapefile = new File(inputDir, FilenameUtils
						.getBaseName(inputFile.getName())
						+ "-contour.shp");

				/**
				 * Initialize shapefile
				 */
				if (contourShapefile.exists()) {
					Path.deleteFile(contourShapefile);
					try {
					    Path.deleteFile(new File(inputDir, FilenameUtils
								.getBaseName(contourShapefile.getName())
								+ ".shx"));
					    Path.deleteFile(new File(inputDir, FilenameUtils
								.getBaseName(contourShapefile.getName())
								+ ".dbf"));
					    Path.deleteFile(new File(inputDir, FilenameUtils
								.getBaseName(contourShapefile.getName())
								+ ".prj"));
					    Path.deleteFile(new File(inputDir, FilenameUtils
								.getBaseName(contourShapefile.getName())
								+ ".zip"));
					} catch (Exception e) {
						// do nothing ...
					}
				}

				final IndexedShapefileDataStore dsW = new IndexedShapefileDataStore(
						contourShapefile.toURI().toURL(), false, true);

				try {
					final String typeSpec = "*the_geom:MultiLineString:srid=4326,elevation:Double,elev:Double,basetime:java.util.Date,runtime:Integer";
					final SimpleFeatureType schema = DataUtilities.createType(
							FilenameUtils.getBaseName(contourShapefile
									.getName()), typeSpec);
					dsW.createSchema(schema);
				} finally {
					dsW.dispose();
				}

				final File[] contourFiles = inputDir
						.listFiles(new FilenameFilter() {

							/**
							 * Accept shapefiles
							 */
							public boolean accept(File dir, String filename) {
								boolean res = !FilenameUtils.getBaseName(
										filename).equals(
										FilenameUtils
												.getBaseName(contourShapefile
														.getName()))
										&& FilenameUtils.getExtension(filename)
												.equalsIgnoreCase("shp");
								return res;
							}
						});

				if (contourFiles != null && contourFiles.length > 0) {
					for (File contourShp : contourFiles) {
						final IndexedShapefileDataStore dsR = new IndexedShapefileDataStore(
								contourShp.toURI().toURL(), false, true);

						FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;
						FeatureReader<SimpleFeatureType, SimpleFeature> fr = null;
						try {
							fw = dsW
									.getFeatureWriterAppend(Transaction.AUTO_COMMIT);
							fr = dsR.getFeatureReader();

							while (fr.hasNext()) {
								final SimpleFeature dstFeature = fw.next();
								final SimpleFeature srcFeature = fr.next();

								// get attributes and copy them over
								boolean isSetBasetime = false;
								for (int i = srcFeature.getAttributeCount() - 1; i >= 0; i--) {
									Object attribute = srcFeature
											.getAttribute(i);

									final AttributeDescriptor descriptor = dsR
											.getSchema(dsR.getTypeNames()[0])
											.getDescriptor(i);
									if (descriptor.getType().getBinding()
											.equals(MultiLineString.class)) {
										dstFeature.setAttribute("the_geom",
												attribute);
									}

									if (descriptor.getType().getBinding()
											.equals(Date.class)) {
										dstFeature.setAttribute("basetime",
												attribute);
										isSetBasetime = true;
									}

									if (descriptor.getLocalName()
											.equalsIgnoreCase("elev")) {
										dstFeature.setAttribute("elev",
												attribute);
									}
								}

								if (!isSetBasetime && timePattern != null) {
									final Matcher matcher = timePattern
											.matcher(FilenameUtils
													.getBaseName(contourShp
															.getName()));
									if (matcher.find()) {
										TimeParser timeParser = new TimeParser();
										List<Date> dates = timeParser
												.parse(matcher.group());
										if (dates != null && dates.size() > 0) {
											Calendar cal = Calendar
													.getInstance();
											cal.setTimeZone(TimeZone
													.getTimeZone("UTC"));
											cal.setTime(dates.get(0));
											cal.setTimeZone(LAMMA_TZ);

											dstFeature.setAttribute("basetime",
													cal.getTime());
										}
									}
								}

								if (elevPattern != null) {
									final Matcher matcher = elevPattern
											.matcher(FilenameUtils
													.getBaseName(contourShp
															.getName()));
									if (matcher.find()) {
										dstFeature
												.setAttribute("elevation",
														Double.valueOf(matcher
																.group()));
									}
								}

								if (runtimePattern != null) {
									final Matcher matcher = runtimePattern
											.matcher(FilenameUtils
													.getBaseName(contourShp
															.getName()));
									if (matcher.find()) {
										dstFeature.setAttribute("runtime",
												Integer
														.valueOf(matcher
																.group()));
									}
								}

								fw.write();
							}
						} finally {
							if (fw != null) {
								fw.close();
							}

							if (fr != null) {
								fr.close();
							}

							dsW.dispose();
							dsR.dispose();
						}
					}
				}

				final File[] files = inputDir.listFiles(new FilenameFilter() {

					/**
					 * Accept shapefiles
					 */
					public boolean accept(File dir, String filename) {
						boolean res = FilenameUtils.getBaseName(filename)
								.equals(
										FilenameUtils
												.getBaseName(contourShapefile
														.getName()))
								&& (FilenameUtils.getExtension(filename)
										.equalsIgnoreCase("shp")
										|| FilenameUtils.getExtension(filename)
												.equalsIgnoreCase("shx")
										|| FilenameUtils.getExtension(filename)
												.equalsIgnoreCase("dbf") || FilenameUtils
										.getExtension(filename)
										.equalsIgnoreCase("prj"));
						return res;
					}
				});
				final File compressedShapefile = Compressor.deflate(inputDir,
						FilenameUtils.getBaseName(contourShapefile.getName()),
						files);
				outEvents.add(new FileSystemEvent(compressedShapefile,
						FileSystemEventType.FILE_ADDED));
			}

			listenerForwarder.completed();

			return outEvents;
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
