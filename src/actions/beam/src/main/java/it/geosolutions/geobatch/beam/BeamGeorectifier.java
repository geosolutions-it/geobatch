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

package it.geosolutions.geobatch.beam;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.beam.msgwarp.MSGConfiguration;
import it.geosolutions.geobatch.beam.msgwarp.MSGProduct;
import it.geosolutions.geobatch.beam.msgwarp.MSGWarp;
import it.geosolutions.geobatch.beam.netcdf.NCUtilities;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.jaiext.interpolators.InterpolationNearest;
import it.geosolutions.jaiext.range.Range;
import it.geosolutions.jaiext.range.RangeFactory;
import it.geosolutions.jaiext.warp.WarpDescriptor;
import it.geosolutions.jaiext.warp.WarpRIF;
import it.geosolutions.tools.io.file.Collector;

import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.OperationRegistry;
import javax.media.jai.RegistryElementDescriptor;
import javax.media.jai.RenderedOp;
import javax.media.jai.Warp;
import javax.media.jai.registry.RenderedRegistryMode;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductSubsetDef;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.gpf.operators.standard.reproject.ReprojectionOp;
import org.jaitools.imageutils.ImageLayout2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bc.ceres.jai.operator.ReinterpretDescriptor;
import com.bc.ceres.jai.opimage.ReinterpretRIF;

/**
 * Action to georectify beam products
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 */
public class BeamGeorectifier extends BaseAction<FileSystemEvent> {

    // Externalize this through configuration
    static Map<String, Object> DEFAULT_PARAMS = new HashMap<String, Object>();

    private final static Logger LOGGER = LoggerFactory.getLogger(BeamGeorectifier.class);
    
    private final static String wgs84code = "EPSG:4326";
    static {
        
        OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        
        try {
            final OperationDescriptor op = new ReinterpretDescriptor();
            final String descName = op.getName();
            
            RegistryElementDescriptor desc = registry.getDescriptor(RenderedRegistryMode.MODE_NAME, descName);
            if (desc == null) {
                registry.registerDescriptor(op);
                final RenderedImageFactory rif = new ReinterpretRIF();
                registry.registerFactory(RenderedRegistryMode.MODE_NAME, descName, "com.bc.ceres.jai", rif);
            }
            
            final OperationDescriptor opWarp = new WarpDescriptor();
            final String descNameWarp = opWarp.getName();
            
            RegistryElementDescriptor descWarp = registry.getDescriptor(RenderedRegistryMode.MODE_NAME, descNameWarp);
            
            if (descWarp == null) {
                registry.registerDescriptor(opWarp);
                final RenderedImageFactory rif = new WarpRIF();
                registry.registerFactory(RenderedRegistryMode.MODE_NAME, descNameWarp, "it.geosolutions.jaiext.roiaware", rif);
            }
            
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(new ReprojectionOp.Spi());

        //TODO: Parse them from configuration
        DEFAULT_PARAMS.put("resamplingName", "Nearest");
        DEFAULT_PARAMS.put("includeTiePointGrids", false);
        DEFAULT_PARAMS.put("region", null);
        // parameterMap.put("bandNames", null);
        DEFAULT_PARAMS.put("copyMetaData", true);
        DEFAULT_PARAMS.put("orientation", "0");
        DEFAULT_PARAMS.put("crs", wgs84code);
    }

    public boolean checkConfiguration() {
        //TODO: Check it
        return true;
    }

    private BeamGeorectifierConfiguration configuration;

    public BeamGeorectifier(BeamGeorectifierConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {

        try {
            // looking for file
            if (events.size() == 0)
                throw new IllegalArgumentException(
                        "BeamGeorectifier::execute(): Wrong number of elements for this action: "
                                + events.size());

            listenerForwarder.setTask("config");
            listenerForwarder.started();

            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                final String message = "BeamGeorectifier::execute(): DataFlowConfig is null.";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new IllegalStateException(message);
            }

            // The return
            Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();

            while (events.size() > 0) {

                // run
                listenerForwarder.setTask("Georectifying");

                final FileSystemEvent event = events.remove();

                final File eventFile = event.getSource();
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Processing file " + eventFile);

                if (eventFile.exists() && eventFile.canRead() && eventFile.canWrite()) {
                    /*
                     * If here: we can start retiler actions on the incoming file event
                     */

                    if (eventFile.isDirectory()) {

                        final FileFilter filter = new RegexFileFilter(".+\\.[nN][cC]");
                        final Collector collector = new Collector(filter);
                        final List<File> fileList = collector.collect(eventFile);
                        int size = fileList.size();
                        for (int progress = 0; progress < size; progress++) {

                            final File inFile = fileList.get(progress);

                            try {
                                //
                                // georectify;
                                georectify(inFile, ret);
                                
                            } catch (UnsupportedOperationException uoe) {
                                listenerForwarder.failed(uoe);
                                if (LOGGER.isWarnEnabled())
                                    LOGGER.warn(
                                            "BeamGeorectifier::execute(): "
                                                    + uoe.getLocalizedMessage(), uoe);
                            } catch (IllegalArgumentException iae) {
                                listenerForwarder.failed(iae);
                                if (LOGGER.isWarnEnabled())
                                    LOGGER.warn(
                                            "BeamGeorectifier::execute(): "
                                                    + iae.getLocalizedMessage(), iae);
                            } finally {
                                listenerForwarder.setProgress((progress * 100)
                                        / ((size != 0) ? size : 1));
                                listenerForwarder.progressing();
                            }
                        }
                    } else {
                        try {
                            //
                            georectify(eventFile, ret);

                        } catch (UnsupportedOperationException uoe) {
                            listenerForwarder.failed(uoe);
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn(
                                        "BeamGeorectifier::execute(): " + uoe.getLocalizedMessage(),
                                        uoe);
                        } catch (IllegalArgumentException iae) {
                            listenerForwarder.failed(iae);
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn(
                                        "BeamGeorectifier::execute(): " + iae.getLocalizedMessage(),
                                        iae);
                        } finally {
                            listenerForwarder.setProgress(100 / ((events.size() != 0) ? events
                                    .size() : 1));
                        }
                    }

                } else {
                    final String message = "BeamGeorectifier::execute(): The passed file event refers to a not existent "
                            + "or not readable/writeable file! File: "
                            + eventFile.getAbsolutePath();
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn(message);

                    throw new ActionException(this, message);

                }
            } // endwile
            listenerForwarder.completed();

            // return
            if (ret.size() > 0) {
                events.clear();
                return ret;
            } else {
                /*
                 * If here: we got an error no file are set to be returned the input queue is returned
                 */
                return events;
            }
        } catch (Exception t) {
            final String message = "BeamGeorectifier::execute(): " + t.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message, t);
            final ActionException exc = new ActionException(this, message, t);
            listenerForwarder.failed(exc);
            throw exc;
        }
    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Georectify the specified inputFile
     * @param inputFile the inputFile to be georectified
     * @param ret 
     * @throws IOException
     */
    public void georectify(final File inputFile, Queue<FileSystemEvent> ret) throws IOException {
        final String outputFilePath = configuration.getOutputFolder() + File.separatorChar + inputFile.getName();
        // Get product from inputFile
        if (inputFile == null || !inputFile.exists() || !inputFile.canRead()) {
            throw new IllegalArgumentException("Need to provide a valid file");
        }

        Product inputProduct = null;
        Product reducedProduct = null;
        Product reprojectedProduct = null;

        try {
            // Reading product
            listenerForwarder.setTask("Reading");
            inputProduct = ProductIO.readProduct(inputFile);
            listenerForwarder.progressing(10f, "Reading");
            
            // Refining products through filters
            reducedProduct = refineProductsList(inputProduct);

            // Reprojecting
            listenerForwarder.setTask("Reprojecting");
            Map<String,Object> operationParams = null;
            final String params = configuration.getParams();
            if (params != null && params.trim().length() > 0) {
                String parameters[] = params.split(",");
                if (parameters != null) {
                    operationParams = new HashMap<String,Object>(DEFAULT_PARAMS);
                    for (String parameter: parameters) {
                        String[] key_value = parameter.trim().split("=");
                        operationParams.put(key_value[0], key_value[1]);
                    }
                }
            } else {
                operationParams = DEFAULT_PARAMS;
            }

            // Check from the configuration if the beam georectification must be used or not
            if(configuration.isUseBeam()){
                reprojectedProduct = GPF.createProduct("Reproject", operationParams, reducedProduct);
            }else{
                reprojectedProduct = createGeorectifiedProduct(reducedProduct);
            }

            listenerForwarder.progressing(20f, "Reprojecting");

            // Get a store depending on the requested format
            final BeamFormatWriter writer = BeamGeorectifierConfiguration.getFormatWriter(configuration.getOutputFormat());
            if (writer == null) {
                throw new IllegalArgumentException("No writers have been found for that output format: " + configuration.getOutputFormat());
            }

            Map<String, Object> storingParams = new HashMap<String, Object>();
            storingParams.put(BeamFormatWriter.PARAM_GEOPHYSIC, configuration.isGeophysics());
            storingParams.put(BeamFormatWriter.PARAM_CUSTOMDIMENSION, configuration.getDimensions()); // Get from config
            storingParams.put(BeamFormatWriter.PARAM_FORCECOORDINATE, configuration.isForceCoordinates());
            storingParams.put(BeamFormatWriter.PARAM_LARGEFILE, configuration.isLargeFile());

            // store the resulting product
            listenerForwarder.setTask("storing results");
            writer.storeProduct(outputFilePath, inputProduct, reprojectedProduct, storingParams, listenerForwarder);
            if (outputFilePath != null) {
//              listenerForwarder.failed(thrn"null output file");
              final File outputFile = new File(outputFilePath);
              if (outputFile.exists() && outputFile.canRead()) {
                  ret.add(new FileSystemEvent(outputFile, FileSystemEventType.FILE_ADDED));
              } else {
                  listenerForwarder.failed(new IllegalStateException("The specified file path doesn't exist or can't be read"));
              }
          }
        } finally {
            
            // Disposing products, releasing resources
            if (inputProduct != null) {
                try {
                    inputProduct.dispose();
                } catch (Throwable t) {
                    // Does nothing
                }
            }
            if (reducedProduct != null) {
                try {
                    reducedProduct.dispose();
                } catch (Throwable t) {
                    // Does nothing
                }
            }
            if (reprojectedProduct != null) {
                try {
                    reprojectedProduct.dispose();
                } catch (Throwable t) {
                    // Does nothing
                }
            }
        }
        
    }

    /**
     * Check the bands to be parsed (if specified in the configuration) and create a subset
     * on the specified filters
     * @param inputProduct
     * @return the subset product to be managed
     * @throws IOException
     */
    private Product refineProductsList(Product inputProduct) throws IOException {
        Product product = inputProduct;
        final String filterVariables = configuration.getFilterVariables();
        boolean filterIsInclude = configuration.isFilterInclude();
        // Excluding input bands which are outside of the specified list
        if (filterVariables != null) {
            List<String> requestedBands = new ArrayList<String>();
            final String[] variables = filterVariables.split(",");
            final Band bands[] = inputProduct.getBands();
            
            if (filterIsInclude) {
                // Loop on the variables 
                for (String variable : variables) {
                    for (Band band : bands) {
                        String name = band.getName();
                        variable = variable.trim();
                        if (name.startsWith(variable)) {
                            requestedBands.add(name);
                            break;
                        }
                    }
                }
            } else {
                // Filter exclusion: loop on bands for faster exclusions
                boolean excludeMe = false;
                for (Band band : bands) {
                    String name = band.getName();
                    excludeMe = false;
                    for (String variable : variables) {
                        variable = variable.trim();
                        if (name.startsWith(variable)) {
                            excludeMe = true;
                            break;
                        }
                    }
                    if (!excludeMe) {
                        requestedBands.add(name);
                    }
                }
            }

            // create product subset
            if (requestedBands != null) {
                ProductSubsetDef subsetDef = new ProductSubsetDef();
                String[] subset = requestedBands.toArray(new String[requestedBands.size()]);
                subsetDef.setNodeNames(subset);
                product = inputProduct.createSubset(subsetDef, "subset", "subset");
            }
        }
        return product;
    }


    /**
     * Creation of a final product where each Band is georectified.
     *
     * @param reducedProduct input product
     * @return
     */
    private Product createGeorectifiedProduct(Product reducedProduct) {
        int sceneRasterWidth = reducedProduct.getSceneRasterWidth();
        int sceneRasterHeight = reducedProduct.getSceneRasterHeight();
        // Output product
        MSGProduct outputProduct = new MSGProduct(reducedProduct.getName(),
                reducedProduct.getProductType(), sceneRasterWidth, sceneRasterHeight);
        // Selection of the bands
        Band[] bands = reducedProduct.getBands();
        // Creation of the Warp object to use for doing the georectification
        MSGConfiguration conf = configuration.getMsgConf();
        double earthCentreDistance = conf.getEarthCentreDistance();
        double ulx = conf.getUlx();
        double uly = conf.getUly();
        double lrx = conf.getLrx();
        double lry = conf.getLry();
        double subSatLon = conf.getSubSatLon();
        Double resol_angle_X = conf.getResol_angle_X();
        Double resol_angle_Y = conf.getResol_angle_Y();

        // Cycle on the bands for rectification
        for (Band band : bands) {
            // Selection of the band raster dimensions
            int bandSceneRasterWidth = band.getSceneRasterWidth();
            int bandSceneRasterHeight = band.getSceneRasterHeight();
            // Creation of the new band
            Band newBand = new Band(band.getName(), band.getDataType(), bandSceneRasterWidth,
                    bandSceneRasterHeight);
            // For each band the associated image is taken
            RenderedImage image = band.getSourceImage().getImage(0);
            // Selection of the band related nodata
            double nodata = band.getNoDataValue();
            // Setting of the Band nodata
            newBand.setNoDataValue(nodata);
            // Setting of various parameters from the old band to the new band
            prepareNewBand(newBand, band);
            // Selection of the image data type
            int dataType = image.getSampleModel().getDataType();
            // Creation of the related nodata range.
            Range range = prepareRange(nodata, dataType);
            // Backgroudn values
            double[] backgroundValues = new double[] { nodata };
            // Interpolation object
            Interpolation interpolation = new InterpolationNearest(range, false, nodata, dataType);
            // JAI hints used for forcing the image to the selected width/height
            ImageLayout2 layout = new ImageLayout2();
            layout.setMinX(0).setMinY(0).setWidth(sceneRasterWidth).setHeight(sceneRasterHeight);
            // Setting of the layout inside the RenderingHints
            RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            // Creation of the Warp object
            Warp msgWarp = new MSGWarp(earthCentreDistance, ulx, uly, lrx, lry, sceneRasterWidth,
                    sceneRasterHeight, subSatLon, resol_angle_X, resol_angle_Y,
                    bandSceneRasterWidth * 1l, bandSceneRasterHeight * 1l);
            // Elaboration of the image
            RenderedOp warpedImage = WarpDescriptor.create(image, msgWarp, interpolation,
                    backgroundValues, null, hints);
            // Setting of the image inside the new band
            newBand.setSourceImage(warpedImage);
            // Adding the new band to the new product
            outputProduct.addBand(newBand);
        }
        // Add the associated MathTransform
        outputProduct.initMathTransform(ulx, uly, lrx, lry, sceneRasterWidth, sceneRasterHeight);

        return outputProduct;
    }

    /**
     * Private method for populating the new band with the parameters from the old band
     *
     * @param newBand
     * @param band
     */
    private void prepareNewBand(Band newBand, Band band) {
        // Setting of the Band description
        newBand.setDescription(band.getDescription());
        // Setting of the Scaling
        newBand.setScalingFactor(band.getScalingFactor());
        newBand.setScalingOffset(band.getScalingOffset());
        // Setting of the unit
        newBand.setUnit(band.getUnit());
        // Setting of the band related geophysical nodata
        newBand.setGeophysicalNoDataValue(band.getGeophysicalNoDataValue());
        // Setting of the comments if present
        String comment = band.getComment();
        if(comment!=null && !comment.isEmpty()){
            newBand.setComment(comment);
        }
        // Setting of the band standard name if present
        String standardName = band.getStandardName();
        if(standardName!=null && !standardName.isEmpty()){
            newBand.setStandardName(standardName);
        }
        // Setting of the Valid Maximum Data if present
        Number validMax = band.getValidMax();
        if(validMax!=null){
            newBand.setValidMax(validMax);
        }
        // Setting of the Valid Minimum Data if present
        Number validMin = band.getValidMin();
        if(validMin!=null){
            newBand.setValidMin(validMin);
        }
    }

    /**
     * Prepares the nodata Range to use inside the JAI-EXT Warp operator
     *
     * @param nodata
     * @param dataType
     * @return
     */
    private Range prepareRange(double nodata, int dataType) {
        Range range = null;

        // Switch on the data type
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                range = RangeFactory.create((byte) nodata, true, (byte) nodata, true);
                break;
            case DataBuffer.TYPE_USHORT:
                range = RangeFactory.createU((short) nodata, true, (short) nodata, true);
                break;
            case DataBuffer.TYPE_SHORT:
                range = RangeFactory.create((short) nodata, true, (short) nodata, true);
                break;
            case DataBuffer.TYPE_INT:
                range = RangeFactory.create((int) nodata, true, (int) nodata, true);
                break;
            case DataBuffer.TYPE_FLOAT:
                range = RangeFactory.create((float) nodata, true, (float) nodata, true, true);
                break;
            case DataBuffer.TYPE_DOUBLE:
                range = RangeFactory.create(nodata, true, nodata, true, true);
                break;
            default:
                throw new IllegalArgumentException("Wrong image data type");
        }

        return range;
    }

}
