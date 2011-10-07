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
package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.tools.file.Collector;
import it.geosolutions.geobatch.tools.file.Path;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageStore;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSWorkspaceEncoder;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action which is able to create and update a layer into the GeoServer
 * 
 * @author AlFa (r1)
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it (r2,r3)
 * 
 * @version <br>
 *          $ ImageMosaicConfiguratorAction.java Rev: 0.1 $ 12/feb/07 <br>
 *          $ [Renamed] ImageMosaicAction.java $ Rev: 0.2 $ 25/feb/11 <br>
 *          $ ImageMosaicAction.java $ Rev: 0.3 $ 8/jul/11
 */

public class ImageMosaicAction extends BaseAction<FileSystemEvent> {

	protected final static int WAIT = 10; // seconds to wait for nfs propagation

	/**
	 * Default logger
	 */
	protected final static Logger LOGGER = LoggerFactory
			.getLogger(ImageMosaicAction.class);

	protected final ImageMosaicConfiguration configuration;

	/**
	 * Constructs a producer. The operation name will be the same than the
	 * parameter descriptor name.
	 * 
	 * @throws IOException
	 */
	public ImageMosaicAction(ImageMosaicConfiguration configuration) {
		super(configuration);
		this.configuration = configuration;
	}

	/**
	 * Public or update an ImageMosaic layer on the specified GeoServer
	 */
	public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
			throws ActionException {

		if (LOGGER.isInfoEnabled())
			LOGGER.info("ImageMosaicAction: Starting with processing...");

		listenerForwarder.started();

		try {
			// looking for file
			if (events.size() == 0)
				throw new IllegalArgumentException(
						"ImageMosaicAction: Wrong number of elements for this action: "
								+ events.size());

			/*
			 * If here: we can execute the action
			 */
			Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();

			/**
			 * For each event into the queue
			 */
			while (events.size() > 0) {
				final FileSystemEvent event = events.remove();

				/**
				 * If the input file exists and it is a file: Check if it is: -
				 * A Directory - An XML -> Serialized ImageMosaicCommand
				 * 
				 * Building accordingly the ImageMosaicCommand command.
				 */
				final ImageMosaicCommand cmd;


				/*
				 * Checking input files.
				 */
				final File input = event.getSource();
				if (input == null) {
					if (LOGGER.isWarnEnabled())
						LOGGER.warn("ImageMosaicAction: The input file event points to a null file object.");
					// no file is found for this event try with the next one
					continue;
				}
				
				// if the input exists
				if (input.exists()) {
					
					/**
					 * the file event points to an XML file...
					 * 
					 * @see ImageMosaicCommand
					 */
					if (input.isFile()
							&& FilenameUtils.getExtension(input.getName())
									.equalsIgnoreCase("xml"))
					{
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("ImageMosaicAction: working on an XML command file: "
									+ input.getAbsolutePath());
						}

						// try to deserialize
						cmd = ImageMosaicCommand.deserialize(input.getAbsoluteFile());
						if (cmd == null) {
							if (LOGGER.isWarnEnabled())
								LOGGER.warn("ImageMosaicAction: Unable to deserialize the passed file: "
										+ input.getAbsolutePath());
							continue;
						}
						
						// override local configuration with the command one
						cmd.overrideImageMosaicConfiguration(configuration);
						
					}
					else if (input.isDirectory()) {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("ImageMosaicAction: Input file event points to a directory: "
									+ input.getAbsolutePath());
						}
						final Collector coll=new Collector(null);
						// try to deserialize
						cmd = new ImageMosaicCommand(input,coll.collect(input),null);
					}
					else {
						// the file event do not point to a directory nor to an
						// xml file
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("ImageMosaicAction: the file event do not point to a directory nor to an xml file: "
									+ input.getAbsolutePath());
						}
						continue;
					}

					/**
					 * the file pointing to the directory which the layer will refer
					 * to.
					 */
					final File baseDir= cmd.getBaseDir();
					final String layerID= baseDir.getName();
					
										/**
					 * a descriptor for the mosaic to handle
					 */
					final ImageMosaicGranulesDescriptor mosaicDescriptor = ImageMosaicGranulesDescriptor
							.buildDescriptor(baseDir, configuration);

					if (mosaicDescriptor == null) {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("ImageMosaicAction: Unable to build the imageMosaic descriptor"
									+ input.getAbsolutePath());
						}
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
									if (LOGGER.isWarnEnabled())
										LOGGER.warn("ImageMosaicAction: unable to create the base directory named \'"
												+ baseDir.getAbsolutePath()
												+ "\'.");
									continue;
								}
							} else {
								if (LOGGER.isWarnEnabled())
									LOGGER.warn("ImageMosaicAction: Unexpected not existent baseDir for this layer '"
											+ baseDir.getAbsolutePath()
											+ "'.\n If you want to build a new layer try using an "
											+ "existent or writeable baseDir and append a list of file to use to the addFile list.");
								continue;
							}
						} else {
							if (LOGGER.isWarnEnabled())
								LOGGER.warn("ImageMosaicAction: Unexpected not existent baseDir for this layer '"
										+ baseDir.getAbsolutePath()
										+ "'.\n If you want to build a new layer try using an "
										+ "existent or writeable baseDir and append a list of file to use to the addFile list.");
							continue;
						}
					}
					
					/*
					 * TODO HERE WE HAVE A 'cmd' COMMAND FILE WHICH MAY
					 * HAVE GETADDFILE OR GETDELFILE !=NULL USING THOSE
					 * LIST WE MAY: DEL ->LOG WARNING--- ADD ->INSERT
					 * INTO THE DATASTORE AN IMAGE USING THE ABSOLUTE
					 * PATH.
					 */
					
					// REST library
					GeoServerRESTReader gsReader = new GeoServerRESTReader(
							getConfiguration().getGeoserverURL(),
							getConfiguration().getGeoserverUID(),
							getConfiguration().getGeoserverPWD());

					final String workspace=configuration.getDefaultNamespace()!=null?configuration.getDefaultNamespace():"";
					
					/*
					 * Check if ImageMosaic layer already exists...
					 */
					final boolean layerExists;
					final RESTLayer layer= gsReader.getLayer(layerID);
					if (layer == null)
						layerExists = false;
					else
						layerExists = true;

					if (!layerExists) {
					
						/*
						 * CHECKING FOR datastore.properties
						 */
						final File datastore = ImageMosaicProperties.checkDataStore(configuration, baseDir);
						if (datastore == null) {
							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn("ImageMosaicAction: failed to check for datastore.properties");
							}
							// error occurred
							continue;
						}
						/*
						 * CHECKING FOR indexer.properties
						 */
						final File indexer = new File(baseDir,"indexer.properties");
						final Properties indexerProp=ImageMosaicProperties.buildIndexer(indexer,configuration);
						if (indexerProp == null) {
							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn("ImageMosaicAction: failed to check for indexer.properties");
							}
							// error occurred
							continue;
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
								addedFiles = Path.copyListFileToNFS(
										cmd.getAddFiles(),
										cmd.getBaseDir(), true, WAIT);
								if (addedFiles == null
										|| addedFiles.size() == 0) {
									// no file where transfer to the
									// destination dir
									if (LOGGER.isWarnEnabled())
										LOGGER.warn("ImageMosaicAction: no file were transfer to the destination dir,"
												+ " check your command.");
									continue;
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
									LOGGER.warn("ImageMosaicAction: unable to delete files from a not existent layer,"
											+ " delFile list will be ignored.");
								// files are now into the baseDir and layer
								// do not exists so
								cmd.getDelFiles().clear();
							}
						} // build baseDir using AddFiles

						// layer do not exists so try to create a new one
						// STARTING Switch to the new REST library
						final GeoServerRESTPublisher gsPublisher = new GeoServerRESTPublisher(
								getConfiguration().getGeoserverURL(),
								getConfiguration().getGeoserverUID(),
								getConfiguration().getGeoserverPWD());

						final GSCoverageEncoder coverageEnc=ImageMosaicREST.createGSImageMosaicEncoder(mosaicDescriptor,configuration);
						
					    final GSLayerEncoder layerEnc=new GSLayerEncoder();
					    layerEnc.addDefaultStyle(configuration.getDefaultStyle());
					    //layerEnc.setWmsPath(configuration.getWmsPath()!=null?configuration.getWmsPath():"");
					    
					    // TODO create workspace
					    final GSWorkspaceEncoder workspaceEnc=new GSWorkspaceEncoder();
					    
					    workspaceEnc.addName(workspace);

						// create a new ImageMosaic layer...
					    final RESTCoverageStore store = gsPublisher.publishExternalMosaic(workspace,layerID,baseDir,coverageEnc,layerEnc);
					    
						// STARTING Switch to the new REST library

//						gsPublisher.publishExternalMosaic(???
//								configuration.getDefaultNamespace(),
//								layerID,
//								baseDir, configuration.getCrs(),
//								configuration.getDefaultStyle());
						
						if (store==null) {
							// layer already exists
							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn("ImageMosaicAction: Error creating the new store: "+ layerID);
							}
							continue;
						} // layer Exists
						
						

					} else {
						// layer exists
						/**
						 * If datastore Update ImageMosaic datastore...
						 */
						/*
						 * CHECKING FOR datastore.properties
						 */
						final File datastore = ImageMosaicProperties.checkDataStore(configuration, baseDir);
						if (datastore == null) {
							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn("ImageMosaicAction: failed to check for datastore.properties");
							}
							// error occurred
							continue;
						}
						if (Utils.checkFileReadable(datastore)) {

							// read the properties file
							Properties dataStoreProp = null;
							try {
								dataStoreProp = ImageMosaicProperties.getProperty(datastore);
							} catch (UnsatisfiedLinkError ule) {
								// unrecoverable error
								throw ule;
							}

							/**
							 * This file is generated by the GeoServer and
							 * we need it to get: LocationAttribute -> the
							 * name of the attribute indicating the file
							 * location AbsolutePath -> a boolean indicating
							 * if file locations (paths) are absolutes
							 * 
							 * 20101014T030000_pph.properties
							 * 
							 * AbsolutePath=false Name=20101014T030000_pph
							 * ExpandToRGB=false LocationAttribute=location
							 */
							final File mosaicPropFile = new File(baseDir,layerID+ ".properties");

							Properties mosaicProp = null;
							try {
								mosaicProp = ImageMosaicProperties
										.getProperty(mosaicPropFile);
							} catch (UnsatisfiedLinkError ule) {
								// unrecoverable error
								throw ule;
							}

							// update
							if (ImageMosaicUpdater.updateDataStore(mosaicProp, dataStoreProp,mosaicDescriptor, cmd)) {
								// SUCCESS update the store
								if (LOGGER.isInfoEnabled()) {
									LOGGER.info("ImageMosaicAction: reset GeoServer Cache");
								}
								// clear GeoServer cached readers
								if (ImageMosaicREST.resetGeoserver(
										configuration.getGeoserverURL(),
										configuration.getGeoserverUID(),
										configuration.getGeoserverPWD())) {
									// SUCCESS update the Catalog
									if (LOGGER.isInfoEnabled()) {
										LOGGER.info("ImageMosaicAction: reset DONE");
									}
								} else {
									if (LOGGER.isWarnEnabled()) {
										LOGGER.warn("ImageMosaicAction: GeoServer failed to reset cached readers.");
									}
									continue;
								}
							} else {
								if (LOGGER.isWarnEnabled()) {
									LOGGER.warn("ImageMosaicAction: The following command FAILED:\n"
											+ cmd.toString() + "\n");
								}
								continue;
							}

						} // datastore.properties
						else {
							/*
							 * File 'datastore.properties' do not exists.
							 * Probably we have a ShapeFile as datastore for
							 * this layer. Error unable to UPDATE the shape
							 * file.
							 */

							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn("ImageMosaicAction: STILL NOT IMPLEMENTED: unable to UPDATE a shape file.");
							}
							continue;
						} // shapefile
					
					} // layer exists
				
					
					/**
					 * The returned file: - one for each event - .layer file - will
					 * be added to the output queue
					 */
					final File layerDescriptor;

		            // generate a RETURN file and append it to the return queue
					// TODO get info about store and workspace name...
		            if ((layerDescriptor = ImageMosaicOutput.writeReturn(baseDir, baseDir,layerID,workspace,layerID)) != null) {
		                ret.add(new FileSystemEvent(layerDescriptor, FileSystemEventType.FILE_ADDED));
		            }
		            
				} // input file event exists
				else {
					// no file is found for this event try with the next one
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("ImageMosaicAction: Unable to handle the passed file event: "
								+ input.getAbsolutePath());
					}
					continue;
				}


			} // while

			listenerForwarder.completed();

			// ... setting up the appropriate event for the next action
			return ret;

		} catch (Throwable t) {
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
				queryString.append(entry.getKey()).append("=")
						.append(entry.getValue());
			}

		return queryString.toString();
	}

	public ImageMosaicConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + "cfg:" + getConfiguration()
				+ "]";
	}
}
