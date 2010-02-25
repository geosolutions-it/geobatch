/*
 * $Header: it.geosolutions.geobatch.wmc.model.GeneralWMCConfiguration,v. 0.1 02/dic/2009 16:54:34 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 16:54:34 $
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
public class GeneralWMCConfiguration {
	private WMCWindow window;
	private String title;
	private String _abstract;

	
	/**
	 * @param window
	 * @param title
	 * @param _abstract
	 */
	public GeneralWMCConfiguration(WMCWindow window, String title, String _abstract) {
		this.window = window;
		this.title = title;
		this._abstract = _abstract;
	}

	/**
	 * @param window the window to set
	 */
	public void setWindow(WMCWindow window) {
		this.window = window;
	}

	/**
	 * @return the window
	 */
	public WMCWindow getWindow() {
		return window;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param _abstract the _abstract to set
	 */
	public void setAbstract(String _abstract) {
		this._abstract = _abstract;
	}

	/**
	 * @return the _abstract
	 */
	public String getAbstract() {
		return _abstract;
	}
}
