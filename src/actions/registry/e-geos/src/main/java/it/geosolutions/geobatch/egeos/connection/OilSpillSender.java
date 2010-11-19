/**
 * 
 */
package it.geosolutions.geobatch.egeos.connection;

import it.geosolutions.geobatch.egeos.logic.CollectionsProcessor;
import it.geosolutions.geobatch.egeos.logic.OilSpillProcessor;
import it.geosolutions.geobatch.egeos.types.dest.CollectionRO;
import it.geosolutions.geobatch.egeos.types.dest.OilSpillRO;
import it.geosolutions.geobatch.egeos.types.dest.SARProductRO;
import it.geosolutions.geobatch.egeos.types.src.OilSpillPackage;

import java.net.URL;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import be.kzen.ergorr.interfaces.soap.csw.ServiceExceptionReport;
import be.kzen.ergorr.model.csw.TransactionResponseType;

/**
 * @author Administrator
 * 
 */
public class OilSpillSender {

    private final static Logger LOGGER = Logger.getLogger(OilSpillSender.class);
    private URL serviceURL;
    private CSWConn cswConn;
    private OilSpillProcessor processor;
    private CollectionRO[] collections;

    public OilSpillSender(OilSpillProcessor processor) {
        this.processor = processor;
    }

    public void run() throws ServiceExceptionReport, JAXBException {
        if(serviceURL == null)
            throw new IllegalStateException("ServiceURL not set");
        
        if (processor != null) {
            OilSpillPackage pkg = processor.parsePackage();
            String eoId = pkg.getEoProductId();
            String sarURN = SARProductRO.getURN(eoId);

            if (cswConn.getById(sarURN) == null) {
                String err = "Can't send OS* objects: base SAR product not found in registry: " + sarURN + " -- OS* package:" + processor.getPackage().getPackageId();
                LOGGER.error(err);
                LOGGER.info("TODO: we have to handle this case");
                throw new IllegalStateException(err);
            }

            int inserted = 0;
            for (int i = 0; i < processor.size(); i++) {
                OilSpillRO ro = processor.getRegistryObject(i);
                TransactionResponseType trt = cswConn.insert(ro.buildXML());
                int res = trt != null && trt.getTransactionSummary() != null ? trt.getTransactionSummary().getTotalInserted().intValue() : -1;
                inserted+= res;
                LOGGER.info("Inserted " + res + " object in registry for OS* package " + pkg.getPackageId());
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
