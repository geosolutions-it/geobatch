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
package it.geosolutions.geobatch.beam.netcdf;

import it.geosolutions.geobatch.beam.BeamFormatWriter;
import it.geosolutions.geobatch.beam.netcdf.NCUtilities.NCCoordinateDimension;
import it.geosolutions.geobatch.beam.netcdf.NCUtilities.NCCoordinates;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.Product;
import org.geotools.util.Utilities;
import org.opengis.referencing.operation.MathTransform;

import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

import com.bc.ceres.glevel.MultiLevelImage;

/**
 * A class which store the reprojected product as a NetCDF file. Depending on the specified parameters, it will look for custom dimensions and then it
 * will group BEAM product BANDS by dimension and by Variable.
 * 
 * When dealing with unkonwn dimensions, BEAM produces different bands with different name by adding the dimension name as suffix followed by a
 * progressive number. So that a fractional_cloud_cover(lat=765, lon=120, cloud_formations=3) will result as 3 different bands:
 * fractional_cloud_cover_cloud_formation_1, fractional_cloud_cover_cloud_formation_2, fractional_cloud_cover_cloud_formation_3
 * All of them will be grouped together as the same variable "fractional_cloud_cover" for the same dimension "cloud_formations".
 * 
 * Note that the NetCDF creation is made on 2 phases: 
 * a first phase for definition and initialization (so we need to know all available dimensions and related coordinates)
 * followed by an actual data write phase.
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 * 
 */
public class BeamNetCDFWriter implements BeamFormatWriter {

    /**
     * Simple holder which group different Variables which have the same dimension.
     */
    static class VariablesGroup {

        public VariablesGroup(Map<String, BandsGroup> variables) {
            super();
            this.variables = variables;
        }

        public VariablesGroup() {
            super();
        }

        public Map<String, BandsGroup> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, BandsGroup> variables) {
            this.variables = variables;
        }

        /** Mapping between variables and related bands for this dimension */
        Map<String, BandsGroup> variables;
    }

    /**
     * Holder which group different Beam Product Bands for the same variable. When dealing with unkonwn dimensions, BEAM produces different bands with
     * different name by adding the dimension name as suffix followed by a progressive number. So that a fractional_cloud_cover(lat=765, lon=120,
     * cloud_formations=3) will result has 3 different bands: - fractional_cloud_cover_cloud_formation_1, fractional_cloud_cover_cloud_formation_2,
     * fractional_cloud_cover_cloud_formation_3
     */
    static class BandsGroup {
        public BandsGroup() {
            this.bands = new ArrayList<Band>();
        }

        public BandsGroup(List<Band> bands) {
            super();
            this.bands = bands;
        }

        public List<Band> getBands() {
            return bands;
        }

        public void setBands(List<Band> bands) {
            this.bands = bands;
        }

        public void add(Band band) {
            bands.add(band);
        }

        /** The list of BEAM product Bands related to this variable */
        List<Band> bands;
    }

    private final static Set<String> EXCLUSION_DIMS;

    private static final String NONE = "NONE";

    static {
        EXCLUSION_DIMS = new HashSet<String>();
        EXCLUSION_DIMS.add("along_track");
        EXCLUSION_DIMS.add("across_track");
        EXCLUSION_DIMS.add("num_rows");
        EXCLUSION_DIMS.add("num_cells");
    }

    @Override
    /**
     * Store a Beam Product to the specified output file path, using the specified params to operate on it.
     */
    public void storeProduct(
            final String outputFilePath, 
            final Product inputProduct,
            final Product outputProduct, 
            final Map<String, Object> params, 
            final ProgressListenerForwarder listenerForwarder) throws IOException {

        boolean geophysics = false;
        boolean forceCoordinate = false;

        NetcdfFile ncFileIn = null;
        HashMap<String, NCCoordinateDimension> coordinateDimensions = null;
        NetcdfFileWriteable ncFileOut = null;

        try {
            boolean isLargeFile = false;
            ncFileOut = NetcdfFileWriteable.createNew(outputFilePath);
            if (params != null) {
                if (params.containsKey(BeamFormatWriter.PARAM_GEOPHYSIC)) {
                    geophysics = (Boolean) params.get(BeamFormatWriter.PARAM_GEOPHYSIC);
                }
                if (params.containsKey(BeamFormatWriter.PARAM_LARGEFILE)) {
                    isLargeFile = (Boolean) params.get(BeamFormatWriter.PARAM_LARGEFILE);
                }
                if (params.containsKey(BeamFormatWriter.PARAM_FORCECOORDINATE)) {
                    forceCoordinate = (Boolean) params.get(BeamFormatWriter.PARAM_FORCECOORDINATE);
                }

                // In case requested dimensions are not specified, the resulting file will contains different
                // variables for each dimensions (e.g.: air_temp_pressure1, air_temp_pressure2, air_temp_pressure3,...)
                if (params.containsKey(BeamFormatWriter.PARAM_CUSTOMDIMENSION)) {
                    final String customDimensions = (String) params
                            .get(BeamFormatWriter.PARAM_CUSTOMDIMENSION);
                    if (customDimensions != null) {
                        // rude check for a netcdf input file
                        final String inputFile = inputProduct.getFileLocation().getAbsolutePath();
                        ncFileIn = NetcdfFile.open(inputFile);
                        if (inputFile.endsWith(".nc")) {
                            final String[] requestedDimensions = customDimensions.split(",");
                            final Set<String> dimensionSet = new HashSet<String>();
                            for (String dimensionName : requestedDimensions) {
                                dimensionSet.add(dimensionName.trim());
                            }
                            coordinateDimensions = new HashMap<String, NCCoordinateDimension>();

                            // look for requested dimensions
                            if (listenerForwarder != null) {
                                listenerForwarder.setTask("findDimensions");
                            }
                            findDimensions(ncFileIn, coordinateDimensions, dimensionSet);
                            if (listenerForwarder != null) {
                                listenerForwarder.progressing(100F, "findDimensions");
                            }
                        }
                    }
                }
            }

            // netCDF creation is made through 2 steps:
            // 1st - define phase where all dimension, coordinates and variable are defined
            // 2nd - populate variables with data

            // Let's do the first step
            final NCUtilities.NCCoordinates latLonCoordinates = new NCUtilities.NCCoordinates();
            if (listenerForwarder != null) {
                listenerForwarder.setTask("definePhase");
            }
            // This will prevent a time consuming phase where the NetCDF lib will fill all arrays with
            // the predefined value
            ncFileOut.setFill(false);

            // Definition is done... create the file
            ncFileOut.setLargeFile(isLargeFile);

            Map<String, VariablesGroup> dimensionsGroup = defineOutputNetCDF(ncFileOut,
                    inputProduct, outputProduct, latLonCoordinates, geophysics, forceCoordinate, 
                    coordinateDimensions);

            ncFileOut.create();

            // Fill coordinates with values
            writeCoordinates(ncFileOut, latLonCoordinates, coordinateDimensions, forceCoordinate);

            // Fill variable with values
            writeValues(ncFileOut, outputProduct, dimensionsGroup, geophysics, listenerForwarder);
            // /////////////////////////////////////WRITE VALUES

        } catch (InvalidRangeException e) {
            throw new IOException(e);
        } finally {

            // Close all files
            if (ncFileOut != null) {
                try {
                    ncFileOut.close();
                } catch (Throwable t) {

                }
            }
            if (ncFileIn != null) {
                try {
                    ncFileIn.close();
                } catch (Throwable t) {

                }
            }
        }
    }

    /**
     * Write the data contained in the product to the specified output file. Use the bands grouping to regroup bands by dimension
     * 
     * @param ncFileOut
     * @param product
     * @param dimensionsGroup
     * @param geophysics
     * @param listenerForwarder 
     * @throws IOException
     * @throws InvalidRangeException
     */
    private static void writeValues(NetcdfFileWriteable ncFileOut, Product product,
            final Map<String, VariablesGroup> dimensionsGroup, final boolean geophysics, 
            final ProgressListenerForwarder listenerForwarder)
            throws IOException, InvalidRangeException {
        if (listenerForwarder != null) {
            listenerForwarder.setTask("Writing values");
        }

        if (!dimensionsGroup.isEmpty()) {
            Set<String> keys = dimensionsGroup.keySet();
            Iterator<String> iterator = keys.iterator();
            final int numElements = keys.size();
            if (listenerForwarder != null) {
                listenerForwarder.setTask("--------> Writing By Dimensions");
            }
            
            float dimensionProgress = 0;
            // Loop over dimensions.
            while (iterator.hasNext()) {
                String dimension = iterator.next();
                if (listenerForwarder != null) {
                    listenerForwarder.setTask("        > Writing By Dimension: " + dimension);
                }

                // Get all the variables pertaining to that dimension
                final VariablesGroup variablesForCurrentDimension = dimensionsGroup.get(dimension);
                final Map<String, BandsGroup> variables = variablesForCurrentDimension.getVariables();
                final Set<String> variablesKeys = variables.keySet();
                final Iterator<String> variablesIterator = variablesKeys.iterator();

                final int numVariables = variablesKeys.size();
                if (listenerForwarder != null) {
                    listenerForwarder.setTask("------------> Writing Variables");
                }
                // Set data for each variable related to that dimension
                float progress = 0;
                while (variablesIterator.hasNext()) {
                    String variableName = variablesIterator.next();
                    BandsGroup bands = variables.get(variableName);
                    setData(ncFileOut, bands, variableName, geophysics, listenerForwarder);
                    progress++;
                    if (listenerForwarder != null) {
                        listenerForwarder.progressing((int)((progress * 100) / numVariables), "            > Writing Variable: " + variableName);
                    }
                }
                dimensionProgress++;
                if (listenerForwarder != null) {
                    listenerForwarder.progressing((int)((dimensionProgress * 100) / numElements), "        > Writing By Dimension: " + dimension);
                }
            }
        } else {

            // No grouped dimensions available... Simply write all bands 1 by 1
            int numBands = product.getNumBands();
            for (int i = 0; i < numBands; i++) {

                Band band = product.getBandAt(i);
                final String bandName = band.getName();
                if (bandName.equalsIgnoreCase(NCUtilities.LON)
                        || bandName.equalsIgnoreCase(NCUtilities.LAT)) {
                    continue;
                }
                final BandsGroup bandsGroup = new BandsGroup(Collections.singletonList(band));

                // Writing variable
                setData(ncFileOut, bandsGroup, bandName, geophysics, listenerForwarder);
                if (listenerForwarder != null) {
                    listenerForwarder.progressing((int)((i * 100) / numBands), "        > Writing By Dimension");
                }
            }
        }

    }

    /**
     * Scan input dataset looking for requested dimensions to be grouped
     * 
     * @param file
     * @param coordinateDimensions
     * @param requestedDimensionSet
     */
    private static void findDimensions(NetcdfFile file,
            HashMap<String, NCCoordinateDimension> coordinateDimensions,
            Set<String> requestedDimensionSet) {
        final List<Dimension> dims = file.getDimensions();
        final List<Variable> variables = file.getVariables();

        // Loop over dimensions available on File
        for (Dimension dimension : dims) {
            final String name = dimension.getName();
            if (!EXCLUSION_DIMS.contains(name)) {

                // Check whether that dimension is requested to be handled
                if (requestedDimensionSet.contains(name)) {
                    boolean foundCoordinate = false;
                    // Look for coordinates variable related to dimensions
                    for (Variable variable : variables) {

                        // Get dimensions for that varialbe
                        final List<Dimension> variableDims = variable.getDimensions();

                        // a coordinate is 1D variable having dimension name equal to the specified dimension
                        if (variableDims != null && variableDims.size() == 1) {
                            Dimension varDim = variableDims.get(0);
                            if (varDim.getName().equalsIgnoreCase(name)
                                    && varDim.getLength() == dimension.getLength()) {
                                NCCoordinateDimension ncCoordDim = new NCCoordinateDimension(
                                        varDim, variable, null);
                                coordinateDimensions.put(name, ncCoordDim);
                                foundCoordinate = true;
                                break;
                            }
                        }
                    }
                    if (!foundCoordinate) {
                        // Assign a dimension without coordinates
                        NCCoordinateDimension ncCoordDim = new NCCoordinateDimension(dimension);
                        coordinateDimensions.put(name, ncCoordDim);
                    }
                }
            }
        }
    }

    /**
     * Store the image contained in that Band to the specified NetCDF output file
     * 
     * @param ncFileOut
     * @param variablesName
     * @param bands
     * @param geophysics
     * @param listenerForwarder 
     * @throws IOException
     * @throws InvalidRangeException
     */
    private static void setData(final NetcdfFileWriteable ncFileOut,
            final BandsGroup beamVariableBands, String variableName, final boolean geophysics, final ProgressListenerForwarder listenerForwarder)
            throws IOException, InvalidRangeException {

        // Retrieve all bands for this variable
        List<Band> bands = beamVariableBands.getBands();
        final int numBands = bands.size();
        final Band bandProduct = bands.get(0);

        // Note that BEAM will always return geophysics images so this check won't provide
        final MultiLevelImage image = geophysics ? bandProduct.getGeophysicalImage() : bandProduct
                .getSourceImage();

        // Initialize size of variable and datatype
        final RenderedImage ri = image.getImage(0);
        final int height = ri.getHeight();
        final int width = ri.getWidth();
        final DataType datatype = NCUtilities.transcodeBeamDataType(geophysics ? bandProduct
                .getGeophysicalDataType() : bandProduct.getDataType());
        final int[] dimensions = numBands > 1 ? new int[] { numBands, height, width } : new int[] {
                height, width };

        // get a properly sized array for this dimension to fill it with values
        Array matrix = NCUtilities.getArray(dimensions, datatype);
        setValues(bands, numBands, matrix, datatype, geophysics, listenerForwarder);
        ncFileOut.write(variableName, matrix);
        matrix = null;

    }

    /**
     * Write values from product bands to NetCDF Array
     * 
     * @param bands
     * @param numBands
     * @param matrix
     * @param datatype
     * @param geophysics
     * @param listenerForwarder 
     */
    private static void setValues(List<Band> bands, int numBands, Array matrix,
            DataType datatype, boolean geophysics, ProgressListenerForwarder listenerForwarder) {

        // Getting first band as sample
        final Band bandProduct = bands.get(0);

        // Note that BEAM seems is always returning geophysic images 
        final MultiLevelImage image = geophysics ? bandProduct.getGeophysicalImage() : bandProduct
                .getSourceImage();
        final RenderedImage ri = image.getImage(0);

        //
        // Preparing tile properties for future scan
        //
        int width = ri.getWidth();
        int height = ri.getHeight();
        int minX = ri.getMinX();
        int minY = ri.getMinY();
        int maxX = minX + width - 1;
        int maxY = minY + height - 1;
        int tileWidth = Math.min(ri.getTileWidth(), width);
        int tileHeight = Math.min(ri.getTileHeight(), height);

        int minTileX = minX / tileWidth - (minX < 0 ? (-minX % tileWidth > 0 ? 1 : 0): 0);
        int minTileY = minY / tileHeight - (minY < 0 ? (-minY % tileHeight > 0 ? 1 : 0): 0);
        int maxTileX = maxX / tileWidth - (maxX < 0 ? (-maxX % tileWidth > 0 ? 1 : 0): 0);
        int maxTileY = maxY / tileHeight - (maxY < 0 ? (-maxY % tileHeight > 0 ? 1 : 0): 0);

        final Index Tima = matrix.getIndex();

        // 
        // Fill data matrix
        //

        // Simple case... just one band. Scan index will be 2D
        if (numBands == 1) {
            final Band band = bandProduct;
            final RandomIter data = RandomIterFactory.create(ri, null);
            for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                    for (int trow = 0; trow < tileHeight; trow++) {
                        int j = (tileY * tileHeight) + trow;
                        if ((j >= minY) && (j <= maxY)) {
                            for (int tcol = 0; tcol < tileWidth; tcol++) {
                                int col = (tileX * tileWidth) + tcol;
                                if ((col >= minX) && (col <= maxX)) {
                                    int k = col;
                                    final int yPos = height - j - 1;
                                    switch (datatype) {
                                    case BYTE:
                                        byte sampleByte = (byte) data.getSampleFloat(k, j, 0);
                                        matrix.setByte(Tima.set(yPos, k), sampleByte);
                                        break;
                                    case SHORT:
                                        short sampleShort = (short) data.getSampleFloat(k, j, 0);
                                        matrix.setShort(Tima.set(yPos, k), sampleShort);
                                        break;
                                    case INT:
                                        int sampleInt = (int) data.getSampleFloat(k, j, 0);
                                        matrix.setInt(Tima.set(yPos, k), sampleInt);
                                        break;
                                    case FLOAT:
                                        float sampleFloat = data.getSampleFloat(k, j, 0);
                                        matrix.setFloat(Tima.set(yPos, k), sampleFloat);
                                        break;
                                    case DOUBLE:
                                        double sampleDouble = data.getSampleDouble(k, j, 0);
                                        matrix.setDouble(Tima.set(yPos, k), sampleDouble);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Finalize the iterator and dispose the Band
            data.done();
            band.dispose();
        } else {
            
            listenerForwarder.setTask("--------------> Writing bands (dimensions)");
            // Loop over bands using a 3D index
            for (int b = 0; b < numBands; b++) {
                Band band = bands.get(b);
                
                MultiLevelImage imageBand = geophysics ? band.getGeophysicalImage() : band
                        .getSourceImage();
                final RenderedImage inputRI = imageBand.getImage(0);
                final RandomIter data = RandomIterFactory.create(inputRI, null);
                for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                    for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                        for (int trow = 0; trow < tileHeight; trow++) {
                            int j = (tileY * tileHeight) + trow;
                            if ((j >= minY) && (j <= maxY)) {
                                for (int tcol = 0; tcol < tileWidth; tcol++) {
                                    int col = (tileX * tileWidth) + tcol;
                                    if ((col >= minX) && (col <= maxX)) {
                                        int k = col;
                                        final int yPos = height - j - 1;
                                        switch (datatype) {
                                        case BYTE:
                                            byte sampleByte = (byte) data.getSampleFloat(k, j, 0);
                                            matrix.setByte(Tima.set(b, yPos, k), sampleByte);
                                            break;
                                        case SHORT:
                                            short sampleShort = (short) data
                                                    .getSampleFloat(k, j, 0);
                                            matrix.setShort(Tima.set(b, yPos, k), sampleShort);
                                            break;
                                        case INT:
                                            int sampleInt = (int) data.getSampleFloat(k, j, 0);
                                            matrix.setInt(Tima.set(b, yPos, k), sampleInt);
                                            break;
                                        case FLOAT:
                                            float sampleFloat = data.getSampleFloat(k, j, 0);
                                            matrix.setFloat(Tima.set(b, yPos, k), sampleFloat);
                                            break;
                                        case DOUBLE:
                                            double sampleDouble = data.getSampleDouble(k, j, 0);
                                            matrix.setDouble(Tima.set(b, yPos, k), sampleDouble);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Finalize the iterator and dispose the Band
                data.done();
                band.dispose();
                listenerForwarder.progressing((100 * (b+1)) / (float)numBands, "                > Writing bands");
          }
        }
    }

    /**
     * Define the netcdf file structure (variables, dimensions, ...)
     * 
     * @param ncFileOut
     * @param outputProduct
     * @param latLonCoordinates
     * @param geophysics
     * @param forceCoordinates
     * @throws IOException
     * @throws InvalidRangeException
     */
    private static Map<String, VariablesGroup> defineOutputNetCDF(NetcdfFileWriteable ncFileOut,
            Product inputProduct, Product outputProduct, NCCoordinates latLonCoordinates,
            final boolean geophysics, final boolean forceCoordinates, final Map<String, NCCoordinateDimension> coordinateDimensions)
            throws IOException, InvalidRangeException {
        Utilities.ensureNonNull("ncFileOut", ncFileOut);

        // Initialize dimensions and global attributes to the ouput file
        initialize(ncFileOut, inputProduct, outputProduct, latLonCoordinates, coordinateDimensions, forceCoordinates);

        final int numBands = outputProduct.getNumBands();
        Map<String, BandsGroup> dimensionToBandsMap = new HashMap<String, BandsGroup>();
        Set<String> variableCoordinates = new HashSet<String>();

        //
        // STEP 1: Check for custom dimensions to do variables grouping
        //
        if (coordinateDimensions != null && !coordinateDimensions.isEmpty()) {

            // Group all bands related to the same dimension
            groupDimensions(outputProduct, coordinateDimensions, dimensionToBandsMap,
                    variableCoordinates);

        } else {
            List<Band> bands = new ArrayList<Band>();
            for (int i = 0; i < numBands; i++) {
                Band band = outputProduct.getBandAt(i);
                if (!NCUtilities.isLatLon(band.getName())) {
                    bands.add(band);
                }
            }
            dimensionToBandsMap.put(NONE, new BandsGroup(bands));
        }

        //
        // STEP 2: Create variable to netcdf output file
        //
        Map<String, VariablesGroup> dimensionGroup = initializeVariables(ncFileOut,
                dimensionToBandsMap, geophysics, latLonCoordinates, coordinateDimensions);
        return dimensionGroup;
    }

    /**
     * Write coordinates values for the related dimensions Note that on NetCDF, a Dimension may have a related coordinate variable containing values
     * along that dimension. This method will fill those values
     * 
     * @param ncFileOut
     * @param latLonCoordinates
     * @param coordinateDimensions
     * @param forceCoordinates Specify whether we need to create a coordinate variable in case it missing on the related dimensions
     * @throws IOException
     * @throws InvalidRangeException
     */
    private static void writeCoordinates(NetcdfFileWriteable ncFileOut,
            NCCoordinates latLonCoordinates, Map<String, NCCoordinateDimension> coordinateDimensions, 
            final boolean forceCoordinates)
            throws IOException, InvalidRangeException {

        ncFileOut.write(NCUtilities.LAT, latLonCoordinates.getLatitude().getData());
        ncFileOut.write(NCUtilities.LON, latLonCoordinates.getLongitude().getData());

        // Writing coordinates for dimensions if existing
        if (coordinateDimensions != null && !coordinateDimensions.isEmpty()) {
            Set<String> coordinates = coordinateDimensions.keySet();
            for (String coordinateName : coordinates) {
                NCCoordinateDimension coordinate = coordinateDimensions.get(coordinateName);
                if (coordinate.isHasCoords()) {
                    ncFileOut.write(coordinate.getDimension().getName(), coordinate.getVariable().read());
                } else if (forceCoordinates) {
                    final int length = coordinate.getDimension().getLength();
                    Array array = new ArrayInt(new int[]{3});
                    for (int i = 0; i < length; i++) {
                        array.setInt(i, i);
                    }
                    ncFileOut.write(coordinate.getDimension().getName(), array);
                }
            }
        }
    }

    /**
     * 
     * @param ncFileOut
     * @param dimensionToBandsMap
     * @param geophysics
     * @param latLonCoordinates
     * @param dimensions
     * @return
     */
    private static Map<String, VariablesGroup> initializeVariables(NetcdfFileWriteable ncFileOut,
            Map<String, BandsGroup> dimensionToBandsMap, boolean geophysics,
            NCCoordinates latLonCoordinates, Map<String, NCCoordinateDimension> dimensions) {

        // Default dimensions for each variable are latitude and longitude
        Set<String> dimension = dimensionToBandsMap.keySet();
        Iterator<String> dimensionIt = dimension.iterator();
        Map<String, VariablesGroup> dimensionGroup = new HashMap<String, VariablesGroup>();

        // Scan dimensions
        while (dimensionIt.hasNext()) {
            String dimensionsName = dimensionIt.next();
            VariablesGroup variablesGroup = new VariablesGroup();
            dimensionGroup.put(dimensionsName, variablesGroup);
            if (dimensionsName.equalsIgnoreCase(NONE)) {
                // CASE A: Dealing with Single bands
                // NONE is a Default dimension where all 2D variables have been previously collected
                HashMap<String, BandsGroup> variables = new HashMap<String, BandsGroup>();
                variablesGroup.setVariables(variables);

                BandsGroup singleBands = dimensionToBandsMap.get(dimensionsName);
                for (Band band : singleBands.bands) {
                    addVariable(band, variables, latLonCoordinates, ncFileOut, geophysics);
                }

            } else {
                // CASE B: Dealing with bands to be grouped by dimension and then by variable

                // Get all bands pertaining to that dimension and group them by variable
                final BandsGroup bandsForThisDimension = dimensionToBandsMap.get(dimensionsName);
                for (Band band : bandsForThisDimension.bands) {
                    handleBand(band, dimensionsName, variablesGroup, ncFileOut, dimensions, latLonCoordinates, geophysics);
                }
            }
        }
        return dimensionGroup;
    }

    /**
     * Add a Variable related to the specified BEAM Band to the specified NetCDF output file
     * 
     * @param band
     * @param variableBands
     * @param latLonCoordinates
     * @param ncFileOut
     * @param geophysics
     */
    private static void addVariable(Band band, HashMap<String, BandsGroup> variableBands,
            NCCoordinates latLonCoordinates, NetcdfFileWriteable ncFileOut, boolean geophysics) {

        // Get lat lon dimensions
        final Dimension latDim = latLonCoordinates.getLatitude().getDimension();
        final Dimension lonDim = latLonCoordinates.getLongitude().getDimension();

        // initialize variable
        final DataType dataType = NCUtilities.transcodeBeamDataType(band.getDataType());
        String bandName = band.getName();

        ncFileOut.addVariable(bandName, dataType, new Dimension[] { latDim, lonDim });
        NCUtilities.addAttributes(ncFileOut, band, bandName, geophysics);
        variableBands.put(bandName, new BandsGroup(Collections.singletonList(band)));

    }

    /**
     * Check whether the specified band is linked to the specified dimension. In that case, add an entry to the dimensionGroup. Finally, in case the
     * specified band is part of a multiBand variable which has not been initialized yet, add it that variable to the output file.
     * 
     * @param band
     * @param dimensionName
     * @param variablesGroup
     * @param ncFileOut
     * @param dimensions
     * @param latLonCoordinates
     * @param geophysics
     */
    private static void handleBand(Band band, String dimensionName, VariablesGroup variablesGroup,
            NetcdfFileWriteable ncFileOut, Map<String, NCCoordinateDimension> dimensions,
            NCCoordinates latLonCoordinates, boolean geophysics) {
        Map<String, BandsGroup> variablesForCurrentDimension = variablesGroup
                .getVariables();

        // BEAM Bands related to ND Dimensions will have a suffix in the name
        // containing the dimension name to which they are related... e.g.:
        // atmospheric_temperature_nlt1, atmospheric_temperature_nlt2, atmospheric_temperature_nlt3

        // look for that suffix...
        final String bandName = band.getName();
        final String searchKey = "_" + dimensionName;
        // _nlt as an instance

        // Check whether that band contains the dimension name
        if (bandName.contains(searchKey)) {

            final int startOfMatching = bandName.indexOf(searchKey);
            final int endOfMatching = startOfMatching + searchKey.length();

            // We do a substring to check whether we are dealing with the N-th band
            // of a multiBand variable
            final String remaining = bandName.substring(endOfMatching);
            if (remaining.length() > 0) {

                // Getting the variableName without progressive numbering
                // so that "atmospheric_temperature_nlt1" will become "atmospheric_temperature"
                final String varName = bandName.substring(0, startOfMatching);
                BandsGroup bandsForThisVariable = null;

                // check if we have already created a dimensions<->variables grouping for that dimension
                if (variablesForCurrentDimension == null) {
                    variablesForCurrentDimension = new HashMap<String, BandsGroup>();
                    variablesGroup.setVariables(variablesForCurrentDimension);
                }

                // check if we have already defined a Bands group for the current variable
                if (variablesForCurrentDimension.containsKey(varName)) {
                    bandsForThisVariable = variablesForCurrentDimension.get(varName);
                } else {

                    // initialize the mapping for that variable and add that variable
                    bandsForThisVariable = addVariable(ncFileOut, variablesForCurrentDimension,
                            varName, band, dimensions, dimensionName, latLonCoordinates, geophysics);
                }

                List<Band> bands = bandsForThisVariable.getBands();
                // Add this band to the list
                bands.add(band);
            }
        }
    }

    /**
     * Add a Variable to the output NetCDF file
     * 
     * @param ncFileOut
     * @param variablesForCurrentDimension
     * @param varName
     * @param band
     * @param dimensions
     * @param dimensionName
     * @param latLonCoordinates
     * @param geophysics
     * @return
     */
    private static BandsGroup addVariable(NetcdfFileWriteable ncFileOut,
            Map<String, BandsGroup> variablesForCurrentDimension, String varName, Band band,
            Map<String, NCCoordinateDimension> dimensions, String dimensionName,
            NCCoordinates latLonCoordinates, boolean geophysics) {

        final Dimension latDim = latLonCoordinates.getLatitude().getDimension();
        final Dimension lonDim = latLonCoordinates.getLongitude().getDimension();

        BandsGroup bandsForThisVariable = new BandsGroup();
        variablesForCurrentDimension.put(varName, bandsForThisVariable);

        // Create the variable
        final DataType dataType = NCUtilities.transcodeBeamDataType(band.getDataType());
        Dimension dim = dimensions.get(dimensionName).getDimension();
        Dimension[] varDims = new Dimension[] { dim, latDim, lonDim };
        ncFileOut.addVariable(varName, dataType, varDims);
        NCUtilities.addAttributes(ncFileOut, band, varName, geophysics);
        return bandsForThisVariable;

    }

    /**
     * Check whether this band is related to a specified dimensionName. Add that band to the dimension Map
     * 
     * @param dimensioName
     * @param band
     * @param dimensionToBandsMap
     */
    private static boolean linkBandToDimension(String dimensioName, Band band,
            Map<String, BandsGroup> dimensionToBandsMap) {
        final String searchKey = "_" + dimensioName;
        final String bandName = band.getName();
        if (bandName.contains(searchKey)) {
            final int startOfMatching = bandName.indexOf(searchKey);
            final int endOfMatching = startOfMatching + searchKey.length();
            final String remaining = bandName.substring(endOfMatching);
            if (remaining.length() > 0) {
                dimensionToBandsMap.get(dimensioName).add(band);
            }
            return true;
        }
        return false;
    }

    /**
     * 
     * 
     * @param outputProduct
     * @param dimensions
     * @param dimensionToBandsMap
     * @param variableCoordinates
     */
    private static void groupDimensions(Product outputProduct,
            Map<String, NCCoordinateDimension> dimensions,
            Map<String, BandsGroup> dimensionToBandsMap, Set<String> variableCoordinates) {

        // Group Bands for dimension
        Set<String> dimensionNames = dimensions.keySet();
        Iterator<String> dimensionsIterator = dimensionNames.iterator();
        while (dimensionsIterator.hasNext()) {
            String dimensionName = dimensionsIterator.next();

            // Create a BeamVariableBands for that dimensions to be populated
            dimensionToBandsMap.put(dimensionName, new BandsGroup());

            // Populating the coordinates set in order to exclude Bands representing coordinates
            // since they have been already initialized
            Variable var = dimensions.get(dimensionName).getVariable();
            if (var != null) {
                variableCoordinates.add(var.getName());
            }
        }

        // Add default NONE Dimension for variables which won't be grouped together
        dimensionToBandsMap.put(NONE, new BandsGroup());

        final int numBands = outputProduct.getNumBands();

        // Loop over bands to group them by Dimension
        for (int i = 0; i < numBands; i++) {

            dimensionsIterator = dimensionNames.iterator();

            // Loop over dimensions to check whether that variable is related to this dimension
            boolean dimensionFound = false;
            final Band band = outputProduct.getBandAt(i);
            while (dimensionsIterator.hasNext()) {
                final String dimensioName = dimensionsIterator.next();
                final String bandName = band.getName();

                // exclude lat lon as well as coordinates variable
                if (NCUtilities.isLatLon(bandName) || variableCoordinates.contains(bandName)) {
                    dimensionFound = true;
                    break;
                }

                // check whether this variable name should be linked to a dimension
                dimensionFound = linkBandToDimension(dimensioName, band, dimensionToBandsMap);
                if (dimensionFound) {
                    break;
                }
            }
            if (!dimensionFound) {
                dimensionToBandsMap.get(NONE).add(band);
            }
        }
    }

    /**
     * Proceed with adding dimensions and coordinates
     * 
     * @param outputFilePath
     * @param inputProduct
     * @param reprojectedProduct
     * @param latLonCoordinates
     * @param coordinateDimensions
     * @param forceCoordinates
     * @return
     * @throws IOException
     */
    private static NetcdfFileWriteable initialize(NetcdfFileWriteable ncFileOut,
            Product inputProduct, Product reprojectedProduct, NCCoordinates latLonCoordinates,
            Map<String, NCCoordinateDimension> coordinateDimensions, final boolean forceCoordinates) throws IOException {

        // Get geocoding from a coordinate band
        final Band band = reprojectedProduct.getBand(NCUtilities.LAT);
        final GeoCoding geoCoding = band.getGeoCoding();

        // Get geotransformation and image properties to setup the coordinates
        final MathTransform transform = geoCoding.getImageToMapTransform();
        final MultiLevelImage image = band.getGeophysicalImage();
        final RenderedImage ri = image.getImage(0);

        // Add dimensions and related coordinates
        NCUtilities
                .addDimensions(ncFileOut, ri, transform, latLonCoordinates, coordinateDimensions, forceCoordinates);

        // copy global attributes
        NCUtilities.copyGlobalAttributes(ncFileOut, inputProduct);
        return ncFileOut;
    }
}
