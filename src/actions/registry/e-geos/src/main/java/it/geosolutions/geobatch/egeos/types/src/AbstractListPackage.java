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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      <csn:oilSpills total="18">
         <csn:oilSpillReference>
            <csn:identifier>7922_RS2_20100527_055613_0045_SCNA_VV_SCN_83076_0000_0000000_OS_1_1</csn:identifier>
            <csn:fileName>7922_RS2_20100527_055613_0045_SCNA_VV_SCN_83076_0000_0000000_OS_1_1_OSN.xml</csn:fileName>
         </csn:oilSpillReference>
     ...
 * }</PRE>
 * @author ETj (etj at geo-solutions.it)
 */
public abstract class AbstractListPackage extends AbstractBasePackage {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractListPackage.class);
    
    public static class PackedElement {
        private String identifier;
        private String filename;

        public PackedElement(String identifier, String filename) {
            this.identifier = identifier;
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "["+ identifier + ";" + filename + ']';
        }
    }

    protected abstract String getCollectionName();
    protected abstract String getReferenceName();

    protected String getIdentifier() {
        return "identifier";
    }

    private List<PackedElement> elements = new ArrayList<PackedElement>();

    @SuppressWarnings("unchecked")
    public AbstractListPackage(File srcFile) throws JDOMException, IOException {
        super(srcFile); // fixme: the xml will be parsed again

        Document doc = new SAXBuilder().build(srcFile);
        Element root = doc.getRootElement();

        List<PackedElement> tmpList = new ArrayList<PackedElement>();

        Element collection = root.getChild(getCollectionName(), NS_CSN);
        List<Element> refList = new LinkedList<Element>();

        try {
            refList = collection.getChildren(getReferenceName(), NS_CSN);
        } catch (Exception e) {
            LOGGER.warn("Could not find elements of type " + getReferenceName() + " into the package!", e);
        }
        
        for (Element ref : refList) {
            String id = ref.getChildText(getIdentifier(), NS_CSN);
            String filename = ref.getChildText("fileName", NS_CSN);
            PackedElement packedElement = new PackedElement(id, filename);
            tmpList.add(packedElement);
        }

        elements = Collections.unmodifiableList(tmpList);
    }

    public List<PackedElement> getList() {
        return elements;
    }
}
