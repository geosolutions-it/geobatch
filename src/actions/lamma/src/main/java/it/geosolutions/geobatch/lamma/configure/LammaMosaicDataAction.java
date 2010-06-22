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
package it.geosolutions.geobatch.lamma.configure;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.lamma.base.LammaBaseAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions
 */
public class LammaMosaicDataAction extends LammaBaseAction {

    protected final static Logger LOGGER = Logger.getLogger(LammaMosaicDataAction.class.toString());
    protected final LammaMosaicDataConfiguration configuration;

    /**
     *
     * @param configuration
     */
    public LammaMosaicDataAction(LammaMosaicDataConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    /**
     * 
     * @param events
     * @return
     * @throws ActionException
     */
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws ActionException {
        try {
            listenerForwarder.started();

            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            Queue<FileSystemMonitorEvent> outEvents = new LinkedList<FileSystemMonitorEvent>();
            List<File> mosaicInputDirs = new ArrayList<File>();
            
			// Logging to ESB ...
            logMessage.setMessage("Preparing mosaic input dirs ...");
            logMessage.setMessageTime(new Date());
			logToESB(logMessage);

            while(events.size() > 0) {
                // get the first event
                final FileSystemMonitorEvent event = events.remove();
                final File inputFile = event.getSource();

                if (!mosaicInputDirs.contains(inputFile.getParentFile())) {
                	mosaicInputDirs.add(inputFile.getParentFile());
                	outEvents.add(new FileSystemMonitorEvent(inputFile.getParentFile(), FileSystemMonitorNotifications.FILE_ADDED));
                }
            }
            
            listenerForwarder.completed();
            
            return outEvents;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            }
			// Logging to ESB ...
            logMessage.setMessage("[ERROR] " + t.getLocalizedMessage());
            logMessage.setMessageTime(new Date());
			logToESB(logMessage);

			listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }

    }

	@Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
        builder.append(" [");
        if (configuration != null) {
            builder.append("configuration=").append(configuration);
        }
        builder.append("]");
        return builder.toString();
    }
}
