/*
 * $Header: it.geosolutions.geobatch.wmc.model.WMCLayer,v. 0.1 02/dic/2009 16:54:41 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 16:54:41 $
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

import java.util.List;

/**
 * @author Fabiani
 *
 */
public class WMCLayer {
	private String queryable = "0"; 
	private String hidden = "0";
	private WMCServer server;
	private String name;
	private String title;
	private String srs;
	private List<WMCFormat> formatList;
	private List<WMCStyle> styleList;
	private WMCExtension extension;
	
	/**
	 * @param queryable
	 * @param hidden
	 * @param name
	 * @param title
	 * @param srs
	 */
	public WMCLayer(String queryable, String hidden, String name, String title,
			String srs) {
		this.queryable = queryable;
		this.hidden = hidden;
		this.name = name;
		this.title = title;
		this.srs = srs;
	}

	/**
	 * @return the queryable
	 */
	public String getQueryable() {
		return queryable;
	}

	/**
	 * @param queryable the queryable to set
	 */
	public void setQueryable(String queryable) {
		this.queryable = queryable;
	}

	/**
	 * @return the hidden
	 */
	public String getHidden() {
		return hidden;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(String hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the server
	 */
	public WMCServer getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(WMCServer server) {
		this.server = server;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the srs
	 */
	public String getSrs() {
		return srs;
	}

	/**
	 * @param srs the srs to set
	 */
	public void setSrs(String srs) {
		this.srs = srs;
	}

	/**
	 * @return the formatList
	 */
	public List<WMCFormat> getFormatList() {
		return formatList;
	}

	/**
	 * @param formatList the formatList to set
	 */
	public void setFormatList(List<WMCFormat> formatList) {
		this.formatList = formatList;
	}

	/**
	 * @return the styleList
	 */
	public List<WMCStyle> getStyleList() {
		return styleList;
	}

	/**
	 * @param styleList the styleList to set
	 */
	public void setStyleList(List<WMCStyle> styleList) {
		this.styleList = styleList;
	}

	/**
	 * @return the extension
	 */
	public WMCExtension getExtension() {
		return extension;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(WMCExtension extension) {
		this.extension = extension;
	}
	
}
