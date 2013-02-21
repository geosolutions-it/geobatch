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
package it.geosolutions.tools.geotools.file;

import it.geosolutions.tools.commons.check.Objects;

import java.io.File;
import java.net.URL;

import org.geotools.data.DataUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Url {
    private final static Logger LOGGER = LoggerFactory.getLogger(Url.class.toString());
    
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
            if (LOGGER.isTraceEnabled())
                LOGGER.trace(t.getLocalizedMessage(), t);
        }
        return null;
    }
}
