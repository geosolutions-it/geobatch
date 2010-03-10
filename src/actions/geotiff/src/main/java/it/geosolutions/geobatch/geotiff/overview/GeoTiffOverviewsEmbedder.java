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



package it.geosolutions.geobatch.geotiff.overview;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.Interpolation;

import org.geotools.utils.imageoverviews.OverviewsEmbedder;
import org.geotools.utils.progress.ExceptionEvent;
import org.geotools.utils.progress.ProcessingEvent;
import org.geotools.utils.progress.ProcessingEventListener;

/**
 * Comments here ...
 * 
 * @author Simone Giannechini, GeoSolutions
 * 
 * @version $GeoTIFFOverviewsEmbedder.java $ Revision: x.x $ 23/mar/07 11:42:25
 */
public class GeoTiffOverviewsEmbedder extends BaseAction<FileSystemMonitorEvent> implements
        Action<FileSystemMonitorEvent> {

    private GeoTiffOverviewsEmbedderConfiguration configuration;

    private final static Logger LOGGER = Logger
            .getLogger(GeoTiffOverviewsEmbedder.class.toString());

    public GeoTiffOverviewsEmbedder(GeoTiffOverviewsEmbedderConfiguration configuration)
            throws IOException {
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {

            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            
            // get the first event
            final FileSystemMonitorEvent event = events.peek();

            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            // //
            //
            // check the configuration and prepare the overviews embedder
            //
            // //
            int downsampleStep = configuration.getDownsampleStep();
            if (downsampleStep <= 0)
                throw new IllegalArgumentException("Illegal downsampleStep: " + downsampleStep);
            final String inputFileName = event.getSource().getAbsolutePath();
            int numberOfSteps = configuration.getNumSteps();
            if (numberOfSteps <= 0)
                throw new IllegalArgumentException("Illegal numberOfSteps: " + numberOfSteps);

            final OverviewsEmbedder oe = new OverviewsEmbedder();
            oe.setDownsampleStep(downsampleStep);
            oe.setNumSteps(configuration.getNumSteps());
            oe.setInterp(Interpolation.getInstance(configuration.getInterp()));
            oe.setScaleAlgorithm(configuration.getScaleAlgorithm());
            oe.setTileHeight(configuration.getTileH());
            oe.setTileWidth(configuration.getTileW());
            oe.setSourcePath(inputFileName);
//            oe.setTileCacheSize(configuration.getJAICapacity());

            // add logger/listener
            if (configuration.isLogNotification())
	            oe.addProcessingEventListener(new ProcessingEventListener() {
	
	                public void exceptionOccurred(ExceptionEvent event) {
	                    if (LOGGER.isLoggable(Level.SEVERE))
	                        LOGGER.info(event.getMessage());
	
	                }
	
	                public void getNotification(ProcessingEvent event) {
	                    if (LOGGER.isLoggable(Level.SEVERE))
	                        LOGGER.info(event.getMessage());
	
	                }
	            });
            // run
            oe.run();

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }

    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }

}
