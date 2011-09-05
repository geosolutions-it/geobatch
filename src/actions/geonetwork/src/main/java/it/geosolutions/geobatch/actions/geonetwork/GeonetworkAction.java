/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.geonetwork;

import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkInsertConfiguration;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkConfiguration;
import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkDeleteConfiguration;
import it.geosolutions.geobatch.actions.geonetwork.op.GNDelete;
import it.geosolutions.geobatch.actions.geonetwork.op.GNInsert;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geonetwork.GNClient;
import it.geosolutions.geonetwork.exception.GNException;
import it.geosolutions.geonetwork.util.GNInsertConfiguration;
import it.geosolutions.geonetwork.util.GNPrivConfiguration;

import java.io.File;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Perform an operation in GeoNetwork, according to the input configuration.
 * <br/><br/>
 * At the moment only the metadata insertion is supported.<br/>
 * <h3>Insert metadata</h3>
 * The input file may be a pure metadata to be inserted into GN, or a full GN
 * insert metadata request. The full request requires some more meta-metadata.
 * <br/>If such further data are not provided in the input file, they may be specified
 * in the configuration, and the Action will compile the full request to
 * be sent to GN.
 *
 *
 * @author ETj (etj at geo-solutions.it)
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class GeonetworkAction
        extends BaseAction<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkAction.class);

    final GeonetworkConfiguration cfg;

    public GeonetworkAction(GeonetworkConfiguration configuration) {
        super(configuration);
        cfg = configuration;
    }

    /**
     * @param events the queue containing metadata to send to the configured GN server
     * @return events (if success) the empty queue else the remaining events.
     * @note: <br>
     *  1. Handle multiple metadata file events.
     *  2. This action do not set any output.
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException{
        final String user  = cfg.getLoginUsername();
        final String pass  = cfg.getLoginPassword();
        final String gnurl = cfg.getGeonetworkServiceURL();

        boolean loginNeeded = user != null || pass != null;
        if(! loginNeeded )
            if(LOGGER.isInfoEnabled())
                LOGGER.info("Login not needed");

        if (events==null){
            final String message="GeoNetworkAction.execute(): FATAL -> Action list is NULL.";
            if(LOGGER.isErrorEnabled())
                LOGGER.error(message);
            // fatal error!!!
            throw new ActionException(this, message);
        }

        GNClient gnClient = new GNClient(cfg.getGeonetworkServiceURL());
        boolean loggedin = false;

        //TODO return -> List<EventObject> ret=new LinkedList<EventObject>();
        while ( ! events.isEmpty() ){
            // get the input event
            File inputFile = sanitizeInput(events.poll());
            if(inputFile == null)
                continue;

            // perform login (not more than once)
            if( loginNeeded && ! loggedin) {
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("GeoNetworkAction.execute(): Logging in");
                loggedin = gnClient.login(user, pass);
                if( ! loggedin ) {
                    LOGGER.error("Login failed " + user + " @ " + gnurl);
                    // fatal error!!!
                    throw new ActionException(this, "Login failed"); // CHECKME: this error will leave next events in the queue
                }
            }

            // dispatch to the requested operation
            try {
                if(cfg instanceof GeonetworkInsertConfiguration ) {
                    GeonetworkInsertConfiguration gic = (GeonetworkInsertConfiguration)cfg;
                    GNInsert op = new GNInsert(gic);
                    op.run(gnClient, inputFile);
                } else if (cfg instanceof GeonetworkDeleteConfiguration) {
                    GeonetworkDeleteConfiguration gc = (GeonetworkDeleteConfiguration)cfg;
                    GNDelete op = new GNDelete(gc);
                    op.run(gnClient, inputFile);
                } else {
                    throw new UnsupportedOperationException("Configuration not supported yet: " + cfg.getClass().getName());
                }
            } catch (Exception ex) {
                LOGGER.error("Error processing file: " + inputFile, ex);
                continue;
            }

        }

        // should be an empty queue
        return events;
    }

    private File sanitizeInput(FileSystemEvent event) {
        if (event == null) {
            LOGGER.error("GeoNetworkAction.execute(): NUll event encountered: SKIPPING...");
            return null;
        }
        final File inputFile = event.getSource();
        if (inputFile == null) {
            LOGGER.error("GeoNetworkAction.execute(): Incoming file event refer to a null file object: SKIPPING...");
            return null;
        } else if (!inputFile.exists() || !inputFile.canRead()) {
            LOGGER.error("GeoNetworkAction.execute(): Incoming file event refer"
                    + " to a not readable or not existent file: SKIPPING...");
            return null;
        }
        return inputFile;

    }
}
