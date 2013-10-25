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
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.tools.compress.file.Extract;
import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.geotools.data.DataStore;
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
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
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
@Action(configurationClass = Ds2dsConfiguration.class)
public class Ds2dsAction extends DsBaseAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ds2dsAction.class);

    private static final List<String> ACCEPTED_FILE_TYPES = Collections.unmodifiableList(Arrays.asList("xml", "shp", "run"));

	private Ds2dsConfiguration actionConfiguration;

    public Ds2dsAction(Ds2dsConfiguration actionConfiguration) {
        super(actionConfiguration);
        this.actionConfiguration = actionConfiguration;
    }

    @Override
    @CheckConfiguration
    public boolean checkConfiguration() {
        LOGGER.info("Calculating if this action could be Created...");
        return true;
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
            final Ds2dsConfiguration configuration = (Ds2dsConfiguration)actionConfiguration.clone();
            final EventObject ev;
            try {
                if ((ev = events.remove()) != null) {
                    listenerForwarder.started();

                    updateTask("Working on incoming event: " + ev.getSource());

                    Queue<FileSystemEvent> acceptableFiles = acceptableFiles(unpackCompressedFiles(ev));
                    if (acceptableFiles.size() == 0) {
                        failAction("No file to process", configuration);
                    } else {
                        List<ActionException> exceptions = new ArrayList<ActionException>();
                        for (FileSystemEvent fileEvent : acceptableFiles) {
                            try {
                                EventObject output = importFile(fileEvent, configuration);
                                if (output != null) {
                                    // add the event to the return
                                    outputEvents.add(output);
                                } else {
                                    if (LOGGER.isWarnEnabled()) {
                                        LOGGER.warn("No output produced");
                                    }
                                }
                            } catch (ActionException e) {
                                exceptions.add(e);
                            }

                        }
                        if (acceptableFiles.size() == exceptions.size()) {
                            throw new ActionException(this, exceptions.get(0).getMessage());
                        } else if (exceptions.size() > 0) {
                            if (LOGGER.isWarnEnabled()) {
                                for (ActionException ex : exceptions) {
                                    LOGGER.warn("Error in action: " + ex.getMessage());
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
            } catch (ActionException ioe) {
                failAction("Unable to produce the output, "
                        + ioe.getLocalizedMessage(), ioe, configuration);
            } catch (Exception ioe) {
                failAction("Unable to produce the output: "
                        + ioe.getLocalizedMessage(), ioe, configuration);
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
    private EventObject importFile(FileSystemEvent fileEvent, Ds2dsConfiguration configuration) throws ActionException {
        DataStore sourceDataStore = null;
        DataStore destDataStore = null;

        final Transaction transaction = new DefaultTransaction("create");
        try {
            // source
            sourceDataStore = createSourceDataStore(fileEvent, configuration);
            Query query = buildSourceQuery(sourceDataStore, configuration);
            FeatureStore<SimpleFeatureType, SimpleFeature> featureReader = createSourceReader(
                    sourceDataStore, transaction, query);

            // output
            destDataStore = createOutputDataStore(configuration);
            SimpleFeatureType schema = buildDestinationSchema(featureReader
                    .getSchema(), configuration);

            FeatureStore<SimpleFeatureType, SimpleFeature> featureWriter = createOutputWriter(
                    destDataStore, schema, transaction);
            SimpleFeatureType destSchema = featureWriter.getSchema();

            // check for schema case differences from input to output
            Map<String, String> schemaDiffs = compareSchemas(destSchema, schema);
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(destSchema);

            purgeData(featureWriter, configuration);

            updateTask("Reading data");
            int total = featureReader.getCount(query);
            FeatureIterator<SimpleFeature> iterator = createSourceIterator(
                    query, featureReader);
            int count = 0;
            try {
                while (iterator.hasNext()) {
                    SimpleFeature feature = buildFeature(builder,
                            iterator.next(), schemaDiffs, sourceDataStore, configuration);
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
            updateTask("Data imported (" + count + " features)");
            transaction.commit();
            listenerForwarder.completed();
            return buildOutputEvent(configuration);
        } catch (Exception ioe) {
            try {
                transaction.rollback();
            } catch (IOException e1) {
                final String message = "Transaction rollback unsuccessful: "
                        + e1.getLocalizedMessage();
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(message);
                }
                throw new ActionException(this, message);
            }
            String cause = ioe.getCause() == null ? null : ioe.getCause().getMessage();
            String msg = "MESSAGE: " + ioe.getMessage() + " - CAUSE: " + cause;
            throw new ActionException(this, msg);

        } finally {
            updateTask("Closing connections");
            closeResource(sourceDataStore);
            closeResource(destDataStore);
            closeResource(transaction);
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
        if (!fileEvent.getSource().exists()) {
            result.add(fileEvent);
            return result;
        }
        updateTask("Looking for compressed file");
        try {
            String filePath = fileEvent.getSource().getAbsolutePath();
            String uncompressedFolder = Extract.extract(filePath);
            if (!uncompressedFolder.equals(filePath)) {
                updateTask("Compressed file extracted to " + uncompressedFolder);
                Collector c = new Collector(null);
                List<File> fileList = c.collect(new File(uncompressedFolder));

                if (fileList != null) {
                    for (File file : fileList) {
                        if (!file.isDirectory()) {
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
            SimpleFeatureType sourceSchema, Ds2dsConfiguration configuration) {
        String typeName = configuration.getOutputFeature().getTypeName();
        if (typeName == null) {
            typeName = sourceSchema.getTypeName();
            configuration.getOutputFeature().setTypeName(typeName);
        }
        CoordinateReferenceSystem crs = configuration.getOutputFeature()
                .getCoordinateReferenceSystem();
        if (crs == null) {
            String reprojCrs = configuration.getReprojectedCrs();
            if (reprojCrs != null && !reprojCrs.isEmpty()) {
                try {
                    crs = CRS.decode(reprojCrs);
                } catch (Exception e) {
                    LOGGER.error("Failed to decode reprojCrs, use src CRS for now but please fix the configuration. The exception occurred is " + e.getClass());
                    crs = sourceSchema.getCoordinateReferenceSystem();
                }
            } else {
                crs = sourceSchema.getCoordinateReferenceSystem();
            }
            configuration.getOutputFeature().setCoordinateReferenceSystem(crs);
        }
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setCRS(crs);
        builder.setName(typeName);

        for (String attributeName : buildOutputAttributes(sourceSchema, configuration)) {
            builder.add(buildSchemaAttribute(attributeName, sourceSchema, crs, configuration));
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
    private Collection<String> buildOutputAttributes(SimpleFeatureType sourceSchema, Ds2dsConfiguration configuration) {

        if (configuration.isProjectOnMappings()) {
            return configuration.getAttributeMappings().keySet();
        } else {
            List<String> attributes = new ArrayList<String>();
            for (AttributeDescriptor attr : sourceSchema.getAttributeDescriptors()) {
                attributes.add(getAttributeMapping(attr.getLocalName(), configuration));
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
    private String getAttributeMapping(String localName, Ds2dsConfiguration configuration) {
        for (String outputName : configuration.getAttributeMappings().keySet()) {
            if (configuration.getAttributeMappings().get(outputName).toString().equals(localName)) {
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
            SimpleFeatureType schema, CoordinateReferenceSystem crs, Ds2dsConfiguration configuration) {
        AttributeDescriptor attr;
        if (configuration.getAttributeMappings().containsKey(attributeName) && !isExpression(configuration.getAttributeMappings()
                .get(attributeName).toString())) {
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
     * Gets the list of received file events, filtering out those not correct for this action.
     *
     * @param events
     * @return
     */
    private Queue<FileSystemEvent> acceptableFiles(Queue<FileSystemEvent> events) {
        updateTask("Recognize file type");
        Queue<FileSystemEvent> accepted = new LinkedList<FileSystemEvent>();
        for (FileSystemEvent event : events) {
            String fileType = getFileType(event);
            if (ACCEPTED_FILE_TYPES.contains(fileType)) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Accepted file: " + event.getSource().getName());
                }
                accepted.add(event);
            } else {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Skipped file: " + event.getSource().getName());
                }
            }
        }
        return accepted;
    }
}
