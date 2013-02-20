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
package it.geosolutions.tools.commons.file;


import it.geosolutions.tools.commons.file.Path;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PathTest {
    Logger LOGGER=Logger.getLogger(PathTest.class);
    
    File pathTest,srcMountPnt,dstMountPnt;
    
    @Before
    public void setUp() throws Exception {
        srcMountPnt=TestData.file(Path.class, ".");
        dstMountPnt=new File(TestData.file(Path.class, "."),UUID.randomUUID().toString());
        dstMountPnt.mkdirs();
        dstMountPnt.deleteOnExit();
        pathTest=TestData.file(Path.class, "inner/PathTest.txt");
    }

	@After
	public void tearDown() throws Exception {
		
	}
	@Test
	public void toRelativeFileTest() throws IllegalArgumentException, IOException{
		File rebased=Path.toRelativeFile(srcMountPnt, pathTest);
		Assert.assertTrue(!rebased.isAbsolute());
		Assert.assertTrue(rebased.getPath().equals("inner"+File.separator+"PathTest.txt"));
	}
	@Test
	public void RebaseFileTest() throws IllegalArgumentException, IOException{
		File rebased=Path.rebaseFile(srcMountPnt, dstMountPnt, pathTest);
		Assert.assertTrue(rebased.isAbsolute());
		Assert.assertTrue(rebased.getAbsolutePath().equals(dstMountPnt.getAbsolutePath()+File.separator+"inner"+File.separator+"PathTest.txt"));
	}
	

}
