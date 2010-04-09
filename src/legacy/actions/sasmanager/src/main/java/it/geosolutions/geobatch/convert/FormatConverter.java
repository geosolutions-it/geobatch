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

package it.geosolutions.geobatch.convert;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.base.Utils;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.geoserver.matfile5.sas.SasMosaicGeoServerGenerator;
import it.geosolutions.geobatch.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

/**
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class FormatConverter extends FormatConverterConfiguratorAction<FileSystemMonitorEvent> {

	private final static Logger LOGGER = Logger.getLogger(FormatConverter.class.toString());

	/**
	 * 
	 * @param configuration
	 */
    public FormatConverter(FormatConverterConfiguration configuration) throws IOException {
		super(configuration);
	}

    /**
     * 
     */
	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events) 
		throws ActionException {
     try {
         listenerForwarder.started();

            // TODO: Refactor this allowing empty queues
            // looking for file
            // if (events.size() != 1)
            // throw new IllegalArgumentException(
            // "Wrong number of elements for this action: "
            // + events.size());

            // data flow configuration and dataStore name must not be null.
            if (configuration == null) {
                LOGGER.severe("DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            final String inputFormats = configuration.getInputFormats();
            final String[] inputExtensions;
            if (inputFormats != null) {
                inputExtensions = inputFormats.split(":");
            } else {
                inputExtensions = null;
            }

            final String directory = configuration.getWorkingDirectory();
            final String outputDirectory = configuration.getOutputDirectory();

            // //
            //
            // Prepare the output directories structure
            //
            // //
            final File outputDir = new File(outputDirectory);
            if (!outputDir.exists()) {
                Utils.makeDirectories(outputDirectory);
            }
            
            // //
            //
            // Files Scan
            //
            // //
            final File fileDir = new File(directory);
            if (fileDir != null && fileDir.exists() && fileDir.isDirectory()) {
                final File files[] = fileDir.listFiles();

                if (files != null) {
                    final int numFiles = files.length;

                    if (LOGGER.isLoggable(Level.INFO)){
                        LOGGER.info(new StringBuilder("Found ")
                                .append(numFiles).append(" files").toString());
                    }
                    
                    final GeoTiffOverviewsEmbedderConfiguration gtovConfiguration = initGeotiffOverviewsEmbedderConfiguration();
                    
                    // //
                    // Check files for conversion
                    // //
                    for (int i = 0; i < numFiles; i++) {
                        final File file = files[i];
                        final String path = file.getAbsolutePath()
                                .toLowerCase();
                        if (inputExtensions != null) {
                            boolean accepted = false;
                            for (String ext : inputExtensions) {
                                if (path.endsWith(ext)) {
                                    accepted = true;
                                    break;
                                }
                            }
                            if (!accepted)
                                continue;
                        }

                        //A valid file has been found. Start conversion
                        final String name = FilenameUtils.getBaseName(path);
                        final String outputFileName = new StringBuilder(outputDirectory).append(Utils.SEPARATOR)
                        	.append(name).append(".tif").toString();
                        
                        // //
                        //
                        // 1) Conversion
                        //
                        // //
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.info(new StringBuilder("Converting file N. ").append(i + 1)
                                    .append(":").append(path).toString());
                        final File outFile = new File(outputFileName);
                        final boolean converted = convert(file, outFile);
                        if (converted){
                        	
	                        // //
	                        //
	                        // 2) Adding Overviews
	                        //
	                        // //
	                        Utils.addOverviews(outputFileName, gtovConfiguration);
	                        
	                        // //
	                        //
	                        // 3) Geoserver Ingestion
	                        //
	                        // //
	                        String runName = Utils.buildRunName(outFile.getParent(), configuration.getTime(), "");
	                        
	                        int index = runName.lastIndexOf(Utils.SEPARATOR);
	                        if (index == runName.length()-1){
	                        	//Removing the last separator
	                        	runName = runName.substring(0,runName.length()-1);
	                        	index = runName.lastIndexOf(Utils.SEPARATOR);
	                        }
	                        
	                        //Setting up the wmspath.
	                        //Actually it is set by simply changing mosaic's name underscores to slashes.
	                        //TODO: can be improved
	                        final String filePath = runName.substring(index+1, runName.length());
	                        final String wmsPath = SasMosaicGeoServerGenerator.buildWmsPath(filePath);
	                        SasMosaicGeoServerGenerator.ingest(outputFileName, wmsPath, configuration.getGeoserverURL(), 
	                        		configuration.getGeoserverUID(), configuration.getGeoserverPWD(), 
	                        		configuration.getGeoserverUploadMethod(), SasMosaicGeoServerGenerator.SAS_RAW_STYLE, "geotiff", configuration.getGeowebcacheWatchingDir());
	                        
                            //TODO: Add geowebcache ingestion
                        } else {
                        	if (LOGGER.isLoggable(Level.WARNING))
                        		LOGGER.warning("The following file hasn't been converted: " + outputFileName);
                        }
                    }
                }
            }
            
            listenerForwarder.completed();
            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }
    }

}
