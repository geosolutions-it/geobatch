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

import be.kzen.ergorr.interfaces.soap.csw.ServiceExceptionReport;
import be.kzen.ergorr.model.wrs.WrsExtrinsicObjectType;
import it.geosolutions.geobatch.egeos.logic.EOProcessor;
import it.geosolutions.geobatch.egeos.types.dest.PlatformRO;
import it.geosolutions.geobatch.egeos.types.dest.SARProductRO;
import it.geosolutions.geobatch.egeos.types.src.EarthObservation;
import it.geosolutions.geobatch.egeos.types.src.EarthObservationPackage;
import java.net.URL;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.apache.log4j.Logger;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class EOPSender {

    private final static Logger LOGGER = Logger.getLogger(EOPSender.class);
    private URL serviceURL;
    private CSWConn cswConn;

    private EOProcessor processor;

    public EOPSender(EOProcessor processor) {
        this.processor = processor;
    }

    public void run() throws ServiceExceptionReport, JAXBException {
        if(serviceURL == null)
            throw new IllegalStateException("ServiceURL not set");

//        EarthObservationPackage pkg = processor.parsePackage();
//        EarthObservation eo = processor.getEO();
        PlatformRO platform = processor.getPlatform();
        try {
            if (!existPlatform(platform)) {
                insertPlatform(processor.getPlatform());
            }
        } catch (Exception ex) {
            LOGGER.error("Error while handling platform at "+serviceURL+": " + ex.getMessage(), ex);
            return;
        }


        insertEOP(processor.getSARProduct());
    }

    public void setServiceURL(URL serviceURL) {
        this.serviceURL = serviceURL;
        cswConn = new CSWConn(serviceURL);
    }

    public boolean existPlatform(PlatformRO platform) throws ServiceExceptionReport, JAXBException {
        LOGGER.info("Querying for platform " + platform.getURN());

        JAXBElement<WrsExtrinsicObjectType> pext = cswConn.getById(platform.getURN());

        if(pext != null) {
            JAXBElement<WrsExtrinsicObjectType> resp0 = pext;
            WrsExtrinsicObjectType extobj = resp0.getValue();
            LOGGER.info("Found platform "+extobj.getId()+" as a " + resp0.getDeclaredType().getName());
            LOGGER.info(extobj.toString());
        } else
            LOGGER.info("Platform not found :: "+platform.getURN());

        return pext != null;
    }


    private void insertPlatform(PlatformRO platform) {
        LOGGER.info("Inserting platform " + platform.getURN());

        cswConn.insert(platform.getXML());
    }

    private void insertEOP(SARProductRO sp) {
        LOGGER.info("Inserting SARProduct " + sp.getURN());

        cswConn.insert(sp.getXML());
    }


}
