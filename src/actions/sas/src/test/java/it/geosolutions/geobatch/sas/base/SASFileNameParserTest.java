/*
 */

package it.geosolutions.geobatch.sas.base;

import java.util.Date;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASFileNameParserTest extends TestCase {

    public SASFileNameParserTest() {
    }

    @Test
    public void testParse() {
        System.out.println("parse");
        String fileName = "muscle_col2_090316_1_2_p_5790_5962_40_150";
        SASFileNameParser expResult = null;

         // muscle_col2_090316_1_2_p_5790_5962_40_150.tif
        SASFileNameParser parser = SASFileNameParser.parse(fileName);

        assertNotNull("Can't instantiate parser", parser);

        assertEquals("muscle", parser.getMission());
        assertEquals("col2", parser.getCruise());
        assertEquals(new Date(2009-1900, 3-1, 16), parser.getDate());
        assertEquals(1, parser.getMissNum());
        assertEquals(2, parser.getLeg());
        assertEquals(SASFileNameParser.Channel.PORT, parser.getChannel());
        assertEquals(5790, parser.getPingStart());
        assertEquals(5962, parser.getPingEnd());
        assertEquals(40, parser.getRangeStart());
        assertEquals(150, parser.getRangeEnd()); 
    }


}