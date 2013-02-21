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

import it.geosolutions.tools.compress.file.Extractor;
import it.geosolutions.tools.compress.file.reader.TarReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ExtractorTest {

    Logger LOGGER=Logger.getLogger(ExtractorTest.class);
    File compressed;
    File tar,tarCompare;
    
    @Before
    public void setUp() throws Exception {
        compressed=TestData.file(Extractor.class, "nested_folder_test.tar.gz");
        tar=TestData.temp(Extractor.class, "nested_folder_test.tar");
        tarCompare=TestData.file(TarReader.class, "nested_folder_test.tar");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void extractorGZip() throws FileNotFoundException, IOException {
        
        try {
            Extractor.extractGzip(compressed, tar);
            if (!tar.exists()){
                Assert.fail("Failed to uncompress the gzip file");
            } else if (tar.length()!=tarCompare.length()){
                Assert.fail("Failed to compare the resulting tar file");
            }
        } catch (CompressorException e) {
            LOGGER.log(Level.ERROR, e.getMessage(), e);
            Assert.fail(e.getLocalizedMessage());
        }
    }
}
