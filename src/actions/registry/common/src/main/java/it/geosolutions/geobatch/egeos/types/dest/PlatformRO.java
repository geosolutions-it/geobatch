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
 * Registry Object for Platform.
 *
 * <PRE>{@code
	<!-- SAR:PLATFORM:ENVISAT1 .................................................................................................. -->
	<wrs:ExtrinsicObject
		id="urn:SAR:PLATFORM:ENVISAT1"
		objectType="urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOAcquisitionPlatform">
		<rim:Slot
			name="urn:ogc:def:ebRIM-Slot:OGC-06-131:instrumentShortName"
			slotType="urn:oasis:names:tc:ebxml-regrep:DataType:String">
			<rim:ValueList>
				<rim:Value>ENV1</rim:Value>
			</rim:ValueList>
		</rim:Slot>
		<rim:Slot
			name="urn:ogc:def:ebRIM-Slot:OGC-06-131:sensorType"
			slotType="urn:oasis:names:tc:ebxml-regrep:DataType:String">
			<rim:ValueList>
				<rim:Value>ASAR</rim:Value>
			</rim:ValueList>
		</rim:Slot>
		<rim:Slot
			name="urn:ogc:def:ebRIM-Slot:OGC-06-131:sensorResolution"
			slotType="urn:oasis:names:tc:ebxml-regrep:DataType:Double">
			<rim:ValueList>
				<rim:Value>75.0</rim:Value>
			</rim:ValueList>
		</rim:Slot>
	</wrs:ExtrinsicObject>

 * }</PRE>
 * @author ETj (etj at geo-solutions.it)
 */
public class PlatformRO {
    private String id;
    private String instrShortName;
    private String sensorType;
    private Double sensorResolution;

    public void setId(String id) {
        this.id = id;
    }

    public void setInstrShorName(String instrShorName) {
        this.instrShortName = instrShorName;
    }

    public void setSensorResolution(Double resolution) {
        this.sensorResolution = resolution;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getURN() {
        return "urn:SAR:PLATFORM:"+id;
    }

    public String getXML() {
        String xml = 
        "	<wrs:ExtrinsicObject"
        +"		id=\"urn:SAR:PLATFORM:"+id+"\""
        +"		objectType=\"urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOAcquisitionPlatform\">"
        +"		<rim:Slot"
        +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:instrumentShortName\""
        +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:String\">"
        +"			<rim:ValueList>"
        +"				<rim:Value>"+instrShortName+"</rim:Value>"
        +"			</rim:ValueList>"
        +"		</rim:Slot>"
        +"		<rim:Slot"
        +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:sensorType\""
        +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:String\">"
        +"			<rim:ValueList>"
        +"				<rim:Value>"+sensorType+"</rim:Value>"
        +"			</rim:ValueList>"
        +"		</rim:Slot>"
        +"		<rim:Slot"
        +"			name=\"urn:ogc:def:ebRIM-Slot:OGC-06-131:sensorResolution\""
        +"			slotType=\"urn:oasis:names:tc:ebxml-regrep:DataType:Double\">"
        +"			<rim:ValueList>"
        +"				<rim:Value>"+sensorResolution+"</rim:Value>"
        +"			</rim:ValueList>"
        +"		</rim:Slot>"
        +"	</wrs:ExtrinsicObject>";

        return encapsulate(xml);
    }

    protected String encapsulate(String xml) {
        return
            "<rim:RegistryObjectList xmlns:wrs=\"http://www.opengis.net/cat/wrs/1.0\""
            +"	xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\""
            +"	xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\""
            +"	xmlns:ows=\"http://www.opengeospatial.net/ows\""
            +"	xmlns:gml=\"http://www.opengis.net/gml\"> "
            + xml
            + "</rim:RegistryObjectList>";
    }

}
