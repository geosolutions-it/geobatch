/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.settings;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class GBSettingsProcessor implements ApplicationContextAware {
    private static Logger LOGGER = LoggerFactory.getLogger(GBSettingsProcessor.class);

    private GBSettingsDAO settingsDAO;
    private GBSettingsCatalog settingsCatalog;

    private ApplicationContext applicationContext;

    public void init() {
        loadListeners();
    }

    private void loadListeners() {
        final Map<String, ? extends GBSettingsListener> listeners = applicationContext.getBeansOfType(GBSettingsListener.class);
        for (Entry<String, ? extends GBSettingsListener> pair : listeners.entrySet()) {
            final GBSettingsListener listener = pair.getValue();
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Loading listener " + pair.getKey() + " (" +listener.getClass().getSimpleName()+ ")" );

            // init the listener
            try {
                listener.onStartup(settingsDAO);
            } catch (Exception e) {
                LOGGER.warn("Error initializing listener " + pair.getKey() + " (" +listener.getClass().getSimpleName()+ ")", e );
            }

            // register the listener
            settingsCatalog.addListener(listener);
        }
    }

    //==========================================================================

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public void setSettingsCatalog(GBSettingsCatalog settingsCatalog) {
        this.settingsCatalog = settingsCatalog;
    }

    public void setSettingsDAO(GBSettingsDAO settingsDAO) {
        this.settingsDAO = settingsDAO;
    }
}
