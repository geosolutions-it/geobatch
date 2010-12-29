package it.geosolutions.geobatch.tools.file;

import it.geosolutions.geobatch.tools.check.Objects;

import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;

public class Url {
    private final static Logger LOGGER = Logger.getLogger(Url.class.toString());
    
    /**
     * Tries to convert a {@link URL} into a {@link File}. Return null if something bad happens
     * 
     * @param fileURL
     *            {@link URL} to be converted into a {@link File}.
     * @return {@link File} for this {@link URL} or null.
     */
    public static File URLToFile(URL fileURL) {
        Objects.notNull(fileURL);
        try {

            final File retFile = DataUtilities.urlToFile(fileURL);
            return retFile;

        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, t.getLocalizedMessage(), t);
        }
        return null;
    }
}
