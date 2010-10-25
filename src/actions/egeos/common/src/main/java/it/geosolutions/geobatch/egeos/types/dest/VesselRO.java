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
 * Registry Object for Vessel.
 *
 * <PRE>{@code
	<!-- VESSEL:01 -->
	<rim:ExtrinsicObject
		id="urn:VESSEL:01"
		objectType="urn:ogc:def:ebRIM-ObjectType:ElementaryDataset">
		<rim:Slot
			name="http://purl.org/dc/terms/spatial"
			slotType="urn:ogc:def:dataType:ISO-19107:2003:GM_Envelope">
			<wrs:ValueList>
				<wrs:AnyValue>
					<gml:Envelope
						srsName="urn:ogc:def:crs:EPSG:6.3:4326">
						<gml:lowerCorner>13.4273 2.3385</gml:lowerCorner>
						<gml:upperCorner>13.7514 2.9544</gml:upperCorner>
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

	<rim:ExternalLink
		id="urn:VESSEL:01:GML:FILE:LINK"
		objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink"
		externalURI="http://server/vessel01.gml" />
	<rim:Association
		id="urn:VESSEL:01:GML:FILE:EXTERNALLYLINKS:VESSEL:01"
		objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association"
		associationType="urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks"
		sourceObject="urn:VESSEL:01:GML:FILE:LINK"
		targetObject="urn:VESSEL:01" />
 * }</PRE>
 * @author ETj (etj at geo-solutions.it)
 */
public class VesselRO {

    private String id;
    private double l0, l1;
    private double u0, u1;
    private String timeStamp;
    
    private String gmlBaseURI = "http://BASE_URI_IS_UNSET";
    private String gmlFileName = null;
    private String gmlURI = null;

    public void setId(String id) {
        this.id = id;
    }

    public void setEnvelope(double l0, double l1, double u0, double u1) {
        this.l0 = l0;
        this.l1 = l1;
        this.u0 = u0;
        this.u1 = u1;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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

    private String buildXML() {

        String base =
            "<rim:RegistryObjectList"
            +"   xmlns:wrs=\"http://www.opengis.net/cat/wrs/1.0\" "
            +"   xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\" "
            +"   xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" "
            +"   xmlns:ows=\"http://www.opengeospatial.net/ows\" "
            +"   xmlns:gml=\"http://www.opengis.net/gml\"> "
                
            +"	<rim:ExtrinsicObject"
            +"		id=\"urn:VESSEL:"+id+"\""
            +"		objectType=\"urn:ogc:def:ebRIM-ObjectType:ElementaryDataset\">"

            +"		<rim:Slot"
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
            +"		</rim:Slot>"

            +"		<rim:Slot"
            +"			name=\"http://purl.org/dc/terms/temporal\""
            +"			slotType=\"urn:ogc:def:dataType:ISO-19108:2002:TM_Instant\">"
            +"			<rim:ValueList>"
            +"				<rim:Value>"+timeStamp+"</rim:Value>"
            +"			</rim:ValueList>"
            +"		</rim:Slot>"
            +"</rim:ExtrinsicObject>";

        if(gmlURI != null)
            base +=
             "  <rim:ExternalLink"
            +"		id=\"urn:VESSEL:"+id+":GML:FILE:LINK\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink\""
            +"		externalURI=\""+gmlURI+"\" />"
            +"	<rim:Association"
            +"		id=\"urn:VESSEL:"+id+":GML:FILE:EXTERNALLYLINKS:VESSEL:"+id+"\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\""
            +"		associationType=\"urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks\""
            +"		sourceObject=\"urn:VESSEL:"+id+":GML:FILE:LINK\""
            +"		targetObject=\"urn:VESSEL:"+id+"\" />";

        base += "</rim:ExtrinsicObject>";

        return base;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "["
                + id
                + "(" + l0 + "," + l1 + ";" + u0 + "," + u1 + ")"
                + " ts:" + timeStamp
                + " gmlURI=" + gmlURI
                + ']';
    }

}
