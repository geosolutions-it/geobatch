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
public class OilSpillRO {

    private String id;
    private boolean envelopeIsSet = false;
    private double l0, l1;
    private double u0, u1;
    private String timeStamp;

    private String gmlBaseURI = "http://BASE_URI_IS_UNSET";
    private String gmlFileName = null;
    private String gmlURI = null;

    private String imgBaseURI = "http://BASE_URI_IS_UNSET";
    private String imgFileName = null;
    private String imgURI = null;


    public void setId(String id) {
        this.id = id;
    }

    public String getURN() {
        return "urn:OILSPILL:"+id;
    }
    
    public void setEnvelope(double l0, double l1, double u0, double u1) {
        envelopeIsSet = true;
        this.l0 = l0;
        this.l1 = l1;
        this.u0 = u0;
        this.u1 = u1;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public double[] getEnvelope() {
        double[] envelope = new double[] {l0, l1, u0, u1};
        
        if(l0 == Double.NaN || l1 == Double.NaN || u0 == Double.NaN || u1 == Double.NaN)
            return null;
        
        return envelope;
    }
    
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setGmlBaseURI(String gmlBaseURI) {
        this.gmlBaseURI = gmlBaseURI;
        setURIs();
    }

    public void setGmlFileName(String gmlFileName) {
        this.gmlFileName = gmlFileName;
        setURIs();
    }

    protected void setURIs() {
        if(gmlBaseURI != null && gmlFileName != null) {
            gmlURI = gmlBaseURI
                    + (gmlBaseURI.endsWith("/") ? "" : "/" )
                    + gmlFileName;
        }

        if(imgBaseURI != null && imgFileName != null) {
            imgURI = imgBaseURI
                    + (imgBaseURI.endsWith("/") ? "" : "/" )
                    + imgFileName;
        }
    }

    public void setImageBaseURI(String imgBaseURI) {
        this.imgBaseURI = imgBaseURI;
        setURIs();
    }

    public void setImageFileName(String imgFileName) {
        this.imgFileName = imgFileName;
        setURIs();
    }



    public String buildXML() {

        String base =
//            "<rim:RegistryObjectList"
//            +"   xmlns:wrs=\"http://www.opengis.net/cat/wrs/1.0\" "
//            +"   xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\" "
//            +"   xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" "
//            +"   xmlns:ows=\"http://www.opengeospatial.net/ows\" "
//            +"   xmlns:gml=\"http://www.opengis.net/gml\"> "
             "<!-- OILSPILL:01 . -->"
            +"	<rim:ExtrinsicObject"
            +"		id=\"urn:OILSPILL:"+id+"\""
            +"		objectType=\"urn:ogc:def:ebRIM-ObjectType:ElementaryDataset\">";

        if(envelopeIsSet) // else we'll have to take it from the EOP
            base +=
            "		<rim:Slot"
            +"			name=\"http://purl.org/dc/terms/spatial\""
            +"			slotType=\"urn:ogc:def:dataType:ISO-19107:2003:GM_Envelope\">"
            +"			<wrs:ValueList>"
            +"				<wrs:AnyValue>"
            +"					<gml:Envelope"
            +"						srsName=\"urn:ogc:def:crs:EPSG:6.3:4326\">"
            +"						<gml:lowerCorner>"+l0+" "+l1+"</gml:lowerCorner>"
            +"						<gml:upperCorner>"+u0+" "+u1+"</gml:upperCorner>"
            +"					</gml:Envelope>"
            +"				</wrs:AnyValue>"
            +"			</wrs:ValueList>"
            +"		</rim:Slot>";

        base +=
            "		<rim:Slot"
            +"			name=\"http://purl.org/dc/terms/temporal\""
            +"			slotType=\"urn:ogc:def:dataType:ISO-19108:2002:TM_Instant\">"
            +"			<rim:ValueList>"
            +"				<rim:Value>"+timeStamp+"</rim:Value>"
            +"			</rim:ValueList>"
            +"		</rim:Slot>"
            +"	</rim:ExtrinsicObject>";

        if(gmlURI != null)
            base +=
            "	<rim:ExternalLink"
            +"		id=\"urn:OILSPILL:"+id+":GML:FILE:LINK\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink\""
            +"		externalURI=\""+gmlURI+"\" />"
            +"	<rim:Association"
            +"		id=\"urn:OILSPILL:"+id+":GML:FILE:EXTERNALLYLINKS:OILSPILL:"+id+"\"" // probably id can be put once only here
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\""
            +"		associationType=\"urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks\""
            +"		sourceObject=\"urn:OILSPILL:"+id+":GML:FILE:LINK\""
            +"		targetObject=\"urn:OILSPILL:"+id+"\" />";


        if(imgURI != null)
            base +=
            "	<rim:ExternalLink"
            +"		id=\"urn:CLIPIMAGE:"+id+":JPG:FILE:LINK\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink\""
            +"		externalURI=\""+imgURI+"\" />"
            +"	<rim:Association"
            +"		id=\"urn:CLIPIMAGE:"+id+":JPG:FILE:EXTERNALLYLINKS:OILSPILL:"+id+"\"" // probably id can be put once only here
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\""
            +"		associationType=\"urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks\""
            +"		sourceObject=\"urn:CLIPIMAGE:"+id+":JPG:FILE:LINK\""
            +"		targetObject=\"urn:OILSPILL:"+id+"\" />";

        return base;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "[id:" + id
                + " (" + (envelopeIsSet?
                            "" + l0 + "," + l1 + ";" + u0 + "," + u1
                                : " no envelope") + ")"
                + " ts:" + timeStamp
                + " gmlURI=" + gmlURI
                + " imgURI=" + imgURI + ']';
    }
}
