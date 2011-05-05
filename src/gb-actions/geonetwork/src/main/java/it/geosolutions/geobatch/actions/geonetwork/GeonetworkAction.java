/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.geonetwork;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.geonetwork.util.HTTPUtils;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Queue;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.jdom.CDATA;
import org.jdom.Document;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Perform an operation in GeoNetwork, according to the input configuration.
 * <br/><br/>
 * At the moment only the metadata insertion is supported.<br/>
 * <h3>Insert metadata</h3>
 * The input file may be a pure metadata to be inserted into GN, or a full GN
 * insert metadata request. The full request requires some more meta-metadata.
 * <br/>If such further data are not provided in the input file, they may be specified 
 * in the configuration, and the Action will compile the full request to 
 * be sent to GN.
 * 
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkAction 
        extends BaseAction<FileSystemEvent> {
    
    
    private final static Logger LOGGER = LoggerFactory.getLogger(GeonetworkAction.class);

    GeonetworkInsertConfiguration cfg;

    public GeonetworkAction(GeonetworkInsertConfiguration configuration) {
        super(configuration);
        cfg = configuration;
    }

    /**
     * 
     */
//    @Override
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {
        
        // get the input event
        FileSystemEvent event = events.poll();
        File inputFile = event.getSource();
        
        
        Element requestDocument;
        
        if(cfg.isOnlyMetadataInput()) { // only metadata available: we have to build the full request packet
            if(LOGGER.isInfoEnabled()) 
                LOGGER.info("Handling pure metadata file " + inputFile);
            requestDocument = buildRequestDocument(inputFile);
        } else { // the full xml request is ready in the file to be sent to GN; just parse it
            if(LOGGER.isInfoEnabled()) 
                LOGGER.info("Handling full request file " + inputFile);
            requestDocument = parseFile(inputFile);
        }
        
        // login and insert the metadata
        send(requestDocument);
        
        return events;
    }

    /**
     * Creates a Request document for the geonetwork <tt>metadata.insert</tt> operation.
     * <br/>The metadata is read from the file, the other params are read from
     * the configuration.
     * 
     * <ul>
     * <li><b><tt>data</tt></b>: (mandatory) Contains the metadata record</li>
     * <li><b><tt>group</tt></b> (mandatory): Owner group identifier for metadata</li>
     * <li><b><tt>isTemplate</tt></b>: indicates if the metadata content is a new template or not. Default value: "n"</li>
     * <li><b><tt>title</tt></b>: Metadata title. Only required if isTemplate = "y"</li>
     * <li><b><tt>category</tt></b> (mandatory): Metadata category. Use "_none_" value to don't assign any category</li>
     * <li><b><tt>styleSheet</tt></b> (mandatory): Stylesheet name to transform the metadata before inserting in the catalog. Use "_none_" value to don't apply any stylesheet</li>
     * <li><b><tt>validate</tt></b>: Indicates if the metadata should be validated before inserting in the catalog. Values: on, off (default)    </li>
     * </ul>
     */
    private Element buildRequestDocument(File inputFile) throws ActionException {
        if(LOGGER.isDebugEnabled()) 
            LOGGER.debug("Compiling request document");
        
        Element metadataFromFile = parseFile(inputFile);

        XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());
        CDATA cdata = new CDATA(outputter.outputString(metadataFromFile)); // CDATA format is required by GN
        
        Element request = new Element("request");
        request.addContent(new Element("data").addContent(cdata));
        request.addContent(new Element("group").setText(cfg.getGroup()));
        request.addContent(new Element("category").setText(cfg.getCategory()==null?"_none":cfg.getCategory()));
        request.addContent(new Element("styleSheet").setText(cfg.getStyleSheet()==null?"_none":cfg.getStyleSheet()));
        request.addContent(new Element("validate").setText(cfg.getValidate()==null?"off":cfg.getValidate().booleanValue()?"on":"off"));
                    
        return request;
    }

    private void send(Element requestDocument) throws ActionException {
        HTTPUtils connection = new HTTPUtils();
        
        boolean logged = gnLogin(connection, 
                cfg.getGeonetworkServiceURL(), 
                cfg.getLoginUsername(), cfg.getLoginPassword());
        
        if( ! logged ) 
            throw new ActionException(this, "Login failed");
        
        gnInsertMetadata(connection, cfg.getGeonetworkServiceURL(), requestDocument);
    }

    /**
     * Perform a GN login.<br/>
     * GN auth is carried out via a JSESSIONID cookie returned by a successful login
     * call.<br/>
     * 
     * <ul>
     * <li>Url: <tt>http://<i>server</i>:<i>port</i>/geonetwork/srv/en/xml.user.login</tt></li>
     * <li>Mime-type: <tt>application/xml</tt></li>
     * <li>Post request: <pre>{@code
     *   <?xml version="1.0" encoding="UTF-8"?>
     *   <request>
     *       <username>admin</username>
     *       <password>admin</password>
     *   </request>
     * }</pre></li>
     * </ul>
     * 
     * @return true if login was successful
     * 
     * @see <a href="http://geonetwork-opensource.org/manuals/trunk/developer/xml_services/login_xml_services.html#login-services" >GeoNetwork documentation about login</a>
     */
    private static boolean gnLogin(HTTPUtils connection, String serviceURL, String username, String password) {
        Element request = new Element("request");
        request.addContent(new Element("username").setText(username));
        request.addContent(new Element("password").setText(password));
        
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        String xml = outputter.outputString(request);
        
        String loginURL = serviceURL+"/srv/en/xml.user.login";
        String out = connection.postXml(loginURL, xml);
        
        return connection.getLastHttpStatus() == HttpStatus.SC_OK;
    }

    /**
     * Insert a metadata in GN.<br/>
     * 
     * <ul>
     * <li>Url: <tt>http://<i>server</i>:<i>port</i>/geonetwork/srv/en/metadata.insert</tt></li>
     * <li>Mime-type: <tt>application/xml</tt></li>
     * <li>Post request: <pre>{@code 
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *    <group>2</group>
     *    <category>_none_</category>
     *    <styleSheet>_none_</styleSheet>
     *    <data><![CDATA[
     *       <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd"
     *                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     *       ...
     *          </gmd:DQ_DataQuality>
     *         </gmd:dataQualityInfo>
     *       </gmd:MD_Metadata>]]>
     *    </data>
     * </request> }</pre></li>
     * </ul>
     * 
     * @return
     * 
     * @see <a href="http://geonetwork-opensource.org/latest/developers/xml_services/metadata_xml_services.html#insert-metadata-metadata-insert" >GeoNetwork documentation about inserting metadata</a>
     */
    private void gnInsertMetadata(HTTPUtils connection, String serviceURL, final Element gnRequest) throws ActionException {

        String loginURL = serviceURL+"/srv/en/metadata.insert";
        
        final XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos;
        try {
            pos = new PipedOutputStream(pis);
        } catch (IOException ex) {
            throw new ActionException(this, "Error setting up streams", ex);
        }
        final String thid = "GN_SEND_"+System.currentTimeMillis() ; // TODO: set a unique action instance id
        
        Thread t = new Thread(new Runnable() {

//            @Override
            public void run() {
                try {
                    outputter.output(gnRequest, pos);        
                    pos.flush();
                    LOGGER.info("Data sent to GeoNetwork successfully");
                } catch (IOException ex) {
                    LOGGER.error("Error while sending data to GeoNetwork", ex);
                } finally {
                    IOUtils.closeQuietly(pis);
                    IOUtils.closeQuietly(pos);
                }
            }
        }, thid) ;
        t.start();
                
        connection.setIgnoreResponseContentOnSuccess(true);
        connection.postXml(loginURL, pis);
        if(connection.getLastHttpStatus() != HttpStatus.SC_OK)
            throw new ActionException(this, "Error inserting metadata in GeoNetwork");

    }
    
    private Element parseFile(File file) throws ActionException {
        try{
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(file);
			return  (Element)doc.getRootElement().detach();
		} catch (Exception ex) {
			LOGGER.warn("Error parsing input file " + file);
            throw new ActionException(this, "Error parsing input file " + file, ex);
		}
    }    
}
