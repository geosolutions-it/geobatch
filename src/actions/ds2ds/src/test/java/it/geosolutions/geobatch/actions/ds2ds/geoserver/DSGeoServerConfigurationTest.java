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
package it.geosolutions.geobatch.actions.ds2ds.geoserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thoughtworks.xstream.XStream;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"../test-context.xml"})
public class DSGeoServerConfigurationTest {
	
	@Autowired
	private AliasRegistry aliasRegistry;
	
	private DSGeoServerConfiguration configuration = null;
	private static final XStream xstream = new XStream();
	
	@Configuration
	static class ContextConfiguration {}
	
	@Before
	public void setUp() {
		Alias alias=new Alias();
		alias.setAliasRegistry(aliasRegistry);
		alias.setAliases(xstream);
		
		configuration = new DSGeoServerConfiguration("id", "name", "description");
		Map<String,Serializable> parameters=new HashMap<String,Serializable>();
		parameters.put("dbtype", "h2");
		parameters.put("database", "mem:test");
		
		FeatureConfiguration featureConfig = new FeatureConfiguration();
		featureConfig.setDataStore(parameters);
		featureConfig.setTypeName("typeName1");
		featureConfig.setCrs("EPSG:4326");
		configuration.setFeatureConfiguration(featureConfig);
		configuration.setOperation("PUBLISH");
		configuration.setCreateNameSpace(true);
		configuration.setCreateDataStore(true);
		
	}
	
	@Test
	public void testSerialize() {		
		assertNotNull(xstream.toXML(configuration));
	}
	
	@Test
	public void testDeserialize() {		
		Object cfg = xstream.fromXML(xstream.toXML(configuration));
		assertNotNull(cfg);
		assertTrue(cfg instanceof DSGeoServerConfiguration);
		
		DSGeoServerConfiguration config = (DSGeoServerConfiguration) cfg;
		assertEquals("typeName1",config.getFeatureConfiguration().getTypeName());
		assertEquals("EPSG:4326",config.getFeatureConfiguration().getCrs());
		assertEquals("PUBLISH",config.getOperation());
		assertTrue(config.getCreateNameSpace());
		assertTrue(config.getCreateDataStore());
	}
}
