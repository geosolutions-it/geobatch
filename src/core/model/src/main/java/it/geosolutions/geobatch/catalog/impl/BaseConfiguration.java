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

import it.geosolutions.geobatch.catalog.Configuration;

import org.slf4j.LoggerFactory;

public abstract class BaseConfiguration
    extends BaseIdentifiable
    implements Configuration, Cloneable {
    
    private String serviceID;
    
    private boolean dirty;

    public BaseConfiguration() {
        super();
    }

    /**
     * @deprecated name and description not needed here
     */
    public BaseConfiguration(String id, String name) {
        super(id);
        LoggerFactory.getLogger("ROOT").error("Deprecated constructor called from " + getClass().getName() , new Throwable("TRACE!") );
    }
    
    public BaseConfiguration(String id) {
        super(id);
    }

    /**
     * @deprecated name and description not needed here
     */
    public BaseConfiguration(String id, String name, String description) {
        super(id);
        LoggerFactory.getLogger("ROOT").error("Deprecated constructor called from " + getClass().getName() , new Throwable("TRACE!") );
    }

    /**
     * @deprecated name and description not needed here
     */
    public BaseConfiguration(String id, String name, String description, boolean dirty) {
        super(id);
        this.dirty = dirty;
        LoggerFactory.getLogger("ROOT").error("Deprecated constructor called from " + getClass().getName() , new Throwable("TRACE!") );
    }

    public BaseConfiguration(String id, boolean dirty) {
        super(id);
        this.dirty = dirty;
    }

    /**
     * @return
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * @param dirty
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * @return  the serviceID
     */
    public String getServiceID() {
        return serviceID;
    }

    /**
     * @param serviceID  the serviceID to set
     */
    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    @Override
    public BaseConfiguration clone() {
        BaseConfiguration bc = (BaseConfiguration) super.clone();
//        bc.dirty = this.dirty;
//        bc.serviceID = this.serviceID;
        return bc;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + " srvId:" + serviceID
                + " drty:" + isDirty() + "]";
    }
}
