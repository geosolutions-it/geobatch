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

import it.geosolutions.geobatch.beam.msgwarp.MSGProduct;

import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.geotools.util.Utilities;
import org.opengis.referencing.operation.MathTransform;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * A NetCDF Utilities class 
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public class NCUtilities {
    private static final String UP = "up";

    private static final String COORDINATE_ZIS_POSITIVE = "_CoordinateZisPositive";

    private static final String GEO_Z = "GeoZ";

    private static final String COORDINATE_AXIS_TYPE = "_CoordinateAxisType";

    private static final String VERTICAL = "Vertical";

    private static final String COORDINATE_TRANSFORM_TYPE = "_CoordinateTransformType";

    public final static String LAT = "lat";

    public final static String LON = "lon";

    public final static String LATITUDE = "latitude";

    public final static String LONGITUDE = "longitude";

    public final static String UNITS = "units";

    public final static String LONGNAME = "long_name";

    public final static String STANDARD_NAME = "standard_name";

    public final static String DESCRIPTION = "description";

    public final static String FILLVALUE = "_FillValue";

    public final static String MISSING_VALUE = "missing_value";

    public final static String LON_UNITS = "degrees_east";

    public final static String LAT_UNITS = "degrees_north";

    public final static String NO_COORDS = "NoCoords";

    public final static String SCALE_OFFSET = "add_offset";

    public final static String SCALE_FACTOR = "scale_factor";

    final static Set<String> EXCLUDED_ATTRIBUTES = new HashSet<String>();

    private static final String COMMENT = "comment";

    private static final String VALID_MAX = "valid_max";

    private static final String VALID_MIN = "valid_min";

    static {
        EXCLUDED_ATTRIBUTES.add(UNITS);
        EXCLUDED_ATTRIBUTES.add(LONGNAME);
        EXCLUDED_ATTRIBUTES.add(DESCRIPTION);
        EXCLUDED_ATTRIBUTES.add(STANDARD_NAME);
    }

    /**
     * NetCDF Coordinate holder (Dimension and data values)
     */
    static class NCCoordinateDimension {

        public NCCoordinateDimension(Dimension dimension) {
            this.dimension = dimension;
            this.variable = null;
            this.data = null;
            this.hasCoords = false;
        }
        
        public NCCoordinateDimension(Dimension dimension, Variable variable, Array data) {
            this.dimension = dimension;
            this.variable = variable;
            this.data = data;
            this.hasCoords = true;
        }

        private Dimension dimension;

        private Array data;

        private Variable variable;
        
        private boolean hasCoords;

        public Array getData() {
            return data;
        }

        public void setData(Array data) {
            this.data = data;
        }

        public Dimension getDimension() {
            return dimension;
        }

        public void setDimension(Dimension dimension) {
            this.dimension = dimension;
        }

        public Variable getVariable() {
            return variable;
        }

        public void setVariable(Variable variable) {
            this.variable = variable;
        }

        public boolean isHasCoords() {
            return hasCoords;
        }

        public void setHasCoords(boolean hasCoords) {
            this.hasCoords = hasCoords;
        }
    }
    
    static class NCCoordinates
    {
        public NCCoordinates() {
        }

        public NCCoordinates(NCCoordinateDimension longitude, NCCoordinateDimension latitude) {
            super();
            this.longitude = longitude;
            this.latitude = latitude;
        }

        private NCCoordinateDimension longitude;
        
        private NCCoordinateDimension latitude;

        public NCCoordinateDimension getLongitude() {
            return longitude;
        }

        public void setLongitude(NCCoordinateDimension longitude) {
            this.longitude = longitude;
        }

        public NCCoordinateDimension getLatitude() {
            return latitude;
        }

        public void setLatitude(NCCoordinateDimension latitude) {
            this.latitude = latitude;
        }
    }
    
    /**
     * Transcode a NetCDF data type into a java2D  DataBuffer type.
     * 
     * @param type the {@link DataType} to transcode.
     * @param unsigned if the original data is unsigned or not
     * @return an int representing the correct DataBuffer type.
     */
        public static int transcodeNetCDFDataType(
                        final DataType type,
                        final boolean unsigned) {
                if (DataType.BOOLEAN.equals(type) || DataType.BYTE.equals(type)) {
            return DataBuffer.TYPE_BYTE;
        }
        if (DataType.CHAR.equals(type)) {
            return DataBuffer.TYPE_USHORT;
        }
        if (DataType.SHORT.equals(type)) {
            return unsigned ? DataBuffer.TYPE_USHORT: DataBuffer.TYPE_SHORT;
        }
        if (DataType.INT.equals(type)) {
            return DataBuffer.TYPE_INT;
        }
        if (DataType.FLOAT.equals(type)) {
            return DataBuffer.TYPE_FLOAT;
        }
        if (DataType.LONG.equals(type) || DataType.DOUBLE.equals(type)) {
            return DataBuffer.TYPE_DOUBLE;
        }
        return DataBuffer.TYPE_UNDEFINED;
        }
        
    /**
     * Transcode a Beam data type into a NetCDF DataType type.
     * 
     * @param type the beam {@link ProductData} type to transcode.
     * @return an NetCDF DataType type.
     */
    public static DataType transcodeBeamDataType(final int dataType) {
        switch (dataType) {
        case ProductData.TYPE_INT8:
        case ProductData.TYPE_UINT8:
            return DataType.BYTE;
        case ProductData.TYPE_INT16:
            return DataType.SHORT;
        case ProductData.TYPE_INT32:
        case ProductData.TYPE_UINT32:
            return DataType.INT;
        case ProductData.TYPE_FLOAT64:
            return DataType.DOUBLE;
        case ProductData.TYPE_FLOAT32:
            return DataType.FLOAT;
        case DataBuffer.TYPE_UNDEFINED:
        default:
            throw new IllegalArgumentException("Invalid input data type:" + dataType);
        }
    }

    /**
     * Get an Array of proper size and type.
     * 
     * @param dimensions the dimensions
     * @param varDataType the DataType of the required array 
     * @return
     */
    public static Array getArray(int[] dimensions, DataType varDataType) {
        if (dimensions == null)
            throw new IllegalArgumentException("Illegal dimensions");
        final int nDims = dimensions.length;
        switch (nDims) {
        case 4:
            // 4D Arrays
            if (varDataType == DataType.FLOAT) {
                return new ArrayFloat.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else if (varDataType == DataType.DOUBLE) {
                return new ArrayDouble.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else if (varDataType == DataType.BYTE) {
                return new ArrayByte.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else if (varDataType == DataType.SHORT) {
                return new ArrayShort.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else if (varDataType == DataType.INT) {
                return new ArrayInt.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else
                throw new IllegalArgumentException(
                        "Actually unsupported Datatype");

        case 3:
            // 3D Arrays
            if (varDataType == DataType.FLOAT) {
                return new ArrayFloat.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else if (varDataType == DataType.DOUBLE) {
                return new ArrayDouble.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else if (varDataType == DataType.BYTE) {
                return new ArrayByte.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else if (varDataType == DataType.SHORT) {
                return new ArrayShort.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else if (varDataType == DataType.INT) {
                return new ArrayInt.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else
                throw new IllegalArgumentException(
                        "Actually unsupported Datatype");
        case 2:
            // 2D Arrays
            if (varDataType == DataType.FLOAT) {
                return new ArrayFloat.D2(dimensions[0], dimensions[1]);
            } else if (varDataType == DataType.DOUBLE) {
                return new ArrayDouble.D2(dimensions[0], dimensions[1]);
            } else if (varDataType == DataType.BYTE) {
                return new ArrayByte.D2(dimensions[0], dimensions[1]);
            } else if (varDataType == DataType.SHORT) {
                return new ArrayShort.D2(dimensions[0], dimensions[1]);
            } else if (varDataType == DataType.INT) {
                return new ArrayInt.D2(dimensions[0], dimensions[1]);
            } else
                throw new IllegalArgumentException(
                        "Actually unsupported Datatype");
        }
        throw new IllegalArgumentException(
                "Unable to create a proper array unsupported Datatype");
    }

    static boolean isLatLon(String bandName) {
        return bandName.equalsIgnoreCase(LON)
                || bandName.equalsIgnoreCase(LAT);
    }

    static void addAttributes(NetcdfFileWriteable ncFileOut, Band band,
            final String varName, boolean isGeophysics) {
        Utilities.ensureNonNull("ncFileOut", ncFileOut);
        Utilities.ensureNonNull("band", band);
        ncFileOut.addVariableAttribute(varName, DESCRIPTION, band.getDescription());
        String unit = band.getUnit();
        if (unit != null) {
            ncFileOut.addVariableAttribute(varName, UNITS, band.getUnit());
        }
        ncFileOut.addVariableAttribute(varName, FILLVALUE,
                isGeophysics ? band.getGeophysicalNoDataValue() : band.getNoDataValue());
    
        // TODO need to add more attributes when dealing with non geophysical values

        // if the input band belongs to a MSGProduct
        Product prod = band.getProduct();
        if(prod instanceof MSGProduct){
            ncFileOut.addVariableAttribute(varName, NCUtilities.LONGNAME, band.getDescription());
            ncFileOut.addVariableAttribute(varName, NCUtilities.SCALE_FACTOR, band.getScalingFactor());
            ncFileOut.addVariableAttribute(varName, NCUtilities.SCALE_OFFSET, band.getScalingOffset());
            String comment = band.getComment();
            if(comment!=null && !comment.isEmpty()){
                ncFileOut.addVariableAttribute(varName, NCUtilities.COMMENT, comment);
            }

            String standardName = band.getStandardName();
            if(standardName!=null && !standardName.isEmpty()){
                ncFileOut.addVariableAttribute(varName, NCUtilities.STANDARD_NAME, standardName);
            }

            Number validMax = band.getValidMax();
            if(validMax!=null){
                ncFileOut.addVariableAttribute(varName, NCUtilities.VALID_MAX, validMax);
            }

            Number validMin = band.getValidMin();
            if(validMin!=null){
                ncFileOut.addVariableAttribute(varName, NCUtilities.VALID_MIN, validMin);
            }
        }
    }

    static void copyGlobalAttributes(NetcdfFileWriteable ncFileOut, Product netcdfProduct) {
        // TODO Auto-generated method stub
        Utilities.ensureNonNull("netcdfProduct", netcdfProduct);
        MetadataElement root = netcdfProduct.getMetadataRoot();
        if (root != null) {
            MetadataElement attribs = root.getElement("global_attributes");
            if (attribs != null) {
                MetadataAttribute[] attributes = attribs.getAttributes();
                for (MetadataAttribute attrib : attributes) {
                    // TODO: Brute setting, fix that
                    ncFileOut
                            .addGlobalAttribute(attrib.getName(), attrib.getData().getElemString());
                }
            }
        }
    }
    
    /**
     * Setup lat lon dimension and related coordinates variable
     * 
     * @param ncFileOut
     * @param ri
     * @param transform
     * @param latLonCoordinates
     */
    static void setupLatLon(NetcdfFileWriteable ncFileOut, RenderedImage ri,
            MathTransform transform, NCCoordinates latLonCoordinates) {
        final int numLat = ri.getHeight();
        final int numLon = ri.getWidth();

        AffineTransform at = (AffineTransform) transform;

        // Setup resolutions and bbox extrema to populate regularly gridded coordinate data
        double ymax = at.getTranslateY();
        double xmin = at.getTranslateX();
        double periodY = -at.getScaleY();
        double periodX = at.getScaleX();
        double ymin = ymax - (periodY * numLat);

        // Adding lat lon dimensions
        Dimension latDim = ncFileOut.addDimension(NCUtilities.LAT, numLat);
        Dimension lonDim = ncFileOut.addDimension(NCUtilities.LON, numLon);

        // lat Variable
        ArrayFloat latData = new ArrayFloat(new int[] { numLat });
        Index latIndex = latData.getIndex();
        ncFileOut.addVariable(NCUtilities.LAT, DataType.FLOAT, new Dimension[] { latDim });
        ncFileOut.addVariableAttribute(NCUtilities.LAT, NCUtilities.LONGNAME, NCUtilities.LATITUDE);
        ncFileOut.addVariableAttribute(NCUtilities.LAT, NCUtilities.UNITS, NCUtilities.LAT_UNITS);

        // Flipping Y Axis
        for (int yPos = 0; yPos < numLat; yPos++) {
            latData.setFloat(latIndex.set(yPos),
            // new Float(
            // ymax
            // - (new Float(yPos)
            // .floatValue() * periodY))
            // .floatValue());
                    new Float(ymin + (new Float(yPos).floatValue() * periodY)).floatValue());
        }

        // lon Variable
        ArrayFloat lonData = new ArrayFloat(new int[] { numLon });
        Index lonIndex = lonData.getIndex();
        ncFileOut.addVariable(NCUtilities.LON, DataType.FLOAT, new Dimension[] { lonDim });
        ncFileOut
                .addVariableAttribute(NCUtilities.LON, NCUtilities.LONGNAME, NCUtilities.LONGITUDE);
        ncFileOut.addVariableAttribute(NCUtilities.LON, NCUtilities.UNITS, NCUtilities.LON_UNITS);
        for (int xPos = 0; xPos < numLon; xPos++) {
            lonData.setFloat(lonIndex.set(xPos), new Float(xmin
                    + (new Float(xPos).floatValue() * periodX)).floatValue());
        }

        // Setting lat lon coordinates values
        final NCCoordinateDimension latitude = new NCCoordinateDimension(latDim, null, latData);
        final NCCoordinateDimension longitude = new NCCoordinateDimension(lonDim, null, lonData);
        latLonCoordinates.setLongitude(longitude);
        latLonCoordinates.setLatitude(latitude);
    }

    /**
     * 
     * @param ncFileOut
     * @param ri
     * @param transform
     * @param latLonCoordinates
     * @param coordinateDimensions
     */
    static void addDimensions(NetcdfFileWriteable ncFileOut, RenderedImage ri,
            MathTransform transform, NCCoordinates latLonCoordinates,
            Map<String, NCCoordinateDimension> coordinateDimensions, final boolean forceCoordinates) {

        // Setting up LatLon
        NCUtilities.setupLatLon(ncFileOut, ri, transform, latLonCoordinates);

        // Dealing with custom dimensions
        if (coordinateDimensions != null) {

            // Initializing coordinates for those dimensions
            Set<String> dimensionNames = coordinateDimensions.keySet();
            Iterator<String> dimensionsIterator = dimensionNames.iterator();
            while (dimensionsIterator.hasNext()) {
                String dimensionName = dimensionsIterator.next();
                NCCoordinateDimension nccoord = coordinateDimensions.get(dimensionName);
                Variable var = nccoord.getVariable();
                final Dimension coordDim = nccoord.getDimension();
                ncFileOut.addDimension(dimensionName, coordDim.getLength());

                // Assign coordinates to dimensions having coordinates
                if (nccoord.isHasCoords()) {
                    ncFileOut.addVariable(dimensionName, var.getDataType(),
                            new Dimension[] { nccoord.getDimension() });
                    ncFileOut.addVariableAttribute(dimensionName, NCUtilities.LONGNAME, var.getFullName());
                    ncFileOut.addVariableAttribute(dimensionName, NCUtilities.DESCRIPTION, var.getFullName());
                    ncFileOut.addVariableAttribute(dimensionName, NCUtilities.UNITS, var.getUnitsString());
                    copyAttributes(ncFileOut, dimensionName, var, EXCLUDED_ATTRIBUTES);
                } else if (forceCoordinates){
                    ncFileOut.addVariable(dimensionName, DataType.INT, new Dimension[] { nccoord.getDimension() });
                    ncFileOut.addVariableAttribute(dimensionName, NCUtilities.LONGNAME, dimensionName);
                    ncFileOut.addVariableAttribute(dimensionName, NCUtilities.DESCRIPTION, dimensionName);
                    ncFileOut.addVariableAttribute(dimensionName, COORDINATE_TRANSFORM_TYPE, VERTICAL);
                    ncFileOut.addVariableAttribute(dimensionName, COORDINATE_AXIS_TYPE, GEO_Z);
                    ncFileOut.addVariableAttribute(dimensionName, COORDINATE_ZIS_POSITIVE, UP);
                }
            }
        }
    }

    private static void copyAttributes(NetcdfFileWriteable ncFileOut, String dimensionName,
            Variable var, Set<String> excludedAttributes) {
        List<Attribute> inputAttributes = var.getAttributes();
        for (Attribute inputAttribute: inputAttributes) {
            String attributeName = inputAttribute.getName();
            if (!excludedAttributes.contains(attributeName)) {
                ncFileOut.addVariableAttribute(dimensionName, inputAttribute);
            }
        }
    }
}
