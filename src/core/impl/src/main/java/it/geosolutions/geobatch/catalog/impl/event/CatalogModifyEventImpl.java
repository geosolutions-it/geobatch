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



package it.geosolutions.geobatch.catalog.impl.event;

import it.geosolutions.geobatch.catalog.event.CatalogModifyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public class CatalogModifyEventImpl<T> extends CatalogEventImpl<T> implements CatalogModifyEvent<T> {
    public CatalogModifyEventImpl(final T source) {
        super(source);
    }

    private List<String> propertyNames = new ArrayList<String>();

    private List<T> oldValues = new ArrayList<T>();

    private List<T> newValues = new ArrayList<T>();

    public void setPropertyNames(final List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    public void setNewValues(final List<T> newValues) {
        this.newValues = newValues;
    }

    public void setOldValues(final List<T> oldValues) {
        this.oldValues = oldValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.event.CatalogModifyEvent#getNewValues()
     */
    public List<T> getNewValues() {
        return newValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.event.CatalogModifyEvent#getOldValues()
     */
    public List<T> getOldValues() {
        return oldValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.catalog.event.CatalogModifyEvent#getPropertyNames()
     */
    public List<String> getPropertyNames() {
        return propertyNames;
    }
}
