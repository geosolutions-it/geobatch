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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import it.geosolutions.geobatch.egeos.types.util.GmlPolygonParser;
import it.geosolutions.geobatch.egeos.types.util.JDOMUtils;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * Used to parse both OSN and OSW.
 * 
 * <LI>IDs are only known by the parent PCK</LI>
 * <LI>OSWs do not contain the gml:Polygon information</LI>
 * </UL>
 *
 * <H3>OIL SPILL WARNING </H3>
 * <PRE>{@code
<OilSpill
   xmlns="http://www.emsa.europa.eu/csndc"
   xmlns:gml="http://www.opengis.net/gml"
   xmlns:ows="http://www.opengis.net/ows/1.1"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://www.emsa.europa.eu/csndc ../XSD/csndc_os.xsd">
 
      <eventid>1</eventid>
      <origin>DETECTED</origin>
      <center><gml:pos></gml:pos></center>
      <geometry></geometry>
      <timeStamp>2010-05-27T05:56:13</timeStamp>
      <imageIdentifier type="SAR">7922_RS2_20100527_055613_0045_SCNA_VV_SCN_83076_0000_0000000</imageIdentifier>
      <auxiliaryDataRef>
        <auxiliaryData>
          <dataKey>OSW_JPEG_1</dataKey>
          <dataReference>C:/PMMT/PM/PF/WORK/TEMAS/AK_108.24.1/DATAOUT/TEMASRPT//CSN2_26/\RS2_20100527_055613_0045_SCNA_VV_SCN_83076_0000_0000000_2010-10-14_15-35-13_OSW_1.jpg</dataReference>
       </auxiliaryData>
    </auxiliaryDataRef>
</OilSpill>
 * }</PRE>
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class OilSpill {
    private final static Logger LOGGER = Logger.getLogger(OilSpill.class);

    public final static Namespace NS = Namespace.getNamespace("http://www.emsa.europa.eu/csndc");
    public final static Namespace NS_CSN = Namespace.getNamespace("csn", "http://www.emsa.europa.eu/csndc");
    public final static Namespace NS_GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");

    private static final XPath XP_IMG_NAME = buildXPath("OilSpill/auxiliaryDataRef/auxiliaryData/dataReference");

    private static XPath buildXPath(String xpath) {
        try {
            XPath ret = XPath.newInstance(xpath);
            ret.addNamespace(NS);
            return ret;
        } catch (JDOMException ex) {
            LOGGER.error("Error creating XPath", ex);
            return null;
        }
    }

    private String id;
    private String timestamp;
    private String imageIdentifier;
    private boolean isEnvelopeSet = false;
    private double x0,y0,x1,y1;
    private String refImageFileName = null;

    private OilSpill(String id, String timestamp, String imageIdentifier) {
        this.id = id;
        this.timestamp = timestamp;
        this.imageIdentifier = imageIdentifier;
    }

    public static OilSpill build(String id, File srcFile) throws JDOMException, IOException  {

        Document doc = new SAXBuilder().build(srcFile);
        Element root = doc.getRootElement();

        String ts = root.getChildText("timeStamp", NS);
        String iid = root.getChildText("imageIdentifier", NS);

        OilSpill ret = new OilSpill(id, ts, iid);
        ret.id = id;
        ret.timestamp = ts;
        ret.imageIdentifier = iid;

        Element gmlpoly = root.getChild("geometry", NS).getChild("Polygon", NS_GML);
        if(gmlpoly != null) {
            Polygon poly = GmlPolygonParser.getValue(gmlpoly);
            Geometry penv = poly.getEnvelope();
            ret.isEnvelopeSet = true;
            ret.x0 = penv.getCoordinates()[0].x;
            ret.y0 = penv.getCoordinates()[0].y;
            ret.x1 = penv.getCoordinates()[2].x;
            ret.y1 = penv.getCoordinates()[2].y;
        }

        ret.refImageFileName = JDOMUtils.getString(root, XP_IMG_NAME);
        
        return ret;
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

    public boolean isEnvelopeSet() {
        return isEnvelopeSet;
    }

    public double getX0() {
        return x0;
    }

    public double getX1() {
        return x1;
    }

    public double getY0() {
        return y0;
    }

    public double getY1() {
        return y1;
    }

    public String getRefImageFileName() {
        return refImageFileName;
    }

    public String toString() {
        return getClass().getSimpleName()+"["+id+":"+imageIdentifier+"@"+timestamp+"]";
    }
}
