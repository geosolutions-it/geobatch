/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2008-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.geoserver.shapefile;

import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;

import java.net.URL;
import java.nio.charset.Charset;

/**
 * Extend {@link GeoServerActionConfiguration} with connection
 * parameters specific for shapefile datastores.
 * 
 * @author Oscar Fonts
 */
public class GeoServerShapeActionConfiguration extends GeoServerActionConfiguration {

	URL url;
	Charset charset;
	Boolean createSpatialIndex;
	Boolean memoryMappedBuffer;
	Boolean cacheAndReuseMemoryMaps;
	
	
	public GeoServerShapeActionConfiguration(String id, String name, String description) {
		super(id, name, description);
	}
	
	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public Boolean getCreateSpatialIndex() {
		return createSpatialIndex;
	}

	public void setCreateSpatialIndex(Boolean createSpatialIndex) {
		this.createSpatialIndex = createSpatialIndex;
	}

	public Boolean getMemoryMappedBuffer() {
		return memoryMappedBuffer;
	}

	public void setMemoryMappedBuffer(Boolean memoryMappedBuffer) {
		this.memoryMappedBuffer = memoryMappedBuffer;
	}

	public Boolean getCacheAndReuseMemoryMaps() {
		return cacheAndReuseMemoryMaps;
	}

	public void setCacheAndReuseMemoryMaps(Boolean cacheAndReuseMemoryMaps) {
		this.cacheAndReuseMemoryMaps = cacheAndReuseMemoryMaps;
	}

    @Override
    public GeoServerShapeActionConfiguration clone() { 
        final GeoServerShapeActionConfiguration configuration = (GeoServerShapeActionConfiguration) super
                .clone();

        configuration.setUrl(url);
        configuration.setCharset(charset);
        configuration.setCreateSpatialIndex(createSpatialIndex);
        configuration.setMemoryMappedBuffer(memoryMappedBuffer);
        configuration.setCacheAndReuseMemoryMaps(cacheAndReuseMemoryMaps);
        
        return configuration;
    }
}
