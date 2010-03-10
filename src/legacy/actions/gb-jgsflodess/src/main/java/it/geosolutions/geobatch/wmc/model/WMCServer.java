/*
 * $Header: it.geosolutions.geobatch.wmc.model.WMCServer,v. 0.1 02/dic/2009 18:08:01 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 18:08:01 $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. 
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.geobatch.wmc.model;

/**
 * @author Fabiani
 *
 */
public class WMCServer {
	private String service = "wms"; 
	private String version = "1.1.1";
	private String title;
	private WMCOnlineResource onlineResource;
	
	/**
	 * @param service
	 * @param version
	 * @param title
	 */
	public WMCServer(String service, String version, String title) {
		this.service = service;
		this.version = version;
		this.title = title;
	}

	/**
	 * @return the service
	 */
	public String getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(String service) {
		this.service = service;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the onlineResource
	 */
	public WMCOnlineResource getOnlineResource() {
		return onlineResource;
	}

	/**
	 * @param onlineResource the onlineResource to set
	 */
	public void setOnlineResource(WMCOnlineResource onlineResource) {
		this.onlineResource = onlineResource;
	}
	
}
