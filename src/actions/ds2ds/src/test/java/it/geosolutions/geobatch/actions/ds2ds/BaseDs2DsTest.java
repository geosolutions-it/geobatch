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

import static org.junit.Assert.*;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.junit.Before;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import org.junit.Rule;
import org.junit.rules.TestName;

abstract public class BaseDs2DsTest {

    @Rule
    public TestName _testName = new TestName();

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseDs2DsTest.class);
    
	protected static final Map<String,Serializable> dataStoreParameters = new HashMap<String, Serializable>();
	protected static final String dbName="mem:test;DB_CLOSE_DELAY=-1";
	static {
		dataStoreParameters.put("dbtype", "h2");
		dataStoreParameters.put("database", dbName);
	}
	
	protected static final Map<String,Serializable> sourceStoreParameters = new HashMap<String, Serializable>();
	protected static final String dbNameSource="mem:source;DB_CLOSE_DELAY=-1";
	static {
		sourceStoreParameters.put("dbtype", "h2");
		sourceStoreParameters.put("database", dbNameSource);
	}
	
	protected Ds2dsConfiguration configuration = null;
	protected Ds2dsAction action = null;
	
	protected List<String> receivedEvents = new ArrayList<String>();
	
	protected IProgressListener listener = new IProgressListener() {
		
		@Override
		public void terminated() {
			receivedEvents.add("terminated");				
		}
		
		@Override
		public void started() {
			receivedEvents.add("started");				
		}
		
		@Override
		public void setTask(String currentTask) {
							
		}
		
		@Override
		public void setProgress(float progress) {
							
		}
		
		@Override
		public void resumed() {
			receivedEvents.add("resumed");			
		}
		
		@Override
		public void progressing() {
			receivedEvents.add("progressing");		
		}
		
		@Override
		public void paused() {
			receivedEvents.add("paused");			
		}
		
		@Override
		public String getTask() {				
			return null;
		}
		
		@Override
		public float getProgress() {
			return 0;
		}
		
		@Override
		public Identifiable getOwner() {				
			return null;
		}
		
		@Override
		public void failed(Throwable exception) {
			receivedEvents.add("failed");			
		}
		
		@Override
		public void completed() {
			receivedEvents.add("completed");			
		}
	};
	
	@Before
	public void setUp() throws ActionException, URISyntaxException, SQLException {
        LOGGER.warn("");
        LOGGER.warn("======================================================================");
        LOGGER.warn("=== TEST " + _testName.getMethodName());
        LOGGER.warn("======================================================================");
        LOGGER.warn("");

		configuration = new Ds2dsConfiguration("id", "name", "description");
				
		dropAllDb(dbName);
		dropAllDb(dbNameSource);
		
		// prepare source H2 db		
		configuration.getOutputFeature().getDataStore().putAll(sourceStoreParameters);		
		executeAction("shp");
		configuration.getOutputFeature().setTypeName("other");
		executeAction("shp");
		
		configuration = new Ds2dsConfiguration("id", "name", "description");						
		configuration.getOutputFeature().getDataStore().putAll(dataStoreParameters);				
		
		receivedEvents.clear();		
	}
	
	protected void assertNotReceivedEvent(String event) {
		assertFalse(receivedEvents.contains(event));
	}

	protected void assertReceivedEvent(String event) {
		assertTrue(receivedEvents.contains(event));
	}
	
	protected CoordinateReferenceSystem getCrsFromDb(String table) throws IOException {
		DataStore dataStore = null;
		try {
			dataStore = DataStoreFinder.getDataStore(dataStoreParameters);
			SimpleFeatureType schema = dataStore.getSchema(table);
			return schema.getCoordinateReferenceSystem();
		} finally {
			if(dataStore != null) {
				dataStore.dispose();
			}			
		}
		
	}


	protected Queue<EventObject> executeAction(String fileType) throws ActionException, URISyntaxException {
		return executeAction("test", fileType);
	}
	
	protected Queue<EventObject> executeAction(String fileName, String fileType)
			throws ActionException, URISyntaxException {
		action = new Ds2dsAction(configuration);
		action.setFailIgnored(false);
		File temp;
		try {
			temp = File.createTempFile("prefix", "suffix");
			temp.delete();
			temp.mkdir();
			action.setTempDir(temp);
		} catch (IOException e) {
			throw new ActionException(action, "Error creating temp folder");
		}
		
		
		action.addListener(listener);
		Queue<EventObject> result = action.execute(getEvents(fileName, fileType));
		assertReceivedEvent("completed");
		assertNotReceivedEvent("failed");	
		return result;
	}
	
	protected Queue<EventObject> getEvents(String fileName, String fileType) throws URISyntaxException {
		Queue<EventObject> events = new LinkedList<EventObject>();
		FileSystemEvent event = new FileSystemEvent(getResourceFile("inputs/"+fileName+"."+fileType) , FileSystemEventType.FILE_ADDED);
		events.add(event);
		return events;
	}

	protected File getResourceFile(String resource) throws URISyntaxException {
		return new File(this.getClass().getResource("/test-data/"+resource).toURI());
	}
	
	protected long getRecordCountFromDatabase(String tableName) throws SQLException {
		return (Long) executeOnDb(dbName, "select count(*) from \"" + tableName +"\"", true);				
	}

	protected String getAttributeFromTable(String tableName, String attributeName) {
		try {
			return (String) executeOnDb(dbName,"select \""+attributeName+"\" from \"" + tableName +"\"", true);
		} catch (SQLException e) {
			return "";
		}				
	}
	
	/**
	 * This utility method is useful when a test is needed between different imports using different reprojections
	 * 
	 * @return the geometry of the feature with the attribute STATE_NAME = Wyoming, null if the feature is not found
	 */
	protected Geometry getExampleGeomFromTableTest() {
	    Transaction t = new DefaultTransaction();
	    DataStore ds = null;
	    FeatureReader<SimpleFeatureType, SimpleFeature> fr = null;
	    Query q = new Query();
	    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(); 
	    Filter f = ff.equals(ff.property("STATE_NAME"), ff.literal("Wyoming"));
	    q.setFilter(f);
	    q.setTypeName("test");
	    try {
	            ds = DataStoreFinder.getDataStore(dataStoreParameters);
                    fr = ds.getFeatureReader(q, t);
                    return (Geometry)fr.next().getDefaultGeometry();
            } catch (Exception e) {
                    return null;
            } finally{
                try {
                    t.close();
                } catch (IOException e) {
                    LOGGER.error("Error while closing transaction...");
                }
                try {
                    fr.close();
                } catch (IOException e) {
                    LOGGER.error("Error while closing feature reader...");
                }
                ds.dispose();
            }
        }
	
	protected void dropAllDb(String databaseName) throws SQLException {
		executeOnDb(databaseName, "drop all objects", false);		
	}

	protected Object executeOnDb(String databaseName, String sql, boolean forRead) throws SQLException {
		Connection db = null; 
		Statement stmt = null;
		ResultSet rs = null;
		Object result = null;
		try {
			db = DriverManager.getConnection("jdbc:h2:" + databaseName);			
			stmt = db.createStatement();	
			if(forRead) {
				rs = stmt.executeQuery(sql);
			} else {
				stmt.execute(sql);
			}
			if(rs != null && rs.next()) {
				result = rs.getObject(1);
			}
			return result;
		} finally {
			if(rs != null) {
				rs.close();
			}
			if(stmt != null) {
				stmt.close();
			}
			if(db != null) {
				db.close();
			}
		}
	}

	protected Connection getDatabaseConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:" + dbName);		
	}

	
}
