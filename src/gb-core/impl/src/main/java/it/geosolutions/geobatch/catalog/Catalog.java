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

import it.geosolutions.geobatch.catalog.dao.DAO;
import it.geosolutions.geobatch.catalog.event.CatalogListener;
import it.geosolutions.geobatch.configuration.CatalogConfiguration;
import it.geosolutions.geobatch.configuration.flow.FlowConfiguration;
import it.geosolutions.geobatch.flow.FlowManager;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;

/**
 * The GeoBatch catalog which provides access to meta information about the Flow BaseEventConsumer
 * Types and Flow Managers.
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
    <EO extends EventObject, FC extends FlowConfiguration> void add(FlowManager<EO, FC> resource);

    <EO extends EventObject, FC extends FlowConfiguration, FM extends FlowManager<EO, FC>> List<FM> getFlowManagers(
            Class<FM> clazz);

    <EO extends EventObject, FC extends FlowConfiguration, FM extends FlowManager<EO, FC>> FM getFlowManager(
            String id, Class<FM> clazz);

    <EO extends EventObject, FC extends FlowConfiguration, FM extends FlowManager<EO, FC>> FM getFlowManagerByName(
            String name, Class<FM> clazz);

    <R extends Resource> R getResource(String id, Class<R> clazz);

    <R extends Resource> R getResourceByName(String name, Class<R> clazz);

    <R extends Resource> List<R> getResources(Class<R> clazz);

    <R extends Resource> void add(R resource);

    /**
     * Removes an existing resource.
     */
    <EO extends EventObject, FC extends FlowConfiguration> void remove(FlowManager<EO, FC> resource);

    /**
     * Saves a resource which has been modified.
     */
    <E extends EventObject, FC extends FlowConfiguration> void save(FlowManager<E, FC> resource);

    <C extends Configuration> void setDAO(DAO<C, ?> dao);

    <C extends Configuration> DAO<C, ?> getDAO();

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

    /**
     * Disposes the catalog, freeing up any resources.
     */
    void dispose();

}
