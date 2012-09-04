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
package it.geosolutions.geobatch.registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ETj <etj at geo-solutions.it>
 */
public class AliasRegistry implements Iterable<Map.Entry<String, Class<?>>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(AliasRegistrar.class.getName());

    private Map<String, Class<?>> alias = new HashMap<String, Class<?>>();
    private Map<String, Class<?>> implicitCollections = new HashMap<String, Class<?>>();
    
    public AliasRegistry() {
    }

    public int size() {
        return alias.size();
    }

    public void putAlias(String name, Class<?> clazz) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Adding alias " + name + " for class " + clazz.getSimpleName());
        alias.put(name, clazz);
    }

    public Iterator<Entry<String, Class<?>>> iterator() {
        return alias.entrySet().iterator();
    }

    public void putImplicitCollection(String name, Class<?> clazz) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Adding implicit collection " + name + " for class " + clazz.getSimpleName());
        implicitCollections.put(name, clazz);
    }

    public Iterable<Entry<String, Class<?>>> implicitCollectionIterator() {
        return implicitCollections.entrySet();
    }

}
