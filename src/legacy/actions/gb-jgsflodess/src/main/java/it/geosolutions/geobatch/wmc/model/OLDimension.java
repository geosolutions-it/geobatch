/*
 * $Header: it.geosolutions.geobatch.wmc.model.OLIsBaseLayer,v. 0.1 03/dic/2009 01:22:17 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 03/dic/2009 01:22:17 $
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
public class OLDimension extends OLBaseClass {

	private String name;
	private String defaultValue;
	
	public OLDimension(String values, String name, String defaultValue) {
		super(values);
		this.name = name;
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

}
