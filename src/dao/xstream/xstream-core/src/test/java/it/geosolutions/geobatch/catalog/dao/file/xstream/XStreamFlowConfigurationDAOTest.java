/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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



package it.geosolutions.geobatch.catalog.dao.file.xstream;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.event.consumer.file.FileEventRule;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * 
 * @author etj
 */
public class XStreamFlowConfigurationDAOTest extends TestCase {

    private ClassPathXmlApplicationContext context;

    public XStreamFlowConfigurationDAOTest() {
    }

    @Before
    public void setUp() throws Exception {
        this.context = new ClassPathXmlApplicationContext();
    }


    private Alias createAlias() {
        AliasRegistry aliasRegistry = new AliasRegistry();
        Alias alias = new Alias();
        alias.setAliasRegistry(aliasRegistry);
        return alias;
    }


    @Test
    public void testDAO() throws IOException {

//        Resource resource = context.getResource("data");
//        File dir = resource.getFile();
//        assertTrue(dir.exists());
//
//        File file = new File(dir, "flow1.xml");
//        assertTrue(file.exists());
//
//        XStreamFlowConfigurationDAO dao = new XStreamFlowConfigurationDAO(dir.getAbsolutePath(), createAlias());
//        FileBasedFlowConfiguration fbfc = dao.find("flow1", false);
//
//        assertNotNull(fbfc);
//
//        assertEquals(fbfc.getId(), "flow1id");
//        assertEquals(fbfc.getName(), "flow1name");
//        assertEquals(fbfc.getDescription(), "flow1desc");
//
//        FileBasedEventGeneratorConfiguration fbegc = (FileBasedEventGeneratorConfiguration) fbfc
//                .getEventGeneratorConfiguration();
//        assertNotNull(fbegc);
//
//        FileBasedEventConsumerConfiguration fbecc = (FileBasedEventConsumerConfiguration) fbfc
//                .getEventConsumerConfiguration();
//        assertNotNull(fbecc);
//
//        List<? extends ActionConfiguration> lac = fbecc.getActions();
//        assertNotNull(lac);
//        for (ActionConfiguration actionConfiguration : lac) {
//            System.out.println(actionConfiguration);
//        }
//        assertEquals(1, lac.size());
//
//        List<FileEventRule> lfer = fbecc.getRules();
//        assertNotNull(lfer);
//        for (FileEventRule fileEventRule : lfer) {
//            System.out.println(fileEventRule);
//        }
//
//        assertEquals(1, lfer.size());

     
    }

}