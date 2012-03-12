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

import it.geosolutions.geobatch.catalog.impl.BaseCatalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * A Catalog based on an xml marshalled file.
 *
 * @author Simone Giannecchini, GeoSolutions
 */
@SuppressWarnings("unchecked")
public class FileBasedCatalogImpl extends BaseCatalog implements FileBaseCatalog, ApplicationContextAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedCatalogImpl.class);

    public DataDirHandler dataDirHandler;

    private ApplicationContext applicationContext;

    /**
     * The base directory where the configuration files are located.
     * <br/>The workingDirectory will be relative to this base directory unless
     * an absolute path will be specified.
     */
    private File dataDir;

    public FileBasedCatalogImpl(String id, String name, String description)
    {
        super(id, name, description);
    }

    /**
     * init method called by Spring
     * @throws Exception if could not init data dir
     */
    public void init() throws Exception
    {
        dataDir = dataDirHandler.getDataDirectory();
    }


    public File getBaseDirectory()
    {
        return this.dataDir;
    }

    protected void setBaseDirectory(final File baseDirectory)
    {
        LOGGER.warn("Setting datadir to '" + baseDirectory + "', was '" + this.dataDir + "'");
        this.dataDir = baseDirectory;
    }


    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " [" + dataDir + "]";
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    public DataDirHandler getDataDirHandler()
    {
        return dataDirHandler;
    }

    public void setDataDirHandler(DataDirHandler dataDirHandler)
    {
        this.dataDirHandler = dataDirHandler;
    }

}
