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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.referencing.CRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ActionTest {
	private static final Map<String,Serializable> dataStoreParameters = new HashMap<String, Serializable>();
	private static final String dbName="mem:test;DB_CLOSE_DELAY=-1";
	static {
		dataStoreParameters.put("dbtype", "h2");
		dataStoreParameters.put("database", dbName);
	}
	
	private static final Map<String,Serializable> sourceStoreParameters = new HashMap<String, Serializable>();
	private static final String dbNameSource="mem:source;DB_CLOSE_DELAY=-1";
	static {
		sourceStoreParameters.put("dbtype", "h2");
		sourceStoreParameters.put("database", dbNameSource);
	}
	
	private Ds2dsConfiguration configuration = null;
	private Ds2dsAction action = null; 	
	
	private List<String> receivedEvents = new ArrayList<String>();
	
	private IProgressListener listener = new IProgressListener() {
		
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
		configuration = new Ds2dsConfiguration("id", "name", "description");
				
		dropAllDb(dbName);
		dropAllDb(dbNameSource);
		
		// prepare source H2 db		
		configuration.getOutputFeature().getDataStore().setParameters(sourceStoreParameters);		
		executeAction("shp");
		configuration.getOutputFeature().setTypeName("other");
		executeAction("shp");
		
		configuration = new Ds2dsConfiguration("id", "name", "description");						
		configuration.getOutputFeature().getDataStore().setParameters(dataStoreParameters);				
		
		receivedEvents.clear();		
	}
	
	

	@Test
	public void testActionProcessShapefile() {
		try {			
			Queue<EventObject> result = executeAction("shp");
			assertNotNull(result);
			assertTrue(result.size() > 0);
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testWrongOutputConnection() {
		boolean failure = false;
		try {			
			executeAction("wrongoutput","xml");
		} catch (ActionException e) {
			failure = true;			
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}
		assertTrue(failure);
	}
	
	@Test
	public void testActionProcessZipFile() {
		try {
			Queue<EventObject> result = executeAction("zip");
			assertNotNull(result);
			assertTrue(result.size() > 0);
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testActionProcessWrongZipFile() {
		boolean failure = false;
		try {
			executeAction("fake","zip");
		} catch (ActionException e) {
			failure = true;
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}
		assertTrue(failure);
	}
	
	@Test
	public void testActionProcessXMLFile() {
		try {						
			configuration.getOutputFeature().getDataStore().setParameters(dataStoreParameters);
			Queue<EventObject> result = executeAction("xml");
			assertNotNull(result);
			assertTrue(result.size() > 0);
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testActionDoesntProcessUnknownFile() {
		boolean failure = false;
		try {
			executeAction("test","fake");					
		} catch (ActionException e) {
			failure = true;
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}
		assertTrue(failure);
		assertReceivedEvent("failed");
		assertNotReceivedEvent("completed");		
	}
	
	private void assertNotReceivedEvent(String event) {
		assertFalse(receivedEvents.contains(event));
	}

	private void assertReceivedEvent(String event) {
		assertTrue(receivedEvents.contains(event));
	}

	@Test
	public void testActionReturnsEventsIfCompleted() {
		try {
			Queue<EventObject> result = executeAction("xml");			
			assertNotNull(result);
			assertTrue(result.size() > 0);
			
			assertTrue(result.peek() instanceof FileSystemEvent);
			FileSystemEvent event = (FileSystemEvent) result.peek();
			FeatureConfiguration output = FeatureConfiguration.fromXML(new FileInputStream(event.getSource()));
			assertEquals("test",output.getTypeName());
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			fail("Failure in loading output file: " + e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testProgressionIsReceivedDuringImport() {
		try {
			executeAction("shp");			
			assertReceivedEvent("started");
			assertReceivedEvent("progressing");
			assertReceivedEvent("completed");
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testActionFailsWithWrongShapefile() {
		boolean failure = false;
	
		try {
			executeAction("wrong", "shp");			
			assertReceivedEvent("started");
			assertReceivedEvent("completed");
		} catch (ActionException e) {
			failure = true;
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}
		assertTrue(failure);
	}
	
	@Test
	public void testDataIsImportedFromShapefile() {	
		try {
			executeAction("shp");			
			assertTrue(getRecordCountFromDatabase("test") > 0);			
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		} catch (SQLException e) {
			fail("Failure in testing the output on database: " + e.getLocalizedMessage());
		}		
	}
	
	@Test
	public void testDataIsImportedFromXML() {	
		try {
			executeAction("xml");			
			assertTrue(getRecordCountFromDatabase("test") > 0);			
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		} catch (SQLException e) {
			fail("Failure in testing the output on database: " + e.getLocalizedMessage());
		}		
	}
	
	@Test
	public void testDataIsImportedFromZip() {	
		try {
			executeAction("zip");			
			assertTrue(getRecordCountFromDatabase("test") > 0);			
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		} catch (SQLException e) {
			fail("Failure in testing the output on database: " + e.getLocalizedMessage());
		}		
	}
	
	@Test
	public void testConfiguredTypeNameIsImportedFromXML() {	
		try {
			executeAction("other", "xml");			
			assertTrue(getRecordCountFromDatabase("other") > 0);			
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		} catch (SQLException e) {
			fail("Failure in testing the output on database: " + e.getLocalizedMessage());
		}		
	}
	
	@Test
	public void tesDataIsImportedWithConfiguredTypeName() {	
		try {
			configuration.getOutputFeature().setTypeName("renamed");
			executeAction("shp");			
			assertTrue(getRecordCountFromDatabase("renamed") > 0);			
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		} catch (SQLException e) {
			fail("Failure in testing the output on database: " + e.getLocalizedMessage());
		}		
	}
	
	@Test
	public void testAttributeRenaming() {	
		try {
			Map<String,Serializable> attributes=new HashMap<String,Serializable>();
			attributes.put("NEWNAME", "STATE_NAME");
			configuration.setAttributeMappings(attributes);
			executeAction("shp");			
			assertFalse(getAttributeFromTable("test","NEWNAME").equals(""));			
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}	
	}
	
	@Test
	public void testAttributesProjection() {	
		try {								
			configuration.setProjection(Arrays.asList("STATE_NAME","the_geom"));
			
			executeAction("shp");			
			assertFalse(getAttributeFromTable("test","STATE_NAME").equals(""));	
			assertTrue(getAttributeFromTable("test","STATE_ABBR").equals(""));	
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			fail("Failure in loading resource file: " + e.getLocalizedMessage());
		}	
	}
	
	@Test
	public void testPurgeData() {	
		try {
					
			executeAction("shp");			
			long firstRun = getRecordCountFromDatabase("test");
						
			executeAction("shp");
			assertEquals(getRecordCountFromDatabase("test"), 2 * firstRun);
			
			configuration.setPurgeData(true);	
			executeAction("shp");
			assertEquals(getRecordCountFromDatabase("test"), firstRun);
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (Exception e) {
			fail("Failure in decoding CRS: " + e.getLocalizedMessage());
		}		
	}
	
	@Test
	public void testOutputCRS() {	
		try {
			configuration.getOutputFeature().setCrs("EPSG:32632");
			executeAction("shp");			
			assertEquals(CRS.decode("EPSG:32632"),getCrsFromDb("test"));			
		} catch (ActionException e) {
			fail("Action failure in execution: " + e.getLocalizedMessage());
		} catch (Exception e) {
			fail("Failure in decoding CRS: " + e.getLocalizedMessage());
		}		
	}
	

	private CoordinateReferenceSystem getCrsFromDb(String table) throws IOException {
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



	

	private Queue<EventObject> executeAction(String fileType) throws ActionException, URISyntaxException {
		return executeAction("test", fileType);
	}
	
	private Queue<EventObject> executeAction(String fileName, String fileType)
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
	
	private Queue<EventObject> getEvents(String fileName, String fileType) throws URISyntaxException {
		Queue<EventObject> events = new LinkedList<EventObject>();
		FileSystemEvent event = new FileSystemEvent(getResourceFile("inputs/"+fileName+"."+fileType) , FileSystemEventType.FILE_ADDED);
		events.add(event);
		return events;
	}

	private File getResourceFile(String resource) throws URISyntaxException {
		return new File(this.getClass().getResource("/test-data/"+resource).toURI());
	}
	
	private long getRecordCountFromDatabase(String tableName) throws SQLException {
		return (Long) executeOnDb(dbName, "select count(*) from \"" + tableName +"\"", true);				
	}

	private String getAttributeFromTable(String tableName, String attributeName) {
		try {
			return (String) executeOnDb(dbName,"select \""+attributeName+"\" from \"" + tableName +"\"", true);
		} catch (SQLException e) {
			return "";
		}				
	}
	
	private void dropAllDb(String databaseName) throws SQLException {
		executeOnDb(databaseName, "drop all objects", false);		
	}



	private Object executeOnDb(String databaseName, String sql, boolean forRead) throws SQLException {
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


	private Connection getDatabaseConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:" + dbName);		
	}

	
}
