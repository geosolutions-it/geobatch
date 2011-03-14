package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public abstract class ImageMosaicREST {
    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ImageMosaicREST.class.toString());
    
    /**
     * 
     */
    public final static String GEOSERVER_VERSION = "2.x";

    
    protected static String decurtSlash(String geoserverURL) {
        return !geoserverURL.endsWith("/") ? geoserverURL : geoserverURL.substring(0,
                geoserverURL.length() - 1);
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
    protected static void createNewImageMosaicLayer(File inputDir,
                ImageMosaicGranulesDescriptor mosaicDescriptor,
                ImageMosaicConfiguration config,
                Collection<FileSystemEvent> layers)
                    throws ParserConfigurationException, IOException, TransformerException 
    {

        // ////////////////////////////////////////////////////////////////////
        //
        // SENDING data to GeoServer via REST protocol.
        //
        // ////////////////////////////////////////////////////////////////////
        Map<String, String> queryParams = new HashMap<String, String>();
        Map<String, String> metadataParams = new HashMap<String, String>();
        queryParams.put("namespace", config.getDefaultNamespace());
        queryParams.put("wmspath", config.getWmsPath());
        
        //////////////////
        if (queryParams.containsKey("MaxAllowedTiles")) {
            // Configuring wmsPath
            final String maxTiles = queryParams.get("MaxAllowedTiles");
            queryParams.put("MaxAllowedTiles", maxTiles);
        } else {
            queryParams.put("MaxAllowedTiles", Integer.toString(Integer.MAX_VALUE));
        }
        
        String noData;
        if (mosaicDescriptor.getFirstCvNameParts()==null){
            noData =(config.getBackgroundValue() != null) ? config
                    .getBackgroundValue() : "-1.0";
        }
        else {
            if (mosaicDescriptor.getFirstCvNameParts().length >= 9) {
                noData = mosaicDescriptor.getFirstCvNameParts()[8];
            } else if (mosaicDescriptor.getFirstCvNameParts().length >= 8) {
                noData = mosaicDescriptor.getFirstCvNameParts()[7];
            } else {
                // use default value from configuration?
                noData = (config.getBackgroundValue() != null) ? config
                        .getBackgroundValue() : "-1.0";
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
        queryParams.put("AllowMultithreading",
                config.isAllowMultithreading() ? "true" : "false");
        queryParams.put("USE_JAI_IMAGEREAD", config.isUseJaiImageRead() ? "true"
                : "false");
        if (config.getTileSizeH() < 1 || config.getTileSizeW() < 1) {
            queryParams.put("SUGGESTED_TILE_SIZE", "256,256");
        } else
            queryParams.put("SUGGESTED_TILE_SIZE", config.getTileSizeH() + ","
                    + config.getTileSizeW());
        
        final String[] layerResponse = GeoServerRESTHelper.sendCoverage(inputDir, inputDir,
                decurtSlash(config.getGeoserverURL()), config.getGeoserverUID(), config.getGeoserverPWD(),
                mosaicDescriptor.getCoverageStoreId(), mosaicDescriptor.getCoverageStoreId(), queryParams,
                "", "EXTERNAL", "imagemosaic", GEOSERVER_VERSION, config.getStyles(),
                config.getDefaultStyle());

        if (layerResponse != null && layerResponse.length > 2) {

            final String workspace = layerResponse[1];
            final String coverageName = layerResponse[0];
            final String layer = layerResponse[0];
            
            LOGGER.info("ImageMosaicConfigurator layer: " + layer);
            

            metadataParams.put("timeDimEnabled",
                    config.getTimeDimEnabled() != null ? config
                            .getTimeDimEnabled() : "true");
            metadataParams.put("dirName",
                    config.getDirName() != null ? config.getDirName() : "");
            metadataParams.put("timePresentationMode",
                    config.getTimePresentationMode() != null ? config
                            .getTimePresentationMode() : "LIST");
            
            GeoServerRESTHelper.sendCoverageConfiguration(metadataParams,queryParams, decurtSlash(config.getGeoserverURL()),
                    config.getGeoserverUID(), config.getGeoserverPWD(),workspace, mosaicDescriptor.getCoverageStoreId(), coverageName);

            File layerDescriptor = null;
            
            // generate a RETURN file and append it to the return queue
            if ((layerDescriptor=setReturn(inputDir, layer + ".layer",layerResponse,mosaicDescriptor))!=null) {
                layers.add(new FileSystemEvent(layerDescriptor, FileSystemEventType.FILE_ADDED));
            }
        }
        else
        {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE,
                    "Bad response from the GeoServer:GeoServerRESTHelper.sendCoverage()");
        }
    }
    
    private static File setReturn(File inputDir, String outFileName, String[] layerResponse, ImageMosaicGranulesDescriptor mosaicDescriptor){
        final File layerDescriptor = new File(inputDir, outFileName);
        FileWriter outFile=null;
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
            LOGGER.log(Level.WARNING, "ImageMosaic:setReturn(): "+e.getMessage(), e);
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
     
    protected static void configureMosaic(final Map<String, String> metadataParams,
            final Map<String, String> queryParams, final String geoserverBaseURL,
            final String geoserverUID, final String geoserverPWD, final String workspace,
            final String coverageStore, final String coverageName)
            throws ParserConfigurationException, IOException, TransformerException {
    
        // Map<String, String> configElements = new HashMap<String, String>(2);
    
        if (queryParams.containsKey("MaxAllowedTiles")) {
            // Configuring wmsPath
            final String maxTiles = queryParams.get("MaxAllowedTiles");
            // configElements.put("MaxAllowedTiles", maxTiles);
            queryParams.put("MaxAllowedTiles", maxTiles);
        } else {
            // configElements.put("MaxAllowedTiles", "2147483647");
            queryParams.put("MaxAllowedTiles", "2147483647");
        }
    
        // if (!configElements.isEmpty()) {
        if (!queryParams.isEmpty()) {
            GeoServerRESTHelper.sendCoverageConfiguration(metadataParams,
                    // configElements,
                    queryParams, geoserverBaseURL, geoserverUID, geoserverPWD, workspace,
                    coverageStore, coverageName);
        }
    
    }
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
    protected static boolean configureStyles(String layerName, ImageMosaicConfiguration conf) throws MalformedURLException
    {

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

        ret &= GeoServerRESTHelper.putContent(restUrl, "<LayerConfig><DefaultStyle>" + conf.getDefaultStyle()
                + "</DefaultStyle></LayerConfig>", conf.getGeoserverUID(), conf.getGeoserverPWD());
        return ret;
    }

}
