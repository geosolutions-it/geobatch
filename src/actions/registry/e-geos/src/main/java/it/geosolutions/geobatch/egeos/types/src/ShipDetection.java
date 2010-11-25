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

package it.geosolutions.geobatch.egeos.types.src;

import java.io.File;
import java.io.IOException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * <PRE>{@code
<Ship
     xmlns="http://www.emsa.europa.eu/csndc"
     xmlns:gml="http://www.opengis.net/gml"
     xmlns:ows="http://www.opengis.net/ows/1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.emsa.europa.eu/csndc ../XSD/csndc_ds.xsd">
 * 
      <id>7923_RS2_20100531_170041_0046_SCNA_HH_SCN_83688_0000_0000000_DS_27</id>
      <includeInReport>false</includeInReport>
      <gml:pos>39.166700 13.901900</gml:pos>
      <timeStamp>2010-05-31T17:00:49</timeStamp>
      <heading>999</heading>
      <speed>-999.900000</speed>
      <length>113.667000</length>
      <lengthError>999.990000</lengthError>
      <width>103.187000</width>
      <widthError>999.990000</widthError>
      <confidenceLevel>17.127000</confidenceLevel>
      <imageIdentifier type="SAR">7923_RS2_20100531_170041_0046_SCNA_HH_SCN_83688_0000_0000000</imageIdentifier>
</Ship>
 * }</PRE>
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class ShipDetection {

    public final static Namespace NS = Namespace.getNamespace("http://www.emsa.europa.eu/csndc");
    public final static Namespace NS_GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");

    private final String id;
    private final String timestamp;
    private final String imageIdentifier;
    private final double x0,x1;
    private String gmlFileName;

    public ShipDetection(String id, String timestamp, String imageIdentifier, Double x0, Double x1) {
        this.id = id;
        this.timestamp = timestamp;
        this.imageIdentifier = imageIdentifier;
        this.x0 = x0;
        this.x1 = x1;
    }

    public static ShipDetection build(File srcFile) throws JDOMException, IOException  {

        Document doc = new SAXBuilder().build(srcFile);
        Element root = doc.getRootElement();

        String id = root.getChildText("id", NS);
        String ts = root.getChildText("timeStamp", NS);
        String iid = root.getChildText("imageIdentifier", NS);

        String gmlpos = root.getChildText("pos", NS_GML);
        String[] xy = gmlpos.split("\\s+");
        Double x0 = new Double(xy[0]);
        Double x1 = new Double(xy[1]);
        ShipDetection sd = new ShipDetection(id, ts, iid, x0, x1);
        sd.setGmlFileName(srcFile.getName());
        //        Point point = new GeometryFactory().createPoint(new Coordinate(
//                Double.parseDouble(xy[0]),
//                Double.parseDouble(xy[1])));
        return sd;
    }

    public String getTimestamp() {
        return timestamp;
    }
    
    public String getImageIdentifier() {
        return imageIdentifier;
    }

    public String getId() {
        return id;
    }

    public double getX0() {
        return x0;
    }

    public double getX1() {
        return x1;
    }

    /**
     * @param gmlFileName the gmlFileName to set
     */
    public void setGmlFileName(String gmlFileName) {
        this.gmlFileName = gmlFileName;
    }

    /**
     * @return the gmlFileName
     */
    public String getGmlFileName() {
        return gmlFileName;
    }
    
    public String toString() {
        return getClass().getSimpleName()+"["+id+":"+imageIdentifier+"@"+timestamp+"("+x0+","+x1+")]";
    }
}
