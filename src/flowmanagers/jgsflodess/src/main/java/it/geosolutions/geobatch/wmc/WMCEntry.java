/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.wmc;

import java.util.HashMap;
import java.util.Map;

public class WMCEntry {

	private String layerTitle;
	private String layerName;
	private String nameSpace;
	private Map<String, Map<String, String>> dimensions;

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public WMCEntry(final String nameSpace, final String layerName){
		this.layerName = layerName;
		this.nameSpace = nameSpace;
	}

	/**
	 * @param dimensions the dimensions to set
	 */
	public void setDimensions(Map<String, Map<String, String>> dimensions) {
		this.dimensions = dimensions;
	}

	/**
	 * @return the dimensions
	 */
	public Map<String, Map<String, String>> getDimensions() {
		if (dimensions == null)
			dimensions = new HashMap<String, Map<String,String>>();
		
		return dimensions;
	}

	/**
	 * @param layerTitle the layerTitle to set
	 */
	public void setLayerTitle(String layerTitle) {
		this.layerTitle = layerTitle;
	}

	/**
	 * @return the layerTitle
	 */
	public String getLayerTitle() {
		return layerTitle;
	}
	
}
