package it.geosolutions.filesystemmonitor;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;

import java.util.Map;

public final class FSMSPIFinder{
	
	static FSSPIRegistry registry;

	/**
	 * Do not allows any instantiation of this class.
	 */
	private FSMSPIFinder() {
	}

	public static FileSystemMonitor getMonitor(final Map<String,?>config,final OsType osType) {
		return registry.getMonitor(config, osType);
		
	}
	
	

}