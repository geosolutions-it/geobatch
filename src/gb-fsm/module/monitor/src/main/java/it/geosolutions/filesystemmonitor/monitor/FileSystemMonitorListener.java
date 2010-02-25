package it.geosolutions.filesystemmonitor.monitor;

import java.util.EventListener;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
public interface FileSystemMonitorListener extends EventListener {
    /**
     * Called when one of the monitored files are created, deleted or modified.
     * 
     * @param file
     *            File which has been changed.
     */
    void fileMonitorEventDelivered(FileSystemMonitorEvent fe);
}