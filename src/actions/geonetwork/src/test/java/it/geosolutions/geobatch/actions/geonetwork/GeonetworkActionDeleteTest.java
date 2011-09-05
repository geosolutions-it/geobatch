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

import it.geosolutions.geobatch.actions.geonetwork.configuration.GeonetworkDeleteConfiguration;
import it.geosolutions.geobatch.actions.geonetwork.op.GNDelete;
import it.geosolutions.geonetwork.GNClient;
import it.geosolutions.geonetwork.exception.GNException;
import it.geosolutions.geonetwork.exception.GNLibException;
import it.geosolutions.geonetwork.exception.GNServerException;
import it.geosolutions.geonetwork.util.GNInsertConfiguration;
import it.geosolutions.geonetwork.util.GNSearchRequest;
import it.geosolutions.geonetwork.util.GNSearchResponse;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkActionDeleteTest extends GeonetworkAbstractTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkActionDeleteTest.class);
    
    public GeonetworkActionDeleteTest() {
    }


    protected void insertSome() throws IOException, GNException {
        final String TITLETOKEN = "TOKEN_FOR_TITLE";

        GNClient client = createClientAndLogin();

        File origFile = loadFile("metadata_token.xml");
        String orig = FileUtils.readFileToString(origFile);

        File tempFile = File.createTempFile("gbtest", ".xml");
        FileUtils.forceDeleteOnExit(tempFile);

        for (int i = 0; i < 5; i++) {
            String title = "GeoBatch GeoNetworkAction test"+i+ " ACK00";
            String test = orig.replace(TITLETOKEN, title);
            FileUtils.writeStringToFile(tempFile, test);
            long id = insertMetadata(client, tempFile);
            LOGGER.info("Created test metadata id:"+id+" ["+title+"]");
        }
        
        for (int i = 0; i < 7; i++) {
            String title = "GeoBatch GeoNetworkAction test"+i+ " ACK99";
            String test = orig.replace(TITLETOKEN, title);
            FileUtils.writeStringToFile(tempFile, test);
            long id = insertMetadata(client, tempFile);
            LOGGER.info("Created test metadata id:"+id+" ["+title+"]");
        }
    }

    public long insertMetadata(GNClient client, File file) throws GNServerException, GNLibException {
        GNInsertConfiguration cfg = createDefaultInsertConfiguration();
        long id = client.insertMetadata(cfg, file);
        return id;
    }

    public void testRemoveMetadata() throws GNException, IOException {
        if(!runIntegrationTest)
            return;
        removeAllMetadata();

        GNClient client = createClientAndLogin();
        insertSome();

        // check that entries have been inserted properly
        {
            GNSearchRequest searchRequest = new GNSearchRequest(); // empty filter, get all entries
            GNSearchResponse searchResponse = client.search(searchRequest);
            assertEquals(12, searchResponse.getCount());
        }

        // create a request file
        Element request = new Element("request").addContent(
                new Element("any").setText("ACK99"));
        File requestFile = File.createTempFile("gbtest_request", ".xml");
        FileUtils.forceDeleteOnExit(requestFile);
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        FileUtils.writeStringToFile(requestFile, outputter.outputString(request));

        // check that the search works properly
        {
            GNSearchResponse searchResponse = client.search(requestFile);
            assertEquals(7, searchResponse.getCount());
        }

        GeonetworkDeleteConfiguration cfg = new GeonetworkDeleteConfiguration("test", "test", "test");
        cfg.setGeonetworkServiceURL(gnServiceUrl);
        cfg.setLoginUsername(gnUsername);
        cfg.setLoginPassword(gnPassword);


        // OK: run the operation now
        GNDelete gnd = new GNDelete(cfg);
        boolean ret = gnd.run(client, requestFile);
        if(ret)
            LOGGER.info("Clear procedure successful");
        else
            LOGGER.warn("Problems in clear procedure");

        // check that entries have been inserted properly
        {
            GNSearchRequest searchRequest = new GNSearchRequest(); // empty filter, get all entries
            GNSearchResponse searchResponse = client.search(searchRequest);
            assertEquals(12-7, searchResponse.getCount());
        }
        // check that the search works properly
        {
            GNSearchResponse searchResponse = client.search(requestFile);
            assertEquals(0, searchResponse.getCount());
        }

    }

}
