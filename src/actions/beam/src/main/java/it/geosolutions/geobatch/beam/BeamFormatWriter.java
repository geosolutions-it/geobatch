package it.geosolutions.geobatch.beam;

import java.io.IOException;

import org.esa.beam.framework.datamodel.Product;

/**
 * Simple interface to store a Product 
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public interface BeamFormatWriter {

    public void storeProduct(final String outputFilePath, Product inputProduct, Product reprojectedProduct, boolean geophysics) throws IOException; 
}
