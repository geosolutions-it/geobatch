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

package it.geosolutions.geobatch.settings;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class JAISettings extends GBSettings {

    private float memoryCapacity = 0.5f;
    private float memoryThreshold = 0.75f;
    private int tileThreads = 10;
    private int tileThreadsPriority = 5;
    private boolean tileRecycling = false;
    private boolean jpegNative = true;
    private boolean pngNative = true;
    private boolean mosaicNative = true;

    public boolean isJpegNative() {
        return jpegNative;
    }

    public void setJpegNative(boolean jpegNative) {
        this.jpegNative = jpegNative;
    }

    public float getMemoryCapacity() {
        return memoryCapacity;
    }

    public void setMemoryCapacity(float memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    public float getMemoryThreshold() {
        return memoryThreshold;
    }

    public void setMemoryThreshold(float memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }

    public boolean isMosaicNative() {
        return mosaicNative;
    }

    public void setMosaicNative(boolean mosaicNative) {
        this.mosaicNative = mosaicNative;
    }

    public boolean isPngNative() {
        return pngNative;
    }

    public void setPngNative(boolean pngNative) {
        this.pngNative = pngNative;
    }

    public boolean isTileRecycling() {
        return tileRecycling;
    }

    public void setTileRecycling(boolean tileRecycling) {
        this.tileRecycling = tileRecycling;
    }

    public int getTileThreads() {
        return tileThreads;
    }

    public void setTileThreads(int tileThreads) {
        this.tileThreads = tileThreads;
    }

    public int getTileThreadsPriority() {
        return tileThreadsPriority;
    }

    public void setTileThreadsPriority(int tileThreadsPriority) {
        this.tileThreadsPriority = tileThreadsPriority;
    }

    @Override
    public String toString() {
        return "JAISettings{" + "memoryCapacity=" + memoryCapacity + ", memoryThreshold=" + memoryThreshold + ", tileThreads=" + tileThreads + ", tileThreadsPriority=" + tileThreadsPriority + ", tileRecycling=" + tileRecycling + ", jpegNative=" + jpegNative + ", pngNative=" + pngNative + ", mosaicNative=" + mosaicNative + '}';
    }

}
