package it.geosolutions.filesystemmonitor.monitorfactory;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorType;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DummyMonitor implements FileSystemMonitorSPI, FileSystemMonitor {
    public DummyMonitor() {
    }

    // //
    // default logger
    // //
    private final static Logger LOGGER = LoggerFactory.getLogger(DummyMonitor.class);

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

    public void addListener(FileSystemListener fileListener) {
    }

    public void removeListener(FileSystemListener fileListener) {
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

    public FileSystemMonitorType getType() {
        // TODO Auto-generated method stub
        return null;
    }

}
