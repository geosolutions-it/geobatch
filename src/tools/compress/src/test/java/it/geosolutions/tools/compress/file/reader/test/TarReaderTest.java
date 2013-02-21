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
package it.geosolutions.tools.compress.file.reader.test;

import it.geosolutions.tools.compress.file.reader.TarReader;
import it.geosolutions.tools.compress.file.test.ExtractorTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TarReaderTest {

    Logger LOGGER=Logger.getLogger(ExtractorTest.class);
    File untarredDest, tar, untarredCompare;
    
    @Before
    public void setUp() throws Exception {
        tar=TestData.file(TarReader.class, "nested_folder_test.tar");
        untarredDest=new File(TestData.file(TarReader.class, "."),UUID.randomUUID().toString());
        untarredCompare=TestData.file(TarReader.class,"nested_folder_test");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void tarReaderTest() throws FileNotFoundException, IOException {
        
        try {
            TarReader.readTar(tar,untarredDest);
            if (!untarredDest.exists()){
                Assert.fail("Failed to uncompress the gzip file");
            } else {
                File fileA=untarredDest.listFiles()[0];
                File fileB=untarredCompare;
                File[] fileAList=fileA.listFiles();
                File[] fileBList=fileB.listFiles();
                test(fileAList, fileBList, 0);   
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e.getMessage(), e);
            Assert.fail(e.getLocalizedMessage());
        }
    }
    
    private void test(File[] fileAList, File[] fileBList, int i){
        if (i<fileAList.length && i<fileBList.length){
            
            File fileA=fileAList[i];
            File fileB=fileBList[i];
            LOGGER.info("Comparing A: "+fileA.getName()+" with B: "+fileB.getName());
            if (fileA.length()!=fileB.length()){
                Assert.fail("Failed to compare the resulting tar file");
            } else {
                LOGGER.info("SUCCESS A size: "+fileA.length()+" and B size: "+fileB.length());
            }
            if (fileA.isDirectory() && fileB.isDirectory()){
                fileAList=fileA.listFiles();
                fileBList=fileB.listFiles();
                i=0;
                test(fileAList, fileBList, i);
            } else {
                test(fileAList, fileBList, i+1);
            }
            
        }
    }
}
