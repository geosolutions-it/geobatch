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

package it.geosolutions.geobatch.action.splitting;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.util.Collections;
import java.util.List;

public class SplittingConfiguration extends ActionConfiguration {

    /**
     * Script params are stored as String serviceIDs, since it must be really dynamic and we don't
     * want to configure the Conf marshaller (e.g. XStream) with its details.
     */
    private List<String> serviceIDs = null;
    
    public SplittingConfiguration(String id, String name, String description) {
        super(id, name, description);
    }


    public List<String> getServiceIDs() {
        return serviceIDs;
    }

    public void setServiceIDs(List<String> serviceIDs) {
        this.serviceIDs = serviceIDs;
    }

    @Override
    public SplittingConfiguration clone() {
        final SplittingConfiguration configuration = (SplittingConfiguration) super.clone();

        configuration.setServiceIDs(Collections.unmodifiableList(getServiceIDs()));

        return configuration;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + ", serviceId:" + getServiceID()
                + ", name:" + getName() + "]";
    }

}