/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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

import it.geosolutions.geobatch.catalog.Descriptable;

/**
 * Base class that adds name and description to a Identifiable.
 *
 * Used for all classes that needs some human-friendly identifiers.
 * <p/>
 * Note that some classes (probably inheriting from {@link BaseResource}) may have to implement Descriptable on their own,
 * because of single-parent inheritance.
 *
 * @author Emanuele Tajariol, GeoSolutions
 */
public abstract class BaseDescriptable 
    extends BaseIdentifiable
    implements Descriptable, Cloneable {

    private String name;
    private String description;

//    public BaseDescriptable(String id) {
//        this(id, "unknown", "unknown");
//    }

    public BaseDescriptable(String id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public BaseDescriptable clone() {
        BaseDescriptable bi = (BaseDescriptable) super.clone();
        return bi;
    }

}