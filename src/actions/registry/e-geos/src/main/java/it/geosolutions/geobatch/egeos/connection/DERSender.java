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
import it.geosolutions.geobatch.egeos.logic.SarDerivedProcessor;
import it.geosolutions.geobatch.egeos.logic.ShipProcessor;
import it.geosolutions.geobatch.egeos.types.dest.CollectionRO;
import it.geosolutions.geobatch.egeos.types.dest.SARProductRO;
import it.geosolutions.geobatch.egeos.types.dest.SarDerivedFeatrueRO;
import it.geosolutions.geobatch.egeos.types.dest.VesselRO;
import it.geosolutions.geobatch.egeos.types.src.SarDerivedDataPackage;
import it.geosolutions.geobatch.egeos.types.src.ShipDetectionPackage;

import java.net.URL;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import be.kzen.ergorr.interfaces.soap.csw.ServiceExceptionReport;
import be.kzen.ergorr.model.csw.TransactionResponseType;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class DERSender {

    private final static Logger LOGGER = Logger.getLogger(DERSender.class);

    private URL serviceURL;
    private CSWConn cswConn;
    private Object processor;
    private CollectionRO[] collections;

    public DERSender(Object processor) {
        this.processor = processor;
    }

    public void run() throws ServiceExceptionReport, JAXBException {
        if (serviceURL == null)
            throw new IllegalStateException("ServiceURL not set");

        int inserted = 0;

            if (processor instanceof ShipProcessor) {
                ShipDetectionPackage pkg = ((ShipProcessor) processor).parsePackage();
                String eoId = pkg.getEoProductId();
                String sarURN = SARProductRO.getURN(eoId);

                if (cswConn.getById(sarURN) == null) {
                    String err = "Can't send DER objects: base SAR product not found in registry: " + sarURN + " -- DER package:" + ((ShipProcessor) processor).getPackage().getPackageId();
                    LOGGER.error(err);
                    LOGGER.info("TODO: we have to handle this case");
                    throw new IllegalStateException(err);
                }

                for (int i = 0; i < ((ShipProcessor) processor).size(); i++) {
                    VesselRO ro = ((ShipProcessor) processor).getRegistryObject(i);
                    LOGGER.info("Inserting ShipDetection " + ro.getURN());
                    TransactionResponseType trt = cswConn.insert(ro.getXML(), ro.getProductAssociationXML(pkg.getEoProductId()));
                    int res = trt != null && trt.getTransactionSummary() != null ? trt.getTransactionSummary().getTotalInserted().intValue() : -1;
                    inserted += res;
                    LOGGER.info("Inserted " + inserted + " object in registry for DER package " + pkg.getPackageId());
                    if (res > 0 && collections != null) {
                        for (CollectionRO collection : collections) {
                            try {
                                collection.update(ro.getEnvelope(), ro.getTimeStamp());
                            } catch (NoSuchAuthorityCodeException e) {
                                LOGGER.error(e);
                                throw new ServiceExceptionReport(e.getLocalizedMessage(), e);
                            } catch (FactoryException e) {
                                LOGGER.error(e);
                                throw new ServiceExceptionReport(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
            } else if (processor instanceof SarDerivedProcessor) {
                SarDerivedDataPackage pkg = ((SarDerivedProcessor) processor).parsePackage();
                String eoId = pkg.getEoProductId();
                String sarURN = SARProductRO.getURN(eoId);

                if (cswConn.getById(sarURN) == null) {
                    String err = "Can't send DER objects: base SAR product not found in registry: " + sarURN + " -- DER package:" + ((SarDerivedProcessor) processor).getPackage().getPackageId();
                    LOGGER.error(err);
                    LOGGER.info("TODO: we have to handle this case");
                    throw new IllegalStateException(err);
                }

                for (int i = 0; i < ((SarDerivedProcessor) processor).size(); i++) {
                    SarDerivedFeatrueRO ro = ((SarDerivedProcessor) processor).getRegistryObject(i);
                    LOGGER.info("Inserting SarDerivedFeature " + ro.getURN());
                    TransactionResponseType trt = cswConn.insert(ro.getXML(), ro.getProductAssociationXML(pkg.getEoProductId()));
                    int res = trt != null && trt.getTransactionSummary() != null ? trt.getTransactionSummary().getTotalInserted().intValue() : -1;
                    inserted += res;
                    LOGGER.info("Inserted " + inserted + " object in registry for DER package " + pkg.getPackageId());
                    if (res > 0 && collections != null) {
                        for (CollectionRO collection : collections) {
                            try {
                                collection.update(ro.getEnvelope(), ro.getTimeStamp());
                            } catch (NoSuchAuthorityCodeException e) {
                                LOGGER.error(e);
                                throw new ServiceExceptionReport(e.getLocalizedMessage(), e);
                            } catch (FactoryException e) {
                                LOGGER.error(e);
                                throw new ServiceExceptionReport(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
            }
            
            
            if (inserted > 0 && collections != null) {
                CollectionsProcessor collectionProcessor = new CollectionsProcessor();
                collectionProcessor.setCollections(Arrays.asList(collections));
                CollectionsSender collectionsSender = new CollectionsSender(collectionProcessor);
                collectionsSender.setServiceURL(serviceURL);
                
                for (CollectionRO collection : collections) {
                    collectionsSender.updateCollection(collection);
                }
            }
    }

    public void setServiceURL(URL serviceURL) {
        this.serviceURL = serviceURL;
        cswConn = new CSWConn(serviceURL);
    }

    /**
     * @param collections the collections to set
     */
    public void setCollections(CollectionRO ...collections) {
        this.collections = collections;
    }
    public void setCollections(String collection) {
        this.setCollections(new String[] {collection});
    }
    public void setCollections(String ...collections) {
        this.collections = new CollectionRO[collections.length];
        for (int i=0; i<collections.length; i++) {
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
