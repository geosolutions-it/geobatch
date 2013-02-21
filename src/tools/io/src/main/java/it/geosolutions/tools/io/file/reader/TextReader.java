/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.io.file.reader;

import it.geosolutions.tools.commons.check.Objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextReader {
    private final static Logger LOGGER = LoggerFactory.getLogger(TextReader.class.toString());

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
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(e.getLocalizedMessage(), e);
            return null;
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (Throwable e) {
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace(e.getLocalizedMessage(), e);
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
