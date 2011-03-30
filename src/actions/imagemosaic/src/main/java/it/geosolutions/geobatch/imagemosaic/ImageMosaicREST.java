package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import sun.misc.BASE64Encoder;

public abstract class ImageMosaicREST {
    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ImageMosaicREST.class.toString());

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
    protected static boolean createNewImageMosaicLayer(File inputDir,
            ImageMosaicGranulesDescriptor mosaicDescriptor, ImageMosaicConfiguration config,
            Collection<FileSystemEvent> layers) throws ParserConfigurationException, IOException,
            TransformerException {

        // ////////////////////////////////////////////////////////////////////
        //
        // SENDING data to GeoServer via REST protocol.
        //
        // ////////////////////////////////////////////////////////////////////
        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("namespace", config.getDefaultNamespace());
        queryParams.put("wmspath", config.getWmsPath());

        // ////////////////
        if (queryParams.containsKey("MaxAllowedTiles")) {
            // Configuring wmsPath
            final String maxTiles = queryParams.get("MaxAllowedTiles");
            queryParams.put("MaxAllowedTiles", maxTiles);
        } else {
            queryParams.put("MaxAllowedTiles", Integer.toString(Integer.MAX_VALUE));
        }

        String noData;
        if (mosaicDescriptor.getFirstCvNameParts() == null) {
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
        queryParams.put("BackgroundValues", noData);// NoData

        String param = config.getOutputTransparentColor();
        queryParams.put("OutputTransparentColor", (param != null) ? param : "");
        param = config.getInputTransparentColor();
        queryParams.put("InputTransparentColor", (param != null) ? param : "");
        param = null;

        /*
         * note: setting - AllowMultithreading to true - USE_JAI_IMAGEREAD to true make no sense!
         * Simone on 23 Mar 2011: this check should be done by the user configurator or by GeoServer
         */

        queryParams.put("AllowMultithreading", config.isAllowMultithreading() ? "true" : "false");

        queryParams.put("USE_JAI_IMAGEREAD", config.isUseJaiImageRead() ? "true" : "false");

        if (config.getTileSizeH() < 1 || config.getTileSizeW() < 1) {
            queryParams.put("SUGGESTED_TILE_SIZE", "256,256");
        } else {
            queryParams.put("SUGGESTED_TILE_SIZE",
                    config.getTileSizeH() + "," + config.getTileSizeW());
        }

        final String[] layerResponse = GeoServerRESTHelper.sendCoverage(inputDir, inputDir,
                decurtSlash(config.getGeoserverURL()), config.getGeoserverUID(),
                config.getGeoserverPWD(), mosaicDescriptor.getCoverageStoreId(),
                mosaicDescriptor.getCoverageStoreId(), queryParams, "", "EXTERNAL", "imagemosaic",
                GEOSERVER_VERSION, config.getStyles(), config.getDefaultStyle());

        if (layerResponse != null && layerResponse.length > 2) {

            final String workspace = layerResponse[1];
            final String coverageName = layerResponse[0];
            final String layer = layerResponse[0];

            LOGGER.info("ImageMosaicConfigurator layer: " + layer);

            Map<String, String> metadataParams = new HashMap<String, String>();

            metadataParams.put("timeDimEnabled",
                    config.getTimeDimEnabled() != null ? config.getTimeDimEnabled() : "true");
            metadataParams.put("dirName", config.getDirName() != null ? config.getDirName() : "");
            metadataParams.put("timePresentationMode",
                    config.getTimePresentationMode() != null ? config.getTimePresentationMode()
                            : "LIST");

            Map<String, String> coverageParams = new HashMap<String, String>();

            coverageParams.put(GeoServerRESTHelper.NATIVE_MAXX,
                    config.getNativeMaxBoundingBoxX() != null ? config.getNativeMaxBoundingBoxX()
                            .toString() : "180");

            coverageParams.put(GeoServerRESTHelper.NATIVE_MINX,
                    config.getNativeMinBoundingBoxX() != null ? config.getNativeMinBoundingBoxX()
                            .toString() : "-180");

            coverageParams.put(GeoServerRESTHelper.NATIVE_MINY,
                    config.getNativeMinBoundingBoxY() != null ? config.getNativeMinBoundingBoxY()
                            .toString() : "-90");

            coverageParams.put(GeoServerRESTHelper.NATIVE_MAXY,
                    config.getNativeMaxBoundingBoxY() != null ? config.getNativeMaxBoundingBoxY()
                            .toString() : "90");

            coverageParams.put(GeoServerRESTHelper.LATLON_MAXX,
                    config.getLatLonMaxBoundingBoxX() != null ? config.getLatLonMaxBoundingBoxX()
                            .toString() : "180");

            coverageParams.put(GeoServerRESTHelper.LATLON_MINX,
                    config.getLatLonMinBoundingBoxX() != null ? config.getLatLonMinBoundingBoxX()
                            .toString() : "-180");

            coverageParams.put(GeoServerRESTHelper.LATLON_MINY,
                    config.getLatLonMinBoundingBoxY() != null ? config.getLatLonMinBoundingBoxY()
                            .toString() : "-90");

            coverageParams.put(GeoServerRESTHelper.LATLON_MAXY,
                    config.getLatLonMaxBoundingBoxY() != null ? config.getLatLonMaxBoundingBoxY()
                            .toString() : "90");

            if (config.getCrs() != null)
                coverageParams.put(GeoServerRESTHelper.CRS, config.getCrs().toString());
            else
                coverageParams.put(GeoServerRESTHelper.CRS, ""); // TODO check default value!!!

            GeoServerRESTHelper.sendCoverageConfiguration(coverageParams, metadataParams,
                    queryParams, decurtSlash(config.getGeoserverURL()), config.getGeoserverUID(),
                    config.getGeoserverPWD(), workspace, mosaicDescriptor.getCoverageStoreId(),
                    coverageName);

            File layerDescriptor = null;

            // generate a RETURN file and append it to the return queue
            if ((layerDescriptor = setReturn(inputDir, layer + ".layer", layerResponse,
                    mosaicDescriptor)) != null) {
                layers.add(new FileSystemEvent(layerDescriptor, FileSystemEventType.FILE_ADDED));
            }

            return true;
        } else {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE,
                        "Bad response from the GeoServer:GeoServerRESTHelper.sendCoverage()");
            return false;
        }
    }

    private static File setReturn(File inputDir, String outFileName, String[] layerResponse,
            ImageMosaicGranulesDescriptor mosaicDescriptor) {
        final File layerDescriptor = new File(inputDir, outFileName);
        FileWriter outFile = null;
        try {
            if (layerDescriptor.createNewFile()) {
                PrintWriter out = null;
                try {
                    outFile = new FileWriter(layerDescriptor);
                    out = new PrintWriter(outFile);

                    // Write text to file
                    out.println("namespace=" + layerResponse[1]);
                    out.println("metocFields=" + mosaicDescriptor.getMetocFields());
                    out.println("storeid=" + mosaicDescriptor.getCoverageStoreId());
                    out.println("layerid=" + inputDir.getName());
                    out.println("driver=ImageMosaic");
                    out.println("path=" + File.separator);
                } catch (IOException e) {
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE,
                                "Error occurred while writing indexer.properties file!", e);
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }

                    outFile = null;
                    out = null;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "ImageMosaic:setReturn(): " + e.getMessage(), e);
        }
        return layerDescriptor;
    }

    /**
     * Configure Mosaic Method
     * 
     * @param queryParams
     * @param geoserverBaseURL
     * @param geoserverUID
     * @param geoserverPWD
     * @param workspace
     * @param coverageStore
     * @param coverageName
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     * 
     *             protected static void configureMosaic(final Map<String, String> metadataParams,
     *             final Map<String, String> queryParams, final String geoserverBaseURL, final
     *             String geoserverUID, final String geoserverPWD, final String workspace, final
     *             String coverageStore, final String coverageName) throws
     *             ParserConfigurationException, IOException, TransformerException {
     * 
     *             // Map<String, String> configElements = new HashMap<String, String>(2);
     * 
     *             if (queryParams.containsKey("MaxAllowedTiles")) { // Configuring wmsPath final
     *             String maxTiles = queryParams.get("MaxAllowedTiles"); //
     *             configElements.put("MaxAllowedTiles", maxTiles);
     *             queryParams.put("MaxAllowedTiles", maxTiles); } else { //
     *             configElements.put("MaxAllowedTiles", "2147483647");
     *             queryParams.put("MaxAllowedTiles", "2147483647"); }
     * 
     *             // if (!configElements.isEmpty()) { if (!queryParams.isEmpty()) {
     *             GeoServerRESTHelper.sendCoverageConfiguration(metadataParams, // configElements,
     *             queryParams, geoserverBaseURL, geoserverUID, geoserverPWD, workspace,
     *             coverageStore, coverageName); }
     * 
     *             }
     */
    /**
     * Configures the styles associated in this class' GeoServerActionConfiguration to the layer
     * passed as parameter.
     * 
     * @param layerName
     *            the layer to associate to the given styles.
     * @param
     * @return true if there were no errors in setting the styles.
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     */
    protected static boolean configureStyles(String layerName, ImageMosaicConfiguration conf)
            throws MalformedURLException {

        boolean ret = true;
        URL restUrl = new URL(conf.getGeoserverURL() + "/rest/sldservice/updateLayer/" + layerName);

        for (String styleName : conf.getStyles()) {

            if (GeoServerRESTHelper.putContent(restUrl, "<LayerConfig><Style>" + styleName
                    + "</Style></LayerConfig>", conf.getGeoserverUID(), conf.getGeoserverPWD())) {

                LOGGER.info("added style " + styleName + " for layer " + layerName);
            } else {
                LOGGER.warning("error adding style " + styleName + " for layer " + layerName);
                ret = false;
            }
        }

        ret &= GeoServerRESTHelper.putContent(restUrl,
                "<LayerConfig><DefaultStyle>" + conf.getDefaultStyle()
                        + "</DefaultStyle></LayerConfig>", conf.getGeoserverUID(),
                conf.getGeoserverPWD());
        return ret;
    }

    /**
     * @throws MalformedURLException
     * 
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
            LOGGER.warning("ImageMosaicREST::reloadCatalog(): Error occurred while trying to reload GeoServer Catalog!");
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
                if (is!=null){
                    is.close();
                }
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("HTTP OK: " + response);
                res = true;
            } else if (responseCode == HttpURLConnection.HTTP_CREATED) {
                InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
                String response = GeoServerRESTHelper.readIs(is);
                is.close();
                final String name = GeoServerRESTHelper.extractName(response);
                // if (returnedLayerName!=null && returnedLayerName.length>0)
                // returnedLayerName[0]=name;
                if (LOGGER.isLoggable(Level.FINE)){
                    LOGGER.log(Level.FINE, "HTTP CREATED: " + response);
                }
                else {
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("HTTP CREATED: " + name);
                }
                res = true;
            } else {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.warning("ImageMosaicREST::disposeReader(): HTTP ERROR:"
                            + "\nRequestMethod: " + urlConnection.getRequestMethod()
                            + "\nResponseMessage: " + urlConnection.getResponseMessage()
                            + "\nCode: " + urlConnection.getResponseCode()
                            + "\nReadTimeout is (0 return implies that the option is disabled): "
                            + urlConnection.getReadTimeout());
                res = false;
            }

        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            return false;
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            return false;
        } finally {
            try {
                // Close I/O streams
                if (inStream != null)
                    inStream.close();
                if (outStream != null)
                    outStream.close();
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return res;
    }
}
