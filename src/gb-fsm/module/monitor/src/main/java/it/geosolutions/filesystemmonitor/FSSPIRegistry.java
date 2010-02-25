package it.geosolutions.filesystemmonitor;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

class FSSPIRegistry implements ApplicationContextAware,InitializingBean{

	public static final OsType SUGGESTED_OS_TYPE;
	static {
		final String osName=System.getProperty("os.name").toLowerCase();	
		
		if(osName.contains("linux"))
			SUGGESTED_OS_TYPE= OsType.OS_LINUX;
		else
			if(osName.contains("windows"))
				SUGGESTED_OS_TYPE= OsType.OS_WINDOWS;
			else
				SUGGESTED_OS_TYPE=OsType.OS_UNDEFINED;		
	}

	private final static Logger LOGGER = Logger.getLogger(FSSPIRegistry.class.toString());
	
	private ApplicationContext applicationContext=null;

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext=applicationContext;
		
	}
	@SuppressWarnings("unchecked")
	FileSystemMonitor getMonitor(final Map<String,?>config,final OsType osType) {
		if(applicationContext==null)
		{
			if(LOGGER.isLoggable(Level.SEVERE))
				LOGGER.severe("Underlying applicationContext is null!");
			return null;
		}
		FileSystemMonitorSPI monitorSPI = null;
		final Map beans = applicationContext.getBeansOfType(FileSystemMonitorSPI.class);
		final Set beanSet = beans.entrySet();
		for(final Iterator it=beanSet.iterator();it.hasNext();){
			final Map.Entry entry=(Entry) it.next();
			monitorSPI = (FileSystemMonitorSPI) entry.getValue();
			if (monitorSPI!=null&&monitorSPI.isAvailable()&&monitorSPI.canWatch(osType)) {
				break;
			}
		}
		if (monitorSPI != null) 
			return monitorSPI.createInstance(config);
		
		return null;

		
	}
	public void afterPropertiesSet() throws Exception {
		if(applicationContext==null)
			throw new IllegalStateException("The provided applicationContext is null!");
		FSMSPIFinder.registry=this;
	}
}
