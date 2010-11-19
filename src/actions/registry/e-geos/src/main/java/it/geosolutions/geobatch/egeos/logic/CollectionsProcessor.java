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

package it.geosolutions.geobatch.egeos.logic;

import it.geosolutions.geobatch.egeos.types.dest.CollectionRO;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;

import be.kzen.ergorr.model.gml.EnvelopeType;
import be.kzen.ergorr.model.rim.ExtrinsicObjectType;
import be.kzen.ergorr.model.rim.SlotType;
import be.kzen.ergorr.model.rim.ValueListType;
import be.kzen.ergorr.model.wrs.WrsValueListType;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class CollectionsProcessor {

    @SuppressWarnings("unused")
    private final static Logger LOGGER = Logger.getLogger(CollectionsProcessor.class);

    private List<CollectionRO> collections = new LinkedList<CollectionRO>();

    public CollectionsProcessor() {
        
    }
    
    public CollectionsProcessor(String... collectionIDs) {
        for (String collectionID : collectionIDs) {
            getCollections().add(collectionRO(collectionID));
        }
    }

    public int size() {
        return getCollections().size();
    }

    public CollectionRO getCollection(int index) {
        if (index < getCollections().size())
            return getCollections().get(index);

        return null;
    }

    public CollectionRO getCollection(String collectionID) {
        for (int index = 0; index < getCollections().size(); index++) {
            if (getCollections().get(index).getURN().equals(
                    "urn:" + collectionID.toUpperCase() + ":COLLECTION"))
                return getCollections().get(index);
        }

        return null;
    }

    public static CollectionRO collectionRO(String collectionId) {
        CollectionRO collection = new CollectionRO();
        collection.setId(collectionId);

        return collection;
    }

    @SuppressWarnings("unchecked")
    public static CollectionRO extobj2ro(ExtrinsicObjectType extobj) {
        CollectionRO collection = new CollectionRO();
        collection.setId(extobj.getLid());

        for (SlotType slot : extobj.getSlot()) {
            if (slot.getSlotType().equals("urn:ogc:def:dataType:ISO-19107:2003:GM_Envelope")) {
                JAXBElement<WrsValueListType> envelope = (JAXBElement<WrsValueListType>) slot.getValueList();
                JAXBElement<EnvelopeType> envelopeType = (JAXBElement<EnvelopeType>) envelope.getValue().getAnyValue().get(0).getContent().get(0);

                double l0 = envelopeType.getValue().getLowerCorner().getValue().get(0);
                double l1 = envelopeType.getValue().getLowerCorner().getValue().get(1);
                double u0 = envelopeType.getValue().getUpperCorner().getValue().get(0);
                double u1 = envelopeType.getValue().getUpperCorner().getValue().get(1);
                collection.setEnvelope(l0, l1, u0, u1);
            }

            if (slot.getSlotType().equals("urn:ogc:def:dataType:ISO-19108:2002:TM_Instant")) {
                JAXBElement<ValueListType> timeInstants = (JAXBElement<ValueListType>) slot.getValueList();
                collection.setTimeStamp(new LinkedList<String>(timeInstants.getValue().getValue()));
            }
        }

        return collection;
    }

    /**
     * @param collections the collections to set
     */
    public void setCollections(List<CollectionRO> collections) {
        this.collections = collections;
    }

    /**
     * @return the collections
     */
    public List<CollectionRO> getCollections() {
        return collections;
    }
}
