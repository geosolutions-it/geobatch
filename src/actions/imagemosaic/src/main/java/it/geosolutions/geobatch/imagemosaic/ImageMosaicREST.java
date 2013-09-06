/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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

//import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.imagemosaic.config.DomainAttribute;
import it.geosolutions.geobatch.imagemosaic.utils.ConfigUtil;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import it.geosolutions.geoserver.rest.encoder.coverage.GSImageMosaicEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.GSDimensionInfoEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.GSDimensionInfoEncoder.Presentation;

import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	    coverageEnc.setName(mosaicDescriptor.getCoverageStoreId());
	    coverageEnc.setTitle(mosaicDescriptor.getCoverageStoreId());
	    if (config.getCrs()!=null){
	        coverageEnc.setSRS(config.getCrs());
	    }
	    
//	    coverageEnc.setMaxAllowedTiles(config.get) //TODO
	    coverageEnc.setMaxAllowedTiles(Integer.MAX_VALUE);
	    

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


        for (DomainAttribute attr : config.getDomainAttributes()) {
            final GSDimensionInfoEncoder info = new GSDimensionInfoEncoder(true);

            final String presentation = attr.getPresentationMode();
            if (presentation == null) {
	            info.setPresentation(Presentation.LIST);
            } else  if (presentation.equals(Presentation.DISCRETE_INTERVAL.toString())) {
                BigDecimal interval = attr.getDiscreteInterval();
                if (interval == null || interval.intValue() < 1) {
                    interval = new BigDecimal(1);
                    LOGGER.warn("Invalid value in dimension "+attr.getDimensionName()+" for time DISCRETE_INTERVAL value (" + interval + "). Forcing to 1.");
                }
                info.setPresentation(Presentation.DISCRETE_INTERVAL, interval);
            } else if (presentation.equals(Presentation.LIST.toString())) {
                info.setPresentation(Presentation.LIST);
            } else if (presentation.equals(Presentation.CONTINUOUS_INTERVAL.toString())) {
                info.setPresentation(Presentation.CONTINUOUS_INTERVAL);
            } else {
                LOGGER.error("Unknown presentation type '"+presentation+"'");
            }

	        coverageEnc.setMetadata(attr.getDimensionName(), info);
        }

//        // is this really needed?
//        if( ! ConfigUtil.hasDimension(config, DomainAttribute.DIM_TIME))
//        	coverageEnc.setMetadata("time", new GSDimensionInfoEncoder());
//
//        // is this really needed?
//        if( ! ConfigUtil.hasDimension(config, DomainAttribute.DIM_ELEV))
//        	coverageEnc.setMetadata("elevation", new GSDimensionInfoEncoder());

        
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
        	coverageEnc.setProjectionPolicy(ProjectionPolicy.NONE);	
        }
        
        coverageEnc.setLatLonBoundingBox(
        		config.getLatLonMinBoundingBoxX() != null ? config.getLatLonMinBoundingBoxX(): -180,
                		config.getLatLonMinBoundingBoxY() != null ? config.getLatLonMinBoundingBoxY() : -90,
        		config.getLatLonMaxBoundingBoxX() != null ? config.getLatLonMaxBoundingBoxX(): 180,
        				config.getLatLonMaxBoundingBoxY() != null ? config.getLatLonMaxBoundingBoxY(): 90,
        		config.getCrs());


        if (LOGGER.isDebugEnabled()){
        	LOGGER.debug("Coverage configuration:\n"+coverageEnc.toString());
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
    @Deprecated
    protected static GSCoverageEncoder createGSCoverageEncoder(final String coverageID, ImageMosaicConfiguration config) {

        LOGGER.warn(" **** Deprecated method ****");

	    final GSCoverageEncoder coverageEnc=new GSCoverageEncoder();

	    coverageEnc.setName(coverageID);
	    coverageEnc.setTitle(coverageID);
	    coverageEnc.setSRS(config.getCrs()!=null?config.getCrs():"");
	    

//        final GSMetadataEncoder<GSDimensionInfoEncoder> metadata=new GSMetadataEncoder<GSDimensionInfoEncoder>();
        
        for (DomainAttribute attr : config.getDomainAttributes()) {
            final GSDimensionInfoEncoder info = new GSDimensionInfoEncoder(true);

            final String presentation = attr.getPresentationMode();
            if (presentation == null) {
	            info.setPresentation(Presentation.LIST);
            } else  if (presentation.equals(Presentation.DISCRETE_INTERVAL.toString())) {
                BigDecimal interval = attr.getDiscreteInterval();
                if (interval == null || interval.intValue() < 1) {
                    interval = new BigDecimal(1);
                    LOGGER.warn("Invalid value in dimension "+attr.getDimensionName()+" for time DISCRETE_INTERVAL value (" + interval + "). Forcing to 1.");
                }
                info.setPresentation(Presentation.DISCRETE_INTERVAL, interval);
            } else if (presentation.equals(Presentation.LIST.toString())) {
                info.setPresentation(Presentation.LIST);
            } else if (presentation.equals(Presentation.CONTINUOUS_INTERVAL.toString())) {
                info.setPresentation(Presentation.CONTINUOUS_INTERVAL);
            } else {
                LOGGER.error("Unknown presentation type '"+presentation+"'");
            }

	        coverageEnc.setMetadata(attr.getDimensionName(), info);
        }

        // is this really needed?
        if( ! ConfigUtil.hasDimension(config, DomainAttribute.DIM_TIME))
        	coverageEnc.setMetadata("time", new GSDimensionInfoEncoder());

        // is this really needed?
        if( ! ConfigUtil.hasDimension(config, DomainAttribute.DIM_ELEV))
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
}
