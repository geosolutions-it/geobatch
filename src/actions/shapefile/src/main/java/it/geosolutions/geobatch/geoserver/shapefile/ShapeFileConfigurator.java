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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

/**
 * Geoserver ShapeFile Configuration action.
 *
 * <P>Process shapefiles and inject them into a Geoserver instance.
 * 
 * @author AlFa
 * @author ETj
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 */
public class ShapeFileConfigurator extends BaseAction<FileSystemMonitorEvent> implements Action<FileSystemMonitorEvent> {
	
	private final static Logger LOGGER = Logger.getLogger(ShapeFileConfigurator.class.toString());
	
	private File tempOutDir = null;
	private File zipFileToSend = null;

	private ShapeFileConfiguration configuration;
	
    public ShapeFileConfigurator(final ShapeFileConfiguration configuration)
            throws IOException {
    	super(configuration);
    	this.configuration = configuration;
    }

    /**
     * 
     */
	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws ActionException {

       listenerForwarder.setTask("config");
       listenerForwarder.started();

        try {
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            if (configuration == null) {
//                LOGGER.log(Level.SEVERE, "ActionConfig is null."); // we're rethrowing it, so don't log
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
//                LOGGER.log(Level.SEVERE, "Working directory is null."); // we're rethrowing it, so don't log
                throw new IllegalStateException("Working directory is null.");
            }

            if ( !workingDir.exists() || !workingDir.isDirectory()) {
//                LOGGER.log(Level.SEVERE, "Working directory does not exist ("+workingDir.getAbsolutePath()+")."); // we're rethrowing it, so don't log
                throw new IllegalStateException("Working directory does not exist ("+workingDir.getAbsolutePath()+").");
            }

			// Fetch the first event in the queue.
			// We may have one in these 2 cases:
			// 1) a single event for a .zip file
			// 2) a list of events for the .shp+.dbf+.shx+ some other optional files
			
            FileSystemMonitorEvent event = events.peek();

			File[] shpList;
			final boolean isZipped; 
			File zippedFile = null;
			if(events.size() == 1 && FilenameUtils.getExtension(event.getSource().getAbsolutePath()).equalsIgnoreCase("zip")) {
                listenerForwarder.progressing(5, "unzipping");
				zippedFile = event.getSource();
				shpList = handleZipFile(zippedFile, workingDir);
				isZipped = true;
			} else {
				shpList = handleShapefile(events);
				isZipped = false;
			}

			if(shpList == null)
				throw new Exception("Error while processing the shape file set");

            listenerForwarder.progressing(10, "In progress");
            
			// look for the main shp file in the set
			File shapeFile = null;
			for (File file : shpList) {
				if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("shp")) {
					shapeFile = file;
					break;
				}
			}

			if(shapeFile == null) {
//                LOGGER.log(Level.SEVERE, "Shp file not found in fileset."); // we're rethrowing it, so don't log
                throw new IllegalStateException("Shp file not found in fileset.");
			}
			final String shpBaseName; 
			if (!isZipped)
				shpBaseName = FilenameUtils.getBaseName(shapeFile.getName());
			else{
				shpBaseName = FilenameUtils.getBaseName(zippedFile.getName());
			}

            // //
            // creating dataStore
            // //
            DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

            // //
            // Convert Params into the kind of Map we actually need
            // //
            Map<String, Serializable> connectionParams = new HashMap<String, Serializable>(); // values used for connection

            /**
             * GeoServer url: "file:data/" + dataStoreId + "/" + shpFileName
             */
            try {
                connectionParams.put("url", shapeFile.toURI().toURL());
            } catch (MalformedURLException e) {
//                LOGGER.log(Level.SEVERE, "No valid ShapeFile URL found for this Data Flow: "
//                        + e.getLocalizedMessage()); // we're rethrowing it, so don't log
                throw new IllegalStateException("No valid ShapeFile URL found for this Data Flow: "
                        + e.getLocalizedMessage());
            }

            connectionParams.put("namespace", configuration.getNamespace());

            boolean validShape = factory.canProcess(connectionParams);
            factory = null;

            if (!validShape) {
//                LOGGER.log(Level.SEVERE, "No valid ShapeFile found for this Data Flow!"); // we're rethrowing it, so don't log
                throw new IllegalStateException("No valid ShapeFiles found for this Data Flow!");
            }

			// TODO: check if a layer with the same name already exists in GS

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////
            // http://localhost:8080/geoserver/rest/coveragestores/test_cv_store/test/file.tiff

            listenerForwarder.progressing(40, "Preparing shape");

			if(LOGGER.isLoggable(Level.FINE)) {
				StringBuilder sb = new StringBuilder("Packing shapefiles: ");
				for (File file : shpList) {
					sb.append('[').append(file.getName()).append(']');
				}
				LOGGER.fine(sb.toString());
			}
			if (isZipped) {
				zipFileToSend = zippedFile;
            } else {
                listenerForwarder.progressing(50, "Rezipping shape");
				zipFileToSend = IOUtils.deflate(workingDir, "sending_" + shpBaseName + System.currentTimeMillis(), shpList);
            }
			LOGGER.info("ZIP file: " + zipFileToSend.getAbsolutePath());

            listenerForwarder.progressing(100, "File prepared ... forwarding to the next action");
			
            // Removing old files...
            events.clear();
            // Adding the zipped file to send...
            events.add(new FileSystemMonitorEvent(zipFileToSend, FileSystemMonitorNotifications.FILE_ADDED));
			return events;
        } catch (Throwable t) {
//			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // we're rethrowing it, so don't log
            listenerForwarder.failed(t); // fails the Action
            throw new ActionException(this, t.getMessage(), t);
        } finally {
			// Clear unzipped files, if any
			if(tempOutDir != null) {
                try {
                    FileUtils.deleteDirectory(tempOutDir);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not delete temp dir: " + ex.getMessage(), ex);
                }
            }
		}
    }

	/**
	 * Pack the files received in the events into an array.
	 * 
	 * <P><B>TODO</B>: should we check if all the needed files are in place
	 *                 (such as in {@link handleZipFile(File,File)} ?
	 *
	 * @param events The received event queue
	 * @return
	 */
	private File[] handleShapefile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
	}

	/**
	 * Unzip and inspect the contained files, and check if they are a proper shapefileset.
	 *
	 * <P>We want the fileset in the zip file:<UL>
	 * <LI>To have all the same basename</LI>
	 * <LI>To include all of the mandatory files shp, shx, dbf</LI>
	 * <LI>To optionally include the prj file</LI>
	 * <LI>To have no other files than the ones described above.</LI></UL>
	 *
	 * @param source The source zip file.
	 * @return the array of unzipped files, or null if an error was encountered
	 */
	private File[] handleZipFile(File source, File workingdir) {

		tempOutDir = new File(workingdir, "unzip_"+System.currentTimeMillis());

		try{
			if(!tempOutDir.mkdir()) {
				throw new IOException("Can't create temp dir '"+tempOutDir.getAbsolutePath()+"'");
			}
			List<File> fileList = IOUtils.unzipFlat(source, tempOutDir);
			if(fileList == null) {
				throw new Exception("Error unzipping file");
			}

			if(fileList.isEmpty()) {
				throw new IllegalStateException("Unzip returned no files");
			}

			int shp=0, shx=0, dbf=0;
			int prj=0;

			// check that all the files have the same basename
			File file0 = fileList.get(0);
			String basename = FilenameUtils.getBaseName(file0.getName());
			for (File file : fileList) {
				if( ! basename.equals(FilenameUtils.getBaseName(file.getAbsolutePath()))) {
					throw new Exception("Basename mismatch (expected:'"+basename+"', file found:'"+file.getAbsolutePath()+"')");
				}
				String ext = FilenameUtils.getExtension(file.getAbsolutePath());
				// do we want such an hardcoded list?
				if("shp".equalsIgnoreCase(ext))
					shp++;
				else if("shx".equalsIgnoreCase(ext))
					shx++;
				else if("dbf".equalsIgnoreCase(ext))
					dbf++;
				else if("prj".equalsIgnoreCase(ext))
					prj++;
				else {
					// Do we want to be more lenient if unexpected/useless files are found?
					throw new IllegalStateException("Unexpected file extension in zipfile '"+ext+"'");
				}
			}

			if(shp*shx*dbf != 1) {
				throw new Exception("Bad fileset in zip file.");
			}

			return fileList.toArray(new File[fileList.size()]);

		} catch(Throwable t) {
			LOGGER.log(Level.WARNING, "Error examining zipfile", t);
			try {
				//org.apache.commons.io.IOUtils.
				FileUtils.forceDelete(tempOutDir);
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, "Can't delete temp dir '"+tempOutDir+"'", ex);
			}
			return null;
		}
	}

}

