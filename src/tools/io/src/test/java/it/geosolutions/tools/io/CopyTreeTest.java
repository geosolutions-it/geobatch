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

import it.geosolutions.tools.commons.file.Path;
import it.geosolutions.tools.commons.listener.DefaultProgress;
import it.geosolutions.tools.io.file.CopyTree;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class CopyTreeTest {

	File destMount;

	File testFile;

	CompletionService<File> cs;

	ExecutorService ex;

	private final static Logger LOGGER = LoggerFactory
			.getLogger(CopyTreeTest.class);

	@Before
	public void setUp() throws Exception {

		destMount = new File(TestData.file(this, "."), "test-data2");
		if (!destMount.exists()) {
			new File(destMount, "collector").mkdirs();
		}
		Assert.assertTrue(destMount.exists());

		testFile = TestData
				.file(this,"collector/569_PRO/RS1_geo8.xml");

		ex = Executors.newFixedThreadPool(2);

		if (ex == null || ex.isTerminated()) {
			throw new IllegalArgumentException(
					"Unable to run asynchronously using a terminated or null ThreadPoolExecutor");
		}

		cs = new ExecutorCompletionService<File>(ex);
	}

	@After
	public void tearDown() throws Exception {
		if (ex != null)
			ex.shutdown();

		FileUtils.deleteDirectory(destMount);

	}

	@Test
	public void copyTreeTest() throws Exception {
		LOGGER.info("START: CopyTreeTest");
		File srcMount = TestData.file(this, ".");
		CopyTree act = new CopyTree(FileFilterUtils.or(
				FileFilterUtils.directoryFileFilter(),
				FileFilterUtils.fileFileFilter()), cs, srcMount, destMount);
		act.addCollectingListener(new DefaultProgress("COLLECTING"));
		act.addCopyListener(new DefaultProgress("COPY"));
		int workSize = act.copy();
		try {
			while (workSize-- > 0) {
				Future<File> future = cs.take();
				try {
					LOGGER.info("copied file: " + future.get());
				} catch (ExecutionException e) {
					LOGGER.info(e.getLocalizedMessage(), e);
					Assert.fail();
				}
			}
		} catch (InterruptedException e) {
			LOGGER.info(e.getLocalizedMessage(), e);
			Assert.fail();
		}
		LOGGER.info("STOP: CopyTreeTest");
	}

	@Test
	public void stopCopyTest() throws Exception {
		LOGGER.info("BEGIN: stopCopyTest");
		File srcMount = TestData.file(this, ".");
		final CopyTree act = new CopyTree(FileFilterUtils.or(
				FileFilterUtils.directoryFileFilter(),
				FileFilterUtils.fileFileFilter()), cs, srcMount, destMount);

		final Thread copier = new Thread(new Runnable() {
			public void run() {
				act.setCancelled();
				try {
					Assert.assertEquals("Returned list should be null", 0,
							act.copy());
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Assert.fail(e.getLocalizedMessage());
				} catch (IOException e) {
					e.printStackTrace();
					Assert.fail(e.getLocalizedMessage());
				}
			}
		});

		copier.start();

		try {
			copier.join();
		} catch (InterruptedException e) {
			LOGGER.info(e.getLocalizedMessage(), e);
			Assert.fail();
		}

		LOGGER.info("STOP: stopCopyTest");
	}

	@Test
	public void rebaseTest() throws Exception {
		LOGGER.info("BEGIN: rebaseTest");
		// rebase a file
		LOGGER.info("Before: " + testFile);
		File srcMount = TestData.file(this, ".");
		File rebasedFile = Path.rebaseFile(srcMount, destMount, testFile);
		rebasedFile.getParentFile().mkdirs();
		Assert.assertTrue(testFile.renameTo(rebasedFile));
		LOGGER.info("After: " + rebasedFile);

		// TEST: File is not in the mount point dir
		Assert.assertFalse(testFile.exists());

		// move it back
		LOGGER.info("Before: " + rebasedFile);
		testFile = Path.rebaseFile(destMount, srcMount, rebasedFile);
		testFile.getParentFile().mkdirs();
		Assert.assertTrue(rebasedFile.renameTo(testFile));
		LOGGER.info("After: " + testFile.getAbsolutePath());

		// TEST: File is not in the mount point dir
		Assert.assertFalse(rebasedFile.exists());

		LOGGER.info("END: rebaseTest");
	}
	/*
	 * @Test
	 * 
	 * @Ignore public void testFileFilter() {
	 * LOGGER.info("START: testFileFilter"); XStream xstream = new XStream(new
	 * DomDriver()); FileFilter filter = FileFilterUtils.or(
	 * FileFilterUtils.directoryFileFilter(), FileFilterUtils.fileFileFilter());
	 * FileFilter filter2 = FileFilterUtils.and(FileFilterUtils
	 * .asFileFilter(filter), FileFilterUtils
	 * .notFileFilter(FileFilterUtils.nameFileFilter("*.lck",
	 * IOCase.INSENSITIVE))); LOGGER.info("FILTER: " + xstream.toXML(filter2));
	 * FileInputStream fis = null; try { fis = new FileInputStream(new File(
	 * "src/test/resources/FileFilter.xml")); Object obj = xstream.fromXML(fis);
	 * LOGGER.info("FILTER OBJ: " + obj.toString()); } catch
	 * (FileNotFoundException e) { e.printStackTrace(); Assert.fail(); } catch
	 * (XStreamException e) { e.printStackTrace(); Assert.fail(); } catch
	 * (Exception e) { e.printStackTrace(); Assert.fail(); } finally {
	 * IOUtils.closeQuietly(fis); } LOGGER.info("STOP: testFileFilter"); }
	 */
}
