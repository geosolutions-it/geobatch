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

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Registry Object for Service.
 *
 * <PRE>{@code
        <!--................WFS .................................................................................................. -->
        <rim:Service
                id="urn:WFS:SERVICE"
                objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Service">
                <rim:Classification
                        id="urn:WFS:SERVICE:CLASSIFICATION"
                        classifiedObject="urn:WFS:SERVICE"
                        classificationNode="urn:ogc:def:ebRIM-ClassificationScheme:ISO-19119:2005:Services:FeatureAccess" />
        </rim:Service>
        *
        <wrs:ExtrinsicObject
                id="urn:WFS:SERVICE:PROFILE"
                objectType="urn:ogc:def:ebRIM-ObjectType:OGC:ServiceProfile" />
        <rim:Association
                id="urn:WFS:SERVICE:PRESENTS:WFS:PROFILE"
                objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association"
                sourceObject="urn:WFS:SERVICE"
                targetObject="urn:WFS:SERVICE:PROFILE"
                associationType="urn:ogc:def:ebRIM-AssociationType:OGC:Presents" />
        *
        <rim:ExternalLink
                id="urn:WFS:SERVICE:CAPABILITIES:FILE:LINK"
                objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink"
                externalURI="http://geoserver/wfs?SERVICE=wfs&amp;VERSION=1.1.0&amp;REQUEST=GetCapabilities" />
        <rim:Association
                id="urn:WFS:SERVICE:CAPABILITIES:FILE:LINK:SOURCE:WFS:SERVICE:PROFILE"
                objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association"
                associationType="urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks"
                sourceObject="urn:WFS:SERVICE:CAPABILITIES:FILE:LINK"
                targetObject="urn:WFS:SERVICE:PROFILE" />
 * }</PRE>
 * @author ETj (etj at geo-solutions.it)
 */
public class ServiceRO {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceRO.class);
    
    private String id;
    private String getCapURL = null;

    public void setId(String id) {
        this.id = id;
    }

    public void setGetCapURL(String getCapURL) {
        this.getCapURL = getCapURL;
    }
    
    public URL getCapURL() {
        URL capURL = null;
        
        try {
            capURL = new URL(getCapURL);
       } catch (MalformedURLException ex) {
           LOGGER.warn("Malformed getCapabilities URL.", ex);
       }
        
        return capURL;
    }
    
    public String getURN() {
        return "urn:"+id+":SERVICE";
    }
    
    public String getXML() {

        String base =
             "	<rim:Service id=\"urn:" + id + ":SERVICE\" " 
            +"               objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Service\">"
            
            +"<rim:Classification id=\"urn:" + id + ":SERVICE:CLASSIFICATION\" "
            +"                    classifiedObject=\"urn:" + id + ":SERVICE\" "
            +"                    classificationNode=\"" + getClassificationNode(id) + "\" />"
            +"</rim:Service>"

            +"<wrs:ExtrinsicObject id=\"urn:" + id + ":SERVICE:PROFILE\" "
            +"                     objectType=\"urn:ogc:def:ebRIM-ObjectType:OGC:ServiceProfile\" />"
            
            +"<rim:Association id=\"urn:" + id + ":SERVICE:PRESENTS:" + id + ":PROFILE\" "
            +"                 objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\" "
            +"                 sourceObject=\"urn:" + id + ":SERVICE\" "
            +"                 targetObject=\"urn:" + id + ":SERVICE:PROFILE\" "
            +"                 associationType=\"urn:ogc:def:ebRIM-AssociationType:OGC:Presents\" />";
        
        if (getCapURL != null) {
            base +=
                 "<rim:ExternalLink "
                +"       id=\"urn:" + id + ":SERVICE:CAPABILITIES:FILE:LINK\" "
                +"       objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalLink\" "
                +"       externalURI=\"" + getCapURL + "\" />"
        
                +"<rim:Association "
                +"      id=\"urn:" + id + ":SERVICE:CAPABILITIES:FILE:LINK:SOURCE:" + id + ":SERVICE:PROFILE\" "
                +"      objectType=\"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association\" "
                +"      associationType=\"urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks\" "
                +"      sourceObject=\"urn:" + id + ":SERVICE:CAPABILITIES:FILE:LINK\" "
                +"      targetObject=\"urn:" + id + ":SERVICE:PROFILE\" />";
        }
        
        return base;
    }

    public static String getClassificationNode(String serviceId) {
        if (serviceId.equalsIgnoreCase("WFS")) {
            return "urn:ogc:def:ebRIM-ClassificationScheme:ISO-19119:2005:Services:FeatureAccess";
        } else if (serviceId.equalsIgnoreCase("WCS")) {
            return "urn:ogc:def:ebRIM-ClassificationScheme:ISO-19119:2005:Services:CoverageAccess";
        } else if (serviceId.equalsIgnoreCase("WMS")) {
            return "urn:ogc:def:ebRIM-ClassificationScheme:ISO-19119:2005:Services:MapAccess";
        }
        
        return null;
    }

}
