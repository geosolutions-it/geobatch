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

import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkInsertConfiguration;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkActionInsertTest extends GeonetworkAbstractTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkActionInsertTest.class);
    
    public GeonetworkActionInsertTest() {
    }

    
    @Test
    public void testInsertPureMetadata() throws Exception {
        if( ! runIntegrationTest ) return;
        removeAllMetadata();
        
        GeonetworkInsertConfiguration cfg = createConfiguration();
        cfg.setOnlyMetadataInput(true);
        cfg.addPrivileges(0, "012345");
        cfg.addPrivileges(1, "012345");
        cfg.addPrivileges(2, "012345");
        cfg.addPrivileges(3, "012345");
        cfg.addPrivileges(4, "012345");
        
        File file = loadFile("metadata.xml");
        assertNotNull(file);
        
        FileSystemEvent event = new FileSystemEvent(file, FileSystemEventType.FILE_ADDED);
        Queue<FileSystemEvent> queue = new LinkedList<FileSystemEvent>();
        queue.add(event);
        
        GeonetworkAction action = new GeonetworkAction(cfg);
        Queue<FileSystemEvent> retQueue = action.execute(queue);

        assertEquals(0, retQueue.size());
    }

    @Test
    public void testInsertRequest() throws Exception {
        if( ! runIntegrationTest ) return;
        removeAllMetadata();
        
        GeonetworkInsertConfiguration cfg = createConfiguration();
        cfg.setOnlyMetadataInput(false);
        
        File file = loadFile("request.xml");
        assertNotNull(file);
        
        FileSystemEvent event = new FileSystemEvent(file, FileSystemEventType.FILE_ADDED);
        Queue<FileSystemEvent> queue = new LinkedList<FileSystemEvent>();
        queue.add(event);
        
        GeonetworkAction action = new GeonetworkAction(cfg);
        Queue<FileSystemEvent> retQueue = action.execute(queue);

        assertEquals(0, retQueue.size());
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
