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
    /**
     * the key of the ServiceID which should match to the GeoBatch's action you want to call
     */
    public final static String SERVICE_ID_KEY = "SERVICE_ID";
    /**
     * the key of the input list of files will be passed to the running action  
     */
    public final static String INPUT_KEY = "INPUT";

    /**
     * returns the status of the selected consumer
     * 
     * @param uuid
     * @return {@link ConsumerStatus}
     */
    public ConsumerStatus getStatus(final String uuid);

    /**
     * create the configured action on the remote GeoBatch server through the
     * JMX connection
     * 
     * @param config A map containing the list of needed parameters, inputs and
     *            outputs used by the action
     * @return uuid a string representing the remote consumer ID or null if
     *         GeoBatch is unable to create the consumer (remotely)
     * @throws Exception if:
     *             <ul>
     *             <li>the passed map is null</li>
     *             <li>the passed map doesn't contains needed keys</li>
     *             <li>the connection is lost</li>
     *             </ul>
     */
    public String callAction(Map<String, String> config) throws Exception;

    /**
     * Used to dispose the consumer instance from the consumer registry.
     * 
     * @param uuid the unique id of the remote consumer to dispose
     * @throws Exception if:
     *             <ul>
     *             <li>the consumer is uuid is null</li>
     *             <li>the consumer is already disposed</li>
     *             <li>the connection is lost</li>
     *             </ul>
     */
    public void disposeAction(final String uuid) throws Exception;

}
