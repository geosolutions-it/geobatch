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

import it.geosolutions.geobatch.egeos.types.util.JDOMUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class EarthObservation {
    private final static Logger LOGGER = Logger.getLogger(EarthObservation.class);

    public final static Namespace NS = Namespace.getNamespace("http://www.emsa.europa.eu/csndc");
    public final static Namespace NS_CSN = Namespace.getNamespace("csn", "http://www.emsa.europa.eu/csndc");
    public final static Namespace NS_GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
    public final static Namespace NS_EOP = Namespace.getNamespace("eop", "http://earth.esa.int/eop");

    private String id;
    private String gmlFileName;
    private String tsBegin;
    private String tsEnd;
//    private boolean isEnvelopeSet = false;
//    private double x0,y0,x1,y1;
    private int orbitNum;
    private String orbitDir;
    private String acquisitionStation;

    private String equipPlatformName;
    private String equipInstrumentName;
    private String equipSensorType;
    private Double equipSensorResolution; // TODO: should we harmonize UOM?

    private List<String> footprintPosList;

    private static final XPath XP_ID = buildXPath("/eop:EarthObservation/gml:metaDataProperty/eop:EarthObservationMetaData/eop:identifier");

    private static final XPath XP_ACQ_STAT  = buildXPath("/eop:EarthObservation/gml:metaDataProperty/eop:EarthObservationMetaData/eop:downlinkedTo/eop:DownlinkInformation/eop:acquisitionStation");

    private static final XPath XP_ORBIT_NUM = buildXPath("/eop:EarthObservation/gml:using/eop:EarthObservationEquipment/eop:acquisitionParameters/eop:Acquisition/eop:orbitNumber");
    private static final XPath XP_ORBIT_DIR = buildXPath("/eop:EarthObservation/gml:using/eop:EarthObservationEquipment/eop:acquisitionParameters/eop:Acquisition/eop:orbitDirection");
    private static final XPath XP_BEG_POS   = buildXPath("/eop:EarthObservation/gml:validTime/gml:TimePeriod/gml:beginPosition");
    private static final XPath XP_END_POS   = buildXPath("/eop:EarthObservation/gml:validTime/gml:TimePeriod/gml:endPosition");

    private static final XPath XP_EQP_PLAT  = buildXPath("/eop:EarthObservation/gml:using/eop:EarthObservationEquipment/eop:platform/eop:Platform/eop:shortName");
    private static final XPath XP_EQP_INST  = buildXPath("/eop:EarthObservation/gml:using/eop:EarthObservationEquipment/eop:instrument/eop:Instrument/eop:shortName");
    private static final XPath XP_EQP_STYPE = buildXPath("/eop:EarthObservation/gml:using/eop:EarthObservationEquipment/eop:sensor/eop:Sensor/eop:sensorType");
    private static final XPath XP_EQP_SRES  = buildXPath("/eop:EarthObservation/gml:using/eop:EarthObservationEquipment/eop:sensor/eop:Sensor/eop:resolution");

    private static final XPath XP_FOOTPRINT = buildXPath("/eop:EarthObservation/gml:target/eop:Footprint/gml:multiExtentOf/gml:MultiSurface/gml:surfaceMembers/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList");

    private static XPath buildXPath(String xpath) {
        try {
            XPath ret = XPath.newInstance(xpath);
            ret.addNamespace(NS_EOP);
            ret.addNamespace(NS_GML);
            return ret;
        } catch (JDOMException ex) {
            LOGGER.error("Error creating XPath", ex);
            return null;
        }
    }

    private EarthObservation() {
    }

    public static EarthObservation build(File srcFile) throws JDOMException, IOException  {

        Document doc = new SAXBuilder().build(srcFile);
        Element root = doc.getRootElement();

        EarthObservation ret = new EarthObservation();
        ret.id = JDOMUtils.getString(root, XP_ID);
        ret.setGmlFileName(srcFile.getName());
        
        ret.orbitNum = JDOMUtils.getInt(root, XP_ORBIT_NUM);
        ret.orbitDir = JDOMUtils.getString(root, XP_ORBIT_DIR);
        ret.tsBegin = JDOMUtils.getString(root, XP_BEG_POS);
        ret.tsEnd = JDOMUtils.getString(root, XP_END_POS);
        ret.acquisitionStation = JDOMUtils.getString(root, XP_ACQ_STAT);

        ret.equipPlatformName = JDOMUtils.getString(root, XP_EQP_PLAT);
        ret.equipInstrumentName = JDOMUtils.getString(root, XP_EQP_INST);
        ret.equipSensorType = JDOMUtils.getString(root, XP_EQP_STYPE);
        ret.equipSensorResolution = JDOMUtils.getDouble(root, XP_EQP_SRES);

        String sposlist = JDOMUtils.getString(root, XP_FOOTPRINT);
        LOGGER.info("sposlist: " + sposlist);
        ret.footprintPosList = Arrays.asList(sposlist.split(" "));

        
//        ret. = JDOMUtils.getString(root, );
//        ret. = JDOMUtils.getString(root, );
//        ret. = JDOMUtils.getString(root, );



//eop:EarthObservation/gml:validTime/gml:TimePeriod/gml:beginPosition
//eop:EarthObservation/gml:validTime/gml:TimePeriod/gml:endPosition
//
//
//eop:EarthObservation/gml:using/eop:EarthObservationEquipment/eop:platform/eop:Platform/eop:shortName
//
//
//        String ts = root.getChildText("timeStamp", NS);
//        String iid = root.getChildText("imageIdentifier", NS);
//
//        EarthObservation ret = new EarthObservation(id, ts, iid);
//        ret.id = id;
//        ret.timestamp = ts;
//        ret.imageIdentifier = iid;
//
//        Element gmlpoly = root.getChild("geometry", NS).getChild("Polygon", NS_GML);
//        if(gmlpoly != null) {
//            Polygon poly = GmlPolygonParser.getValue(gmlpoly);
//            Geometry penv = poly.getEnvelope();
//            ret.isEnvelopeSet = true;
//            ret.x0 = penv.getCoordinates()[0].x;
//            ret.y0 = penv.getCoordinates()[0].y;
//            ret.x1 = penv.getCoordinates()[2].x;
//            ret.y1 = penv.getCoordinates()[2].y;
//        }
//
        return ret;
    }


    public String getId() {
        return id;
    }

    public String getAcquisitionStation() {
        return acquisitionStation;
    }

    public String getEquipInstrumentName() {
        return equipInstrumentName;
    }

    public String getEquipPlatformName() {
        return equipPlatformName;
    }

    public Double getEquipSensorResolution() {
        return equipSensorResolution;
    }

    public String getEquipSensorType() {
        return equipSensorType;
    }

    public String getOrbitDir() {
        return orbitDir;
    }

    public int getOrbitNum() {
        return orbitNum;
    }

    public String getTsBegin() {
        return tsBegin;
    }

    public String getTsEnd() {
        return tsEnd;
    }

    public List<String> getFootprintPosList() {
        return footprintPosList;
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
}
