/*
 *  Copyright (C) 2007 - 2010 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.egeos.types.util;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import java.util.regex.Pattern;
import org.geotools.geometry.jts.LiteCoordinateSequence;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GmlPolygonParser {

    public final static Namespace NS_GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");

    public static Polygon getValue(Element gmlPolygon) {
        GeometryFactory gf = new GeometryFactory();
        String ordinates = gmlPolygon.getChild("exterior", NS_GML)
                                .getChild("LinearRing", NS_GML)
                                .getChildText("posList", NS_GML);
        double[] doubles = parseRingOrdinates(ordinates);
        LinearRing shell = gf.createLinearRing(new LiteCoordinateSequence(doubles));
        return gf.createPolygon(shell, null);
    }

    protected static double[] parseRingOrdinates(String ordinates) {
        String[] strarr = Pattern.compile("[\\s\\n]+", Pattern.DOTALL).split(ordinates.trim());
        double[] doubles = new double[strarr.length];
        for (int j = 0; j < strarr.length; j++) {
            doubles[j] = Double.parseDouble(strarr[j]);
        }
        // check if the ordinates form a closed ring, if not, fix it
        if(doubles[0] != doubles[doubles.length - 2] || doubles[1] != doubles[doubles.length - 1]) {
            double[] tmp  = new double[doubles.length + 2];
            System.arraycopy(doubles, 0, tmp, 0, doubles.length);
            tmp[doubles.length] = doubles[0];
            tmp[doubles.length + 1] = doubles[1];
            doubles = tmp;
        }
        return doubles;
    }

}
