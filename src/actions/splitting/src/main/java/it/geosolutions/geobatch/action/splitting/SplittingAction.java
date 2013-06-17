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
package it.geosolutions.geobatch.action.splitting;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actions in scripting languages shall extend this class.
 * 
 * @author etj
 */
@Action(configurationClass=SplittingConfiguration.class)
public class SplittingAction extends BaseAction<FileSystemEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplittingAction.class.getName());

    public SplittingAction(SplittingConfiguration configuration) throws IOException {
        super(configuration);
    }

    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
            throws ActionException {
        try {
            listenerForwarder.started();

            Queue<FileSystemEvent> forwardingEvents = new LinkedList<FileSystemEvent>();

            int numEvents = events.size();
            for (int i = 0; i < numEvents; i++) {
                FileSystemEvent event = events.remove();

                // //
                // data flow configuration and dataStore name must not be null.
                // //
                if (getConfiguration() == null) {
                    LOGGER.error("Conf is null.");
                    throw new IllegalStateException("Conf is null.");
                }

                // final String configId = getConfiguration().getName();

                listenerForwarder.setTask("Processing event " + event);

                // FORWARDING EVENTS
                for (String flowId : ((SplittingConfiguration) getConfiguration()).getServiceIDs()) {
                    FileBasedFlowManager flow = CatalogHolder.getCatalog().getResource(flowId,
                            FileBasedFlowManager.class);
                    if (flow != null) {
                        flow.postEvent(event);
                    } else {
                        LOGGER.warn("Trying to forward event to flow " + flowId
                                + " but it was unavailable!");
                    }
                }

                forwardingEvents.add(event);
            }

            listenerForwarder.completed();
            return forwardingEvents;
        } catch (Exception t) {
            LOGGER.error(t.getLocalizedMessage(), t); // no need to log, we're
            // rethrowing it
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }
    }
    
	@Override
	@CheckConfiguration
	public boolean checkConfiguration() {
		// TODO Auto-generated method stub
		return true;
	}
}