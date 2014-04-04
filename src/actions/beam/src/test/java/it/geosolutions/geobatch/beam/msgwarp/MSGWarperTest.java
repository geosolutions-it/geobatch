package it.geosolutions.geobatch.beam.msgwarp;

import java.awt.geom.Point2D;

import org.apache.log4j.Logger;
import org.junit.Test;

public class MSGWarperTest {

    public final static Logger LOGGER = Logger.getLogger(MSGWarperTest.class);

    /**
     * This test is used for describing the mapping between destination image points and source image points
     */
    @Test
    public void testWarper() {
        // X,Y final image points
        Point2D upperLeft = new Point2D.Double(500, 500);
        Point2D upperRight = new Point2D.Double(3211, 500);
        Point2D lowerLeft = new Point2D.Double(500, 3211);
        Point2D lowerRight = new Point2D.Double(3211, 3211);

        MSGWarp warper = new MSGWarp(42164.0, -82, 82, 82, -82, 3712, 3712, 0);

        Point2D.Double coord = new Point2D.Double(0, 0);

        // FINAL IMAGE UPPER-LEFT POINT
        warper.gridToWorld((int) upperLeft.getX(), (int) upperLeft.getY(), coord);

        double x = coord.getX();
        double y = coord.getY();

        LOGGER.info("Mapping for the pixel (" + upperLeft.getX() + ", " + upperLeft.getY()
                + ") is " + " (" + y + ", " + x + ")");

        warper.coordToPixel(coord);

        LOGGER.info("Mapping for the Coordinates (" + y + ", " + x + ") is " + " (" + coord.getX()
                + ", " + coord.getY() + ")\n");

        // FINAL IMAGE UPPER-RIGHT POINT
        warper.gridToWorld((int) upperRight.getX(), (int) upperRight.getY(), coord);

        x = coord.getX();
        y = coord.getY();

        LOGGER.info("Mapping for the pixel (" + upperRight.getX() + ", " + upperRight.getY()
                + ") is " + " (" + y + ", " + x + ")");

        warper.coordToPixel(coord);

        LOGGER.info("Mapping for the Coordinates (" + y + ", " + x + ") is " + " (" + coord.getX()
                + ", " + coord.getY() + ")\n");

        // FINAL IMAGE LOWER-LEFT POINT
        warper.gridToWorld((int) lowerLeft.getX(), (int) lowerLeft.getY(), coord);

        x = coord.getX();
        y = coord.getY();

        LOGGER.info("Mapping for the pixel (" + lowerLeft.getX() + ", " + lowerLeft.getY()
                + ") is " + " (" + y + ", " + x + ")");

        warper.coordToPixel(coord);

        LOGGER.info("Mapping for the Coordinates (" + y + ", " + x + ") is " + " (" + coord.getX()
                + ", " + coord.getY() + ")\n");

        // FINAL IMAGE LOWER-RIGHT POINT
        warper.gridToWorld((int) lowerRight.getX(), (int) lowerRight.getY(), coord);

        x = coord.getX();
        y = coord.getY();

        LOGGER.info("Mapping for the pixel (" + lowerRight.getX() + ", " + lowerRight.getY()
                + ") is " + " (" + y + ", " + x + ")");

        warper.coordToPixel(coord);

        LOGGER.info("Mapping for the Coordinates (" + y + ", " + x + ") is " + " (" + coord.getX()
                + ", " + coord.getY() + ")\n");
    }
}
