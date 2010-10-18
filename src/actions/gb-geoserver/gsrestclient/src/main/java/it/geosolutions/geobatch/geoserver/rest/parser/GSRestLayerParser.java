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
package it.geosolutions.geobatch.geoserver.rest.parser;

import org.jdom.Element;

/**
 * Parses fields of a layer got via a
 * {@link it.geosolutions.geobatch.geoserver.rest.GeoServerRESTReader}.
 * 
 * @author etj
 */
public class GSRestLayerParser {
	private final Element layerElem;

	public enum TYPE {
		VECTOR("VECTOR"), RASTER("RASTER"), UNKNOWN(null);

		private final String restName;

		private TYPE(String restName) {
			this.restName = restName;
		}

		public static TYPE get(String restName) {
			for (TYPE type : values()) {
				if (type == UNKNOWN)
					continue;
				if (type.restName.equals(restName))
					return type;
			}
			return UNKNOWN;
		}
	};

	public GSRestLayerParser(Element layerElem) {
		this.layerElem = layerElem;
	}

	public String getName() {
		return layerElem.getChildText("name");
	}

	public String getTypeString() {
		return layerElem.getChildText("type");
	}

	public TYPE getType() {
		return TYPE.get(getTypeString());
	}

	public String getDefaultStyle() {
		Element defaultStyle = layerElem.getChild("defaultStyle");
		return defaultStyle == null ? null : defaultStyle.getChildText("name");
	}

	public String getTitle() {
		Element resource = layerElem.getChild("resource");
		return resource.getChildText("title");
	}

	public String getAbstract() {
		Element resource = layerElem.getChild("resource");
		return resource.getChildText("abstract");
	}

	public String getNameSpace() {
		Element resource = layerElem.getChild("resource");
		return resource.getChild("namespace").getChildText("name");
	}

	public String getStoreName() {
		Element resource = layerElem.getChild("resource");
		return resource.getChild("store").getChildText("name");
	}

	public String getStoreType() {
		Element resource = layerElem.getChild("resource");
		return resource.getChild("store").getAttributeValue("class");
	}

	public String getCRS() {
		Element resource = layerElem.getChild("resource");
		Element elBBox = resource.getChild("latLonBoundingBox");
		return elBBox.getChildText("crs");
	}

	protected double getLatLonEdge(String edge) {
		Element resource = layerElem.getChild("resource");
		Element elBBox = resource.getChild("latLonBoundingBox");
		return Double.parseDouble(elBBox.getChildText(edge));
	}

	public double getMinX() {
		return getLatLonEdge("minx");
	}

	public double getMaxX() {
		return getLatLonEdge("maxx");
	}

	public double getMinY() {
		return getLatLonEdge("miny");
	}

	public double getMaxY() {
		return getLatLonEdge("maxy");
	}
}
