/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.io;

import it.geosolutions.tools.io.file.MultiPropertyFile;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simone Giannecchini, GeoSolutoins SAS
 * @author ETj (etj at geo-solutions.it)
 */
public class MultiPropertyFileTest extends TestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(MultiPropertyFileTest.class);

    @Test
    public void testIS() throws Exception {
        InputStream is = TestData.openStream(this, "test.properties");
        assertNotNull(is);

        MultiPropertyFile mpf = new MultiPropertyFile(is);
        boolean ok = mpf.read();
        assertTrue(ok);

        doTestMPF(mpf);
    }

    @Test
    public void testFile() throws Exception {
        File file = TestData.file(this, "test.properties");
        assertNotNull(file);

        MultiPropertyFile mpf = new MultiPropertyFile(file);
        boolean ok = mpf.read();
        assertTrue(ok);

        doTestMPF(mpf);
    }

    private void doTestMPF(MultiPropertyFile mpf) {
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

    @Test
    public void testErrors() throws Exception {
        InputStream is = TestData.openStream(this, "test.properties");
        assertNotNull(is);

        MultiPropertyFile mpf = new MultiPropertyFile(is);

        try {
            mpf.exist("pippo");
            fail("Unrecognized IllegalStateEx");
        } catch (IllegalStateException e) {
        }

    }

    @Test
    public void testErrorFile() throws Exception {
        File file = TestData.file(this, "test_err.properties");
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
