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
package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.tools.commons.file.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ImageMosaicProperties {

    private static final String CACHING_KEY = "Caching";

    /**
     * Default logger
     */
    protected final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicProperties.class);

    /**
     * get the properties object from the file.
     * 
     * @param properties
     *            the file referring to the prop file to load
     * @return
     * @throws NullPointerException TODO
     * @throws IOException 
     */
    protected static Properties getProperty(File properties) throws NullPointerException, IOException {
        URL url = DataUtilities.fileToURL(properties);
        Properties props = null;
        if (url != null) {
            props = Utils.loadPropertiesFromURL(url);
        } else {
            throw new NullPointerException("Unable to resolve the URL: "
                    + properties.getAbsolutePath());
        }

        return props;
    }

    /**
     * If the regex file do not exists, build it using the passed configuration and return the
     * corresponding properties object
     * 
     * @param regexFile
     * @param configuration
     * @return
     * @throws NullPointerException
     * @throws IOException 
     */
    private static Properties build(File regexFile, String regex) throws NullPointerException, IOException {

        if (!regexFile.exists()) {
            FileWriter outFile = null;
            PrintWriter out = null;
            if (regex != null) {
                try {
                    outFile = new FileWriter(regexFile);
                    out = new PrintWriter(outFile);

                    // Write text to file
                    out.println("regex=" + regex);
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("Error occurred while writing " + regexFile.getAbsolutePath()
                                + " file!", e);
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }

                    outFile = null;
                    out = null;
                }
            } else
                throw new NullPointerException(
                        "Unable to build the property file using a null regex string");

            return getProperty(regexFile);
        }
        return null;
    }

    /**
     * If the indexer file do not exists, build the indexer using the passed configuration and
     * return the corresponding properties object
     * 
     * @note: here we suppose that elevation are stored as double
     * @note: for a list of available SPI refer to:<br>
     *        geotools/trunk/modules/plugin/imagemosaic/src/main/resources/META-INF/services/org.
     *        geotools.gce.imagemosaic.properties.PropertiesCollectorSPI
     * 
     * @param indexer
     * @param configuration
     * @return
     * @throws NullPointerException 
     * @throws IOException 
     */
    protected static Properties buildIndexer(File indexer, ImageMosaicConfiguration configuration) throws NullPointerException, IOException {
        // ////
        // INDEXER
        // ////
        if (!indexer.exists()) {

            FileWriter outFile = null;
            PrintWriter out = null;
            try {
                indexer.createNewFile();

                if (!indexer.canWrite()) {
                    final String message = "Unable to write on indexer.properties file at URL: "
                            + indexer.getAbsolutePath();
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(message);
                    throw new IOException(message);
                }

                outFile = new FileWriter(indexer);
                out = new PrintWriter(outFile);

                File baseDir = indexer.getParentFile();

                // Write text to file
                // setting caching of file to false
                out.println("Caching=false");

                if (configuration.getTimeRegex() != null) {
                    out.println("TimeAttribute=ingestion");

                    final File timeregex = new File(baseDir, "timeregex.properties");
                    ImageMosaicProperties.build(timeregex, configuration.getTimeRegex());
                }

                if (configuration.getElevationRegex() != null) {
                    out.println("ElevationAttribute=elevation");

                    final File elevationRegex = new File(baseDir, "elevationregex.properties");
                    ImageMosaicProperties.build(elevationRegex, configuration.getElevationRegex());
                }

                if (configuration.getRuntimeRegex() != null) {
                    out.println("RuntimeAttribute=runtime");

                    final File runtimeRegex = new File(baseDir, "runtimeregex.properties");
                    ImageMosaicProperties.build(runtimeRegex, configuration.getRuntimeRegex());
                }

                out.println("Schema=*the_geom:Polygon,location:String"
                        + (configuration.getTimeRegex() != null ? ",ingestion:java.util.Date" : "")
                        + (configuration.getElevationRegex() != null ? ",elevation:Double" : "")
                        + (configuration.getRuntimeRegex() != null ? ",runtime:Integer" : ""));
                out.println("PropertyCollectors="
                        + (configuration.getTimeRegex() != null ? "TimestampFileNameExtractorSPI[timeregex](ingestion)"
                                : "")
                        + (configuration.getElevationRegex() != null ? (configuration
                                .getTimeRegex() != null ? "," : "")
                                + "DoubleFileNameExtractorSPI[elevationregex](elevation)" : "")
                        + (configuration.getRuntimeRegex() != null ? (configuration.getTimeRegex() != null
                                || configuration.getElevationRegex() != null ? "," : "")
                                + "TimestampFileNameExtractorSPI[runtimeregex](runtime)"
                                : ""));
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(
                            "ImageMosaicProperties.buildIndexer(): Error occurred while writing indexer.properties file at URL: "
                                    + indexer.getAbsolutePath(), e);
                return null;
            } finally {
                if (out != null) {
                    out.flush();
                    IOUtils.closeQuietly(out);
                }
                out = null;
                if (outFile != null) {
                    IOUtils.closeQuietly(outFile);
                }
                outFile = null;
            }
            return getProperty(indexer);
        } else {
            // file -> indexer.properties
            /**
             * get the Caching property and set it to false
             */
            Properties indexerProps = getProperty(indexer);
            String caching = indexerProps.getProperty(CACHING_KEY);
            if (caching != null) {
                if (caching.equals("true")) {
                    indexerProps.setProperty(CACHING_KEY, "false");
                }
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("ImageMosaicProperty:buildIndexer():Unable to get the "
                            + CACHING_KEY + " property into the " + indexer.getAbsolutePath()
                            + " file.");
            }

            return indexerProps;
        }
    }

    /**
     * CHECKING FOR datastore.properties If the 'datastore.properties' do not exists into the
     * baseDir, try to use the configured one. If not found a shape file will be used (done by the
     * geoserver).
     * 
     * @param baseDir
     *            the directory of the layer
     * @return File (unchecked) datastore.properties if succes or null if some error occurred.
     */
    protected static File checkDataStore(ImageMosaicConfiguration configuration, File baseDir) {
        final File datastore = new File(baseDir, "datastore.properties");
        if (!datastore.exists()) {
            if (configuration.getDatastorePropertiesPath() != null) {
                File dsFile = Path.findLocation(configuration.getDatastorePropertiesPath(),
                        new File(configuration.getWorkingDirectory()));
                
                if (dsFile == null) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("ImageMosaicProperties.checkDataStore(): unable to get the absolute "
                                + "path of the 'datastore.properties'");
                    }
                    return null;
                }
                if (dsFile != null) {
                    if (!dsFile.isDirectory()) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("ImageMosaicProperties.checkDataStore() Configuration DataStore file found: '"
                                    + dsFile.getAbsolutePath() + "'.");
                        }
                        try {
                            FileUtils.copyFileToDirectory(dsFile, baseDir);
                        } catch (IOException e) {
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn("ImageMosaicProperties.checkDataStore() " + e.getMessage());
                            return null;
                        }
                    } else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("ImageMosaicProperties.checkDataStore() DataStoreProperties file points to a directory! "
                                    + dsFile.getAbsolutePath() + "'. Skipping event");
                        }
                        return null;
                    }
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("ImageMosaicProperties DataStoreProperties file not configured "
                            + "nor found into destination dir. A shape file will be used.");
                }
            }
        }
        return datastore;
    }

}
