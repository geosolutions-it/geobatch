package it.geosolutions.filesystemmonitor.monitorfactory;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

public final class DummyMonitor implements FileSystemMonitorSPI, FileSystemMonitor {
	public DummyMonitor() {
	}

	// //
	// default logger
	// //
	private final static Logger LOGGER = Logger
			.getLogger("it.geosolutions.filesystemmonitor.monitor.ThreadedFileSystemMonitor");

	public void start() {
		LOGGER.info("Dummy-ThreadedFileSystemMonitor Started ...");
	}


	public void stop() {
		LOGGER.info("Dummy-ThreadedFileSystemMonitor Stopped ...");
	}

	public boolean isRunning() {
		return false;
	}

	public void setFile(File file, String extension) {
	}

	public void addListener(FileSystemMonitorListener fileListener) {
	}

	public void removeListener(FileSystemMonitorListener fileListener) {
	}

	public void dispose() {
	}



	public boolean canWatch(OsType osType) {
		return true;
	}

	public FileSystemMonitorSPI getSPI() {
		return new DummyMonitor();
	}

	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub

	}

	public void pause() {
		// TODO Auto-generated method stub

	}

	public boolean isPaused() {
		// TODO Auto-generated method stub
		return false;
	}

	public void reset() {
		// TODO Auto-generated method stub

	}


	public File getFile() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getWildCard() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFile(File file) {
		// TODO Auto-generated method stub
		
	}


	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return true;
	}


	public FileSystemMonitor createInstance(Map<String, ?> configuration) {
		return new DummyMonitor();
	}

}
