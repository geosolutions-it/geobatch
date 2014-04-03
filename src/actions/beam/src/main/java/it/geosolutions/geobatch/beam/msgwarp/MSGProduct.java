package it.geosolutions.geobatch.beam.msgwarp;

import org.esa.beam.framework.datamodel.Product;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.referencing.operation.MathTransform;

/**
 * Implementation of the {@link Product} class, specific for the MSGWarp. This class simply returns
 * the MathTransform object associated to the Warp operation.
 * 
 * @author Nicola Lagomarsini GeoSolutions S.A.S.
 *
 */
public class MSGProduct extends Product {

    /** Transformation object to pass to the Writer*/
    private MathTransform transform;

    public MSGProduct(String name, String type, int sceneRasterWidth, int sceneRasterHeight) {
        super(name, type, sceneRasterWidth, sceneRasterHeight);
    }

    /**
     * Method for creating the MathTransform to use
     * 
     * @param ulx
     * @param uly
     * @param lrx
     * @param lry
     * @param outXsize
     * @param outYsize
     */
    public void initMathTransform(double ulx, double uly, double lrx, double lry, double outXsize,
            double outYsize) {
        double psX = (lrx - ulx) / outXsize;
        double psY = (uly - lry) / outYsize;

        transform = new AffineTransform2D(psX, 0, 0, -psY, ulx, uly);
    }

    /**
     * @return the MathTransform to use
     */
    public MathTransform getTransform() {
        return transform;
    }

}
