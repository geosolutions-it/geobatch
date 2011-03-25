package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.tools.file.Collector;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;

/**
 * 
 * @author afabiani
 * 
 *         NOTE: may we want to use some other already defined classes ??? GranuleDescriptor....
 * 
 */
final class ImageMosaicGranulesDescriptor {
    private final static Logger LOGGER = Logger.getLogger(ImageMosaicGranulesDescriptor.class
            .toString());

    // TODO a better file filter
    private final static Collector coll = new Collector(new SuffixFileFilter(new String[] { ".tif",
            ".tiff" }, IOCase.INSENSITIVE));

    private String coverageStoreId = null;

    private String metocFields = null;

    private String[] firstCvNameParts = null;

    private String[] lastCvNameParts = null;

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
    private ImageMosaicGranulesDescriptor(String coverageStoreId, String metocFields,
            String[] firstCvNameParts, String[] lastCvNameParts) {
        this.coverageStoreId = coverageStoreId;
        this.metocFields = metocFields;
        this.firstCvNameParts = firstCvNameParts;
        this.lastCvNameParts = lastCvNameParts;
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

    public void setMetocFields(String metocFields) {
        this.metocFields = metocFields;
    }

    public void setFirstCvNameParts(String[] firstCvNameParts) {
        this.firstCvNameParts = firstCvNameParts;
    }

    public void setLastCvNameParts(String[] lastCvNameParts) {
        this.lastCvNameParts = lastCvNameParts;
    }

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
    public String[] getFirstCvNameParts() {
        return firstCvNameParts;
    }

    /**
     * @return the lastCvNameParts
     */
    public String[] getLastCvNameParts() {
        return lastCvNameParts;
    }

    protected static ImageMosaicGranulesDescriptor buildDescriptor(ImageMosaicCommand cmd,
            ImageMosaicConfiguration config) {
        File inputDir = cmd.getBaseDir();

        List<File> fileNameList = coll.collect(inputDir);

        if (cmd.getAddFiles() != null) {
            for (File file : cmd.getAddFiles()) {
                if (!fileNameList.contains(file)) {
                    fileNameList.add(file);
                }
            }
        }
        if (cmd.getDelFiles() != null) {
            for (File file : cmd.getDelFiles()) {
                if (!fileNameList.contains(file)) {
                    fileNameList.remove(file);
                }
            }
        }

        if (fileNameList == null) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("ImageMosaicGranulesDescriptor:buildDescriptor(): unable to collect files from the dir: "
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
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("ImageMosaicGranulesDescriptor:buildDescriptor(): unable to collect files from the dir: "
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

        ImageMosaicGranulesDescriptor mosaicDescriptor = null;

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

            final File firstFile = fileNameList.get(0);
            final String[] firstCvNameParts = FilenameUtils.getBaseName(firstFile.getName()).split(
                    "_");

            final File lastFile = fileNameList.get(fileNameList.size() - 1);
            final String[] lastCvNameParts = FilenameUtils.getBaseName(lastFile.getName()).split(
                    "_");

            if (config.isCOARDS()) {
                mosaicDescriptor = buildCOARDS(coverageStoreId, firstCvNameParts, lastCvNameParts);
                if (mosaicDescriptor != null) {
                    return mosaicDescriptor;
                } else {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(
                                Level.WARNING,
                                "ImageMosaicGranulesDescriptor:buildDescriptor(): Unable to use COARDS naming convention for file: "
                                        + firstFile.getAbsolutePath()
                                        + "\nLet's use default configuration parameters.");
                    }
                }
            }
            return new ImageMosaicGranulesDescriptor(coverageStoreId, coverageStoreId,
                    firstCvNameParts, lastCvNameParts);

        } else {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning("ImageMosaicGranulesDescriptor:buildDescriptor(): The passed base dir is empty! Dir:"
                        + inputDir.getAbsolutePath());
            }

            return new ImageMosaicGranulesDescriptor(coverageStoreId, coverageStoreId, null, null);
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
    private static ImageMosaicGranulesDescriptor buildCOARDS(final String coverageID,
            final String[] firstCvNameParts, final String[] lastCvNameParts) {
        ImageMosaicGranulesDescriptor mosaicDescriptor = null;
        if (firstCvNameParts != null && firstCvNameParts.length > 3) {

            mosaicDescriptor = new ImageMosaicGranulesDescriptor();
            mosaicDescriptor.setCoverageStoreId(coverageID);
            mosaicDescriptor.setFirstCvNameParts(firstCvNameParts);
            mosaicDescriptor.setLastCvNameParts(lastCvNameParts);

            // TODO:
            // Temp workaround to leverages on a coverageStoreId having the
            // same name of the coverage
            // and the same name of the mosaic folder
            if (firstCvNameParts.length >= 8 && firstCvNameParts.length == lastCvNameParts.length) {
                try {
                    StringBuilder fields = new StringBuilder().append(firstCvNameParts[0])
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
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "ImageMosaicGranulesDescriptor:buildCOARDS(): "
                                + "\nSome of the name parts don't contain a parsable number: "
                                + nfe.getLocalizedMessage(), nfe);
                    }
                }
            }
        }
        return null;
    }

}