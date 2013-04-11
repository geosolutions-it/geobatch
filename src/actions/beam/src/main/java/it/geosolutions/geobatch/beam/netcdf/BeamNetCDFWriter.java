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
import it.geosolutions.geobatch.beam.netcdf.NCUtilities.NCCoordinate;
import it.geosolutions.geobatch.beam.netcdf.NCUtilities.NCCoordinates;

import java.awt.geom.AffineTransform;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.geotools.util.Utilities;
import org.opengis.referencing.operation.MathTransform;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

import com.bc.ceres.glevel.MultiLevelImage;

/**
 * A class which store the reprojected product as a NetCDF file
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 * 
 */
public class BeamNetCDFWriter implements BeamFormatWriter {

    public void storeProduct(final String outputFilePath, Product inputProduct,
            Product reprojectedProduct, boolean geophysics) throws IOException {

        final int numBands = reprojectedProduct.getNumBands();

        // Initializing longitude and latitude coordinates
        final NCUtilities.NCCoordinates coordinates = new NCUtilities.NCCoordinates();
        final NetcdfFileWriteable ncFileOut = initFile(outputFilePath, inputProduct,
                reprojectedProduct, coordinates);

        try {
            finalizeInitialization(ncFileOut, reprojectedProduct, coordinates, geophysics);

            for (int i = 0; i < numBands; i++) {
                
                Band band = reprojectedProduct.getBandAt(i);
                final String bandName = band.getName();
                if (bandName.equalsIgnoreCase("lon") || bandName.equalsIgnoreCase("lat")) {
                    continue;
                }

                // Adding variable to the outputFile
                setData(ncFileOut, band, geophysics);
            }

            ncFileOut.close();
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }

    }

    private void setData(final NetcdfFileWriteable ncFileOut, final Band band, final boolean geophysics) throws IOException, InvalidRangeException {
        final String bandName = band.getName();
        final MultiLevelImage image = geophysics ? band.getGeophysicalImage() : band.getSourceImage();
        final RenderedImage ri = image.getImage(0);
        final int height = ri.getHeight();
        final int width = ri.getWidth();
        final DataType datatype = NCUtilities.transcodeBeamDataType(geophysics ?  band.getGeophysicalDataType() : band.getDataType());
        final Array Tmatrix = NCUtilities.getArray(new int[]{height, width}, datatype);

        Raster raster = ri.getData(); // Consider looping over tiles instead of calling getData which does a copy
        Index Tima = Tmatrix.getIndex();
        for (int j = 0; j < height; j++) {
            for (int k = 0; k < width; k++) {
                // need to flip on Y axis
                final int yPos = height - j - 1;
                switch (datatype) {
                case BYTE:
                    byte sampleByte = (byte) raster.getSampleFloat(k, j, 0);
                    Tmatrix.setByte(Tima.set(yPos, k), sampleByte);
                    break;
                case SHORT:
                    short sampleShort = (short) raster.getSampleFloat(k, j, 0);
                    Tmatrix.setShort(Tima.set(yPos, k), sampleShort);
                    break;
                case INT:
                    int sampleInt = (int) raster.getSampleFloat(k, j, 0);
                    Tmatrix.setInt(Tima.set(yPos, k), sampleInt);
                    break;
                case FLOAT:
                    float sampleFloat = raster.getSampleFloat(k, j, 0);
                    Tmatrix.setFloat(Tima.set(yPos, k), sampleFloat);
                    break;
                case DOUBLE:
                    double sampleDouble = raster.getSampleDouble(k, j, 0);
                    Tmatrix.setDouble(Tima.set(yPos, k), sampleDouble);
                    break;
                }
            }
        }
        ncFileOut.write(bandName, Tmatrix);
        
    }

    /**
     * Finalize the netcdf file initialization
     * 
     * @param ncFileOut
     * @param reprojectedProduct
     * @param coordinates
     * @param geophysics
     * @throws IOException
     * @throws InvalidRangeException
     */
    private static void finalizeInitialization(NetcdfFileWriteable ncFileOut,
            Product reprojectedProduct, NCCoordinates coordinates, final boolean geophysics)
            throws IOException, InvalidRangeException {
        Utilities.ensureNonNull("ncFileOut", ncFileOut);
        final Dimension latDim = coordinates.getLatitude().getDimension();
        final Dimension lonDim = coordinates.getLongitude().getDimension();

        final int numBands = reprojectedProduct.getNumBands();
        for (int i = 0; i < numBands; i++) {
            Band band = reprojectedProduct.getBandAt(i);
            final String bandName = band.getName();
            if (bandName.equalsIgnoreCase("lon") || bandName.equalsIgnoreCase("lat")) {
                continue;
            }

            final DataType dataType = NCUtilities.transcodeBeamDataType(band.getDataType());
            ncFileOut.addVariable(bandName, dataType, new Dimension[] { latDim, lonDim });
            addAttributes(ncFileOut, band, geophysics);

        }

        // initialize coordinates
        ncFileOut.create();
        ncFileOut.write("lat", coordinates.getLatitude().getData());
        ncFileOut.write("lon", coordinates.getLongitude().getData());

    }

    private static void addAttributes(NetcdfFileWriteable ncFileOut, Band band, boolean isGeophysics) {
        Utilities.ensureNonNull("ncFileOut", ncFileOut);
        Utilities.ensureNonNull("band", band);
        final String bandName = band.getName();
        ncFileOut.addVariableAttribute(bandName, NCUtilities.DESCRIPTION, band.getDescription());
        String unit = band.getUnit();
        if (unit != null) {
            ncFileOut.addVariableAttribute(bandName, NCUtilities.UNITS, band.getUnit());
        }
        ncFileOut.addVariableAttribute(bandName, NCUtilities.FILLVALUE, isGeophysics ? band.getGeophysicalNoDataValue() : band.getNoDataValue());
        
        //TODO need to add more attributes when dealing with non geophysical values
    }

    private static NetcdfFileWriteable initFile(String outputFilePath, Product inputProduct,
            Product reprojectedProduct, NCCoordinates coordinates) throws IOException {
        // Get geocoding from a coordinate band
        final Band band = reprojectedProduct.getBand("lat");
        final GeoCoding geoCoding = band.getGeoCoding();

        // Get geotransformation and image properties to setup the coordinates
        final MathTransform transform = geoCoding.getImageToMapTransform();
        MultiLevelImage image = band.getGeophysicalImage();
        RenderedImage ri = image.getImage(0);
        NetcdfFileWriteable ncFileOut = NetcdfFileWriteable.createNew(outputFilePath);
        setCoordinates(ncFileOut, ri, transform, coordinates);
        copyGlobalAttributes(ncFileOut, inputProduct);
        return ncFileOut;
    }

    /**
     * 
     * @param ncFileOut
     * @param ri
     * @param transform
     * @param coordinates
     */
    private static void setCoordinates(NetcdfFileWriteable ncFileOut, RenderedImage ri,
            MathTransform transform, NCCoordinates coordinates) {
        final int numLat = ri.getHeight();
        final int numLon = ri.getWidth();

        AffineTransform at = (AffineTransform) transform;
        double ymax = at.getTranslateY();
        double xmin = at.getTranslateX();
        double periodY = -at.getScaleY();
        double periodX = at.getScaleX();
        double ymin = ymax - (periodY * numLat);

        Dimension latDim = ncFileOut.addDimension(NCUtilities.LAT, numLat);
        Dimension lonDim = ncFileOut.addDimension(NCUtilities.LON, numLon);

        // lat Variable
        ArrayFloat latData = new ArrayFloat(new int[] { numLat });
        Index latIndex = latData.getIndex();
        ncFileOut.addVariable(NCUtilities.LAT, DataType.FLOAT, new Dimension[] { latDim });
        ncFileOut.addVariableAttribute(NCUtilities.LAT, NCUtilities.LONGNAME, NCUtilities.LATITUDE);
        ncFileOut.addVariableAttribute(NCUtilities.LAT, NCUtilities.UNITS, NCUtilities.LAT_UNITS);

        // Consider flipping
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
        final NCCoordinate latitude = new NCCoordinate(latDim, latData);
        final NCCoordinate longitude = new NCCoordinate(lonDim, lonData);
        coordinates.setLongitude(longitude);
        coordinates.setLatitude(latitude);
    }

    private static void copyGlobalAttributes(NetcdfFileWriteable ncFileOut, Product netcdfProduct) {
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
}
