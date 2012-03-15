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
package it.geosolutions.geobatch.services;

import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
@ManagedResource(objectName = "bean:name=JMXActionManager", description = "My Managed Bean", log = true, logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200, persistLocation = "foo", persistName = "bar")
public interface ActionManager {

    public final static String SERVICE_ID_KEY = "SERVICE_ID";
    public final static String INPUT_KEY = "INPUT";
    
    public final static String FlowManagerID = "JMX_FLOW_MANAGER";
    /**
     * @param uuid of the consumer
     * @return IDLE: return 4<br>
     *         WAITING: return 3<br>
     *         PAUSED: return 2<br>
     *         EXECUTING: return 1<br>
     *         COMPLETED: return 0<br>
     *         CANCELED: return -1<br>
     *         FAILED: return -2<br>
     *         UUID not found return -3;<br>
     * @see {@link EventConsumerStatus}
     */
    @org.springframework.jmx.export.annotation.ManagedOperation(description = "get the status of the selected consumer")
    @ManagedOperationParameters({@ManagedOperationParameter(name = "uuid", description = "The uuid of the consumer")})
    public int getStatus(String uuid);

    @org.springframework.jmx.export.annotation.ManagedOperation(description = "callAction")
    @ManagedOperationParameters({@ManagedOperationParameter(name = "config", description = "A map containing the list of needed paramethers, inputs and outputs used by the action")})
    public String callAction(Map<String, String> config) throws IllegalAccessException,
        InvocationTargetException, SecurityException, NoSuchMethodException, IllegalArgumentException,
        InstantiationException, InterruptedException, IOException;
    }
