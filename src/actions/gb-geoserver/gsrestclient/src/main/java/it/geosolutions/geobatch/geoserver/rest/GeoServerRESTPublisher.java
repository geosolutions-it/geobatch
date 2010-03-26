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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Connect to a GeoServer instance for publishing or modify data .
 *
 * @author ETj <etj at geo-solutions.it>
 */

public class GeoServerRESTPublisher {

    private static final Logger LOGGER = Logger.getLogger(GeoServerRESTPublisher.class);
	private final String restURL;
	private final String gsuser;
	private final String gspass;

	public GeoServerRESTPublisher(String restURL, String username, String pw) {
		this.restURL = restURL;
		this.gsuser = username;
		this.gspass = pw;
	}

	public boolean publishFT(String dsName, String ftName, String xmlFeatureType) {

		LOGGER.info("GeoserverPublisher::publish('" + ftName + "') : start");

		try {			
			URL dssUrl = new URL(restURL + "/rest/folders/" + dsName +
									"/layers/"+ftName+".xml");

			LOGGER.info("### Putting FT into " + dssUrl.toExternalForm() + " (" + dssUrl + ")");
			LOGGER.info("### Feature Type: "+ xmlFeatureType);
						
			if( HTTPUtils.put(dssUrl, xmlFeatureType, gsuser, gspass))
				return true;
			else {
				LOGGER.warn("Could not publish layer " + ftName);
				return false;
			}

		} catch(MalformedURLException e) {
			LOGGER.warn("Could not publish layer " + ftName , e);
			return false;
		} catch(IOException e) {
			LOGGER.warn("Could not publish layer " + ftName , e);
			return false;
		}
	}

	public void unpublish(String ftName) {
		LOGGER.error("GeoserverPublisher::unpublish('" + ftName + "') : TODO"); // TODO
	}

	public void createClassifiedSLD(String ftName, String styleName){
		
		LOGGER.info("GeoserverPublisher::createClassifiedSLD('" + ftName + "') : start");
		
		/**
		 * nella url richiesta va inserito userStyleID e featureType
		 * Va creata la stringa con i parametri da passere a sldService:
		 *  
		 *  classMethod = "unique", "equalInterval", "quantile"
		 *  property = nome della property (field name in table/view) su cui eseguire classificazione
		 *  classNum = optional numero delle classi che voglio generare default 4
         *  colorRamp = tipo di colorRamp che voglio generare valid value = red blue gray random custom 
         *  startColor = required se custom colorRam è stata scelta;
         *  endColor = required se custom colorRam è stata scelta;
         *  midColor = optional;
         *  RulesBuilder ruBuild;
         *  String userStyleId = null;
         *  String featureTypeName = null;
		 * 
		 */
		StringBuilder szString = new StringBuilder();
		szString.append("classMethod=quantile");
		szString.append("&property=quantity");
		szString.append("&classNum=10");
		szString.append("&colorRamp=red");
		
		try {
			URL dssUrl = new URL(restURL + "/rest/sldservice/" +ftName+"/styles/"+styleName);
			LOGGER.info("### Putting FT into " + dssUrl.toExternalForm() + " (" + dssUrl + ")");
			LOGGER.info("### Feature Type: "+ szString);

			HTTPUtils.put(dssUrl, szString.toString(), gsuser, gspass);

		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public boolean publishStyle(String styleName, String sldBody) {
		try {
			URL dssUrl = new URL(restURL + "/rest/styles/" + styleName.replaceAll(":", "_"));
			return HTTPUtils.put(dssUrl, sldBody, gsuser, gspass);
		} catch (IOException ex) {
			LOGGER.warn("Could not publish style '"+styleName+"'", ex);
			return false;
		}
	}

	public void createStyleForLayer(String styleName, String sldBody, String layername) {
		try {
			final String gsURL = /* geoserver.getBaseUrl() */ restURL.substring(0, restURL.lastIndexOf("/"));
			String rstyleName = styleName.replaceAll(":", "_");
			URL dssUrl = new URL(gsURL + "/rest/styles/" + rstyleName);
			if ( HTTPUtils.put(dssUrl, sldBody, gsuser, gspass)) {
				LOGGER.info("Created style '"+rstyleName+"' for layer '"+layername+"'");
				//final String featureTypeName = styleName.substring(0, styleName.lastIndexOf("_"));
				dssUrl = new URL(gsURL + "/rest/sldservice/updateLayer/" + layername);
				if( HTTPUtils.put(dssUrl, "<LayerConfig><Style>" + rstyleName + "</Style></LayerConfig>",
						gsuser, gspass)) {
					LOGGER.info("Added new style '"+rstyleName+"' to layer '"+layername+"'");
				} else
					LOGGER.warn("Could not add style '"+rstyleName+"' to layer '"+layername+"'");
			} else
				LOGGER.warn("Could not create style '"+rstyleName+"' for layer '"+layername+"'");
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public boolean setDefaultStyle(String layerName, String styleName) {
		try {
			URL dssUrl = new URL(restURL + "/rest/sldservice/updateLayer/" + layerName);

			if(HTTPUtils.put(dssUrl, "<LayerConfig><DefaultStyle>" + styleName + "</DefaultStyle></LayerConfig>",
							gsuser, gspass)) {
				return true;
			} else {
				LOGGER.warn("Could not set style " + styleName + " for layer "+ layerName);
				return false;
			}
		} catch (MalformedURLException e) {
			LOGGER.warn("Could not set style " + styleName + " for layer "+ layerName, e);
			return false;
		} catch (IOException e) {
			LOGGER.warn("Could not set style " + styleName + " for layer "+ layerName, e);
			return false;
		}
	}


 	public boolean publishShp(String storename, String layername, File zipFile) throws FileNotFoundException {
		try {
			URL url = new URL("/rest/folders/" + storename + "/layers/" + layername + "/file.shp?" + "namespace=fenix" + "&SRS=4326&SRSHandling=Force"); // hack
			boolean sent = HTTPUtils.put(url, new FileInputStream(zipFile), gsuser, gspass);
			return sent;
		} catch (MalformedURLException ex) {
			LOGGER.error(ex);
			return false;
		}
 	}

 	public boolean publishExternalGeoTIFF(String storeName, String layerName, File geotiff) throws FileNotFoundException {
		try {
			URL url = new URL(restURL + "/rest/folders/" + storeName + "/layers/" + layerName + "/external.geotiff");
			InputStream is = new FileInputStream(geotiff);
			boolean sent = HTTPUtils.put(url, is, gsuser, gspass);
			return sent;
		} catch (MalformedURLException ex) {
			LOGGER.error(ex);
			return false;
		}
	}

//	/** TODO */
// 	public boolean unpublishExternalGeoTIFF(String storeName, String layerName, File geotiff) throws FileNotFoundException {
//		try {
//			URL url = new URL(restURL + "/rest/folders/" + storeName + "/layers/" + layerName);
//			boolean sent = HTTPUtils.put(url, geotiff.toURL().toExternalForm(), gsuser, gspass);
//			return sent;
//		} catch (MalformedURLException ex) {
//			LOGGER.error(ex);
//			return false;
//		}
//	}

 	public boolean unpublishLayer(String storename, String layername) {
		try {
			URL url = new URL("/rest/folders/" + storename + "/layers/" + layername ); 
			return HTTPUtils.delete(url.toExternalForm(), gsuser, gspass);
		} catch (MalformedURLException ex) {
			LOGGER.error(ex);
			return false;
		}
 	}

}
