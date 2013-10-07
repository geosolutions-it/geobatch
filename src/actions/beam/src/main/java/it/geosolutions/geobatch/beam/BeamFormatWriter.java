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

import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;

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

    /** 
     * This parameter is used to specify the dimensions to be parsed  
     */
    public final static String PARAM_CUSTOMDIMENSION = "customDimensions";

    /**
     * This parameter is used to specify whether we want to create coordinate variables for 
     * each additional dimension if they are missing from the input dataset
     */
    public final static String PARAM_FORCECOORDINATE = "forceCoordinate";

    /**
     * This parameter is used to specify whether we want to create a largeFile (64 bits offset). 
     * We may consider making this parameter automatically-computed from the upper layer.
     */
    public final static String PARAM_LARGEFILE = "largeFile";

    
    public void storeProduct(final String outputFilePath, 
            final Product inputProduct, 
            final Product reprojectedProduct, 
            final Map<String, Object> params, 
            final ProgressListenerForwarder forwarder) throws IOException; 
}
