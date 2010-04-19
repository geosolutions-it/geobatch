/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.geoserver.rest;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Connect to a GeoServer instance and read its data.
 * <BR>Info are returned as Strings or, for complex data, as JDOM elements.
 * You may use the {@link parser.GSRestLayerParser}, {@link parser.GSRestDatastoreParser}, etc
 * to retrieve info from these xml trees.
 *
 * <P><B>TODO: username and pw are now mandatory also for getting data from GS.</B>
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class GeoServerRESTReader {
	private final static Logger LOGGER = Logger.getLogger(GeoServerRESTReader.class.getName());

	private final String baseurl;
    private  String username;
    private  String password;

	public GeoServerRESTReader(URL restUrl) {
		this.baseurl = restUrl.toExternalForm();
	}

	public GeoServerRESTReader(String restUrl)
            throws MalformedURLException{
		new URL (restUrl); // check URL correctness
		this.baseurl = restUrl;
	}

    public GeoServerRESTReader(URL restUrl, String username, String password) {
        this(restUrl);
        this.username = username;
        this.password = password;
    }

	private Element load(String url) {
		try {
			String response = HTTPUtils.get(baseurl + url, username, password);
			if (response == null) {
				return null;
			}
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new StringReader(response));
			return doc.getRootElement();
		} catch (JDOMException ex) {
			LOGGER.log(Level.WARNING, "Ex parsing HTTP REST response", ex);
		} catch (MalformedURLException ex) {
			LOGGER.log(Level.WARNING, "Bad URL", ex);
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Ex loading HTTP REST response", ex);
		}

		return null;
	}


	public boolean existGeoserver() {
		return HTTPUtils.httpPing(baseurl + "/rest/", username, password);
	}

	public boolean existsStyle(String styleName) {
		String url = baseurl + "/rest/styles/" + styleName + ".xml";
		return HTTPUtils.exists(url, username, password);
	}
	
	public Element getDatastores(String workspace) {
		String url = "/rest/workspaces/" + workspace + "/datastores.xml";
        if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("### Retrieving DS list from " + url);
		return load(url);
	}

	public List<String> getDatastoresNames(String workspace) {
		List<String> names = new ArrayList<String>();
		Element dslist = getDatastores(workspace);
		if(dslist == null)
			return null;
		for (Element ds : (List<Element>)dslist.getChildren("dataStore")) {
			names.add(ds.getChildText("name"));
		}
		return names;
	}

	public Element getDatastore(String workspace, String dsName) {
		String url = "/rest/workspaces/" + workspace + "/datastores"+dsName+".xml";
		if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("### Retrieving DS from " + url);
		return load(url);
	}

	public Element getLayers() {
		String url = "/rest/layers.xml";
		if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("### Retrieving layers from " + url);
		return load(url);
	}

	public List<String> getLayerNames() {
		List<String> names = new ArrayList<String>();
		Element layers = getLayers();
		if(layers == null)
			return null;
		for (Element layer : (List<Element>)layers.getChildren("layer")) {
			names.add(layer.getChildText("name"));
		}
		return names;
	}

	public Element getLayer(String name) {
		String url = "/rest/layers/"+name+".xml";
		if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("### Retrieving layer from " + url);
		return load(url);
	}

	public Element getNamespaces() {
		String url = "/rest/namespaces.xml";
		if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("### Retrieving namespaces from " + url);
		return load(url);
	}

	public List<String> getNamespaceNames() {
		List<String> names = new ArrayList<String>();
		Element namespaces = getNamespaces();
		if(namespaces == null)
			return null;
		for (Element namespace : (List<Element>)namespaces.getChildren("namespace")) {
			names.add(namespace.getChildText("name"));
		}
		return names;
	}

	public Element getWorkspaces() {
		String url = "/rest/workspaces.xml";
		if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("### Retrieving workspaces from " + url);
		return load(url);
	}

	public List<String> getWorkspaceNames() {
		List<String> names = new ArrayList<String>();
		Element workspaces = getWorkspaces();
		if(workspaces == null)
			return null;
		for (Element workspace : (List<Element>)workspaces.getChildren("workspace")) {
			names.add(workspace.getChildText("name"));
		}
		return names;
	}

	
//	public List<FeatureType> getFeatureTypes(DataStore ds){

	////////////////////////////////////////////////////////////////////////////
	// COVERAGES
	////////////////////////////////////////////////////////////////////////////

//	public List<CoverageStore> getCoverageStores(){
//	public List<Coverage> getCoverages(CoverageStore cs){

}
