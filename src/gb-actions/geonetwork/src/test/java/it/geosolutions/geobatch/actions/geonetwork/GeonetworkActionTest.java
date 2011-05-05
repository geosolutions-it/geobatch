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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Queue;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkActionTest extends TestCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkActionTest.class);

    boolean runIntegrationTest = true;
    
    public GeonetworkActionTest() {
    }

//    @Before 
    public void setUp() throws Exception {
        super.setUp();
        LOGGER.info("====================> " + getName());
    }
    
    @Test
    public void testInsertPureMetadata() throws Exception {
        if( ! runIntegrationTest ) return;
        
        GeonetworkInsertConfiguration cfg = createConfiguration();
        cfg.setOnlyMetadataInput(true);
        
        File file = loadFile("metadata.xml");
        assertNotNull(file);
        
        FileSystemEvent event = new FileSystemEvent(file, FileSystemEventType.FILE_ADDED);
        Queue<FileSystemEvent> queue = new ArrayDeque<FileSystemEvent>();
        queue.add(event);
        
        GeonetworkAction action = new GeonetworkAction(cfg);
        Queue<FileSystemEvent> retQueue = action.execute(queue);

        assertEquals(0, retQueue.size());
    }

    @Test
    public void testInsertRequest() throws Exception {
        if( ! runIntegrationTest ) return;
        
        GeonetworkInsertConfiguration cfg = createConfiguration();
        cfg.setOnlyMetadataInput(false);
        
        File file = loadFile("request.xml");
        assertNotNull(file);
        
        FileSystemEvent event = new FileSystemEvent(file, FileSystemEventType.FILE_ADDED);
        Queue<FileSystemEvent> queue = new ArrayDeque<FileSystemEvent>();
        queue.add(event);
        
        GeonetworkAction action = new GeonetworkAction(cfg);
        Queue<FileSystemEvent> retQueue = action.execute(queue);

        assertEquals(0, retQueue.size());
    }

    protected GeonetworkInsertConfiguration createConfiguration() {
        GeonetworkInsertConfiguration cfg = new GeonetworkInsertConfiguration("GNIC", "TestGeoNetworkInsert", "test configuration");
        cfg.setWorkingDirectory("/tmp");
        
        cfg.setCategory("datasets");
        cfg.setGeonetworkServiceURL("http://localhost:8080/geonetwork");
        cfg.setGroup("1"); // group 1 is usually "all"
        cfg.setLoginPassword("admin");
        cfg.setLoginUsername("admin");
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
