/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSWorkspaceEncoder;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import it.geosolutions.tool.errorhandling.ActionExceptionHandler;
import it.geosolutions.tools.io.file.Collector;
import it.geosolutions.tools.io.file.Copy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.geotools.gce.imagemosaic.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action which is able to create and update a layer into the GeoServer
 * 
 * @author AlFa (r1)
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it (r2,r3)
 * @author Simone Giannecchini, GeoSolutions SAS
 * 
 * @version <br>
 *          $ ImageMosaicConfiguratorAction.java Rev: 0.1 $ 12/feb/07 <br>
 *          $ [Renamed] ImageMosaicAction.java $ Rev: 0.2 $ 25/feb/11 <br>
 *          $ ImageMosaicAction.java $ Rev: 0.3 $ 8/jul/11
 */

@Action(configurationClass=ImageMosaicConfiguration.class,configurationAlias="ImageMosaicActionConfiguration")
public class ImageMosaicAction extends BaseAction<EventObject> {

	/** Seconds to wait for nfs propagation.*/
	public final static int DEFAULT_COPY_WAIT = 10;

	/** Default behavior with geoserver rest.*/
	public final static boolean DEFAULT_RESET_BEHAVIOR=true;

	/**
	 * Default logger
	 */
	protected final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicAction.class);

	/**
	 * Constructs a producer. The operation name will be the same than the
	 * parameter descriptor name.
	 * 
	 * @throws IOException
	 */
	public ImageMosaicAction(ImageMosaicConfiguration configuration) {
		super(configuration);
	}

	@Override
	public boolean checkConfiguration() {
		if (getConfiguration() == null) {
			final String message = "ImageMosaicAction: DataFlowConfig is null.";
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message,new IllegalStateException(message));
			return false;
		} else if ((getConfiguration().getGeoserverURL() == null)) {
			final String message = "GeoServerURL is null.";
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message,new IllegalStateException(message));
			return false;
		} else if (getConfiguration().getGeoserverURL().isEmpty()) {
			final String message = "GeoServerURL is empty.";
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message,new IllegalStateException(message));
			return false;
		}
		return true;
	}

	/**
	 * Public or update an ImageMosaic layer on the specified GeoServer
	 */
	public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Start processing...");

		listenerForwarder.started();

		try {
			// looking for file
			if (events == null)
				throw new IllegalArgumentException("Unable to execute action with incoming null parameter");
			if (events.size() == 0)
				throw new IllegalArgumentException("Wrong number of elements for this action: "
						+ events.size());

			/*
			 * If here: we can execute the action
			 */
			Queue<EventObject> ret = new LinkedList<EventObject>();

			/**
			 * For each event into the queue
			 */
			while (events.size() > 0) {
				final Object evObj = events.remove();

				/**
				 * If the input file exists and it is a file: Check if it is: -
				 * A Directory - An XML -> Serialized ImageMosaicCommand
				 * 
				 * Building accordingly the ImageMosaicCommand command.
				 */
				final ImageMosaicCommand cmd;

				if (evObj == null) {
					ActionExceptionHandler.handleError(getConfiguration(),this,"Input null object.");
					continue;
				}

				if (evObj instanceof FileSystemEvent) {
					/*
					 * Checking input files.
					 */
					final File input = ((FileSystemEvent)evObj).getSource();
					if (!input.exists()) {
						// no file is found for this event try with the next one
						ActionExceptionHandler.handleError(getConfiguration(),this,"The input file does not exists at url: " + input.getAbsolutePath());
						continue;
					}

					/**
					 * the file event points to an XML file...
					 * 
					 * @see ImageMosaicCommand
					 */
					if (input.isFile() && FilenameUtils.getExtension(input.getName()).equalsIgnoreCase("xml")) {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Working on an XML command file: " + input.getAbsolutePath());
						}

						// try to deserialize
						cmd = ImageMosaicCommand.deserialize(input.getAbsoluteFile());
						if (cmd == null) {
							ActionExceptionHandler.handleError(getConfiguration(),this,"Unable to deserialize the passed file: " + input.getAbsolutePath());
							continue;
						}

					} else if (input.isDirectory()) {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Input file event points to a directory: " + input.getAbsolutePath());
						}
						String format = ((ImageMosaicConfiguration)super.getConfiguration()).getGranuleFormat();
						if (format==null || format.isEmpty()){
							LOGGER.warn("No granule format specified in flow configuration... try force it to .tif");
							format="tif";
						}
						StringBuilder builder = new StringBuilder();
						builder.append("*.");
						builder.append(format);
						final Collector coll = new Collector(new WildcardFileFilter(builder.toString(), IOCase.INSENSITIVE));
						// try to deserialize
						cmd = new ImageMosaicCommand(input, coll.collect(input), null);
					} else {
						// the file event does not point to a directory nor to an xml file
						ActionExceptionHandler.handleError(getConfiguration(),this,"The file event does not point to a directory nor to an xml file: " + input.getAbsolutePath());
						continue;
					}
				} else if (evObj instanceof EventObject) {
					Object innerObject=((EventObject)evObj).getSource();
					if (innerObject instanceof ImageMosaicCommand){
						cmd = (ImageMosaicCommand)innerObject;
					} else {
						// the file event does not point to a directory nor to an xml file
						ActionExceptionHandler.handleError(getConfiguration(),this,"The file event does not point to a valid object: " + evObj);
						continue;
					}
				} else {
					// the file event does not point to a directory nor to an xml file
					ActionExceptionHandler.handleError(getConfiguration(),this,"The file event does not point to a valid object: " + evObj);
					continue;
				}

				/**
				 * the file pointing to the directory which the layer will refer
				 * to.
				 */
				final File baseDir = cmd.getBaseDir();
				/**
				 * a descriptor for the mosaic to handle
				 */
				final ImageMosaicGranulesDescriptor mosaicDescriptor = ImageMosaicGranulesDescriptor
						.buildDescriptor(baseDir, getConfiguration());

				if (mosaicDescriptor == null) {
					ActionExceptionHandler.handleError(getConfiguration(),this,"Unable to build the imageMosaic descriptor" + cmd.getBaseDir());
					continue;
				}

				// Perform tests on the base dir file
				if (!baseDir.exists() || !baseDir.isDirectory()) {
					// no base dir exists try to build a new one using
					// addList()
					if (cmd.getAddFiles() != null) {
						if (cmd.getAddFiles().size() > 0) {
							// try build the baseDir
							if (!baseDir.mkdirs()) {
								ActionExceptionHandler.handleError(getConfiguration(),this,"Unable to create the base directory named \'" + baseDir.getAbsolutePath() + "\'.");
								continue;
							}
						} else {
							final StringBuilder msg = new StringBuilder();
							msg.append("Unexpected not existent baseDir for this layer '").append(baseDir.getAbsolutePath()).append("'.\n If you want to build a new layer try using an ").append("existent or writeable baseDir and append a list of file to use to the addFile list.");
							ActionExceptionHandler.handleError(getConfiguration(),this,msg.toString());
							continue;
						}
					} else {
						final StringBuilder msg = new StringBuilder();
						msg.append("Unexpected not existent baseDir for this layer '").append(baseDir.getAbsolutePath()).append("'.\n If you want to build a new layer try using an ").append("existent or writeable baseDir and append a list of file to use to the addFile list.");
						ActionExceptionHandler.handleError(getConfiguration(),this,msg.toString());
						continue;
					}
				}

				// override local cmd null params with the getConfiguration()
				cmd.copyConfigurationIntoCommand(getConfiguration());

				// prepare configuration for layername and storename
				final String layerName;
				if (cmd.getLayerName()==null){
					layerName=baseDir.getName();
					cmd.setLayerName(layerName);
				} else {
					layerName=cmd.getLayerName();
				}
				final String storeName;
				if (cmd.getStoreName()==null){
					storeName=layerName;
					cmd.setStoreName(storeName);
				} else {
					storeName=cmd.getStoreName();
				}

				/**
				 * HERE WE HAVE A 'cmd' COMMAND FILE WHICH MAY HAVE GETADDFILE
				 * OR GETDELFILE !=NULL USING THOSE LIST WE MAY:<br>
				 * DEL ->DELETE FROM THE DATASTORE AN IMAGE USING THE ABSOLUTE
				 * PATH.<br>
				 * ADD ->INSERT INTO THE DATASTORE AN IMAGE USING THE ABSOLUTE
				 * PATH.<br>
				 */
				// REST library read
				GeoServerRESTReader gsReader = new GeoServerRESTReader(cmd.getGeoserverURL(),
						cmd.getGeoserverUID(),
						cmd.getGeoserverPWD());
				// REST library write
				final GeoServerRESTPublisher gsPublisher = new GeoServerRESTPublisher(
						cmd.getGeoserverURL(),
						cmd.getGeoserverUID(),
						cmd.getGeoserverPWD());

				final String workspace = cmd.getDefaultNamespace() != null  ? cmd.getDefaultNamespace(): "";

				/*
				 * Check if ImageMosaic layer already exists...
				 */
				final boolean layerExists;

				if(cmd.getIgnoreGeoServer()) {
					if(LOGGER.isInfoEnabled()) {
						LOGGER.info("GeoServer will be ignored by configuration. Assuming that an updated is required. ");
					}
					layerExists = true;
				} else {
					final RESTLayer layer = cmd.getIgnoreGeoServer()? null: gsReader.getLayer(layerName);
					layerExists = layer != null;
				}

				if ( layerExists ) {
					if ( ! updateMosaicLayer(cmd, baseDir, layerName, mosaicDescriptor, gsPublisher)){
						ActionExceptionHandler.handleError(getConfiguration(),this,"Mosaic not Updated...");
						continue;
					}

				} else {
					if ( ! createMosaicLayer(cmd, baseDir, workspace, mosaicDescriptor, layerName, gsPublisher, storeName)){
						ActionExceptionHandler.handleError(getConfiguration(),this,"Mosaic not Created...");
						continue;
					}
				}

				/**
				 * The returned file: - one for each event - .layer file - will
				 * be added to the output queue
				 */
				final File layerDescriptor;

				// generate a RETURN file and append it to the return queue
				// TODO get info about store and workspace name...
				if ((layerDescriptor = ImageMosaicOutput.writeReturn(baseDir, baseDir, cmd)) != null) {
					ret.add(new FileSystemEvent(layerDescriptor, FileSystemEventType.FILE_ADDED));
				}

			} // while

			listenerForwarder.completed();

			// ... setting up the appropriate event for the next action
			return ret;

		} catch (Exception t) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(t.getLocalizedMessage(), t);
			listenerForwarder.failed(t);
			throw new ActionException(this, t.getMessage(), t);
		} 
	}

	/**
	 * @param queryParams
	 * @return
	 */
	protected static String getQueryString(Map<String, String> queryParams) {
		StringBuilder queryString = new StringBuilder();

		if (queryParams != null)
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				if (queryString.length() > 0)
					queryString.append("&");
				queryString.append(entry.getKey()).append("=").append(entry.getValue());
			}

		return queryString.toString();
	}

	public ImageMosaicConfiguration getConfiguration() {
		return (ImageMosaicConfiguration)super.getConfiguration();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + "cfg:" + getConfiguration() + "]";
	}

	private boolean createMosaicLayer(final ImageMosaicCommand cmd, final File baseDir, final String workspace, final ImageMosaicGranulesDescriptor mosaicDescriptor, final String layerName, final GeoServerRESTPublisher gsPublisher, final String storeName) throws NullPointerException, ActionException, FileNotFoundException, IllegalArgumentException, IOException {
		// layer does not exists so try to create a new one
		// looking for datastore.properties
		final File datastore = ImageMosaicProperties.checkDataStore(cmd, getConfigDir(), baseDir);
		if (datastore == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Failed to check for datastore.properties into:" + baseDir);
			}
		}
		// looking for indexer.properties
		final File indexer = new File(baseDir, "indexer.properties");
		final Properties indexerProp = ImageMosaicProperties.buildIndexer(indexer,
				cmd);
		if (indexerProp == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Failed to check for indexer.properties into:" + baseDir);
			}
			// error occurred
			return false;
		}
		// store addeddFiles for rollback purposes
		List<File> addedFiles = null;
		// no base dir exists try to build a new one using
		// addList()
		if (cmd.getAddFiles() != null) {
			if (cmd.getAddFiles().size() > 0) {
				// copy files from the addFile list to the
				// baseDir (do not
				// preventing overwrite)
				addedFiles = Copy.copyListFileToNFS(cmd.getAddFiles(), cmd.getBaseDir(), true,
						cmd.getNFSCopyWait());
				if (addedFiles == null || addedFiles.size() == 0) {
					// no file where transfer to the
					// destination dir
					if (LOGGER.isWarnEnabled())
						LOGGER.warn("No file were transferred to the destination dir,"
								+ " check your command.");
					return false;
				}
				// files are now into the baseDir and layer
				// do not exists so
				cmd.getAddFiles().clear();
			}
			// Already checked else {
			// if (LOGGER.isWarnEnabled())
			// LOGGER.warn("ImageMosaicAction: Unexpected not existent baseDir for this layer '"
			// + baseDir.getAbsolutePath()
			// +
			// "'. If you want to build a new layer try using an "
			// +
			// "existent or writeable baseDir and append a list of file to use to the addFile list.");
			// continue;
			// }
			// TODO!!!!
			if (cmd.getDelFiles() != null) {
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("Unable to delete files from a not existent layer,"
							+ " delFile list will be ignored.");
				// files are now into the baseDir and layer
				// do not exists so
				cmd.getDelFiles().clear();
			}
		} // build baseDir using AddFiles
		// workspace encoder
		final GSWorkspaceEncoder workspaceEnc = new GSWorkspaceEncoder();
		workspaceEnc.setName(workspace);
		// coverage encoder
		final GSCoverageEncoder coverageEnc = ImageMosaicREST.createGSImageMosaicEncoder(mosaicDescriptor, cmd);
		coverageEnc.setName(layerName);
		// layerEnc.setWmsPath(cmd.getWmsPath()!=null?cmd.getWmsPath():"");
		// layer encoder
		final GSLayerEncoder layerEnc = new GSLayerEncoder();
		String style=cmd.getDefaultStyle();
		if (style==null || style.isEmpty()){
			style="raster";
		}
		layerEnc.setDefaultStyle(style);
		// create a new ImageMosaic layer...
		final boolean published = gsPublisher.publishExternalMosaic(workspace, storeName, baseDir,coverageEnc, layerEnc);
		/**
		 * TODO gsPublisher.createExternalMosaic TODO
		 * gsPublisher.publishLayer
		 * gsPublisher.publishExternalMosaic(???
		 * cmd.getDefaultNamespace(),
		 * layerID,baseDir, cmd.getCrs(),
		 * cmd.getDefaultStyle());
		 */
		if (!published) {
			final String msg="Error creating the new store: " + layerName;
			ActionExceptionHandler.handleError(getConfiguration(),this,msg);
			return false;
		}
		return true;
	}

	protected boolean updateMosaicLayer(final ImageMosaicCommand cmd, final File baseDir, final String layerName, final ImageMosaicGranulesDescriptor mosaicDescriptor, final GeoServerRESTPublisher gsPublisher) throws IOException, NullPointerException, ActionException {
		// layer exists
		/*
		 * looking for datastore.properties
		 */
		final File datastore = ImageMosaicProperties.checkDataStore(cmd, getConfigDir(), baseDir);
		if (datastore == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Failed to check for datastore.properties");
			}
			// error occurred
			return false;
		}
		if (!Utils.checkFileReadable(datastore)) {
			/*
			 * File 'datastore.properties' does not exists. 
			 * Probably we have a ShapeFile as datastore for this layer.
			 * Error unable to UPDATE the shape file.
			 */
			// SHAPEFILE
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("STILL NOT IMPLEMENTED: unable to UPDATE a shape file.");
			}
			return false;
		}
		// read the properties file
		final Properties dataStoreProp = ImageMosaicProperties.getPropertyFile(datastore);
		/**
		 * This file is generated by the GeoServer and we need it to
		 * get: LocationAttribute -> the name of the attribute
		 * indicating the file location AbsolutePath -> a boolean
		 * indicating if file locations (paths) are absolutes
		 *
		 * 20101014T030000_pph.properties
		 *
		 * AbsolutePath=false Name=20101014T030000_pph
		 * ExpandToRGB=false LocationAttribute=location
		 */
		File mosaicPropFile = new File(baseDir, layerName + ".properties");
		if (!Utils.checkFileReadable(mosaicPropFile)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Unable to locate the imagemosaic properties file at: "
						+ mosaicPropFile.getCanonicalPath());
			}
			// ETj: the prop file may be named as the mosaic dir name:
			final String upperParent = cmd.getBaseDir().getName();
			mosaicPropFile = new File(baseDir, upperParent + ".properties");
			if (!Utils.checkFileReadable(mosaicPropFile)) {
				ActionExceptionHandler.handleError(getConfiguration(),this,"Unable to locate the imagemosaic properties file at: " + mosaicPropFile.getCanonicalPath());
			}
		}
		final Properties mosaicProp = ImageMosaicProperties.getPropertyFile(mosaicPropFile);
		// update
		if (ImageMosaicUpdater.updateDataStore(mosaicProp, dataStoreProp, mosaicDescriptor, cmd)) {
			//
			// Clear GeoServer cached readers if needed. This might be important when the BBOX has grown or shrunk
			// due to an add or remove operation.
			//
			if (cmd.getIgnoreGeoServer()) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("GeoServer is disabled by configuration. Reset will not be performed. ");
				}
			} else if (cmd.getFinalReset()) {
				// SUCCESS update the store
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Reset GeoServer Cache");
				}
				if (gsPublisher.reset()) {
					// SUCCESS update the Catalog
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Reset DONE");
					}
				} else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("GeoServer failed to reset cached readers.");
					}
					return false;
				}
			}
		} else {
			ActionExceptionHandler.handleError(getConfiguration(),this,"The following command FAILED:\n" + cmd.toString() + "\n");
			return false;
		}
		return true;
	}
}
