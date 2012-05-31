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

import it.geosolutions.geobatch.catalog.DescriptableConfiguration;
import it.geosolutions.geobatch.catalog.ServiceableConfiguration;

/**
 *
 * @author Emanuele Tajariol, GeoSolutions
 */
public abstract class BaseDescriptableConfiguration
    extends BaseConfiguration
    implements DescriptableConfiguration, ServiceableConfiguration, Cloneable {

    private String name;
    private String description;

    /**
    * @deprecated add name and description whenever possible
    */
    public BaseDescriptableConfiguration(String id) {
        this(id, "unknown", "unknown");
    }

    /**
     */
    public BaseDescriptableConfiguration(String id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    public BaseDescriptableConfiguration(String id, String name, String description, boolean dirty) {
        super(id, dirty);
        this.name = name;
        this.description = description;
    }

//    public BaseDescriptableConfiguration(String id, boolean dirty) {
//        super(id, dirty);
//    }

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
    public BaseDescriptableConfiguration clone() {
        BaseDescriptableConfiguration bc = (BaseDescriptableConfiguration) super.clone();
        return bc;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "["
                + "id:" + getId()
                + " name:" + getName()
                + " srvId:" + getServiceID()
                + " drty:" + isDirty() + "]";
    }
}
