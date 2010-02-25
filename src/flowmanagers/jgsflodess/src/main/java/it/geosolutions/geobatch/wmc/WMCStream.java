/*
 * $Header: it.geosolutions.geobatch.wmc.WMCStream,v. 0.1 03/dic/2009 01:55:21 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 03/dic/2009 01:55:21 $
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
import it.geosolutions.geobatch.wmc.model.OLBaseClass;
import it.geosolutions.geobatch.wmc.model.OLDimension;
import it.geosolutions.geobatch.wmc.model.OLMaxExtent;
import it.geosolutions.geobatch.wmc.model.OLStyleColorRamps;
import it.geosolutions.geobatch.wmc.model.OLStyleMaxValue;
import it.geosolutions.geobatch.wmc.model.OLStyleMinValue;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Fabiani
 *
 */
public class WMCStream {

	private XStream xstream = new XStream(new DomDriver("UTF-8"));
	
	/**
	 * 
	 */
	public WMCStream() {
    	// WMC ViewContext
    	xstream.alias("ViewContext", ViewContext.class);
    	xstream.useAttributeFor(ViewContext.class, "xmlns");
    	xstream.useAttributeFor(ViewContext.class, "xlink");
    	xstream.useAttributeFor(ViewContext.class, "id");
    	xstream.useAttributeFor(ViewContext.class, "version");
    	xstream.aliasField("xmlns:xlink", ViewContext.class, "xlink");
    	xstream.aliasField("General", ViewContext.class, "general");
    	xstream.aliasField("LayerList", ViewContext.class, "layerList");

    	// WMC ViewContext::General
    	xstream.aliasField("Window", GeneralWMCConfiguration.class, "window");
    	xstream.aliasField("Title", GeneralWMCConfiguration.class, "title");
    	xstream.aliasField("Abstract", GeneralWMCConfiguration.class, "_abstract");

    	// WMC ViewContext::General::Window
    	xstream.useAttributeFor(WMCWindow.class, "height");
    	xstream.useAttributeFor(WMCWindow.class, "width");
    	xstream.aliasField("BoundingBox", WMCWindow.class, "bbox");

    	// WMC ViewContext::General::Window::BoundingBox
    	xstream.useAttributeFor(WMCBoundingBox.class, "srs");
    	xstream.useAttributeFor(WMCBoundingBox.class, "maxx");
    	xstream.useAttributeFor(WMCBoundingBox.class, "maxy");
    	xstream.useAttributeFor(WMCBoundingBox.class, "minx");
    	xstream.useAttributeFor(WMCBoundingBox.class, "miny");
    	xstream.aliasField("SRS", WMCBoundingBox.class, "srs");

    	// WMC ViewContext::LayerList::Layer
    	xstream.alias("Layer", WMCLayer.class);
    	xstream.useAttributeFor(WMCLayer.class, "queryable");
    	xstream.useAttributeFor(WMCLayer.class, "hidden");
    	xstream.aliasField("SRS", WMCLayer.class, "srs");
    	xstream.aliasField("Name", WMCLayer.class, "name");
    	xstream.aliasField("Title", WMCLayer.class, "title");
    	xstream.aliasField("Server", WMCLayer.class, "server");
    	xstream.aliasField("FormatList", WMCLayer.class, "formatList");
    	xstream.aliasField("StyleList", WMCLayer.class, "styleList");
    	xstream.aliasField("Extension", WMCLayer.class, "extension");

    	// WMC ViewContext::LayerList::Layer::Server
    	xstream.useAttributeFor(WMCServer.class, "service");
    	xstream.useAttributeFor(WMCServer.class, "version");
    	xstream.useAttributeFor(WMCServer.class, "title");
    	xstream.aliasField("OnlineResource", WMCServer.class, "onlineResource");

    	// WMC ViewContext::LayerList::Layer::Server::OnlineResource
    	xstream.useAttributeFor(WMCOnlineResource.class, "xlink_type");
    	xstream.useAttributeFor(WMCOnlineResource.class, "xlink_href");
    	xstream.aliasField("xlink:type", WMCOnlineResource.class, "xlink_type");
    	xstream.aliasField("xlink:href", WMCOnlineResource.class, "xlink_href");

    	// WMC ViewContext::LayerList::Layer::FormatList::Format
    	xstream.alias("Format", WMCFormat.class);
    	xstream.registerConverter(new Converter() {

			public boolean canConvert(Class clazz) {
				return WMCFormat.class.isAssignableFrom(clazz);
			}

			public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
				WMCFormat format = (WMCFormat) value;
				
				writer.addAttribute("current", format.getCurrent());
				if (format.getContent() != null)
					writer.setValue(format.getContent());
			}

			public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
				WMCFormat format = new WMCFormat("1", reader.getValue());
				
				return format;
			}
    		
    	});

    	// WMC ViewContext::LayerList::Layer::FormatList::Style
    	xstream.alias("Style", WMCStyle.class);
    	xstream.useAttributeFor(WMCStyle.class, "current");
    	xstream.aliasField("SLD", WMCStyle.class, "sld");
    	xstream.aliasField("OnlineResource", WMCSLD.class, "onlineResource");

    	// WMC ViewContext::LayerList::Layer::Extension
    	xstream.alias("Extension", WMCExtension.class);
    	
    	// WMC ViewContext::LayerList::Layer::Extension::OL
    	xstream.aliasField("ol:id", WMCExtension.class, "id");
    	xstream.aliasField("ol:transparent", WMCExtension.class, "transparent");
    	xstream.aliasField("ol:isBaseLayer", WMCExtension.class, "isBaseLayer");
    	xstream.aliasField("ol:opacity", WMCExtension.class, "opacity");
    	xstream.aliasField("ol:displayInLayerSwitcher", WMCExtension.class, "displayInLayerSwitcher");
    	xstream.aliasField("ol:singleTile", WMCExtension.class, "singleTile");
    	xstream.aliasField("ol:numZoomLevels", WMCExtension.class, "numZoomLevels");
    	xstream.aliasField("ol:units", WMCExtension.class, "units");
    	xstream.aliasField("ol:maxExtent", WMCExtension.class, "maxExtent");
    	xstream.aliasField("ol:dimension", WMCExtension.class, "time");
    	xstream.aliasField("ol:dimension", WMCExtension.class, "elevation");
    	
    	xstream.aliasField("ol:styleClassNumber", WMCExtension.class, "styleClassNumber");
    	xstream.aliasField("ol:styleColorRamps", WMCExtension.class, "styleColorRamps");
    	xstream.aliasField("ol:styleMaxValue", WMCExtension.class, "styleMaxValue");
    	xstream.aliasField("ol:styleMinValue", WMCExtension.class, "styleMinValue");
    	xstream.aliasField("ol:styleRestService", WMCExtension.class, "styleRestService");
    	
    	xstream.useAttributeFor(OLStyleColorRamps.class, "defaultRamp");
    	xstream.aliasField("default", OLStyleColorRamps.class, "defaultRamp");
    	
    	xstream.registerConverter(new Converter() {

			public boolean canConvert(Class clazz) {
				return OLBaseClass.class.isAssignableFrom(clazz);
			}

			public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
				OLBaseClass ol = (OLBaseClass) value;
				
				writer.addAttribute("xmlns:ol", ol.getXmlns_ol());
				
				if (value instanceof OLMaxExtent) {
					OLMaxExtent maxExtent = (OLMaxExtent) value;
					writer.addAttribute("minx", String.valueOf(maxExtent.getMinx()));
					writer.addAttribute("miny", String.valueOf(maxExtent.getMiny()));
					writer.addAttribute("maxx", String.valueOf(maxExtent.getMaxx()));
					writer.addAttribute("maxy", String.valueOf(maxExtent.getMaxy()));
				}
				
				if (value instanceof OLDimension) {
					OLDimension dimension = (OLDimension) value;
					writer.addAttribute("name", dimension.getName());
					writer.addAttribute("default", dimension.getDefaultValue());
				}
				
				if (value instanceof OLStyleMaxValue) {
					OLStyleMaxValue styleValue = (OLStyleMaxValue) value;
					writer.addAttribute("default", styleValue.getDefaultValue());
				}
				
				if (value instanceof OLStyleMinValue) {
					OLStyleMinValue styleValue = (OLStyleMinValue) value;
					writer.addAttribute("default", styleValue.getDefaultValue());
				}
				
				if (value instanceof OLStyleColorRamps) {
					OLStyleColorRamps styleValue = (OLStyleColorRamps) value;
					writer.addAttribute("default", styleValue.getDefaultRamp());
				}
				
				if (ol.getContent() != null)
					writer.setValue(ol.getContent());
			}

			public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
				OLBaseClass ol = null;
				
				if (reader.getAttribute("minx") != null && reader.getAttribute("miny") != null && 
						reader.getAttribute("maxx") != null && reader.getAttribute("maxy") != null) {
					ol = new OLMaxExtent(reader.getValue());
					((OLMaxExtent) ol).setMinx(Double.parseDouble(reader.getAttribute("minx")));
					((OLMaxExtent) ol).setMaxx(Double.parseDouble(reader.getAttribute("maxx")));
					((OLMaxExtent) ol).setMiny(Double.parseDouble(reader.getAttribute("miny")));
					((OLMaxExtent) ol).setMaxy(Double.parseDouble(reader.getAttribute("maxy")));
				} else if (reader.getAttribute("name") != null && reader.getAttribute("default") != null) {
					ol = new OLDimension(reader.getValue(), reader.getAttribute("name"), reader.getAttribute("default")); 
				} else {
					ol = new OLBaseClass(reader.getValue());
				}
				
				return ol;
			}
    		
    	});
    	
	}

	/**
	 * 
	 * @param viewContext
	 * @return
	 */
	public String toXML(ViewContext viewContext) {
		return xstream.toXML(viewContext);
	}

	/**
	 * 
	 * @param viewContext
	 * @param out
	 * @throws IOException 
	 */
	public void toXML(ViewContext viewContext, OutputStream out) throws IOException {
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		xstream.toXML(viewContext, writer);
	}

	/**
	 * 
	 * @param viewContext
	 * @param out
	 * @throws IOException 
	 */
	public void toXML(ViewContext viewContext, Writer out) throws IOException {
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		xstream.toXML(viewContext, out);
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	public ViewContext fromXML(InputStream input) {
		return (ViewContext) xstream.fromXML(input);
	}
	
	/**
	 * 
	 * @param xml
	 * @return
	 */
	public ViewContext fromXML(Reader xml) {
		return (ViewContext) xstream.fromXML(xml);
	}
	
	/**
	 * 
	 * @param xml
	 * @return
	 */
	public ViewContext fromXML(String xml) {
		return (ViewContext) xstream.fromXML(xml);
	}

}
