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
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;

/**
 * An action which is able to create and update a layer into the GeoServer
 * 
 * @author (r1)AlFa
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $ ImageMosaicConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06 $
 *          ImageMosaicAction.java $ Revision: 0.2 $ 25/feb/11 09:00:00
 */

public class ImageMosaicAction extends BaseAction<FileSystemEvent> implements
        Action<FileSystemEvent> {

    protected final static int WAIT = 10; // seconds to wait for nfs propagation

    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ImageMosaicAction.class.toString());

    protected final ImageMosaicConfiguration configuration;

    /**
     * Constructs a producer. The operation name will be the same than the parameter descriptor
     * name.
     * 
     * @throws IOException
     */
    public ImageMosaicAction(ImageMosaicConfiguration configuration) {
        super(configuration);
        this.configuration = configuration;
        // //
        // data flow configuration and dataStore name must not be null.
        // //
        
        if (configuration == null) {
            final String message = "ImageMosaicAction: DataFlowConfig is null.";
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, message);
            throw new IllegalStateException(message);
        } else if ((configuration.getGeoserverURL() == null)) {
            final String message = "GeoServerURL is null.";
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, message);
            throw new IllegalStateException(message);
        } else if ("".equals(configuration.getGeoserverURL())) {
            final String message = "GeoServerURL is empty.";
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, message);
            throw new IllegalStateException(message);
        }
    }

    /**
     * Public or update an ImageMosaic layer on the specified GeoServer
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {

        if (LOGGER.isLoggable(Level.INFO))
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
            Collection<FileSystemEvent> layers = new ArrayList<FileSystemEvent>();

            /**
             * For each event into the queue
             */
            while (events.size() > 0) {
                FileSystemEvent event = events.remove();

                /**
                 * If the input file exists and it is a file: Check if it is: - A Directory - An XML
                 * -> Serialized ImageMosaicCommand
                 * 
                 * Building accordingly the ImageMosaicCommand command.
                 */
                ImageMosaicCommand cmd;

                /**
                 * The returned file: - one for each event - .layer file - will be added to the
                 * output queue
                 */
                File layerDescriptor;

                /**
                 * a descriptor for the mosaic to handle
                 */
                ImageMosaicGranulesDescriptor mosaicDescriptor;

                /**
                 * the file pointing to the directory which the layer will refer to.
                 */
                File baseDir;

                /*
                 * Checking input files.
                 */
                File input = event.getSource();
                if (input == null) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.warning("ImageMosaicAction: The input file event points to a null file object.");
                    // no file is found for this event try with the next one
                    continue;
                }
                // if the input exists
                if (input.exists()) {
                    /*
                     * Try to extract file event COMMENTED: where to put the baseDir of this layer?
                     * TODO: discuss about this functionality try { input=new
                     * File(Extract.extract(input.getAbsolutePath())); } catch(Exception e){ if
                     * (LOGGER.isLoggable(Level.WARNING)) LOGGER.log(Level.WARNING,
                     * "ImageMosaicAction:Extract: message: "+e.getLocalizedMessage()); continue; }
                     */
                    /**
                     * the file event points to an XML file...
                     * 
                     * @see ImageMosaicCommand
                     */
                    if (input.isFile()
                            && FilenameUtils.getExtension(input.getName()).equalsIgnoreCase("xml")) {
                        // try to deserialize
                        cmd = ImageMosaicCommand.deserialize(input.getAbsoluteFile());
                        if (cmd == null) {
                            if (LOGGER.isLoggable(Level.WARNING))
                                LOGGER.warning("ImageMosaicAction: Unable to deserialize the passed file: "
                                        + input.getAbsolutePath());
                            continue;
                        }

                        /**
                         * If here: the command is ready: - get the base dir file which will be used
                         * as ID.
                         */
                        baseDir = cmd.getBaseDir();

                        mosaicDescriptor = ImageMosaicGranulesDescriptor.buildDescriptor(cmd,
                                configuration);

                        if (mosaicDescriptor == null) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.warning("ImageMosaicAction: Unable to build the imageMosaic descriptor"
                                        + input.getAbsolutePath());
                            }
                            continue;
                        }

                        /*
                         * Check if ImageMosaic layer already exists... TODO: check if the Store
                         * exists!!!
                         */
                        boolean layerExists = false;
                        try {
                            layerExists = GeoServerRESTHelper.checkLayerExistence(ImageMosaicREST
                                    .decurtSlash(getConfiguration().getGeoserverURL()),
                                    getConfiguration().getGeoserverUID(), getConfiguration()
                                            .getGeoserverPWD(), mosaicDescriptor
                                            .getCoverageStoreId());
                        } catch (ParserConfigurationException pce) {
                            if (LOGGER.isLoggable(Level.SEVERE)) {
                                LOGGER.severe("ImageMosaicAction: " + pce.getLocalizedMessage());
                            }
                            // unrecoverable error
                            throw pce;
                        } catch (IOException ioe) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.log(Level.WARNING,
                                        "ImageMosaicAction: " + ioe.getLocalizedMessage(), ioe);
                            }
                            continue;
                        } catch (TransformerException te) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.log(Level.WARNING,
                                        "ImageMosaicAction: " + te.getLocalizedMessage(), te);
                            }
                            continue;
                        }

                        /*
                         * CHECKING FOR datastore.properties
                         */
                        final File datastore = ImageMosaicProperties.checkDataStore(configuration,
                                baseDir);
                        if (datastore == null) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.warning("ImageMosaicAction: failed to check for datastore.properties");
                            }
                            // error occurred
                            continue;
                        }

                        final File indexer = new File(baseDir, "indexer.properties");
                        ImageMosaicProperties.buildIndexer(indexer, configuration);

                        if (!layerExists) {

                            // Perform tests on the base dir file
                            if (!baseDir.exists() || !baseDir.isDirectory()) {
                                // no base dir exists try to build a new one using addList()
                                if (cmd.getAddFiles() != null) {
                                    if (cmd.getAddFiles().size() > 0) {
                                        // try build the baseDir
                                        if (!baseDir.mkdirs()) {
                                            if (LOGGER.isLoggable(Level.WARNING))
                                                LOGGER.warning("ImageMosaicAction: unable to create the base directory named \'"
                                                        + baseDir.getAbsolutePath() + "\'.");
                                            continue;
                                        }
                                    } else {
                                        if (LOGGER.isLoggable(Level.WARNING))
                                            LOGGER.warning("ImageMosaicAction: Unexpected not existent baseDir for this layer '"
                                                    + baseDir.getAbsolutePath()
                                                    + "'.\n If you want to build a new layer try using an "
                                                    + "existent or writeable baseDir and append a list of file to use to the addFile list.");
                                        continue;
                                    }
                                } else {
                                    if (LOGGER.isLoggable(Level.WARNING))
                                        LOGGER.warning("ImageMosaicAction: Unexpected not existent baseDir for this layer '"
                                                + baseDir.getAbsolutePath()
                                                + "'.\n If you want to build a new layer try using an "
                                                + "existent or writeable baseDir and append a list of file to use to the addFile list.");
                                    continue;
                                }
                            }

                            // no base dir exists try to build a new one using addList()
                            if (cmd.getAddFiles() != null) {
                                List<File> addFiles = cmd.getAddFiles();
                                if (addFiles.size() > 0) {
                                    // copy files from the addFile list to the baseDir (do not
                                    // preventing overwrite)
                                    List<File> addedFiles = Path.copyListFileToNFS(
                                            cmd.getAddFiles(), cmd.getBaseDir(), true, WAIT);
                                    if (addedFiles == null || addedFiles.size() == 0) {
                                        // no file where transfer to the destination dir
                                        if (LOGGER.isLoggable(Level.WARNING))
                                            LOGGER.warning("ImageMosaicAction: no file were transfer to the destination dir,"
                                                    + " check your command.");
                                        continue;
                                    }
                                    // files are now into the baseDir and layer do not exists so
                                    addFiles.clear();
                                }
                                // Already checked else {
                                // if (LOGGER.isLoggable(Level.WARNING))
                                // LOGGER.warning("ImageMosaicAction: Unexpected not existent baseDir for this layer '"
                                // + baseDir.getAbsolutePath()
                                // + "'. If you want to build a new layer try using an "
                                // +
                                // "existent or writeable baseDir and append a list of file to use to the addFile list.");
                                // continue;
                                // }
                                if (cmd.getDelFiles() != null) {
                                    if (LOGGER.isLoggable(Level.WARNING))
                                        LOGGER.warning("ImageMosaicAction: unable to delete files from a not existent layer,"
                                                + " delFile list will be ignored.");
                                    // files are now into the baseDir and layer do not exists so
                                    cmd.getDelFiles().clear();
                                }
                            }

                            // layer do not exists
                            ImageMosaicREST.createNewImageMosaicLayer(baseDir, mosaicDescriptor,
                                    configuration, layers);
                            /*
                             * TODO HERE WE HAVE A 'cmd' COMMAND FILE WHICH MAY HAVE GETADDFILE OR
                             * GETDELFILE !=NULL USING THOSE LIST WE MAY: DEL ->LOG WARNING--- ADD
                             * ->INSERT INTO THE DATASTORE AN IMAGE USING THE ABSOLUTE PATH.
                             */

                        } else {
                            // layer exists
                            /**
                             * If datastore Update ImageMosaic datastore...
                             */
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
                                 * This file is generated by the GeoServer and we need it to get:
                                 * LocationAttribute -> the name of the attribute indicating the
                                 * file location AbsolutePath -> a boolean indicating if file
                                 * locations (paths) are absolutes
                                 * 
                                 * 20101014T030000_pph.properties
                                 * 
                                 * AbsolutePath=false Name=20101014T030000_pph ExpandToRGB=false
                                 * LocationAttribute=location
                                 */
                                final File mosaicPropFile = new File(baseDir,
                                        mosaicDescriptor.getCoverageStoreId() + ".properties");

                                Properties mosaicProp = null;
                                try {
                                    mosaicProp = ImageMosaicProperties.getProperty(mosaicPropFile);
                                } catch (UnsatisfiedLinkError ule) {
                                    // unrecoverable error
                                    throw ule;
                                }

                                // update
                                if (ImageMosaicUpdater.updateDataStore(mosaicProp, dataStoreProp,
                                        mosaicDescriptor, cmd)) {
                                    // SUCCESS update the store
                                    if (LOGGER.isLoggable(Level.INFO)) {
                                        LOGGER.info("ImageMosaicAction: reset GeoServer Cache");
                                    }
                                    // clear GeoServer cached readers
                                    if (ImageMosaicREST.resetGeoserver(
                                            configuration.getGeoserverURL(),
                                            configuration.getGeoserverUID(),
                                            configuration.getGeoserverPWD())) {
                                        // SUCCESS update the Catalog
                                        if (LOGGER.isLoggable(Level.INFO)) {
                                            LOGGER.info("ImageMosaicAction: reset DONE");
                                        }
                                    } else {
                                        if (LOGGER.isLoggable(Level.WARNING)) {
                                            LOGGER.warning("ImageMosaicAction: GeoServer failed to reset cached readers.");
                                        }
                                        continue;
                                    }
                                } else {
                                    if (LOGGER.isLoggable(Level.WARNING)) {
                                        LOGGER.warning("ImageMosaicAction: The following command FAILED:\n"
                                                + cmd.toString() + "\n");
                                    }
                                    continue;
                                }

                            } // datastore.properties
                            else {
                                /*
                                 * File 'datastore.properties' do not exists. Probably we have a
                                 * ShapeFile as datastore for this layer. Error unable to UPDATE the
                                 * shape file.
                                 */

                                if (LOGGER.isLoggable(Level.WARNING)) {
                                    LOGGER.warning("ImageMosaicAction: Error unable to UPDATE a shape file.");
                                }
                                continue;
                            } // shapefile

                        } // layer Exists
                    }
                    // the file event points to a directory
                    else if (input.isDirectory()) {
                        /**
                         * If here: - get the base dir file which will be used as ID.
                         */
                        baseDir = input;

                        mosaicDescriptor = ImageMosaicGranulesDescriptor.buildDescriptor(baseDir,
                                configuration);

                        if (mosaicDescriptor == null) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.warning("ImageMosaicAction: Unable to build the imageMosaic descriptor"
                                        + input.getAbsolutePath());
                            }
                            continue;
                        }

                        /*
                         * Check if ImageMosaic layer already exists...
                         */
                        boolean layerExists = false;
                        try {
                            layerExists = GeoServerRESTHelper.checkLayerExistence(ImageMosaicREST
                                    .decurtSlash(getConfiguration().getGeoserverURL()),
                                    getConfiguration().getGeoserverUID(), getConfiguration()
                                            .getGeoserverPWD(), mosaicDescriptor
                                            .getCoverageStoreId());

                        } catch (ParserConfigurationException pce) {
                            // unrecoverable error
                            throw pce;
                        } catch (IOException ioe) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.log(Level.WARNING,
                                        "ImageMosaicAction: " + ioe.getLocalizedMessage(), ioe);
                            }
                            continue;
                        } catch (TransformerException te) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.log(Level.WARNING,
                                        "ImageMosaicAction: " + te.getLocalizedMessage(), te);
                            }
                            continue;
                        }

                        /*
                         * CHECKING FOR datastore.properties
                         */
                        final File datastore = ImageMosaicProperties.checkDataStore(configuration,
                                baseDir);
                        if (datastore == null) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.warning("ImageMosaicAction: failed to check for datastore.properties");
                            }
                            // error occurred
                            continue;
                        }

                        final File indexer = new File(baseDir, "indexer.properties");
                        ImageMosaicProperties.buildIndexer(indexer, configuration);

                        if (!layerExists) {
                            // create a new ImageMosaic layer... normal case
                            ImageMosaicREST.createNewImageMosaicLayer(baseDir, mosaicDescriptor,
                                    configuration, layers);

                        } else {
                            // layer already exists
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.warning("ImageMosaicAction: The Layer referring to the directory: "
                                        + input.getAbsolutePath() + " do not exists!");
                            }
                            continue;
                        } // layer Exists

                    } // input is Directory || xml
                    else {
                        // the file event do not point to a directory nor to an xml file
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.warning("ImageMosaicAction: the file event do not point to a directory nor to an xml file: "
                                    + input.getAbsolutePath());
                        }
                        continue;
                    }
                } // input file event exists
                else {
                    // no file is found for this event try with the next one
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning("ImageMosaicAction: Unable to handle the passed file event: "
                                + input.getAbsolutePath());
                    }
                    continue;
                }

                // prepare the return
                layerDescriptor = new File(baseDir, mosaicDescriptor.getCoverageStoreId()
                        + ".layer");
                if (layerDescriptor.exists() && layerDescriptor.isFile())
                    layers.add(new FileSystemEvent(layerDescriptor, FileSystemEventType.FILE_ADDED));

            } // while

            // ... setting up the appropriate event for the next action
            events.addAll(layers);

            listenerForwarder.completed();
            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
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
        return configuration;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "cfg:" + getConfiguration() + "]";
    }
}
