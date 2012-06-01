/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.postgres.shp2pg;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.encoder.GSPostGISDatastoreEncoder;
import it.geosolutions.tools.commons.file.Path;
import it.geosolutions.tools.compress.file.Extract;
import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Andrea Di Nora - andrea.dinora@sinergis.it
 * 
 */
public class Shp2pgAction extends BaseAction<EventObject> {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(Shp2pgAction.class);

	/**
	 * configuration
	 */
	private final Shp2pgConfiguration configuration;

	public Shp2pgAction(Shp2pgConfiguration configuration) throws IOException {
		super(configuration);
		this.configuration = configuration;
	}

	/**
	 * Removes TemplateModelEvents from the queue and put
	 */
	public Queue<EventObject> execute(Queue<EventObject> events)
			throws ActionException {
		listenerForwarder.setTask("config");
		listenerForwarder.started();
		if (configuration == null) {
			throw new IllegalStateException("ActionConfig is null.");
		}
		File workingDir = Path.findLocation(
				configuration.getWorkingDirectory(),
				((FileBaseCatalog) CatalogHolder.getCatalog())
						.getBaseDirectory());
		if (workingDir == null) {
			throw new IllegalStateException("Working directory is null.");
		}
		if (!workingDir.exists() || !workingDir.isDirectory()) {
			throw new IllegalStateException((new StringBuilder())
					.append("Working directory does not exist (")
					.append(workingDir.getAbsolutePath()).append(").")
					.toString());
		}
		FileSystemEvent event = (FileSystemEvent) events.peek();
		// String shapeName = null;
		File shapefile = null;
		File zippedFile = null;
		File files[];
		if (events.size() == 1) {
			zippedFile = event.getSource();
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace((new StringBuilder())
						.append("Testing for compressed file: ")
						.append(zippedFile.getAbsolutePath()).toString());
			}
			String tmpDirName = null;
			try {
				tmpDirName = Extract.extract(zippedFile.getAbsolutePath());
			} catch (Exception e) {
				final String message = "Shp2pgAction.execute(): Unable to read zip file: "
						+ e.getLocalizedMessage();
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				throw new ActionException(this, message);
			}
			listenerForwarder.progressing(5F, "File extracted");
			File tmpDirFile = new File(tmpDirName);
			if (!tmpDirFile.isDirectory()) {
				throw new IllegalStateException(
						"Not valid input: we need a zip file ");
			}
			Collector c = new Collector(null);
			List fileList = c.collect(tmpDirFile);
			if (fileList != null) {
				files = (File[]) fileList.toArray(new File[1]);
			} else {
				String message = "Input is not a zipped file nor a valid collection of files";
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(message);
				}
				throw new IllegalStateException(message);
			}
		} else if (events.size() >= 3) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Checking input collection...");
			}
			listenerForwarder.progressing(6F, "Checking input collection...");
			files = new File[events.size()];
			int i = 0;
			for (Iterator i$ = events.iterator(); i$.hasNext();) {
				FileSystemEvent ev = (FileSystemEvent) i$.next();
				files[i++] = ev.getSource();
			}

		} else {
			throw new IllegalStateException(
					"Input is not a zipped file nor a valid collection of files");
		}
		if ((shapefile = acceptable(files)) == null) {
			throw new IllegalStateException(
					"The file list do not contains mondadory files");
		}

		listenerForwarder.progressing(10F, "In progress");

		// At this moment i have the shape and a file list

		// connect to the shapefile
		final Map<String, Object> connect = new HashMap<String, Object>();
		connect.put("url", DataUtilities.fileToURL(shapefile));

		DataStore sourceDataStore = null;
		String typeName = null;
		SimpleFeatureType originalSchema = null;
		try {
			sourceDataStore = DataStoreFinder.getDataStore(connect);
			String[] typeNames = sourceDataStore.getTypeNames();
			typeName = typeNames[0];

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Reading content " + typeName);
			}

			originalSchema = sourceDataStore.getSchema(typeName);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("SCHEMA HEADER: "
						+ DataUtilities.spec(originalSchema));
			}
		} catch (IOException e) {
			final String message = "Error to create PostGres datastore"
					+ e.getLocalizedMessage();
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message);
			if (sourceDataStore != null)
				sourceDataStore.dispose();
			throw new ActionException(this, message);
		}
		// prepare to open up a reader for the shapefile
		Query query = new Query();
		query.setTypeName(typeName);
		CoordinateReferenceSystem prj = originalSchema
				.getCoordinateReferenceSystem();
		query.setCoordinateSystem(prj);

		DataStore destinationDataSource = null;
		try {
			destinationDataSource = this.createPostgisDataStore(configuration);

			// check if the schema is present in postgis
			boolean schema = false;
			if (destinationDataSource.getTypeNames().length != 0) {
				for (String tableName : destinationDataSource.getTypeNames()) {
					if (tableName.equalsIgnoreCase(typeName)) {
						schema = true;
					}
				}
			} else {
				schema = false;
			}
			if (!schema)
				destinationDataSource.createSchema(originalSchema);
			LOGGER.info("SCHEMA: " + schema);

		} catch (IOException e) {
			String message = "Error to create postGis datastore";
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(message);
			}
			if (destinationDataSource != null)
				destinationDataSource.dispose();
			throw new IllegalStateException(message);
		}

		final Transaction transaction = new DefaultTransaction("create");
		FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;
		FeatureReader<SimpleFeatureType, SimpleFeature> fr = null;
		try {
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(
					destinationDataSource.getSchema(typeName));
			fw = destinationDataSource.getFeatureWriter(typeName, transaction);
			fr = sourceDataStore.getFeatureReader(query, transaction);
			SimpleFeatureType sourceSchema = sourceDataStore
					.getSchema(typeName);
			FeatureStore postgisStore = (FeatureStore) destinationDataSource
					.getFeatureSource(typeName);
			while (fr.hasNext()) {
				final SimpleFeature oldfeature = fr.next();

				for (AttributeDescriptor ad : sourceSchema
						.getAttributeDescriptors()) {
					String attribute = ad.getLocalName();
					builder.set(attribute, oldfeature.getAttribute(attribute));
				}
				postgisStore.addFeatures(DataUtilities.collection(builder
						.buildFeature(null)));

			}

			// close transaction
			transaction.commit();

		} catch (Throwable e) {
			try {
				transaction.rollback();
			} catch (IOException e1) {
				final String message = "Transaction rollback unsuccessful: "
						+ e1.getLocalizedMessage();
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				throw new ActionException(this, message);
			}
		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				final String message = "Transaction close unsuccessful: "
						+ e.getLocalizedMessage();
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				throw new ActionException(this, message);
			}
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e1) {
					final String message = "Feature reader IO exception: "
							+ e1.getLocalizedMessage();
					if (LOGGER.isErrorEnabled())
						LOGGER.error(message);
					throw new ActionException(this, message);
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e1) {
					final String message = "Feature writer IO exception: "
							+ e1.getLocalizedMessage();
					if (LOGGER.isErrorEnabled())
						LOGGER.error(message);
					throw new ActionException(this, message);
				}
			}
			if (sourceDataStore != null) {
				try {
					sourceDataStore.dispose();
				} catch (Throwable t) {
				}
			}
			if (destinationDataSource != null) {
				try {
					destinationDataSource.dispose();
				} catch (Throwable t) {
				}
			}
		}

		GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
				configuration.getGeoserverURL(),
				configuration.getGeoserverUID(),
				configuration.getGeoserverPWD());

		publisher.createWorkspace(configuration.getDefaultNamespace());

		GSPostGISDatastoreEncoder datastoreEncoder = new GSPostGISDatastoreEncoder();

		datastoreEncoder.setUser(configuration.getDbUID());
		datastoreEncoder.setDatabase(configuration.getDbName());
		datastoreEncoder.setPassword(configuration.getDbPWD());
		datastoreEncoder.setHost(configuration.getDbServerIp());
		datastoreEncoder.setPort(Integer.valueOf(configuration.getDbPort()));
		datastoreEncoder.setName(configuration.getDbName());

		publisher.createPostGISDatastore(configuration.getDefaultNamespace(),
				datastoreEncoder);
		String shapeFileName = FilenameUtils.getBaseName(shapefile.getName());

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Layer postgis publishing xml-> ");
			LOGGER.info("datastoreEncoder xml: " + datastoreEncoder.toString());
		}

		if (publisher.publishDBLayer(configuration.getDefaultNamespace(),
				configuration.getDbName(), shapeFileName,
				configuration.getCrs(), configuration.getDefaultStyle())) {
			String message = "PostGis layer SUCCESFULLY registered";
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(message);
			}
			listenerForwarder.progressing(100F, message);
		} else {
			String message = "PostGis layer not registered";
			ActionException ae = new ActionException(this, message);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(message, ae);
			}
			listenerForwarder.failed(ae);
		}
		events.clear();

		return events;
	}

	private static File acceptable(File files[]) {
		if (files == null) {
			return null;
		}
		String shapeFileName = null;
		File shapeFile = null;
		int acceptable = 0;
		File arr$[] = files;
		int len$ = arr$.length;
		for (int i$ = 0; i$ < len$; i$++) {
			File file = arr$[i$];
			if (file == null) {
				continue;
			}
			String ext = FilenameUtils.getExtension(file.getAbsolutePath());
			if (ext.equals("shp")) {
				acceptable++;
				if (shapeFileName == null) {
					shapeFileName = FilenameUtils.getBaseName(file.getName());
					shapeFile = file;
				} else {
					return null;
				}
				continue;
			}
			if (ext.equals("shx")) {
				acceptable++;
				continue;
			}
			if (ext.equals("dbf")) {
				acceptable++;
			}
		}

		if (acceptable == 3) {
			return shapeFile;
		} else {
			return null;
		}
	}

	private DataStore createPostgisDataStore(Shp2pgConfiguration configuration)
			throws IOException {
		DataStoreFactorySpi factoryPG = new PostgisDataStoreFactory();// NG

		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("user", configuration.getDbUID());
		map.put("database", configuration.getDbName());
		map.put("passwd", configuration.getDbPWD());
		map.put("host", configuration.getDbServerIp());
		map.put("port", configuration.getDbPort());
		map.put("dbtype", configuration.getDbType());

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("PostGis params-> ");
			LOGGER.info("user: " + configuration.getDbUID());
			LOGGER.info("database: " + configuration.getDbName());
			LOGGER.info("passwd: " + configuration.getDbPWD());
			LOGGER.info("host: " + configuration.getDbServerIp());
			LOGGER.info("port: " + configuration.getDbPort());
			LOGGER.info("dbtype: " + configuration.getDbType());
		}

		return factoryPG.createDataStore(map);
	}
}
