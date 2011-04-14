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
package it.geosolutions.geobatch.tools.file;

import it.geosolutions.geobatch.tools.check.Objects;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.geotools.data.DataUtilities;

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
