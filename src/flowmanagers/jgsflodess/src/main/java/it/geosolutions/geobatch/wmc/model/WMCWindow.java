/*
 * $Header: it.geosolutions.geobatch.wmc.model.WMCWindow,v. 0.1 02/dic/2009 16:57:46 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 16:57:46 $
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
public class WMCWindow {
	private Integer height;
	private Integer width;
	private WMCBoundingBox bbox;
	
	/**
	 * @param height
	 * @param width
	 */
	public WMCWindow(Integer height, Integer width) {
		this.height = height;
		this.width = width;
	}
	/**
	 * @param height the height to set
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}
	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height;
	}
	/**
	 * @param width the width to set
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}
	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}
	/**
	 * @param bbox the bbox to set
	 */
	public void setBbox(WMCBoundingBox bbox) {
		this.bbox = bbox;
	}
	/**
	 * @return the bbox
	 */
	public WMCBoundingBox getBbox() {
		return bbox;
	}
	
}
