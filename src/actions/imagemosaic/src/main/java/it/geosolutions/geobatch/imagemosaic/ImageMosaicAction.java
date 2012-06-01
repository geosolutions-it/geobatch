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
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSWorkspaceEncoder;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import it.geosolutions.tools.io.file.Collector;
import it.geosolutions.tools.io.file.Copy;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Launcher;

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

public class ImageMosaicAction extends BaseAction<EventObject> {

    protected final static int WAIT = 10; // seconds to wait for nfs propagation

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
                throw new IllegalArgumentException("Uanble execute action with incoming null parameter");
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
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Input null object.");
                    continue;
                }

                if (evObj instanceof FileSystemEvent) {
                    /*
                     * Checking input files.
                     */
                    final File input = ((FileSystemEvent)evObj).getSource();
                    if (!input.exists()) {
                        // no file is found for this event try with the next one
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("The input file does not exists at url: " + input.getAbsolutePath());
                        }
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
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn("Unable to deserialize the passed file: "
                                            + input.getAbsolutePath());
                            continue;
                        }

                    } else if (input.isDirectory()) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Input file event points to a directory: " + input.getAbsolutePath());
                        }
                        final Collector coll = new Collector(null);
                        // try to deserialize
                        cmd = new ImageMosaicCommand(input, coll.collect(input), null);
                    } else {
                        // the file event does not point to a directory nor to
                        // an
                        // xml file
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("The file event does not point to a directory nor to an xml file: "
                                        + input.getAbsolutePath());
                        }
                        continue;
                    }
                } else if (evObj instanceof EventObject) {
                    Object innerObject=((EventObject)evObj).getSource();
                    if (innerObject instanceof ImageMosaicCommand){
                        cmd = (ImageMosaicCommand)innerObject;
                    } else {
                     // the file event does not point to a directory nor to an
                        // xml file
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("The file event does not point to a valid object: " + evObj);
                        }
                        continue;
                    }
                } else {
                    // the file event does not point to a directory nor to an
                    // xml file
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("The file event does not point to a valid object: " + evObj);
                    }
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
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Unable to build the imageMosaic descriptor" + cmd.getBaseDir());
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
                                    LOGGER.warn("Unable to create the base directory named \'"
                                                + baseDir.getAbsolutePath() + "\'.");
                                continue;
                            }
                        } else {
                            if (LOGGER.isWarnEnabled())
                                LOGGER
                                    .warn("Unexpected not existent baseDir for this layer '"
                                          + baseDir.getAbsolutePath()
                                          + "'.\n If you want to build a new layer try using an "
                                          + "existent or writeable baseDir and append a list of file to use to the addFile list.");
                            continue;
                        }
                    } else {
                        if (LOGGER.isWarnEnabled())
                            LOGGER
                                .warn("Unexpected not existent baseDir for this layer '"
                                      + baseDir.getAbsolutePath()
                                      + "'.\n If you want to build a new layer try using an "
                                      + "existent or writeable baseDir and append a list of file to use to the addFile list.");
                        continue;
                    }
                }
                // override local cmd null params with the getConfiguration()
                cmd.copyConfigurationIntoCommand(getConfiguration());

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

                final String workspace = cmd.getDefaultNamespace() != null  
                        ? cmd.getDefaultNamespace()
                        : "";

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

                if (!layerExists) {
                    // layer does not exists so try to create a new one

                    /*
                     * CHECKING FOR datastore.properties
                     */
                    final File datastore = ImageMosaicProperties.checkDataStore(cmd, getConfigDir(), baseDir);
                    if (datastore == null) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Failed to check for datastore.properties into:" + baseDir);
                        }
                    }

                    /*
                     * CHECKING FOR indexer.properties
                     */
                    final File indexer = new File(baseDir, "indexer.properties");
                    final Properties indexerProp = ImageMosaicProperties.buildIndexer(indexer,
                                                                                      cmd);
                    if (indexerProp == null) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Failed to check for indexer.properties into:" + baseDir);
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
                            addedFiles = Copy.copyListFileToNFS(cmd.getAddFiles(), cmd.getBaseDir(), true,
                                                                WAIT);
                            if (addedFiles == null || addedFiles.size() == 0) {
                                // no file where transfer to the
                                // destination dir
                                if (LOGGER.isWarnEnabled())
                                    LOGGER.warn("No file were transfer to the destination dir,"
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
                    final GSCoverageEncoder coverageEnc = ImageMosaicREST
                        .createGSImageMosaicEncoder(mosaicDescriptor, cmd);
                    coverageEnc.setName(layerName);
                    // layerEnc.setWmsPath(cmd.getWmsPath()!=null?cmd.getWmsPath():"");

                    // layer encoder
                    final GSLayerEncoder layerEnc = new GSLayerEncoder();
                    layerEnc.setDefaultStyle(cmd.getDefaultStyle());
                    

                    // create a new ImageMosaic layer...
                    final boolean published = gsPublisher.publishExternalMosaic(workspace, storeName, baseDir,
                                                                                coverageEnc, layerEnc);

                    /**
                     * TODO gsPublisher.createExternalMosaic TODO
                     * gsPublisher.publishLayer
                     * gsPublisher.publishExternalMosaic(???
                     * cmd.getDefaultNamespace(),
                     * layerID,baseDir, cmd.getCrs(),
                     * cmd.getDefaultStyle());
                     */

                    if (!published) {
                        // layer already exists
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Error creating the new store: " + layerName);
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
                    final File datastore = ImageMosaicProperties.checkDataStore(cmd, getConfigDir(), baseDir);
                    if (datastore == null) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Failed to check for datastore.properties");
                        }
                        // error occurred
                        continue;
                    }

                    if (!Utils.checkFileReadable(datastore)) {
                        /*
                         * File 'datastore.properties' do not exists. Probably
                         * we have a ShapeFile as datastore for this layer.
                         * Error unable to UPDATE the shape file.
                         */

                        // SHAPEFILE
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("STILL NOT IMPLEMENTED: unable to UPDATE a shape file.");
                        }
                        continue;
                    }

                    // read the properties file
                    final Properties dataStoreProp = ImageMosaicProperties.getProperty(datastore);

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
                    final File mosaicPropFile = new File(baseDir, layerName + ".properties");

                    if (!Utils.checkFileReadable(mosaicPropFile)) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Unable to locate the imagemosaic properties file at: "
                                        + mosaicPropFile.getCanonicalPath());
                        }
                        continue;
                    }

                    final Properties mosaicProp = ImageMosaicProperties.getProperty(mosaicPropFile);

                    // update
                    if (ImageMosaicUpdater.updateDataStore(mosaicProp, dataStoreProp, mosaicDescriptor, cmd)) {
                        // SUCCESS update the store
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Reset GeoServer Cache");
                        }
                        // clear GeoServer cached readers
                        if(cmd.getIgnoreGeoServer()) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("GeoServer is disabled by configuration. Reset will not be performed. ");
                            }
                        } else {
                            if (gsPublisher.reset()) {
                                // SUCCESS update the Catalog
                                if (LOGGER.isInfoEnabled()) {
                                    LOGGER.info("Reset DONE");
                                }
                            } else {
                                if (LOGGER.isWarnEnabled()) {
                                    LOGGER.warn("GeoServer failed to reset cached readers.");
                                }
                                continue;
                            }
                        }
                    } else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("The following command FAILED:\n" + cmd.toString() + "\n");
                        }
                        continue;
                    }

                } // layer exists

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

        } catch (IOException t) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(t.getLocalizedMessage(), t);
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        } catch (IllegalArgumentException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            listenerForwarder.failed(e);
            throw new ActionException(this, e.getMessage(), e);
        } catch (InstantiationException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            listenerForwarder.failed(e);
            throw new ActionException(this, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            listenerForwarder.failed(e);
            throw new ActionException(this, e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
            listenerForwarder.failed(e);
            throw new ActionException(this, e.getMessage(), e);
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
}
