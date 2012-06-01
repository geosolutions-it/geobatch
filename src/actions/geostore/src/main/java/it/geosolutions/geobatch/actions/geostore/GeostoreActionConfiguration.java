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
package it.geosolutions.geobatch.actions.geostore;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

/**
 * 
 * @author Luca Paolino - luca.paolino@geo-solutions.it
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class GeostoreActionConfiguration extends ActionConfiguration {

    public GeostoreActionConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    String url = null;
    String user = null;
    String password = null;

    GeostoreOperation.Operation operation = null;
    boolean shortResource = true;

    public GeostoreOperation.Operation getOperation() {
        return operation;
    }

    public void setOperation(GeostoreOperation.Operation operation) {
        this.operation = operation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isShortResource() {
        return shortResource;
    }

    public void setShortResource(boolean shortResource) {
        this.shortResource = shortResource;
    }

    @Override
    public GeostoreActionConfiguration clone() {
        final GeostoreActionConfiguration ret = (GeostoreActionConfiguration)super.clone();

        return ret;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "url=" + url + ", user=" + user + ", overrideConfigDir="
               + getOverrideConfigDir() + ", super=" + super.toString() + ']';
    }
}
