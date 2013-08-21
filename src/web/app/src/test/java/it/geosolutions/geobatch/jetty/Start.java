/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2013 GeoSolutions S.A.S.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.BoundedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty starter, will run GeoBatch inside the Jetty web container.<br>
 * Useful for debugging, especially in IDE were you have direct dependencies between the sources of the various modules (such as Eclipse).
 * 
 * @author Andrea Aime - GeoSolutions SAS
 * @author Carlo Cancellieri - GeoSolutions SAS
 * @author Emanuele Tajariol - GeoSolutions SAS
 * 
 */
public class Start {
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) {

        Server server = null;
        SocketConnector conn = null;
        try {
            server = new Server();

            // TODO pass properties file
            File properties = null;
            if (args.length == 1) {
                String propertiesFileName = args[0];
                if (!propertiesFileName.isEmpty()) {
                    properties = new File(propertiesFileName);
                }
            } else {
                properties = new File("src/test/resources/jetty.properties");
            }
            Properties prop = loadProperties(properties);

            // load properties into system env
            setSystemProperties(prop);

            server.setHandler(configureContext(prop));

            conn = configureConnection(prop);
            server.setConnectors(new Connector[] { conn });

            server.start();

            // use this to test normal stop behavior, that is, to check stuff
            // that
            // need to be done on container shutdown (and yes, this will make
            // jetty stop just after you started it...)
            // jettyServer.stop();
        } catch (Throwable e) {
            log.error("Could not start the Jetty server: " + e.getMessage(), e);

            if (server != null) {
                try {
                    server.stop();
                } catch (Exception e1) {
                    log.error("Unable to stop the Jetty server:" + e1.getMessage(), e1);
                }
            }
            if (conn != null) {
                try {
                    conn.stop();
                } catch (Exception e1) {
                    log.error("Unable to stop the connection:" + e1.getMessage(), e1);
                }
            }
        }
    }

    private static Properties loadProperties(final File props) throws IllegalArgumentException,
            IOException {
        Properties prop = new Properties();
        if (props == null || !props.exists()) {
            throw new IllegalArgumentException("Bad file name argument: " + props);
        }
        FileInputStream is = null;
        try {
            is = new FileInputStream(props);
            prop.load(is);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return prop;
    }

    private static int parseInt(String portVariable) {
        try {
            return Integer.parseInt(portVariable);
        } catch (Exception e) {
            return -1;
        }
    }

    public final static String JETTY_PORT_PROP = "jetty.port";
    public final static String JETTY_PORT_DEFAULT = "8080";

    public final static String CONTEXT_PATH_PROP = "context.path";
    public final static String CONTEXT_PATH_DEFAULT = "/geobatch";

    public final static String WAR_PATH_PROP = "war.path";
    public final static String WAR_PATH_DEFAULT = "target/geobatch"; // working with WAR overlays
//    public final static String WAR_PATH_DEFAULT = "src/main/webapp";

    public final static String TEMP_DIR_PROP = "temp.dir";
    public final static String TEMP_DIR_DEFAULT = "target/work";

    private static SocketConnector configureConnection(final Properties prop) {
        // don't even think of serving more than XX requests in parallel...
        // we have a limit in our processing and memory capacities
        BoundedThreadPool tp = new BoundedThreadPool();
        tp.setMaxThreads(50);

        SocketConnector conn = new SocketConnector();

        conn.setPort(parseInt(prop.getProperty(JETTY_PORT_PROP, JETTY_PORT_DEFAULT)));
        conn.setThreadPool(tp);
        conn.setAcceptQueueSize(100);

        return conn;
    }

    private static WebAppContext configureContext(final Properties prop) {
        WebAppContext wah = new WebAppContext();

        wah.setContextPath(prop.getProperty(CONTEXT_PATH_PROP, CONTEXT_PATH_DEFAULT));

        final String warPath = prop.getProperty(WAR_PATH_PROP, WAR_PATH_DEFAULT);
        wah.setWar(warPath);

        wah.setTempDirectory(new File(prop.getProperty(TEMP_DIR_PROP, TEMP_DIR_DEFAULT)));

        // we need to skip the libs in WEB-INF bc they are already included as dependencies.
        try {
            String skipPath = warPath;
            if( ! skipPath.endsWith("/"))
                skipPath += "/";
            skipPath += "WEB-INF/lib";
            log.info("Libs with path containing " + skipPath + " will be skipped");

            final FilterClassLoader classLoader = new FilterClassLoader(wah);
            classLoader.setSkipPath(skipPath);
            wah.setClassLoader(classLoader);
        } catch (IOException ex) {
            log.error("Cannot set classloader", ex);
        }

        return wah;
    }

    private static void setSystemProperties(final Properties prop) {
        for (Object key : prop.keySet()) {
            System.out.println(" --- Setting prop '"+key+"' --> "+ prop.get(key));
            System.setProperty(key.toString(), prop.get(key).toString());
        }
        System.out.println("");
    }

    static class FilterClassLoader extends WebAppClassLoader {

        private String skipPath;

        public FilterClassLoader(WebAppContext context) throws IOException {
            super(context);
        }

        public void setSkipPath(String skipPath) {
            this.skipPath = skipPath;
        }

        @Override
        protected void addURL(URL url) {
            if(url.toString().contains(skipPath)) {
                log.debug("Skipping lib " + url);
            } else {
                super.addURL(url);
            }
        }

    }
}


