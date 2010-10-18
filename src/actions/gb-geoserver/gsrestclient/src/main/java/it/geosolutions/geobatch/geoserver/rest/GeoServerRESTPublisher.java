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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Connect to a GeoServer instance for publishing or modify data.
 * <P>
 * There are no modifiable instance fields, so all the calls are thread-safe.
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class GeoServerRESTPublisher {

	private static final Logger LOGGER = Logger
			.getLogger(GeoServerRESTPublisher.class);
	private final String restURL;
	private final String gsuser;
	private final String gspass;

	public GeoServerRESTPublisher(String restURL, String username, String pw) {
		this.restURL = restURL;
		this.gsuser = username;
		this.gspass = pw;
	}

	// public boolean publishFT(String dsName, String ftName, String
	// xmlFeatureType) {
	//
	// LOGGER.info("GeoserverPublisher::publish('" + ftName + "') : start");
	//
	// String sUrl = restURL + "/rest/folders/" + dsName + "/layers/" + ftName +
	// ".xml";
	//
	// if (HTTPUtils.putXml(sUrl, xmlFeatureType, gsuser, gspass)) {
	// return true;
	// } else {
	// LOGGER.warn("Could not publish layer " + ftName);
	// return false;
	// }
	// }

	// public void unpublish(String ftName) {
	// LOGGER.error("GeoserverPublisher::unpublish('" + ftName + "') : TODO");
	// // TODO
	// }

	// public boolean createClassifiedSLD(String ftName, String styleName) {
	//
	// LOGGER.info("GeoserverPublisher::createClassifiedSLD('" + ftName +
	// "') : start");
	//
	// /**
	// * nella url richiesta va inserito userStyleID e featureType
	// * Va creata la stringa con i parametri da passere a sldService:
	// *
	// * classMethod = "unique", "equalInterval", "quantile"
	// * property = nome della property (field name in table/view) su cui
	// eseguire classificazione
	// * classNum = optional numero delle classi che voglio generare default 4
	// * colorRamp = tipo di colorRamp che voglio generare valid value = red
	// blue gray random custom
	// * startColor = required se custom colorRam è stata scelta;
	// * endColor = required se custom colorRam è stata scelta;
	// * midColor = optional;
	// * RulesBuilder ruBuild;
	// * String userStyleId = null;
	// * String featureTypeName = null;
	// *
	// */
	// StringBuilder classDefinition = new
	// StringBuilder("&").append("classMethod=quantile").append("&property=quantity").append("&classNum=10").append("&colorRamp=red");
	//
	// String sUrl = restURL + "/rest/sldservice/" + ftName + "/styles/" +
	// styleName + classDefinition;
	// return HTTPUtils.put(sUrl, null, gsuser, gspass);
	// }

	/**
	 * Will take the name from sld contents
	 * 
	 * <TT>curl -u admin:geoserver -XPOST
	 *    <BR>&nbsp;&nbsp;&nbsp;  -H 'Content-type: application/vnd.ogc.sld+xml'
	 *    <BR>&nbsp;&nbsp;&nbsp;  -d @$FULLSLD
     *    <BR>&nbsp;&nbsp;&nbsp;  http://$GSIP:$GSPORT/$SERVLET/rest/styles
	 * 
	 * @param styleName
	 * @param sldBody
	 * @return
	 */
	public boolean publishStyle(String sldBody) {
		String sUrl = restURL + "/rest/styles";
		return HTTPUtils.post(sUrl, sldBody, "application/vnd.ogc.sld+xml",
				gsuser, gspass);
	}

	public boolean publishStyle(File sldFile) {
		String sUrl = restURL + "/rest/styles/";
		return HTTPUtils.post(sUrl, sldFile, "application/vnd.ogc.sld+xml",
				gsuser, gspass);
	}

	public boolean removeStyle(String styleName) {
		styleName = styleName.replaceAll(":", "_");
		String sUrl = restURL + "/rest/styles/" + styleName + "?purge=true";
		return HTTPUtils.delete(sUrl, gsuser, gspass);
	}

	/**
	 * 
	 * @param styleName
	 * @param sldBody
	 * @param layername
	 * @return true if the operation completed successfully.
	 */
	// public boolean createStyleForLayer(String styleName, String sldBody,
	// String layername) {
	// final String rstyleName = styleName.replaceAll(":", "_");
	// String sUrl = restURL + "/rest/styles/" + rstyleName;
	// if (HTTPUtils.putXml(sUrl, sldBody, gsuser, gspass)) {
	// LOGGER.info("Created style '" + rstyleName + "' for layer '" + layername
	// + "'");
	// //final String featureTypeName = styleName.substring(0,
	// styleName.lastIndexOf("_"));
	// sUrl = restURL + "/rest/sldservice/updateLayer/" + layername;
	// String content = "<LayerConfig><Style>" + rstyleName +
	// "</Style></LayerConfig>";
	// if (HTTPUtils.putXml(sUrl, content, gsuser, gspass)) {
	// LOGGER.info("Added new style '" + rstyleName + "' to layer '" + layername
	// + "'");
	// return true;
	// } else {
	// LOGGER.warn("Could not add style '" + rstyleName + "' to layer '" +
	// layername + "'");
	// return false;
	// }
	// } else {
	// LOGGER.warn("Could not create style '" + rstyleName + "' for layer '" +
	// layername + "'");
	// return false;
	// }
	// }

	/**
	 * 
	 * @param layerName
	 * @param styleName
	 * @return true if the operation completed successfully.
	 */
	// public boolean setDefaultStyle(String layerName, String styleName) {
	// String sUrl = restURL + "/rest/sldservice/updateLayer/" + layerName;
	// String content = "<LayerConfig><DefaultStyle>" + styleName +
	// "</DefaultStyle></LayerConfig>";
	//
	// if (HTTPUtils.putXml(sUrl, content, gsuser, gspass)) {
	// return true;
	// } else {
	// LOGGER.warn("Could not set style " + styleName + " for layer " +
	// layerName);
	// return false;
	// }
	// }

	/**
	 * Publish a zipped shapefile with native CRS EPSG:4326.
	 * 
	 * @param workspace
	 * @param storename
	 * @param layername
	 * @param zipFile
	 * @return true if the operation completed successfully.
	 * @throws FileNotFoundException
	 */
	public boolean publishShp(String workspace, String storename,
			String layername, File zipFile) throws FileNotFoundException {
		return publishShp(workspace, storename, layername, zipFile, "EPSG:4326");
	}

	/**
	 * Publish a zipped shapefile with a defaultStyle.
	 * 
	 * @param workspace
	 * @param storename
	 * @param layerName
	 * @param nativeCrs
	 * @param defaultStyle
	 * @return true if the operation completed successfully.
	 * @throws FileNotFoundException
	 */
	public boolean publishShp(String workspace, String storename,
			String layerName, File zipFile, String nativeCrs,
			String defaultStyle) throws FileNotFoundException {
		boolean sent = publishShp(workspace, storename, layerName, zipFile,
				nativeCrs);
		if (sent && defaultStyle != null) {
			final Map<String, String> queryParams = new HashMap<String, String>();

			try {
				configureLayer(queryParams, defaultStyle, restURL, gsuser,
						gspass, layerName);
			} catch (Exception e) {
				sent = false;
			}
		}

		return sent;
	}

	/**
	 * <BR>
	 * <TT>curl -u admin:geoserver -XPUT -H 'Content-type: application/zip' \ <BR>
	 *      &nbsp;&nbsp;&nbsp;--data-binary @$ZIPFILE \<BR>
	 *      &nbsp;&nbsp;&nbsp;http://$GSIP:$GSPORT/$SERVLET/rest/workspaces/$WORKSPACE/datastores/$BARE/file.shp</TT> <BR>
	 * <BR>
	 * <TT>
	 * curl -u admin:geoserver -XPOST -H 'Content-type: text/xml'  \ <BR>
	 *      &nbsp;&nbsp;&nbsp;-d "<featureType><name>$BARE</name><nativeCRS>EPSG:4326</nativeCRS><enabled>true</enabled></featureType>"  \ <BR>
	 *      &nbsp;&nbsp;&nbsp;http://$GSIP:$GSPORT/$SERVLET/rest/workspaces/$WORKSPACE/datastores/$BARE/featuretypes/$BARE
     * </TT>
	 * 
	 * @return true if the operation completed successfully.
	 */
	public boolean publishShp(String workspace, String storename,
			String layername, File zipFile, String nativeCrs)
			throws FileNotFoundException {
		// build full URL
		StringBuilder sbUrl = new StringBuilder(restURL.toString()).append(
				"/rest/workspaces/").append(workspace).append("/datastores/")
				.append(storename).append("/file.shp?");
		// if (workspace != null) {
		// sbUrl.append("namespace=").append(workspace);
		// }
		// sbUrl.append("&SRS=4326&SRSHandling=Force"); // hack

		boolean shpSent = HTTPUtils.put(sbUrl.toString(), zipFile,
				"application/zip", gsuser, gspass);

		if (shpSent) {
			// StringBuilder postMsg = new StringBuilder("<featureType>")
			// .append("<name>").append(layername).append("</name>")
			// .append("<nativeCRS>").append(nativeCrs).append("</nativeCRS>")
			// .append("<enabled>true</enabled></featureType>");
			//
			// //
			// http://$GSIP:$GSPORT/$SERVLET/rest/workspaces/$WORKSPACE/datastores/$BARE/featuretypes/$BARE
			// StringBuilder postUrl = new StringBuilder(restURL.toString())
			// .append("/rest/workspaces/").append(workspace)
			// .append("/datastores/").append(storename)
			// .append("/featuretypes/").append(layername);
			//
			// boolean shpConfigured = HTTPUtils.postXml(postUrl.toString(),
			// postMsg.toString(), this.gsuser, this.gspass);
			//
			// if ( ! shpConfigured) {
			// LOGGER.warn("Error in configuring " + workspace + ":" + storename
			// + "/" + layername + " -- Zipfile was uploaded successfully: " +
			// zipFile);
			// }
			//
			// return shpConfigured;

		} else {
			LOGGER.warn("Error in sending zipfile " + workspace + ":"
					+ storename + "/" + layername + " " + zipFile);
			return false;
		}

		return shpSent;
	}

	/**
	 * <TT>curl -u admin:geoserver -XPUT -H 'Content-type: text' -d "file:$FULLPATH" http://$GSIP:$GSPORT/$SERVLET/rest/workspaces/$WORKSPACE/coveragestores/$BARENAME/external.geotiff</TT>
	 * 
	 * @return true if the operation completed successfully.
	 * @deprecated UNTESTED
	 */

	public boolean publishGeoTIFF(String workspace, String storeName,
			File geotiff) throws FileNotFoundException {
		String sUrl = restURL + "/rest/workspaces/" + workspace
				+ "/coveragestores/" + storeName + "/external.geotiff";
		boolean sent = HTTPUtils.put(sUrl, geotiff, "text", gsuser, gspass);
		return sent;
	}

	/**
	 * 
	 * @param workspace
	 * @param layerName
	 * @param geotiff
	 * @return true if the operation completed successfully.
	 * @throws FileNotFoundException
	 */
	public boolean publishExternalGeoTIFF(String workspace, String layerName,
			File geotiff) throws FileNotFoundException {
		String sUrl = restURL + "/rest/workspaces/" + workspace
				+ "/coveragestores/" + layerName + "/external.geotiff";
		boolean sent = HTTPUtils.put(sUrl, geotiff.toURI().toString(),
				"text/plain", gsuser, gspass);
		return sent;
	}

	/**
	 * 
	 * @param workspace
	 * @param layerName
	 * @param geotiff
	 * @param defaultStyle
	 * @return true if the operation completed successfully.
	 * @throws FileNotFoundException
	 */
	public boolean publishExternalGeoTIFF(String workspace, String layerName,
			File geotiff, String defaultStyle) throws FileNotFoundException {
		boolean sent = publishExternalGeoTIFF(workspace, layerName, geotiff);
		if (sent && defaultStyle != null) {
			final Map<String, String> queryParams = new HashMap<String, String>();

			try {
				configureLayer(queryParams, defaultStyle, restURL, gsuser,
						gspass, layerName);
			} catch (Exception e) {
				sent = false;
			}
		}

		return sent;
	}

	/**
	 * <P>
	 * Sample cUrl usage:<BR>
	 * <TT>curl -u admin:geoserver -XPUT -H 'Content-type: text' -d "file:$ABSPORTDIR"
	 *          http://$GSIP:$GSPORT/$SERVLET/rest/workspaces/$WORKSPACE/coveragestores/$BAREDIR/external.imagemosaic </TT>
	 * 
	 * @param workspace
	 * @param layerName
	 * @param mosaicDir
	 * @return true if the operation completed successfully.
	 * @throws FileNotFoundException
	 * @deprecated UNTESTED
	 */
	public boolean publishExternalMosaic(String workspace, String layerName,
			File mosaicDir) throws FileNotFoundException {
		if (!mosaicDir.isDirectory())
			throw new IllegalArgumentException("Not a directory '" + mosaicDir
					+ "'");
		String sUrl = restURL + "/rest/workspaces/" + workspace
				+ "/coveragestores/" + layerName + "/external.imagemosaic";
		boolean sent = HTTPUtils.put(sUrl, mosaicDir.toURI().toString(),
				"text/plain", gsuser, gspass);
		return sent;
	}

	/**
	 * <P>
	 * Sample cUrl usage:<BR>
	 * <TT>curl -u admin:geoserver -XPUT -H 'Content-type: text' -d "file:$ABSPORTDIR"
	 *          http://$GSIP:$GSPORT/$SERVLET/rest/workspaces/$WORKSPACE/coveragestores/$BAREDIR/external.imagemosaic </TT>
	 * 
	 * @param workspace
	 * @param layerName
	 * @param mosaicDir
	 * @param defaultStyle
	 * @return true if the operation completed successfully.
	 * @throws FileNotFoundException
	 * @deprecated UNTESTED
	 */
	public boolean publishExternalMosaic(String workspace, String layerName,
			File mosaicDir, String defaultStyle) throws FileNotFoundException {
		boolean sent = publishExternalMosaic(workspace, layerName, mosaicDir);
		if (sent && defaultStyle != null) {
			final Map<String, String> queryParams = new HashMap<String, String>();

			try {
				configureLayer(queryParams, defaultStyle, restURL, gsuser,
						gspass, layerName);
			} catch (Exception e) {
				sent = false;
			}
		}

		return sent;
	}

	/**
	 * Remove the Coverage configuration from GeoServer. <BR>
	 * First, the associated layer is removed, then the Coverage configuration
	 * itself.
	 * <P>
	 * <B>CHECKME</B> Maybe the coveragestore has to be removed as well.
	 * 
	 * <P>
	 * REST URL:
	 * <TT>http://localhost:8080/geoserver/rest/workspaces/it.geosolutions/coveragestores/gbRESTtestStore/coverages/resttestdem.xml</TT>
	 * 
	 * @return true if the operation completed successfully.
	 */
	public boolean unpublishCoverage(String workspace, String storename,
			String layername) {
		try {
			// delete related layer
			URL deleteLayerUrl = new URL(restURL + "/rest/layers/" + layername);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Going to delete " + "/rest/layers/" + layername);
			boolean layerDeleted = HTTPUtils.delete(deleteLayerUrl
					.toExternalForm(), gsuser, gspass);
			if (!layerDeleted) {
				LOGGER.warn("Could not delete layer '" + layername + "'");
				return false;
			}
			// delete the coverage
			URL deleteCovUrl = new URL(restURL + "/rest/workspaces/"
					+ workspace + "/coveragestores/" + storename
					+ "/coverages/" + layername);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Going to delete " + "/rest/workspaces/"
						+ workspace + "/coveragestores/" + storename
						+ "/coverages/" + layername);
			boolean covDeleted = HTTPUtils.delete(
					deleteCovUrl.toExternalForm(), gsuser, gspass);
			if (!covDeleted) {
				LOGGER.warn("Could not delete coverage " + workspace + ":"
						+ storename + "/" + layername
						+ ", but layer was deleted.");
			}
			return covDeleted;

			// the covstore is still there: should we delete it?

		} catch (MalformedURLException ex) {
			LOGGER.error(ex);
			return false;
		}
	}

	/**
	 * Removes the featuretype and the associated layer. <BR>
	 * You may also want to {@link removeDatastore(String, String) remove the
	 * datastore}.
	 * 
	 * @return true if the operation completed successfully.
	 */
	public boolean unpublishFeatureType(String workspace, String storename,
			String layername) {
		try {
			// delete related layer
			URL deleteLayerUrl = new URL(restURL + "/rest/layers/" + layername);
			boolean layerDeleted = HTTPUtils.delete(deleteLayerUrl
					.toExternalForm(), gsuser, gspass);
			if (!layerDeleted) {
				LOGGER.warn("Could not delete layer '" + layername + "'");
				return false;
			}
			// delete the coverage
			URL deleteFtUrl = new URL(restURL + "/rest/workspaces/" + workspace
					+ "/datastores/" + storename + "/featuretypes/" + layername);
			boolean ftDeleted = HTTPUtils.delete(deleteFtUrl.toExternalForm(),
					gsuser, gspass);
			if (!ftDeleted) {
				LOGGER.warn("Could not delete featuretype " + workspace + ":"
						+ storename + "/" + layername
						+ ", but layer was deleted.");
			}
			return ftDeleted;

			// the store is still there: should we delete it?

		} catch (MalformedURLException ex) {
			LOGGER.error(ex);
			return false;
		}
	}

	public boolean removeDatastore(String workspace, String storename) {
		try {
			URL deleteStore = new URL(restURL + "/rest/workspaces/" + workspace
					+ "/datastores/" + storename);
			boolean deleted = HTTPUtils.delete(deleteStore.toExternalForm(),
					gsuser, gspass);
			if (!deleted) {
				LOGGER.warn("Could not delete datastore " + workspace + ":"
						+ storename);
			}
			return deleted;
		} catch (MalformedURLException ex) {
			LOGGER.error(ex);
			return false;
		}
	}

	/**
	 * @deprecated to be reviewed
	 */
	public static void configureLayer(final Map<String, String> queryParams,
			final String defaultStyle, final String geoserverBaseURL,
			final String geoserverUID, final String geoserverPWD,
			final String layerName) throws ParserConfigurationException,
			IOException, TransformerException {
		Map<String, String> configElements = new HashMap<String, String>(2);
		if (queryParams.containsKey("wmspath")) {
			// Configuring wmsPath
			final String wmsPath = queryParams.get("wmspath");
			configElements.put("path", wmsPath);
		}
		if (defaultStyle != null && defaultStyle.trim().length() > 0) {
			configElements.put("defaultStyle", defaultStyle);
		}
		if (!configElements.isEmpty()) {
			sendLayerConfiguration(configElements, geoserverBaseURL,
					geoserverUID, geoserverPWD, layerName);
		}

	}

	/**
	 * Allows to configure some layer attributes such as WmsPath and
	 * DefaultStyle
	 * 
	 * @param configElements
	 * @param geoserverBaseURL
	 * @param geoserverUID
	 * @param geoserverPWD
	 * @param layerName
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws TransformerException
	 * 
	 * @deprecated to be reviewed
	 */
	private static void sendLayerConfiguration(
			final Map<String, String> configElements,
			final String geoserverBaseURL, final String geoserverUID,
			final String geoserverPWD, final String layerName)
			throws ParserConfigurationException, IOException,
			TransformerException {

		String layer = URLEncoder.encode(layerName, "UTF-8");
		if (layer.contains("."))
			layer = layer + ".fake";
		final URL geoserverREST_URL = new URL(new StringBuilder(
				geoserverBaseURL).append("/rest/layers/").append(layer)
				.toString());
		File file = null;
		FileInputStream inStream = null;
		try {
			file = buildXMLConfiguration(configElements);
			inStream = new FileInputStream(file);
			final boolean send = putBinaryFileTo(geoserverREST_URL, inStream,
					geoserverUID, geoserverPWD, null, "text/xml");
			if (send)
				LOGGER
						.info("GeoServerConfiguratorAction: Layer SUCCESSFULLY configured!");
		} finally {
			if (file != null) {
				try {
					file.delete();
				} catch (Throwable t) {
					// Eat me
				}
			}
			IOUtils.closeQuietly(inStream);
		}

	}

	/**
	 * Setup an XML file to be sent via REST to configure the Layer
	 * 
	 * @param configElements
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws TransformerException
	 * 
	 * @deprecated to be moved in parser package
	 */
	private static File buildXMLConfiguration(
			final Map<String, String> configElements)
			throws ParserConfigurationException, IOException,
			TransformerException {
		final DocumentBuilderFactory dfactory = DocumentBuilderFactory
				.newInstance();

		// Get the DocumentBuilder
		DocumentBuilder parser = dfactory.newDocumentBuilder();
		// Create blank DOM Document
		Document doc = parser.newDocument();
		Element root = doc.createElement("layer");
		doc.appendChild(root);

		Set<String> keys = configElements.keySet();
		Element enabledElement = doc.createElement("enabled");
		root.appendChild(enabledElement);
		enabledElement.insertBefore(doc.createTextNode("true"), null);

		for (String key : keys) {
			final String value = configElements.get(key);
			final Element element = doc.createElement(key);
			root.appendChild(element);
			element.insertBefore(doc.createTextNode(value), null);
		}

		final TransformerFactory factory = TransformerFactory.newInstance();
		final Transformer transformer = factory.newTransformer();
		final File file = File.createTempFile("config", ".xml");
		final Result result = new StreamResult(file);
		final Source xmlSource = new DOMSource(doc);
		transformer.transform(xmlSource, result);
		return file;
	}

	/**
	 * @deprecated use HTTPUtils -- review!!!
	 */
	public static boolean putBinaryFileTo(URL geoserverREST_URL,
			InputStream inputStream, String geoserverUser,
			String geoserverPassword, final String[] returnedLayerName,
			final String contentType) {
		boolean res = false;

		try {
			HttpURLConnection con = (HttpURLConnection) geoserverREST_URL
					.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("PUT");
			if (contentType != null && contentType.trim().length() > 0)
				con.setRequestProperty("Content-Type", contentType);

			final String login = geoserverUser;
			final String password = geoserverPassword;

			if ((login != null) && (login.trim().length() > 0)) {
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(login, password
								.toCharArray());
					}
				});
			}

			OutputStream outputStream = con.getOutputStream();
			IOUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
			outputStream.close();

			final int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStreamReader is = new InputStreamReader(con
						.getInputStream());
				String response = IOUtils.toString(is);
				is.close();
				LOGGER.info("HTTP OK: {" + response + "}");
				res = true;
			} else if (responseCode == HttpURLConnection.HTTP_CREATED) {
				InputStreamReader is = new InputStreamReader(con
						.getInputStream());
				String response = IOUtils.toString(is);
				is.close();
				final String name = extractName(response);
				extractContent(response, returnedLayerName);
				// if (returnedLayerName!=null && returnedLayerName.length>0)
				// returnedLayerName[0]=name;
				if (LOGGER.isEnabledFor(Level.DEBUG))
					LOGGER.debug("HTTP CREATED: {" + response + "}");
				else {
					LOGGER.info("HTTP CREATED: {" + name + "}");
				}
				res = true;
			} else {
				LOGGER.log(Level.INFO, "HTTP ERROR: {"
						+ con.getResponseMessage() + "}");
				res = false;
			}
		} catch (MalformedURLException e) {
			LOGGER.log(Level.INFO, "HTTP ERROR: {" + e.getLocalizedMessage()
					+ "}");
			res = false;
		} catch (IOException e) {
			LOGGER.log(Level.INFO, "HTTP ERROR: {" + e.getLocalizedMessage()
					+ "}");
			res = false;
		}
		return res;

	}

	/**
	 * @deprecated use HTTPUtils
	 * 
	 */
	public static boolean putBinaryFileTo(URL geoserverREST_URL,
			InputStream inputStream, String geoserverUser,
			String geoserverPassword) {
		return putBinaryFileTo(geoserverREST_URL, inputStream, geoserverUser,
				geoserverPassword, null, null);

	}

	/**
	 * 
	 * @deprecated move into parser package
	 */
	private static String extractName(final String response) {
		String name = "";
		if (response != null && response.trim().length() > 0) {
			final int indexOfNameStart = response.indexOf("<name>");
			final int indexOfNameEnd = response.indexOf("</name>");
			try {
				name = response.substring(indexOfNameStart + 6, indexOfNameEnd);
			} catch (StringIndexOutOfBoundsException e) {
				name = response;
			}
		}
		return name;
	}

	/**
	 * 
	 * @param response
	 * @param result
	 *            will contain the following elements: result[0]: the store name
	 *            result[1]: the namespace result[2]: the layername
	 * 
	 * @deprecated move into parser package
	 */
	private static void extractContent(final String response,
			final String[] result) {
		if (response != null && response.trim().length() > 0) {
			final int indexOfName1Start = response.indexOf("<name>");
			final int indexOfName1End = response.indexOf("</name>");
			final int indexOfName2Start = response.indexOf("<name>",
					indexOfName1Start + 1);
			final int indexOfName2End = response.indexOf("</name>",
					indexOfName2Start + 1);
			final int indexOfWorkspaceStart = response.indexOf("<workspace>");
			try {
				if (indexOfName1Start < indexOfWorkspaceStart) {
					result[2] = response.substring(indexOfName1Start + 6,
							indexOfName1End);
					result[1] = response.substring(indexOfName2Start + 6,
							indexOfName2End);
				} else {
					result[1] = response.substring(indexOfName1Start + 6,
							indexOfName1End);
					result[2] = response.substring(indexOfName2Start + 6,
							indexOfName2End);
				}

			} catch (StringIndexOutOfBoundsException e) {

			}
		}
	}

}
