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

package it.geosolutions.geobatch.compose;

import it.geosolutions.geobatch.base.BaseImageProcessingConfiguration;
import it.geosolutions.geobatch.catalog.Configuration;


/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class ComposerConfiguration extends BaseImageProcessingConfiguration implements
        Configuration {

    private final static long DEFAULT_JAI_CACHE_CAPACITY = 128 * 1024 * 1024;

    private final static int DEFAULT_JAI_PARALLELISM = 2;

    private final static float DEFAULT_JAI_CACHE_THRESHOLD = 0.75f;

    private long JAICacheCapacity = DEFAULT_JAI_CACHE_CAPACITY;

    private int JAIParallelism = DEFAULT_JAI_PARALLELISM;

    private float JAICacheThreshold = DEFAULT_JAI_CACHE_THRESHOLD;

    private String outputFormat;

    private String outputBaseFolder;

    private String leavesFolders;

    private String inputFormats;

    private String geoserverURL;

    private String geoserverUID;

    private String geoserverPWD;

    private String geoserverUploadMethod;
    
    private String geowebcacheWatchingDir;

	private int chunkW = 10240;
    
    private int chunkH = 10240;
    
    private String rawScaleAlgorithm;
    
    private String mosaicScaleAlgorithm;
    
    private float logarithmMultiplier = 20;
    
    private float logarithmBase = 10;
    
    private boolean logNotification = false;

    public void setLogNotification(boolean logNotification) {
		this.logNotification = logNotification;
	}

	public boolean isLogNotification() {
		return logNotification;
	}

	public void setLogarithmMultiplier(float logarithmMultiplier) {
		this.logarithmMultiplier = logarithmMultiplier;
	}

	public float getLogarithmMultiplier() {
		return logarithmMultiplier;
	}

	public void setLogarithmBase(float logarithmBase) {
		this.logarithmBase = logarithmBase;
	}

	public float getLogarithmBase() {
		return logarithmBase;
	}
    
    public String getRawScaleAlgorithm() {
        return rawScaleAlgorithm;
    }

    public void setRawScaleAlgorithm(final String rawScaleAlgorithm) {
        this.rawScaleAlgorithm = rawScaleAlgorithm;
    }

    public String getMosaicScaleAlgorithm() {
        return mosaicScaleAlgorithm;
    }

    public void setMosaicScaleAlgorithm(final String mosaicScaleAlgorithm) {
        this.mosaicScaleAlgorithm = mosaicScaleAlgorithm;
    }
    
    public String getGeoserverURL() {
        return geoserverURL;
    }

    public void setGeoserverURL(String geoserverURL) {
        this.geoserverURL = geoserverURL;
    }

    public String getGeoserverUID() {
        return geoserverUID;
    }

    public void setGeoserverUID(String geoserverUID) {
        this.geoserverUID = geoserverUID;
    }

    public String getGeoserverPWD() {
        return geoserverPWD;
    }

    public void setGeoserverPWD(String geoserverPWD) {
        this.geoserverPWD = geoserverPWD;
    }

    public String getGeoserverUploadMethod() {
        return geoserverUploadMethod;
    }

    public void setGeoserverUploadMethod(String geoserverUploadMethod) {
        this.geoserverUploadMethod = geoserverUploadMethod;
    }

    public String getGeowebcacheWatchingDir() {
		return geowebcacheWatchingDir;
	}

	public void setGeowebcacheWatchingDir(String geowebcacheWatchingDir) {
		this.geowebcacheWatchingDir = geowebcacheWatchingDir;
	}
    

    public ComposerConfiguration() {
        super();
    }

    protected ComposerConfiguration(String id, String name, String description,
            boolean dirty) {
        super(id, name, description, dirty);
    }

  

    public int getChunkW() {
        return chunkW;
    }

    public void setChunkW(final int chunkW) {
        this.chunkW = chunkW;
    }

    public int getChunkH() {
        return chunkH;
    }

    public void setChunkH(final int chunkH) {
        this.chunkH = chunkH;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getInputFormats() {
        return inputFormats;
    }

    public void setInputFormats(String inputFormats) {
        this.inputFormats = inputFormats;
    }

    public float getJAICacheThreshold() {
        return JAICacheThreshold;
    }

    public void setJAICacheThreshold(float cacheThreshold) {
        JAICacheThreshold = cacheThreshold;
    }

    public int getJAIParallelism() {
        return JAIParallelism;
    }

    public void setJAIParallelism(int parallelism) {
        JAIParallelism = parallelism;
    }

    public long getJAICacheCapacity() {
        return JAICacheCapacity;
    }

    public void setJAICacheCapacity(final long JAICacheCapacity) {
        this.JAICacheCapacity = JAICacheCapacity;
    }

    public String getOutputBaseFolder() {
        return outputBaseFolder;
    }

    public void setOutputBaseFolder(String outputBaseFolder) {
        this.outputBaseFolder = outputBaseFolder;
    }

    public String getLeavesFolders() {
        return leavesFolders;
    }

    public void setLeavesFolders(String leavesFolders) {
        this.leavesFolders = leavesFolders;
    }
    
    @Override
    public BaseImageProcessingConfiguration clone() throws CloneNotSupportedException {
        final ComposerConfiguration configuration = new ComposerConfiguration(
                getId(), getName(), getDescription(), isDirty());
        configuration.setServiceID(getServiceID());
        configuration.setChunkH(chunkH);
        configuration.setChunkW(chunkW);
        configuration.setCompressionRatio(getCompressionRatio());
        configuration.setCompressionScheme(getCompressionScheme());
        configuration.setDownsampleStep(getDownsampleStep());
        configuration.setInputFormats(inputFormats);
        configuration.setJAICacheCapacity(JAICacheCapacity);
        configuration.setJAICacheThreshold(JAICacheThreshold);
        configuration.setJAIParallelism(JAIParallelism);
        configuration.setLeavesFolders(leavesFolders);
        configuration.setNumSteps(getNumSteps());
        configuration.setOutputBaseFolder(outputBaseFolder);
        configuration.setOutputFormat(outputFormat);
        configuration.setRawScaleAlgorithm(rawScaleAlgorithm);
        configuration.setMosaicScaleAlgorithm(mosaicScaleAlgorithm);
        configuration.setTileH(getTileH());
        configuration.setTileW(getTileW());
        configuration.setWorkingDirectory(getWorkingDirectory());
        configuration.setGeoserverPWD(geoserverPWD);
        configuration.setGeoserverUID(geoserverUID);
        configuration.setGeoserverUploadMethod(geoserverUploadMethod);
        configuration.setGeoserverURL(geoserverURL);
        configuration.setLogarithmBase(logarithmBase);
        configuration.setLogarithmMultiplier(logarithmMultiplier);
        configuration.setLogNotification(logNotification);
        configuration.setCorePoolSize(getCorePoolSize());
        configuration.setMaxPoolSize(getMaxPoolSize());
        configuration.setMaxWaitingTime(getMaxWaitingTime());
        configuration.setGeowebcacheWatchingDir(geowebcacheWatchingDir);
        return configuration;
    }

}
