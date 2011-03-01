package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.geotools.data.DataUtilities;

public abstract class ImageMosaicProperties {
    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ImageMosaicProperties.class.toString());

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
     * If the regex file do not exists, build it using the passed configuration and
     * return the corresponding properties object
     * 
     * @param regexFile
     * @param configuration
     * @return
     * @throws UnsatisfiedLinkError
     */
    private static Properties build(File regexFile,
            String regex) throws UnsatisfiedLinkError {

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
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE,
                                "Error occurred while writing "+regexFile.getAbsolutePath()+" file!", e);
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }

                    outFile = null;
                    out = null;
                }
            }
            else
                throw new NullPointerException("Unable to build the property file using a null regex string");
            
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
    protected static Properties buildIndexer(File indexer,
            ImageMosaicConfiguration configuration) {
        // ////
        // INDEXER
        // ////
        if (!indexer.exists()) {
            FileWriter outFile = null;
            PrintWriter out = null;
            try {
                outFile = new FileWriter(indexer);
                out = new PrintWriter(outFile);
                
                File baseDir=indexer.getParentFile();
                // Write text to file
                if (configuration.getTimeRegex() != null){
                    out.println("TimeAttribute=ingestion");
                    
                    final File timeregex = new File(baseDir, "timeregex.properties");
                    ImageMosaicProperties.build(timeregex, configuration.getTimeRegex());
                }

                if (configuration.getElevationRegex() != null){
                    out.println("ElevationAttribute=elevation");

                    final File elevationRegex = new File(baseDir, "elevationregex.properties");
                    ImageMosaicProperties.build(elevationRegex, configuration.getElevationRegex());
                }

                if (configuration.getRuntimeRegex() != null){
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
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE,
                            "Error occurred while writing indexer.properties file!", e);
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }

                outFile = null;
                out = null;
            }
            return getProperty(indexer);
        }

        return null;
    }

}
