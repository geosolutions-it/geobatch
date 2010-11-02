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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.Service;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Actions in scripting languages shall extend this class.
 * 
 * @author etj
 */
public class SplittingAction extends BaseAction<FileSystemMonitorEvent> {
    private static final Logger LOGGER = Logger.getLogger(SplittingAction.class.getName());

    private SplittingConfiguration configuration;

    public SplittingAction(SplittingConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    protected SplittingConfiguration getConfiguration() {
        return configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws ActionException {
        try {
            listenerForwarder.started();

            // looking for file
            if (events.size() != 1) {
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            }
            FileSystemMonitorEvent event = events.remove();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (getConfiguration() == null) {
                LOGGER.log(Level.SEVERE, "Configuration is null.");
                throw new IllegalStateException("Configuration is null.");
            }

            final String configId = getConfiguration().getName();

            listenerForwarder.setTask("Processing event " + event);

            // FORWARDING EVENTS
            for (String flowId : configuration.getServiceIDs()) {
                Service flow = CatalogHolder.getCatalog().getResource(flowId,
                        Service.class);
                if (flow != null) {
                    //flow.postEvent(event);
                } else {
                    LOGGER.warning("Trying to forward event to flow " + flowId
                            + " but it was unavailable!");
                }
            }

            events.add(event);

            listenerForwarder.completed();
            return events;
        } catch (Exception t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // no need to log, we're
                                                                  // rethrowing it
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }
    }
}