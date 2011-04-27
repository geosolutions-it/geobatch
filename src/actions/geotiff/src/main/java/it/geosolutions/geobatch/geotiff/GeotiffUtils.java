/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.geotiff;

import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;

/**
 * @author simone giannecchini
 * 
 * note: this is introduced as Hack to get the GeoTiffFormat working in a multithread
 * environment.
 *
 */
public class GeotiffUtils {
    
    private final static GeoTiffFormat SPI= new GeoTiffFormat();

    /**
     * 
     */
    private GeotiffUtils() {
    }

    public static synchronized GeoTiffReader getReader(final Object o){
        return getReader(o, null);
    }
    
    
    public static synchronized GeoTiffReader getReader(final Object o, final Hints hints){
        if(o==null)
            throw new NullPointerException();
        return SPI.getReader(o,hints);
    }
}
