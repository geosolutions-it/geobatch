/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2013 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.ds2ds;

import static org.junit.Assert.*;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import org.junit.Before;
import org.junit.Ignore;

public class ReprojectionTest extends BaseDs2DsTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ReprojectionTest.class);

    final String CRS_SRC = "EPSG:4326";
    final String CRS_FOR_REPROJ = "EPSG:4324";

    Geometry srcGeom = null;

    public ReprojectionTest() throws Exception {

        LOGGER.info(CRS_SRC + " --> " + CRS.decode(CRS_SRC).getName());
        LOGGER.info(CRS_FOR_REPROJ + " --> " + CRS.decode(CRS_FOR_REPROJ).getName());
    }



    @Before
    public void loadSourceGeom() throws Exception {

        if (srcGeom == null) {
            LOGGER.info("Loading source geom");
            //STEP 0 - obtain a geom in CRS_SRC to be compared later
            //Load the table with with a non reprojected import and get a feature to use as test
            executeAction("shp");
            srcGeom = getExampleGeomFromTableTest();
            assertEquals(getRecordCountFromDatabase("test"), 49);
            // reset db
            dropAllDb(dbName);
            dropAllDb(dbNameSource);
        }
    }

    @Test
    public void testReprojection1() throws Exception {
        //STEP 1 - Do a on-the-fly reprojection to CRS_FOR_REPROJ,
        //the reprojection will be performed:
        //from EPSG:CRS_SRC
        configuration.getSourceFeature().setCrs(CRS_SRC);
        //to CRS_FOR_REPROJ
        configuration.setReprojectedCrs(CRS_FOR_REPROJ);
        //and set the output feature correctly to CRS_FOR_REPROJ
        configuration.getOutputFeature().setCrs(CRS_FOR_REPROJ);
        executeAction("shp");
        Geometry trgGeom = getExampleGeomFromTableTest();
        assertEquals(49, getRecordCountFromDatabase("test"));
        //Src and output features must be different (the reprojection has been done)
        assertNotEquals(srcGeom.toString(), trgGeom.toString());
        //Check if CRS has been correctly assigned
        assertEquals(CRS.decode(CRS_FOR_REPROJ), getCrsFromDb("test"));
    }

    @Test
    public void testReprojection2() throws Exception {
        //STEP 2 - Similar to the STEP1 but without forcing the output CRS...
        // the result must be the same as before
        configuration.getSourceFeature().setCrs(CRS_SRC);
        configuration.setReprojectedCrs(CRS_FOR_REPROJ);
        configuration.getOutputFeature().setCrs(null); // Reset the outputCRS
        executeAction("shp");
        Geometry trgGeom = getExampleGeomFromTableTest();
        assertEquals(49, getRecordCountFromDatabase("test"));
        assertNotEquals(srcGeom.toString(), trgGeom.toString());
        assertEquals(CRS.decode(CRS_FOR_REPROJ), getCrsFromDb("test"));
    }

    @Test
    @Ignore
    // fails with GT10
    public void testReprojection3() throws Exception {
        //STEP 3 - Force source feature to CRS_FOR_REPROJ and reproject with the same CRS...
        //Although the source feature is in CRS_SRC we must get the same features due to src crs forcing
        configuration.setReprojectedCrs(CRS_FOR_REPROJ);
        configuration.getSourceFeature().setCrs(CRS_FOR_REPROJ);
        configuration.getOutputFeature().setCrs(CRS_FOR_REPROJ);
        executeAction("shp");
        Geometry trgGeom = getExampleGeomFromTableTest();
        assertEquals(49, getRecordCountFromDatabase("test"));
        System.out.println(srcGeom.toString());
        assertEquals(srcGeom.toString(), trgGeom.toString());
        assertEquals(CRS.decode(CRS_FOR_REPROJ), getCrsFromDb("test"));
    }

    @Test
    @Ignore
    // fails with GT10
    public void testReprojection4() throws Exception {
        //STEP 4 - Reproject trg in the same CRS as src... nothing should changes in trg feature
        configuration.setReprojectedCrs(CRS_SRC);
        configuration.getSourceFeature().setCrs(CRS_SRC);
        configuration.getOutputFeature().setCrs(null);
        executeAction("shp");
        Geometry trgGeom = getExampleGeomFromTableTest();
        assertEquals(49, getRecordCountFromDatabase("test"));
        assertTrue(srcGeom.equals(trgGeom));
        assertEquals(CRS.decode(CRS_SRC), getCrsFromDb("test"));
    }
}
