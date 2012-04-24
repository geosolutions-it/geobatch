/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
 * Check the configuration to find out where the GeoBatch operation dirs are located.
 * <br/>
 * There are two main directories in GeoBatch:<ul>
 * <li><B>GEOBATCH_CONFIG_DIR</B>: this is where the configuration files are stored</li>
 * <li><B>GEOBATCH_TEMP_DIR</B>: this is there runtime temporary files are created</li>
 * </ul>
  * The old (deprecated) way consisted in a single directory, named <B>GEOBATCH_DATA_DIR</B> which contained both configuration and
 * temporary data.<br/>
 * DataDirHandler will try to retrieve the value for GEOBATCH_CONFIG_DIR. <br/>
 * If not found, it will try to retrieve a setting for the older GEOBATCH_DATA_DIR.<br/><br/>
 *
 * Then it will try to retrieve the value for GEOBATCH_TEMP_DIR. If not found, the temp dir will be set to GEOBATCH_CONFIG_DIR/temp/.
 * If this directory does not exists it will be created, an IllegalStateException is thrown if it is not writeable.
 *
 * <li></li>
 * <li></li>
 *
 * @author Etj
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class DataDirHandler implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataDirHandler.class);

    public static final String GEOBATCH_DATA_DIR = "GEOBATCH_DATA_DIR";
    public static final String GEOBATCH_CONFIG_DIR = "GEOBATCH_CONFIG_DIR";
    public static final String GEOBATCH_TEMP_DIR = "GEOBATCH_TEMP_DIR";

    private ApplicationContext applicationContext;
    /**
     * The base directory where the configuration files are located. <br/>
     */
    private File baseConfigDir;
    /**
     * The temporary directory where the temporary files are created. <br/>
     */
    private File baseTempDir;

    public DataDirHandler() {
    }

    /**
     * init method called by Spring
     *
     * @throws Exception if could not init data dir
     */
    public void init() throws Exception {
        boolean obsolete = false;
        baseConfigDir = retrieveConfiguredDir(GEOBATCH_CONFIG_DIR);
        if(baseConfigDir == null) {
            obsolete = true;
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("No " + GEOBATCH_CONFIG_DIR + " configuration was found. Will try for older " + GEOBATCH_DATA_DIR);

            baseConfigDir = retrieveConfiguredDir(GEOBATCH_DATA_DIR);
            if(baseConfigDir == null) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("No " + GEOBATCH_DATA_DIR + " configuration was found. Will try to force a default one.");
                baseConfigDir = forceDataDir(); // assign or throw
            }
        }
        if (LOGGER.isInfoEnabled()){
                LOGGER.info("----------------------------------");
            if(obsolete) {
                LOGGER.info("- OBSOLETE GEOBATCH_DATA_DIR: " + baseConfigDir.getAbsolutePath());
                LOGGER.info("- Please update this configuration using GEOBATCH_CONFIG_DIR setting");
            } else
                LOGGER.info("- GEOBATCH_CONFIG_DIR: " + baseConfigDir.getAbsolutePath());
            LOGGER.info("----------------------------------");
        }

        // and now for the TEMP_DIR

        baseTempDir = retrieveConfiguredDir(GEOBATCH_TEMP_DIR);
        if(baseTempDir == null) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("No " + GEOBATCH_TEMP_DIR+ " configuration was found. Will be forced.");
            baseTempDir = new File(baseConfigDir, "temp");
            if (!baseTempDir.exists()){
                baseTempDir.mkdir();
                if (!baseTempDir.canRead() || !baseTempDir.canWrite()){
                    throw new IllegalStateException("Unable to read/write from the temp dir:"+baseTempDir);
                }
            }
        }
        if (LOGGER.isInfoEnabled()){
            LOGGER.info("----------------------------------");
            LOGGER.info("- GEOBATCH_TEMP_DIR: " + baseTempDir.getAbsolutePath());
            LOGGER.info("----------------------------------");
        }
    }

    /**
     *
     * @deprecated use {@link #getBaseConfigDirectory() } or {@link #getBaseTempDirectory() }
     */
    public File getDataDirectory() {
        return this.baseConfigDir;
    }

    /**
     *
     * @return the directory where the catalog and the flow configurations are stored.
     */
    public File getBaseConfigDirectory() {
        return this.baseConfigDir;
    }

    /**
     *
     * @return the base directory where the temporary directories will be created in.
     */
    public File getBaseTempDirectory() {
        return this.baseTempDir;
    }
   
    /**
     * Try some well know places where the data dir may be located.
     * <br/> A valid dir is returned, or an exception will be thrown.
     *
     * @return a valid dir
     *
     * @throws NullPointerException
     * @throws IllegalStateException
     */
    protected File forceDataDir() throws NullPointerException, IllegalStateException {
        File ret = null;
        if ( this.applicationContext instanceof WebApplicationContext ) {
            String rootDir = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/WEB-INF/data");
            if ( rootDir != null ) {
                ret = new File(rootDir);
                if ( LOGGER.isInfoEnabled() ) {
                    LOGGER.info("data dir automatically set inside webapp");
                }
            }
        } else {
            ret = new File("./data");
            if ( LOGGER.isInfoEnabled() ) {
                LOGGER.info("data dir automatically set in current dir");
            }
        }

        if ( ret == null ) {
            throw new NullPointerException("Could not initialize Data Directory.");
        }

        if ( !ret.exists() ) {
            throw new IllegalStateException(
                    "Could not initialize Data Directory: The provided path does not exists ("
                    + ret + ").");
        }

        if ( !ret.isDirectory() || !ret.canRead() ) {
            throw new IllegalStateException(
                    "Could not initialize Data Directory: The provided path is not a readable directory ("
                    + ret + ")");
        }

        return ret;
    }

    /**
     * Try to retrieve the info about where the requested property dir is located.
     *
     * @param propertyName a property name referring to a string containing a path
     *
     * @return a valid dir or null.
     *
     * @throws NullPointerException
     * @throws IllegalStateException
     */
    protected File retrieveConfiguredDir(String propertyName) throws NullPointerException, IllegalStateException {
        File ret = null;

//        try {
            String prop = System.getProperty(propertyName);
            if ( prop != null ) {
                ret = new File(prop);
                if ( LOGGER.isInfoEnabled() ) {
                    LOGGER.info(propertyName + " read from property");
                }
            } else {
                prop = System.getenv(propertyName);
                if ( prop != null ) {
                    ret = new File(prop);
                    if ( LOGGER.isInfoEnabled() ) {
                        LOGGER.info(propertyName + " read from environment var");
                    }
                } else {
                    if ( this.applicationContext instanceof WebApplicationContext ) {
                        final WebApplicationContext wContext = (WebApplicationContext) applicationContext;
                        final ServletContext servletContext = wContext.getServletContext();
                        String rootDir = servletContext.getInitParameter(propertyName);
                        if ( rootDir != null ) {
                            ret = new File(rootDir);
                            if ( LOGGER.isInfoEnabled() ) {
                                LOGGER.info(propertyName + " read from servlet init param");
                            }
                        }
                    }
                }
            }
//        } catch (SecurityException e) {
//            // gobble exception
//            if ( LOGGER.isInfoEnabled() ) {
//                LOGGER.info(e.getLocalizedMessage(), e);
//            }
//        }

        if ( ret == null ) {
            return null;
        }

        if ( !ret.exists() ) {
            throw new IllegalStateException(
                    "Could not initialize " + propertyName + ": The provided path does not exists ("
                    + ret + ")");
        }

        if ( !ret.isDirectory() || !ret.canRead() ) {
            throw new IllegalStateException(
                    "Could not initialize " + propertyName + ": The provided path is not a readable directory ("
                    + ret + ")");
        }

        if( ! ret.isAbsolute())
            LOGGER.warn("The configured " + propertyName + " is not absolute: " + ret);

        return ret;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [CONFIG:" + baseConfigDir + " TEMP:"+baseTempDir+"]";
    }

    // ==========================================================================
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
