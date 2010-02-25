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

package it.geosolutions.geobatch.catalog.event;

/**
 * @author Alessio
 * 
 */
public interface CatalogListener {
    /**
     * Handles the event of an addition to the catalog.
     */
    <T, C extends CatalogAddEvent<T>> void handleAddEvent(C event);

    /**
     * Handles the event of a removal from the catalog.
     * 
     */
    <T, C extends CatalogRemoveEvent<T>> void handleRemoveEvent(C event);

    /**
     * Handles the event of a modification to an object in the catalog.
     */
    <T, C extends CatalogModifyEvent<T>> void handleModifyEvent(C event);
}
