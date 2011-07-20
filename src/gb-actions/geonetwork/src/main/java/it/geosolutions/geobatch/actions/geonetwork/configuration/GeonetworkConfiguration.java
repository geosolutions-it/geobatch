/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.geonetwork.configuration;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

/**
 * TODO: implement the clone() method
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkConfiguration 
        extends ActionConfiguration 
        implements Configuration {
    
    public GeonetworkConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * URL where the GN services can be accessed.
     */
    private String geonetworkServiceURL;

    /**
     * Credential for accessing to GeoNetwork services.
     */
    private String loginUsername;
    /**
     * Credential for accessing to GeoNetwork services.
     */
    private String loginPassword;

    // we shall tell the action how the metadata to be deleted will be passed to
    // the action. atm we use a file representing a full request.

    public String getGeonetworkServiceURL() {
        return geonetworkServiceURL;
    }

    public void setGeonetworkServiceURL(String geonetworkServiceURL) {
        this.geonetworkServiceURL = geonetworkServiceURL;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

}
