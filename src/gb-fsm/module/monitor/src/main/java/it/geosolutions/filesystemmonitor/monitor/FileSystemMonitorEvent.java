package it.geosolutions.filesystemmonitor.monitor;

import java.io.File;
import java.util.EventObject;

/**
 * @author   Alessio Fabiani, GeoSolutions
 */
public class FileSystemMonitorEvent extends EventObject {

    /**
     * 
     */
    private static final long serialVersionUID = 7915893220009824087L;
    /**
	 * @uml.property  name="notification"
	 */
    private final FileSystemMonitorNotifications notification;
	private final long timestamp;

    public FileSystemMonitorEvent(File source,FileSystemMonitorNotifications notification) {
        super(source);
        this.timestamp=System.currentTimeMillis();
        this.notification=notification;
    }

    /**
	 * @return   Returns the notification.
	 * @uml.property  name="notification"
	 */
    public FileSystemMonitorNotifications getNotification() {
        return notification;
    }

	@Override
	public File getSource() {
		return (File) super.getSource();
	}

	public long getTimestamp() {
		return timestamp;
	}
}