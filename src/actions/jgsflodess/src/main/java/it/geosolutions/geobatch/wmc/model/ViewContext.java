/*
 * $Header: it.geosolutions.geobatch.wmc.model.ViewContext,v. 0.1 02/dic/2009 16:52:25 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 16:52:25 $
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
public class ViewContext {
	private String xmlns = "http://www.opengis.net/context";
	private String xlink = "http://www.w3.org/1999/xlink";
	private String id;
	private String version;
	private GeneralWMCConfiguration general;
	private List<WMCLayer> layerList;
	
	/**
	 * @param id
	 * @param version
	 */
	public ViewContext(String id, String version) {
		this.setId(id);
		this.setVersion(version);
	}
	/**
	 * @param general the general to set
	 */
	public void setGeneral(GeneralWMCConfiguration general) {
		this.general = general;
	}
	/**
	 * @return the general
	 */
	public GeneralWMCConfiguration getGeneral() {
		return general;
	}
	/**
	 * @param layerList the layerList to set
	 */
	public void setLayerList(List<WMCLayer> layerList) {
		this.layerList = layerList;
	}
	/**
	 * @return the layerList
	 */
	public List<WMCLayer> getLayerList() {
		return layerList;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param xmlns the xmlns to set
	 */
	public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}
	/**
	 * @return the xmlns
	 */
	public String getXmlns() {
		return xmlns;
	}
	/**
	 * @param xlink the xlink to set
	 */
	public void setXlink(String xlink) {
		this.xlink = xlink;
	}
	/**
	 * @return the xlink
	 */
	public String getXlink() {
		return xlink;
	}
}
