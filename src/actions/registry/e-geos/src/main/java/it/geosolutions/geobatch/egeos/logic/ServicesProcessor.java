/*
 *  Copyright (C) 2007 - 2010 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.egeos.logic;

import it.geosolutions.geobatch.egeos.types.dest.CollectionRO;
import it.geosolutions.geobatch.egeos.types.dest.ServiceRO;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class ServicesProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServicesProcessor.class);

    private Map<String, URL> services = new HashMap<String, URL>();

    public ServicesProcessor(String... getCapURLs) {
        for (String getCap : getCapURLs) {
            if (getCap.toUpperCase().indexOf("SERVICE=") > 0) {
                int beginIndex = getCap.toUpperCase().indexOf("SERVICE=") + "SERVICE=".length();
                int endIndex = beginIndex + 3;
                String serviceId = getCap.substring(beginIndex, endIndex);
                try {
                    URL getCapURL = new URL(getCap);
                    services.put(serviceId.toUpperCase(), getCapURL);
                } catch (MalformedURLException e) {
                    LOGGER.warn("Could not process getCapabilitiesURL " + getCap
                            + ": malformed URL", e);
                }
            } else {
                LOGGER.warn("Could not process getCapabilitiesURL " + getCap
                        + ": missing SERVICE parameter!");
            }
        }
    }

    public int size() {
        return services.size();
    }

    public Set<String> getServiceIDs() {
        return services.keySet();
    }

    public URL getServiceGetCapURL(String serviceId) {
        return services.get(serviceId.toUpperCase());
    }

    public static ServiceRO serviceRO(String serviceId, URL getCapURL) {
        ServiceRO service = new ServiceRO();
        service.setId(serviceId);
        service.setGetCapURL(getCapURL.toExternalForm());

        return service;
    }
    
    public static CollectionRO collectionRO(String collectionId) {
        CollectionRO collection = new CollectionRO();
        collection.setId(collectionId);
        
        return collection;
    }
}
