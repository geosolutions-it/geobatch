/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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

package it.geosolutions.geobatch.actions.geonetwork.op;

import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkInsertConfiguration;
import it.geosolutions.geonetwork.GNClient;
import it.geosolutions.geonetwork.exception.GNException;
import it.geosolutions.geonetwork.util.GNInsertConfiguration;
import it.geosolutions.geonetwork.util.GNPrivConfiguration;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GNInsert {
    private final static Logger LOGGER = LoggerFactory.getLogger(GNInsert.class);

    private GeonetworkInsertConfiguration cfg;

    public GNInsert(GeonetworkInsertConfiguration gic) {
        this.cfg = gic;
    }

    public boolean run(GNClient gnClient, File inputFile) {

        long metadataId;

        try{
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
        } catch (GNException ex) {
            LOGGER.error("Metadata not created: " + inputFile, ex);
            return false;
        }

        // set the metadata privileges if needed
        List<GeonetworkInsertConfiguration.Privileges> privs = cfg.getPrivileges();
        if (privs != null && !privs.isEmpty()) {
            GNPrivConfiguration pcfg = new GNPrivConfiguration();
            for (GeonetworkInsertConfiguration.Privileges priv : privs) {
                pcfg.addPrivileges(priv.getGroup(), priv.getOps());
            }

            LOGGER.debug("Setting privileges");
            try {
                gnClient.setPrivileges(metadataId, pcfg);
                LOGGER.info("Set privileges for " + privs.size() + " groups");
            } catch (GNException ex) {
                LOGGER.error("Privileges not set for metadata id:" + metadataId +" file:" + inputFile, ex);
            }
        }
        return true;
    }

}
