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

package it.geosolutions.geobatch.mosaic;

import it.geosolutions.geobatch.base.BaseImageProcessingConfiguration;
import it.geosolutions.geobatch.catalog.Configuration;


/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class MosaicerConfiguration extends BaseImageProcessingConfiguration implements
        Configuration {

    private String mosaicDirectory;

    public String getMosaicDirectory() {
        return mosaicDirectory;
    }

    public void setMosaicDirectory(String mosaicDirectory) {
        this.mosaicDirectory = mosaicDirectory;
    }

    private int chunkWidth = 5120;

    private int chunkHeight = 5120;

    private int chunkSize;

    private int tileSizeLimit;
    
   private float logarithmMultiplier = 20;
    
    private float logarithmBase = 10;

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

    public MosaicerConfiguration() {
        super();
    }

    protected MosaicerConfiguration(String id, String name, String description,
            boolean dirty) {
        super(id, name, description, dirty);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + ", name:"
                + getName() + ", wxh:" + getTileW() + "x" + getTileH() + "]";
    }

    public int getTileSizeLimit() {
        return tileSizeLimit;
    }

    public void setTileSizeLimit(final int tileSizeLimit) {
        this.tileSizeLimit = tileSizeLimit;
    }

    public int getChunkWidth() {
        return chunkWidth;
    }

    public void setChunkWidth(final int chunkWidth) {
        this.chunkWidth = chunkWidth;
    }

    public int getChunkHeight() {
        return chunkHeight;
    }

    public void setChunkHeight(final int chunkHeight) {
        this.chunkHeight = chunkHeight;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(final int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public MosaicerConfiguration clone() throws CloneNotSupportedException {
        final MosaicerConfiguration configuration = new MosaicerConfiguration(
                getId(), getName(), getDescription(), isDirty());
        configuration.setChunkHeight(chunkHeight);
        configuration.setChunkWidth(chunkWidth);
        configuration.setChunkSize(chunkSize);
        configuration.setCompressionRatio(getCompressionRatio());
        configuration.setCompressionScheme(getCompressionScheme());
        configuration.setDownsampleStep(getDownsampleStep());
        configuration.setMosaicDirectory(mosaicDirectory);
        configuration.setNumSteps(getNumSteps());
        configuration.setScaleAlgorithm(getScaleAlgorithm());
        configuration.setServiceID(getServiceID());
        configuration.setTileH(getTileH());
        configuration.setTileW(getTileW());
        configuration.setTileSizeLimit(tileSizeLimit);
        configuration.setWorkingDirectory(getWorkingDirectory());
        configuration.setMaxWaitingTime(getMaxWaitingTime());
        configuration.setCorePoolSize(getCorePoolSize());
        configuration.setMaxPoolSize(getMaxPoolSize());
        configuration.setLogarithmBase(logarithmBase);
        configuration.setLogarithmMultiplier(logarithmMultiplier);
        return configuration;
    }

}
