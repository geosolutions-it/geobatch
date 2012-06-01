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

package it.geosolutions.geobatch.catalog.event;

/**
 * @author Alessio
 * 
 */
public interface CatalogListener {
    /**
     * Handles the event of an addition to the catalog.
     */
    <T, CAE extends CatalogAddEvent<T>> void handleAddEvent(CAE event);

    /**
     * Handles the event of a removal from the catalog.
     * 
     */
    <T, CRE extends CatalogRemoveEvent<T>> void handleRemoveEvent(CRE event);

    /**
     * Handles the event of a modification to an object in the catalog.
     */
    <T, CME extends CatalogModifyEvent<T>> void handleModifyEvent(CME event);
}
