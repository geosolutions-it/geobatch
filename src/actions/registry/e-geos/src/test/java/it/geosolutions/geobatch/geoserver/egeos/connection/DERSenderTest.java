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
import it.geosolutions.geobatch.egeos.connection.DERSender;
import it.geosolutions.geobatch.egeos.connection.EOPSender;
import it.geosolutions.geobatch.egeos.logic.EOProcessor;
import it.geosolutions.geobatch.egeos.logic.SarDerivedProcessor;
import it.geosolutions.geobatch.egeos.logic.ShipProcessor;
import it.geosolutions.geobatch.egeos.types.dest.PlatformRO;
import it.geosolutions.geobatch.egeos.types.dest.SARProductRO;
import it.geosolutions.geobatch.egeos.types.dest.VesselRO;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBElement;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class DERSenderTest extends TestCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(DERSenderTest.class);

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
   
    public void testEOPSender_noSAR() throws Exception {
        File dirDER = new ClassPathResource("testDER").getFile();
        assertNotNull("Could not find DER test dir", dirDER);

        CSWConn conn = new CSWConn(serviceURL);

        // check platform is really not in the registry
        File dirEOP = new ClassPathResource("testEOP").getFile();
        assertNotNull("Could not find EOP test dir", dirEOP);
        EOProcessor eopProcessor = new EOProcessor(dirEOP);
        eopProcessor.parsePackage();
        //assertNull("Stale SARP in Registry", conn.getById(eopProcessor.getSARProduct().getURN()));

        ShipProcessor DSprocessor = new ShipProcessor(dirDER);
        DERSender sender = new DERSender(DSprocessor);
        sender.setServiceURL(serviceURL);

        try {
            sender.run();
            fail("IllegalStateException not detected");
        } catch (IllegalStateException e) {
            LOGGER.info("OK: Caught proper IllegalStateException");
        }
        
        SarDerivedProcessor SDFprocessor = new SarDerivedProcessor(dirDER);
        sender = new DERSender(SDFprocessor);
        sender.setServiceURL(serviceURL);

        try {
            sender.run();
            fail("IllegalStateException not detected");
        } catch (IllegalStateException e) {
            LOGGER.info("OK: Caught proper IllegalStateException");
        }
 
    }

    public void testEOPSender() throws Exception {
        File dirDER = new ClassPathResource("testDER").getFile();
        assertNotNull("Could not find DER test dir", dirDER);

        CSWConn conn = new CSWConn(serviceURL);
        TestContext testContext = insertTestProduct();
        
        try{
            ShipProcessor shipProcessor = new ShipProcessor(dirDER);
            DERSender shipSender = new DERSender(shipProcessor);
            shipSender.setServiceURL(serviceURL);
            shipSender.run();

            for (int i= 0; i < shipProcessor.size(); i++) {
                VesselRO vesselRO = shipProcessor.getRegistryObject(i);
                JAXBElement j = conn.getById(vesselRO.getURN());
                if(j == null)
                    LOGGER.warn("!!! Vessel not found " + vesselRO.getURN());
                assertNotNull("Vessel not inserted " + vesselRO.getURN(), j);
            }

            for (int i= 0; i < shipProcessor.size(); i++) {
                VesselRO vesselRO = shipProcessor.getRegistryObject(i);
//                assertNotNull("Vessel not inserted " + vesselRO.getURN(), conn.getById(vesselRO.getURN()));

                conn.delete(vesselRO.getURN(), CSWConn.DELETE_EXTRINSIC_OBJECT);
                assertNull("Vessel not deleted " + vesselRO.getURN(), conn.getById(vesselRO.getURN()));
            }

        } finally {
            removeTestProduct(testContext);
        }
    }

    protected TestContext insertTestProduct() throws Exception {
        File dir = new ClassPathResource("testEOP").getFile();
        assertNotNull("Could not find test dir", dir);

        CSWConn conn = new CSWConn(serviceURL);

        EOProcessor processor = new EOProcessor(dir);
        processor.parsePackage();
        EOPSender sender = new EOPSender(processor);
        sender.setServiceURL(serviceURL);

        LOGGER.info("INSERTING TEST PRODUCT " + processor.getSARProduct().getURN());

        sender.run();

        PlatformRO platform = processor.getPlatform();
        assertTrue("Platform not in Registry", sender.existsPlatform(platform));

        SARProductRO sarp = processor.getSARProduct();
        assertNotNull("SARP not in Registry", conn.getById(sarp.getURN()));

        return new TestContext(platform.getURN(), sarp.getURN());
    }

    protected void removeTestProduct(TestContext tc) throws Exception {
        // cleanup
        CSWConn conn = new CSWConn(serviceURL);

        LOGGER.info("REMOVING TEST PRODUCT " + tc.sarpURN);

        int deletedItems = conn.delete(tc.platformURN, CSWConn.DELETE_EXTRINSIC_OBJECT);
        assertEquals("Expected deletion of Platform and association to SARP", 2, deletedItems);
        assertNull("Platform not deleted", conn.getById(tc.platformURN));
//        assertNull("SARP not cascaded", conn.getById(sarp.getURN())); /// ???

        conn.delete(tc.sarpURN, CSWConn.DELETE_EXTRINSIC_OBJECT);

    }


    static protected class TestContext {
        String platformURN;
        String sarpURN;

        public TestContext(String platformURN, String sarpURN) {
            this.platformURN = platformURN;
            this.sarpURN = sarpURN;
        }
    }

}
