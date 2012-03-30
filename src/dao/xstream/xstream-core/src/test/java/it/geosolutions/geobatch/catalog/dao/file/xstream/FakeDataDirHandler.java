/*
 *  Copyright (C) 2007 - 2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.catalog.dao.file.xstream;

import it.geosolutions.geobatch.catalog.file.DataDirHandler;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class FakeDataDirHandler extends DataDirHandler implements ApplicationContextAware {

    private final static Logger LOGGER = LoggerFactory.getLogger(FakeDataDirHandler.class);

    private File dataDirectory;
    public void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * @deprecated
     */
    @Override
    public File getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public File getConfigDirectory() {
        return dataDirectory;
    }

    @Override
    public File getBaseTempDirectory() {
        throw new UnsupportedOperationException("Not intended to provide this functionality");
    }


    public void init() throws Exception {
        LOGGER.warn("Overriding init() call");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            super.setApplicationContext(applicationContext);

            Resource resource = applicationContext.getResource("data");
            dataDirectory = resource.getFile();
            LOGGER.info("DATA DIR: " + dataDirectory);

        } catch (IOException ex) {
            throw new BeanInitializationException("Can't set data dir", ex);
        }
    }


}
