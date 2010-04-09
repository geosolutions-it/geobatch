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

package it.geosolutions.geobatch.compose;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.convert.FormatConverter;
import it.geosolutions.geobatch.convert.FormatConverterConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.mosaic.Mosaicer;
import it.geosolutions.geobatch.mosaic.MosaicerConfiguration;

import java.io.IOException;
import java.util.EventObject;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.TileScheduler;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ DetectionManagerConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06
 */

public abstract class ComposerConfiguratorAction<T extends EventObject>
	extends BaseAction<T>
	implements Action<T> {
	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ComposerConfiguratorAction.class.toString());

    protected final ComposerConfiguration configuration;

    /**
     * Constructs a producer.
	 * The operation name will be the same than the parameter descriptor name.
     *
     * @throws IOException
     */
    public ComposerConfiguratorAction(ComposerConfiguration configuration) {
		super(configuration);
        this.configuration = configuration;
    }

    /**
     * 
     * @return
     */
    public ComposerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Compose a mosaic using the set of specified parameters.
     * @param events 
     * @param directory the directory containing raw tiles
     * 
     * @param outputDir the directory where to store the produced results.
     * @param compressionRatio the compression ratio to be used to compress output files.
     * @param compressionScheme the compression type
     * @param inputFormats the input formats to be converted and then mosaicked.
     * @param outputFormat the requested output format of conversion (As an instance, GeoTIFF)
     * @param tileW the inner image tiling width
     * @param tileH the inner image tiling height
     * @param numSteps the number of steps of overviews generation
     * @param downsampleStep the downsampling step between overviews
     * @param chunkW the width of each separated file composing the big final mosaic
     * @param chunkH the height of each separated file composing the big final mosaic
     * @param time the time of the tiles composing that mission. (Used to setup the output folder)
     * @param geoserverURL 
     * @param geoserverUID 
     * @param geoserverPWD 
     * @param geoserverUploadMethod 
     * @param corePoolSize 
     * @param maxPoolSize 
     * @param maxWaitingTime
     * @return the location where the mosaic have been created
     * @throws Exception
     */
    protected String composeMosaic(Queue<FileSystemMonitorEvent> events, final String directory, final String outputDir,
            final double compressionRatio, final String compressionScheme, 
            final String inputFormats, String outputFormat, final int tileW, final int tileH, 
            final int numSteps, final int downsampleStep, final String rawScaleAlgorithm, final String mosaicScaleAlgorithm,
            final int chunkW, final int chunkH, final String time, 
            final String geoserverURL, final String geoserverUID, final String geoserverPWD, final String geoserverUploadMethod, 
            final int corePoolSize, final int maxPoolSize, final long maxWaitingTime, final String geowebcacheWatchingDir) throws Exception {
        
        // //
        //
        // 1) Data conversion
        //
        // //
        final FormatConverterConfiguration converterConfig = new FormatConverterConfiguration();
        converterConfig.setWorkingDirectory(directory);
        converterConfig.setOutputDirectory(outputDir);
        converterConfig.setId("conv");
        converterConfig.setDescription("Mat5 to tiff converter");
        converterConfig.setCompressionRatio(compressionRatio);
        converterConfig.setCompressionScheme(compressionScheme);
        converterConfig.setInputFormats(inputFormats);
        converterConfig.setOutputFormat(outputFormat);
        converterConfig.setDownsampleStep(downsampleStep);
        converterConfig.setScaleAlgorithm(rawScaleAlgorithm);
        converterConfig.setNumSteps(numSteps);
        converterConfig.setTileH(tileH);
        converterConfig.setTileW(tileW);
        converterConfig.setTime(time);
        converterConfig.setGeoserverURL(geoserverURL);
        converterConfig.setGeoserverUID(geoserverUID);
        converterConfig.setGeoserverPWD(geoserverPWD);
        converterConfig.setGeoserverUploadMethod(geoserverUploadMethod);
        converterConfig.setGeowebcacheWatchingDir(geowebcacheWatchingDir);
        
        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.log(Level.INFO, "Ingesting MatFiles in the mosaic composer");
        
        final FormatConverter converter = new FormatConverter(converterConfig);
        Queue<FileSystemMonitorEvent> proceed = converter.execute(events);
        
        if (proceed == null){
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE, "Unable to proceed with the mosaic composition due to problems occurred during conversion");
        	return "";
        }
        // //
        //
        // 2) Mosaic Composition
        //
        // //
        final MosaicerConfiguration mosaicerConfig = new MosaicerConfiguration();
        mosaicerConfig.setCompressionRatio(compressionRatio);
        mosaicerConfig.setCompressionScheme(compressionScheme);
        mosaicerConfig.setId("mosaic");
        mosaicerConfig.setDescription("Mosaic composer");
        mosaicerConfig.setNumSteps(numSteps);
        mosaicerConfig.setDownsampleStep(downsampleStep);
        mosaicerConfig.setScaleAlgorithm(mosaicScaleAlgorithm);
        mosaicerConfig.setWorkingDirectory(outputDir);
        mosaicerConfig.setTileH(tileH);
        mosaicerConfig.setTileW(tileW);
        mosaicerConfig.setChunkHeight(chunkH);
        mosaicerConfig.setChunkWidth(chunkW);
        mosaicerConfig.setTime(time);
        mosaicerConfig.setCorePoolSize(corePoolSize);
        mosaicerConfig.setMaxPoolSize(maxPoolSize);
        mosaicerConfig.setMaxWaitingTime(maxWaitingTime);

        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.log(Level.INFO, "Mosaic Composition");
        final Mosaicer mosaicer = new Mosaicer(mosaicerConfig);
        proceed = mosaicer.execute(proceed);
        if (proceed == null){
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.severe( "Unable to proceed with the mosaic ingestion due to problems occurred during mosaic composition");
        	return "";
        }
        return mosaicerConfig.getMosaicDirectory();
    }

    
    /**
     * Set JAI Hints from the current configuration
     * @param configuration
     */
    protected void setJAIHints(final ComposerConfiguration configuration) {
        if (configuration!=null){
            final JAI jaiDef = JAI.getDefaultInstance();

            final TileCache cache = jaiDef.getTileCache();
            final long cacheSize = configuration.getJAICacheCapacity();
            cache.setMemoryCapacity(cacheSize*1024*1024);
//            cache.setMemoryThreshold(configuration.getJAICacheThreshold());

            final TileScheduler scheduler = jaiDef.getTileScheduler();
            scheduler.setParallelism(configuration.getJAIParallelism());
            scheduler.setPrefetchParallelism(configuration.getJAIParallelism());
            
            ImageIO.setUseCache(false);
        }
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DetectionManagerConfiguratorAction [");
		if (configuration != null)
			builder.append("configuration=").append(configuration);
		builder.append("]");
		return builder.toString();
	}

}
