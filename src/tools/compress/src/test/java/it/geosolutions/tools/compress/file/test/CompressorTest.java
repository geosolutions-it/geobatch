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

import it.geosolutions.tools.compress.file.Compressor;
import it.geosolutions.tools.compress.file.reader.TarReader;
import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CompressorTest {

    Logger LOGGER=Logger.getLogger(CompressorTest.class);
    File compressed;
    File folderCompare,destinationFolder;
    
    @Before
    public void setUp() throws Exception {
        folderCompare=TestData.file(TarReader.class, "nested_folder_test");
        destinationFolder=TestData.file(Compressor.class, ".");
        compressed=TestData.file(Compressor.class, "nested_folder_test.zip");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void deflate() throws FileNotFoundException, IOException {
        	Collector c=new Collector(FileFilterUtils.fileFileFilter());
            File compressed=Compressor.deflate(destinationFolder, "deflate_test",c.collect(folderCompare).toArray(new File[]{}));
            LOGGER.info("Compressed model: "+this.compressed+" length:"+this.compressed.length());
            LOGGER.info("Compressed to compare: "+compressed+" length:"+compressed.length());
            Assert.assertEquals(this.compressed.length(),compressed.length());   
    }
}
