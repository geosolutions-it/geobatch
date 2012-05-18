/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.tools.commons.time.TimeParser;
import it.geosolutions.tools.io.file.Copy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.Hints;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Abstract class to provide update functions to the ImageMosaic action
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
abstract class ImageMosaicUpdater {

	/**
	 * Default logger
	 */
	protected final static Logger LOGGER = LoggerFactory
			.getLogger(ImageMosaicAction.class);

	/**
	 * 
	 * @param files
	 * @param absolute
	 * @param key
	 *            optional list of string
	 * @return the query string if success, null otherwise.
	 * @throws NullPointerException
	 * @throws CQLException
	 */
	private static Filter getQuery(List<File> files, boolean absolute,
			String... key) throws IllegalArgumentException, CQLException {

		if (files == null) { // Optional -> || key==null
			throw new IllegalArgumentException(
					"The passed argument file list is null!");
		}

		// check the size
		final int size = files.size();
		if (size == 0) {
			return null;
		}
		/**
		 * TODO probably we may want to change the query if the size is too big
		 * to list all of the file into it! Carlo 03 Mar 2011
		 */
		// case fileLocation IN ('f1','f2',...,'fn')
		if (key[0] == null) {
			throw new IllegalArgumentException(
					"The passed argument key list contains a null element!");
		}
		StringBuilder query = new StringBuilder(key[0] + " IN (");

		if (absolute) {
			for (int i = 0; i < size; i++) {
				File file = files.get(i);
				if (file.exists()) {
					query.append((i == 0) ? "'" : ",'");
					query.append(file.getAbsolutePath()
							.replaceAll("\\", "\\\\"));
					query.append("'");
				} else if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Unable to use the following "
							+ "file to build the query, it does not exists.\nFile"
							+ file.getAbsolutePath());
				}
			}
			query.append(")");
		} else {
			for (int i = 0; i < size; i++) {
				File file = files.get(i);
				if (file.exists()) {
					query.append((i == 0) ? "'" : ",'");
					query.append(file.getName());
					query.append("'");
				} else if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Unable to use the following "
							+ "file to build the query, it does not exists.\nFile"
							+ file.getAbsolutePath());
				}
			}
			query.append(")");
		}

		// filter=ff.equals(ff.property(locationKey), ff.literal());
		/**
		 * The "in predicate" was added in ECQL. (Have a look in the bnf
		 * http://docs
		 * .codehaus.org/display/GEOTOOLS/ECQL+Parser+Design#ECQLParserDesign-
		 * INPredicate) this is the rule for the falue list: <in value list> ::=
		 * <expression> {"," <expression>}
		 * 
		 * Thus, you could write sentences like: Filter filter =
		 * ECQL.toFilter("length IN (4100001,4100002, 4100003 )"); or Filter
		 * filter = ECQL.toFilter("name IN ('one','two','three')"); other Filter
		 * filter = ECQL.toFilter("length IN ( (1+2), 3-4, [5*6] )");
		 */
		return ECQL.toFilter(query.toString());
	}

	private static boolean setFeature(File baseDir, File granule,
			SimpleFeature feature, String geometryName, String locationKey) {


        String granuleBaseName = FilenameUtils.getBaseName(granule.getAbsolutePath());

		// get attributes and copy them over
		try {
			AbstractGridFormat format = null;
			format = (AbstractGridFormat) GridFormatFinder.findFormat(granule);
			if (format == null || (format instanceof UnknownFormat)) {
				throw new IllegalArgumentException(
						"Unable to find a reader for the provided file: "
								+ granule.getAbsolutePath());
			}
			// can throw UnsupportedOperationsException
			final AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) format
					.getReader(granule, new Hints(
							Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
							Boolean.TRUE));

			GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

			ReferencedEnvelope bb = new ReferencedEnvelope(originalEnvelope);

			WKTReader wktReader = new WKTReader();
			Geometry the_geom = wktReader.read("POLYGON((" + bb.getMinX() + " "
					+ bb.getMinY() + "," + bb.getMinX() + " " + bb.getMaxY()
					+ "," + bb.getMaxX() + " " + bb.getMaxY() + ","
					+ bb.getMaxX() + " " + bb.getMinY() + "," + bb.getMinX()
					+ " " + bb.getMinY() + "))");

			Integer SRID = CRS.lookupEpsgCode(
					bb.getCoordinateReferenceSystem(), true);
			/*
			 * TODO ETJ suggestion: String crsId =
			 * CRS.lookupIdentifier(bb.getCoordinateReferenceSystem(), true);
			 */
			if (SRID == null) {
				throw new IllegalArgumentException(
						"Unable to get the EPSG code for the granule: "
								+ granule);
			}
			the_geom.setSRID(SRID);

			feature.setAttribute(geometryName, the_geom);
			// TODO absolute
			feature.setAttribute(locationKey, granule.getName());
			// granule.getName().replaceAll("\\", "\\\\"));

			final File indexer = new File(baseDir, org.geotools.gce.imagemosaic.Utils.INDEXER_PROPERTIES);
			final Properties indexerProps = ImageMosaicProperties.getProperty(indexer);

			/**
			 * @see {@link #org.geotools.gce.imagemosaic.properties.RegExPropertiesCollector.collect(File)}
			 */
			final String granuleName=FilenameUtils.getBaseName(granule.getName());
			if (indexerProps.getProperty("TimeAttribute") != null) {
				// TODO move out of the cycle
				final File timeregexFile = new File(baseDir, "timeregex.properties");
				final Properties timeProps = ImageMosaicProperties.getProperty(timeregexFile);
                String timeregex = timeProps.getProperty("regex");
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("time regex: --->"+timeregex+"<--");
                }
				final Pattern timePattern = Pattern.compile(timeregex);
				// TODO move out of the cycle
				if (timePattern != null) { // when it is == null?????
					final Matcher matcher = timePattern.matcher(granuleName);
					if (matcher.find()) {
                        String matchedGroup = matcher.group();
                        if(LOGGER.isDebugEnabled()) {
                            LOGGER.debug("time regex is matching: ["+matchedGroup+"]");
                        }
						TimeParser timeParser = new TimeParser();
						List<Date> dates = timeParser.parse(matchedGroup);
                        if(LOGGER.isDebugEnabled()) {
                            LOGGER.debug("TimeParser parsed dates:" + dates);
                        }

						if (dates != null && !dates.isEmpty()) {
							Calendar cal = Calendar.getInstance();
							cal.setTimeZone(TimeZone.getTimeZone("UTC"));
							cal.setTime(dates.get(0));

							feature.setAttribute(
									indexerProps.getProperty("TimeAttribute"),
									cal.getTime());
						}
					} else {
                        if(LOGGER.isWarnEnabled()) {
                            LOGGER.warn("time regex is not matching");
                        }
                    }
				}
			}

			if (indexerProps.getProperty("ElevationAttribute") != null) {
				// TODO move out of the cycle
				final File elevationRegex = new File(baseDir,
						"elevationregex.properties");
				final Properties elevProps = ImageMosaicProperties
						.getProperty(elevationRegex);
				final Pattern elevPattern = Pattern.compile(elevProps
						.getProperty("regex"));
				// TODO move out of the cycle
				final Matcher matcher = elevPattern.matcher(granuleName);
				if (matcher.find()) {
					feature.setAttribute(
							indexerProps.getProperty("ElevationAttribute"),
							Double.valueOf(matcher.group()));
				}
			}

			if (indexerProps.getProperty("RuntimeAttribute") != null) {
				// TODO move out of the cycle
				final File runtimeRegex = new File(baseDir,
						"runtimeregex.properties");
				final Properties runtimeProps = ImageMosaicProperties
						.getProperty(runtimeRegex);
				final Pattern runtimePattern = Pattern.compile(runtimeProps
						.getProperty("regex"));
				// TODO move out of the cycle
				final Matcher matcher = runtimePattern.matcher(granuleName);
				if (matcher.find()) {
					feature.setAttribute(
							indexerProps.getProperty("RuntimeAttribute"),
							Integer.valueOf(matcher.group()));
				}
			}

			return true;

		} catch (Throwable e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(e.getLocalizedMessage(), e);
			return false;
		}

	}

	/**
	 * return the datastore or null
	 * 
	 * @param mosaicProp
	 * @param dataStoreProp
	 * @param mosaicDescriptor
	 * @param cmd
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 *             if datastoreProp is null
	 * @throws InstantiationException
	 * @throws IOException
	 */
	private static DataStore getDataStore(Properties dataStoreProp)
			throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, IOException {
		if (dataStoreProp == null) {
			throw new IllegalArgumentException(
					"Unable to get datastore properties.");
		}

		DataStore dataStore = null;

		// SPI
		final String SPIClass = dataStoreProp.getProperty("SPI");
		try {
			DataStoreFactorySpi spi = (DataStoreFactorySpi) Class.forName(
					SPIClass).newInstance();

			final Map<String, Serializable> params = Utils
					.createDataStoreParamsFromPropertiesFile(dataStoreProp, spi);

			// datastore creation
			dataStore = spi.createDataStore(params);

		} catch (IOException ioe) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(
						"Problems setting up (creating or connecting) the datasource. The message is: "
								+ ioe.getLocalizedMessage(), ioe);
			}
			throw ioe;
		}

		if (dataStore == null) {
			throw new NullPointerException(
					"The required resource (DataStore) was not found or if insufficent parameters were given.");
		}
		return dataStore;
	}

	/**
	 * Check the value passed for a boolean: Object could be String or boolean
	 * instance
	 * 
	 * @param value
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static boolean isTrue(Object value) throws IllegalArgumentException {
		String valueStr;
		if (value == null) {
			throw new IllegalArgumentException(
					"Unable to parse the boolean flag. Object is null");
		}
		final boolean ret;
		if (value instanceof String) {
			valueStr = (String) value;
			if (valueStr.equalsIgnoreCase("true")) {
				ret = true;
				// no need to copy files
			} else if (valueStr.equalsIgnoreCase("false")) {
				ret = false;
			} else {
				throw new IllegalArgumentException(
						"Unable to parse the boolean flag. Value: " + valueStr);
			}
		} else if (value instanceof Boolean) {
			ret = (Boolean) value;
		} else {
			throw new IllegalArgumentException(
					"Unable to parse the boolean flag: object is not a String nor Boolean");
		}
		return ret;
	}

	/**
	 * Remove features in a new transaction.<br>
	 * Do not dispose datastore.
	 * 
	 * @param dataStore
	 * @param delFilter
	 * @return true if success
	 * 
	 */
	private static boolean removeFeatures(final DataStore dataStore,
			final String store, final Filter delFilter) {
		Transaction transaction = null;
		if (delFilter == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("The file list is not used to query datastore: Probably it is empty");
			}
			return true;
		}

		final String handle = "ImageMosaic:" + Thread.currentThread().getId();

		FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;

		try {
			// once closed you have to renew the reference
			if (transaction == null)
				transaction = new DefaultTransaction(handle);

			fw = dataStore.getFeatureWriter(store, delFilter, transaction);
			if (fw == null) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("The FeatureWriter is null, it's impossible to get a writer on the dataStore: "
							+ dataStore.toString());
				}
				return false;
			}
			// get the schema if this feature
			// final FeatureType schema = fw.getFeatureType();

			// TODO check needed??? final String geometryPropertyName =
			// schema.getGeometryDescriptor().getLocalName();
			
			while (fw.hasNext()) {
				fw.next();
				fw.remove();
			}

		} catch (Exception e) {
			if (transaction != null){
				try {
					transaction.rollback();
				} catch (IOException ioe) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(ioe.getLocalizedMessage(), ioe);
					}
				}
				try {
					transaction.close();
				} catch (IOException ioe) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(ioe.getLocalizedMessage(), ioe);
					}
				}
				transaction=null;
			}
			// TODO recover removed file to rollback!
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getLocalizedMessage(),e);
			}
			return false;
		} finally {
			if (transaction != null) {
				try {
					transaction.commit();
				} catch (IOException ioe) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(ioe.getLocalizedMessage(), ioe);
					}
				}
				try {
					transaction.close();
				} catch (IOException ioe) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(ioe.getLocalizedMessage(), ioe);
					}
				}
				transaction = null; // once closed you have to renew the
									// reference
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(e.getLocalizedMessage(), e);
					}
				}
				fw = null;
			}
		}
		return true;
	}

	/**
	 * Removes from the passed addFileList already present (into the passed
	 * dataStore) features TODO this can be skipped to perform update (instead
	 * of perform remove+update)
	 */
	private static boolean purgeAddFileList(List<File> addFileList,
			DataStore dataStore, String store, Filter addFilter,
			final String locationKey, final File baseDir, boolean absolute) {

		final String handle = "ImageMosaic:" + Thread.currentThread().getId();

		Transaction transaction = null;
		/*
		 * CHECK IF ADD FILES ARE ALREADY INTO THE LAYER
		 */
		if (addFilter == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("the ADD query ins null. Probably add list is empty");
			}
			return false;
		}

		FeatureReader<SimpleFeatureType, SimpleFeature> fr = null;
		try {
			// once closed you have to renew the reference
			if (transaction == null)
				transaction = new DefaultTransaction(handle);

			// get the schema if this feature
			final SimpleFeatureType schema = dataStore.getSchema(store);
			/*
			 * TODO to save time we could use the store name which should be the
			 * same
			 */

			Query q = new Query(schema.getTypeName(), addFilter);
			fr = dataStore.getFeatureReader(q, transaction);
			if (fr == null) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("The FeatureReader is null, it's impossible to get a reader on the dataStore: "
							+ dataStore.toString());
				}
				return false;
			}
			while (fr.hasNext()) {
				SimpleFeature feature = fr.next();
				if (feature != null) {
					String path = (String) feature.getAttribute(locationKey);

					// remove from the list the image which is already
					// into the layer
					if (absolute) {
						File added = new File(baseDir, path);
						addFileList.remove(added);
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("The file: "
									+ path
									+ " is removed from the addFiles list because it is already present into the layer");
						}
					} else {
						// check relative paths
						Iterator<File> it = addFileList.iterator();
						while (it.hasNext()) {
							File file = it.next();
							if (file.getName().equals(path)) {
								it.remove();
								if (LOGGER.isWarnEnabled()) {
									LOGGER.warn("The file: "
											+ path
											+ " is removed from the addFiles list because it is already present into the layer");
								}
							}
						}
					}
				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Problem getting the next feature: it is null!");
					}
				}

			}
		} catch (Exception e) {
			if (transaction != null)
				try {
					transaction.rollback();
					transaction.close();
					transaction = null;
				} catch (IOException ioe) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(ioe.getLocalizedMessage(), ioe);
					}
				}
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}
			return false;
		} finally {
			try {
				if (transaction != null) {
					transaction.commit();
					transaction.close();
					transaction = null; // once closed you have to renew
										// the reference
				}
				if (fr != null) {
					fr.close();
					fr = null;
				}
			} catch (Throwable t) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(
							"problem closing transaction: "
									+ t.getLocalizedMessage(), t);
				}
			}
		}

		return true;
	}

	/**
	 * add files to the passed datastore
	 * 
	 * @param addList
	 * @param dataStore
	 * @param store
	 * @param locationKey
	 * @param baseDir
	 * @return
	 */
	private static boolean addFileToStore(List<File> addList,
			DataStore dataStore, String store, final String locationKey,
			final File baseDir) {
		// //////////////////////////////
		/*
		 * ADD FILES TO THE LAYER
		 */

		final String handle = "ImageMosaic:" + Thread.currentThread().getId();

		Transaction transaction = null;
		FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;
		try {
			// once closed you have to renew the reference
			if (transaction == null)
				transaction = new DefaultTransaction(handle);

			try {
				fw = dataStore.getFeatureWriterAppend(store, transaction);
			} catch (IOException ioe) {
				try {

					if (transaction != null) {
						transaction.rollback();
						transaction.close();
						transaction = null;
					}
				} catch (Throwable t) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(
								"problem closing transaction: "
										+ t.getLocalizedMessage(), t);
					}
				}
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("unable to access to the datastore in append mode. Message: "
							+ ioe.getLocalizedMessage());
				}
				return false;
			}
			if (fw == null) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("The FeatureWriter is null, it's impossible"
							+ " to get a writer on the dataStore: "
							+ dataStore.toString());
				}
				return false;
			}

			// get the schema if this feature
			final FeatureType schema = fw.getFeatureType();

			// TODO check needed???
			final String geometryPropertyName = schema.getGeometryDescriptor()
					.getLocalName();

			for (File file : addList) {
				// get the next feature to append
				SimpleFeature feature = fw.next();
				if (feature != null) {
                    if(LOGGER.isInfoEnabled()) {
                        LOGGER.info("Updating feature for file " + file);
                    }
					setFeature(baseDir, file, feature, geometryPropertyName,
							locationKey);
					fw.write();
				}
			}

		} catch (Exception e) {

			if (transaction != null) {
				try {
					transaction.rollback();
					transaction.close();
					transaction = null;
				} catch (Throwable t) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(
								"Problem during rollback"
										+ t.getLocalizedMessage(), t);
					}
				}
			}
			return false;
		} finally {
			if (transaction != null) {
				try {
					transaction.commit();
					transaction.close();
					transaction = null; // once closed you have to renew
										// the reference
				} catch (Throwable t) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(
								"Problem closing transaction: "
										+ t.getLocalizedMessage(), t);
					}
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (Throwable t) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(
								"Problem closing transaction: "
										+ t.getLocalizedMessage(), t);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Try to update the datastore using the passed command and the
	 * mosaicDescriptor as data and
	 * 
	 * @param mosaicProp
	 * @param dataStoreProp
	 * @param mosaicDescriptor
	 * @param cmd
	 * @return boolean representing the operation success (true) or failure
	 *         (false)
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 */
	protected static boolean updateDataStore(Properties mosaicProp,
			Properties dataStoreProp,
			ImageMosaicGranulesDescriptor mosaicDescriptor,
			ImageMosaicCommand cmd) throws IllegalArgumentException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		if (mosaicProp == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Unable to get mosaic properties.");
			}
			return false;
		}

		DataStore dataStore = null;
		try {
			// SPI
			dataStore = getDataStore(dataStoreProp);

			boolean absolute = isTrue(mosaicProp.get(org.geotools.gce.imagemosaic.Utils.Prop.ABSOLUTE_PATH));
			// does the layer use absolute path?
			if (!absolute) {
				/*
				 * if we have some absolute path into delFile list we have to
				 * skip those files since the layer is relative and acceptable
				 * (to deletion) passed path are to be relative
				 */
				List<File> files = null;
				if ((files = cmd.getDelFiles()) != null) {
					for (File file : files) {
						if (file.isAbsolute()) {
							/*
							 * this file can still be acceptable since it can be
							 * child of the layer baseDir
							 */
							final String path = file.getAbsolutePath();
							if (!path.contains(cmd.getBaseDir()
									.getAbsolutePath())) {
								// the path is absolute AND the file is outside
								// the layer baseDir!
								// files.remove(file); // remove it
								// TODO move into a recoverable path to
								// rollback!
								// log as warning
								if (LOGGER.isWarnEnabled()) {
									LOGGER.warn("Layer specify a relative pattern for files but the "
											+ "incoming xml command file has an absolute AND outside the layer baseDir file into the "
											+ "delFile list! This file will NOT be removed from the layer: "
											+ file.getAbsolutePath());
								}
							}
						}
					}
				}
			}

			// TODO check object cast
			// the attribute key location
			final String locationKey = (String) mosaicProp.get(org.geotools.gce.imagemosaic.Utils.Prop.LOCATION_ATTRIBUTE);

			// final String[] typeNames = dataStore.getTypeNames();
			// if (typeNames.length <= 0)
			// throw new IllegalArgumentException(
			// "ImageMosaicAction: Problems when opening the index, no typenames for the schema are defined");

			final String store = mosaicDescriptor.getCoverageStoreId();

			final List<File> delList = cmd.getDelFiles();
			Filter delFilter = null;
			// query
            if(delList != null && ! delList.isEmpty()) {
    			try {
                    delFilter = getQuery(delList, absolute, locationKey);
                } catch (IllegalArgumentException e) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(e.getLocalizedMessage());
                    }
                } catch (CQLException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Unable to build a query. Message: " + e, e);
                    }
                    return false;
                }

                // REMOVE features
                if (!removeFeatures(dataStore, store, delFilter)) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Failed to remove features.");
                    }
                }
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("No items to delete");
                }
            }

			// ///////////////////////////////////
			final List<File> addList = cmd.getAddFiles();
			Filter addFilter = null;
			// calculate the query
			try {
				addFilter = getQuery(addList, absolute, locationKey);
			} catch (IllegalArgumentException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(e.getLocalizedMessage());
				}
			} catch (CQLException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Unable to build a query. Message: " + e,
							e);
				}
				return false;
			}
			// purge the addlist
			// TODO remove (ALERT please remove existing file from destination
			// for the copyListFileToNFS()
			purgeAddFileList(addList, dataStore, store, addFilter, locationKey,
					cmd.getBaseDir(), absolute);
			addFilter = null;

			// //////////////////////////////////
			if (cmd.getAddFiles() == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("addFiles list is null.");
				}
				return false;
			} else if ( cmd.getAddFiles().isEmpty()) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No more images to add to the layer were found, please check the command.");
				}
				return false;
			} else if (cmd.getAddFiles().size() > 0) {
				/*
				 * copy purged addFiles list of files to the baseDir and replace
				 * addFiles list with the new copied file list
				 */
				// store copied file for rollback purpose
				List<File> addedFile = null;
				if (!absolute) {
                    if(LOGGER.isInfoEnabled()) {
                        LOGGER.info("Starting file copy ("+cmd.getAddFiles().size()+" file/s)");
                    }
					addedFile = Copy.copyListFileToNFS(cmd.getAddFiles(),
							cmd.getBaseDir(), false, ImageMosaicAction.WAIT);
				}
				if (!addFileToStore(addedFile, dataStore, store, locationKey,
						cmd.getBaseDir())) {
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Unable to update the new layer, removing copied files...");
					// if fails rollback the copied files
					if (addedFile != null) {
						for (File file : addedFile) {
							if (LOGGER.isWarnEnabled())
								LOGGER.warn("ImageMosaicAction: DELETING -> "
										+ file.getAbsolutePath());
							// this is done since addedFiles are copied
							// not moved
							file.delete();
						}
					}
				}
			} // addFiles size > 0

		} catch (Exception e) {

			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}

			return false;

		} finally {
			if (dataStore != null) {
				try {
					dataStore.dispose();
				} catch (Throwable t) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(t.getLocalizedMessage(), t);
					}
					/*
					 * return false; TODO: check is this formally correct? if
					 * the datastore failed to be disposed...
					 */
				}
			}
		}
		return true;
	}

	/**
	 * Try to update the datastore using the passed command and the
	 * mosaicDescriptor as data and
	 * 
	 * @param mosaicProp
	 * @param dataStoreProp
	 * @param mosaicDescriptor
	 * @param cmd
	 * @return boolean representing the operation success (true) or failure
	 *         (false)
	 * @deprecated TODO use to update existing methods using FeatureStore
	 *             instead of FeatureWriter
	 */
	private static void removeDataStore(Properties mosaicProp,
			Properties dataStoreProp,
			ImageMosaicGranulesDescriptor mosaicDescriptor,
			ImageMosaicCommand cmd) {

		DataStore dataStore = null;

		try {
			// SPI
			final String SPIClass = dataStoreProp.getProperty("SPI");

			// datastore creation
			dataStore = getDataStore(dataStoreProp);
			if (dataStore == null) {
				final String message = "The required resource was not found or if insufficent parameters were given.";
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(message);
				}
				// return false;
			}

			SimpleFeatureStore fs = null;

			Transaction transaction = null;

			/*
			 * CHECK IF ADD FILES ARE ALREADY INTO THE LAYER
			 */
			try {
				// once closed you have to renew the reference
				if (transaction == null)
					transaction = new DefaultTransaction("transaction");

				final Filter f;
				// TODO use SQL query
				// if (limitSize>0)
				// f= CQL.toFilter(selectFilter+ " LIMIT " + limitSize); // TODO
				// check problems
				// else
				// f = CQL.toFilter(selectFilter);
				f = CQL.toFilter("");

				fs = (SimpleFeatureStore) dataStore.getFeatureSource("TABLE");

				// the returned results
				// final List<Map<String, Object>> ret = new
				// ArrayList<Map<String, Object>>(
				// limitSize);

				final SimpleFeatureCollection coll = fs.getFeatures(new Query(
						""));
				final SimpleFeatureIterator featureIt = coll.features();
				while (featureIt.hasNext()) {
					final SimpleFeature feature = featureIt.next();
					final SimpleFeatureType type = feature.getFeatureType();

					final Map<String, Object> map = new HashMap<String, Object>();
					final int attrCount = feature.getAttributeCount();
					for (int attr = 0; attr < attrCount; attr++) {
						map.put(type.getDescriptor(attr).getLocalName(),
								feature.getAttribute(attr));
					}
					// add data to ret ret.add(map);

					fs.modifyFeatures("NAME", "VALUE", CQL.toFilter(""));
				}

				// return ret;

			} catch (NullPointerException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(
							"Unable to access to the datastore in append mode. Message: "
									+ e.getLocalizedMessage(), e);
				}
				e.printStackTrace();
				try {
					if (transaction != null)
						transaction.rollback();
				} catch (Throwable t) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(
								"::updateDataStore(): unable to rollback transaction: "
										+ t.getLocalizedMessage(), t);
					}
				}
				// return null;
			} catch (RuntimeException re) {
				if (LOGGER.isErrorEnabled()) {
					final String message = "problem with connection: "
							+ re.getLocalizedMessage();
					LOGGER.error(message, re);
				}
				re.printStackTrace();
				try {
					if (transaction != null)
						transaction.rollback();
				} catch (Throwable t) {

				}
				// return null;
			} finally {
				try {
					if (transaction != null) {
						transaction.commit();
						transaction.close();
						transaction = null; // once closed you have to renew the
											// reference
					}
				} catch (Throwable t) {

				}
			}

		} catch (Throwable e) {

			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}

			// return null;
		} finally {
			try {
				if (dataStore != null) {
					dataStore.dispose();
				}
			} catch (Throwable t) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(
							"::updateDataStore(): " + t.getLocalizedMessage(),
							t);
				}
				/*
				 * return false; TODO: check is this formally correct? if the
				 * datastore failed to be disposed...
				 */
			}
		}
	}
}
