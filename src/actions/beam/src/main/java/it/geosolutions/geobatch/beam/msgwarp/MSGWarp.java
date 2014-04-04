package it.geosolutions.geobatch.beam.msgwarp;

import java.awt.geom.Point2D;
import javax.media.jai.Warp;

/**
 * Extension of the {@link Warp} class for mapping Meteosat-MSG images.
 * 
 * NOTE: The user must define the output image bounding box in raster coordinates.
 *
 * This code has been taken from the work of C. Harris((c) EUMETSAT 1994)
 * 
 * @author Nicola Lagomarsini GeoSolutions S.A.S.
 * 
 */
public class MSGWarp extends Warp {

    public final static double POLAR_RADIUS = 6356.755;

    private static final double SQUARE_POL_RADIUS = Math.pow(POLAR_RADIUS, 2);

    public final static double EQUATOR_RADIUS = 6378.137;

    private static final double RADIUS_RATIO = EQUATOR_RADIUS / POLAR_RADIUS;

    private static final double SQUARE_RADIUS_RATIO = Math.pow(RADIUS_RATIO, 2);

    private static final double RADIUS_PRODUCT = EQUATOR_RADIUS * POLAR_RADIUS;

    private static final double SQUARE_EQ_RADIUS = Math.pow(EQUATOR_RADIUS, 2);

    private static final double OBLATE_MULTIPLIER = Math.pow((1.0 - (1.0 / 298.257)), 2);

    public final static double RAD_TO_DEG = 180.0 / Math.PI;

    public final static double DEG_TO_RAD = Math.PI / 180.0;

    private final double earthCentreDistance;

    /** Upper left pixel longitude value */
    private final double ulx;

    /** Upper left pixel latitude value */
    private final double uly;

    /** Pixel resolution on the X direction */
    private final double psX;

    /** Pixel resolution on the Y direction */
    private final double psY;

    /** Longitude of the subsatellite */
    private final double subSatLon;

    /** Satellite angular step on the X direction */
    private final double resol_angle_X;

    /** Satellite angular step on the Y direction */
    private final double resol_angle_Y;

    /** Source image width */
    private final long srcWidth;

    /** Source image height */
    private final long srcHeight;

    /** Double value for undefined coordinates */
    private final double undefined;

    /** FOV on the X direction */
    private final double fovX;

    /** FOV on the Y direction */
    private final double fovY;

    /** Parameter used for mapping source values to destination values */
    private final double c;

    /** Half of the source image Width */
    private final long halfWidth;

    /** Half of the source image Height */
    private final long halfHeight;

    public MSGWarp(double earthCentreDistance, double ulx, double uly, double lrx, double lry,
            double outXsize, double outYsize, double subSatLon) {
        this(earthCentreDistance, ulx, uly, lrx, lry, outXsize, outYsize, subSatLon, null, null,
                null, null);
    }

    public MSGWarp(double earthCentreDistance, double ulx, double uly, double lrx, double lry,
            double outXsize, double outYsize, double subSatLon, Double resol_angle_X,
            Double resol_angle_Y, Long srcWidth, Long srcHeight) {
        // Setting of the input Satellite parameters
        this.earthCentreDistance = earthCentreDistance;
        this.ulx = ulx;
        this.uly = uly;
        this.subSatLon = subSatLon;

        this.psX = (lrx - ulx) / outXsize;
        this.psY = (uly - lry) / outYsize;

        // Optional parameters to set (Different satellite parameters)
        if (resol_angle_X == null) {
            this.resol_angle_X = 17.83;
        } else {
            this.resol_angle_X = resol_angle_X;
        }

        if (resol_angle_Y == null) {
            this.resol_angle_Y = 17.83;
        } else {
            this.resol_angle_Y = resol_angle_Y;
        }

        if (srcWidth == null) {
            this.srcWidth = 3712;
        } else {
            this.srcWidth = srcWidth;
        }

        if (srcHeight == null) {
            this.srcHeight = 3712;
        } else {
            this.srcHeight = srcHeight;
        }

        undefined = Math.max(this.srcWidth + 1, this.srcHeight + 1);
        fovX = this.resol_angle_X / (this.srcWidth * 1.0);
        fovY = this.resol_angle_Y / (this.srcHeight * 1.0);
        c = earthCentreDistance * earthCentreDistance - SQUARE_EQ_RADIUS;
        halfWidth = this.srcWidth / 2;
        halfHeight = this.srcHeight / 2;
    }

    /**
     * This method map a destination image rectangle into the source coordinates
     */
    @Override
    public float[] warpSparseRect(int x, int y, int width, int height, int periodX, int periodY,
            float[] destRect) {

        // If the destRect array is not defined, it must be created
        if (destRect == null) {
            destRect = new float[((width + periodX - 1) / periodX)
                    * ((height + periodY - 1) / periodY) * 2];
        }

        // Compute final bounds
        width += x;
        height += y;
        // destRect index
        int index = 0;

        // Coordinate storing the values
        Point2D.Double coord = new Point2D.Double(0d, 0d);

        // Cycle on all the y values
        for (int j = y; j < height; j += periodY) {

            // Cycle on all the x values
            for (int i = x; i < width; i += periodX) {
                // Transformation from (x,y) to Lat/Lon
                gridToWorld(i, j, coord);
                // Transformation from Lat/Lon to source pixel coordinates
                coordToPixel(coord);
                // Setting of the output values
                destRect[index++] = (float) coord.getX();
                destRect[index++] = (float) coord.getY();
            }
        }

        return destRect;
    }

    /**
     * Transforms the input (x,y) couple into Lat/Lon and stores it inside the {@link Point2D.Double} instance
     * 
     * @param x
     * @param y
     * @param point
     */
    Point2D.Double gridToWorld(int x, int y, Point2D.Double coord) {
        coord.setLocation(ulx + (x * psX) - subSatLon, uly - (y * psY));

        return coord;
    }

    /**
     * This method takes in input a Lat/Lon pair and computes the (x,y) indexes related to the source image. These values are stored inside a
     * {@link Point2D} object provided in input.
     * 
     * @param coord
     * @param point
     * @return
     */
    Point2D.Double coordToPixel(Point2D.Double point) {
        // Selection of Latitude and Longitude from the input data
        double in_lat = point.getY();
        double in_lon = point.getX();

        // Convert inputs to radians
        double geolat = in_lat * DEG_TO_RAD;
        double lon = in_lon * DEG_TO_RAD;

        // Convert the geodetic latitude (as input) to geocentric latitudes
        // for use within the algorithm.
        double lat = Math.atan(OBLATE_MULTIPLIER * Math.tan(geolat));

        // Math cariable initialization
        double sinLon = Math.sin(lon);
        double cosLat = Math.cos(lat);
        double sinLat = Math.sin(lat);
        // Calculate rtheta. This is the distance from the Earth centre to
        // a point on the surface at latitude 'lat'.
        double rtheta = RADIUS_PRODUCT
                / Math.sqrt(SQUARE_POL_RADIUS * cosLat * cosLat + SQUARE_EQ_RADIUS * sinLat
                        * sinLat);

        // Carry on with conversion ...
        // Calculate Cartesian coordinates of target point. This is
        // basic geometry. The coordinate system is geocentric with
        // the x-axis towards the spacecraft, the y-axis to the East
        // and the z-axis towards the N pole.
        double x = rtheta * cosLat * Math.cos(lon);
        double y = rtheta * cosLat * sinLon;
        double z = rtheta * sinLat;

        // Check for invisibility. This is done using the basic geometric
        // theorem that the dot product of two vectors A and B is equal
        // to |A||B| cos (theta)
        // where theta is the angle between them. In this case, the test
        // is simple. The horizon is defined as the locus of points where
        // the local normal is perpendicular to the spacecraft sightline
        // vector. All visible points have (theta) less than 90 degrees
        // and all invisible points have (theta) greater than 90 degrees.
        // The test therefore reduces to whether the sign of the dot
        // product is +ve or -ve; if it is -ve the point is invisible.

        // The vector from the point to the spacecraft has components
        // Rs-x, -y, -z where Rs is the distance from the origin to the
        // satellite. The vector for the normal has components
        // x y z(Re/Rp)^2

        double distance = earthCentreDistance - x;
        double dotprod = distance * x - y * y - z * z * SQUARE_RADIUS_RATIO;

        if (dotprod <= 0.0) {
            // Setting of the bounds outside of the source image dimensions
            point.setLocation(undefined, undefined);
            return point;
        }

        // In this coordinate system the spacecraft (S) is at position
        // (altitude,0,0), the Earth centre (O) at (0,0,0) and the point
        // (P) at (x,y,z). Two additional points need to be defined,
        // so that the angles from the reference planes to the target point
        // (i.e. the position of the point in the sensor FOV) can be extracted.
        // These points are defined by dropping lines perpendicuarly from P
        // onto the equatorial plane and the Greenwich meridian plane. Their
        // coordinates are defined as

        // O' = (x, y, 0) and
        // O'' = (x, 0, z).

        // With these points, right-angled triangles can be defined SO'P
        // and SO''P which can be used directly to determine the angular
        // coordinates (aline, asamp) of P in the FOV.

        // New change from C. Harris from 10th July, 1997.
        // asamp = atan (y / sqrt (z*z + (altitude-x)*(altitude-x) ) );

        double asamp = Math.atan(y / distance);
        double aline = Math.atan(z / Math.sqrt(y * y + distance * distance));

        // Convert back to degrees
        asamp = asamp * RAD_TO_DEG;
        aline = aline * RAD_TO_DEG;

        // Calculate line, sample. Note that since samples are measured
        // from the right of the image, and the angular conversion was
        // measured in the x (east) direction, a sign correction has to be
        // included for samples.

        // Step is the radiometer step as seen by the spacecraft,
        // in degrees. The image represents an 18 x 18 degree field
        // of view divided up on an equi-angular basis.
        // resol_angle = 17.83;// MSG
        // MTP has 2500x2500 and MSG 3712x3712

        asamp = asamp / fovX;
        aline = aline / fovY;
        // Rounding
        if (asamp >= 0.0) {
            point.x = (double) srcWidth / 2 + (int) (asamp + 0.5);
        } else {
            point.x = (double) srcWidth / 2 + 1 + (int) (asamp + 0.5);
        }

        if (aline >= 0.0) {
            point.y = (double) srcHeight / 2 + 1 + (int) (aline + 0.5);
        } else {
            point.y = (double) srcHeight / 2 + (int) (aline + 0.5);
        }

        return point;
    }

    /**
     * This method takes in input a (x,y) pair and computes the Lat/Lon values related to the destination image. These values are stored inside a
     * {@link Point2D} object provided in input.
     * 
     * @param input
     * @param coord
     * @return
     */
    Point2D pixelToCoord(Point2D coord) {

        double samp = coord.getX();
        double line = coord.getY();

        // Convert line/sample values to angular offsets from centre point
        double asamp = (samp - halfWidth) * fovX;
        double aline = (line - halfHeight) * fovY;

        asamp = asamp * DEG_TO_RAD;
        aline = aline * DEG_TO_RAD;

        // Calculate tangents of angles
        double tanal = Math.tan(aline);
        double tanas = Math.tan(asamp);

        // Calculate components of an arbitrary vector from the spacecraft
        // in the viewing direction.
        double p = -1.0;

        // Fix from C. Harris 18th July, 1997.
        // r = tanal * sqrt( (1.0+tanas*tanas) / (1.0-tanal*tanal*tanas*tanas)
        // );
        // q = tanas * sqrt(1.0 + r*r);
        double q = tanas;
        double r = tanal * Math.sqrt(1.0 + q * q);

        // The location of the point on the Earth can be identified by
        // solving a quadratic equation for the intersection between
        // the Earth's surface and the viewing line from the spacecraft.
        // If this equation has no real roots then there is no intersection;
        // otherwise the required root is the one nearer to the spacecraft
        // (on the visible side of the Earth).

        double a2 = r * RADIUS_RATIO;
        double a = q * q + a2 * a2 + p * p;
        double b = 2.0 * earthCentreDistance * p;

        // Calculate determinant

        double det = b * b - 4 * a * c;

        if (det > 0.0) {
            double k = (-b - Math.sqrt(det)) / (2.0 * a);
            double x = earthCentreDistance + k * p;
            double y = k * q;
            double z = k * r;
            double lon = Math.atan(y / x);
            double cenlat = Math.atan(z * Math.cos(lon) / x);

            // This is the geocentric latitude; convert it to the geodetic
            // (or geographic) latitude before returning it to the user.
            double lat = Math.atan(Math.tan(cenlat) / OBLATE_MULTIPLIER);

            coord.setLocation(lon * RAD_TO_DEG, lat * RAD_TO_DEG);

        } else {
            throw new UnsupportedOperationException(
                    "Unable to map source points to destination points, try forcing the destination dimensions");
        }

        return coord;
    }

    /**
     * Transforms the input (Lat,Lon) couple into x/y and stores it inside the {@link LatLon} instance
     * 
     * @param x
     * @param y
     * @param point
     */
    Point2D worldToGrid(Point2D point) {
        double lat = point.getY();
        double lon = point.getX();

        point.setLocation((lon + subSatLon - ulx) / psX, (lat + uly) / psY);

        return point;
    }

    /**
     * Mapping from a source point to a destination point(If present).
     * This method throws an exception when no mapping is found.
     */
    @Override
    public Point2D mapSourcePoint(Point2D sourcePt) {
        // Conversion from source coordinates to Lat/Lon
        pixelToCoord(sourcePt);
        // Conversion from Lat/Lon to Destination point
        worldToGrid(sourcePt);
        return sourcePt;
    }
}
