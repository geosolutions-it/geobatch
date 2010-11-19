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

package it.geosolutions.geobatch.geoserver.egeos.types;

import it.geosolutions.geobatch.egeos.connection.DERSender;
import it.geosolutions.geobatch.egeos.connection.OilSpillSender;
import it.geosolutions.geobatch.egeos.logic.EOProcessor;
import it.geosolutions.geobatch.egeos.logic.OilSpillProcessor;
import it.geosolutions.geobatch.egeos.logic.SarDerivedProcessor;
import it.geosolutions.geobatch.egeos.logic.ShipProcessor;
import it.geosolutions.geobatch.egeos.types.src.AbstractListPackage.PackedElement;
import it.geosolutions.geobatch.egeos.types.src.EarthObservation;
import it.geosolutions.geobatch.egeos.types.src.EarthObservationPackage;
import it.geosolutions.geobatch.egeos.types.src.OilSpill;
import it.geosolutions.geobatch.egeos.types.src.OilSpillPackage;
import it.geosolutions.geobatch.egeos.types.src.ShipDetectionPackage;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class OSNTest extends TestCase {

    
    public void testOSNPackage() throws Exception {
        File dir = new ClassPathResource("testOSN").getFile();
        assertNotNull("Could not find test dir");

        File[] pckList = dir.listFiles((FilenameFilter)new SuffixFileFilter("PCK.xml"));
        assertEquals(1, pckList.length);

        OilSpillPackage osnp = new OilSpillPackage(pckList[0]);
        assertEquals("7923_RS2_20100531_170041_0046_SCNA_HH_SCN_83688_0000_0000000", osnp.getEoProductId());
        assertEquals("7923_RS2_20100531_170041_0046_SCNA_HH_SCN_83688_0000_0000000_OSN", osnp.getPackageId());
        assertEquals("OS_NOTIFICATION", osnp.getPackageType());

        assertEquals(7, osnp.getList().size());

        for (PackedElement pe : osnp.getList()) {
            File osnFile = new File(dir, pe.getFilename());
            OilSpill osn = OilSpill.build(pe.getIdentifier(), osnFile);
            assertNotNull(osn.getTimestamp());
            assertNotNull(osn.getImageIdentifier());
            System.out.println(osn);
        }
    
    }

    public void testOilSpillProcessor_notifications() throws Exception {
        File dir = new ClassPathResource("testOSN").getFile();
        assertNotNull("Could not find test dir");

        OilSpillProcessor processor = new OilSpillProcessor(dir);
        OilSpillPackage pkg = processor.parsePackage();
        
        assertEquals("7923_RS2_20100531_170041_0046_SCNA_HH_SCN_83688_0000_0000000", pkg.getEoProductId());
        assertEquals("7923_RS2_20100531_170041_0046_SCNA_HH_SCN_83688_0000_0000000_OSN", pkg.getPackageId());
        assertEquals("OS_NOTIFICATION", pkg.getPackageType());
        assertEquals(OilSpillPackage.PackageType.OS_NOTIFICATION, pkg.getType());

        assertEquals(7, pkg.getList().size());

        for (int i = 0; i < pkg.getList().size(); i++) {
            PackedElement pe = pkg.getList().get(i);
            assertTrue(processor.getList().get(i).isEnvelopeSet());

            System.out.println(i+". "+pe);
            System.out.println("   " + processor.getRegistryObject(i));
        }

        /** **/
        URL serviceURL = new URL("http://localhost:8080/ergorr/webservice");
        
        OilSpillProcessor osnProcessor = new OilSpillProcessor(dir);
        osnProcessor.setBaseGMLURL(null /** myURL here **/);

        OilSpillSender osSender = new OilSpillSender(osnProcessor);
        osSender.setServiceURL(serviceURL);
        try {
            osSender.run();
            fail("IllegalStateException not detected");
        } catch (IllegalStateException e) {
        }
    }

    public void testOilSpillProcessor_warning() throws Exception {
        File dir = new ClassPathResource("testOSW").getFile();
        assertNotNull("Could not find test dir");

        OilSpillProcessor processor = new OilSpillProcessor(dir);
        OilSpillPackage pkg = processor.parsePackage();

        assertEquals("7922_RS2_20100527_055613_0045_SCNA_VV_SCN_83076_0000_0000000", pkg.getEoProductId());
        assertEquals("7922_RS2_20100527_055613_0045_SCNA_VV_SCN_83076_0000_0000000_OSW", pkg.getPackageId());
        assertEquals(OilSpillPackage.PackageType.OS_WARNING, pkg.getType());

        assertEquals(4, pkg.getList().size());

        for (int i = 0; i < pkg.getList().size(); i++) {
            PackedElement pe = pkg.getList().get(i);
            assertFalse(processor.getList().get(i).isEnvelopeSet());

            System.out.println(i+". "+pe);
            System.out.println("   " + processor.getRegistryObject(i));
        }

        /** **/
        URL serviceURL = new URL("http://localhost:8080/ergorr/webservice");
        
        OilSpillProcessor oswProcessor = new OilSpillProcessor(dir);
        oswProcessor.setBaseGMLURL(null /** myURL here **/);

        OilSpillSender osSender = new OilSpillSender(oswProcessor);
        osSender.setServiceURL(serviceURL);
        try {
            osSender.run();
            fail("IllegalStateException not detected");
        } catch (IllegalStateException e) {
        }
    }


    public void testShipProcessor() throws Exception {
        File dir = new ClassPathResource("testDER").getFile();
        assertNotNull("Could not find test dir");

        ShipProcessor processor = new ShipProcessor(dir);
        ShipDetectionPackage pkg = processor.parsePackage();

        assertEquals("7865_ASA_WSM_1PNACS20101014_191518_000000552093_00443_45086_0001.N1.00000", pkg.getEoProductId());
        assertEquals("7865_ASA_WSM_1PNACS20101014_191518_000000552093_00443_45086_0001.N1.00000_DER", pkg.getPackageId());

        assertEquals(22, pkg.getList().size());

        for (int i = 0; i < pkg.getList().size(); i++) {
            PackedElement pe = pkg.getList().get(i);
            System.out.println(i+". "+pe);
            System.out.println(i+". "+processor.getList().get(i));
            System.out.println("   " + processor.getRegistryObject(i));
        }

        
        /** **/
        URL serviceURL = new URL("http://localhost:8080/ergorr/webservice");
        
        ShipProcessor DSprocessor = new ShipProcessor(dir);
        DSprocessor.setGmlBaseURI(null /** myURL here **/);
        DERSender sender = new DERSender(DSprocessor);
        sender.setServiceURL(serviceURL);

//        try {
//            sender.run();
//            fail("IllegalStateException not detected");
//        } catch (IllegalStateException e) {
//        }
        
        SarDerivedProcessor SDFprocessor = new SarDerivedProcessor(dir);
        SDFprocessor.setGmlBaseURI(null /** myURL here **/);
        sender = new DERSender(SDFprocessor);
        sender.setServiceURL(serviceURL);

        try {
            sender.run();
            fail("IllegalStateException not detected");
        } catch (IllegalStateException e) {
        }
    }

    public void testEOProcessor() throws Exception {
        File dir = new ClassPathResource("testEOP2").getFile();
        assertNotNull("Could not find test dir");

        EOProcessor processor = new EOProcessor(dir);
        EarthObservationPackage pkg = processor.parsePackage();
        assertNotNull(pkg);
        EarthObservation eo = processor.getEO();

        assertEquals(pkg.getEoProductId(), eo.getId());
        assertEquals("2010-05-27T05:56:13.907648Z", eo.getTsBegin());
        assertEquals("2010-05-27T05:56:58.797971Z", eo.getTsEnd());


//        assertEquals("7865_ASA_WSM_1PNACS20101014_191518_000000552093_00443_45086_0001.N1.00000", pkg.getEoProductId());
//        assertEquals("7865_ASA_WSM_1PNACS20101014_191518_000000552093_00443_45086_0001.N1.00000_DER", pkg.getPackageId());
//
//        assertEquals(22, pkg.getList().size());
//
//        for (int i = 0; i < pkg.getList().size(); i++) {
//            PackedElement pe = pkg.getList().get(i);
//            System.out.println(i+". "+pe);
//            System.out.println(i+". "+processor.getList().get(i));
//            System.out.println("   " + processor.getRegistryObject(i));
//        }

    }



}
