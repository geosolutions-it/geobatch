package it.geosolutions.filesystemmonitor.monitorjni;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.filesystemmonitor.monitor.impl.BaseFileSystemMonitor;
import it.geosolutions.filesystemmonitor.osnative.NativeFileSystemWatcherSPI;
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
public class JNITest extends AbstractJUnit4SpringContextTests {
	// //
	// default logger
	// //
	private final static Logger LOGGER = Logger.getLogger(JNITest.class.toString());

	private TestListener listener;

	private BaseFileSystemMonitor monitor;

	private File dir;

	private boolean go;


	@Before
	public void setUp() throws Exception {
		go=false;
		try {
			System.loadLibrary("jnotify");
			go=true;
		} catch (UnsatisfiedLinkError e) {
			go=false;
			return;
		}
		listener = new TestListener();
		dir = TestData.file(this, ".");
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
	public  void testPolling() {
		if(!go)
		{
			LOGGER.severe("Missing native libs, skipping tests.");
			return;
		}

		String osName = System.getProperty("os.name").toLowerCase();
		OsType os;
		if(osName.contains("linux"))
			os=OsType.OS_LINUX;
		else
			if(osName.contains("windows"))
				os=OsType.OS_WINDOWS;
			else
			{
				LOGGER.severe("JNI version of this code works only on widnows and linux.");
				return;
			}
		final Map<String,Object>params= new HashMap<String, Object>();
		params.put(NativeFileSystemWatcherSPI.SOURCE, dir);
		params.put(NativeFileSystemWatcherSPI.SUBDIRS, Boolean.FALSE);
		params.put(NativeFileSystemWatcherSPI.WILDCARD, "*.txt");
		monitor = new NativeFileSystemWatcherSPI().createInstance(params);	
		listener = new TestListener();

		LOGGER.info("Aggiungo la dir prova ai listener");
		// Add a dummy listener
		monitor.addListener(listener);
		monitor.start();
		//Thread.yield();
		LOGGER.info("Inizio l'ascolto della directory");

		// prepare the pause
		LOGGER.info("prepare the pause");
		final Timer t = new Timer("MonitorPolling Test", true);
		t.schedule(new TimerTask() {

			public void run() {
				LOGGER.info("!!! pause !!!");
				monitor.pause();

			}
		}, 8000);

		// prepare the wake up
		LOGGER.info("prepare the restart");
		t.schedule(new TimerTask() {

			public void run() {
				LOGGER.info("!!! restart !!!");
				monitor.start();

			}
		}, 15000);

		// prepare the wake up
		LOGGER.info("prepare the reset");
		t.schedule(new TimerTask() {

			public void run() {
				LOGGER.info("!!! reset !!!");
				monitor.reset();

			}
		}, 25000);

		FileCreatorThread fileCreator = new FileCreatorThread(dir.getAbsolutePath(), "txt");

		// Avoid program exit
		synchronized (this) {

			fileCreator.start();
			// while (true) {
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
			// }
		}

	}

	private final class TestListener implements FileSystemMonitorListener {
		public void fileMonitorEventDelivered(FileSystemMonitorEvent fe) {
			Assert.assertTrue("Controllo valore del MonitorEvent ", fe != null);
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

	/*
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
			this.setDaemon(true);
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

				} else {
					final StringBuffer destFileName = new StringBuffer(
							this.outputDir.getAbsolutePath()).append("/file_");

					// //
					// Copying the file "numFiles" times to the outputdir.
					// //
					for (int f = 0; f < numFiles; f++) {

						sleep(2000);
						copyFile(tempFile, new File(new StringBuffer(
								destFileName).append(f).append(".").append(
								this.outputExt).toString()));

					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);

			}
			synchronized (JNITest.this) {
				JNITest.this.notify();
			}
		}
	}
}
