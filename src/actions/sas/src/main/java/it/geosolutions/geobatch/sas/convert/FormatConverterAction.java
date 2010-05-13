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

package it.geosolutions.geobatch.sas.convert;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration;
import it.geosolutions.geobatch.sas.base.SASDirNameParser;
import it.geosolutions.geobatch.sas.base.SASUtils;
import it.geosolutions.geobatch.sas.event.SASTileEvent;
import it.geosolutions.opensdi.sas.model.Layer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;

/**
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class FormatConverterAction
	extends BaseAction<FileSystemMonitorEvent>
	implements Action<FileSystemMonitorEvent> {

	private final static Logger LOGGER = Logger.getLogger(FormatConverterAction.class.toString());

    protected final FormatConverterConfiguration configuration;

	/**
	 * 
	 * @param configuration
	 */
    public FormatConverterAction(FormatConverterConfiguration configuration) {
        super(configuration);
		this.configuration = configuration;
	}

    /**
     *
     * @return a Queue<SASTileEvent> containing info about the tiles created
     */
	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
		throws ActionException {
     try {
         listenerForwarder.started();

        Queue<FileSystemMonitorEvent> retEvents = new LinkedList<FileSystemMonitorEvent>();

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
                SASUtils.makeDirectories(outputDirectory);
            }
            
            // //
            //
            // Files Scan
            //
            // //
            final File fileDir = new File(directory);
            if (fileDir != null && fileDir.exists() && fileDir.isDirectory()) {
                final File files[] = inputExtensions != null ? 
                    fileDir.listFiles((FilenameFilter)new SuffixFileFilter(inputExtensions, IOCase.INSENSITIVE)):
                    fileDir.listFiles();

                if (files != null) {
                    final int numFiles = files.length;

                    if (LOGGER.isLoggable(Level.INFO)){
                        LOGGER.info(new StringBuilder("Found ")
                                .append(numFiles).append(" files").toString());
                    }
                    
                    final GeoTiffOverviewsEmbedderConfiguration gtovConfiguration = 
                            FormatConverterUtils.initGeotiffOverviewsEmbedderConfiguration(configuration);
                    
                    // //
                    // Check files for conversion
                    // //
                    int i = 0;
                    for (File file : files) {
                        final String path = file.getAbsolutePath().toLowerCase();
                        // Added suffixFileFilter above
//                        if (inputExtensions != null) {
//                            boolean accepted = false;
//                            for (String ext : inputExtensions) {
//                                if (path.endsWith(ext)) {
//                                    accepted = true;
//                                    break;
//                                }
//                            }
//                            if (!accepted)
//                                continue;
//                        }

                        //A valid file has been found. Start conversion
                        final String name = FilenameUtils.getBaseName(path);
                        final String outputFileName = new StringBuilder(outputDirectory).append(SASUtils.SEPARATOR)
                        	.append(name).append(".tif").toString();
                        
                        // //
                        //
                        // 1) Conversion
                        //
                        // //
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.info(new StringBuilder("Converting file N. ").append(++i)
                                    .append(":").append(path).toString());
                        final File outFile = new File(outputFileName);
                        final Layer converted = FormatConverterUtils.convert(file, outFile,
                                    configuration.getTileW(), configuration.getTileH(),
                                    configuration.getOutputFormat());
                        if (converted != null){
                        	
	                        // //
	                        //
	                        // 2) Adding Overviews
	                        //
	                        // //
	                        SASUtils.addOverviews(outputFileName, gtovConfiguration);
	                        
	                        // //
	                        //
	                        // 3) Geoserver Ingestion
	                        //
	                        // //
	                        String runName = SASUtils.buildRunName(outFile.getParent(), configuration.getTime(), "");

                            if(runName.endsWith(File.separator))
                                runName = runName.substring(0,runName.length()-1);

	                        int index = runName.lastIndexOf(SASUtils.SEPARATOR);
	                        if (index == runName.length()-1){
	                        	//Removing the last separator
	                        	runName = runName.substring(0,runName.length()-1);
	                        	index = runName.lastIndexOf(SASUtils.SEPARATOR);
	                        }
	                        
	                        //Setting up the wmspath.
	                        //Actually it is set by simply changing mosaic's name underscores to slashes.
	                        //TODO: can be improved
	                        final String filePath = /* FilenameUtils.getName(runName); */
                                                    runName.substring(index+1, runName.length());
	                        final String wmsPath = SASUtils.buildWmsPath(filePath);

                            // Create event
                            SASTileEvent tileEvent = new SASTileEvent(outFile);
                            tileEvent.setWmsPath(wmsPath);
                            tileEvent.setFormat("geotiff");
                            tileEvent.setLayer(converted);
                            retEvents.offer(tileEvent);

                            SASDirNameParser nameParser = SASDirNameParser.parse(filePath);
                            if(nameParser != null) {
                                //tileEvent.setMissionName(FilenameUtils.getBaseName(outFile.getParentFile().getParentFile().getParent()));
                            	tileEvent.setMissionName(nameParser.getMission());
                                tileEvent.setDate(nameParser.getDate());
                            } else {
                            	LOGGER.severe("ATTENTION: Unparsable Mission name for Tile Event!");
                            }

//	                        SasMosaicGeoServerAction.ingest(outputFileName, wmsPath, configuration.getGeoserverURL(),
//	                        		configuration.getGeoserverUID(), configuration.getGeoserverPWD(),
//	                        		configuration.getGeoserverUploadMethod(), SasMosaicGeoServerAction.SAS_RAW_STYLE,
//                                        "geotiff", configuration.getGeowebcacheWatchingDir());
	                        
                            //TODO: Add geowebcache ingestion
                        } else {
                        	if (LOGGER.isLoggable(Level.WARNING))
                        		LOGGER.warning("The following file hasn't been converted: " + outputFileName);
                        }
                    }
                }
            }
            
            listenerForwarder.completed();
            return retEvents;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[");
		if (configuration != null)
			builder.append("configuration=").append(configuration);
		builder.append("]");
		return builder.toString();
	}

}
