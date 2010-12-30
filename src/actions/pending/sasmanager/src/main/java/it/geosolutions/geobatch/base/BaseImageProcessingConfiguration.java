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
package it.geosolutions.geobatch.base;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

/**
 * A Base Configuration class sharing common configuration's parameters
 *
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public abstract class BaseImageProcessingConfiguration extends ActionConfiguration
		implements Configuration {

	private String workingDirectory;
	
	/** Downsampling step. */
	private int downsampleStep;
	
	/** Number of steps -> overviews */
	private int numSteps;
	
	/** Scale algorithm. */
	private String scaleAlgorithm;
	
	/** Compression ratio */
	private double compressionRatio = 0.75f;
	
	/** Compression type */
	private String compressionScheme = "LZW";
	
	/** Tile height. */
	private int tileH = -1;
	
	/** Tile width. */
	private int tileW = -1;
	
	private String serviceID;

	private String time = "";
	
	private int corePoolSize = 1;
	
	private int maxPoolSize = 1;
	
	private long maxWaitingTime = 300; //Seconds
    
	public BaseImageProcessingConfiguration(String id, String name, String description,
			boolean dirty) {
		super(id, name, description, dirty);
	}

	public BaseImageProcessingConfiguration() {
		super();
	}

	public void setTime(final String time) {
        this.time = time;
    }
    
    public String getTime() {
        return time;
    }
    
    public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(final int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(final int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public long getMaxWaitingTime() {
		return maxWaitingTime;
	}

	public void setMaxWaitingTime(final long maxWaitingTime) {
		this.maxWaitingTime = maxWaitingTime;
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
	public void setWorkingDirectory(final String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public int getDownsampleStep() {
		return downsampleStep;
	}

	public String getScaleAlgorithm() {
		return scaleAlgorithm;
	}

	public void setDownsampleStep(final int downsampleWH) {
		this.downsampleStep = downsampleWH;
	}

	public void setScaleAlgorithm(final String scaleAlgorithm) {
		this.scaleAlgorithm = scaleAlgorithm;
	}

	public int getNumSteps() {
		return numSteps;
	}

	public void setNumSteps(final int numSteps) {
		this.numSteps = numSteps;
	}

	public final double getCompressionRatio() {
		return compressionRatio;
	}

	public final String getCompressionScheme() {
		return compressionScheme;
	}

	public void setCompressionRatio(double compressionRatio) {
		this.compressionRatio = compressionRatio;
	}

	public void setCompressionScheme(String compressionScheme) {
		this.compressionScheme = compressionScheme;
	}

	public int getTileH() {
	    return tileH;
	}

	public int getTileW() {
	    return tileW;
	}

	public void setTileH(int tileH) {
	    this.tileH = tileH;
	}

	public void setTileW(int tileW) {
	    this.tileW = tileW;
	}

	/**
	 * @return the serviceID
	 */
	public String getServiceID() {
	    return serviceID;
	}

	/**
	 * @param serviceID
	 *                the serviceID to set
	 */
	public void setServiceID(String serviceID) {
	    this.serviceID = serviceID;
	}
}
