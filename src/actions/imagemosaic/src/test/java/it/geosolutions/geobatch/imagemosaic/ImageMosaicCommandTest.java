/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.geotools.TestData;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the ImageMosaicCommand
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * @author Simone Giannecchini, GeoSolutions SAS
 * 
 */
public class ImageMosaicCommandTest extends Assert {
	ImageMosaicCommand cmd = null;
	File cmdFile;
	List<File> addList;
	List<File> delList;

	@Before
	public void setUp() throws Exception {
		// create in memory object
		addList = new ArrayList<File>();
		addList.add(TestData.file(this,
				"time_mosaic/world.200406.3x5400x2700.tiff"));
		delList = new ArrayList<File>();
		delList.add(TestData.file(this,
				"time_mosaic/world.200406.3x5400x2700.tiff"));
		cmd = new ImageMosaicCommand(new File(TestData.file(this, null), "test"), addList, delList);

		// GeoServerActionConfig
		cmd.setGeoserverUID("admin");

		// GeoServerActionConfiguration
		cmd.setCrs("EPSG:4326");

		// ImageMosaicConfiguration
		cmd.setAllowMultithreading(true);

		cmd.setTileSizeH(128);
		cmd.setTileSizeW(128);

		cmdFile = new File(TestData.file(this, null), "test_cmd_out.xml");
	}

	@Test
	public final void serializeDeserialize() throws IOException {
		final String path = cmdFile.getAbsolutePath();

		// change params
		File out = ImageMosaicCommand.serialize(cmd, path);
		assertTrue("Unable to serialize object to: " + path, out.exists());

		ImageMosaicCommand cmd2 = ImageMosaicCommand.deserialize(cmdFile);
		assertNotNull("Unable to deserialize object from: " + path,cmd2);
		
		//check for equals
		compare(cmd,cmd2);
	}
	
	@Test
	public final void serializeDeserializeNewParams() throws IOException {
		final String path = cmdFile.getAbsolutePath();
		
		// change params
		cmd.setNFSCopyWait(11);
		cmd.setFinalReset(true);

		// change params
		File out = ImageMosaicCommand.serialize(cmd, path);
		assertTrue("Unable to serialize object to: " + path, out.exists());

		ImageMosaicCommand cmd2 = ImageMosaicCommand.deserialize(cmdFile);
		assertNotNull("Unable to deserialize object from: " + path,cmd2);
		assertTrue(cmd2.getFinalReset());
		assertEquals(cmd.getNFSCopyWait(), cmd2.getNFSCopyWait());
		
		//check for equals
		compare(cmd,cmd2);
	}


	void compare(ImageMosaicCommand cmd, ImageMosaicCommand cmd2) {
		assertEquals(cmd.getBackgroundValue(),cmd2.getBackgroundValue());
		assertEquals(cmd.getCrs(),cmd2.getCrs());
		assertEquals(cmd.getElevationAttribute(),cmd2.getElevationAttribute());
		assertEquals(cmd.getElevationPresentationMode(),cmd2.getElevationPresentationMode());
		assertEquals(cmd.getElevationRegex(),cmd2.getElevationRegex());
		assertEquals(cmd.getFinalReset(),cmd2.getFinalReset());
		assertEquals(cmd.getNFSCopyWait(),cmd2.getNFSCopyWait());
		assertEquals(cmd.getAddFiles(),cmd2.getAddFiles());
		assertEquals(cmd.getDelFiles(),cmd2.getDelFiles());
	}

	@Test
	public final void testToString() {
		Assert.assertNotNull("null cmd object", cmd);
		Assert.assertNotNull(cmd.toString());
	}

	@Test
	public final void testClone() {
		ImageMosaicCommand cmd2 = cmd.clone();
		Assert.assertNotNull(cmd2);
		
		//check for equals
		compare(cmd,cmd2);
	}

	@Test
	public final void testoverrideImageMosaicCommand() {
		final ImageMosaicConfiguration conf = new ImageMosaicConfiguration("id", "name", "description");
		conf.setTileSizeH(256);
		conf.setTileSizeW(256);

		try {

			cmd.copyConfigurationIntoCommand(conf);

			// tile size is set to 128 into cmd and to 256 into conf
			// result should be 128 since cmd settings may override
			Assert.assertEquals((Integer) 128, cmd.getTileSizeH());
			Assert.assertEquals((Integer) 128, cmd.getTileSizeW());

			// not set into configuration but already present into command is
			// not overridden
			Assert.assertEquals((Boolean) true, cmd.getAllowMultithreading());
		} catch (SecurityException e) {
			Assert.fail(e.getLocalizedMessage());
		}

	}
}
