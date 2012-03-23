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
package it.geosolutions.geobatch.services.jmx;

import java.util.Map;

/**
 * 
 * Interface used to proxy the JMXServiceManager
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public interface ActionManager {

    public final static String SERVICE_ID_KEY = "SERVICE_ID";
    public final static String INPUT_KEY = "INPUT";

    public final static String FlowManagerID = "JMX_FLOW_MANAGER";


    public ConsumerStatus getStatus(final String uuid);

    public String callAction(Map<String, String> config) throws Exception;

    public void disposeAction(final String uuid) throws Exception;

}
