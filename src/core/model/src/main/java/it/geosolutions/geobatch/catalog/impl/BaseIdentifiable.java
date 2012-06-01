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

package it.geosolutions.geobatch.catalog.impl;

import it.geosolutions.geobatch.catalog.Identifiable;

public abstract class BaseIdentifiable implements Identifiable, Cloneable {

    
    private String id;
    
    /**
     * A constructor which do not initialize the resource id
     * @deprecated use the complete constructor
     */
    protected BaseIdentifiable() {
    }

    /**
     * Constructor forcing initialization of: id ,name and description of this resource 
     * @param id
     */
    public BaseIdentifiable(String id) {
        this.id = id;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @param id  the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public BaseIdentifiable clone() {
        try {
            BaseIdentifiable bi = (BaseIdentifiable) super.clone();
            return bi;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

}