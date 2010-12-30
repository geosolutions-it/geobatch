/*
 * $Header: it.geosolutions.geobatch.wmc.model.WMCFormat,v. 0.1 02/dic/2009 18:08:08 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 18:08:08 $
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
public class WMCFormat {
	private String current = "1";
	private String content;
	
	/**
	 * @param current
	 * @param content
	 */
	public WMCFormat(String current, String content) {
		this.setCurrent(current);
		this.content = content;
	}

	/**
	 * @param current the current to set
	 */
	public void setCurrent(String current) {
		this.current = current;
	}

	/**
	 * @return the current
	 */
	public String getCurrent() {
		return current;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

}
