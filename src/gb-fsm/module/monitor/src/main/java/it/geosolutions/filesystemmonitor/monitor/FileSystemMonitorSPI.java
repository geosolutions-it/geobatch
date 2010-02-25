/**
 * 
 */
package it.geosolutions.filesystemmonitor.monitor;

import it.geosolutions.filesystemmonitor.OsType;

import java.util.Map;

/**
 * ServiceProviderInterface class for {@link ThreadedFileSystemMonitor} implementations.
 * 
 * @author Simone Giannecchini
 * @since 0.2
 */
public interface FileSystemMonitorSPI {

	public final static String SOURCE="source";
	public final static String WILDCARD="wildcard";

	/**
	 * Tells me if a certain implementation of this interface is able to run on
	 * the specified operating system.
	 * 
	 * @param osType
	 *            identfies an operating system.
	 * @return <code>true</code> if this {@link FileSystemMonitor} runs on this
	 *         operating system, <code>false</code> otherwise.
	 */
	public boolean canWatch(OsType osType);

	/**
	 * 
	 * @return an instance of a {@link FileSystemMonitor}.
	 * 
	 */
	public FileSystemMonitor createInstance(final Map<String,?>configuration);
	
	public boolean isAvailable();

}
