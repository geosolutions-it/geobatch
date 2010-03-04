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

import java.util.List;
import org.jdom.Element;

/**
 * Parses fields of a Datastore got via a
 * {@link it.geosolutions.geobatch.geoserver.rest.GeoserverRESTReader}.
 *
 * @author etj
 */
public class GSRestDatastoreParser {
	private final Element dsElem;

	public enum DBType {
		POSTGIS("postgis"),
		SHP("shp"),
		UNKNOWN(null);

		private final String restName;

		private DBType(String restName) {
			this.restName = restName;
		}

		public static DBType get(String restName) {
			for (DBType type : values()) {
				if(type == UNKNOWN)
					continue;
				if(type.restName.equals(restName))
					return type;
			}
			return UNKNOWN;
		}
	};


	public GSRestDatastoreParser(Element dsElem) {
		this.dsElem = dsElem;
	}

	public String getName() {
		return dsElem.getChildText("name");
	}

	public String getWorkSpace() {
		Element resource = dsElem.getChild("resource");
		return resource.getChild("workspace").getChildText("name");
	}

	protected String getConnectionParameter(String paramName) {
		Element elConnparm = dsElem.getChild("connectionParameters");
		if(elConnparm!=null) {
			for (Element entry : (List<Element>)elConnparm.getChildren("entry")) {
				String key = entry.getAttributeValue("key");
				if(paramName.equals(key)) {
					return entry.getTextTrim();
				}
			}
		}

		return null;
	}

	public DBType getType() {
		return DBType.get(getConnectionParameter("dbtype"));
	}
}
