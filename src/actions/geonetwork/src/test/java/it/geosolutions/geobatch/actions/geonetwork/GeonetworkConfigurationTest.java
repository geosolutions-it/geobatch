/*
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.geonetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkInsertConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.xstream.Alias;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkConfigurationTest extends GeonetworkAbstractTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkConfigurationTest.class);

    
    public GeonetworkConfigurationTest() {
    }
    
    @Test
    public void testActionFile() throws Exception {
        try {            
            GeonetworkInsertConfiguration cfg = loadActionConfig("geonetworkAction.xml");
        } catch (Exception e) {
            LOGGER.error("Error instantiating ActionConfig", e);
            fail("Error instantiating ActionConfig");
        } 
    }
    
    @Test
    public void testBadActionFile() throws Exception {
        try {            
            GeonetworkInsertConfiguration cfg = loadActionConfig("geonetworkAction_bad.xml");
            fail("Bad config not recognized");
        } catch (Exception e) {
            LOGGER.info("Exception trapped properly");
        } 
    }
        
    private GeonetworkInsertConfiguration loadActionConfig(String filename) throws Exception {
        File file = loadFile(filename);
        assertNotNull(file);

        AliasRegistry aliasRegistry = new AliasRegistry();
        Alias alias = new Alias();
        alias.setAliasRegistry(aliasRegistry);
        new GeonetworkAliasRegistrar(aliasRegistry);
        XStream xstream = new XStream();
        alias.setAliases(xstream);

        InputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            return (GeonetworkInsertConfiguration) xstream.fromXML(new BufferedInputStream(inStream));
        } finally {
            if (inStream != null) {
                IOUtils.closeQuietly(inStream);
            }
        }
    }

    private void dumpActionConfig(GeonetworkInsertConfiguration cfg) {

        AliasRegistry aliasRegistry = new AliasRegistry();
        Alias alias = new Alias();
        alias.setAliasRegistry(aliasRegistry);
        new GeonetworkAliasRegistrar(aliasRegistry);
        XStream xstream = new XStream();
        alias.setAliases(xstream);
        
        String xml = xstream.toXML(cfg);
        LOGGER.info("ActionConfig xml " + xml);
    }

    
    public void testDump() {
    
        GeonetworkInsertConfiguration cfg = createConfiguration();
        cfg.addPrivileges(42, "012345");
        
        dumpActionConfig(cfg);
    }
    
    
    protected GeonetworkInsertConfiguration createConfiguration() {
        GeonetworkInsertConfiguration cfg = new GeonetworkInsertConfiguration("GNIC", "TestGeoNetworkInsert", "test configuration");
        cfg.setWorkingDirectory("/tmp");

        cfg.setGeonetworkServiceURL(gnServiceUrl);
        cfg.setLoginUsername(gnUsername);
        cfg.setLoginPassword(gnPassword);

        cfg.setCategory("datasets");
        cfg.setGroup("1"); // group 1 is usually "all"
        cfg.setOnlyMetadataInput(true);
        cfg.setStyleSheet("_none_");
        cfg.setValidate(Boolean.FALSE);
        return cfg;
    }
        
}
