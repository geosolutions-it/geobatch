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

package it.geosolutions.geobatch.jetty;

import java.io.File;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.BoundedThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty starter, will run GeoBatch inside the Jetty web container.<br>
 * Useful for debugging, especially in IDE were you have direct dependencies between the sources of
 * the various modules (such as Eclipse).
 * 
 * @author wolf
 * 
 */
public class Start {
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) {
        Server jettyServer = null;

        try {
            jettyServer = new Server();

            // don't even think of serving more than XX requests in parallel...
            // we
            // have a limit in our processing and memory capacities
            BoundedThreadPool tp = new BoundedThreadPool();
            tp.setMaxThreads(50);

            SocketConnector conn = new SocketConnector();
            String portVariable = System.getProperty("jetty.port");
            int port = parsePort(portVariable);

            if (port <= 0) {
                port = 8080;
            }

            conn.setPort(port);
            conn.setThreadPool(tp);
            conn.setAcceptQueueSize(100);
            jettyServer.setConnectors(new Connector[] { conn });

            WebAppContext wah = new WebAppContext();
            wah.setContextPath("/geobatch");
            // wah.setWar("target/geobatch");
            wah.setWar("src/main/webapp");
            jettyServer.setHandler(wah);
            wah.setTempDirectory(new File("target/work"));

            jettyServer.start();

            // use this to test normal stop behavior, that is, to check stuff
            // that
            // need to be done on container shutdown (and yes, this will make
            // jetty stop just after you started it...)
            // jettyServer.stop();
        } catch (Throwable e) {
            log.error("Could not start the Jetty server: " + e.getMessage(), e);

            if (jettyServer != null) {
                try {
                    jettyServer.stop();
                } catch (Exception e1) {
                    log.error("Unable to stop the " + "Jetty server:" + e1.getMessage(), e1);
                }
            }
        }
    }

    private static int parsePort(String portVariable) {
        if (portVariable == null) {
            return -1;
        }

        try {
            return Integer.valueOf(portVariable).intValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
