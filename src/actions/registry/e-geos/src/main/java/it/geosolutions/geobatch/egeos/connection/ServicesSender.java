/**
 * 
 */
package it.geosolutions.geobatch.egeos.connection;

import it.geosolutions.geobatch.egeos.logic.ServicesProcessor;
import it.geosolutions.geobatch.egeos.types.dest.ServiceRO;

import java.net.URL;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import be.kzen.ergorr.interfaces.soap.csw.ServiceExceptionReport;
import be.kzen.ergorr.model.rim.ServiceType;

/**
 * @author Administrator
 * 
 */
public class ServicesSender {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServicesSender.class);
    private URL serviceURL;
    private CSWConn cswConn;

    private ServicesProcessor processor;

    public ServicesSender(ServicesProcessor processor) {
        this.processor = processor;
    }

    public void run() throws ServiceExceptionReport, JAXBException {
        if(serviceURL == null)
            throw new IllegalStateException("ServiceURL not set");
        
        if (processor != null) {
            for (String serviceId : processor.getServiceIDs()) {
                ServiceRO service = ServicesProcessor.serviceRO(serviceId, processor
                        .getServiceGetCapURL(serviceId));
                if (cswConn.getById(service.getURN()) == null) {
                    cswConn.insert(service.getXML());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean existsService(ServiceRO service) throws ServiceExceptionReport, JAXBException {
        LOGGER.info("Querying for service " + service.getURN());

        JAXBElement<ServiceType> pext = cswConn.getById(service.getURN());

        if(pext != null) {
            JAXBElement<ServiceType> resp0 = pext;
            ServiceType extobj = resp0.getValue();
            LOGGER.info("Found service "+extobj.getId()+" as a " + resp0.getDeclaredType().getName());
            LOGGER.info(extobj.toString());
        } else
            LOGGER.info("Service not found :: "+service.getURN());

        return pext != null;
    }
    
    public void setServiceURL(URL serviceURL) {
        this.serviceURL = serviceURL;
        cswConn = new CSWConn(serviceURL);
    }
}
