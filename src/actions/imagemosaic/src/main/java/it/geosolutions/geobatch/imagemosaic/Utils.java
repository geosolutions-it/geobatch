/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.util.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author afabiani
 * 
 * 
 */
public class Utils {
    /**
     * Logger.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Checks that a {@link File} is a real file, exists and is readable.
     * 
     * @param file
     *            the {@link File} instance to check. Must not be null.
     * 
     * @return <code>true</code> in case the file is a real file, exists and is readable;
     *         <code>false </code> otherwise.
     */
    public static boolean checkFileReadable(final File file) {
        if (LOGGER.isTraceEnabled()){
            final StringBuilder builder = new StringBuilder();
            builder.append("Checking file:").append(
                    FilenameUtils.getFullPath(file.getAbsolutePath())).append("\n");
            builder.append("canRead: ").append(file.canRead()).append("\n");
            builder.append("isHidden: ").append(file.isHidden()).append("\n");
            builder.append("isFile: ").append(file.isFile()).append("\n");
            builder.append("canWrite: ").append(file.canWrite()).append("\n");
            LOGGER.trace(builder.toString());
        }
        if (!file.exists() || !file.canRead() || !file.isFile())
            return false;
        return true;
    }

    /**
     * 
     * @param propsURL
     * @return
     * @throws IOException 
     */
    public static Properties loadPropertiesFromURL(URL propsURL) throws IOException {
        final Properties properties = new Properties();
        InputStream stream = null;
        InputStream openStream = null;
        try {
            openStream = propsURL.openStream();
            stream = new BufferedInputStream(openStream);
            properties.load(stream);
        } catch (FileNotFoundException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            throw e;
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            throw e;
        } finally {

            if (stream != null)
                IOUtils.closeQuietly(stream);

            if (openStream != null)
                IOUtils.closeQuietly(openStream);

        }
        return properties;
    }

    /**
     * 
     * @param properties
     * @param spi
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Serializable> createDataStoreParamsFromPropertiesFile(
            Properties properties, DataStoreFactorySpi spi) throws IOException {
        // get the params
        final Map<String, Serializable> params = new HashMap<String, Serializable>();
        final Param[] paramsInfo = spi.getParametersInfo();
        for (Param p : paramsInfo) {
            // search for this param and set the value if found
            if (properties.containsKey(p.key))
                params.put(p.key, (Serializable) Converters.convert(properties.getProperty(p.key),
                        p.type));
            else if (p.required && p.sample == null)
                throw new IOException("Required parameter missing: " + p.toString());
        }

        return params;
    }
}
