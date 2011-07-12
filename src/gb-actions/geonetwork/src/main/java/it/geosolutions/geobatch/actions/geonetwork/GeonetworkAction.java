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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
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
 */
public class GeonetworkAction 
        extends BaseAction<FileSystemEvent> {
    
    
    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkAction.class);

    GeonetworkInsertConfiguration cfg;

    public GeonetworkAction(GeonetworkInsertConfiguration configuration) {
        super(configuration);
        cfg = configuration;
    }

    /**
     * 
     */
//    @Override
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {
        
        // get the input event
        FileSystemEvent event = events.poll();
        File inputFile = event.getSource();
                
        
        GNClient gnClient = new GNClient(cfg.getGeonetworkServiceURL()) ;        

        try {
            // perform a login into GeoNetwork
            LOGGER.debug("Logging in");
            boolean logged = gnClient.login(cfg.getLoginUsername(), cfg.getLoginPassword());
            if (!logged) {
                throw new ActionException(this, "Login failed");
            }

            long metadataId;

            if (cfg.isOnlyMetadataInput()) { // only metadata available: we have to build the full request packet
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Handling pure metadata file " + inputFile);
                }

                GNInsertConfiguration gncfg = new GNInsertConfiguration();
                gncfg.setCategory(cfg.getCategory());
                gncfg.setGroup(cfg.getGroup());
                gncfg.setStyleSheet(cfg.getStyleSheet());
                gncfg.setValidate(cfg.getValidate());

                LOGGER.debug("Creating metadata");
                metadataId = gnClient.insertMetadata(gncfg, inputFile);
                LOGGER.info("Created metadata " + metadataId);

            } else { // the full xml request is ready in the file to be sent to GN; just parse it
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Handling full request file " + inputFile);
                }

                LOGGER.debug("Creating metadata");
                metadataId = gnClient.insertRequest(inputFile);
                LOGGER.info("Created metadata " + metadataId);

            }

            // set the metadata privileges if needed
            List<GeonetworkInsertConfiguration.Privileges> privs = cfg.getPrivileges();
            if (privs != null && !privs.isEmpty()) {
                GNPrivConfiguration pcfg = new GNPrivConfiguration();
                for (GeonetworkInsertConfiguration.Privileges priv : privs) {
                    pcfg.addPrivileges(priv.getGroup(), priv.getOps());
                }

                LOGGER.debug("Setting privileges");
                gnClient.setPrivileges(metadataId, pcfg);
                LOGGER.info("Set privileges for " + privs.size() + " groups");
            }

            return events;
        } catch (GNException ex) {
            throw new ActionException(this, "Error performing a GeoNetwork call", ex);
        }
    }


}
