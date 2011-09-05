/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.catalog.file;

import java.io.File;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * Check the configuration to find out where the data dir is located.
 * 
 * @author Etj
 */
public class DataDirHandler 
    implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataDirHandler.class);
        
    public final static String GEOBATCH_DATA_DIR = "GEOBATCH_DATA_DIR";
    
    private ApplicationContext applicationContext;
    
    /**
     * The base directory where the configuration files are located. 
     * <br/>The workingDirectory will be relative to this base directory unless 
     * an absolute path will be specified.
     */
    private File dataDir;

    public DataDirHandler() {
    }

    /**
     * init method called by Spring
     * @throws Exception if could not init data dir
     */
    public void init() throws Exception {
        dataDir = retrieveDataDir();

        System.out.println("----------------------------------");
        System.out.println("- GEOBATCH_DATA_DIR: " + dataDir.getAbsolutePath());
        System.out.println("----------------------------------");
    }
    
    
    public File getDataDirectory() {
        return this.dataDir;
    }
    
    /**
     * Try to retrieve the info about where the data dir is located.
     * <br/>
     * On exit, the <tt>datadir</tt> var will be set, or an exception will be thrown.
     * 
     * @throws NullPointerException
     * @throws IllegalStateException 
     */
    protected File retrieveDataDir() throws NullPointerException, IllegalStateException {
        File ret = null;
        
        try {

            if (ret == null) {
                String prop = System.getProperty(GEOBATCH_DATA_DIR);
                if (prop != null) {
                    ret = new File(prop); 
                    if(LOGGER.isInfoEnabled()) 
                        LOGGER.info("data dir read from property");
                } else {
                    prop = System.getenv(GEOBATCH_DATA_DIR);
                    if (prop != null) {
                        ret = new File(prop);
                        if(LOGGER.isInfoEnabled()) 
                            LOGGER.info("data dir read from environment var");                        
                    } else {
                        if (this.applicationContext instanceof WebApplicationContext) {
                            final WebApplicationContext wContext = (WebApplicationContext) applicationContext;
                            final ServletContext servletContext = wContext.getServletContext();
                            String rootDir = servletContext.getInitParameter(GEOBATCH_DATA_DIR);
                            if (rootDir != null) {
                                ret = new File(rootDir); 
                                if(LOGGER.isInfoEnabled()) 
                                    LOGGER.info("data dir read from servlet init param");
                            }else {
                                rootDir = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/WEB-INF/data");
                                if (rootDir != null) {
                                    ret = new File(rootDir);
                                    if(LOGGER.isInfoEnabled()) 
                                        LOGGER.info("data dir automatically set inside webapp");
                                    
                                }
                            }
                        } else {
                            ret = new File("./data");
                            if(LOGGER.isInfoEnabled()) 
                                LOGGER.info("data dir automatically set in current dir");
                        }
                    }
                }
            }

        } catch (SecurityException e) {
            // gobble exception
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
        }

        if (ret == null)
            throw new NullPointerException(
                    "Could not initialize Data Directory: The provided path is null.");

        if (!ret.exists())
            throw new IllegalStateException(
                    "Could not initialize Data Directory: The provided path does not exists ("
                            + ret + ").");

        if (!ret.isDirectory() || !ret.canRead())
            throw new IllegalStateException(
                    "Could not initialize Data Directory: The provided path is not a readable directory ("
                            + ret + ")");
        return ret;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + dataDir + "]";
    }

    //==========================================================================
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
