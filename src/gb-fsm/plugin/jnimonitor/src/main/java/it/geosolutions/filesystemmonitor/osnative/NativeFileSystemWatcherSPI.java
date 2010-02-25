package it.geosolutions.filesystemmonitor.osnative;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;

import java.io.File;
import java.util.Map;

public class NativeFileSystemWatcherSPI implements FileSystemMonitorSPI {

	public final static String SUBDIRS="subdirs";

	public boolean canWatch(OsType osType) {
		if(!NativeLibsUtils.available)
			return false;
		return osType == OsType.OS_WINDOWS||osType==OsType.OS_LINUX||osType==OsType.OS_UNDEFINED;
	}



	public boolean isAvailable() {
		return NativeLibsUtils.available;
	}

	public NativeFileSystemWatcher createInstance(Map<String, ?> configuration) {
		if(!NativeLibsUtils.available)
			throw new IllegalStateException("Native monitor unable to work.");
		//get the params
		//polling interval
		Boolean includeSubdir=null;
		Object element=configuration.get(SUBDIRS);
		if(element!=null && element.getClass().isAssignableFrom(Boolean.class))
			includeSubdir=(Boolean)element;
		
		//file
		File file=null;
		element=configuration.get(SOURCE);
		if(element!=null && element.getClass().isAssignableFrom(File.class))
			file=(File)element;
		
		// wildcard
		String wildcard=null;
		element=configuration.get(WILDCARD);
		if(element!=null && element.getClass().isAssignableFrom(String.class))
			wildcard=(String)element;
		
		//checks
		if (!file.exists()||!file.canRead())
			return null;
		
		if(wildcard!=null&&includeSubdir!=null)
			return new NativeFileSystemWatcher(file,includeSubdir,wildcard);
		if(wildcard!=null&&includeSubdir==null)
			return new NativeFileSystemWatcher(file,wildcard);
		if(wildcard==null&&includeSubdir!=null)
			return new NativeFileSystemWatcher(file,includeSubdir);
		if(wildcard==null&&includeSubdir==null)
			return new NativeFileSystemWatcher(file);
		
		return null;
	}

}
