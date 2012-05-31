/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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

package it.geosolutions.geobatch.geotiff.overview;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import javax.media.jai.Interpolation;

import org.geotools.utils.CoverageToolsConstants;

public class GeotiffOverviewsEmbedderConfiguration extends ActionConfiguration {

    
    protected GeotiffOverviewsEmbedderConfiguration() {
        super("XSTREAM PROBLEM!", "XSTREAM PROBLEM!", "XSTREAM PROBLEM!");
    }


    public GeotiffOverviewsEmbedderConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    private long JAICapacity;

    private float compressionRatio = CoverageToolsConstants.DEFAULT_COMPRESSION_RATIO;
    //CoverageToolsConstants.DEFAULT_COMPRESSION_RATIO; // 20120427: this constant stopped working for a com.thoughtworks.xstream.converters.ConversionException: Uncompilable source code - Erroneous tree type: org.geotools.utils.CoverageToolsConstants : Uncompilable source code - Erroneous tree type: org.geotools.utils.CoverageToolsConstants

    private String compressionScheme = CoverageToolsConstants.DEFAULT_COMPRESSION_SCHEME;

    /** Downsampling step. */
    private int downsampleStep;

    private int numSteps;

    /** Scale algorithm. */
    private String scaleAlgorithm;

    /** Tile height. */
    private int tileH = -1;

    /** Tile width. */
    private int tileW = -1;

    private String wildcardString = "*.*";

    private boolean logNotification = true;

    /**
     *
     * Interpolation method used througout all the program.
     *
     * @TODO make the interpolation method customizable from the user perpsective.
     *
     */
    private int interp = Interpolation.INTERP_NEAREST;

    public long getJAICapacity() {
        return JAICapacity;
    }

    public void setJAICapacity(long JAICapacity) {
        this.JAICapacity = JAICapacity;
    }

    public final float getCompressionRatio() {
        return compressionRatio;
    }

    public final String getCompressionScheme() {
        return compressionScheme;
    }

    public int getDownsampleStep() {
        return downsampleStep;
    }

    public String getScaleAlgorithm() {
        return scaleAlgorithm;
    }

    public int getTileH() {
        return tileH;
    }

    public int getTileW() {
        return tileW;
    }

    public void setCompressionRatio(float compressionRatio) {
        this.compressionRatio = compressionRatio;
    }

    public void setCompressionScheme(String compressionScheme) {
        this.compressionScheme = compressionScheme;
    }

    public void setDownsampleStep(int downsampleWH) {
        this.downsampleStep = downsampleWH;
    }

    public void setScaleAlgorithm(String scaleAlgorithm) {
        this.scaleAlgorithm = scaleAlgorithm;
    }

    public void setTileH(int tileH) {
        this.tileH = tileH;
    }

    public void setTileW(int tileW) {
        this.tileW = tileW;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(int numSteps) {
        this.numSteps = numSteps;
    }

    public String getWildcardString() {
        return wildcardString;
    }

    public void setWildcardString(String wildcardString) {
        this.wildcardString = wildcardString;
    }

    public int getInterp() {
        return interp;
    }

    public void setInterp(int interp) {
        this.interp = interp;
    }

    public boolean isLogNotification() {
        return logNotification;
    }

    public void setLogNotification(boolean logNotification) {
        this.logNotification = logNotification;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + ", name:" + getName()
                + ", wxh:" + getTileW() + "x" + getTileH() + ", stp:" + getNumSteps() + "]";
    }

    @Override
    public GeotiffOverviewsEmbedderConfiguration clone() {
        final GeotiffOverviewsEmbedderConfiguration configuration = (GeotiffOverviewsEmbedderConfiguration) super.clone();
        return configuration;
    }
}
