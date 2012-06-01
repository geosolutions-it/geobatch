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

import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkDeleteConfiguration;

import it.geosolutions.geonetwork.GNClient;
import it.geosolutions.geonetwork.exception.GNException;
import it.geosolutions.geonetwork.util.GNSearchResponse;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GNDelete {
    private final static Logger LOGGER = LoggerFactory.getLogger(GNDelete.class);

    private GeonetworkDeleteConfiguration cfg;

    public GNDelete(GeonetworkDeleteConfiguration cfg) {
        this.cfg = cfg;
    }

    // Fixme
    public boolean run(GNClient gnClient, File inputFile) {

        boolean complete = false;
        int loopcnt = 0;

        while ( ! complete ) {

            GNSearchResponse searchResponse;
            try {
                searchResponse = gnClient.search(inputFile);
            } catch (GNException ex) {
                LOGGER.error("Error searching for metadata: " + inputFile, ex);
                return false;
            }

            complete = searchResponse.isCompleteResponse();
            final int total = searchResponse.getCount();
            final int count = searchResponse.getFrom() == 0 ? 0 : searchResponse.getTo() - searchResponse.getFrom() + 1;

            LOGGER.info("Found " + total + " metadata, " +count+" listed -- loop#" + loopcnt++);

            int cntdel = 0;

            for (GNSearchResponse.GNMetadata metadata : searchResponse) {
                Long metadataId = metadata.getId();
                LOGGER.info("Deleting metadata #" + metadataId);

                try {
                    gnClient.deleteMetadata(metadataId);
                    cntdel++;
                } catch (GNException ex) {
                    LOGGER.error("Error deleting metadata #" + metadataId, ex);
                    // go on with next entries
                }
            }

            if( !complete && total > 0 && cntdel == 0) {
                // this check is needed in order to bail out from the while loop
                // if we got N entries but none of them could be deleted,
                // otherwise we could get into and endless loop.
                LOGGER.warn("Bailing out from the delete loop (deleted:"+cntdel+" listed:"+count+ " total:"+total+")");
                return false;
            }
        }

        return true;
    }

}
