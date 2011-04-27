/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.global;

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.catalog.Service;
import it.geosolutions.geobatch.catalog.dao.DAO;
import it.geosolutions.geobatch.catalog.dao.file.xstream.XStreamCatalogDAO;
import it.geosolutions.geobatch.catalog.dao.file.xstream.XStreamFlowConfigurationDAO;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedCatalogConfiguration;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * The application configuration facade.
 * 
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public class XStreamCatalogLoader extends CatalogHolder implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(XStreamCatalogLoader.class.toString());

    private final Alias alias;

    // enforcing singleton
    private XStreamCatalogLoader(Catalog catalog, Alias alias) {
        CatalogHolder.setCatalog(catalog);
        this.alias = alias;
    }

    ApplicationContext context;

    /**
     * GeoBatch data dir. This directory is used by the GeoBatch to store Flows configuration files.
     */
    private File dataDir;

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;

    }

    @SuppressWarnings("unchecked")
    public void init() throws Exception {

        configureDataDir();

        ((FileBaseCatalog) CatalogHolder.getCatalog()).setBaseDirectory(dataDir.getAbsolutePath());
        System.out.println("----------------------------------");
        System.out.println("- GEOBATCH_DATA_DIR: " + dataDir.getAbsolutePath());
        System.out.println("----------------------------------");

        
        // //
        //
        // force loading all alias registrars
        //
        // //
        context.getBeansOfType(AliasRegistrar.class);

        // //
        //
        // Now get the catalog we have been injected
        //
        // //
        final Catalog catalog = getCatalog();
        final FileBasedCatalogConfiguration configuration = 
            new FileBasedCatalogConfiguration(catalog.getId(), catalog.getName(), catalog.getDescription(), true);
        catalog.setConfiguration(configuration);
        catalog.setDAO(new XStreamCatalogDAO(dataDir.getAbsolutePath(), alias));
        catalog.load();

        // //
        //
        // Force loading all all services
        //
        // //
        final Map<String, ? extends Service> services = context.getBeansOfType(Service.class);
        for (Entry<String, ? extends Service> servicePair : services.entrySet()) {
            final Service service = servicePair.getValue();
            if (!service.isAvailable()) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Skipping service " + servicePair.getKey() + " (" +service.getClass()+ ")" );
                continue;
            }
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Loading service " + servicePair.getKey() + " (" +service.getClass()+ ")");
            catalog.add(servicePair.getValue());
        }

        // //
        //
        // load all flows
        //
        // //
        final Iterator<File> it = FileUtils.iterateFiles(dataDir, new String[] { "xml" }, false);
        while (it.hasNext()) {
            final File flowConfigFile = it.next();

            // skip catalog config file
            if (flowConfigFile.getName().equalsIgnoreCase(catalog.getId() + ".xml"))
                continue;

            try {

                // loaded
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Loading flow from file " + flowConfigFile.getAbsolutePath());

                // try to load the flow and add it to the catalog
// TODO change this:
                final FileBasedFlowManager flowManager = new FileBasedFlowManager(
                        FilenameUtils.getBaseName(flowConfigFile.getName()),
                        configuration.getName(),
                        configuration.getDescription());
//                flow.setId(FilenameUtils.getBaseName(o.getName()));
                DAO flowLoader = new XStreamFlowConfigurationDAO(dataDir.getAbsolutePath(), alias);
                flowManager.setDAO(flowLoader);
                flowManager.load();
// TODO ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                // add to the catalog
                catalog.add(flowManager);
                
                // loaded
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(new StringBuilder("Loaded flow from file ").append(
                            flowConfigFile.getAbsolutePath()).toString());
            } catch (Throwable t) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Skipping flow", t);
            }

        }

    }

    /**
     * Try to retrieve the info about where the data dir is located.
     * <br/>
     * On exit, the <tt>datadir</tt> var will be set, or an exception will be thrown.
     * 
     * @throws NullPointerException
     * @throws IllegalStateException 
     */
    protected void configureDataDir() throws NullPointerException, IllegalStateException {
        try {

            if (dataDir == null) {
                String prop = System.getProperty("GEOBATCH_DATA_DIR");
                if (prop != null) {
                    dataDir = new File(prop); 
                    if(LOGGER.isInfoEnabled()) 
                        LOGGER.info("data dir read from property");
                } else {
                    prop = System.getenv("GEOBATCH_DATA_DIR");
                    if (prop != null) {
                        dataDir = new File(prop);
                        if(LOGGER.isInfoEnabled()) 
                            LOGGER.info("data dir read from environment var");                        
                    } else {
                        if (this.context instanceof WebApplicationContext) {
                            final WebApplicationContext wContext = (WebApplicationContext) context;
                            final ServletContext servletContext = wContext.getServletContext();
                            String rootDir = servletContext.getInitParameter("GEOBATCH_DATA_DIR");
                            if (rootDir != null) {
                                dataDir = new File(rootDir); 
                                if(LOGGER.isInfoEnabled()) 
                                    LOGGER.info("data dir read from servlet init param");
                            }else {
                                rootDir = ((WebApplicationContext) context).getServletContext().getRealPath("/WEB-INF/data");
                                if (rootDir != null) {
                                    dataDir = new File(rootDir);
                                    if(LOGGER.isInfoEnabled()) 
                                        LOGGER.info("data dir automatically set inside webapp");
                                    
                                }
                            }
                        } else {
                            dataDir = new File("./data");
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

        if (dataDir == null)
            throw new NullPointerException(
                    "Could not initialize Data Directory: The provided path is null.");

        if (!dataDir.exists())
            throw new IllegalStateException(
                    "Could not initialize Data Directory: The provided path does not exists ("
                            + dataDir + ").");

        if (!dataDir.isDirectory() || !dataDir.canRead())
            throw new IllegalStateException(
                    "Could not initialize Data Directory: The provided path is not a readable directory ("
                            + dataDir + ")");
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }
    
    public File getDataDir() {
        return dataDir;
    }

}
