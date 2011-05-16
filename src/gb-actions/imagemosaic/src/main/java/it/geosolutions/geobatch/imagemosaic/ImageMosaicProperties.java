package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.geotools.data.DataUtilities;

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
     * @throws UnsatisfiedLinkError
     */
    protected static Properties getProperty(File properties) throws UnsatisfiedLinkError {
        URL url = DataUtilities.fileToURL(properties);
        Properties props = null;
        if (url != null) {
            props = Utils.loadPropertiesFromURL(url);
        } else {
            throw new UnsatisfiedLinkError("unable to resolve the URL: "
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
     * @throws UnsatisfiedLinkError
     */
    private static Properties build(File regexFile, String regex) throws UnsatisfiedLinkError {

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
     * @param indexer
     * @param configuration
     * @return
     */
    protected static Properties buildIndexer(File indexer, ImageMosaicConfiguration configuration) {
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
                                + "ElevationFileNameExtractorSPI[elevationregex](elevation)" : "")
                        + (configuration.getRuntimeRegex() != null ? (configuration.getTimeRegex() != null
                                || configuration.getElevationRegex() != null ? "," : "")
                                + "RuntimeFileNameExtractorSPI[runtimeregex](runtime)"
                                : ""));
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Error occurred while writing indexer.properties file at URL: "
                            + indexer.getAbsolutePath(), e);
                return null;
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }

                outFile = null;
                out = null;
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
    static File checkDataStore(ImageMosaicConfiguration configuration, File baseDir) {
        final File datastore = new File(baseDir, "datastore.properties");
        if (!datastore.exists()) {
            if (configuration.getDatastorePropertiesPath() != null) {
                File dsFile = Path.findLocation(configuration.getDatastorePropertiesPath(),
                        ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory());
                if (dsFile == null) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("ImageMosaicAction:checkDataStore(): unable to get the absolute "
                                + "path of the 'datastore.properties'");
                    }
                    return null;
                }
                if (dsFile != null) {
                    if (!dsFile.isDirectory()) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("ImageMosaicAction:checkDataStore() Configuration DataStore file found: '"
                                    + dsFile.getAbsolutePath() + "'.");
                        }
                        try {
                            FileUtils.copyFileToDirectory(dsFile, baseDir);
                        } catch (IOException e) {
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn("ImageMosaicAction:checkDataStore() " + e.getMessage());
                            return null;
                        }
                    } else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("ImageMosaicAction:checkDataStore() DataStoreProperties file points to a directory! "
                                    + dsFile.getAbsolutePath() + "'. Skipping event");
                        }
                        return null;
                    }
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("ImageMosaicAction: DataStoreProperties file not configured "
                            + "nor found into destination dir. A shape file will be used.");
                }
            }
        }
        return datastore;
    }

}
