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

package it.geosolutions.geobatch.egeos.types.dest;

import java.util.List;
import org.apache.log4j.Logger;

/**
 * Registry Object for OilSpill.
 *
 * <PRE>{@code
<rim:ExtrinsicObject
    id="urn:OILSPILL:01"
    objectType="urn:ogc:def:ebRIM-ObjectType:ElementaryDataset">
    <rim:Slot
        name="http://purl.org/dc/terms/spatial"
        slotType="urn:ogc:def:dataType:ISO-19107:2003:GM_Envelope">
        <wrs:ValueList>
            <wrs:AnyValue>
                <gml:Envelope
                    srsName="urn:ogc:def:crs:EPSG:6.3:4326">
                    <gml:lowerCorner>15.4273 1.9385</gml:lowerCorner>
                    <gml:upperCorner>16.1514 2.0544</gml:upperCorner>
                </gml:Envelope>
            </wrs:AnyValue>
        </wrs:ValueList>
    </rim:Slot>
    <rim:Slot
        name="http://purl.org/dc/terms/temporal"
        slotType="urn:ogc:def:dataType:ISO-19108:2002:TM_Instant">
        <rim:ValueList>
            <rim:Value>2009-05-17T08:33:04.876</rim:Value>
        </rim:ValueList>
    </rim:Slot>
</rim:ExtrinsicObject>
 *
<rim:ExternalLink
    id="urn:OILSPILL:01:GML:FILE:LINK"
    objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink"
    externalURI="http://server/oilspill01.gml" />
 *
<rim:Association
    id="urn:OILSPILL:01:GML:FILE:EXTERNALLYLINKS:OILSPILL:01"
    objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association"
    associationType="urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks"
    sourceObject="urn:OILSPILL:01:GML:FILE:LINK"
    targetObject="urn:OILSPILL:01" />
 *
<rim:ExternalLink
    id="urn:CLIPIMAGE:01:JPG:FILE:LINK"
    objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink"
    externalURI="http://server/clipimage01.jpg" />
 *
<rim:Association
    id="urn:CLIPIMAGE:01:JPG:FILE:EXTERNALLYLINKS:OILSPILL:01"
    objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association"
    associationType="urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks"
    sourceObject="urn:CLIPIMAGE:01:JPG:FILE:LINK"
    targetObject="urn:OILSPILL:01" />
 * }</PRE>
 * @author ETj (etj at geo-solutions.it)
 */
public class SARProductRO {
    private final static Logger LOGGER = Logger.getLogger(SARProductRO.class);

    private String id;

    private String platformId;

    private String gmlBaseURI = "http://BASE_URI_IS_UNSET";
    private String gmlFileName = null;
    private String gmlURI = null;

    private Integer orbitNumber;
    private String  orbitDirection;
    private String  beginTimePosition;
    private String  endTimePosition;
    private String  acquisitionStation;

    private List<String> footprintListPos;

    public void setId(String id) {
        this.id = id;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public void setGmlBaseURI(String gmlBaseURI) {
        this.gmlBaseURI = gmlBaseURI;
        setGmlURI();
    }

    public void setGmlFileName(String gmlFileName) {
        this.gmlFileName = gmlFileName;
        setGmlURI();
    }

    protected void setGmlURI() {
        if(gmlBaseURI != null && gmlFileName != null) {
            gmlURI = gmlBaseURI
                    + (gmlBaseURI.endsWith("/") ? "" : "/" )
                    + gmlFileName;
        }
    }

    public void setAcquisitionStation(String acquisitionStation) {
        this.acquisitionStation = acquisitionStation;
    }

    public void setBeginTimePosition(String beginTimePosition) {
        this.beginTimePosition = beginTimePosition;
    }

    public String getBeginTimePosition() {
        return this.beginTimePosition;
    }
    
    public void setEndTimePosition(String endTimePosition) {
        this.endTimePosition = endTimePosition;
    }

    public String getEndTimePosition() {
        return this.endTimePosition;
    }
    
    public void setOrbitDirection(String orbitDirection) {
        this.orbitDirection = orbitDirection;
    }

    public void setOrbitNumber(Integer orbitNumber) {
        this.orbitNumber = orbitNumber;
    }

    public void setFootprintListPos(List<String> footprintListPos) {
        this.footprintListPos = footprintListPos;
    }

    public double[] getEnvelope() {
        double[] bbox = new double[4];
        
        bbox[0] = Double.POSITIVE_INFINITY;
        bbox[2] = Double.NEGATIVE_INFINITY;
        bbox[1] = Double.POSITIVE_INFINITY;
        bbox[3] = Double.NEGATIVE_INFINITY;
        
        for (int i = 0; i < footprintListPos.size(); i+=2) {
            double p1 = Double.parseDouble(footprintListPos.get(i));
            double p2 = Double.parseDouble(footprintListPos.get(i+1));
            
            bbox[0] = bbox[0] > p1 ? p1 : bbox[0];
            bbox[1] = bbox[1] > p2 ? p2 : bbox[1];
            
            bbox[2] = bbox[2] < p1 ? p1 : bbox[2];
            bbox[3] = bbox[3] < p2 ? p2 : bbox[3];
            
        }

        if (bbox[0] == Double.POSITIVE_INFINITY ||
            bbox[2] == Double.NEGATIVE_INFINITY ||
            bbox[1] == Double.POSITIVE_INFINITY ||
            bbox[3] == Double.NEGATIVE_INFINITY ) {
            return null;
        }
        
        return bbox;
    }
    
    public static String getURN(String id) {
        return "urn:SAR:PRODUCT:"+id;
    }

    public String getURN() {
        return getURN(id);
    }

    public String getXML() {

        String xml =
            "	<wrs:ExtrinsicObject"
            +"		id=\"urn:SAR:PRODUCT:"+id+"\""
            +"		objectType=\"urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOProduct\">"

            +"		<rim:Classification"
            +"			id=\"urn:SAR:PRODUCT:"+id+":CLASSIFICATION\""
            +"			objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification\""
            +"			classifiedObject=\"urn:SAR:PRODUCT:"+id+"\""
            +"			classificationNode=\"urn:x-ogc:specification:csw-ebrim:EO:EOProductTypes:SAR\" />"

            +"		<rim:ExternalIdentifier"
            +"			id=\"urn:SAR:PRODUCT:"+id+":EXTERNALIDENTIFIER\""
            +"			objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier\""
            +"			identificationScheme=\"CleanSeaNet\""
            +"			registryObject=\"urn:SAR:PRODUCT:"+id+"\""
            +"			value=\""+id+"\" />"

            +"		<rim:Slot"
            +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:orbitNumber\""
            +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:Integer\">"
            +"			<rim:ValueList>"
            +"				<rim:Value>"+orbitNumber+"</rim:Value>"
            +"			</rim:ValueList>"
            +"		</rim:Slot>"

            +"		<rim:Slot"
            +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:swathId\""
            +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:String\">"
            +"			<rim:ValueList>"
            +"				<rim:Value>WIDE SWATH</rim:Value>"
            +"			</rim:ValueList>"
            +"		</rim:Slot>"
            +"		<rim:Slot"
            +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:orbitDirection\""
            +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:String\">"
            +"			<rim:ValueList>"
            +"				<rim:Value>"+orbitDirection+"</rim:Value>"
            +"			</rim:ValueList>"
            +"		</rim:Slot>"

            +"		<rim:Slot"
            +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:acquisitionStation\""
            +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:String\">"
            +"			<rim:ValueList>"
            +"				<rim:Value>"+acquisitionStation+"</rim:Value>"
            +"			</rim:ValueList>"
            +"		</rim:Slot>"

            +"		<rim:Slot"
            +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:beginPosition\""
            +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:DateTime\">"
            +"			<rim:ValueList>"
            +"				<rim:Value>"+beginTimePosition+"</rim:Value>"
            +"			</rim:ValueList>"
            +"		</rim:Slot>"

            +"		<rim:Slot"
            +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:endPosition\""
            +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:DateTime\">"
            +"			<rim:ValueList>"
            +"				<rim:Value>"+endTimePosition+"</rim:Value>"
            +"			</rim:ValueList>"
            +"		</rim:Slot>"

            +"		<rim:Slot"
            +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:multiExtentOf\""
            +"			slotType=\"urn:ogc:def:dataType:ISO-19107:2003:GM_Polygon\">"
            +"			<wrs:ValueList>"
            +"				<wrs:AnyValue>"
            +"					<gml:Polygon"
            +"						srsName=\"urn:ogc:def:crs:EPSG:6.3:4326\">"
            +"						<gml:exterior>"
            +"							<gml:LinearRing>";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < footprintListPos.size(); i+=2) {
            String p1 = footprintListPos.get(i);
            String p2 = footprintListPos.get(i+1);
            sb.append("<gml:pos>").append(p1).append(' ').append(p2).append("</gml:pos>");
        }

        LOGGER.info("Source pos list > " + footprintListPos);
        LOGGER.info("Creating LinearRing \n" + sb);

        xml += sb.toString();
        xml +=
             "							</gml:LinearRing>"
            +"						</gml:exterior>"
            +"					</gml:Polygon>"
            +"				</wrs:AnyValue>"
            +"			</wrs:ValueList>"
            +"		</rim:Slot>"
            +"	</wrs:ExtrinsicObject>";

        if(gmlURI != null) {
            xml +=
             "  <rim:ExternalLink"
            +"		id=\"urn:SAR:PRODUCT:"+id+":GML:FILE:LINK\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink\""
            +"		externalURI=\""+gmlURI+"\" />"
            +"	<rim:Association"
            +"		id=\"urn:SAR:PRODUCT:"+id+":GML:FILE:LINK:SOURCE:SAR:PRODUCT:"+id+"\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\""
            +"		associationType=\"urn:ogc:def:ebRIM-AssociationType:OGC:Source\""
            +"		sourceObject=\"urn:SAR:PRODUCT:"+id+":GML:FILE:LINK\""
            +"		targetObject=\"urn:SAR:PRODUCT:"+id+"\" />";
        }

        xml +=
             "	<rim:Association"
            +"		id=\"urn:SAR:PRODUCT:"+id+":ACQUIREDBY:SAR:PLATFORM:"+platformId+"\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\""
            +"		associationType=\"urn:x-ogc:specification:csw-ebrim:AssociationType:EO:AcquiredBy\""
            +"		sourceObject=\"urn:SAR:PRODUCT:"+id+"\""
            +"		targetObject=\"urn:SAR:PLATFORM:"+platformId+"\" />";

        // USE THIS PIECE OF CODE IF YOU WANT TO CREATE THE REGISTRY OBJECTS USING JAVA BEANS
//
//        AssociationType prodplatass = OFactory.rim.createAssociationType1();
//        prodplatass.setId("urn:SAR:PRODUCT:"+id+":ACQUIREDBY:SAR:PLATFORM:"+platformId);
//        prodplatass.setSourceObject("urn:SAR:PRODUCT:"+id);
//        prodplatass.setTargetObject("urn:SAR:PLATFORM:"+platformId);
//        prodplatass.setAssociationType("urn:x-ogc:specification:csw-ebrim:AssociationType:EO:AcquiredBy");
//
//        RegistryObjectListType registryObjectList = OFactory.rim.createRegistryObjectListType();
//        registryObjectList.getIdentifiable().add(OFactory.rim.createAssociation(prodplatass));
//
        return xml;
    }

//    protected String encapsulate(String xml) {
//        return
//            "<rim:RegistryObjectList xmlns:wrs=\"http://www.opengis.net/cat/wrs/1.0\""
//            +"	xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\""
//            +"	xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\""
//            +"	xmlns:ows=\"http://www.opengeospatial.net/ows\""
//            +"	xmlns:gml=\"http://www.opengis.net/gml\"> "
//            + xml
//            + "</rim:RegistryObjectList>";
//    }

}
