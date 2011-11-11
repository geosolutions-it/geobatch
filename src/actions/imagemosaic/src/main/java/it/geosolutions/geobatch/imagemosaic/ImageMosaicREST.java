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
package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import it.geosolutions.geoserver.rest.encoder.coverage.GSImageMosaicEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.GSDimensionInfoEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.GSDimensionInfoEncoder.Presentation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Encoder;

public abstract class ImageMosaicREST {
    /**
     * Default logger
     */
    protected final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicREST.class);

    /**
     * 
     */
    public final static String GEOSERVER_VERSION = "2.x";

    /**
     * recursively remove ending slashes
     * 
     * @param geoserverURL
     * @return
     */
    protected static String decurtSlash(String geoserverURL) {
        if (geoserverURL.endsWith("/")) {
            geoserverURL = decurtSlash(geoserverURL.substring(0, geoserverURL.length() - 1));
        }
        return geoserverURL;
    }

    /**
     * Create Mosaic Method
     * 
     * @param layers
     * @param inputDir
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    protected static GSImageMosaicEncoder createGSImageMosaicEncoder(ImageMosaicGranulesDescriptor mosaicDescriptor, ImageMosaicConfiguration config) {

	    final GSImageMosaicEncoder coverageEnc=new GSImageMosaicEncoder();

	    coverageEnc.addName(mosaicDescriptor.getCoverageStoreId());
	    coverageEnc.addTitle(mosaicDescriptor.getCoverageStoreId());
	    coverageEnc.addSRS(config.getCrs()!=null?config.getCrs():"");
	    
//	    coverageEnc.setMaxAllowedTiles(config.get) //TODO
	    coverageEnc.addMaxAllowedTiles(Integer.MAX_VALUE);
	    

        final String noData;
        if (mosaicDescriptor.getFileListNameParts() == null) {
            noData = (config.getBackgroundValue() != null) ? config.getBackgroundValue() : "-1.0";
        } else {
            if (mosaicDescriptor.getNoData() != null) {
                noData = mosaicDescriptor.getNoData().toString();
            } else {
                // use default value from configuration?
                noData = (config.getBackgroundValue() != null) ? config.getBackgroundValue()
                        : "-1.0";
            }
        }

        // Actually, the ImageMosaicConfiguration is contained in the
        // flow.xml.
        // therefore, there is no way to set the background values a runtime
        // for the moment, we take the nodata from the file name.
        coverageEnc.setBackgroundValues(noData);// NoData
        
        String param = config.getOutputTransparentColor();
        coverageEnc.setOutputTransparentColor((param != null) ? param : "");
        param = config.getInputTransparentColor();
        coverageEnc.setInputTransparentColor((param != null) ? param : "");
        param = null;

        /*
         * note: setting - AllowMultithreading to true - USE_JAI_IMAGEREAD to true make no sense!
         * Simone on 23 Mar 2011: this check should be done by the user configurator or by GeoServer
         */
        coverageEnc.setAllowMultithreading(config.isAllowMultithreading());

        coverageEnc.setUSE_JAI_IMAGEREAD(config.isUseJaiImageRead());

        if (config.getTileSizeH() < 1 || config.getTileSizeW() < 1) {
        	coverageEnc.setSUGGESTED_TILE_SIZE("256,256");
        } else {
        	coverageEnc.setSUGGESTED_TILE_SIZE(config.getTileSizeH() + "," + config.getTileSizeW());
        }

//        final GSMetadataEncoder<GSDimensionInfoEncoder> metadata=new GSMetadataEncoder<GSDimensionInfoEncoder>();
        
        if (config.getTimeDimEnabled()!=null && config.getTimeDimEnabled().equals("true")){
        	final GSDimensionInfoEncoder timeDimensionInfo=new GSDimensionInfoEncoder(true);
	        final String presentation=config.getTimePresentationMode();
	        if (presentation != null){
	        	if (presentation.equals(Presentation.LIST.toString())){
	    			timeDimensionInfo.setPresentation(Presentation.LIST);
	        	}
	//        	else if (config.getTimePresentationMode().equals(DiscretePresentation.DISCRETE_INTERVAL.toString()))
	//        			timeDimensionInfo.addPresentation(DiscretePresentation.DISCRETE_INTERVAL,config.getDiscreteInterval());
	        	else if (presentation.equals(Presentation.CONTINUOUS_INTERVAL.toString())) {
        			timeDimensionInfo.setPresentation(Presentation.CONTINUOUS_INTERVAL);
	        	}
	        }
	        else {
	            timeDimensionInfo.setPresentation(Presentation.LIST);
	        }
	        coverageEnc.setMetadata("time", timeDimensionInfo);
        }
        else
        	coverageEnc.setMetadata("time", new GSDimensionInfoEncoder());
        
        if (config.getElevDimEnabled()!=null && config.getElevDimEnabled().equals("true")){
        	final GSDimensionInfoEncoder elevationDimensionInfo=new GSDimensionInfoEncoder(true);
	        final String presentation=config.getElevationPresentationMode();
	        if (presentation != null){
	        	if (presentation.equals(Presentation.LIST.toString())){
	    			elevationDimensionInfo.setPresentation(Presentation.LIST);
	        	}
	//        	else if (config.getTimePresentationMode().equals(DiscretePresentation.DISCRETE_INTERVAL.toString()))
	//        			timeDimensionInfo.addPresentation(DiscretePresentation.DISCRETE_INTERVAL,config.getDiscreteInterval());
	        	else if (presentation.equals(Presentation.CONTINUOUS_INTERVAL.toString())) {
        			elevationDimensionInfo.setPresentation(Presentation.CONTINUOUS_INTERVAL);
	        	}
	        }
	        else {
	            elevationDimensionInfo.setPresentation(Presentation.LIST);
	        }
	        coverageEnc.setMetadata("elevation", elevationDimensionInfo);
        }
        else
        	coverageEnc.setMetadata("elevation", new GSDimensionInfoEncoder());
        
//        coverageEnc.addNativeBoundingBox(minx, maxy, maxx, miny, crs)
            // coverageParams.put(GeoServerRESTHelper.NATIVE_MAXX,
            // config.getNativeMaxBoundingBoxX() != null ? config.getNativeMaxBoundingBoxX()
            // .toString() : "180");
            //
            // coverageParams.put(GeoServerRESTHelper.NATIVE_MINX,
            // config.getNativeMinBoundingBoxX() != null ? config.getNativeMinBoundingBoxX()
            // .toString() : "-180");
            //
            // coverageParams.put(GeoServerRESTHelper.NATIVE_MINY,
            // config.getNativeMinBoundingBoxY() != null ? config.getNativeMinBoundingBoxY()
            // .toString() : "-90");
            //
            // coverageParams.put(GeoServerRESTHelper.NATIVE_MAXY,
            // config.getNativeMaxBoundingBoxY() != null ? config.getNativeMaxBoundingBoxY()
            // .toString() : "90");
            /*
             * NONE, REPROJECT_TO_DECLARED, FORCE_DECLARED
             */
        final String proj=config.getProjectionPolicy();
        if (proj != null){
        	if (proj.equalsIgnoreCase(ProjectionPolicy.REPROJECT_TO_DECLARED.toString())){
        		coverageEnc.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);
        	}
        	else if (proj.equalsIgnoreCase(ProjectionPolicy.FORCE_DECLARED.toString())){
        		coverageEnc.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
        	}
        	else if (proj.equalsIgnoreCase(ProjectionPolicy.NONE.toString())){
        		coverageEnc.setProjectionPolicy(ProjectionPolicy.NONE);
        	}
        }
        else {
        	coverageEnc.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);	
        }
        
        coverageEnc.setLatLonBoundingBox(
        		config.getLatLonMinBoundingBoxX() != null ? config.getLatLonMinBoundingBoxX(): -180,
                		config.getLatLonMinBoundingBoxY() != null ? config.getLatLonMinBoundingBoxY() : -90,
        		config.getLatLonMaxBoundingBoxX() != null ? config.getLatLonMaxBoundingBoxX(): 180,
        				config.getLatLonMaxBoundingBoxY() != null ? config.getLatLonMaxBoundingBoxY(): 90,
        		config.getCrs());


        if (LOGGER.isDebugEnabled()){
        	LOGGER.debug("ImageMosaicREST.createGSCoverageEncoder(): Coverage configuration:\n"+coverageEnc.toString());
        }
        
        return coverageEnc;
    }    		
    

    /**
     * Create Mosaic Method
     * 
     * @param layers
     * @param inputDir
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    protected static GSCoverageEncoder createGSCoverageEncoder(final String coverageID, ImageMosaicConfiguration config) {

	    final GSCoverageEncoder coverageEnc=new GSCoverageEncoder();

	    coverageEnc.setName(coverageID);
	    coverageEnc.setTitle(coverageID);
	    coverageEnc.setSRS(config.getCrs()!=null?config.getCrs():"");
	    

//        final GSMetadataEncoder<GSDimensionInfoEncoder> metadata=new GSMetadataEncoder<GSDimensionInfoEncoder>();
        
        if (config.getTimeDimEnabled()!=null && config.getTimeDimEnabled().equals("true")){
        	final GSDimensionInfoEncoder timeDimensionInfo=new GSDimensionInfoEncoder(true);
	        final String presentation=config.getTimePresentationMode();
	        if (presentation != null){
	        	if (presentation.equals(Presentation.LIST.toString())){
	    			timeDimensionInfo.setPresentation(Presentation.LIST);
	        	}
	//        	else if (config.getTimePresentationMode().equals(DiscretePresentation.DISCRETE_INTERVAL.toString()))
	//        			timeDimensionInfo.addPresentation(DiscretePresentation.DISCRETE_INTERVAL,config.getDiscreteInterval());
	        	else if (presentation.equals(Presentation.CONTINUOUS_INTERVAL.toString())) {
        			timeDimensionInfo.setPresentation(Presentation.CONTINUOUS_INTERVAL);
	        	}
	        }
	        else {
	            timeDimensionInfo.setPresentation(Presentation.LIST);
	        }
	        coverageEnc.setMetadata("time", timeDimensionInfo);
        }
        else
        	coverageEnc.setMetadata("time", new GSDimensionInfoEncoder());
        
        if (config.getElevDimEnabled()!=null && config.getElevDimEnabled().equals("true")){
        	final GSDimensionInfoEncoder elevationDimensionInfo=new GSDimensionInfoEncoder(true);
	        final String presentation=config.getElevationPresentationMode();
	        if (presentation != null){
	        	if (presentation.equals(Presentation.LIST.toString())){
	    			elevationDimensionInfo.setPresentation(Presentation.LIST);
	        	}
	//        	else if (config.getTimePresentationMode().equals(DiscretePresentation.DISCRETE_INTERVAL.toString()))
	//        			timeDimensionInfo.setPresentation(DiscretePresentation.DISCRETE_INTERVAL,config.getDiscreteInterval());
	        	else if (presentation.equals(Presentation.CONTINUOUS_INTERVAL.toString())) {
        			elevationDimensionInfo.setPresentation(Presentation.CONTINUOUS_INTERVAL);
	        	}
	        }
	        else {
	            elevationDimensionInfo.setPresentation(Presentation.LIST);
	        }
	        coverageEnc.setMetadata("elevation", elevationDimensionInfo);
        }
        else
        	coverageEnc.setMetadata("elevation", new GSDimensionInfoEncoder());
        
//        coverageEnc.setNativeBoundingBox(minx, maxy, maxx, miny, crs)
            // coverageParams.put(GeoServerRESTHelper.NATIVE_MAXX,
            // config.getNativeMaxBoundingBoxX() != null ? config.getNativeMaxBoundingBoxX()
            // .toString() : "180");
            //
            // coverageParams.put(GeoServerRESTHelper.NATIVE_MINX,
            // config.getNativeMinBoundingBoxX() != null ? config.getNativeMinBoundingBoxX()
            // .toString() : "-180");
            //
            // coverageParams.put(GeoServerRESTHelper.NATIVE_MINY,
            // config.getNativeMinBoundingBoxY() != null ? config.getNativeMinBoundingBoxY()
            // .toString() : "-90");
            //
            // coverageParams.put(GeoServerRESTHelper.NATIVE_MAXY,
            // config.getNativeMaxBoundingBoxY() != null ? config.getNativeMaxBoundingBoxY()
            // .toString() : "90");
            /*
             * NONE, REPROJECT_TO_DECLARED, FORCE_DECLARED
             */
        final String proj=config.getProjectionPolicy();
        if (proj != null){
        	if (proj.equalsIgnoreCase(ProjectionPolicy.REPROJECT_TO_DECLARED.toString())){
        		coverageEnc.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);
        	}
        	else if (proj.equalsIgnoreCase(ProjectionPolicy.FORCE_DECLARED.toString())){
        		coverageEnc.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
        	}
        	else if (proj.equalsIgnoreCase(ProjectionPolicy.NONE.toString())){
        		coverageEnc.setProjectionPolicy(ProjectionPolicy.NONE);
        	}
        }
        else {
        	coverageEnc.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);	
        }
        
        coverageEnc.setLatLonBoundingBox(
        		config.getLatLonMinBoundingBoxX() != null ? config.getLatLonMinBoundingBoxX(): -180,
                		config.getLatLonMinBoundingBoxY() != null ? config.getLatLonMinBoundingBoxY() : -90,
        		config.getLatLonMaxBoundingBoxX() != null ? config.getLatLonMaxBoundingBoxX(): 180,
        				config.getLatLonMaxBoundingBoxY() != null ? config.getLatLonMaxBoundingBoxY(): 90,
        		config.getCrs());


        if (LOGGER.isDebugEnabled()){
        	LOGGER.debug("ImageMosaicREST.createGSCoverageEncoder(): Coverage configuration:\n"+coverageEnc.toString());
        }
        
        return coverageEnc;
    }    		

    
    /**
     * @throws MalformedURLException
     * @deprecated will be removed soon (use GS REST manager) 
     */
    public static boolean reloadCatalog(ImageMosaicConfiguration conf) throws MalformedURLException {
        boolean ret = true;
        URL geoserverREST_URL = new URL(decurtSlash(conf.getGeoserverURL()) + "/rest/reload");

        LOGGER.info("ImageMosaicREST::reloadCatalog():postTextFileTo URL: "
                + geoserverREST_URL.toString());

        if (GeoServerRESTHelper.postTextFileTo(geoserverREST_URL, null, conf.getGeoserverUID(),
                conf.getGeoserverPWD(), null)) {
            LOGGER.info("ImageMosaicREST::reloadCatalog(): GeoServer Catalog successfully reloaded!");
        } else {
            LOGGER.warn("ImageMosaicREST::reloadCatalog(): Error occurred while trying to reload GeoServer Catalog!");
            ret = false;
        }
        return ret;
    }

    /**
     * Perform a reset of the GeoServer cached reader
     * 
     * @param geoserverURL
     *            the url to GeoServer
     * @param user
     * @param passwd
     * @return true if SUCCESS
     * @deprecated will be removed soon (use GS REST manager)
     */
    protected static boolean resetGeoserver(final String geoserverURL, final String user,
            final String passwd) {
        DataOutputStream outStream = null;
        DataInputStream inStream = null;
        boolean res = false;
        try {
            final URL url = new URL(decurtSlash(geoserverURL) + "/rest/reset");

            // Create connection
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");

            // Add Authentication
            if (passwd != null && user != null) {
                BASE64Encoder enc = new BASE64Encoder();
                String userpassword = user + ":" + passwd;
                String encodedAuthorization = enc.encode(userpassword.getBytes());
                urlConnection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
            }

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "text/xml");

            // Build request body
            final String body = URLEncoder.encode(geoserverURL, "UTF-8");
            urlConnection.setRequestProperty("Content-Length", "" + body.length());

            // Create I/O streams
            outStream = new DataOutputStream(urlConnection.getOutputStream());
            inStream = new DataInputStream(urlConnection.getInputStream());

            // Send request
            outStream.writeBytes(body);
            outStream.flush();

            /*
             * Gets the status code from an HTTP response message. For example, in the case of the
             * following status lines: HTTP/1.0 200 OK HTTP/1.0 401 Unauthorized
             */
            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
                String response = GeoServerRESTHelper.readIs(is);
                if (is != null) {
                    is.close();
                }
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("HTTP OK: " + response);
                res = true;
            } else if (responseCode == HttpURLConnection.HTTP_CREATED) {
                InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
                String response = GeoServerRESTHelper.readIs(is);
                is.close();
                final String name = GeoServerRESTHelper.extractName(response);
                // if (returnedLayerName!=null && returnedLayerName.length>0)
                // returnedLayerName[0]=name;
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("HTTP CREATED: " + response);
                } else {
                    if (LOGGER.isInfoEnabled())
                        LOGGER.info("HTTP CREATED: " + name);
                }
                res = true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("ImageMosaicREST::disposeReader(): HTTP ERROR:"
                            + "\nRequestMethod: " + urlConnection.getRequestMethod()
                            + "\nResponseMessage: " + urlConnection.getResponseMessage()
                            + "\nCode: " + urlConnection.getResponseCode()
                            + "\nReadTimeout is (0 return implies that the option is disabled): "
                            + urlConnection.getReadTimeout());
                res = false;
            }

        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(e.getMessage(), e);
            return false;
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(e.getMessage(), e);
            return false;
        } finally {
            try {
                // Close I/O streams
                if (inStream != null)
                    inStream.close();
                if (outStream != null)
                    outStream.close();
            } catch (Exception e) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn(e.getMessage(), e);
            }
        }
        return res;
    }
}
