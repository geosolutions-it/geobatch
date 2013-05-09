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

package it.geosolutions.geobatch.actions.ds2ds;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.tools.compress.file.Extract;
import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action can be used to copy data from two different GeoTools datastores,
 * eventually transforming them in the process.
 * 
 * @author Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it
 * 
 */
public class Ds2dsAction extends BaseAction<EventObject> {

	private final static Logger LOGGER = LoggerFactory.getLogger(Ds2dsAction.class);
	
	private static final List<String> acceptedFileTypes = Arrays.asList("xml", "shp");	
	
	private Ds2dsConfiguration configuration = null;
		
	public Ds2dsAction(ActionConfiguration actionConfiguration) {
		super(actionConfiguration);		
		configuration = (Ds2dsConfiguration)actionConfiguration.clone();
	}
	/**
	 * Imports data from the source DataStore to the output one
	 * transforming the data as configured.
	 */
	@Override
	public Queue<EventObject> execute(Queue<EventObject> events)
			throws ActionException {

		// return object
		final Queue<EventObject> outputEvents = new LinkedList<EventObject>();

		while (events.size() > 0) {
			final EventObject ev;
			try {
				if ((ev = events.remove()) != null) {
					listenerForwarder.started();

					updateTask("Working on incoming event: " + ev.getSource());

					Queue<FileSystemEvent> acceptableFiles = acceptableFiles(unpackCompressedFiles(ev));
					if (acceptableFiles.size() == 0) {
						failAction("No file to process");
					} else {
						for (FileSystemEvent fileEvent : acceptableFiles) {
							EventObject output = importFile(fileEvent);
							if (output != null) {
								// add the event to the return
								outputEvents.add(output);
							} else {
								if (LOGGER.isWarnEnabled()) {
									LOGGER.warn("No output produced");
								}
							}
						}
					}

				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Encountered a NULL event: SKIPPING...");
					}
					continue;
				}
			} catch (Exception ioe) {
				failAction("Unable to produce the output: "
						+ ioe.getLocalizedMessage());
			}
		}
		return outputEvents;
	}
	

	/**
	 * Does the actual import on the given file event.
	 * 
	 * @param fileEvent
	 * @return ouput EventObject (an xml describing the output feature)
	 * @throws ActionException 
	 */
	private EventObject importFile(FileSystemEvent fileEvent) throws ActionException {
		DataStore sourceDataStore = null;			
		DataStore destDataStore = null;				
					
		final Transaction transaction = new DefaultTransaction("create");
		try {
			// source
			sourceDataStore = createSourceDataStore(fileEvent);
			Query query = buildSourceQuery(sourceDataStore);
			FeatureStore<SimpleFeatureType, SimpleFeature> featureReader = createSourceReader(
					sourceDataStore, transaction, query);

			// output
			destDataStore = createOutputDataStore();
			SimpleFeatureType schema = buildDestinationSchema(featureReader
					.getSchema());
			
			FeatureStore<SimpleFeatureType, SimpleFeature> featureWriter = createOutputWriter(
					destDataStore, schema, transaction);	
			SimpleFeatureType destSchema = featureWriter.getSchema();
			
			// check for schema case differences from input to output
			Map<String, String> schemaDiffs = compareSchemas(destSchema, schema);
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(destSchema);
			
			purgeData(featureWriter);
			
			updateTask("Reading data");
			int total = featureReader.getCount(query);
			FeatureIterator<SimpleFeature> iterator = createSourceIterator(
					query, featureReader);
			try {
				int count = 0;
				while (iterator.hasNext()) {
					SimpleFeature feature = buildFeature(builder,
							iterator.next(), schemaDiffs);
					featureWriter.addFeatures(DataUtilities
							.collection(feature));
					count++;
					if (count % 100 == 0) {
						updateImportProgress(count, total, "Importing data");							
					}
				}
				listenerForwarder.progressing(100F, "Data imported");
			
			} finally {
				iterator.close();
			}
			updateTask("Data imported");
			transaction.commit();
			listenerForwarder.completed();
			return buildOutputEvent();
		} catch (Exception ioe) {
			try {
				transaction.rollback();
			} catch (IOException e1) {
				final String message = "Transaction rollback unsuccessful: "
						+ e1.getLocalizedMessage();
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				throw new ActionException(this, message);
			}
			throw new ActionException(this, ioe.getMessage());
							
		} finally {		
			updateTask("Closing connections");					
			closeResource(sourceDataStore);										
			closeResource(destDataStore);	
			closeResource(transaction);
		}
		
		
		
	}
	/**
	 * Compare input and output schemas for different case mapping in attribute names.
	 * 
	 * @param destSchema
	 * @param schema
	 * @return
	 */
	private Map<String, String> compareSchemas(SimpleFeatureType destSchema,
			SimpleFeatureType schema) {
		Map<String, String> diffs = new HashMap<String,String>();
		for (AttributeDescriptor ad :destSchema.getAttributeDescriptors()) {
			String attribute = ad.getLocalName();
			if(schema.getDescriptor(attribute) == null) {
				for(String variant : getNameVariants(attribute)) {
					if(schema.getDescriptor(variant) != null) {
						diffs.put(attribute, variant);
						break;
					}					
				}
			}							
		}
		return diffs;
	}
	/**
	 * Returns case variants for the given name.
	 *  
	 * @param name
	 * @return
	 */
	private String[] getNameVariants(String name) {
		return new String[] {name.toLowerCase(), name.toUpperCase()};
	}
	
	private String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	/**
	 * Updates the import progress ( progress / total )
	 * for the listeners.
	 * 
	 * @param progress
	 * @param total
	 * @param message
	 */
	private void updateImportProgress(int progress, int total, String message) {
		listenerForwarder.progressing((float) progress , message);
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("Importing data: "+progress + "/" + total);
		}
	}

	/**
	 * Purge data on output feature, if requested.
	 * 
	 * @param featureWriter
	 * @throws IOException
	 */
	private void purgeData(
			FeatureStore<SimpleFeatureType, SimpleFeature> featureWriter) throws IOException {
		if(configuration.isPurgeData()) {
			updateTask("Purging existing data");
			featureWriter.removeFeatures(Filter.INCLUDE);
			updateTask("Data purged");
		}		
	}

	/**
	 * Creates an iterator on the source features
	 * 
	 * @param query Query used to filter the source
	 * @param featureReader store for the source
	 * @return
	 * @throws IOException
	 */
	private FeatureIterator<SimpleFeature> createSourceIterator(Query query,
			FeatureStore<SimpleFeatureType, SimpleFeature> featureReader)
			throws IOException {
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureReader
				.getFeatures(query);					
		FeatureIterator<SimpleFeature> iterator = features.features();
		return iterator;
	}

	/**
	 * Creates the source datastore reader.
	 * 
	 * @param sourceDataStore
	 * @param transaction
	 * @param query
	 * @return
	 * @throws IOException
	 */
	private FeatureStore<SimpleFeatureType, SimpleFeature> createSourceReader(
			DataStore sourceDataStore, final Transaction transaction,
			Query query) throws IOException {
		FeatureStore<SimpleFeatureType, SimpleFeature> featureReader = 
				(FeatureStore<SimpleFeatureType, SimpleFeature>) sourceDataStore
				.getFeatureSource(query.getTypeName());							
		featureReader.setTransaction(transaction);
		return featureReader;
	}

	/**
	 * Builds the output event, with information about the imported data.
	 * 
	 * @param outputEvents
	 * @param schema
	 * @return
	 * @throws FileNotFoundException
	 * @throws ActionException
	 */
	private EventObject buildOutputEvent()
			throws FileNotFoundException, ActionException {
		updateTask("Building output event");
		FileOutputStream outStream = null;
		try {
			File outputDir = getTempDir();
			File outputFile = new File(outputDir.getAbsolutePath()
					+ File.separator + "output.xml");
			outStream = new FileOutputStream(outputFile);
			configuration.getOutputFeature().toXML(outStream);

			updateTask("Output event built");
			return new FileSystemEvent(outputFile,
					FileSystemEventType.FILE_ADDED);
			
		} catch (Exception e) {
			throw new ActionException(this, "Error writing output event");
		} finally {
			IOUtils.closeQuietly(outStream);
		}		
	}

	/**
	 * Eventually unpacks compressed files.
	 * 
	 * @param fileEvent
	 * @return
	 * @throws ActionException 
	 */
	private Queue<FileSystemEvent> unpackCompressedFiles(EventObject event)
			throws ActionException {
		Queue<FileSystemEvent> result = new LinkedList<FileSystemEvent>();
		
		FileSystemEvent fileEvent = (FileSystemEvent) event;
		updateTask("Looking for compressed file");		
		try {
			String filePath = fileEvent.getSource().getAbsolutePath();
			String uncompressedFolder = Extract.extract(filePath);
			if(!uncompressedFolder.equals(filePath)) {
				updateTask("Compressed file extracted to " + uncompressedFolder);
				Collector c = new Collector(null);
				List<File> fileList = c.collect(new File(uncompressedFolder));
				
				if (fileList != null) {
					for(File file : fileList) {
						if(!file.isDirectory()) {
							result.add(new FileSystemEvent(file, fileEvent.getEventType()));
						}
					}
				}
			} else {
				// no compressed file, add as is
				updateTask("File is not compressed");
				result.add(fileEvent);
			}
		} catch (Exception e) {
			throw new ActionException(this, e.getMessage());
		}			

		return result;
	}

	/**
	 * Builds the output Feature schema.
	 * By default it uses the original source schema, if not overriden by configuration.
	 * 
	 * @param sourceSchema
	 * @return
	 */
	private SimpleFeatureType buildDestinationSchema(
			SimpleFeatureType sourceSchema) {		
		String typeName = configuration.getOutputFeature().getTypeName();
		if (typeName == null) {
			typeName = sourceSchema.getTypeName();
			configuration.getOutputFeature().setTypeName(typeName);
		}
		CoordinateReferenceSystem crs = configuration.getOutputFeature()
				.getCoordinateReferenceSystem();
		if (crs == null) {
			crs = sourceSchema.getCoordinateReferenceSystem();
			configuration.getOutputFeature().setCoordinateReferenceSystem(crs);
		}
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setCRS(crs);
		builder.setName(typeName);
		
		for(String attributeName : buildOutputAttributes(sourceSchema)) {
			builder.add(buildSchemaAttribute(attributeName, sourceSchema, crs));			
		}				
		return builder.buildFeatureType();				
	}

	/**
	 * Builds the list of output attributes, looking at mappings configuration and
	 * source schema.
	 * 
	 * @param sourceSchema
	 * @return
	 */
	private Collection<String> buildOutputAttributes(SimpleFeatureType sourceSchema) {
		
		if(configuration.isProjectOnMappings()) {
			return configuration.getAttributeMappings().keySet();			
		} else {
			List<String> attributes = new ArrayList<String>();
			for (AttributeDescriptor attr : sourceSchema.getAttributeDescriptors()) {
				attributes.add(getAttributeMapping(attr.getLocalName()));
			}
			return attributes;
		}				
	}

	/**
	 * Gets the eventual output mapping for the given source attribute name.
	 * 
	 * @param localName
	 * @return
	 */
	private String getAttributeMapping(String localName) {
		for(String outputName : configuration.getAttributeMappings().keySet()) {
			if(configuration.getAttributeMappings().get(outputName).toString().equals(localName)) {
				return outputName;
			}
		}
		return localName;
	}

	/**
	 * Builds a single attribute for the output Feature schema.
	 * By default it uses the original source attribute definition, if not overridden by
	 * configuration. 
	 * @param attr
	 * @param crs crs to use for geometric attributes
	 * @return
	 */
	private AttributeDescriptor buildSchemaAttribute(String attributeName,
			SimpleFeatureType schema, CoordinateReferenceSystem crs) {
		AttributeDescriptor attr;
		if (configuration.getAttributeMappings().containsKey(attributeName)) {
			attr = schema.getDescriptor(configuration.getAttributeMappings()
					.get(attributeName).toString());
		} else {
			attr = schema.getDescriptor(attributeName);
		}	
		AttributeTypeBuilder builder = new AttributeTypeBuilder();
		builder.setName(attr.getLocalName());
		builder.setBinding(attr.getType().getBinding());
		if (attr instanceof GeometryDescriptor) {
			if (crs == null) {
				crs = ((GeometryDescriptor) attr).getCoordinateReferenceSystem();
			}
			builder.setCRS(crs);
		}

		// set descriptor information
		builder.setMinOccurs(attr.getMinOccurs());
		builder.setMaxOccurs(attr.getMaxOccurs());
		builder.setNillable(attr.isNillable());

		return builder.buildDescriptor(attributeName);

	}

	/**
	 * Builds a FeatureStore for the output Feature.
	 * 
	 * @param store
	 * @param schema
	 * @return
	 * @throws IOException
	 */
	private FeatureStore<SimpleFeatureType, SimpleFeature> createOutputWriter(
			DataStore store, SimpleFeatureType schema, Transaction transaction)
			throws IOException {
		String destTypeName = schema.getTypeName();
		boolean createSchema = true;
		for (String typeName : store.getTypeNames()) {
			if (typeName.equalsIgnoreCase(destTypeName)) {
				createSchema = false;
				destTypeName = typeName;
			}
		}
		// check for case changing in typeName
		if (createSchema) {
			store.createSchema(schema);
			for (String typeName : store.getTypeNames()) {
				if (! typeName.equals(destTypeName) && typeName.equalsIgnoreCase(destTypeName)) {
					destTypeName = typeName;
				}
			}
		}
		FeatureStore<SimpleFeatureType, SimpleFeature> result = (FeatureStore<SimpleFeatureType, SimpleFeature>) store
				.getFeatureSource(destTypeName);
		result.setTransaction(transaction);
		return result;
	}

	/**
	 * Creates the destination DataStore, from the configuration connection parameters.
	 * 
	 * @return
	 * @throws IOException
	 * @throws ActionException 
	 */
	private DataStore createOutputDataStore() throws IOException, ActionException {
		updateTask("Connecting to output DataStore");
		return createDataStore(configuration.getOutputFeature().getDataStore());
	}
	
	/**
	 * Creates the source DataStore from the given input file event.
	 * 
	 * @param fileEvent
	 * @return
	 * @throws IOException
	 * @throws ActionException 
	 */
	private DataStore createSourceDataStore(FileSystemEvent fileEvent) throws IOException, ActionException {
		updateTask("Connecting to source DataStore");		
		String fileType = getFileType(fileEvent);
		FeatureConfiguration sourceFeature = configuration.getSourceFeature();
		if(fileType.equals("xml")) {
			InputStream inputXML = null;
			try {
				inputXML = new FileInputStream(fileEvent.getSource());				
				sourceFeature  = FeatureConfiguration.fromXML(inputXML);							
			} catch (Exception e) {
	            throw new IOException("Unable to load input XML", e);
	        } finally {
	            IOUtils.closeQuietly(inputXML);
	        }
		} else if(fileType.equals("shp")) {			
			sourceFeature.getDataStore()
					.put("url", DataUtilities.fileToURL(fileEvent.getSource()));
		} 		
		DataStore source = createDataStore(sourceFeature.getDataStore());
		// if no typeName is configured, takes the first one registered in store
		if(sourceFeature.getTypeName() == null) {
			sourceFeature.setTypeName(source.getTypeNames()[0]);
		}
		// if no CRS is configured, takes if from the feature
		if (sourceFeature.getCrs() == null) {
			sourceFeature.setCoordinateReferenceSystem(source.getSchema(
					sourceFeature.getTypeName())
					.getCoordinateReferenceSystem());
		}
		configuration.setSourceFeature(sourceFeature);
		return source;
	}

	/**
	 * Creates a DataStore from the given connection parameters.
	 * 
	 * @param connect
	 * @return
	 * @throws IOException
	 * @throws ActionException 
	 */
	private DataStore createDataStore(Map<String,Serializable> connect) throws IOException, ActionException {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("DataStore connection parameters:");
			for(String connectKey : connect.keySet()) {
				LOGGER.info(connectKey + " -> "+connect.get(connectKey));
			}
		}
		
		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		if(dataStore == null) {
			failAction("Cannot connect to DataStore: wrong parameters");
		}
		return dataStore;
	}

	/**
	 * Builds a Feature instance to be written on output.
	 * 
	 * @param builder
	 * @param sourceFeature
	 * @return
	 */
	private SimpleFeature buildFeature(SimpleFeatureBuilder builder, SimpleFeature sourceFeature, Map<String, String> mappings) {
		for (AttributeDescriptor ad : builder.getFeatureType().getAttributeDescriptors()) {
			String attribute = ad.getLocalName();
			builder.set(attribute, getAttributeValue(sourceFeature, attribute, mappings));
		}
		return builder.buildFeature(null);
	}

	/**
	 * Builds an attribute value to be written on output.
	 * @param sourceFeature source used to get values to write
	 * @param attributeName name of the attribute in the output feature
	 * @return
	 */
	private Object getAttributeValue(SimpleFeature sourceFeature,
			String attributeName, Map<String, String> mappings) {
		// gets mapping for renamed attributes
		
		if(configuration.getAttributeMappings().containsKey(attributeName)) {
			attributeName = configuration.getAttributeMappings().get(attributeName).toString();
		} else if(mappings.containsKey(attributeName)) {
			attributeName = mappings.get(attributeName);
		}
		return sourceFeature.getAttribute(attributeName);
	}

	private void updateTask(String task) {
		listenerForwarder.setTask(task);
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(task);					
		}
	}

	private void closeResource(DataStore dataStore) {
		if(dataStore != null) {
			try {				
				dataStore.dispose();
			} catch(Throwable t) {
				if(LOGGER.isErrorEnabled()) {
					LOGGER.error("Error closing datastore connection");					
				}
			}
		}
	}	
	
	private void closeResource(Transaction transaction) {
		if(transaction != null) {
			try {
				transaction.close();
			} catch(Throwable t) {
				if(LOGGER.isErrorEnabled()) {
					LOGGER.error("Error closing transaction");					
				}
			}
		}
	}	

	/**
	 * Builds a Query Object for the source Feature.
	 * 
	 * @param sourceStore
	 * @return
	 * @throws IOException
	 */
	private Query buildSourceQuery(DataStore sourceStore) throws IOException {
		Query query = new Query();		
		query.setTypeName(configuration.getSourceFeature().getTypeName());
		query.setCoordinateSystem(configuration.getSourceFeature().getCoordinateReferenceSystem());		
		return query;
	}

	private void failAction(String message) throws ActionException {
		failAction(message, null);
	}

	private void failAction(String message, Throwable t) throws ActionException {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error(message);
			if(t != null) {
				LOGGER.error(getStackTrace(t));
			}
		}
		if(!configuration.isFailIgnored()) {
			final ActionException e = new ActionException(this, message, t);
			listenerForwarder.failed(e);		
			throw e;
		}
	}

	/**
	 * Gets the list of received file events, filtering out those not correct for
	 * this action.
	 * 
	 * @param events
	 * @return
	 */
	private Queue<FileSystemEvent> acceptableFiles(Queue<FileSystemEvent> events) {
		updateTask("Recognize file type");	
		Queue<FileSystemEvent> accepted = new LinkedList<FileSystemEvent>();
		for(FileSystemEvent event : events) {			
			String fileType = getFileType(event);			
			if(acceptedFileTypes.contains(fileType)) {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Accepted file: "+event.getSource().getName());
				}
				accepted.add(event);
			} else {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Skipped file: "+event.getSource().getName());
				}
			}
		}
		return accepted;
	}

	private String getFileType(FileSystemEvent event) {
		return FilenameUtils.getExtension(event.getSource().getName()).toLowerCase();
	}
}
