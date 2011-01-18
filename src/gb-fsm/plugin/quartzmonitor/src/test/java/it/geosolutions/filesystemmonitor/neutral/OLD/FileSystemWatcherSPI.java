package it.geosolutions.filesystemmonitor.neutral.OLD;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorType;

import java.io.File;
import java.util.Map;

public final class FileSystemWatcherSPI implements FileSystemMonitorSPI {

    public final static String INTERVAL = "interval";

    public boolean canWatch(OsType osType) {
        return true;
    }

    public boolean isAvailable() {
        return true;
    }
    
    public FileSystemMonitorType getType(){
        return FileSystemMonitorType.DEFAULT;
    }

    public FileSystemWatcher createInstance(Map<String, ?> configuration) {
        // get the params
        // polling interval
        Long interval = null;
        Object element = configuration.get(INTERVAL);
        if (element != null && element.getClass().isAssignableFrom(Long.class))
            interval = (Long) element;

        // file
        File file = null;
        element = configuration.get(SOURCE);
        if (element != null && element.getClass().isAssignableFrom(File.class))
            file = (File) element;

        // wildcard
        String wildcard = null;
        element = configuration.get(WILDCARD);
        if (element != null && element.getClass().isAssignableFrom(String.class))
            wildcard = (String) element;

        // checks
        if (!file.exists() || !file.canRead())
            return null;

        if (wildcard != null && interval != null && interval > 0)
            return new FileSystemWatcher(file, wildcard, interval);
        if (wildcard != null && (interval == null || interval <= 0))
            return new FileSystemWatcher(file, wildcard);
        if (wildcard == null && interval != null && interval > 0)
            return new FileSystemWatcher(file, interval);
        if (wildcard == null && (interval == null || interval <= 0))
            return new FileSystemWatcher(file);

        return null;
    }

}
