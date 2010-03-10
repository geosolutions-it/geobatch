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

package it.geosolutions.geobatch.ais.anomalies;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.ais.dao.AISAnomaliesDAO;
import it.geosolutions.geobatch.ais.dao.DAOException;
import it.geosolutions.geobatch.ais.model.AISAnomalies;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Point;

public class AISAnomaliesGeoServerGenerator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	private AISAnomaliesDAO aisAnomaliesDAO;

	private File tempOutDir = null;

	public AISAnomaliesGeoServerGenerator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	public AISAnomaliesGeoServerGenerator(
			GeoServerActionConfiguration configuration,
			AISAnomaliesDAO aisAnomaliesDAO) throws IOException {
		super(configuration);
		this.aisAnomaliesDAO = aisAnomaliesDAO;
	}

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		// ////////////////////////////////////////////////////////////////////
		//
		// Initializing input variables
		//
		// ////////////////////////////////////////////////////////////////////
		try {
			if (configuration == null) {
				LOGGER.log(Level.SEVERE, "ActionConfig is null.");
				throw new IllegalStateException("ActionConfig is null.");
			}

			final File workingDir = IOUtils.findLocation(configuration
					.getWorkingDirectory(), new File(
					((FileBaseCatalog) CatalogHolder.getCatalog())
							.getBaseDirectory()));

			if (workingDir == null) {
				LOGGER.log(Level.SEVERE, "Working directory is null.");
				throw new IllegalStateException("Working directory is null.");
			}

			if (!workingDir.exists() || !workingDir.isDirectory()) {
				LOGGER.log(Level.SEVERE, "Working directory does not exist ("
						+ workingDir.getAbsolutePath() + ").");
				throw new IllegalStateException(
						"Working directory does not exist ("
								+ workingDir.getAbsolutePath() + ").");
			}

			FileSystemMonitorEvent event = events.peek();

			File[] shpList;

			if (events.size() == 1
					&& FilenameUtils.getExtension(
							event.getSource().getAbsolutePath())
							.equalsIgnoreCase("zip")) {
				shpList = handleZipFile(event.getSource(), workingDir);
			} else {
				shpList = handleShapefile(events);
			}

			if (shpList == null)
				throw new Exception("Error while processing the shape file set");

			// look for the main shp file in the set
			File shapeFile = null;
			for (File file : shpList) {
				if (FilenameUtils.getExtension(file.getName())
						.equalsIgnoreCase("shp")) {
					shapeFile = file;
					break;
				}
			}

			if (shapeFile == null) {
				LOGGER.log(Level.SEVERE, "Shp file not found in fileset.");
				throw new IllegalStateException(
						"Shp file not found in fileset.");
			}

			String shpBaseName = FilenameUtils.getBaseName(shapeFile.getName());

			LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + shpBaseName);

			// //
			// creating dataStore
			// //

			Map<String, Serializable> connectionParams = new HashMap();

			try {
				connectionParams.put("url", shapeFile.toURI().toURL());
			} catch (MalformedURLException e) {
				LOGGER.log(Level.SEVERE,
						"No valid ShapeFile URL found for this Data Flow: "
								+ e.getLocalizedMessage());
				throw new IllegalStateException(
						"No valid ShapeFile URL found for this Data Flow: "
								+ e.getLocalizedMessage());
			}

			DataStore dataStore = DataStoreFinder
					.getDataStore(connectionParams);

			String typeName = dataStore.getTypeNames()[0];

			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
			FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
			FeatureIterator<SimpleFeature> iterator;

			featureSource = dataStore.getFeatureSource(typeName);
			collection = featureSource.getFeatures();
			iterator = collection.features();

			List<AISAnomalies> anomalies = new ArrayList<AISAnomalies>();
			final SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyyMMdd'T'HHmmss");

			while (iterator.hasNext()) {
				AISAnomalies theAnomaly = null;
				try {
					final Feature feature = iterator.next();
					theAnomaly = new AISAnomalies();
					theAnomaly.setMsmsi((Long) feature.getProperty("MMSI")
							.getValue());
					theAnomaly.setTime(sdf.parse((String) feature.getProperty(
							"Time").getValue()));
					theAnomaly.setType((String) feature.getProperty("Class")
							.getValue());

					theAnomaly.setLocation((Point) feature.getProperty(
							"the_geom").getValue());

				} catch (java.text.ParseException e) {
					theAnomaly = null;
					LOGGER.finest("createAndStoreAnomaly - " + e.toString()
							+ " : " + e.getLocalizedMessage());
					throw e;
				} finally {
					if (theAnomaly != null)
						anomalies.add(theAnomaly);
				}
			}

			LOGGER.info(" CLASS TYPE ANOMALIES >>>>>>>>>>>>>>>>>>>>>>>>>"
					+ anomalies.get(0).getType());

			storeAISAnomalies(anomalies);

		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		} finally {
			// Clear unzipped files, if any
			// if(tempOutDir != null)
			// FileUtils.deleteDirectory(tempOutDir);
			//
			// // Clear sent zip file
			// if(zipFileToSend != null)
			// zipFileToSend.delete();
		}

		return null;
	}

	private void storeAISAnomalies(List<AISAnomalies> anomalies) {
		try {
			// ///////////////////////////////////////////
			// First deleted form DB
			// ///////////////////////////////////////////
			if (anomalies.size() > 0)
				aisAnomaliesDAO.delete(anomalies.get(0).getType());

			// ///////////////////////////////////////////
			// Thean insert all features
			// ///////////////////////////////////////////
			for (AISAnomalies ais : anomalies) {
				aisAnomaliesDAO.save(ais);
			}
		} catch (DAOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private File[] handleShapefile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
	}

	private File[] handleZipFile(File source, File workingdir) {

		tempOutDir = new File(workingdir, "unzip_" + System.currentTimeMillis());

		try {
			if (!tempOutDir.mkdir()) {
				throw new IOException("Can't create temp dir '"
						+ tempOutDir.getAbsolutePath() + "'");
			}
			List<File> fileList = IOUtils.unzipFlat(source, tempOutDir);
			if (fileList == null) {
				throw new Exception("Error unzipping file");
			}

			if (fileList.isEmpty()) {
				throw new IllegalStateException("Unzip returned no files");
			}

			int shp = 0, shx = 0, dbf = 0;
			int prj = 0;

			// check that all the files have the same basename
			File file0 = fileList.get(0);
			String basename = FilenameUtils.getBaseName(file0.getName());
			for (File file : fileList) {
				if (!basename.equals(FilenameUtils.getBaseName(file
						.getAbsolutePath()))) {
					throw new Exception("Basename mismatch (expected:'"
							+ basename + "', file found:'"
							+ file.getAbsolutePath() + "')");
				}
				String ext = FilenameUtils.getExtension(file.getAbsolutePath());
				// do we want such an hardcoded list?
				if ("shp".equalsIgnoreCase(ext))
					shp++;
				else if ("shx".equalsIgnoreCase(ext))
					shx++;
				else if ("dbf".equalsIgnoreCase(ext))
					dbf++;
				else if ("prj".equalsIgnoreCase(ext))
					prj++;
				else {
					// Do we want to be more lenient if unexpected/useless files
					// are found?
					throw new IllegalStateException(
							"Unexpected file extension in zipfile '" + ext
									+ "'");
				}
			}

			if (shp * shx * dbf != 1) {
				throw new Exception("Bad fileset in zip file.");
			}

			return fileList.toArray(new File[fileList.size()]);

		} catch (Throwable t) {
			LOGGER.log(Level.WARNING, "Error examining zipfile", t);
			try {
				// org.apache.commons.io.IOUtils.
				FileUtils.forceDelete(tempOutDir);
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, "Can't delete temp dir '" + tempOutDir
						+ "'", ex);
			}
			return null;
		}
	}

}
