/*
 *  Copyright (C) 2013 GeoSolutions S.A.S.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQLCompiler;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.NonIncrementingPrimaryKeyColumn;
import org.geotools.jdbc.PrimaryKey;
import org.geotools.jdbc.PrimaryKeyColumn;
import org.geotools.jdbc.PrimaryKeyFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public abstract class DsBaseAction extends BaseAction<EventObject> {

	private final static Logger LOGGER = LoggerFactory.getLogger(DsBaseAction.class);

	protected Ds2dsConfiguration configuration = null;
	
	private Filter filter;
	
	private List<PrimaryKeyColumn> pks;
	
	private Boolean isPkGenerated;
	
	public DsBaseAction(ActionConfiguration actionConfiguration) {
		super(actionConfiguration);
		this.configuration = (Ds2dsConfiguration)actionConfiguration.clone();
		this.pks = null;
		this.isPkGenerated = false;
	}
    
    /**
     * Purge data on output feature, if requested.
     *
     * @param featureWriter
     * @throws IOException
     */
    protected void purgeData(FeatureStore<SimpleFeatureType, SimpleFeature> featureWriter) throws Exception {
        if(configuration.isForcePurgeAllData()){
            updateTask("Purging ALL existing data");
            featureWriter.removeFeatures(Filter.INCLUDE);
            updateTask("Data purged");
        }
        else if (configuration.isPurgeData()) {
            updateTask("Purging existing data");
            featureWriter.removeFeatures(buildFilter());
            updateTask("Data purged");
        }
    }

    protected void updateTask(String task) {
        listenerForwarder.setTask(task);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(task);
        }
    }
    
    protected Filter buildFilter() throws Exception {
        if(filter != null){
            return filter;
        }
        String cqlFilter = configuration.getEcqlFilter();
        if(cqlFilter == null || cqlFilter.isEmpty()){
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No cql source store filter set...");
            }
            return Filter.INCLUDE;
        }
        ECQLCompiler compiler = new ECQLCompiler(cqlFilter, CommonFactoryFinder.getFilterFactory2());
        try {
            compiler.compileFilter();
            return compiler.getFilter();
        } catch (CQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception("Error while cql filter compilation. please check and be sure that the cql filter specified in configuration is correct, see the log for more infos about the error.");
        }
    }

    /**
     * Builds a FeatureStore for the output Feature.
     *
     * @param store
     * @param schema
     * @return
     * @throws IOException
     */
    protected FeatureStore<SimpleFeatureType, SimpleFeature> createOutputWriter(DataStore store, SimpleFeatureType schema, Transaction transaction) throws IOException {
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
                if (!typeName.equals(destTypeName) && typeName.equalsIgnoreCase(destTypeName)) {
                    destTypeName = typeName;
                }
            }
        }
        FeatureStore<SimpleFeatureType, SimpleFeature> result = (FeatureStore<SimpleFeatureType, SimpleFeature>) store.getFeatureSource(destTypeName);
        result.setTransaction(transaction);
        return result;
    }

    /**
     * Builds a Feature instance to be written on output.
     *
     * @param builder
     * @param sourceFeature
     * @return
     */
    protected SimpleFeature buildFeature(SimpleFeatureBuilder builder, SimpleFeature sourceFeature, Map<String, String> mappings, DataStore srcDataStore) {
        for (AttributeDescriptor ad : builder.getFeatureType().getAttributeDescriptors()) {
            String attribute = ad.getLocalName();
            builder.set(attribute, getAttributeValue(sourceFeature, attribute, mappings));
        }
        SimpleFeature smf = null;
        if (srcDataStore != null && srcDataStore instanceof JDBCDataStore && isPkGenerated == false) {
            if (pks == null) {
                SimpleFeatureType schema = builder.getFeatureType();
                JDBCDataStore jdbcDS = ((JDBCDataStore) srcDataStore);
                Connection cx = null;
                PrimaryKeyFinder pkFinder = jdbcDS.getPrimaryKeyFinder();
                try {
                    cx = jdbcDS.getDataSource().getConnection();
                    PrimaryKey pk = pkFinder.getPrimaryKey(jdbcDS, jdbcDS.getDatabaseSchema(), schema.getTypeName(), cx);
                    List<PrimaryKeyColumn> pkc = new ArrayList<PrimaryKeyColumn>();
                    pks = pk.getColumns();
                    for(PrimaryKeyColumn el : pks){
                        if(el instanceof NonIncrementingPrimaryKeyColumn){
                            isPkGenerated = false;
                        }
                        else{
                            isPkGenerated = true;
                            break;
                        }
                    }
                
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new IllegalStateException("Error Occurred while search for the PK");
                } finally {
                    if (cx != null) {
                        try {
                            cx.close();
                        } catch (SQLException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }
            if(!isPkGenerated){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (PrimaryKeyColumn el : pks) {
                    if (!first) {
                        sb.append(".");
                    }
                    first = false;
                    sb.append(sourceFeature.getAttribute(el.getName()));
                }
                String fid = sb.toString();
                smf = builder.buildFeature(fid);
                Map map = smf.getUserData();
                map.put(Hints.USE_PROVIDED_FID, true);
                return smf;
            }
        }
        return builder.buildFeature(null);
    }

    /**
     * Compare input and output schemas for different case mapping in attribute names.
     *
     * @param destSchema
     * @param schema
     * @return
     */
    protected Map<String, String> compareSchemas(SimpleFeatureType destSchema, SimpleFeatureType schema) {
        Map<String, String> diffs = new HashMap<String, String>();
        for (AttributeDescriptor ad : destSchema.getAttributeDescriptors()) {
            String attribute = ad.getLocalName();
            if (schema.getDescriptor(attribute) == null) {
                for (String variant : getNameVariants(attribute)) {
                    if (schema.getDescriptor(variant) != null) {
                        diffs.put(attribute, variant);
                        break;
                    }
                }
            }
        }
        return diffs;
    }

    /**
     * Creates the destination DataStore, from the configuration connection parameters.
     *
     * @return
     * @throws IOException
     * @throws ActionException
     */
    protected DataStore createOutputDataStore() throws IOException, ActionException {
        updateTask("Connecting to output DataStore");
        return createDataStore(configuration.getOutputFeature().getDataStore());
    }

    /**
     * Updates the import progress ( progress / total )
     * for the listeners.
     *
     * @param progress
     * @param total
     * @param message
     */
    protected void updateImportProgress(int progress, int total, String message) {
        float f = total == 0 ? 0 : (float) progress / total;
        listenerForwarder.progressing(f, message);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Importing data: " + progress + "/" + total);
        }
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
    protected EventObject buildOutputEvent() throws FileNotFoundException, ActionException {
        updateTask("Building output event");
        FileOutputStream outStream = null;
        try {
            File outputDir = getTempDir();
            File outputFile = new File(outputDir.getAbsolutePath()
+ File.separator + "output.xml");

//                    new File(outputDir, "output.xml");
            outStream = new FileOutputStream(outputFile);
            configuration.getOutputFeature().toXML(outStream);
            updateTask("Output event built");
            return new FileSystemEvent(outputFile, FileSystemEventType.FILE_ADDED);
        } catch (Exception e) {
            throw new ActionException(this, "Error writing output event");
        } finally {
            IOUtils.closeQuietly(outStream);
        }
    }

    protected void closeResource(DataStore dataStore) {
        if (dataStore != null) {
            try {
                dataStore.dispose();
            } catch (Throwable t) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error closing datastore connection");
                }
            }
        }
    }

    protected void closeResource(Transaction transaction) {
        if (transaction != null) {
            try {
                transaction.close();
            } catch (Throwable t) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error closing transaction");
                }
            }
        }
    }

    protected void failAction(String message) throws ActionException {
        failAction(message, null);
    }

    protected void failAction(String message, Throwable t) throws ActionException {

        if (LOGGER.isErrorEnabled()) {
            // checkme: this check may be useless: null checks may be performed by the log libs
            if(t != null)
                LOGGER.error(message, t);
            else
                LOGGER.error(message);
        }
        if (!configuration.isFailIgnored()) {
            // checkme: the flowmanager should already be dealing with failIgnored checks and notification to the listenerForwarder
            final ActionException e = new ActionException(this, message, t);
            listenerForwarder.failed(e);
            throw e;
        }
    }

    /**
     * Builds an attribute value to be written on output.
     * @param sourceFeature source used to get values to write
     * @param attributeName name of the attribute in the output feature
     * @return
     */
    protected Object getAttributeValue(SimpleFeature sourceFeature, String attributeName, Map<String, String> mappings) {
        // gets mapping for renamed attributes
        if (configuration.getAttributeMappings().containsKey(attributeName)) {
            attributeName = configuration.getAttributeMappings().get(attributeName).toString();
        } else if (mappings.containsKey(attributeName)) {
            attributeName = mappings.get(attributeName);
        }
        return sourceFeature.getAttribute(attributeName);
    }

    /**
     * Returns case variants for the given name.
     *
     * @param name
     * @return
     */
    protected String[] getNameVariants(String name) {
        return new String[]{name.toLowerCase(), name.toUpperCase()};
    }

    /**
     * Creates a DataStore from the given connection parameters.
     *
     * @param connect
     * @return
     * @throws IOException
     * @throws ActionException
     */
    protected DataStore createDataStore(Map<String, Serializable> connect) throws IOException, ActionException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("DataStore connection parameters:");
            for (String connectKey : connect.keySet()) {
                Serializable value = connect.get(connectKey);
                if(connectKey.equalsIgnoreCase("pw") || 
                        connectKey.equalsIgnoreCase("password") ||
                        connectKey.equalsIgnoreCase("passwd"))
                    value = "***HIDDEN***";
                LOGGER.info("     " + connectKey + " -> " + value);
            }
        }
        DataStore dataStore = DataStoreFinder.getDataStore(connect);
        if (dataStore == null) {
            failAction("Cannot connect to DataStore: wrong parameters");
        }
        return dataStore;
    }

	protected DataStore createSourceDataStore(FileSystemEvent fileEvent) throws IOException, ActionException {
		updateTask("Connecting to source DataStore");
		String fileType = getFileType(fileEvent);
		FeatureConfiguration sourceFeature = configuration.getSourceFeature();
        if(sourceFeature == null){
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
	 * Builds a Query Object for the source Feature.
	 *
	 * @param sourceStore
	 * @return
	 * @throws IOException
	 */
	protected Query buildSourceQuery(DataStore sourceStore) throws Exception {
		Query query = new Query();
		query.setTypeName(configuration.getSourceFeature().getTypeName());
		query.setCoordinateSystem(configuration.getSourceFeature().getCoordinateReferenceSystem());
		query.setFilter(buildFilter());
		return query;
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
	protected FeatureStore<SimpleFeatureType, SimpleFeature> createSourceReader(
			DataStore sourceDataStore, final Transaction transaction,
			Query query) throws IOException {
		FeatureStore<SimpleFeatureType, SimpleFeature> featureReader =
				(FeatureStore<SimpleFeatureType, SimpleFeature>) sourceDataStore
				.getFeatureSource(query.getTypeName());
		featureReader.setTransaction(transaction);
		return featureReader;
	}
    
	public static String getFileType(FileSystemEvent event) {
		return FilenameUtils.getExtension(event.getSource().getName()).toLowerCase();
	}
}
