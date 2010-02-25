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



package it.geosolutions.geobatch.geotiff.retile;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import org.geotools.utils.CoverageToolsConstants;

public class GeoTiffRetilerConfiguration extends ActionConfiguration implements
        Configuration {

	protected GeoTiffRetilerConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
	}

	private long JAICapacity;
	
    public long getJAICapacity() {
		return JAICapacity;
	}

	public void setJAICapacity(long JAICapacity) {
			this.JAICapacity = JAICapacity;
	}

	private String workingDirectory;

    private double compressionRatio = Double.NaN;
    
    private String compressionScheme = CoverageToolsConstants.DEFAULT_COMPRESSION_SCHEME;

    /** Tile height. */
    private int tileH = 256;

    /** Tile width. */
    private int tileW = 256;

    private String serviceID;

    public GeoTiffRetilerConfiguration() {
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
				+ "]";
	}

	@Override
	public GeoTiffRetilerConfiguration clone() throws CloneNotSupportedException {
		final GeoTiffRetilerConfiguration configuration= 
			new GeoTiffRetilerConfiguration(getId(),getName(),getDescription(),isDirty());
		configuration.setCompressionRatio(compressionRatio);
		configuration.setCompressionScheme(compressionScheme);
		configuration.setJAICapacity(JAICapacity);
		configuration.setServiceID(serviceID);
		configuration.setTileH(tileH);
		configuration.setTileW(tileW);
		configuration.setWorkingDirectory(workingDirectory);
		return null;
	}
}
