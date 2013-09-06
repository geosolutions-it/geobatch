/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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

import it.geosolutions.geobatch.imagemosaic.config.DomainAttribute;
import it.geosolutions.geobatch.imagemosaic.config.DomainAttribute.TYPE;
import it.geosolutions.geobatch.imagemosaic.utils.ConfigUtil;
import it.geosolutions.tools.commons.file.Path;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataUtilities;
import org.geotools.gce.imagemosaic.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ImageMosaicProperties {

    private static final String CACHING_KEY = "Caching";

    private static final String TIME_DEFAULT_ATTRIBUTE = "ingestion";
    private static final String ELEV_DEFAULT_ATTRIBUTE = "elevation";

    /**
     * Default logger
     */
    protected final static Logger LOGGER = LoggerFactory.getLogger(ImageMosaicProperties.class);


    private static class IndexerHelper {
        String type;
        String extractor;

        public IndexerHelper(String type, String extractor) {
            this.type = type;
            this.extractor = extractor;
        }
    }

    private static final Map<DomainAttribute.TYPE, IndexerHelper> indexerMap;
    static {
        Map<DomainAttribute.TYPE, IndexerHelper> tmp = new HashMap<DomainAttribute.TYPE, IndexerHelper>();
        tmp.put(DomainAttribute.TYPE.DATE, new IndexerHelper("java.util.Date", "TimestampFileNameExtractorSPI"));
        tmp.put(DomainAttribute.TYPE.DOUBLE, new IndexerHelper("Double", "DoubleFileNameExtractorSPI"));
        tmp.put(DomainAttribute.TYPE.INTEGER, new IndexerHelper("Integer", "IntegerFileNameExtractorSPI"));
        tmp.put(DomainAttribute.TYPE.STRING, new IndexerHelper("String", "StringFileNameExtractorSPI"));
        indexerMap = Collections.unmodifiableMap(tmp);
    }

    /**
     * get the properties object from the file.
     * 
     * @param properties
     *            the file referring to the prop file to load
     * @return
     * @throws NullPointerException TODO
     * @throws IOException 
     */
    protected static Properties getPropertyFile(File properties) throws NullPointerException, IOException {
        URL url = DataUtilities.fileToURL(properties);
        Properties props = null;
        if (url != null) {
            props = Utils.loadPropertiesFromURL(url);
        } else {
            throw new NullPointerException("Unable to resolve the URL: "
                    + properties.getAbsolutePath());
        }

        return props;
    }

    /**
     * If the regex file do not exists, build it using the passed configuration and return the
     * corresponding properties object
     * 
     * @param regexFile
     * @param configuration
     * @return
     * @throws NullPointerException
     * @throws IOException 
     */
    private static Properties createRegexFile(File regexFile, String regex) throws NullPointerException, IOException {

        if (!regexFile.exists()) {
            FileWriter outFile = null;
            PrintWriter out = null;
            if (regex != null) {
                try {
                    outFile = new FileWriter(regexFile);
                    out = new PrintWriter(outFile);

                    // Write text to file
                    out.println("regex=" + regex);
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("Error occurred while writing " + regexFile.getAbsolutePath()
                                + " file!", e);
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }

                    outFile = null;
                    out = null;
                }
            } else
                throw new NullPointerException(
                        "Unable to build the property file using a null regex string");

            return getPropertyFile(regexFile);
        }
        return null;
    }

    /**
     * If the indexer file do not exists, build the indexer using the passed configuration and
     * return the corresponding properties object
     * 
     * @note: here we suppose that elevation are stored as double
     * @note: for a list of available SPI refer to:<br>
     *        geotools/trunk/modules/plugin/imagemosaic/src/main/resources/META-INF/services/org.
     *        geotools.gce.imagemosaic.properties.PropertiesCollectorSPI
     * 
     * @param indexer
     * @param configuration
     * @return 
     * @throws NullPointerException 
     * @throws IOException 
     */
    protected static Properties buildIndexer(File indexer, ImageMosaicConfiguration configuration) throws NullPointerException, IOException {
        // ////
        // INDEXER
        // ////
        if (!indexer.exists()) {

            FileWriter outFile = null;
            PrintWriter out = null;
            try {
                indexer.createNewFile();

                if (!indexer.canWrite()) {
                    final String message = "Unable to write on indexer.properties file at URL: "
                            + indexer.getAbsolutePath();
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error(message);
                    throw new IOException(message);
                }

                outFile = new FileWriter(indexer);
                out = new PrintWriter(outFile);

                File baseDir = indexer.getParentFile();

                // Write text to file
                // setting caching of file to false

                // create a private copy and fix inconsistencies in it
                configuration = configuration.clone();
                ConfigUtil.sanitize(configuration);
                
                // create regex files
                for (DomainAttribute domainAttr : configuration.getDomainAttributes()) {
                    final File regexFile = new File(baseDir, domainAttr.getAttribName() +"regex.properties");
                    ImageMosaicProperties.createRegexFile(regexFile, domainAttr.getRegEx());

                    if(domainAttr.getEndRangeAttribName() != null) {
                        final File endRangeRegexFile = new File(baseDir, domainAttr.getEndRangeAttribName() +"regex.properties");
                        ImageMosaicProperties.createRegexFile(endRangeRegexFile, domainAttr.getEndRangeRegEx());
                    }
                }

                StringBuilder indexerSB = createIndexer(configuration);
                out.append(indexerSB);

//                out.println(org.geotools.gce.imagemosaic.Utils.Prop.CACHING+"=false");
//
//                // create indexer
//                DomainAttribute timeAttr = ConfigUtil.getTimeAttribute(configuration);
//                DomainAttribute elevAttr = ConfigUtil.getElevationAttribute(configuration);
//
//                if(timeAttr != null) {
//                    out.println(org.geotools.gce.imagemosaic.Utils.Prop.TIME_ATTRIBUTE+"="+getAttribDeclaration(timeAttr));
//                }
//                if(elevAttr != null) {
//                    out.println(org.geotools.gce.imagemosaic.Utils.Prop.ELEVATION_ATTRIBUTE+"="+getAttribDeclaration(elevAttr));
//                }
//
//                List<DomainAttribute> customAttribs = ConfigUtil.getCustomDimensions(configuration);
//                if( ! customAttribs.isEmpty() ) {
//                    out.println("AdditionalDomainAttributes");
//                    String sep="=";
//                    for (DomainAttribute customAttr : customAttribs) {
//                        out.print(sep);
//                        sep=",";
//                        out.print(getDimensionDeclaration(customAttr));
//                    }
//                    out.println();
//                }
//
//                out.print("Schema=*the_geom:Polygon,location:String");
//                for (DomainAttribute attr : configuration.getDomainAttributes()) {
//                    TYPE type = attr.getType();
//                    printSchemaField(out, attr.getAttribName(), type);
//                    if(attr.getEndRangeAttribName() != null)
//                        printSchemaField(out, attr.getEndRangeAttribName(), type);
//                }
//                out.println();
//
//                String sep="";
//                out.print("PropertyCollectors=");
//                for (DomainAttribute attr : configuration.getDomainAttributes()) {
//                    TYPE type = attr.getType();
//                    out.print(sep);
//                    sep=";";
//                    printCollectorField(out, attr.getAttribName(), type);
//                    if(attr.getEndRangeAttribName() != null)
//                        printCollectorField(out, attr.getEndRangeAttribName(), type);
//                }
//                out.println();

            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(
                            "Error occurred while writing indexer.properties file at URL: "
                                    + indexer.getAbsolutePath(), e);
                return null;
            } finally {
                if (out != null) {
                    out.flush();
                    IOUtils.closeQuietly(out);
                }
                out = null;
                if (outFile != null) {
                    IOUtils.closeQuietly(outFile);
                }
                outFile = null;
            }
            return getPropertyFile(indexer);
        } else {
            // file -> indexer.properties
            /**
             * get the Caching property and set it to false
             */
            Properties indexerProps = getPropertyFile(indexer);
            String caching = indexerProps.getProperty(CACHING_KEY);
            if (caching != null) {
                if (caching.equals("true")) {
                    indexerProps.setProperty(CACHING_KEY, "false");
                }
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Unable to get the "
                            + CACHING_KEY + " property into the " + indexer.getAbsolutePath()
                            + " file.");
            }

            return indexerProps;
        }
    }

    public static StringBuilder createIndexer(ImageMosaicConfiguration configuration) {

        StringBuilder sb = new StringBuilder();

        sb.append(org.geotools.gce.imagemosaic.Utils.Prop.CACHING).append("=false").append("\n");

        // create indexer
        DomainAttribute timeAttr = ConfigUtil.getTimeAttribute(configuration);
        DomainAttribute elevAttr = ConfigUtil.getElevationAttribute(configuration);

        if(timeAttr != null) {
            sb.append(org.geotools.gce.imagemosaic.Utils.Prop.TIME_ATTRIBUTE)
                    .append("=")
                    .append(getAttribDeclaration(timeAttr)).append("\n");
        }
        if(elevAttr != null) {
            sb.append(org.geotools.gce.imagemosaic.Utils.Prop.ELEVATION_ATTRIBUTE)
                    .append("=")
                    .append(getAttribDeclaration(elevAttr)).append("\n");
        }

        List<DomainAttribute> customAttribs = ConfigUtil.getCustomDimensions(configuration);
        if( ! customAttribs.isEmpty() ) {
            sb.append("AdditionalDomainAttributes");
            String sep="=";
            for (DomainAttribute customAttr : customAttribs) {
                sb.append(sep);
                sep=",";
                sb.append(getDimensionDeclaration(customAttr));
            }
            sb.append("\n");
        }

        sb.append("Schema=*the_geom:Polygon,location:String");
        for (DomainAttribute attr : configuration.getDomainAttributes()) {
            TYPE type = attr.getType();
            System.out.println("Adding to schema" + attr);
            appendSchemaField(sb, attr.getAttribName(), type);
            if(attr.getEndRangeAttribName() != null)
                appendSchemaField(sb, attr.getEndRangeAttribName(), type);
        }
        sb.append("\n");

        String sep="";
        sb.append("PropertyCollectors=");
        for (DomainAttribute attr : configuration.getDomainAttributes()) {
            TYPE type = attr.getType();
            sb.append(sep);
            sep=",";
            appendCollectorField(sb, attr.getAttribName(), type);
            if(attr.getEndRangeAttribName() != null) {
                sb.append(sep);
                appendCollectorField(sb, attr.getEndRangeAttribName(), type);
            }
        }
        sb.append("\n");

        return sb;
    }

    protected static void appendSchemaField(StringBuilder sb, String attrName, TYPE type) {
        sb.append(",");
        sb.append(attrName);
        sb.append(":");
        sb.append(indexerMap.get(type).type);
    }

    protected static void appendCollectorField(StringBuilder sb, String attrName, TYPE type) {
        sb.append(indexerMap.get(type).extractor);
        sb.append('[').append(attrName).append("regex]");
        sb.append('(').append(attrName).append(')');
    }

    private static String getAttribDeclaration(DomainAttribute attr) {
        if(attr.getEndRangeAttribName() == null)
            return attr.getAttribName();
        else
            return attr.getAttribName() + ";" + attr.getEndRangeAttribName();
    }

    private static String getDimensionDeclaration(DomainAttribute attr) {
        if(attr.getEndRangeAttribName() == null)
            return attr.getAttribName();
        else
            return attr.getDimensionName()+ "(" + attr.getAttribName() + ";" + attr.getEndRangeAttribName() + ")";
    }



    /**
     * CHECKING FOR datastore.properties If the 'datastore.properties' do not exists into the
     * baseDir, try to use the configured one. If not found a shape file will be used (done by the
     * geoserver).
     * 
     * @param baseDir
     *            the directory of the layer
     * @return File (unchecked) datastore.properties if succes or null if some error occurred.
     */
    protected static File checkDataStore(ImageMosaicConfiguration configuration, File configDir, File baseDir) {
        final File datastore = new File(baseDir, "datastore.properties");
        if (datastore.exists()) {
            return datastore;
        }
        
        if (configuration.getDatastorePropertiesPath() == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("DataStoreProperties file not configured "
                        + "nor found into destination dir. A shape file will be used.");
            }
            return null;
        }
        
        final File dsFile = Path.findLocation(configuration.getDatastorePropertiesPath(),configDir);
        if (dsFile == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unable to get the absolute path of the datastore properties file "
                        + "(file: "+configuration.getDatastorePropertiesPath()+") "
                        + "(cfgdir: "+configDir+")");
            }
        } else {
            if (!dsFile.isDirectory()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Configuration DataStore file found: '"
                            + dsFile.getAbsolutePath() + "'.");
                }
                try {
                    FileUtils.copyFileToDirectory(dsFile, baseDir);
                    return new File(baseDir,dsFile.getName());
                } catch (IOException e) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn(e.getMessage(),e);
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("DataStoreProperties file points to a directory! "
                            + dsFile.getAbsolutePath() + "'. Skipping event");
                }
            }
        }
        return null;
    }

}
