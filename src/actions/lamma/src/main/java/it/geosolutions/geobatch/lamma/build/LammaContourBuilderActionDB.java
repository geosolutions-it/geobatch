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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.imagemosaic.Utils;
import it.geosolutions.geobatch.lamma.base.LammaBaseAction;
import it.geosolutions.geobatch.utils.TimeParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.MultiLineString;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions S.A.S.
 * 
 */
public class LammaContourBuilderActionDB extends LammaBaseAction {

	protected final static Logger LOGGER = Logger
			.getLogger(LammaContourBuilderActionDB.class.toString());
	protected final LammaContourBuilderConfiguration configuration;

	/**
	 * 
	 * @param configuration
	 */
	public LammaContourBuilderActionDB(
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
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws ActionException {
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

			Queue<FileSystemMonitorEvent> outEvents = new LinkedList<FileSystemMonitorEvent>();

			// Logging to ESB ...
			logMessage.setMessage("Building contour shapefiles...");
			logMessage.setMessageTime(new Date());
			logToESB(logMessage);

			while (events.size() > 0) {
				final FileSystemMonitorEvent event = events.remove();
				final File inputFile = event.getSource();
				final File inputDir = inputFile.getParentFile();

				// ////
				//
				// CHECK FOR REGEX PROPERTIES FILES
				//
				// ////
				final File timeregex = new File(inputDir,
						"timeregex.properties");
				final File elevationregex = new File(inputDir,
						"elevationregex.properties");
				final File runtimeregex = new File(inputDir,
						"runtimeregex.properties");

				final File datastore = new File(inputDir,
						"datastore.properties");

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

				final String contourLayerName = FilenameUtils
						.getBaseName(inputFile.getName())
						+ "_contour";

				/**
				 * CASE 0: check if layer already exists...
				 */
				boolean exists = checkIfContourLayerExists(contourLayerName);

				updateContourLayer(inputDir, timePattern, elevPattern,
						runtimePattern, contourLayerName, datastore, exists);
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

	/**
	 * @param inputDir
	 * @param timePattern
	 * @param elevPattern
	 * @param runtimePattern
	 * @param contourLayerName
	 * @param dsW
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws NoSuchElementException
	 * @throws IndexOutOfBoundsException
	 * @throws ParseException
	 * @throws NumberFormatException
	 * @throws SchemaException
	 */
	private ReferencedEnvelope writeContourFeatureTypes(final File inputDir,
			Pattern timePattern, Pattern elevPattern, Pattern runtimePattern,
			final String contourLayerName, DataStore dsW)
			throws MalformedURLException, IOException,
			IllegalArgumentException, NoSuchElementException,
			IndexOutOfBoundsException, ParseException, NumberFormatException,
			SchemaException {
		ReferencedEnvelope bounds = null;

		final File[] contourFiles = inputDir.listFiles(new FilenameFilter() {

			/**
			 * Accept shapefiles
			 */
			public boolean accept(File dir, String filename) {
				boolean res = !FilenameUtils.getBaseName(filename).equals(
						contourLayerName)
						&& FilenameUtils.getExtension(filename)
								.equalsIgnoreCase("shp");
				return res;
			}
		});

		if (contourFiles != null && contourFiles.length > 0) {
			final List<SimpleFeature> features = new ArrayList<SimpleFeature>();

			final String typeSpec = "*the_geom:MultiLineString:srid=4326,elevation:Double,elev:Double,basetime:java.util.Date,runtime:Integer";
			final SimpleFeatureType schema = DataUtilities.createType(
					contourLayerName, typeSpec);

			for (File contourShp : contourFiles) {
				final IndexedShapefileDataStore dsR = new IndexedShapefileDataStore(
						contourShp.toURI().toURL(), false, true);

				FeatureReader<SimpleFeatureType, SimpleFeature> fr = null;

				final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
				final Lock lock = rwLock.readLock();
				try {
					lock.lock();
					fr = dsR.getFeatureReader();

					if (bounds == null)
						bounds = dsR.getFeatureSource(dsR.getTypeNames()[0])
								.getBounds();

					while (fr.hasNext()) {
						final SimpleFeature dstFeature = DataUtilities
								.template(schema);
						final SimpleFeature srcFeature = fr.next();

						// get attributes and copy them over
						boolean isSetBasetime = false;
						for (int i = srcFeature.getAttributeCount() - 1; i >= 0; i--) {
							Object attribute = srcFeature.getAttribute(i);

							final AttributeDescriptor descriptor = dsR
									.getSchema(dsR.getTypeNames()[0])
									.getDescriptor(i);
							if (descriptor.getType().getBinding().equals(
									MultiLineString.class)) {
								dstFeature.setAttribute("the_geom", attribute);
							}

							if (descriptor.getType().getBinding().equals(
									Date.class)) {
								dstFeature.setAttribute("basetime", attribute);
								isSetBasetime = true;
							}

							if (descriptor.getLocalName().equalsIgnoreCase(
									"elev")) {
								dstFeature.setAttribute("elev", attribute);
							}
						}

						if (!isSetBasetime && timePattern != null) {
							final Matcher matcher = timePattern
									.matcher(FilenameUtils
											.getBaseName(contourShp.getName()));
							if (matcher.find()) {
								TimeParser timeParser = new TimeParser();
								List<Date> dates = timeParser.parse(matcher
										.group());
								if (dates != null && dates.size() > 0) {
									Calendar cal = Calendar.getInstance();
									cal
											.setTimeZone(TimeZone
													.getTimeZone("UTC"));
									cal.setTime(dates.get(0));
									cal.setTimeZone(LAMMA_TZ);

									dstFeature.setAttribute("basetime", cal
											.getTime());
								}
							}
						}

						if (elevPattern != null) {
							final Matcher matcher = elevPattern
									.matcher(FilenameUtils
											.getBaseName(contourShp.getName()));
							if (matcher.find()) {
								dstFeature.setAttribute("elevation", Double
										.valueOf(matcher.group()));
							}
						}

						if (runtimePattern != null) {
							final Matcher matcher = runtimePattern
									.matcher(FilenameUtils
											.getBaseName(contourShp.getName()));
							if (matcher.find()) {
								dstFeature.setAttribute("runtime", Integer
										.valueOf(matcher.group()));
							}
						}

						features.add(dstFeature);
					}
				} finally {
					if (fr != null) {
						fr.close();
					}

					dsR.dispose();

					lock.unlock();
				}
			}

			writeFeatureTypes(dsW, contourLayerName, features);
		}

		return bounds;
	}

	/**
	 * 
	 * @param dsW
	 * @param contourLayerName
	 * @param features
	 * @throws IOException
	 */
	private void writeFeatureTypes(DataStore dsW, String contourLayerName,
			List<SimpleFeature> features) throws IOException {
		FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;
		final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
		final Lock lock = rwLock.readLock();
		try {
			fw = dsW.getFeatureWriterAppend(contourLayerName,
					Transaction.AUTO_COMMIT);
			lock.lock();
			for (SimpleFeature ft : features) {
				final SimpleFeature dstFeature = fw.next();
				dstFeature.setAttributes(ft.getAttributes());

				fw.write();
			}
		} catch (IOException e) {
			LOGGER.severe(e.getLocalizedMessage());
			throw e;
		} finally {
			if (fw != null) {
				fw.close();
			}

			lock.unlock();
		}
	}

	/**
	 * 
	 * @param inputDir
	 * @param runtimePattern
	 * @param elevPattern
	 * @param timePattern
	 * @param contourLayerName
	 * @param datastore
	 * @param exists
	 * @throws IOException
	 */
	private void updateContourLayer(File inputDir, Pattern timePattern,
			Pattern elevPattern, Pattern runtimePattern,
			String contourLayerName, File datastore, boolean exists)
			throws IOException {
		if (datastore.exists()) {
			if (Utils.checkFileReadable(datastore)) {
				// read the properties file
				Properties properties = Utils
						.loadPropertiesFromURL(DataUtilities
								.fileToURL(datastore));
				if (properties == null)
					throw new IOException();

				// SPI
				final String SPIClass = properties.getProperty("SPI");
				DataStore contourDataStore = null;
				try {
					// create a datastore as instructed
					final DataStoreFactorySpi spi = (DataStoreFactorySpi) Class
							.forName(SPIClass).newInstance();
					final Map<String, Serializable> params = Utils
							.createDataStoreParamsFromPropertiesFile(
									properties, spi);

					// special case for postgis
					if (spi instanceof PostgisNGJNDIDataStoreFactory
							|| spi instanceof PostgisNGDataStoreFactory) {
						contourDataStore = spi.createDataStore(params);

						// Creating tables or cleanup DB...
						if (!exists) {
							try {
								final String typeSpec = "*the_geom:MultiLineString:srid=4326,elevation:Double,elev:Double,basetime:java.util.Date,runtime:Integer";
								final SimpleFeatureType schema = DataUtilities
										.createType(contourLayerName, typeSpec);
								contourDataStore.createSchema(schema);
							} catch (SchemaException e) {
								throw new IllegalStateException(e);
							} finally {
							}
						} else {
							FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;

							final ReadWriteLock rwLock = new ReentrantReadWriteLock(
									true);
							final Lock lock = rwLock.readLock();
							try {
								lock.lock();
								fw = contourDataStore.getFeatureWriter(
										contourLayerName,
										Transaction.AUTO_COMMIT);

								while (fw.hasNext()) {
									fw.next();
									fw.remove();
								}
							} finally {
								if (fw != null) {
									fw.close();
								}

								lock.unlock();
							}
						}

						final String[] typeNames = contourDataStore
								.getTypeNames();
						if (typeNames.length <= 0)
							throw new IllegalArgumentException(
									"Problems when opening the index, no typenames for the schema are defined");

						// loading all the features into memory to build an
						// in-memory index.
						String typeName = contourLayerName;

						SimpleFeatureSource featureSource = contourDataStore
								.getFeatureSource(typeName);
						if (featureSource == null)
							throw new NullPointerException(
									"The provided SimpleFeatureSource is null, it's impossible to create an index!");

						ReferencedEnvelope bounds = writeContourFeatureTypes(
								inputDir, timePattern, elevPattern,
								runtimePattern, contourLayerName,
								contourDataStore);

						if (!exists)
							createGeoServerLayer(contourLayerName, bounds,
									params);
					}
				} catch (InstantiationException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e);
				} catch (NumberFormatException e) {
					throw new IllegalStateException(e);
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				} catch (NoSuchElementException e) {
					throw new IllegalStateException(e);
				} catch (IndexOutOfBoundsException e) {
					throw new IllegalStateException(e);
				} catch (ParseException e) {
					throw new IllegalStateException(e);
				} catch (SchemaException e) {
					throw new IllegalStateException(e);
				} finally {
					if (contourDataStore != null)
						contourDataStore.dispose();
				}
			}
		}
	}

	/**
	 * 
	 * @param contourLayerName
	 * @param bounds
	 * @param params
	 */
	private void createGeoServerLayer(String contourLayerName,
			ReferencedEnvelope bounds, Map<String, Serializable> params) {

		FileWriter outFile = null;
		PrintWriter out = null;

		// ///
		// Configure datastore
		// ///
		try {
			File dsXML = File.createTempFile("dsXML", ".xml");
			outFile = new FileWriter(dsXML);
			out = new PrintWriter(outFile);

			// Write text to file
			StringBuilder xmlText = new StringBuilder();
			xmlText
					.append(
							"<dataStore><name>lammaDS</name><type>PostGIS</type><enabled>true</enabled><workspace><name>")
					.append(configuration.getDefaultNamespace())
					.append(
							"</name></workspace><connectionParameters><entry key=\"Connection timeout\">20</entry>")
					.append("<entry key=\"port\">")
					.append(params.get("port"))
					.append("</entry>")
					.append("<entry key=\"user\">")
					.append(params.get("user"))
					.append("</entry>")
					.append("<entry key=\"passwd\">")
					.append(params.get("passwd"))
					.append("</entry>")
					.append("<entry key=\"dbtype\">postgis</entry>")
					.append("<entry key=\"host\">")
					.append(params.get("host"))
					.append("</entry>")
					.append("<entry key=\"validate connections\">true</entry>")
					.append("<entry key=\"max connections\">10</entry>")
					.append("<entry key=\"database\">")
					.append(params.get("database"))
					.append("</entry>")
					.append("<entry key=\"namespace\">")
					.append(configuration.getDefaultNamespace())
					.append("</entry>")
					.append("<entry key=\"schema\">")
					.append(params.get("schema"))
					.append("</entry>")
					.append("<entry key=\"Loose bbox\">true</entry>")
					.append("<entry key=\"Expose primary keys\">false</entry>")
					.append(
							"<entry key=\"Max open prepared statements\">50</entry>")
					.append("<entry key=\"fetch size\">1000</entry>").append(
							"<entry key=\"preparedStatements\">false</entry>")
					.append("<entry key=\"min connections\">1</entry>").append(
							"</connectionParameters></dataStore>");
			out.println(xmlText.toString());
			out.flush();
			out.close();

			final String restURL = configuration.getGeoserverURL()
					+ "/rest/workspaces/" + configuration.getDefaultNamespace()
					+ "/datastores/";
			GeoServerRESTHelper.postTextFileTo(new URL(restURL),
					new FileInputStream(dsXML),
					configuration.getGeoserverPWD(), configuration
							.getGeoserverUID(), "application/xml");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"Error occurred while writing datstore file!", e);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}

			outFile = null;
			out = null;
		}

		// ///
		// Configure layer
		// ///
		try {
			File ftXML = File.createTempFile("ftXML", ".xml");
			outFile = new FileWriter(ftXML);
			out = new PrintWriter(outFile);

			// Write text to file
			StringBuilder xmlText = new StringBuilder();
			xmlText
					.append("<featureType>")
					.append("<name>")
					.append(contourLayerName)
					.append("</name>")
					.append("<nativeName>")
					.append(contourLayerName)
					.append("</nativeName>")
					.append("<title>")
					.append(contourLayerName)
					.append("</title>")
					.append("<namespace><name>")
					.append(configuration.getDefaultNamespace())
					.append("</name></namespace>")
					.append(
							"<nativeCRS>GEOGCS[&quot;WGS 84&quot;, &#xd;DATUM[&quot;World Geodetic System 1984&quot;, &#xd;SPHEROID[&quot;WGS 84&quot;, 6378137.0, 298.257223563, AUTHORITY[&quot;EPSG&quot;,&quot;7030&quot;]], &#xd;AUTHORITY[&quot;EPSG&quot;,&quot;6326&quot;]], &#xd;PRIMEM[&quot;Greenwich&quot;, 0.0, AUTHORITY[&quot;EPSG&quot;,&quot;8901&quot;]], &#xd;UNIT[&quot;degree&quot;, 0.017453292519943295], &#xd;AXIS[&quot;Geodetic longitude&quot;, EAST], &#xd;AXIS[&quot;Geodetic latitude&quot;, NORTH], &#xd;AUTHORITY[&quot;EPSG&quot;,&quot;4326&quot;]]</nativeCRS>")
					.append("<srs>EPSG:4326</srs>")
					.append("<nativeBoundingBox>")
					.append("<minx>")
					.append(bounds.getMinX())
					.append("</minx>")
					.append("<maxx>")
					.append(bounds.getMaxX())
					.append("</maxx>")
					.append("<miny>")
					.append(bounds.getMinY())
					.append("</miny>")
					.append("<maxy>")
					.append(bounds.getMaxY())
					.append("</maxy>")
					.append("<crs>EPSG:4326</crs>")
					.append("</nativeBoundingBox>")
					.append("<latLonBoundingBox>")
					.append("<minx>")
					.append(bounds.getMinX())
					.append("</minx>")
					.append("<maxx>")
					.append(bounds.getMaxX())
					.append("</maxx>")
					.append("<miny>")
					.append(bounds.getMinY())
					.append("</miny>")
					.append("<maxy>")
					.append(bounds.getMaxY())
					.append("</maxy>")
					.append("<crs>EPSG:4326</crs>")
					.append("</latLonBoundingBox>")
					.append(
							"<projectionPolicy>FORCE_DECLARED</projectionPolicy>")
					.append("<enabled>true</enabled>")
					.append(
							"<store class=\"dataStore\"><name>lammaDS</name></store>")
					.append("<maxFeatures>0</maxFeatures>")
					.append("<numDecimals>0</numDecimals>")
					.append("<timeAttribute>basetime</timeAttribute>")
					.append("<elevAttribute>elevation</elevAttribute>")
					.append("<attributes>")
					.append("<attribute>")
					.append("<name>the_geom</name>")
					.append("<minOccurs>0</minOccurs>")
					.append("<maxOccurs>1</maxOccurs>")
					.append("<nillable>true</nillable>")
					.append(
							"<binding>com.vividsolutions.jts.geom.MultiLineString</binding>")
					.append("</attribute>").append("<attribute>").append(
							"<name>elevation</name>").append(
							"<minOccurs>0</minOccurs>").append(
							"<maxOccurs>1</maxOccurs>").append(
							"<nillable>true</nillable>").append(
							"<binding>java.lang.Double</binding>").append(
							"</attribute>").append("<attribute>").append(
							"<name>elev</name>").append(
							"<minOccurs>0</minOccurs>").append(
							"<maxOccurs>1</maxOccurs>").append(
							"<nillable>true</nillable>").append(
							"<binding>java.lang.Double</binding>").append(
							"</attribute>").append("<attribute>").append(
							"<name>basetime</name>").append(
							"<minOccurs>0</minOccurs>").append(
							"<maxOccurs>1</maxOccurs>").append(
							"<nillable>true</nillable>").append(
							"<binding>java.sql.Timestamp</binding>").append(
							"</attribute>").append("<attribute>").append(
							"<name>runtime</name>").append(
							"<minOccurs>0</minOccurs>").append(
							"<maxOccurs>1</maxOccurs>").append(
							"<nillable>true</nillable>").append(
							"<binding>java.lang.Integer</binding>").append(
							"</attribute>").append("</attributes>").append(
							"</featureType>");
			out.println(xmlText.toString());
			out.flush();
			out.close();

			final String restURL = configuration.getGeoserverURL()
					+ "/rest/workspaces/" + configuration.getDefaultNamespace()
					+ "/datastores/lammaDS/featuretypes";
			GeoServerRESTHelper.postTextFileTo(new URL(restURL),
					new FileInputStream(ftXML),
					configuration.getGeoserverPWD(), configuration
							.getGeoserverUID(), "application/xml");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"Error occurred while writing featureType file!", e);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}

			outFile = null;
			out = null;
		}

		try {
			GeoServerRESTHelper.configureLayer(new HashMap<String, String>(),
					configuration.getDefaultStyle(), configuration
							.getGeoserverURL(),
					configuration.getGeoserverUID(), configuration
							.getGeoserverPWD(), contourLayerName);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, "Error occurred while configuring "
					+ contourLayerName + " Layer!", e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error occurred while configuring "
					+ contourLayerName + " Layer!", e);
		} catch (TransformerException e) {
			LOGGER.log(Level.SEVERE, "Error occurred while configuring "
					+ contourLayerName + " Layer!", e);
		}
	}

	/**
	 * 
	 * @param layerId
	 * @return
	 * @throws TransformerException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private boolean checkIfContourLayerExists(String layerId)
			throws ParserConfigurationException, IOException,
			TransformerException {
		return GeoServerRESTHelper.checkLayerExistence(configuration
				.getGeoserverURL(), configuration.getGeoserverUID(),
				configuration.getGeoserverPWD(), layerId);
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