/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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



package it.geosolutions.geobatch.gwc;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.jettison.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Response;


/**
 * 
 * Public class to insert layer configuration file into GeoWebCache 
 *  
 */
public class GWCConfigurator extends  GeoWebCacheConfiguratorAction<FileSystemMonitorEvent>{
	

    protected GWCConfigurator(GeoWebCacheActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
		
        try {
        	
            if (configuration == null) {
                throw new IllegalStateException("ActionConfig is null.");
            }

            // ///////////////////////////////////
            // Initializing input variables
            // ///////////////////////////////////
            
            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ///////////////////////////////////
            // Checking input files.
            // ///////////////////////////////////
            
            if (workingDir == null) {
                LOGGER.log(Level.SEVERE, "Working directory is null.");
                throw new IllegalStateException("Working directory is null.");
            }

            if ( !workingDir.exists() || !workingDir.isDirectory()) {
                LOGGER.log(Level.SEVERE, "Working directory does not exist ("+workingDir.getAbsolutePath()+").");
                throw new IllegalStateException("Working directory does not exist ("+workingDir.getAbsolutePath()+").");
            }
            
			File[] dataList;
			dataList = handleDataFile(events);

			if(dataList == null)
				throw new Exception("Error while processing the layer data file set");
			
			// /////////////////////////////////////////////
			// Look for the main netcdf file in the set
			// /////////////////////////////////////////////
			
			File dataFile = null;
			for (File file : dataList) {
				if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
					dataFile = file;
					break;
				}
			}

			if(dataFile == null) {
                LOGGER.log(Level.SEVERE, "layer data file not found in fileset.");
                throw new IllegalStateException("layer data file not found in fileset.");
			}
            
			String dataString = IOUtils.toString(dataFile);
			String[] layerData = dataString.split("&");
			
			String namespace = layerData[0].split("=")[1];
			String store = layerData[1].split("=")[1];
		    String wmsLayerName = layerData[2].split("=")[1];
			
			String json = getGSLayerData(namespace, store, wmsLayerName, configuration.getGeoserverUrl());
	        
	        JSONObject obj = new JSONObject(json);
	        String layerSRS = "EPSG:4326";
	        String minX = "";
	        String minY = "";
	        String maxX = "";
	        String maxY = "";	        
			
	        Object features = obj.get("coverage");
	        if(features instanceof JSONObject){
	        	JSONObject object = (JSONObject)features;
	        	layerSRS = (String)object.get("srs");
	        	
	        	JSONObject envelope = (JSONObject)object.get("nativeBoundingBox");
		        minX = envelope.getString("minx");
		        minY = envelope.getString("miny");
		        maxX = envelope.getString("maxx");
		        maxY = envelope.getString("maxy");
	        }
	        
	        layerSRS = layerSRS.split(":")[1];
	        
	 	 	SimpleDateFormat simple_date = new SimpleDateFormat("yyMMddHHmmss");	 	
	 	 	Date date = new Date();	
	 	 	String layerName = wmsLayerName + "_" + simple_date.format(date);
	        
	        StringBuilder bf = buildGWCLayerConfiguration(layerSRS, minX, minY, maxX, maxY, configuration.getGeoserverUrl(), 
	        		namespace, wmsLayerName, layerName); 
	        
	        sendLayerConfiguration(bf, configuration.getGwcUrl(), configuration.getGwcUser(),
	        		configuration.getGwcPassword(), namespace, layerName);
			
        	return events;        	
        	
        } catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        } finally {
        	
		}
    }    
	
	/**
	 * Pack the files received in the events into an array.
	 * 
	 *
	 * @param events The received event queue
	 * @return
	 */
	private File[] handleDataFile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
	}

	
	private String getGSLayerData(final String namespace, final String store, final String layerName, final String geoserverUrl) throws Exception{
    	try{
    		if(namespace != null && layerName != null){ 	
    	    	
    	    	// //////////////
    	        // Prepare HTTP client connector.
    	    	// //////////////
    	    	
    	        Client client = new Client(Protocol.HTTP);     	        
    	        Response response = client
    	        	.get(geoserverUrl + "rest/workspaces/" + namespace + "/coveragestores/" + store + "/coverages/" 
        	        		+ layerName + ".json");
    	        
    	        if (response.getStatus().isSuccess()) {
    	        	LOGGER.log(Level.INFO, "Client succes !");
    	        	return response.getEntity().getText();
    	        }else {    	        	
    	        	throw new Exception("Client failure! an unexpected status was returned: "+ response.getStatus());
    	        }   	

    		}else throw new Exception("Client failure! buffer is null");
    	}catch(Exception e){
    		throw new Exception("EXCEPTION -> " + e.getLocalizedMessage());
    	}
    }
    
	private StringBuilder buildGWCLayerConfiguration(String srs, String minX, String minY, String maxX, 
			String maxY, String geoserverUrl, String namespace, String wmsLayerName, String layerName){
		
        StringBuilder sb = new StringBuilder();
        
        sb.append("<wmsLayer><name>");
		sb.append(namespace + ":" + layerName);
		sb.append("</name>");
		sb.append("<mimeFormats><string>image/png</string><string>image/jpeg</string></mimeFormats>");		  
		sb.append("<grids>");
		sb.append("<entry>");
		sb.append("<srs><number>" + srs + "</number></srs>");
		sb.append("<grid>");
		sb.append("<srs><number>" + srs + "</number></srs>");
		sb.append("<dataBounds>");
		sb.append("<coords>");
		sb.append("<double>"+minX+"</double>");
		sb.append("<double>"+minY+"</double>");
		sb.append("<double>"+maxX+"</double>");
		sb.append("<double>"+maxY+"</double>");
		sb.append("</coords>");
		sb.append("</dataBounds>");
		sb.append("<gridBounds>");
		sb.append("<coords>");
		sb.append("<double>"+minX+"</double>");
		sb.append("<double>"+minY+"</double>");
		sb.append("<double>"+maxX+"</double>");
		sb.append("<double>"+maxY+"</double>");
		sb.append("</coords>");
		sb.append("</gridBounds>");
		sb.append("<zoomStart>"+configuration.getZoomStart()+"</zoomStart>");
		sb.append("<zoomStop>"+configuration.getZoomStop()+"</zoomStop>");
		sb.append("</grid>");
		sb.append("</entry>");
		sb.append("</grids>");
		  
		sb.append("<wmsUrl>");
		sb.append("<string>" + geoserverUrl + "wms" + "</string>");
		sb.append("<string>" + geoserverUrl + "wms" + "</string>");
		sb.append("</wmsUrl>");
		sb.append("<wmsLayers>" + namespace + ":" + wmsLayerName + "</wmsLayers>");
		sb.append("<wmsStyles></wmsStyles>");
		sb.append("<metaWidthHeight>");
		sb.append("<int>"+configuration.getMetaWidth()+"</int>");
		sb.append("<int>"+configuration.getMetaHeight()+"</int>");
		sb.append("</metaWidthHeight>");
		sb.append("<gutter>"+configuration.getGutter()+"</gutter>");
		sb.append("<tiled>"+configuration.getTiled()+"</tiled>");
		sb.append("<transparent>"+configuration.getTransparent()+"</transparent>");
		sb.append("<bgColor></bgColor>");
		sb.append("<palette></palette>");
		sb.append("<expireCache>"+configuration.getExpireCache()+"</expireCache>");
		sb.append("<expireClients>"+configuration.getExpireClients()+"</expireClients>");
		sb.append("</wmsLayer>");

        return sb;
	} 
	
	private boolean  sendLayerConfiguration(final StringBuilder buffer, final String gwcUrl, 
			final String gwcUser, final String gwcPassword, final String namespace, final String layerName) throws Exception{

        boolean res = false;

        try {

        	final URL url = new URL(gwcUrl + namespace + ":" +layerName + ".xml");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "text/xml");

            final String login = gwcUser;
            final String password = gwcPassword;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            OutputStreamWriter outReq = new OutputStreamWriter(con.getOutputStream());
            outReq.write(buffer.toString());
            outReq.flush();
            outReq.close();

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = IOUtils.toString(is);
                is.close();
                LOGGER.info("HTTP OK: " + response);
                res = true;
            } else if (responseCode == HttpURLConnection.HTTP_CREATED){
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response =  IOUtils.toString(is);
                is.close();
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE,"HTTP CREATED: " + response);
                else{
                    final String name = extractName(response);
                    LOGGER.info("HTTP CREATED: " + name);
                }
                res = true;
            } else {
                LOGGER.info("HTTP ERROR: " + con.getResponseMessage());
                res = false;
            }
        } catch (MalformedURLException e) {
            LOGGER.info("HTTP ERROR: " + e.getLocalizedMessage());
            res = false;
        } catch (IOException e) {
            LOGGER.info("HTTP ERROR: " + e.getLocalizedMessage());
            res = false;
        }
        
        return res;
    }
	
    
    private static String extractName(final String response) {
        String name ="";
        if (response!=null && response.trim().length()>0){
            final int indexOfNameStart = response.indexOf("<name>");
            final int indexOfNameEnd = response.indexOf("</name>");
            name = response.substring(indexOfNameStart+6, indexOfNameEnd);
        }
        return name;
    }
}





