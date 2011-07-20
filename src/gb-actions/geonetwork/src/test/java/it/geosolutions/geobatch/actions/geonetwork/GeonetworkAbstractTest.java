/*
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
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

import it.geosolutions.geonetwork.GNClient;
import it.geosolutions.geonetwork.exception.GNLibException;
import it.geosolutions.geonetwork.exception.GNServerException;
import it.geosolutions.geonetwork.util.GNInsertConfiguration;
import it.geosolutions.geonetwork.util.GNSearchRequest;
import it.geosolutions.geonetwork.util.GNSearchResponse;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public abstract class GeonetworkAbstractTest extends TestCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkAbstractTest.class);

    protected boolean runIntegrationTest = false;
    protected String gnServiceUrl = "http://localhost:8080/geonetwork";
    protected String gnUsername   = "admin";
    protected String gnPassword   = "admin";


    public GeonetworkAbstractTest() {
    }

//    @Before 
    public void setUp() throws Exception {
        super.setUp();
        LOGGER.info("====================> " + getName());
    }

    protected GNClient createClientAndLogin() throws IllegalStateException {
        GNClient client = new GNClient(gnServiceUrl);
        if( ! client.login(gnUsername, gnPassword))
            throw new IllegalStateException("Could not login into GeoNetwork");
        return client;
    }

    protected GNInsertConfiguration createDefaultInsertConfiguration() {
        GNInsertConfiguration cfg = new GNInsertConfiguration();
        cfg.setGroup("1");
        cfg.setCategory("datasets");
        cfg.setStyleSheet("_none_");
        cfg.setValidate(false);
        return cfg;
    }

    /**
     * Utility method to remove all metadata in GN.
     * The GNClient will be used here, in order to have the most direct interaction to GN.
     */
    protected void removeAllMetadata() throws GNLibException, GNServerException {
        GNClient client = createClientAndLogin();

        GNSearchRequest searchRequest = new GNSearchRequest(); // empty fiter, all metadaat will be returned
        GNSearchResponse searchResponse = client.search(searchRequest);

        LOGGER.info("Found " + searchResponse.getCount() + " existing metadata");
        for (GNSearchResponse.GNMetadata metadata : searchResponse) {
            LOGGER.info("Removing md ID:" + metadata.getId() + " UUID:" + metadata.getUUID());
            Long id = metadata.getId();
            client.deleteMetadata(id);
        }

        // check that the catalog is really empty
        searchResponse = client.search(searchRequest);
        assertEquals(0, searchResponse.getCount());
        LOGGER.info("All metadata removed successfully");
    }

    protected File loadFile(String name) {
        try {
            URL url = this.getClass().getClassLoader().getResource(name);
            if(url == null)
                throw new IllegalArgumentException("Cant get file '"+name+"'");
            File file = new File(url.toURI());
            return file;
        } catch (URISyntaxException e) {
            LOGGER.error("Can't load file " + name + ": " + e.getMessage(), e);
            return null;
        }    
    }
    
}
