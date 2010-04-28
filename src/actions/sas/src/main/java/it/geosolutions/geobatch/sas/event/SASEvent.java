/*
 */

package it.geosolutions.geobatch.sas.event;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import java.io.File;

/**
 *
 * TODO: should extend ActionEvent
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASEvent extends FileSystemMonitorEvent{

    protected String wmsPath;
    protected String format;

    protected String missionName;


    public SASEvent(File source) {
        super(source, FileSystemMonitorNotifications.FILE_ADDED);
    }

    /**
     * Get the value of wmsPath
     *
     * @return the value of wmsPath
     */
    public String getWmsPath() {
        return wmsPath;
    }

    /**
     * Set the value of wmsPath
     *
     * @param wmsPath new value of wmsPath
     */
    public void setWmsPath(String wmsPath) {
        this.wmsPath = wmsPath;
    }

    /**
     * Get the value of format
     *
     * @return the value of format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Set the value of format
     *
     * @param format new value of format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "[source:" + source
                + " format:" + format
                + " wmspath:" + wmsPath
                + " mission:" + (missionName!=null?missionName:"-")
                + "]";
    }

}
