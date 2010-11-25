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

import be.kzen.ergorr.interfaces.soap.CswSoapClient;
import be.kzen.ergorr.interfaces.soap.csw.CswClient;
import be.kzen.ergorr.interfaces.soap.csw.ServiceExceptionReport;
import be.kzen.ergorr.model.csw.DeleteType;
import be.kzen.ergorr.model.csw.GetRecordByIdResponseType;
import be.kzen.ergorr.model.csw.GetRecordByIdType;
import be.kzen.ergorr.model.csw.InsertType;
import be.kzen.ergorr.model.csw.QueryConstraintType;
import be.kzen.ergorr.model.csw.TransactionResponseType;
import be.kzen.ergorr.model.csw.TransactionSummaryType;
import be.kzen.ergorr.model.csw.TransactionType;
import be.kzen.ergorr.model.ogc.BinaryComparisonOpType;
import be.kzen.ergorr.model.ogc.FilterType;
import be.kzen.ergorr.model.ogc.LiteralType;
import be.kzen.ergorr.model.ogc.PropertyNameType;
import be.kzen.ergorr.model.util.JAXBUtil;
import be.kzen.ergorr.model.util.OFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.apache.log4j.Logger;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class CSWConn {
    private final static Logger LOGGER = Logger.getLogger(CSWConn.class);

    private static final RemoteFileCache FILE_CACHE = new RemoteFileCache();

    private URL serviceUrl;
    private URL wsdlFile;

    public CSWConn(URL serviceURL) {
        this.serviceUrl = serviceURL;

        File file = FILE_CACHE.get(serviceUrl);
        if(file == null) {
            LOGGER.warn("URL " + serviceURL + " has not yet been init'ted");
            file = FILE_CACHE.add(serviceURL);
            if(file == null) {
                LOGGER.warn("Could not init URL " + serviceURL);
                return;
            }
        }

        try {
            wsdlFile = file.toURI().toURL();
        } catch (MalformedURLException ex) {
            LOGGER.error(ex); // should not happen
        }
    }

    public static File getWSDL(URL serviceURL) {
        File file = FILE_CACHE.get(serviceURL);
        if(file == null) {
            LOGGER.warn("WSDL at " + serviceURL + " not cached");
        }
        return file;
    }

    private CswClient createClient() {
        return new CswSoapClient(wsdlFile != null ? wsdlFile : serviceUrl);
    }

    public JAXBElement getById(String urn) throws ServiceExceptionReport {
        LOGGER.info("Querying record by ID " + urn);

        CswClient client = createClient();
        GetRecordByIdType request = new GetRecordByIdType();
        request.getId().add(urn);

        GetRecordByIdResponseType response = client.getRecordById(request);
        List any = response.getAny();

        if(any.size() > 0) {
            JAXBElement resp0 = (JAXBElement)any.get(0);
            LOGGER.info("Found record " + urn + " as a " + resp0.getDeclaredType().getName());
            return resp0;
        } else {
            LOGGER.info("Record not found :: " + urn);
            return null;
        }
    }

    private final static String REGISTRY_OBJECT_LIST_HEADER = 
            "<rim:RegistryObjectList xmlns:wrs=\"http://www.opengis.net/cat/wrs/1.0\""
            +"	xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\""
            +"	xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\""
            +"	xmlns:ows=\"http://www.opengeospatial.net/ows\""
            +"	xmlns:gml=\"http://www.opengis.net/gml\">";

    public TransactionResponseType insert(String ...xml) {
        CswClient client = createClient();

        TransactionType request = new TransactionType();
        InsertType insert = new InsertType();

        request.getInsertOrUpdateOrDelete().add(insert);

        StringBuilder sb = new StringBuilder(REGISTRY_OBJECT_LIST_HEADER);
        for (String s : xml) {
            sb.append(s);
        }
        sb.append("</rim:RegistryObjectList>");

        JAXBElement jaxbEl = null;
        try {
            jaxbEl = (JAXBElement) JAXBUtil.getInstance().unmarshall(sb.toString());
        } catch (JAXBException e) {
            LOGGER.error("", e);
            return null;
        }

        insert.getAny().add(jaxbEl);

        TransactionResponseType response = null;
        try {
            response = client.transact(request);
        } catch (ServiceExceptionReport e) {
            LOGGER.error("", e);
            return null;
        }

        try {
            LOGGER.info("Insert operation details: "
                    + JAXBUtil.getInstance().marshallToStr(OFactory.csw.createTransactionResponse(response)));
        } catch (JAXBException e) {
            LOGGER.error("Could not extract operation details: ", e);
        }

        return response;
    }

    public static final String DELETE_EXTRINSIC_OBJECT = "wrs:ExtrinsicObject";
    
    public int delete(String urn, String typeName) {

        // first operand
        PropertyNameType sourceObject = new PropertyNameType();
        sourceObject.getContent().add("/rim:ExtrinsicObject/@id");
        // second operand
        LiteralType sourceObjectValue = new LiteralType();
        sourceObjectValue.getContent().add(urn);

        // setting a generic binary comparison operation
        BinaryComparisonOpType comparisonOp = new BinaryComparisonOpType();
        comparisonOp.getExpression().add(OFactory.ogc.createPropertyName(sourceObject));
        comparisonOp.getExpression().add(OFactory.ogc.createLiteral(sourceObjectValue));

        FilterType filter = new FilterType();
        filter.setComparisonOps(OFactory.ogc.createPropertyIsEqualTo(comparisonOp));

        // setting query constraint
        QueryConstraintType queryConstraint = new QueryConstraintType();
        queryConstraint.setFilter(filter);

        DeleteType operation = new DeleteType();
        operation.setConstraint(queryConstraint);
        operation.setTypeName(typeName);

        TransactionType request = new TransactionType();
        request.getInsertOrUpdateOrDelete().add(operation);
        request.setVerboseResponse(true);

        CswClient client = createClient();

        TransactionResponseType response = null;
        try {
            response = client.transact(request);
            TransactionSummaryType trt = response.getTransactionSummary();
            int deleted = trt != null && trt.getTotalDeleted() != null ? trt.getTotalDeleted().intValue() : -1;
            LOGGER.info("Deleted " + deleted + " entries from Registry ");

            try {
                LOGGER.info("Delete operation details: "
                        + JAXBUtil.getInstance().marshallToStr(OFactory.csw.createTransactionResponse(response)));
            } catch (JAXBException e) {
                LOGGER.error("Could not extract operation details: ", e);
            }

            return deleted;
        } catch (ServiceExceptionReport e) {
            LOGGER.error("Error in transaction: ", e);
            return -1;
        }
    }

}
