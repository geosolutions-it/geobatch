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
package it.geosolutions.geobatch.egeos.connection;

import it.geosolutions.geobatch.egeos.logic.CollectionsProcessor;
import it.geosolutions.geobatch.egeos.logic.EOProcessor;
import it.geosolutions.geobatch.egeos.types.dest.CollectionRO;
import it.geosolutions.geobatch.egeos.types.dest.PlatformRO;
import it.geosolutions.geobatch.egeos.types.dest.SARProductRO;

import java.net.URL;
import java.util.Arrays;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kzen.ergorr.interfaces.soap.csw.ServiceExceptionReport;
import be.kzen.ergorr.model.csw.TransactionResponseType;
import be.kzen.ergorr.model.wrs.WrsExtrinsicObjectType;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class EOPSender {

    private final static Logger LOGGER = LoggerFactory.getLogger(EOPSender.class);

    private URL serviceURL;

    private CSWConn cswConn;

    private EOProcessor processor;

    private CollectionRO[] collections;

    public EOPSender(EOProcessor processor) {
        this.processor = processor;
    }

    public void run() throws ServiceExceptionReport, JAXBException {
        if (serviceURL == null)
            throw new IllegalStateException("ServiceURL not set");

        // EarthObservationPackage pkg = processor.parsePackage();
        // EarthObservation eo = processor.getEO();
        processor.parsePackage();
        PlatformRO platform = processor.getPlatform();
        try {
            if (!existsPlatform(platform)) {
                insertPlatform(processor.getPlatform());
            }
        } catch (Exception ex) {
            LOGGER.error("Error while handling platform at " + serviceURL + ": " + ex.getMessage(),
                    ex);
            return;
        }

        insertEOP(processor.getSARProduct());
    }

    public void setServiceURL(URL serviceURL) {
        this.serviceURL = serviceURL;
        cswConn = new CSWConn(serviceURL);
    }

    @SuppressWarnings("unchecked")
    public boolean existsPlatform(PlatformRO platform) throws ServiceExceptionReport, JAXBException {
        LOGGER.info("Querying for platform " + platform.getURN());

        JAXBElement<WrsExtrinsicObjectType> pext = cswConn.getById(platform.getURN());

        if (pext != null) {
            JAXBElement<WrsExtrinsicObjectType> resp0 = pext;
            WrsExtrinsicObjectType extobj = resp0.getValue();
            LOGGER.info("Found platform " + extobj.getId() + " as a "
                    + resp0.getDeclaredType().getName());
            LOGGER.info(extobj.toString());
        } else
            LOGGER.info("Platform not found :: " + platform.getURN());

        return pext != null;
    }

    private void insertPlatform(PlatformRO platform) {
        LOGGER.info("Inserting platform " + platform.getURN());

        cswConn.insert(platform.getXML());
    }

    private void insertEOP(SARProductRO sp) throws ServiceExceptionReport, JAXBException {
        LOGGER.info("Inserting SARProduct " + sp.getURN());

        TransactionResponseType trt = cswConn.insert(sp.getXML());
        int inserted = trt != null && trt.getTransactionSummary() != null ? trt
                .getTransactionSummary().getTotalInserted().intValue() : -1;

        if (inserted > 0 && collections != null) {
            CollectionsProcessor collectionProcessor = new CollectionsProcessor();
            collectionProcessor.setCollections(Arrays.asList(collections));
            CollectionsSender collectionsSender = new CollectionsSender(collectionProcessor);
            collectionsSender.setServiceURL(serviceURL);

            for (CollectionRO collection : collections) {
                try {
                    collection.update(sp.getEnvelope(), sp.getBeginTimePosition());
                } catch (NoSuchAuthorityCodeException e) {
                    LOGGER.error(e.getLocalizedMessage(),e);
                    throw new ServiceExceptionReport(e.getLocalizedMessage(), e);
                } catch (FactoryException e) {
                    LOGGER.error(e.getLocalizedMessage(),e);
                    throw new ServiceExceptionReport(e.getLocalizedMessage(), e);
                }
                collectionsSender.updateCollection(collection);
            }
        }
    }

    /**
     * @param collections
     *            the collections to set
     */
    public void setCollections(CollectionRO... collections) {
        this.collections = collections;
    }

    public void setCollections(String collection) {
        this.setCollections(new String[] { collection });
    }

    public void setCollections(String... collections) {
        this.collections = new CollectionRO[collections.length];
        for (int i = 0; i < collections.length; i++) {
            this.collections[i] = new CollectionRO();
            this.collections[i].setId(collections[i]);
        }
    }

    /**
     * @return the collections
     */
    public CollectionRO[] getCollections() {
        return collections;
    }

}
