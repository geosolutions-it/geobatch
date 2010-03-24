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
package it.geosolutions.geobatch.geoserver.rest;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ GeoServerRESTConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06
 */

public class GeoServerRESTConfiguratorAction extends GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(GeoServerRESTConfiguratorAction.class.toString());

    protected final GeoServerRESTActionConfiguration configuration;


	/**
	 * 
	 * @param configuration
	 */
	public GeoServerRESTConfiguratorAction(
			GeoServerRESTActionConfiguration configuration) {
		super(configuration);

		this.configuration = new GeoServerRESTActionConfiguration(configuration);
	}

    /**
     * 
     * @return
     */
    public GeoServerRESTActionConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 
     * @param events
     * @return
     * @throws ActionException
     */
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events) throws ActionException {
    	
    	listenerForwarder.setTask("config");
    	listenerForwarder.started();

    	try {
    		// ////////////////////////////////////////////////////////////////////
    		//
    		// Initializing input variables
    		//
    		// ////////////////////////////////////////////////////////////////////
    		if (configuration == null) {
    			//        LOGGER.log(Level.SEVERE, "ActionConfig is null."); // we're rethrowing it, so don't log
    			throw new IllegalStateException("ActionConfig is null.");
    		}

    		// ////////////////////////////////////////////////////////////////////
    		//
    		// Initializing input variables
    		//
    		// ////////////////////////////////////////////////////////////////////
    		final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
    				new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

    		// ////////////////////////////////////////////////////////////////////
    		//
    		// Checking input files.
    		//
    		// ////////////////////////////////////////////////////////////////////
    		if (workingDir == null) {
    			//        LOGGER.log(Level.SEVERE, "Working directory is null."); // we're rethrowing it, so don't log
    			throw new IllegalStateException("Working directory is null.");
    		}

    		if ( !workingDir.exists() || !workingDir.isDirectory()) {
    			//        LOGGER.log(Level.SEVERE, "Working directory does not exist ("+workingDir.getAbsolutePath()+")."); // we're rethrowing it, so don't log
    			throw new IllegalStateException("Working directory does not exist ("+workingDir.getAbsolutePath()+").");
    		}

    		// Fetch the first event in the queue.
    		listenerForwarder.progressing(10, "In progress");
            FileSystemMonitorEvent event = events.peek();
    		final File fileToSend = event.getSource();
			final String fileBaseName = FilenameUtils.getBaseName(fileToSend.getName());


    		// TODO: check if a layer with the same name already exists in GS

    		// ////////////////////////////////////////////////////////////////////
    		//
    		// SENDING data to GeoServer via REST protocol.
    		//
    		// ////////////////////////////////////////////////////////////////////
    		// http://localhost:8080/geoserver/rest/coveragestores/test_cv_store/test/file.tiff

    		listenerForwarder.progressing(40, "Preparing file");

    		LOGGER.info("Sending ShapeFile to GeoServer ... " + getConfiguration().getGeoserverURL());
    		Map<String, String> queryParams = new HashMap<String, String>();
    		queryParams.put("namespace", getConfiguration().getDefaultNamespace());
    		queryParams.put("wmspath",   getConfiguration().getWmsPath());

    		listenerForwarder.progressing(60, "Sending");
    		final String[] returnedLayer = GeoServerRESTHelper.send(
    				fileToSend, 
    				fileToSend, 
    				configuration.isVectorialLayer(),
    				configuration.getGeoserverURL(),
    				configuration.getGeoserverUID(),
    				configuration.getGeoserverPWD(),
    				configuration.getStoreId() != null ? configuration.getStoreId() : fileBaseName, 
    				configuration.getStoreFilePrefix() != null ? configuration.getStoreFilePrefix() : fileBaseName, 
    				queryParams, 
    				"",
    				configuration.getDataTransferMethod(),
    				configuration.getStoreType(), 
    				configuration.getGeoserverVersion(), 
    				configuration.getStyles(), 
    				configuration.getDefaultStyle()
    		);

    		if(returnedLayer != null) {
    			listenerForwarder.setProgress(100);
    			listenerForwarder.completed();
    			
    			events.add(new FileSystemMonitorEvent(fileToSend, FileSystemMonitorNotifications.FILE_ADDED));
    			return events;
    		} else {
    			throw new RuntimeException("Error configuring the layer on GeoServer");
    		}

    	} catch (Throwable t) {
    		//	LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // we're rethrowing it, so don't log
    		listenerForwarder.failed(t); // fails the Action
    		throw new ActionException(this, t.getMessage(), t);
    	} finally {
    	}
    }
	
	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ "["
				+ "cfg:"+getConfiguration()
				+ "]";
	}

}
