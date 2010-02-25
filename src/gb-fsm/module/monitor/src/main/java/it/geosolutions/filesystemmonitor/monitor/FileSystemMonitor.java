package it.geosolutions.filesystemmonitor.monitor;

import java.io.File;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
public interface FileSystemMonitor {

	/**
	 * Start the file monitor polling.
	 */
	public  void start();

	/**
	 * Stop the file monitor polling.
	 */
	public  void stop();

	public  void pause();

	public  void reset();

	/**
	 * Check if {@link FileSystemMonitor} is running
	 */
	public  boolean isRunning();

	public  boolean isPaused();

	/**
	 * Disposing all the collections of objects I have created along the path,
	 * which are, listeners, files and of course the time.
	 * 
	 */
	public  void dispose();

	/**
	 * @return   the watchDirectory
	 * @uml.property  name="watchDirectory"
	 */
	public File getFile() ;

	/**
	 * @return   the wildCard
	 * @uml.property  name="wildCard"
	 */
	public String getWildCard();
	
	public void addListener(FileSystemMonitorListener fileListener);
	
	public void removeListener(FileSystemMonitorListener fileListener);
}