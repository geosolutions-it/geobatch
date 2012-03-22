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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class JMXActionManager implements ActionManager {

    public JMXActionManager() {
    }

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
    @Override
    public ConsumerStatus getStatus(String uuid) {
        return JMXServiceManager.getStatus(uuid); // consumer UUID not found

    }

    @Override
    public String callAction(Map<String, String> config) throws Exception, InstantiationException,
        InterruptedException, IOException {
        final String serviceId = config.remove(SERVICE_ID_KEY);
        if (serviceId == null || serviceId.isEmpty())
            throw new IllegalArgumentException(
                                               "Unable to locate the key "
                                                   + SERVICE_ID_KEY
                                                   + " matching the serviceId action in the passed paramether table");

        final String input = config.remove(INPUT_KEY);
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Unable to locate the key " + INPUT_KEY
                                               + " matching input in the passed paramether table.");
        }
        FileSystemEvent event = new FileSystemEvent(new File(input), FileSystemEventType.FILE_ADDED);
        Queue<FileSystemEvent> events = new java.util.LinkedList<FileSystemEvent>();
        events.add(event);

        // TODO remove all 'NOT configuration' param

        return JMXServiceManager.callAction(serviceId, config, events);
    }

    @Override
    public void disposeAction(String uuid) throws Exception {
        JMXServiceManager.dispose(uuid);
    }
}
