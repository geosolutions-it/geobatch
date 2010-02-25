/*
 * $Header: it.geosolutions.geobatch.wmc.model.WMCOnlineResource,v. 0.1 02/dic/2009 18:15:23 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 18:15:23 $
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
public class WMCOnlineResource {
	private String xlink_type = "simple";
	private String xlink_href;
	
	/**
	 * @param xlink_type
	 * @param xlink_href
	 */
	public WMCOnlineResource(String xlink_type, String xlink_href) {
		this.xlink_type = xlink_type;
		this.xlink_href = xlink_href;
	}

	/**
	 * @return the xlink_type
	 */
	public String getXlink_type() {
		return xlink_type;
	}

	/**
	 * @param xlink_type the xlink_type to set
	 */
	public void setXlink_type(String xlink_type) {
		this.xlink_type = xlink_type;
	}

	/**
	 * @return the xlink_href
	 */
	public String getXlink_href() {
		return xlink_href;
	}

	/**
	 * @param xlink_href the xlink_href to set
	 */
	public void setXlink_href(String xlink_href) {
		this.xlink_href = xlink_href;
	}
	
}
