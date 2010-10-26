/*
 *  Copyright (C) 2007 - 2010 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.geoserver.egeos.connection;

import it.geosolutions.geobatch.egeos.connection.CSWConn;
import it.geosolutions.geobatch.egeos.connection.EOPSender;
import it.geosolutions.geobatch.egeos.logic.EOProcessor;
import it.geosolutions.geobatch.egeos.types.dest.PlatformRO;
import it.geosolutions.geobatch.egeos.types.dest.SARProductRO;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class EOPSenderTest extends TestCase {
    private final static Logger LOGGER = Logger.getLogger(EOPSenderTest.class);

    private static final URL serviceURL;

    static {
        URL tmpurl = null;
        try {
             tmpurl = new URL("http://localhost:8080/ergorr/webservice");
        } catch (MalformedURLException ex) {
            LOGGER.error("Error initializing URL");
        }
        serviceURL = tmpurl;
    }
   
    public void testEOPSender() throws Exception {
        File dir = new ClassPathResource("testEOP").getFile();
        assertNotNull("Could not find test dir");

        CSWConn conn = new CSWConn(serviceURL);

        EOProcessor processor = new EOProcessor(dir);
        EOPSender sender = new EOPSender(processor);
        sender.setServiceURL(serviceURL);

        sender.run();

        PlatformRO platform = processor.getPlatform();
        assertTrue("Platform not in Registry", sender.existPlatform(platform));

        SARProductRO sarp = processor.getSARProduct();
        assertNotNull("SARP not in Registry", conn.getById(sarp.getURN()));

        // cleanup
        int deletedItems = conn.delete(platform.getURN(), CSWConn.DELETE_EXTRINSIC_OBJECT);
        assertEquals("Expected deletion of Platform and association to SARP", 2, deletedItems);
        assertNull("Platform not deleted", conn.getById(platform.getURN()));
//        assertNull("SARP not cascaded", conn.getById(sarp.getURN())); /// ???
        assertEquals(1, conn.delete(sarp.getURN(), CSWConn.DELETE_EXTRINSIC_OBJECT));

    }

}
