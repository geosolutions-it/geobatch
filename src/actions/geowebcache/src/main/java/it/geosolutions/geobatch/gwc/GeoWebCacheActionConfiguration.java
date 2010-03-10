/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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



package it.geosolutions.geobatch.gwc;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;



public class GeoWebCacheActionConfiguration extends ActionConfiguration implements Configuration {

	protected GeoWebCacheActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
	}
    
	private String gwcUrl;
    private String workingDirectory;
    private String geoserverUrl;    
    private String zoomStart;  
    private String zoomStop;     
    private String metaWidth;    
    private String metaHeight;    
    private String gutter;    
    private String transparent;
    private String tiled;
    private String expireCache;    
    private String expireClients;    
    private String gwcUser;
    private String gwcPassword;
    
    public GeoWebCacheActionConfiguration() {
        super();
    }
    
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	public String getGwcUrl() {
		return gwcUrl;
	}

	public void setGwcUrl(String gwcUrl) {
		this.gwcUrl = gwcUrl;
	}
	
	public String getGeoserverUrl() {
		return geoserverUrl;
	}

	public void setGeoserverUrl(String geoserverUrl) {
		this.geoserverUrl = geoserverUrl;
	}
	
	public String getZoomStart() {
		return zoomStart;
	}

	public void setZoomStart(String zoomStart) {
		this.zoomStart = zoomStart;
	}

	public String getZoomStop() {
		return zoomStop;
	}

	public void setZoomStop(String zoomStop) {
		this.zoomStop = zoomStop;
	}

	public String getMetaWidth() {
		return metaWidth;
	}

	public void setMetaWidth(String metaWidth) {
		this.metaWidth = metaWidth;
	}

	public String getMetaHeight() {
		return metaHeight;
	}

	public void setMetaHeight(String metaHeight) {
		this.metaHeight = metaHeight;
	}

	public String getGutter() {
		return gutter;
	}

	public void setGutter(String gutter) {
		this.gutter = gutter;
	}

	public String getTransparent() {
		return transparent;
	}

	public void setTransparent(String transparent) {
		this.transparent = transparent;
	}

	public String getTiled() {
		return tiled;
	}

	public void setTiled(String tiled) {
		this.tiled = tiled;
	}

	public String getExpireCache() {
		return expireCache;
	}

	public void setExpireCache(String expireCache) {
		this.expireCache = expireCache;
	}

	public String getExpireClients() {
		return expireClients;
	}

	public void setExpireClients(String expireClients) {
		this.expireClients = expireClients;
	}
	
	public String getGwcUser() {
		return gwcUser;
	}

	public void setGwcUser(String gwcUser) {
		this.gwcUser = gwcUser;
	}

	public String getGwcPassword() {
		return gwcPassword;
	}

	public void setGwcPassword(String gwcPassword) {
		this.gwcPassword = gwcPassword;
	}
	
    @Override
    public ActionConfiguration clone() throws CloneNotSupportedException {
		final GeoWebCacheActionConfiguration configuration = 
			new GeoWebCacheActionConfiguration(super.getId(),super.getName(),super.getDescription(),super.isDirty());
		
		configuration.setServiceID(getServiceID());
		configuration.setGwcUrl(gwcUrl);
		configuration.setWorkingDirectory(workingDirectory);
		configuration.setGeoserverUrl(geoserverUrl);
		configuration.setZoomStart(zoomStart);
		configuration.setZoomStop(zoomStop);
		configuration.setTiled(tiled);
		configuration.setExpireCache(expireCache);
		configuration.setExpireClients(expireClients);
		configuration.setGutter(gutter);
		configuration.setMetaHeight(metaHeight);
		configuration.setMetaWidth(metaWidth);
		configuration.setTransparent(transparent);		
		configuration.setGwcPassword(gwcPassword);		
		configuration.setGwcUser(gwcUser);

		return configuration;
    }
}
