package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;

/**
 * 
 * @author afabiani
 * 
 * NOTE: may we want to use some other already defined classes ???
 * GranuleDescriptor....
 * 
 */
final class ImageMosaicGranulesDescriptor {
    private final static Logger LOGGER = Logger.getLogger(ImageMosaicGranulesDescriptor.class
            .toString());
    
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
    

    /**
     * Try to parse the input File name to build an Image Mosaic Descriptor
     * 
     * @param inputDir
     * @param mosaicDescriptor
     * @return
     */
    protected static ImageMosaicGranulesDescriptor buildDescriptor(File inputDir) {
        ImageMosaicGranulesDescriptor mosaicDescriptor = null;
//TODO a better file filter
        String[] fileNames = inputDir.list(new SuffixFileFilter(new String[]{ ".tif", ".tiff" },IOCase.INSENSITIVE));
        List<String> fileNameList = Arrays.asList(fileNames);
        Collections.sort(fileNameList);
        fileNames = fileNameList.toArray(new String[1]);

        // Store ID
        String coverageStoreId = inputDir.getName();

        //
        if (fileNames != null && fileNames.length > 0) {
            String[] firstCvNameParts = FilenameUtils.getBaseName(fileNames[0]).split("_");
            String[] lastCvNameParts = FilenameUtils.getBaseName(fileNames[fileNames.length - 1]).split("_");

            if (firstCvNameParts != null && firstCvNameParts.length > 3) {
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

                LOGGER.info("Coverage Store ID: " + coverageStoreId);

                mosaicDescriptor = new ImageMosaicGranulesDescriptor(coverageStoreId, metocFields,
                        firstCvNameParts, lastCvNameParts);
            }
            else {
                mosaicDescriptor = new ImageMosaicGranulesDescriptor(coverageStoreId,
                                                                    coverageStoreId,
                                                                    firstCvNameParts,
                                                                    lastCvNameParts);
            }
        }
        else {
  //TODO
            LOGGER.info("ERROR!"); 
        }
        return mosaicDescriptor;
    }

}