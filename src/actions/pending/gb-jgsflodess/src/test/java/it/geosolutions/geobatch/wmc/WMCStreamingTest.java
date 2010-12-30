/*
 * $Header: it.geosolutions.geobatch.wmc.WMCStreamingTest,v. 0.1 02/dic/2009 16:44:32 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 16:44:32 $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. 
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.geobatch.wmc;

import it.geosolutions.geobatch.wmc.model.GeneralWMCConfiguration;
import it.geosolutions.geobatch.wmc.model.OLDimension;
import it.geosolutions.geobatch.wmc.model.OLIsBaseLayer;
import it.geosolutions.geobatch.wmc.model.OLLayerID;
import it.geosolutions.geobatch.wmc.model.OLMaxExtent;
import it.geosolutions.geobatch.wmc.model.OLSingleTile;
import it.geosolutions.geobatch.wmc.model.OLTransparent;
import it.geosolutions.geobatch.wmc.model.ViewContext;
import it.geosolutions.geobatch.wmc.model.WMCBoundingBox;
import it.geosolutions.geobatch.wmc.model.WMCExtension;
import it.geosolutions.geobatch.wmc.model.WMCFormat;
import it.geosolutions.geobatch.wmc.model.WMCLayer;
import it.geosolutions.geobatch.wmc.model.WMCOnlineResource;
import it.geosolutions.geobatch.wmc.model.WMCSLD;
import it.geosolutions.geobatch.wmc.model.WMCServer;
import it.geosolutions.geobatch.wmc.model.WMCStyle;
import it.geosolutions.geobatch.wmc.model.WMCWindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

/**
 * @author Fabiani
 *
 */
public class WMCStreamingTest {

	private final static Logger LOGGER = Logger.getLogger(WMCStreamingTest.class.toString());

    @Test
    public void testWMCStreamingOut() throws IOException, InterruptedException {
    	/**
    	 * creating objects ...
    	 */
    	ViewContext viewContext = new ViewContext("tstWMC", "1.0.0");
    	WMCWindow window = new WMCWindow(331, 560);
    	GeneralWMCConfiguration generalConfig = new GeneralWMCConfiguration(window, "Prova", "prova");
    	WMCBoundingBox bbox = new WMCBoundingBox("EPSG:4326", -180.0, -90.0, 180.0, 90.0);
    	
    	List<WMCLayer> layerList = new ArrayList<WMCLayer>();
    	
    	WMCLayer testLayer = new WMCLayer("0", "1", "nurc:testLayer", "Test Layer", "EPSG:4326");
    	WMCServer server = new WMCServer("wms", "1.1.1", "wms");
    	List<WMCFormat> formatList = new ArrayList<WMCFormat>();
    	List<WMCStyle> styleList = new ArrayList<WMCStyle>();
    	WMCExtension extension = new WMCExtension();
    	extension.setId(new OLLayerID("observations"));
    	extension.setMaxExtent(new OLMaxExtent(null));
    	extension.setIsBaseLayer(new OLIsBaseLayer("TRUE"));
    	extension.setSingleTile(new OLSingleTile("FALSE"));
    	extension.setTransparent(new OLTransparent("FALSE"));
    	extension.setTime(new OLDimension("0000-00-00T00:00:000Z", "TIME", "current"));
    	extension.setElevation(new OLDimension("0.0,10.0", "ELEVATION", "0.0"));
    	
    	formatList.add(new WMCFormat("1", "image/png"));
    	styleList.add(new WMCStyle("1", new WMCSLD(new WMCOnlineResource("simple", "http://localhost:8081/NurcCruises/resources/xml/SLDDefault.xml"))));
    	
    	server.setOnlineResource(new WMCOnlineResource("simple", "http://localhost:8081/geoserver/wms"));
		testLayer.setServer(server);
    	testLayer.setFormatList(formatList);
    	testLayer.setStyleList(styleList);
    	testLayer.setExtension(extension);
    	
    	layerList.add(testLayer);
    	
    	window.setBbox(bbox);
		viewContext.setGeneral(generalConfig);
		viewContext.setLayerList(layerList);
    	
		new WMCStream().toXML(viewContext, System.out);
    }
}
