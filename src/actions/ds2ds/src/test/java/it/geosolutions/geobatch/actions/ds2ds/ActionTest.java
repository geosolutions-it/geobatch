/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2013 GeoSolutions S.A.S.
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
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.geotools.referencing.CRS;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ActionTest extends BaseDs2DsTest {
        
        private final static Logger LOGGER = LoggerFactory.getLogger(ActionTest.class);
    	
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
			configuration.getOutputFeature().getDataStore().putAll(dataStoreParameters);
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
	
	@Test
	public void testActionReturnsEventsIfCompletedTest() {
		try {
			Queue<EventObject> result = executeAction("xml");			
			assertNotNull(result);
			assertFalse( result.isEmpty() );			
			assertTrue(result.peek() instanceof FileSystemEvent);
			FileSystemEvent event = (FileSystemEvent) result.peek();
			FeatureConfiguration output = FeatureConfiguration.fromXML(new FileInputStream(event.getSource()));
			assertNotNull(output);
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
        public void testDataIsImportedFromShapefileWithFilter() { 
                try {
                        configuration.setEcqlFilter("LAND_KM < 3000 OR STATE_NAME = 'California'");
                        executeAction("shp");                   
                        assertTrue(getRecordCountFromDatabase("test") == 3);                     
                } catch (ActionException e) {
                        fail("Action failure in execution: " + e.getLocalizedMessage());
                } catch (URISyntaxException e) {
                        fail("Failure in loading resource file: " + e.getLocalizedMessage());
                } catch (SQLException e) {
                        fail("Failure in testing the output on database: " + e.getLocalizedMessage());
                }               
        }
	
	@Test
        public void testWrongCqlFilterSpecification() { 
                try {
                        configuration.setEcqlFilter("AND AND AND");
                        executeAction("shp");
                } catch (ActionException e) {
                        assertTrue(e.getMessage().toLowerCase().contains("cql"));
                } catch (URISyntaxException e) {
                        fail("Failure in loading resource file: " + e.getLocalizedMessage());
                }               
        }
	
	@Test
        public void testEmptyCqlFilterSpecification() { 
                try {
                        configuration.setEcqlFilter("");
                        executeAction("shp");
                        assertTrue(getRecordCountFromDatabase("test") == 49);
                } catch (ActionException e) {
                        assertTrue(e.getLocalizedMessage().startsWith("Unable to produce the output: Error while cql filter compilation."));
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
        public void testPurgeDataWithFilter() {   
                try {
                                        
                        executeAction("shp");                   
                        long firstRun = getRecordCountFromDatabase("test");
                        
                        configuration.setEcqlFilter("LAND_KM < 3000 OR STATE_NAME = 'California'");
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
        public void testPurgeAllDataWithFilter() {   
                try {
                                        
                        executeAction("shp");                   
                        long firstRun = getRecordCountFromDatabase("test");
                        
                        configuration.setEcqlFilter("LAND_KM < 3000 OR STATE_NAME = 'California'");
                        configuration.setPurgeData(false);
                        configuration.setForcePurgeAllData(true);
                        executeAction("shp");
                        assertEquals(getRecordCountFromDatabase("test"), 3);
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
	}
