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
	<!-- WAVE:04 .................................................................................................. -->
        <rim:ExtrinsicObject
                id="urn:WAVE:04"
                objectType="urn:ogc:def:ebRIM-ObjectType:ElementaryDataset">
                <rim:Slot
                        name="http://purl.org/dc/terms/spatial"
                        slotType="urn:ogc:def:dataType:ISO-19107:2003:GM_Envelope">
                        <wrs:ValueList>
                                <wrs:AnyValue>
                                        <gml:Envelope
                                                srsName="urn:ogc:def:crs:EPSG:6.3:4326">
                                                <gml:lowerCorner>30.1123 -45.1144</gml:lowerCorner>
                                                <gml:upperCorner>91.5629 -24.3457</gml:upperCorner>
                                        </gml:Envelope>
                                </wrs:AnyValue>
                        </wrs:ValueList>
                </rim:Slot>
                <rim:Slot
                        name="http://purl.org/dc/terms/temporal"
                        slotType="urn:ogc:def:dataType:ISO-19108:2002:TM_Instant">
                        <rim:ValueList>
                                <rim:Value>2010-03-22T09:43:29.121</rim:Value>
                        </rim:ValueList>
                </rim:Slot>
        </rim:ExtrinsicObject>
        
        <rim:ExternalLink
                id="urn:WAVE:04:NETCDF:FILE:LINK"
                objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink"
                externalURI="http://server/wave04.nc" />
        <rim:Association
                id="urn:WAVE:04:NETCDF:FILE:EXTERNALLYLINKS:WAVE:04"
                objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association"
                associationType="urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks"
                sourceObject="urn:WAVE:04:NETCDF:FILE:LINK"
                targetObject="urn:WAVE:04" />
 * }</PRE>
 * @author ETj (etj at geo-solutions.it)
 */
public class SarDerivedFeatrueRO {

    private String id;
    private String type;
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

    public double[] getEnvelope() {
        double[] envelope = new double[] {l0, l1, u0, u1};
        
        if(l0 == Double.NaN || l1 == Double.NaN || u0 == Double.NaN || u1 == Double.NaN)
            return null;
        
        return envelope;
    }
    
    public String getTimeStamp() {
        return timeStamp;
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

    public String getURN() {
        return "urn:"+getType()+":"+id+"";
    }

    public String getXML() {

        String base =
            "	<rim:ExtrinsicObject"
            +"		id=\"urn:"+getType()+":"+id+"\""
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
            +"		id=\"urn:"+getType()+":"+id+":NETCDF:FILE:LINK\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink\""
            +"		externalURI=\""+gmlURI+"\" />"
            +"	<rim:Association"
            +"		id=\"urn:"+getType()+":"+id+":NETCDF:FILE:EXTERNALLYLINKS:"+getType()+":"+id+"\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\""
            +"		associationType=\"urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks\""
            +"		sourceObject=\"urn:"+getType()+":"+id+":NETCDF:FILE:LINK\""
            +"		targetObject=\"urn:"+getType()+":"+id+"\" />";

        return base;
    }

    /**
     * Get the XML for the Association vessel/product.
     *
     * Putting the association as a different call because
     * the product may be inserted after the ShipDetection
     * and we may want to register it in a second stage.
     */
    public String getProductAssociationXML(String productID) {
        String xml =
            "	<rim:Association"
            +"		id=\"urn:SAR:PRODUCT:"+productID+":SOURCE:"+getType()+":"+id+"\""
            +"		objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\""
            +"		associationType=\"urn:ogc:def:ebRIM-AssociationType:OGC:Source\""
            +"		sourceObject=\"urn:SAR:PRODUCT:"+productID+"\""
            +"		targetObject=\"urn:"+getType()+":"+id+"\" />";

        return xml;
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

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

}
