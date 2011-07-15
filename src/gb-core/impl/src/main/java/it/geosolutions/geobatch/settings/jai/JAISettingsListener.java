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
import it.geosolutions.geobatch.settings.GBSettingsDAO;
import it.geosolutions.geobatch.settings.GBSettingsListener;

import javax.media.jai.JAI;
import javax.media.jai.RecyclingTileFactory;
import javax.media.jai.TileCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//you have to link the tilecachetool project!!!
//import tilecachetool.TCTool;


/**
 * 
 * @author ETj (etj at geo-solutions.it)
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @see http://svn.codehaus.org/geoserver/trunk/src/main/src/main/java/org/geoserver/jai/JAIInitializer.java
 */
public class JAISettingsListener extends GBSettingsListener<JAISettings> {
    private static Logger LOGGER = LoggerFactory.getLogger(JAISettingsListener.class);

    @Override
    public void onStartup(GBSettingsDAO settingsDAO) {
        JAISettings settings = null;
        try {
            final GBSettings loaded = settingsDAO.find("JAI");
            settings = (JAISettings)loaded;
        } catch (Exception ex) {
        	if (LOGGER.isWarnEnabled())
        		LOGGER.warn("Could not read JAI settings.", ex);
        }

        if(settings == null) {
        	if (LOGGER.isInfoEnabled())
        		LOGGER.info("Using default JAI settings");
            settings = new JAISettings();
        }
        
        if (LOGGER.isInfoEnabled())
        	LOGGER.info("Initializing JAI settings");
        setJAIProperties(settings);
    }

    @Override
    public void beforeSave(JAISettings settings) {
        // Move along, nothing to see here!
    }

    @Override
    public void afterSave(JAISettings settings, boolean success) {
    	if (LOGGER.isInfoEnabled())
    		LOGGER.info("Applying new JAI settings");
        setJAIProperties(settings);
    }


    private void setJAIProperties(JAISettings jai) {
    	
    	JAI jaiDef = JAI.getDefaultInstance();
        jai.setJai( jaiDef );
        
        // setting JAI wide hints
        jaiDef.setRenderingHint(JAI.KEY_CACHED_TILE_RECYCLING_ENABLED, jai.isRecycling());
        
        // tile factory and recycler
        if(jai.isRecycling()) {
            final RecyclingTileFactory recyclingFactory = new RecyclingTileFactory();
            jaiDef.setRenderingHint(JAI.KEY_TILE_FACTORY, recyclingFactory);
            jaiDef.setRenderingHint(JAI.KEY_TILE_RECYCLER, recyclingFactory);
        }
        
        // Setting up Cache Capacity
        final TileCache jaiCache =  jaiDef.getTileCache();
        jai.setTileCache( jaiCache );
        
        long jaiMemory = (long) (jai.getMemoryCapacity() * Runtime.getRuntime().maxMemory());
        jaiCache.setMemoryCapacity(jaiMemory);
        
        // Setting up Cache Threshold
        jaiCache.setMemoryThreshold((float) jai.getMemoryThreshold());
        
        jaiDef.getTileScheduler().setParallelism(jai.getTileThreads());
        jaiDef.getTileScheduler().setPrefetchParallelism(jai.getTileThreads());
        jaiDef.getTileScheduler().setPriority(jai.getTilePriority());
        jaiDef.getTileScheduler().setPrefetchPriority(jai.getTilePriority());
        
        // Workaround for native mosaic BUG
//        Registry.setNativeAccelerationAllowed("Mosaic", jai.isAllowNativeMosaic(), jaiDef);
        

      //you have to link the tilecachetool project!!!
//      new TCTool((SunTileCache)JAI.getDefaultInstance().getTileCache());
      

    	if (LOGGER.isInfoEnabled())
    		LOGGER.info("JAI is set as following: "+jai.toString());
    }

}
