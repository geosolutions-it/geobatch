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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class GBSettingsCatalogImpl implements GBSettingsCatalog {
    private static Logger LOGGER = LoggerFactory.getLogger(GBSettingsCatalogImpl.class);

    private GBSettingsDAO settingsDAO;
    
    private Set<GBSettingsListener> listeners = new HashSet<GBSettingsListener>();

    @Override
    public List<String> getIds() {
        return settingsDAO.getIds();
    }


    @Override
    public GBSettings find(String id) throws Exception {
        return settingsDAO.find(id);
    }

    @Override
    public boolean save(GBSettings settings) {
        notifyBeforeSave(settings);
        boolean ret = false;
        try{
            ret = settingsDAO.save(settings);
        } catch(Exception e) {        
        }
        notifyAfterSave(settings, ret);
        return ret;
    }

    //==========================================================================

    private void notifyBeforeSave(GBSettings settings) {
        if(LOGGER.isTraceEnabled())
            LOGGER.trace("Notifying setting beforeSave on " + settings);
        for (GBSettingsListener listener : listeners) {
            try{
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("Notifying setting presave on " + settings + " to " + listener.getClass().getSimpleName());
                listener.beforeSave(settings);
            }catch(Exception e) {
                LOGGER.warn("Listener " + listener.getClass().getSimpleName() + " threw exception in notification", e);
            }
        }
    }

    private void notifyAfterSave(GBSettings settings, boolean ret) {
        if(LOGGER.isTraceEnabled())
            LOGGER.trace("Notifying setting afterSave on " + settings);
        for (GBSettingsListener listener : listeners) {
            try{
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("Notifying setting afterSave on " + settings + " to " + listener.getClass().getSimpleName());
                listener.afterSave(settings, ret);
            }catch(Exception e) {
                LOGGER.warn("Listener " + listener.getClass().getSimpleName() + " threw exception in notification", e);
            }
        }
    }

    //==========================================================================

    @Override
    public void flush() {
        LOGGER.info("Flushing");
    }

    //==========================================================================

    @Override
    public Collection<GBSettingsListener> getListeners() {
        return Collections.unmodifiableCollection(listeners);
    }

    @Override
    public void addListener(GBSettingsListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean removeListener(GBSettingsListener listener) {
        return listeners.remove(listener);
    }

    //==========================================================================

    public void setSettingsDAO(GBSettingsDAO settingsDAO) {
        this.settingsDAO = settingsDAO;
    }

}
