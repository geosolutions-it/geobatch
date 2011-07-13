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

import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.settings.GBSettings;
import it.geosolutions.geobatch.settings.GBSettingsDAO;
import it.geosolutions.geobatch.settings.JAISettings;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * TODO: in order to test this stuff, we have to load all the available appcontext, so that xstream
 * registrars will set up all the needed aliases.
 * 
 * @author etj
 */
public class GBSettingsXStreamDAOTest extends TestCase {
    private Logger LOGGER = LoggerFactory.getLogger(GBSettingsXStreamDAOTest.class);

    private ClassPathXmlApplicationContext context;

    public GBSettingsXStreamDAOTest() {
    }

    @Before
    public void setUp() throws Exception {
        this.context = new ClassPathXmlApplicationContext("classpath*:applicationContext.xml");
    }

    private Alias createAlias() {
        AliasRegistry aliasRegistry = new AliasRegistry();
        Alias alias = new Alias();
        alias.setAliasRegistry(aliasRegistry);
        return alias;
    }

    @Test
    public void testDAO() throws IOException, Exception {
        GBSettingsDAO settingsDAO = CatalogHolder.getSettingsDAO();
        assertNotNull("settingsDAO not set", settingsDAO);

        GBSettings s1 = settingsDAO.find("UNK");
        assertNull(s1);

        GBSettings s2 = settingsDAO.find("JAI");
        assertNotNull("JAI config not loaded", s2);
        assertTrue(s2 instanceof JAISettings);
        
        JAISettings js = (JAISettings) s2;

        assertEquals(42.0f, js.getMemoryCapacity());
        assertTrue(js.isPngNative());
        assertFalse(js.isJpegNative());

        try {
            settingsDAO.find("BAD");
            fail("Untrapped error");
        } catch (Exception exception) {
            LOGGER.info("Exception properly trapped: " + exception.getMessage());
        }
    }


}