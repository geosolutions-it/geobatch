package it.geosolutions.geobatch.beam.msgwarp;

/**
 * This class provides an object containing all the informations associated to the MSG NetCDF file to use.
 * 
 * @author Nicola Lagomarsini GeoSolutions S.A.S.
 * 
 */
public class MSGConfiguration {
    /**
     * Distance between the satellite and the earth center. (Typical value 42164)
     */
    private double earthCentreDistance = 0;

    /**
     * Upper left corner longitude value
     */
    private double ulx = 0;

    /**
     * Upper left corner latitude value
     */
    private double uly = 0;

    /**
     * Lower right corner longitude value
     */
    private double lrx = 0;

    /**
     * Lower right corner latitude value
     */
    private double lry = 0;

    /**
     * Sub-Satellite Longitude (at the nadir). (Typical value 0)
     */
    private double subSatLon = 0;

    /**
     * Satellite resolution angle on the X direction. (Typical value 17.83)
     */
    private Double resol_angle_X = null;

    /**
     * Satellite resolution angle on the Y direction. (Typical value 17.83)
     */
    private Double resol_angle_Y = null;

    public MSGConfiguration() {
    }

    public double getEarthCentreDistance() {
        return earthCentreDistance;
    }

    public void setEarthCentreDistance(double earthCentreDistance) {
        this.earthCentreDistance = earthCentreDistance;
    }

    public double getUlx() {
        return ulx;
    }

    public void setUlx(double ulx) {
        this.ulx = ulx;
    }

    public double getUly() {
        return uly;
    }

    public void setUly(double uly) {
        this.uly = uly;
    }

    public double getLrx() {
        return lrx;
    }

    public void setLrx(double lrx) {
        this.lrx = lrx;
    }

    public double getLry() {
        return lry;
    }

    public void setLry(double lry) {
        this.lry = lry;
    }

    public double getSubSatLon() {
        return subSatLon;
    }

    public void setSubSatLon(double subSatLon) {
        this.subSatLon = subSatLon;
    }

    public Double getResol_angle_X() {
        return resol_angle_X;
    }

    public void setResol_angle_X(Double resol_angle_X) {
        this.resol_angle_X = resol_angle_X;
    }

    public Double getResol_angle_Y() {
        return resol_angle_Y;
    }

    public void setResol_angle_Y(Double resol_angle_Y) {
        this.resol_angle_Y = resol_angle_Y;
    }

}
