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

import com.thoughtworks.xstream.XStream;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.tools.file.IOUtils;
import it.geosolutions.geobatch.xstream.Alias;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkActionLoadTest extends TestCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkActionLoadTest.class);

    boolean runIntegrationTest = false;
    
    public GeonetworkActionLoadTest() {
    }

//    @Before 
    public void setUp() throws Exception {
        super.setUp();
        LOGGER.info("====================> " + getName());
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
        
        cfg.setCategory("datasets");
        cfg.setGeonetworkServiceURL("http://localhost:8080/geonetwork");
        cfg.setGroup("1"); // group 1 is usually "all"
        cfg.setLoginUsername("admin");
        cfg.setLoginPassword("admin");
        cfg.setOnlyMetadataInput(true);
        cfg.setStyleSheet("_none_");
        cfg.setValidate(Boolean.FALSE);
        return cfg;
    }
    
    private File loadFile(String name) {        
        try {
            URL url = this.getClass().getClassLoader().getResource(name);
            if(url == null)
                throw new IllegalArgumentException("Cant get file '"+name+"'");
            File file = new File(url.toURI());
            return file;
        } catch (URISyntaxException e) {
            LOGGER.error("Can't load file " + name + ": " + e.getMessage(), e);
            return null;
        }    
    }
    
}
