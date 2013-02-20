/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.commons.listener;

import java.io.Serializable;

/**
 * 
 * This is a simple Warning container helper class
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class Warning implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String source;
	private final String location;
	private final String warning;

	public Warning(String source, String location, String warning) {
		super();
		this.source = source;
		this.location = location;
		this.warning = warning;
	}

	@Override
	public String toString() {
		return "Warning [location=" + location + ", source=" + source
				+ ", warning=" + warning + "]";
	}

	public String getSource() {
		return source;
	}

	public String getLocation() {
		return location;
	}

	public String getWarning() {
		return warning;
	}
}