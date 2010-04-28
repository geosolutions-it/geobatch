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

package it.geosolutions.geobatch.sas.compose;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.sas.base.BaseImageProcessingConfiguration;
import it.geosolutions.geobatch.sas.convert.FormatConverterAction;
import it.geosolutions.geobatch.sas.convert.FormatConverterConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.sas.mosaic.MosaicerAction;
import it.geosolutions.geobatch.sas.mosaic.MosaicerConfiguration;

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

public class SASComposerUtils {
	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(SASComposerUtils.class.toString());

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
    static Queue<FileSystemMonitorEvent> processTiles(Queue<FileSystemMonitorEvent> events,
            final BaseImageProcessingConfiguration imageProcessingConfiguration,
            final String directory, final String outputDir, 
//            final double compressionRatio, final String compressionScheme,
            final String inputFormats, String outputFormat,
//            final int tileW, final int tileH,
//            final int numSteps, final int downsampleStep,
            final String rawScaleAlgorithm
//            final String mosaicScaleAlgorithm,
//            final int chunkW, final int chunkH, final String time,
//            final String geoserverURL, final String geoserverUID, final String geoserverPWD, final String geoserverUploadMethod,
//            final int corePoolSize, final int maxPoolSize, final long maxWaitingTime,
//            final String geowebcacheWatchingDir
            ) throws Exception {
        
        // //
        //
        // 1) Data conversion
        //
        // //
        final FormatConverterConfiguration converterConfig = new FormatConverterConfiguration(imageProcessingConfiguration);
        converterConfig.setWorkingDirectory(directory);
        converterConfig.setOutputDirectory(outputDir);
        converterConfig.setId("conv");
        converterConfig.setDescription("Mat5 to tiff converter");
//        converterConfig.setCompressionRatio(compressionRatio);
//        converterConfig.setCompressionScheme(compressionScheme);
        converterConfig.setInputFormats(inputFormats);
        converterConfig.setOutputFormat(outputFormat);
//        converterConfig.setDownsampleStep(downsampleStep);
        converterConfig.setScaleAlgorithm(rawScaleAlgorithm);
//        converterConfig.setNumSteps(numSteps);
//        converterConfig.setTileH(tileH);
//        converterConfig.setTileW(tileW);
//        converterConfig.setTime(time);
//        converterConfig.setGeoserverURL(geoserverURL);
//        converterConfig.setGeoserverUID(geoserverUID);
//        converterConfig.setGeoserverPWD(geoserverPWD);
//        converterConfig.setGeoserverUploadMethod(geoserverUploadMethod);
//        converterConfig.setGeowebcacheWatchingDir(geowebcacheWatchingDir);
        
        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.log(Level.INFO, "Ingesting MatFiles in the mosaic composer");
        
        final FormatConverterAction converter = new FormatConverterAction(converterConfig);
        Queue<FileSystemMonitorEvent> sasTileEvents = converter.execute(events);
        return sasTileEvents;
    }
        
    static String composeMosaic(Queue<FileSystemMonitorEvent> events,
            final BaseImageProcessingConfiguration imageProcessingConfiguration,
            final String outputDir,
            final String mosaicScaleAlgorithm
            ) throws Exception {

        // //
        //
        // 2) Mosaic Composition
        //
        // //
        final MosaicerConfiguration mosaicerConfig = new MosaicerConfiguration(imageProcessingConfiguration);
        mosaicerConfig.setId("mosaic");
        mosaicerConfig.setDescription("Mosaic composer");
        mosaicerConfig.setScaleAlgorithm(mosaicScaleAlgorithm);
        mosaicerConfig.setWorkingDirectory(outputDir);

        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.log(Level.INFO, "Mosaic Composition");
        final MosaicerAction mosaicer = new MosaicerAction(mosaicerConfig);
        events = mosaicer.execute(events); // throws ActionException

        if (events == null){
        	return null;
        } else
            return mosaicerConfig.getMosaicDirectory();
    }
    
    /**
     * Set JAI Hints from the current configuration
     * @param configuration
     */
    static void setJAIHints(final SASComposerConfiguration configuration) {
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

}
