/*
 * $Header: it.geosolutions.geobatch.wmc.model.WMCExtension,v. 0.1 02/dic/2009 18:08:21 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 02/dic/2009 18:08:21 $
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
package it.geosolutions.geobatch.wmc.model;



/**
 * @author Fabiani
 *
 */
public class WMCExtension {
	private OLLayerID id;
	private OLTransparent transparent;
	private OLIsBaseLayer isBaseLayer;
	private OLOpacity opacity;
	private OLDisplayInLayerSwitcher displayInLayerSwitcher;
	private OLSingleTile singleTile;
	private OLNumZoomLevels numZoomLevels;
	private OLUnits units;
	private OLMaxExtent maxExtent;
	private OLDimension time;
	private OLDimension elevation;
	
	private OLStyleClassNumber styleClassNumber;
	private OLStyleColorRamps  styleColorRamps;
	private OLStyleMinValue    styleMinValue;
	private OLStyleMaxValue    styleMaxValue;
	private OLStyleRestService styleRestService;
	
	/**
	 * @return the id
	 */
	public OLLayerID getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(OLLayerID id) {
		this.id = id;
	}
	/**
	 * @return the transparent
	 */
	public OLTransparent getTransparent() {
		return transparent;
	}
	/**
	 * @param transparent the transparent to set
	 */
	public void setTransparent(OLTransparent transparent) {
		this.transparent = transparent;
	}
	/**
	 * @return the isBaseLayer
	 */
	public OLIsBaseLayer getIsBaseLayer() {
		return isBaseLayer;
	}
	/**
	 * @param isBaseLayer the isBaseLayer to set
	 */
	public void setIsBaseLayer(OLIsBaseLayer isBaseLayer) {
		this.isBaseLayer = isBaseLayer;
	}
	/**
	 * @return the opacity
	 */
	public OLOpacity getOpacity() {
		return opacity;
	}
	/**
	 * @param opacity the opacity to set
	 */
	public void setOpacity(OLOpacity opacity) {
		this.opacity = opacity;
	}
	/**
	 * @return the displayInLayerSwitcher
	 */
	public OLDisplayInLayerSwitcher getDisplayInLayerSwitcher() {
		return displayInLayerSwitcher;
	}
	/**
	 * @param displayInLayerSwitcher the displayInLayerSwitcher to set
	 */
	public void setDisplayInLayerSwitcher(
			OLDisplayInLayerSwitcher displayInLayerSwitcher) {
		this.displayInLayerSwitcher = displayInLayerSwitcher;
	}
	/**
	 * @return the singleTile
	 */
	public OLSingleTile getSingleTile() {
		return singleTile;
	}
	/**
	 * @param singleTile the singleTile to set
	 */
	public void setSingleTile(OLSingleTile singleTile) {
		this.singleTile = singleTile;
	}
	/**
	 * @return the numZoomLevels
	 */
	public OLNumZoomLevels getNumZoomLevels() {
		return numZoomLevels;
	}
	/**
	 * @param numZoomLevels the numZoomLevels to set
	 */
	public void setNumZoomLevels(OLNumZoomLevels numZoomLevels) {
		this.numZoomLevels = numZoomLevels;
	}
	/**
	 * @return the units
	 */
	public OLUnits getUnits() {
		return units;
	}
	/**
	 * @param units the units to set
	 */
	public void setUnits(OLUnits units) {
		this.units = units;
	}
	/**
	 * @return the maxExtent
	 */
	public OLMaxExtent getMaxExtent() {
		return maxExtent;
	}
	/**
	 * @param maxExtent the maxExtent to set
	 */
	public void setMaxExtent(OLMaxExtent maxExtent) {
		this.maxExtent = maxExtent;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(OLDimension time) {
		this.time = time;
	}
	/**
	 * @return the time
	 */
	public OLDimension getTime() {
		return time;
	}
	/**
	 * @param elevation the elevation to set
	 */
	public void setElevation(OLDimension elevation) {
		this.elevation = elevation;
	}
	/**
	 * @return the elevation
	 */
	public OLDimension getElevation() {
		return elevation;
	}
	/**
	 * @param styleClassNumber the styleClassNumber to set
	 */
	public void setStyleClassNumber(OLStyleClassNumber styleClassNumber) {
		this.styleClassNumber = styleClassNumber;
	}
	/**
	 * @return the styleClassNumber
	 */
	public OLStyleClassNumber getStyleClassNumber() {
		return styleClassNumber;
	}
	/**
	 * @param styleColorRamps the styleColorRamps to set
	 */
	public void setStyleColorRamps(OLStyleColorRamps styleColorRamps) {
		this.styleColorRamps = styleColorRamps;
	}
	/**
	 * @return the styleColorRamps
	 */
	public OLStyleColorRamps getStyleColorRamps() {
		return styleColorRamps;
	}
	/**
	 * @param styleMinValue the styleMinValue to set
	 */
	public void setStyleMinValue(OLStyleMinValue styleMinValue) {
		this.styleMinValue = styleMinValue;
	}
	/**
	 * @return the styleMinValue
	 */
	public OLStyleMinValue getStyleMinValue() {
		return styleMinValue;
	}
	/**
	 * @param styleMaxValue the styleMaxValue to set
	 */
	public void setStyleMaxValue(OLStyleMaxValue styleMaxValue) {
		this.styleMaxValue = styleMaxValue;
	}
	/**
	 * @return the styleMaxValue
	 */
	public OLStyleMaxValue getStyleMaxValue() {
		return styleMaxValue;
	}
	/**
	 * @param styleRestService the styleRestService to set
	 */
	public void setStyleRestService(OLStyleRestService styleRestService) {
		this.styleRestService = styleRestService;
	}
	/**
	 * @return the styleRestService
	 */
	public OLStyleRestService getStyleRestService() {
		return styleRestService;
	}

}
