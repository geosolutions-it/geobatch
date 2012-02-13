/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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

import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author afabiani
 * 
 *         NOTE: may we want to use some other already defined classes ??? GranuleDescriptor....
 * 
 */
final class ImageMosaicGranulesDescriptor {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ImageMosaicGranulesDescriptor.class);

    // TODO a better file filter
    private final static Collector coll = new Collector(new SuffixFileFilter(new String[] { ".tif",
            ".tiff" }, IOCase.INSENSITIVE));

    private String coverageStoreId = null;

    private String metocFields = null;

    private List<String[]> fileListNameParts = null;

    // private String[] firstCvNameParts = null;
    //
    // private String[] lastCvNameParts = null;

    public List<String[]> getFileListNameParts() {
        return fileListNameParts;
    }

    // FROM COARDS
    private Double noData;// NoData

    private Double maxZ;// Max Z

    private Double minZ;// Min Z

    private String baseTime;// Base Time

    private String forecastTime; // Forecast Time

    private Integer TAU;// TAU

    /**
     * @param coverageStoreId
     * @param metocFields
     * @param firstCvNameParts
     * @param lastCvNameParts
     */
    private ImageMosaicGranulesDescriptor(final String coverageStoreId, final String metocFields,
            final List<String[]> fileListNameParts) {
        this.coverageStoreId = coverageStoreId;
        this.metocFields = metocFields;
        this.fileListNameParts = fileListNameParts;
        // this.firstCvNameParts = firstCvNameParts;
        // this.lastCvNameParts = lastCvNameParts;
    }

    public void setCoverageStoreId(String coverageStoreId) {
        this.coverageStoreId = coverageStoreId;
    }

    private ImageMosaicGranulesDescriptor() {
    };

    public Double getNoData() {
        return noData;
    }

    public void setNoData(Double noData) {
        this.noData = noData;
    }

    public Double getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(Double maxZ) {
        this.maxZ = maxZ;
    }

    public Double getMinZ() {
        return minZ;
    }

    public void setMinZ(Double minZ) {
        this.minZ = minZ;
    }

    public Integer getTAU() {
        return TAU;
    }

    public void setTAU(Integer tAU) {
        TAU = tAU;
    }

    public void setMetocFields(final String metocFields) {
        this.metocFields = metocFields;
    }

    // public void setFirstCvNameParts(String[] firstCvNameParts) {
    // this.firstCvNameParts = firstCvNameParts;
    // }
    //
    // public void setLastCvNameParts(String[] lastCvNameParts) {
    // this.lastCvNameParts = lastCvNameParts;
    // }

    public String getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(String baseTime) {
        this.baseTime = baseTime;
    }

    public String getForecastTime() {
        return forecastTime;
    }

    public void setForecastTime(String forecastTime) {
        this.forecastTime = forecastTime;
    }

    /**
     * @return the coverageStoreId
     */
    public String getCoverageStoreId() {
        return coverageStoreId;
    }

    /**
     * @return the metocFields
     */
    public String getMetocFields() {
        return metocFields;
    }

    /**
     * @return the firstCvNameParts
     */
    // public String[] getFirstCvNameParts() {
    // return firstCvNameParts;
    // }
    //
    // /**
    // * @return the lastCvNameParts
    // */
    // public String[] getLastCvNameParts() {
    // return lastCvNameParts;
    // }

    protected static ImageMosaicGranulesDescriptor buildDescriptor(ImageMosaicCommand cmd,
            ImageMosaicConfiguration config) {

        final File inputDir = cmd.getBaseDir();

        final List<File> fileNameList = coll.collect(inputDir);

        // add from command
        if (cmd.getAddFiles() != null) {
            for (File file : cmd.getAddFiles()) {
                if (!fileNameList.contains(file)) {
                    fileNameList.add(file);
                }
            }
        }
        // remove from command
        if (cmd.getDelFiles() != null) {
            for (File file : cmd.getDelFiles()) {
                if (!fileNameList.contains(file)) {
                    fileNameList.remove(file);
                }
            }
        }

        if (fileNameList == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Unable to collect files from the dir: "
                        + inputDir.getAbsolutePath());
            }
            return null;
        }

        return buildDescriptor(inputDir, fileNameList, config);
    }

    /**
     * Try to parse the input File name to build an Image Mosaic Descriptor
     * 
     * @param inputDir
     * @param mosaicDescriptor
     * @return
     */
    protected static ImageMosaicGranulesDescriptor buildDescriptor(File inputDir,
            ImageMosaicConfiguration config) {

        List<File> fileNameList = null;

        fileNameList = coll.collect(inputDir);

        if (fileNameList == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Unable to collect files from the dir: "
                        + inputDir.getAbsolutePath());
            }
            return null;
        }

        return buildDescriptor(inputDir, fileNameList, config);
    }

    /**
     * 
     * @param fileNameList
     *            (must be not null)
     * @param inputDir
     *            baseDir (must be not null)
     * @return
     */
    private static ImageMosaicGranulesDescriptor buildDescriptor(File inputDir,
            List<File> fileNameList, ImageMosaicConfiguration config) {
        // Store ID
        final String coverageStoreId = inputDir.getName();

        //
        if (fileNameList.size() > 0) {

            // to get it ordered from the first to the last file (by Name)
            Collections.sort(fileNameList, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            // generate the ORDERED list of file name splitted by name parts using "_"
            final List<String[]> fileListNameParts = new ArrayList<String[]>();
            for (File file : fileNameList) {
                fileListNameParts.add(FilenameUtils.getBaseName(file.getName()).split("_"));
            }
            // final File firstFile = fileNameList.get(0);
            // final String[] firstCvNameParts =
            // FilenameUtils.getBaseName(firstFile.getName()).split(
            // "_");
            //
            // final File lastFile = fileNameList.get(fileNameList.size() - 1);
            // final String[] lastCvNameParts = FilenameUtils.getBaseName(lastFile.getName()).split(
            // "_");

            if (config.isCOARDS()) {
                final ImageMosaicGranulesDescriptor mosaicDescriptor = buildCOARDS(coverageStoreId,
                        coverageStoreId, fileListNameParts);
                if (mosaicDescriptor != null) {
                    return mosaicDescriptor;
                } else {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Unable to use COARDS naming convention for file: "
                                + fileNameList.get(0)
                                + "\nLet's use default configuration parameters.");
                    }
                }
            }
            return new ImageMosaicGranulesDescriptor(coverageStoreId, coverageStoreId,
                    fileListNameParts);
            // firstCvNameParts, lastCvNameParts);

        } else {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("The passed base dir is empty! Dir:"
                        + inputDir.getAbsolutePath());
            }

            return new ImageMosaicGranulesDescriptor(coverageStoreId, coverageStoreId, null);
        }
    }

    /**
     * This method should implement the COARDS file name recognition and update the
     * ImageMosaicGranulesDescriptor members
     * 
     * @param coverageID
     * @param firstCvNameParts
     * @param lastCvNameParts
     * @return
     */
    private static ImageMosaicGranulesDescriptor buildCOARDS(final String storeID,
            final String coverageID, final List<String[]> fileListNameParts) {
        // final String[] firstCvNameParts, final String[] lastCvNameParts) {

        // if (firstCvNameParts != null && firstCvNameParts.length > 3) {
        if (fileListNameParts != null && fileListNameParts.size() > 0
                && fileListNameParts.get(0).length > 3) {

            final ImageMosaicGranulesDescriptor mosaicDescriptor = new ImageMosaicGranulesDescriptor(
                    storeID, coverageID, fileListNameParts);

            
            // TODO:
            // Temp workaround to leverages on a coverageStoreId having the
            // same name of the coverage
            // and the same name of the mosaic folder
            final String[] firstCvNameParts=fileListNameParts.get(0);
            final String[] lastCvNameParts=fileListNameParts.get(fileListNameParts.size()-1);
            if (firstCvNameParts.length >= 8 && firstCvNameParts.length == lastCvNameParts.length) {
                try {
                    final StringBuilder fields = new StringBuilder().append(firstCvNameParts[0])
                            .append("_").append(firstCvNameParts[1]).append("_")
                            .append(firstCvNameParts[2]).append("_");

                    double val = Double.parseDouble(firstCvNameParts[3]);
                    fields.append(val).append("_"); // Min Z
                    mosaicDescriptor.setMinZ(val);

                    val = Double.parseDouble(lastCvNameParts[3]);
                    fields.append(val).append("_"); // Max Z
                    mosaicDescriptor.setMaxZ(val);

                    fields.append(firstCvNameParts[5]).append("_"); // Base Time
                    mosaicDescriptor.setBaseTime(firstCvNameParts[5]);

                    fields.append(lastCvNameParts[6]).append("_"); // Forecast Time
                    mosaicDescriptor.setForecastTime(firstCvNameParts[6]);

                    if (firstCvNameParts.length == 9) {
                        int intVal = Integer.parseInt(firstCvNameParts[7]);
                        fields.append(intVal).append("_"); // TAU
                        mosaicDescriptor.setTAU(intVal);

                        val = Double.parseDouble(firstCvNameParts[8]);
                        fields.append(val); // NoDATA
                        mosaicDescriptor.setNoData(val);
                    } else { // firstCvNameParts.length == 8
                        val = Double.parseDouble(firstCvNameParts[7]);
                        fields.append(val); // NoDATA
                        mosaicDescriptor.setNoData(val);
                    }

                    mosaicDescriptor.setMetocFields(fields.toString());

                    return mosaicDescriptor;

                } catch (NumberFormatException nfe) {
                    // if the string does not contain a parsable float.
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Some of the name parts don't contain a parsable number: "
                                + nfe.getLocalizedMessage(), nfe);
                    }
                }
            }
        }
        return null;
    }

}