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

package it.geosolutions.geobatch.settings.jai;

import it.geosolutions.geobatch.settings.GBSettings;

import javax.media.jai.JAI;
import javax.media.jai.TileCache;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class JAISettings extends GBSettings {

	private static final long serialVersionUID = 7121137497699361776L;
    
    private transient JAI jai;
    private transient TileCache tileCache;
    
    private boolean allowInterpolation;
    
    public static final boolean DEFAULT_Recycling = false;
    private boolean recycling = DEFAULT_Recycling;
    
    public static final int DEFAULT_TilePriority = Thread.NORM_PRIORITY;
    private int tilePriority = DEFAULT_TilePriority;
    
    public static final int DEFAULT_TileThreads = 7;
    private int tileThreads = DEFAULT_TileThreads;
    
    public static final double DEFAULT_MemoryCapacity = 0.5;
    private double memoryCapacity = DEFAULT_MemoryCapacity;
    
    public static final double DEFAULT_MemoryThreshold = 0.75;
    private double memoryThreshold = DEFAULT_MemoryThreshold;
    
    public static final boolean DEFAULT_ImageIOCache = false;
    private boolean imageIOCache = DEFAULT_ImageIOCache;
    
    public static final boolean DEFAULT_PNGNative = false;
    private boolean pngAcceleration = DEFAULT_PNGNative;
    
    public static final boolean DEFAULT_JPEGNative = false;
    private boolean jpegAcceleration = DEFAULT_JPEGNative;
    
    public static final boolean DEFAULT_MosaicNative = false;
    private boolean allowNativeMosaic = DEFAULT_MosaicNative;


    /**
	 * @return the jai
	 */
	public JAI getJai() {
		return jai;
	}


	/**
	 * @param jai the jai to set
	 */
	public void setJai(JAI jai) {
		this.jai = jai;
	}


	/**
	 * @return the tileCache
	 */
	public TileCache getTileCache() {
		return tileCache;
	}


	/**
	 * @param tileCache the tileCache to set
	 */
	public void setTileCache(TileCache tileCache) {
		this.tileCache = tileCache;
	}


	/**
	 * @return the allowInterpolation
	 */
	public boolean isAllowInterpolation() {
		return allowInterpolation;
	}


	/**
	 * @param allowInterpolation the allowInterpolation to set
	 */
	public void setAllowInterpolation(boolean allowInterpolation) {
		this.allowInterpolation = allowInterpolation;
	}


	/**
	 * @return the recycling
	 */
	public boolean isRecycling() {
		return recycling;
	}


	/**
	 * @param recycling the recycling to set
	 */
	public void setRecycling(boolean recycling) {
		this.recycling = recycling;
	}


	/**
	 * @return the tilePriority
	 */
	public int getTilePriority() {
		return tilePriority;
	}


	/**
	 * @param tilePriority the tilePriority to set
	 */
	public void setTilePriority(int tilePriority) {
		this.tilePriority = tilePriority;
	}


	/**
	 * @return the tileThreads
	 */
	public int getTileThreads() {
		return tileThreads;
	}


	/**
	 * @param tileThreads the tileThreads to set
	 */
	public void setTileThreads(int tileThreads) {
		this.tileThreads = tileThreads;
	}


	/**
	 * @return the memoryCapacity
	 */
	public double getMemoryCapacity() {
		return memoryCapacity;
	}


	/**
	 * @param memoryCapacity the memoryCapacity to set
	 */
	public void setMemoryCapacity(double memoryCapacity) {
		this.memoryCapacity = memoryCapacity;
	}


	/**
	 * @return the memoryThreshold
	 */
	public double getMemoryThreshold() {
		return memoryThreshold;
	}


	/**
	 * @param memoryThreshold the memoryThreshold to set
	 */
	public void setMemoryThreshold(double memoryThreshold) {
		this.memoryThreshold = memoryThreshold;
	}


	/**
	 * @return the imageIOCache
	 */
	public boolean isImageIOCache() {
		return imageIOCache;
	}


	/**
	 * @param imageIOCache the imageIOCache to set
	 */
	public void setImageIOCache(boolean imageIOCache) {
		this.imageIOCache = imageIOCache;
	}


	/**
	 * @return the pngAcceleration
	 */
	public boolean isPngAcceleration() {
		return pngAcceleration;
	}


	/**
	 * @param pngAcceleration the pngAcceleration to set
	 */
	public void setPngAcceleration(boolean pngAcceleration) {
		this.pngAcceleration = pngAcceleration;
	}


	/**
	 * @return the jpegAcceleration
	 */
	public boolean isJpegAcceleration() {
		return jpegAcceleration;
	}


	/**
	 * @param jpegAcceleration the jpegAcceleration to set
	 */
	public void setJpegAcceleration(boolean jpegAcceleration) {
		this.jpegAcceleration = jpegAcceleration;
	}


	/**
	 * @return the allowNativeMosaic
	 */
	public boolean isAllowNativeMosaic() {
		return allowNativeMosaic;
	}


	/**
	 * @param allowNativeMosaic the allowNativeMosaic to set
	 */
	public void setAllowNativeMosaic(boolean allowNativeMosaic) {
		this.allowNativeMosaic = allowNativeMosaic;
	}


	@Override
    public String toString() {
        return "JAISettings{" + "memoryCapacity=" + memoryCapacity + ", memoryThreshold=" + memoryThreshold + ", tileThreads=" + tileThreads + ", tileThreadsPriority=" + tilePriority + ", tileRecycling=" + recycling + ", jpegNative=" + jpegAcceleration + ", pngNative=" + pngAcceleration + ", mosaicNative=" + allowNativeMosaic + '}';
    }

}
