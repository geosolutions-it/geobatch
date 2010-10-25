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

import be.kzen.ergorr.model.wrs.WrsExtrinsicObjectType;
import it.geosolutions.geobatch.egeos.connection.CSWConn;
import it.geosolutions.geobatch.egeos.connection.EOPSender;
import it.geosolutions.geobatch.egeos.logic.EOProcessor;
import it.geosolutions.geobatch.egeos.types.dest.PlatformRO;
import java.io.File;
import java.net.URL;
import javax.xml.bind.JAXBElement;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class CSWConnTest extends TestCase {
    private final static Logger LOGGER = Logger.getLogger(CSWConnTest.class);
   
    public void testCSWConn() throws Exception {
        File dir = new ClassPathResource("testEOP").getFile();
        assertNotNull("Could not find test dir");

        EOProcessor processor = new EOProcessor(dir);
        processor.parsePackage();
        
        CSWConn conn = new CSWConn(new URL("http://localhost:8080/ergorr/webservice"));

        PlatformRO platform = processor.getPlatform();
        boolean px = conn.getById(platform.getURN()) != null;
        LOGGER.info("Platform exist: " +px+ " : "+ platform.getURN());

        if( ! px ) {
            LOGGER.info("Inserting platform : "+ platform.getURN());
            conn.insert(platform.getXML());
        }

        JAXBElement<WrsExtrinsicObjectType> rGet = conn.getById(platform.getURN());
        assertNotNull("Platform not in Registry", rGet);

        WrsExtrinsicObjectType extobj = rGet.getValue();
        LOGGER.info("Platform id:              " + extobj.getId());
        LOGGER.info("Platform lid:             " + extobj.getLid());
        LOGGER.info("Platform object type:     " + extobj.getObjectType());
        LOGGER.info("Platform object type urn: " + extobj.getObjectTypeUrn());
        LOGGER.info("Platform home:            " + extobj.getHome());
        LOGGER.info("Platform name:            " + extobj.getName());
        LOGGER.info("Platform status           " + extobj.getStatus());
//        LOGGER.info("Platform repo item name:  " + extobj.getRepositoryItem().getName());

        LOGGER.info("Deleting platform : "+ platform.getURN());

        int deleted = conn.delete(platform.getURN(), CSWConn.DELETE_EXTRINSIC_OBJECT);
        assertEquals("Bad delete info returned", 1, deleted);

        assertNull("Platform not deleted", conn.getById(platform.getURN()));
    }


}
