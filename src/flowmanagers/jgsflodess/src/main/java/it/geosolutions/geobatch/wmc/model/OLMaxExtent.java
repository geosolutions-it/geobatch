/*
 * $Header: it.geosolutions.geobatch.wmc.model.OLMaxExtent,v. 0.1 03/dic/2009 01:23:16 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 03/dic/2009 01:23:16 $
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
public class OLMaxExtent extends OLBaseClass {

	private Double maxx = 180.0; 
	private Double maxy = 90.0; 
	private Double minx = -180.0; 
	private Double miny = -90.0;
	
	public OLMaxExtent(String content) {
		super(content);
	}

	/**
	 * @return the maxx
	 */
	public Double getMaxx() {
		return maxx;
	}

	/**
	 * @param maxx the maxx to set
	 */
	public void setMaxx(Double maxx) {
		this.maxx = maxx;
	}

	/**
	 * @return the maxy
	 */
	public Double getMaxy() {
		return maxy;
	}

	/**
	 * @param maxy the maxy to set
	 */
	public void setMaxy(Double maxy) {
		this.maxy = maxy;
	}

	/**
	 * @return the minx
	 */
	public Double getMinx() {
		return minx;
	}

	/**
	 * @param minx the minx to set
	 */
	public void setMinx(Double minx) {
		this.minx = minx;
	}

	/**
	 * @return the miny
	 */
	public Double getMiny() {
		return miny;
	}

	/**
	 * @param miny the miny to set
	 */
	public void setMiny(Double miny) {
		this.miny = miny;
	}

}
