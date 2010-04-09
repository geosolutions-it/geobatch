package it.geosolutions.geobatch.geoserver.matfile5.sas;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;

public class SasMosaicGeoServerConfiguration extends GeoServerActionConfiguration implements Configuration{
	
	private String geowebcacheWatchingDir;
	
	 protected SasMosaicGeoServerConfiguration(String id, String name,
				String description, boolean dirty) {
			super(id, name, description, dirty);
			// TODO Auto-generated constructor stub
		}

	public SasMosaicGeoServerConfiguration() {
		super();
	}

    public String getGeowebcacheWatchingDir() {
		return geowebcacheWatchingDir;
	}

	public void setGeowebcacheWatchingDir(String geowebcacheWatchingDir) {
		this.geowebcacheWatchingDir = geowebcacheWatchingDir;
	}
    

}
