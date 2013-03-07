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
package it.geosolutions.tools.compress.file.test;

import it.geosolutions.tools.compress.file.Extract;

import java.io.File;

import static junit.framework.Assert.*;

import org.apache.log4j.Logger;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExtractTest {
    Logger LOGGER=Logger.getLogger(ExtractTest.class);
    File compressed,compressedTgz;
    File outFile;
    
    @Before
    public void setUp() throws Exception {
        compressed=TestData.file(Extract.class, "nested_folder_test.tar.gz");
        compressedTgz=TestData.file(Extract.class, "tarGzip.tgz");
    }

    @After
    public void tearDown() throws Exception {
        if (outFile!=null)
            outFile.delete();
    }
    
    @Test
    public void extractTest() throws Exception {
    	LOGGER.info("Extracting file: "+compressed);
    	LOGGER.info("Extracting here: "+compressed.getParentFile());
    	outFile=Extract.extract(compressed, compressed.getParentFile(), false);
    	assertTrue(outFile.exists());
    	assertTrue(outFile.isDirectory());
    }
    
    @Test
    public void extractTgzTest() throws Exception {
        LOGGER.info("Extracting tgz file: "+compressedTgz);
        File outdir = new File(compressedTgz.getParentFile(), Long.toString(System.currentTimeMillis()));
        assertFalse(outdir.exists());
        outdir.mkdir();
        LOGGER.info("Extracting here: "+ outdir);
        outFile=Extract.extract(compressedTgz, outdir, false);
        assertTrue(outFile.exists());
        assertTrue(outFile.isDirectory());
    }
}
