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
import java.util.List;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public interface GBSettingsCatalog {


    List<String> getIds();

    /**
     * @return the GBSetting requested, or nulll if none was found.
     */
    GBSettings find(String id) throws Exception;

    /**
     * Store the settings, overwriting previous ones.
     * @return true on success.
     */
    boolean save(GBSettings settings);

    /**
     * clear internal cache and force reloading when needed.
     */
    void flush();

    //==========================================================================
    /**
     * catalog listeners.
     *
     */
    Collection<GBSettingsListener> getListeners();

    /**
     * Adds a listener to the catalog.
     */
    void addListener(GBSettingsListener listener);

    /**
     * Removes a listener from the catalog.
     */
    boolean removeListener(GBSettingsListener listener);

}
