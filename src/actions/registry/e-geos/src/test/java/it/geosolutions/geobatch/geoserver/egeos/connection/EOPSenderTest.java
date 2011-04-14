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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class EOPSenderTest extends TestCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(EOPSenderTest.class);

    private static final URL serviceURL;

    static {
        URL tmpurl = null;
        try {
             tmpurl = new URL("http://localhost:8082/ergorr/webservice?wsdl");
        } catch (MalformedURLException ex) {
            LOGGER.error("Error initializing URL");
        }
        serviceURL = tmpurl;
    }
   
    public void testEOPSender() throws Exception {
        File dir = new ClassPathResource("testEOP").getFile();
        assertNotNull("Could not find test dir", dir);

        CSWConn conn = new CSWConn(serviceURL);
        
//        ServicesProcessor serviceProcessor = new ServicesProcessor("http://geoserver/wfs?SERVICE=wfs&amp;VERSION=1.1.1&amp;REQUEST=GetCapabilities", "http://geoserver/wcs?SERVICE=wcs&amp;VERSION=1.0.0&amp;REQUEST=GetCapabilities", "http://geoserver/wms?SERVICE=wms&amp;VERSION=1.1.0&amp;REQUEST=GetCapabilities");
//        assertEquals(3, serviceProcessor.size());
//        ServicesSender serviceSender = new ServicesSender(serviceProcessor); 
//        serviceSender.setServiceURL(serviceURL);
//        
//        serviceSender.run();
//        
//        ServiceRO service = ServicesProcessor.serviceRO("WFS", new URL("http://geoserver/wfs?SERVICE=wfs&amp;VERSION=1.1.1&amp;REQUEST=GetCapabilities"));
//        assertTrue("Service not in Registry", serviceSender.existsService(service));
//        
//        CollectionsProcessor collectionProcessor = new CollectionsProcessor("SAR:DATA");
//        CollectionsSender collectionsSender = new CollectionsSender(collectionProcessor);
//        collectionsSender.setServiceURL(serviceURL);
//        
//        assertNotNull("Collection not processed", collectionProcessor.getCollection("SAR:DATA"));
//        
//        CollectionRO sarData = collectionProcessor.getCollection("SAR:DATA");
//        sarData.setEnvelope(0.4567, -451144, 91.5629, 16.8238);
//        LinkedList<String> timeStamps = new LinkedList<String>();
//        timeStamps.add("2009-05-17T08:33:04.876");
//        sarData.setTimeStamp(timeStamps);
//        
//        collectionsSender.run();
//        
//        assertTrue("Collection not in Registry", collectionsSender.existsCollection(sarData));
//        
//        timeStamps.add("2010-02-19T18:12:05.234");
//        sarData.setTimeStamp(timeStamps);
//        
//        collectionsSender.updateCollection(sarData);
//        JAXBElement<ExtrinsicObjectType> pext = conn.getById(sarData.getURN());
//        assertNotNull("Collection not present into Registry", pext);
//        
//        ExtrinsicObjectType extobj = pext.getValue();
//        assertEquals(sarData.getURN(), extobj.getId());
//        assertEquals(sarData.getTypeName(), extobj.getObjectType());
//        assertEquals(2, extobj.getSlot().size());
//        
//        CollectionRO registrySarData = CollectionsProcessor.extobj2ro(extobj);
//        assertEquals(sarData.getEnvelope()[0], registrySarData.getEnvelope()[0]);
//        assertEquals(sarData.getEnvelope()[1], registrySarData.getEnvelope()[1]);
//        assertEquals(sarData.getEnvelope()[2], registrySarData.getEnvelope()[2]);
//        assertEquals(sarData.getEnvelope()[3], registrySarData.getEnvelope()[3]);
//        
//        assertEquals(sarData.getTimeStamps().size(), registrySarData.getTimeStamps().size());
//        
//        TransactionResponseType trt = conn.insert(sarData.getServiceAssociationXML("WCS"));
//        assertEquals(1, trt.getInsertResult().size());
        
        EOProcessor EOProcessor = new EOProcessor(dir);
        EOProcessor.setGmlBaseURI("http://alfa.net/shared/");
        EOPSender EOSender = new EOPSender(EOProcessor);
        EOSender.setServiceURL(serviceURL);

        EOSender.run();

        PlatformRO platform = EOProcessor.getPlatform();
        assertTrue("Platform not in Registry", EOSender.existsPlatform(platform));

        SARProductRO sarp = EOProcessor.getSARProduct();
        assertNotNull("SARP not in Registry", conn.getById(sarp.getURN()));

        // cleanup
        int deletedItems = conn.delete(platform.getURN(), CSWConn.DELETE_EXTRINSIC_OBJECT);
        assertEquals("Expected deletion of Platform and association to SARP", 2, deletedItems);
        assertNull("Platform not deleted", conn.getById(platform.getURN()));
//        assertNull("SARP not cascaded", conn.getById(sarp.getURN())); /// ???
        assertEquals(2, conn.delete(sarp.getURN(), CSWConn.DELETE_EXTRINSIC_OBJECT));

    }

}
