package it.geosolutions.filesystemmonitor.monitorpolling;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.neutral.monitorpolling.PureJavaFileSystemWatcherSPI;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author   Alessio
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({})
@ContextConfiguration(locations={"/applicationContext.xml"})
public class PureJavaTest extends AbstractJUnit4SpringContextTests {


	private final static Logger LOGGER = Logger
			.getLogger("it.geosolutions.filesystemmonitor.monitorpolling");

	private TestListener listener;

	private FileSystemMonitor monitor;

	private File dir;


	@Before
	public void setUp() throws Exception {
		listener = new TestListener();
		dir = TestData.file(this, "");
		assert dir != null && dir.exists();
	}

	@After
	public void tearDown() throws Exception {
		if (monitor != null) {
			monitor.stop();
			monitor.dispose();

		}
	}

	@Test
	public void testPolling() {
		// ///////////////////////////////////////////////////////////
		// Attiva il thead che fa polling sulla direcroty di lavoro
		// ///////////////////////////////////////////////////////////

		// Create the monitor
		final Map<String,Object>params= new HashMap<String, Object>();
		params.put(PureJavaFileSystemWatcherSPI.SOURCE, dir);
		params.put(PureJavaFileSystemWatcherSPI.INTERVAL, 1000L);
		params.put(PureJavaFileSystemWatcherSPI.WILDCARD, "*.txt");
		monitor = ((FileSystemMonitorSPI)this.applicationContext.getBean("pureJavaFSMSPI")).createInstance(params);
		listener = new TestListener();

		LOGGER.info("Add a dummy listener");
		// Add a dummy listener
		monitor.addListener(listener);
		monitor.start();
		Thread.yield();
		LOGGER.info("Start folder observer");
		
		//prepare the pause
		LOGGER.info("prepare the pause");
		final Timer t= new Timer("MonitorPolling Test",true);
		t.schedule(new TimerTask(){

			public void run() {
				LOGGER.info("pause");
				monitor.pause();
				
			}}, 6000);
		
		
		//prepare the wake up
		LOGGER.info("prepare the restart");
		t.schedule(new TimerTask(){

			public void run() {
				LOGGER.info("restart");
				monitor.start();
				
			}}, 20000);

		FileCreatorThread fileCreator = new FileCreatorThread(dir
				.getAbsolutePath(), "txt");

		// Avoid program exit
		synchronized (this) {

			fileCreator.start();
			try {
				Thread.yield();
				this.wait();
				LOGGER.info("successfull");
				// break;
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
				// �don�t catch an exception unless
				// you know what to do with it�
			}
		}

	}

	private final class TestListener implements FileSystemMonitorListener {
		public void fileMonitorEventDelivered(FileSystemMonitorEvent fe) {
			Assert.assertTrue("MonitorEvent value ", fe != null);
			LOGGER.info(new StringBuffer("\nFile changed: ").append(
					fe.getSource()).toString());
			String s = "";
			if (fe.getNotification()==FileSystemMonitorNotifications.FILE_ADDED)
				s = "file added";
			else
				if(fe.getNotification()==FileSystemMonitorNotifications.FILE_REMOVED)
				s = "file removed";
				else
					if(fe.getNotification()==FileSystemMonitorNotifications.FILE_MODIFIED)
						s = "file modified";
			LOGGER.info(new StringBuffer("Event: ").append(s).toString());
			LOGGER.info(Thread.currentThread().getName());
		}
	}

	/**
	 * FileCreatorThread: Utility thread to create/copy a number of files into a
	 * certain directory.
	 * 
	 * @author Alessio Fabiani
	 */
	private class FileCreatorThread extends Thread {
		/**
		 * The number of files to create. It must be > 0.
		 */
		private int numFiles = 30;

		/**
		 * A template output file, if someone don't want to create a fake temp
		 * file but use its own.
		 */
		private File outFile = null;

		/**
		 * The output directory.
		 */
		private File outputDir = null;

		/**
		 * The file suffix. "tmp" as default.
		 */
		private String outputExt = "tmp";

		/**
		 * Constructor which take as parameters the number of files (if greater
		 * than 0), the output dir path and the file suffix.
		 * 
		 * @param numFiles
		 * @param outputDir
		 * @param outputExt
		 */
		public FileCreatorThread(final int numFiles, final String outputDir,
				final String outputExt) {
			super();
			if (numFiles > 0)
				this.numFiles = numFiles;
			this.outputDir = new File(outputDir);
			if (!this.outputDir.exists() || !this.outputDir.isDirectory())
				this.outputDir = null;
			this.outputExt = outputExt;
			this.setDaemon(true);
		}

		/**
		 * Constructor which take as parameters the template output file, the
		 * output dir path and the file suffix.
		 * 
		 * @param outFile
		 * @param outputDir
		 * @param outputExt
		 */
		public FileCreatorThread(final File outFile, final String outputDir,
				final String outputExt) {
			this(-1, outputDir, outputExt);
			this.outFile = outFile;
		}

		/**
		 * Constructor which take as parameters the output dir path and the file
		 * suffix.
		 * 
		 * @param outputDir
		 * @param outputExt
		 */
		public FileCreatorThread(final String outputDir, final String outputExt) {
			this(-1, outputDir, outputExt);
		}

		/**
		 * An helper function using the "nio" classes to copy a file.
		 * 
		 * @param in
		 * @param out
		 * @throws Exception
		 */
		private void copyFile(File in, File out) throws Exception {
			FileChannel sourceChannel = new FileInputStream(in).getChannel();
			FileChannel destinationChannel = new FileOutputStream(out)
					.getChannel();
			sourceChannel.transferTo(0, sourceChannel.size(),
					destinationChannel);
			// or
			// destinationChannel.transferFrom(sourceChannel, 0,
			// sourceChannel.size());
			sourceChannel.close();
			destinationChannel.close();
		}

		/**
		 * Main Thread running method.
		 */
		public void run() {
			try {

				// //
				// Check for the source file
				// //
				File tempFile = null;

				// //
				// Create a new one if the template is null
				// //
				tempFile = (this.outFile != null ? this.outFile : File
						.createTempFile("FileCreatorThread", this.outputExt));

				// //
				// Check for the output dir existence and consistency.
				// //
				if (this.outputDir == null) {
					LOGGER.log(Level.SEVERE, "Output Directory doesn't exist!");
					return;
				} else {
					final StringBuffer destFileName = new StringBuffer(
							this.outputDir.getAbsolutePath()).append("\\file_");

					// //
					// Copying the file "numFiles" times to the outputdir.
					// //
					for (int f = 0; f < numFiles; f++) {

						sleep(2000);
						copyFile(tempFile, new File(new StringBuffer(destFileName).append(f)
								.append(".").append(this.outputExt).toString()));

					}

				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return;
			}
			synchronized (PureJavaTest.this) {
				PureJavaTest.this.notify();
			}
		}
	}
}