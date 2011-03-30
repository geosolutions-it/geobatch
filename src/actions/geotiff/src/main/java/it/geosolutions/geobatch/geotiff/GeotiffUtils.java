/**
 * 
 */
package it.geosolutions.geobatch.geotiff;

import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class GeotiffUtils {
    
    private final static GeoTiffFormat SPI= new GeoTiffFormat();

    /**
     * 
     */
    private GeotiffUtils() {
        // TODO Auto-generated constructor stub
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
