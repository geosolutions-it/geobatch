/**
 * 
 */
package it.geosolutions.geobatch.egeos.connection;

import it.geosolutions.geobatch.egeos.logic.CollectionsProcessor;
import it.geosolutions.geobatch.egeos.types.dest.CollectionRO;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.kzen.ergorr.interfaces.soap.csw.ServiceExceptionReport;
import be.kzen.ergorr.model.csw.TransactionResponseType;
import be.kzen.ergorr.model.rim.ExtrinsicObjectType;
import be.kzen.ergorr.model.wrs.WrsExtrinsicObjectType;

/**
 * @author Administrator
 * 
 */
public class CollectionsSender {

    private final static Logger LOGGER = Logger.getLogger(CollectionsSender.class);

    private URL serviceURL;

    private CSWConn cswConn;

    private CollectionsProcessor processor;

    public CollectionsSender(CollectionsProcessor processor) {
        this.processor = processor;
    }

    public void run() throws ServiceExceptionReport, JAXBException {
        if (serviceURL == null)
            throw new IllegalStateException("ServiceURL not set");

        if (processor != null) {
            for (int i = 0; i < processor.size(); i++) {
                CollectionRO collection = processor.getCollection(i);
                if (cswConn.getById(collection.getURN()) == null) {
                    cswConn.insert(collection.getXML());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean existsCollection(CollectionRO collection) throws ServiceExceptionReport,
            JAXBException {
        LOGGER.info("Querying for collection " + collection.getURN());

        JAXBElement<WrsExtrinsicObjectType> pext = cswConn.getById(collection.getURN());

        if (pext != null) {
            JAXBElement<WrsExtrinsicObjectType> resp0 = pext;
            ExtrinsicObjectType extobj = resp0.getValue();
            LOGGER.info("Found collection " + extobj.getId() + " as a "
                    + resp0.getDeclaredType().getName());
            LOGGER.info(extobj.toString());
        } else
            LOGGER.info("Colleciton not found :: " + collection.getURN());

        return pext != null;
    }

    @SuppressWarnings("unchecked")
    public boolean updateCollection(CollectionRO collection) throws ServiceExceptionReport,
            JAXBException {
        LOGGER.info("Updating for collection " + collection.getURN());

        JAXBElement<ExtrinsicObjectType> pext = cswConn.getById(collection.getURN());

        if (pext != null && pext.getValue() != null) {
            ExtrinsicObjectType extobj = pext.getValue();
            CollectionRO rrCollection = CollectionsProcessor.extobj2ro(extobj);
            double[] rrEnvelope = rrCollection.getEnvelope();
            if (rrEnvelope != null && !Double.isNaN(rrEnvelope[0]) && !Double.isNaN(rrEnvelope[1])
                    && !Double.isNaN(rrEnvelope[2]) && !Double.isNaN(rrEnvelope[3])) {
                collection.setEnvelope(rrEnvelope[0], rrEnvelope[1], rrEnvelope[2], rrEnvelope[3]);
            }

            List<String> rrTimeStamps = rrCollection.getTimeStamps();
            if (rrTimeStamps != null && rrTimeStamps.size() > 0) {
                collection.setTimeStamp(new LinkedList<String>(rrTimeStamps));
            }
        }

        TransactionResponseType trt = cswConn.insert(collection.getXML());
        int inserted = trt != null && trt.getTransactionSummary() != null ? trt.getTransactionSummary().getTotalInserted().intValue() : -1;
        LOGGER.info("Inserted " + inserted + " Collections...");
        return inserted > 0;
    }

    public void setServiceURL(URL serviceURL) {
        this.serviceURL = serviceURL;
        cswConn = new CSWConn(serviceURL);
    }
}