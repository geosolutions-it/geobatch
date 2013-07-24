/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.filesystemmonitor;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorType;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

class FSSPIRegistry implements ApplicationContextAware, InitializingBean {

    public static final OsType SUGGESTED_OS_TYPE;
    static {
        final String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("linux"))
            SUGGESTED_OS_TYPE = OsType.OS_LINUX;
        else if (osName.contains("windows"))
            SUGGESTED_OS_TYPE = OsType.OS_WINDOWS;
        else
            SUGGESTED_OS_TYPE = OsType.OS_UNDEFINED;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(FSSPIRegistry.class.toString());

    
    private ApplicationContext applicationContext = null;

    /**
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    FileSystemMonitor getMonitor(final Map<String, ?> config, final OsType osType, final FileSystemMonitorType type) {
        if (applicationContext == null) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Underlying applicationContext is null!");
            return null;
        }
        FileSystemMonitorSPI monitorSPI = null;
        final Map beans = applicationContext.getBeansOfType(FileSystemMonitorSPI.class);
        final Set beanSet = beans.entrySet();
        FileSystemMonitorSPI ret=null;
        for (final Iterator it = beanSet.iterator(); it.hasNext();) {
            final Map.Entry entry = (Entry) it.next();
            monitorSPI = (FileSystemMonitorSPI) entry.getValue();
            if (monitorSPI != null && monitorSPI.isAvailable() && monitorSPI.canWatch(osType)) {
                ret=monitorSPI;
                if (monitorSPI.getType()==type){
                    break;
                }
            }
        }
        if (ret != null){
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Creating an instance of: "+monitorSPI.getClass());
            return ret.createInstance(config);
        }

        return null;

    }

    public void afterPropertiesSet() throws Exception {
        if (applicationContext == null)
            throw new IllegalStateException("The provided applicationContext is null!");
        FSMSPIFinder.registry = this;
    }
}
