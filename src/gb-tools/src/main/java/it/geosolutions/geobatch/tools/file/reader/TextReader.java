package it.geosolutions.geobatch.tools.file.reader;

import it.geosolutions.geobatch.tools.check.Objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.stream.StreamSource;

public class TextReader {
    private final static Logger LOGGER = Logger.getLogger(TextReader.class.toString());

    /**
     * Get the contents of a {@link File} as a String using the specified character encoding.
     * 
     * @param file
     *            {@link File} to read from
     * @param encoding
     *            IANA encoding
     * @return a {@link String} containig the content of the {@link File} or
     *         <code>null<code> if an error happens.
     */
    public static String toString(final File file, final String encoding) {
        Objects.notNull(file);
        if (!file.isFile() || !file.canRead() || !file.exists())
            return null;
        InputStream stream = null;
        try {
            if (encoding == null)
                return org.apache.commons.io.IOUtils.toString(new FileInputStream(file));
            else
                return org.apache.commons.io.IOUtils.toString(new FileInputStream(file), encoding);
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            return null;
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (Throwable e) {
                    if (LOGGER.isLoggable(Level.FINEST))
                        LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                }
        }
    }

    /**
     * Get the contents of a {@link File} as a String using the default character encoding.
     * 
     * @param file
     *            {@link File} to read from
     * @return a {@link String} containig the content of the {@link File} or
     *         <code>null<code> if an error happens.
     */
    public static String toString(final File file) {
        return toString(file, null);
    }

    /**
     * Convert the input from the provided {@link Reader} into a {@link String}.
     * 
     * @param inputStream
     *            the {@link Reader} to copy from.
     * @return a {@link String} that contains the content of the provided {@link Reader}.
     * @throws IOException
     *             in case something bad happens.
     */
    public static String toString(final StreamSource src, final String ecoding) throws IOException {
        Objects.notNull(src);
        InputStream inputStream = src.getInputStream();
        if (inputStream != null) {
            return org.apache.commons.io.IOUtils.toString(inputStream, ecoding);
        } else {

            final Reader r = src.getReader();
            return org.apache.commons.io.IOUtils.toString(r);
        }
    }

    /**
     * Convert the input from the provided {@link Reader} into a {@link String}.
     * 
     * @param inputStream
     *            the {@link Reader} to copy from.
     * @return a {@link String} that contains the content of the provided {@link Reader}.
     * @throws IOException
     *             in case something bad happens.
     */
    public static String toString(StreamSource src) throws IOException {
        Objects.notNull(src);
        InputStream inputStream = src.getInputStream();
        if (inputStream != null) {
            return org.apache.commons.io.IOUtils.toString(inputStream);
        } else {

            final Reader r = src.getReader();
            return org.apache.commons.io.IOUtils.toString(r);
        }
    }

}
