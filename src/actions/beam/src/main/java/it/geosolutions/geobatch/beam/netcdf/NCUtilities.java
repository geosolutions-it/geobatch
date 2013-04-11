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

import java.awt.image.DataBuffer;

import org.esa.beam.framework.datamodel.ProductData;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;

/**
 * A NetCDF Utilities class 
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public class NCUtilities {
    public final static String LAT = "lat";

    public final static String LON = "lon";

    public final static String LATITUDE = "latitude";

    public final static String LONGITUDE = "longitude";

    public final static String UNITS = "units";

    public final static String LONGNAME = "long_name";
    
    public final static String DESCRIPTION = "description";
    
    public final static String FILLVALUE = "_FillValue";
    
    public final static String MISSING_VALUE = "missing_value";

    public final static String LON_UNITS = "degrees_east";

    public final static String LAT_UNITS = "degrees_north";

    /**
     * NetCDF Coordinate holder (Dimension and data values)
     */
    static class NCCoordinate {
        public NCCoordinate(Dimension dimension, ArrayFloat data) {
            super();
            this.dimension = dimension;
            this.data = data;
        }

        private Dimension dimension;
        
        private ArrayFloat data;

        public ArrayFloat getData() {
            return data;
        }

        public void setData(ArrayFloat data) {
            this.data = data;
        }

        public Dimension getDimension() {
            return dimension;
        }

        public void setDimension(Dimension dimension) {
            this.dimension = dimension;
        }
    }
    
    static class NCCoordinates
    {
        public NCCoordinates() {
        }

        public NCCoordinates(NCCoordinate longitude, NCCoordinate latitude) {
            super();
            this.longitude = longitude;
            this.latitude = latitude;
        }

        private NCCoordinate longitude;
        
        private NCCoordinate latitude;

        public NCCoordinate getLongitude() {
            return longitude;
        }

        public void setLongitude(NCCoordinate longitude) {
            this.longitude = longitude;
        }

        public NCCoordinate getLatitude() {
            return latitude;
        }

        public void setLatitude(NCCoordinate latitude) {
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
}
