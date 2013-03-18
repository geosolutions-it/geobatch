/*
 *  Copyright (C) 2013 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.services.rest;

import java.util.HashMap;
import java.util.Map;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GeoBatchRESTClient {

    private String username = null;
    private String password = null;
    private String restUrl = null;

    private Map<Class, Object> services  = new HashMap<Class, Object>();

    public GeoBatchRESTClient() {
    }

    //==========================================================================

    // endpoint is how the service is mapped in the CXF servlet.
    // since it's specified in the applicationcontext, it can not be automatically retrieved by the proxy client
    
    protected <T>T getService(Class<T> clazz, String endpoint) {
        if(services.containsKey(clazz))
            return (T)services.get(clazz);

        if(restUrl == null)
            new IllegalStateException("GeoBatch REST URL not set");

        synchronized(services) {
//            T proxy = JAXRSClientFactory.create(restUrl, clazz, username, password, null);
            
            T proxy = JAXRSClientFactory.create(restUrl+"/"+endpoint, clazz);
            String authorizationHeader = "Basic "  + Base64Utility.encode((username+":"+password).getBytes());
            WebClient.client(proxy).header("Authorization", authorizationHeader);

//        WebClient.client(proxy).accept("text/xml");
            services.put(clazz, proxy);
            return proxy;
        }
    }

    //==========================================================================

    public RESTFlowService getFlowService() {
        return getService(RESTFlowService.class, "/");
    }

    //==========================================================================

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRestUrl() {
        return restUrl;
    }

    public void setGeostoreRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

}
