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
package it.geosolutions.geobatch.geoserver.shapefile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Collector;
import it.geosolutions.geobatch.tools.file.Extract;
import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Geoserver ShapeFile action.
 * 
 * Process shapefiles and inject them into a Geoserver instance.
 * 
 * @author AlFa
 * @author ETj
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class ShapeFileAction extends BaseAction<FileSystemEvent> implements Action<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShapeFileAction.class);

    private ShapeFileConfiguration configuration;

    public ShapeFileAction(final ShapeFileConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    /**
     * 
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {

        listenerForwarder.setTask("config");
        listenerForwarder.started();

        try {
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            if (configuration == null) {
                // LOGGER.error("ActionConfig is null."); // we're
                // rethrowing it, so don't log
                throw new IllegalStateException("ActionConfig is null.");
            }

            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            final File workingDir = Path.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if (workingDir == null) {
                // LOGGER.error("Working directory is null."); //
                // we're rethrowing it, so don't log
                throw new IllegalStateException("Working directory is null.");
            }

            if (!workingDir.exists() || !workingDir.isDirectory()) {
                // LOGGER.error(//
                // "Working directory does not exist ("+workingDir.getAbsolutePath()+").");
                // // we're rethrowing it, so don't log
                throw new IllegalStateException("Working directory does not exist ("
                        + workingDir.getAbsolutePath() + ").");
            }

            // Fetch the first event in the queue.
            // We may have one in these 2 cases:
            // 1) a single event for a .zip file
            // 2) a list of events for the .shp+.dbf+.shx+ some other optional
            // files

            FileSystemEvent event = events.peek();

            File zippedFile = null;
            if (events.size() == 1
                    && FilenameUtils.getExtension(event.getSource().getAbsolutePath())
                            .equalsIgnoreCase("zip")) {
                zippedFile = event.getSource();
            } else {
                throw new IllegalStateException("Input is not a zipped file");
            }

            listenerForwarder.progressing(5, "unzipping");
            String tmpDirName =Extract.extract(zippedFile.getAbsolutePath());
            
            File tmpDirFile =new File(tmpDirName);
            
            Collector coll=new Collector(FileFilterUtils.suffixFileFilter("shp",IOCase.INSENSITIVE));
            List<File> files=coll.collect(tmpDirFile);
            
            String shapeName=null;
            if (files.size()==1) {// TODO cecks >1
                shapeName=FilenameUtils.getBaseName(files.get(0).getName());
            }
            else
                throw new IllegalStateException("Shp file not found");

            listenerForwarder.progressing(10, "In progress");

            // TODO: check if the store do not exists and if so create it
            
            // TODO: check if a layer with the same name already exists in GS
            // GeoServerRESTReader reader = new GeoServerRESTReader(configuration.getGeoserverURL(),
            // configuration.getGeoserverUID(), configuration.getGeoserverPWD());

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("ZIP file: " + zippedFile.getAbsolutePath());
            
            GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
                    configuration.getGeoserverURL(), configuration.getGeoserverUID(),
                    configuration.getGeoserverPWD());
            
            
            
            if (publisher.publishShp(configuration.getWorkspace(), configuration.getStorename(),
                    shapeName, zippedFile)){
                final String message="Shape file SUCCESFULLY sent";
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(message);
                listenerForwarder.progressing(100, message);
            }
            else {
                final String message="Shape file FAILED to be sent";
                final ActionException ae=new ActionException(this, message);
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message, ae);
                listenerForwarder.failed(ae);
            }
            
            // Removing old files...
            events.clear();
            // Adding the zipped file to send...
            events.add(new FileSystemEvent(zippedFile, FileSystemEventType.FILE_ADDED));
            return events;
        } catch (Throwable t) {
            final ActionException ae=new ActionException(this, t.getMessage(), t);
            if (LOGGER.isErrorEnabled())
                LOGGER.error(ae.getLocalizedMessage(), ae);
            LOGGER.error(t.getLocalizedMessage(), t); // we're
            // rethrowing it, so don't log
            listenerForwarder.failed(t); // fails the Action
            throw ae;
        }
    }

}
