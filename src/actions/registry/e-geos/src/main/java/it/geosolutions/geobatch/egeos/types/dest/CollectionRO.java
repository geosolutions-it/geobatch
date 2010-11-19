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

import java.util.LinkedList;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Registry Object for Service.
 * 
 * <PRE>
 * @code
 *         <!-- SAR:DATA:COLLECTION .................................................................................................. -->
 *         <rim:ExtrinsicObject
 *                 id="urn:SAR:DATA:COLLECTION"
 *                 objectType="urn:ogc:def:ebRIM-ObjectType:DatasetCollection">
 *                 <rim:Slot
 *                         name="http://purl.org/dc/terms/spatial"
 *                         slotType="urn:ogc:def:dataType:ISO-19107:2003:GM_Envelope">
 *                         <wrs:ValueList>
 *                                 <wrs:AnyValue>
 *                                         <gml:Envelope
 *                                                 srsName="urn:ogc:def:crs:EPSG:6.3:4326">
 *                                                 <gml:lowerCorner>0.4567 -451144</gml:lowerCorner>
 *                                                 <gml:upperCorner>91.5629 16.8238</gml:upperCorner>
 *                                         </gml:Envelope>
 *                                 </wrs:AnyValue>
 *                         </wrs:ValueList>
 *                 </rim:Slot>
 *                 <rim:Slot
 *                         name="http://purl.org/dc/terms/temporal"
 *                         slotType="urn:ogc:def:dataType:ISO-19108:2002:TM_Instant">
 *                         <rim:ValueList>
 *                                 <rim:Value>2009-05-17T08:33:04.876</rim:Value>
 *                                 <rim:Value>2010-02-19T18:12:05.234</rim:Value>
 *                                 <rim:Value>2010-01-01T02:15:13.381</rim:Value>
 *                                 <rim:Value>2010-03-22T09:43:29.121</rim:Value>
 *                         </rim:ValueList>
 *                 </rim:Slot>
 *         </rim:ExtrinsicObject>
 * 
 *         <!--................WCS-TO-COLLECTION:ASSOCIATIONS.................................................................................................. -->
 *         <rim:Association
 *                 id="urn:WCS:SERVICE:OPERATESON:SAR:DATA:COLLECTION"
 *                 objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association"
 *                 sourceObject="urn:WCS:SERVICE"
 *                 targetObject="urn:SAR:DATA:COLLECTION"
 *                 associationType="urn:ogc:def:ebRIM-AssociationType:OGC:OperatesOn" />
 * }
 * </PRE>
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class CollectionRO {

    private static final String TYPE_NAME = "urn:ogc:def:ebRIM-ObjectType:DatasetCollection";

    private String id;

    private double l0 = Double.NaN, l1 = Double.NaN;

    private double u0 = Double.NaN, u1 = Double.NaN;

    private LinkedList<String> timeStamps = new LinkedList<String>();

    public void setId(String id) {
        this.id = id;
    }

    public double[] getEnvelope() {
        double[] envelope = new double[] { l0, l1, u0, u1 };

        if (l0 == Double.NaN || l1 == Double.NaN || u0 == Double.NaN || u1 == Double.NaN)
            return null;

        return envelope;
    }

    public List<String> getTimeStamps() {
        if (timeStamps == null || timeStamps.size() == 0)
            return null;

        return timeStamps;
    }

    public void setEnvelope(double l0, double l1, double u0, double u1) {
        this.l0 = l0;
        this.l1 = l1;
        this.u0 = u0;
        this.u1 = u1;
    }

    @SuppressWarnings("unchecked")
    public void setTimeStamp(LinkedList<String> timeStamps) {
        this.timeStamps = (LinkedList<String>) timeStamps.clone();
    }

    public String getURN() {
        return "urn:" + id + ":COLLECTION";
    }

    public String getXML() {

        String base = "<rim:ExtrinsicObject " + "   id=\"urn:" + id + ":COLLECTION\" "
                + "   objectType=\"urn:ogc:def:ebRIM-ObjectType:DatasetCollection\"> ";
        if (getEnvelope() != null) {
            base += "   <rim:Slot "
                    + "           name=\"http://purl.org/dc/terms/spatial\" "
                    + "           slotType=\"urn:ogc:def:dataType:ISO-19107:2003:GM_Envelope\"> "
                    + "           <wrs:ValueList> "
                    + "                   <wrs:AnyValue> "
                    + "                           <gml:Envelope "
                    + "                                   srsName=\"urn:ogc:def:crs:EPSG:6.3:4326\"> "
                    + "                                   <gml:lowerCorner>" + l0 + " " + l1
                    + "</gml:lowerCorner> "
                    + "                                   <gml:upperCorner>" + u0 + " " + u1
                    + "</gml:upperCorner> " + "                           </gml:Envelope> "
                    + "                   </wrs:AnyValue> " + "           </wrs:ValueList> "
                    + "   </rim:Slot>";
        }

        if (getTimeStamps() != null) {
            base += "   <rim:Slot " + "           name=\"http://purl.org/dc/terms/temporal\" "
                    + "           slotType=\"urn:ogc:def:dataType:ISO-19108:2002:TM_Instant\"> "
                    + "           <rim:ValueList> ";
            for (String timeStamp : timeStamps) {
                // 2009-05-17T08:33:04.876
                base += "                   <rim:Value>" + timeStamp + "</rim:Value> ";
            }
            base += "           </rim:ValueList>" + "   </rim:Slot> ";
        }

        base += "</rim:ExtrinsicObject>";

        return base;
    }

    /**
     * Get the XML for the Association collection/service.
     * 
     * Putting the association as a different call because the product may be inserted after the
     * ShipDetection and we may want to register it in a second stage.
     */
    public String getServiceAssociationXML(String serviceId) {
        String xml = "<rim:Association "
                + "      id=\"urn:"
                + serviceId
                + ":SERVICE:OPERATESON:"
                + id
                + ":COLLECTION\" "
                + "      objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\" "
                + "      sourceObject=\"urn:" + serviceId + ":SERVICE\" "
                + "      targetObject=\"urn:" + id + ":COLLECTION\" "
                + "      associationType=\"urn:ogc:def:ebRIM-AssociationType:OGC:OperatesOn\" />";

        return xml;
    }

    public String getTypeName() {
        return TYPE_NAME;
    }

    public void update(double[] envelope, String timeStamp) throws NoSuchAuthorityCodeException,
            FactoryException {
        if (timeStamp != null && !timeStamps.contains(timeStamp)) {
            timeStamps.add(timeStamp);
        }

        if (envelope != null) {
            CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
            ReferencedEnvelope originalEnvelope = new ReferencedEnvelope(l0, u0, l1, u1, crs);
            ReferencedEnvelope newEnvelope = new ReferencedEnvelope(envelope[0], envelope[2],
                    envelope[1], envelope[3], crs);

            originalEnvelope.expandToInclude(newEnvelope);

            setEnvelope(originalEnvelope.getLowerCorner().getOrdinate(0), originalEnvelope
                    .getLowerCorner().getOrdinate(1), originalEnvelope.getUpperCorner().getOrdinate(0),
                    originalEnvelope.getUpperCorner().getOrdinate(1));
        }
    }

}
