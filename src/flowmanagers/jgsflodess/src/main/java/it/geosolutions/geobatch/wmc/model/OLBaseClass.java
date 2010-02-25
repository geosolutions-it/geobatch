/*
 * $Header: it.geosolutions.geobatch.wmc.model.OLBaseClass,v. 0.1 03/dic/2009 01:20:48 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 03/dic/2009 01:20:48 $
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
public class OLBaseClass {
	private String xmlns_ol = "http://openlayers.org/context";
	private String content;

	/**
	 * @param content
	 */
	public OLBaseClass(String content) {
		this.setContent(content);
	}

	/**
	 * @param xmlns_ol the xmlns_ol to set
	 */
	public void setXmlns_ol(String xmlns_ol) {
		this.xmlns_ol = xmlns_ol;
	}

	/**
	 * @return the xmlns_ol
	 */
	public String getXmlns_ol() {
		return xmlns_ol;
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
