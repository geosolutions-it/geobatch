/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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

package it.geosolutions.geobatch.geotiff.retile;

import it.geosolutions.geobatch.annotations.ManageAlias;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import org.geotools.utils.CoverageToolsConstants;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@ManageAlias
@XStreamAlias("GeotiffRetilerConfiguration")
public class GeotiffRetilerConfiguration extends ActionConfiguration {


    public GeotiffRetilerConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    protected GeotiffRetilerConfiguration() {
        super("xstream loader", "xstream loader", "xstream loader");
    }

    private long JAICapacity;
    private double compressionRatio = Double.NaN;

    private String compressionScheme = CoverageToolsConstants.DEFAULT_COMPRESSION_SCHEME;

    /** Tile height. */
    private int tileH = 256;

    /** Tile width. */
    private int tileW = 256;
    
    /** rewrite geotiff forcing bigtiff */
    private boolean forceToBigTiff = false;

	public boolean isForceToBigTiff() {
		return forceToBigTiff;
	}

	public void setForceToBigTiff(boolean forceToBigTiff) {
		this.forceToBigTiff = forceToBigTiff;
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

    public long getJAICapacity() {
        return JAICapacity;
    }

    public void setJAICapacity(long JAICapacity) {
        this.JAICapacity = JAICapacity;
    }

    @Override
	public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + " srvId:" + getServiceID()
                + " name:" + getName() + " size:" + getTileW() + "x" + getTileH() + "]";
    }

	@Override
    public GeotiffRetilerConfiguration clone() {
        GeotiffRetilerConfiguration configuration = (GeotiffRetilerConfiguration) super.clone();
        // final GeotiffRetilerConfiguration configuration=
        // new
        // GeotiffRetilerConfiguration(getId(),getName(),getDescription(),isDirty());
/*        configuration.setCompressionRatio(compressionRatio);
        configuration.setCompressionScheme(compressionScheme);
        configuration.setJAICapacity(JAICapacity);
        configuration.setServiceID(serviceID);
        configuration.setForceToBigTiff(forceToBigTiff);
        configuration.setTileH(tileH);
        configuration.setTileW(tileW);
*/
        return configuration;
    }
}
