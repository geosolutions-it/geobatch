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

    protected static ImageMosaicGranulesDescriptor buildDescriptor(ImageMosaicCommand cmd) {
        File inputDir = cmd.getBaseDir();

        List<File> fileNameList = coll.collect(inputDir);
        if (cmd.getAddFiles() != null) {
            fileNameList.addAll(cmd.getAddFiles());
        }
        if (cmd.getDelFiles() != null) {
            fileNameList.removeAll(cmd.getDelFiles());
        }

        if (fileNameList == null) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("ImageMosaicGranulesDescriptor:buildDescriptor(): unable to collect files from the dir: "
                        + inputDir.getAbsolutePath());
            }
            return null;
        }

        return buildDescriptor(inputDir, fileNameList);
    }

    /**
     * Try to parse the input File name to build an Image Mosaic Descriptor
     * 
     * @param inputDir
     * @param mosaicDescriptor
     * @return
     */
    protected static ImageMosaicGranulesDescriptor buildDescriptor(File inputDir) {

        List<File> fileNameList = null;

        fileNameList = coll.collect(inputDir);


        if (fileNameList == null) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("ImageMosaicGranulesDescriptor:buildDescriptor(): unable to collect files from the dir: "
                        + inputDir.getAbsolutePath());
            }
            return null;
        }

        return buildDescriptor(inputDir, fileNameList);
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
            List<File> fileNameList) {

        ImageMosaicGranulesDescriptor mosaicDescriptor = null;

        // to get it ordered from the first to the last file (by Name)
        Collections.sort(fileNameList, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        // fileNames = fileNameList.toArray(new String[1]);

        // Store ID
        String coverageStoreId = inputDir.getName();
        //
        if (fileNameList.size() > 0) {

            String[] firstCvNameParts = FilenameUtils.getBaseName(fileNameList.get(0).getName())
                    .split("_");
            String[] lastCvNameParts = FilenameUtils.getBaseName(
                    fileNameList.get(fileNameList.size() - 1).getName()).split("_");

            if (firstCvNameParts != null && firstCvNameParts.length > 3) {
                // TODO:
                // Temp workaround to leverages on a coverageStoreId having the
                // same name of the coverage
                // and the same name of the mosaic folder
                String metocFields = firstCvNameParts.length == 9
                        && firstCvNameParts.length == lastCvNameParts.length ? new StringBuilder()
                        .append(firstCvNameParts[0]).append("_").append(firstCvNameParts[1])
                        .append("_").append(firstCvNameParts[2]).append("_")
                        .append(firstCvNameParts[3]).append("_") // Min Z
                        .append(lastCvNameParts[3]).append("_") // Max Z
                        .append(firstCvNameParts[5]).append("_") // Base Time
                        .append(lastCvNameParts[6]).append("_") // Forecast Time
                        .append(firstCvNameParts[7]).append("_") // TAU
                        .append(firstCvNameParts[8]) // NoDATA
                        .toString() : inputDir.getName();

                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("ImageMosaicGranulesDescriptor:buildDescriptor(): Coverage Store ID: "
                            + coverageStoreId);
                }

                mosaicDescriptor = new ImageMosaicGranulesDescriptor(coverageStoreId, metocFields,
                        firstCvNameParts, lastCvNameParts);
            } else {
                mosaicDescriptor = new ImageMosaicGranulesDescriptor(coverageStoreId,
                        coverageStoreId, firstCvNameParts, lastCvNameParts);
            }
        } else {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning("ImageMosaicGranulesDescriptor:buildDescriptor(): The passed base dir is empty! Dir:"
                        + inputDir.getAbsolutePath());
            }

            mosaicDescriptor = new ImageMosaicGranulesDescriptor(coverageStoreId, coverageStoreId,
                    null, null);
        }
        return mosaicDescriptor;
    }

}