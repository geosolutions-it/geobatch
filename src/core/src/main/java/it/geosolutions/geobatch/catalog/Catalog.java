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
package it.geosolutions.geobatch.catalog;

import it.geosolutions.geobatch.catalog.event.CatalogListener;
import it.geosolutions.geobatch.configuration.CatalogConfiguration;
import it.geosolutions.geobatch.configuration.flow.FlowConfiguration;
import it.geosolutions.geobatch.flow.FlowManager;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;

/**
 * The GeoBatch catalog which provides access to meta information about the Flow
 * BaseEventConsumer Types and Flow Managers.
 * <p>
 * The following types of metadata are stored:
 * <ul>
 * <li>flow manager types</li>
 * <li>flow managers resources</li>
 * </ul>
 * </p>
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public interface Catalog extends PersistentResource<CatalogConfiguration> {
    /**
     * Adds a new resource.
     */
    <E extends EventObject, C extends FlowConfiguration> void add(FlowManager<E, C> resource);

    /**
     * Disposes the catalog, freeing up any resources.
     */
    void dispose();

    <E extends EventObject, C extends FlowConfiguration, T extends FlowManager<E, C>> List<T> getFlowManagers(
            Class<T> clazz);

    <E extends EventObject, C extends FlowConfiguration, T extends FlowManager<E, C>> T getFlowManager(
            String id, Class<T> clazz);

    <E extends EventObject, C extends FlowConfiguration, T extends FlowManager<E, C>> T getFlowManagerByName(
            String name, Class<T> clazz);

    <T extends Resource> T getResource(String id, Class<T> clazz);

    <T extends Resource> T getResourceByName(String name, Class<T> clazz);

    <T extends Resource> List<T> getResources(Class<T> clazz);

    <T extends Resource> void add(T resource);

    /**
     * Removes an existing resource.
     */
    <E extends EventObject, C extends FlowConfiguration> void remove(FlowManager<E, C> resource);

    /**
     * Saves a resource which has been modified.
     */
    <E extends EventObject, C extends FlowConfiguration> void save(FlowManager<E, C> resource);

    /**
     * catalog listeners.
     * 
     */
    Collection<CatalogListener> getListeners();

    /**
     * Adds a listener to the catalog.
     */
    void addListener(CatalogListener listener);

    /**
     * Removes a listener from the catalog.
     */
    void removeListener(CatalogListener listener);

}
