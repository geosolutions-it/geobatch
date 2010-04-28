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
public class SASDirNameParserTest extends TestCase {

    public SASDirNameParserTest() {
    }

    @Test
    public void testParse() {
        System.out.println("parse");
        String fileName = "090316_pippo_Leg42_port";

        // Input names are in the form: DATE_missionXX_LegXXXX_CHANNEL
        // As an instance: DATE=090316 and CHANNEL=port

        SASDirNameParser parser = SASDirNameParser.parse(fileName);

        assertNotNull("Can't instantiate parser", parser);

        assertEquals(new Date(2009-1900, 3-1, 16), parser.getDate());
        assertEquals("pippo", parser.getMission());
        assertEquals("42", parser.getLeg());
        assertEquals("port", parser.getChannel());
    }


}