/*
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.tools.file;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.TestCase;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class MultiPropertyFileTest extends TestCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(MultiPropertyFileTest.class);
    
    
    public MultiPropertyFileTest() {
    }

    private File loadFile(String name) {        
        try {
            URL url = this.getClass().getClassLoader().getResource(name);
            if(url == null)
                throw new IllegalArgumentException("Cant get file '"+name+"'");
            File file = new File(url.toURI());
            return file;
        } catch (URISyntaxException e) {
            LOGGER.error("Can't load file " + name + ": " + e.getMessage(), e);
            return null;
        }    
    }
    
    private InputStream openStream(String name) {        
        
        return getClass().getClassLoader().getResourceAsStream(name);
    }
    
//    @Test
    public void testIS() {
        InputStream is = openStream("test.properties");
        assertNotNull(is);
        
        MultiPropertyFile mpf = new MultiPropertyFile(is);
        boolean ok = mpf.read();
        assertTrue(ok);
        
        doTestMPF(mpf);        
    }
    
    public void testFile() {
        File file = loadFile("test.properties");
        assertNotNull(file);
        
        MultiPropertyFile mpf = new MultiPropertyFile(file);
        boolean ok = mpf.read();
        assertTrue(ok);
        
        doTestMPF(mpf);        
    }
    
    protected void doTestMPF(MultiPropertyFile mpf) {
        assertTrue(mpf.exist("single"));
        assertTrue(mpf.exist("multi"));
        assertFalse(mpf.exist("notexists"));
        
        assertTrue(mpf.isSingleValue("single"));
        assertTrue(mpf.isMultiValue("multi"));

        assertFalse(mpf.isSingleValue("notexists"));
        assertFalse(mpf.isMultiValue("notexists"));
        
        assertEquals("val1", mpf.getString("single"));
        assertEquals(2, mpf.getList("multi").size());        
    }

//    @Test
    public void testErrors() {
        InputStream is = openStream("test.properties");
        assertNotNull(is);
        
        MultiPropertyFile mpf = new MultiPropertyFile(is);
        
        try {
            mpf.exist("pippo");
            fail("Unrecognized IllegalStateEx");
        } catch (IllegalStateException e) {
        }
                
    }
    
    public void testErrorFile() {
        File file = loadFile("test_err.properties");
        assertNotNull(file);
        
        MultiPropertyFile mpf = new MultiPropertyFile(file);
        boolean ok = mpf.read();
        assertFalse(ok);
        
        assertTrue(mpf.exist("single"));
        assertTrue(mpf.exist("multi"));
        assertTrue(mpf.isSingleValue("single"));
        assertTrue(mpf.isMultiValue("multi"));                        
    }
}
