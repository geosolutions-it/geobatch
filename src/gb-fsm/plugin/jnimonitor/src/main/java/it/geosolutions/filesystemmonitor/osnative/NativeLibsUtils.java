package it.geosolutions.filesystemmonitor.osnative;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
final class NativeLibsUtils {
	
	public final static boolean available;
	
	static {
		boolean av=false;
		try {
			System.loadLibrary("jnotify");
			av=true;
		}catch (Throwable e) {
			av=false;
		}
		available=av;
		
	}
	




}
