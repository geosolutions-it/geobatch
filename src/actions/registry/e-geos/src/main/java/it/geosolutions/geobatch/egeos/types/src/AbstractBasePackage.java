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
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * <PRE>{@code
 <csn:dataPackage
    xmlns:csn="http://www.emsa.europa.eu/csndc"
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:ows="http://www.opengis.net/ows/1.1"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.emsa.europa.eu/csndc ../XSD/csndc_pkg.xsd">
 
      <csn:packageInfo>
         <csn:">7922_RS2_20100527_055613_0045_SCNA_VV_SCN_83076_0000_0000000_OSN</csn:packageId>
         <csn:packageType>OS_NOTIFICATION</csn:packageType>
         <csn:operationType>TEST</csn:operationType>
         <csn:dataPackageDescription>OS Notification package</csn:dataPackageDescription>
      </csn:packageInfo>
     <csn:eoProduct>
        <csn:identifier>7922_RS2_20100527_055613_0045_SCNA_VV_SCN_83076_0000_0000000</csn:identifier>
     </csn:eoProduct>
 * }</PRE>
 * @author ETj (etj at geo-solutions.it)
 */
public abstract class AbstractBasePackage {

    public final static Namespace NS_CSN = Namespace.getNamespace("csn", "http://www.emsa.europa.eu/csndc");

    private String packageId;
    private String packageType;
    private String eoProductId;

    public AbstractBasePackage(File srcFile) throws JDOMException, IOException {
        this(new SAXBuilder().build(srcFile).getRootElement());
    }

    protected AbstractBasePackage(Element root) {

        Element packageInfo = root.getChild("packageInfo", NS_CSN);
        packageId = packageInfo.getChildText("packageId", NS_CSN);
        packageType = packageInfo.getChildText("packageType", NS_CSN);

        Element eoProduct = root.getChild("eoProduct", NS_CSN);
        eoProductId = eoProduct.getChildText("identifier", NS_CSN);
    }

    public String getEoProductId() {
        return eoProductId;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getPackageType() {
        return packageType;
    }
}
