/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.beam;

import java.io.IOException;
import java.util.Map;

import org.esa.beam.framework.datamodel.Product;

/**
 * Simple interface to store a Product 
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public interface BeamFormatWriter {

    public final static String PARAM_GEOPHYSIC = "geophysics";

    public final static String PARAM_CUSTOMDIMENSION = "customDimensions";

    public final static String PARAM_FORCECOORDINATE = "forceCoordinate";

    public void storeProduct(final String outputFilePath, Product inputProduct, Product reprojectedProduct, Map<String, Object> params) throws IOException; 
}
