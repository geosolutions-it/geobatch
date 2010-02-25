/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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



package it.geosolutions.geobatch.geotiff.overview;

import javax.media.jai.Interpolation;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import org.geotools.utils.CoverageToolsConstants;

public class GeoTiffOverviewsEmbedderConfiguration extends ActionConfiguration implements
        Configuration {

	public GeoTiffOverviewsEmbedderConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
	}

	private long JAICapacity;
	
    public long getJAICapacity() {
		return JAICapacity;
	}

	public void setJAICapacity(long JAICapacity) {
			this.JAICapacity = JAICapacity;
	}

	private String workingDirectory;

    private double compressionRatio = CoverageToolsConstants.DEFAULT_COMPRESSION_RATIO;

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

    private String serviceID;

    public GeoTiffOverviewsEmbedderConfiguration() {
        super();
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public final double getCompressionRatio() {
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

    public void setCompressionRatio(double compressionRatio) {
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

    /**
     * @return the serviceID
     */
    public String getServiceID() {
        return serviceID;
    }

    /**
     * @param serviceID
     *            the serviceID to set
     */
    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "id:" + getId()
				+ ", name:" + getName()
				+ ", wxh:" + getTileW() + "x" + getTileH()
				+ ", stp:" + getNumSteps()
				+ "]";
	}

	@Override
	public GeoTiffOverviewsEmbedderConfiguration clone() throws CloneNotSupportedException {
		final GeoTiffOverviewsEmbedderConfiguration configuration= 
			new GeoTiffOverviewsEmbedderConfiguration(getId(),getName(),getDescription(),isDirty());
		configuration.setCompressionRatio(compressionRatio);
		configuration.setCompressionScheme(compressionScheme);
		configuration.setDownsampleStep(downsampleStep);
		configuration.setInterp(interp);
		configuration.setJAICapacity(JAICapacity);
		configuration.setNumSteps(numSteps);
		configuration.setScaleAlgorithm(scaleAlgorithm);
		configuration.setTileH(tileH);
		configuration.setTileW(tileW);
		configuration.setWildcardString(wildcardString);
		configuration.setWorkingDirectory(workingDirectory);
		configuration.setServiceID(serviceID);
		configuration.setLogNotification(logNotification);
		
		return configuration;
	}
}
