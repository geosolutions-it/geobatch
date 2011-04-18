/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.metocs.remsens;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.GeoServerAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;


import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.Category;
import org.geotools.coverage.grid.GridCoverage2D;
//import org.geotools.coverage.io.CoverageAccess;
//import org.geotools.coverage.io.CoverageSource;
//import org.geotools.coverage.io.CoverageAccess.AccessType;
//import org.geotools.coverage.io.domain.RasterDatasetDomainManager.HorizontalDomain;
//import org.geotools.coverage.io.domain.RasterDatasetDomainManager.TemporalDomain;
//import org.geotools.coverage.io.driver.DefaultFileDriver;
//import org.geotools.coverage.io.driver.Driver.DriverOperation;
import org.geotools.coverage.io.hdf4.HDF4Driver;
//import org.geotools.coverage.io.impl.CoverageReadRequest;
//import org.geotools.coverage.io.impl.CoverageResponse;
//import org.geotools.coverage.io.impl.CoverageResponse.Status;
//import org.geotools.coverage.io.impl.range.DefaultRangeType;
//import org.geotools.coverage.io.range.FieldType;
//import org.geotools.coverage.io.range.RangeType;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.opengis.coverage.Coverage;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;

/**
 * 
 * Public class to split HDF4 files to GeoTIFFs and consequently send them to GeoServer along with
 * their basic metadata.
 */
public class HDF42GeoTIFFsFileConfiguratorAction extends
        GeoServerAction<FileSystemEvent> {

    /**
     * GeoTIFF Writer Default Params
     */
    public final static String GEOSERVER_VERSION = "2.x";

    private final CoordinateReferenceSystem WGS84;

    private final static Operations OPERATIONS = new Operations(new Hints(
            Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));

    private static final int DEFAULT_TILE_SIZE = 256;

    private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

    private static final String DEFAULT_COMPRESSION_TYPE = "LZW";

    protected HDF42GeoTIFFsFileConfiguratorAction(final GeoServerActionConfiguration configuration)
            throws IOException, NoSuchAuthorityCodeException, FactoryException {
        super(configuration);
        WGS84 = CRS.decode("EPSG:4326");
    }

    /**
     * EXECUTE METHOD
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
            throws ActionException {

        if (LOGGER.isInfoEnabled())
            LOGGER.info("Starting with processing...");

        throw new ActionException(this,"NOT IMPLEMENTED");
    }
    /*
        try {
            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            FileSystemEvent event = events.remove();
            final String configId = configuration.getName();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                LOGGER.error("DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            final File workingDir = Path.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if ((workingDir == null) || !workingDir.exists() || !workingDir.isDirectory()) {
                LOGGER.error("WorkingDirectory is null or does not exist.");
                throw new IllegalStateException("WorkingDirectory is null or does not exist.");
            }

            // ... BUSINESS LOGIC ... //
            String inputFileName = event.getSource().getAbsolutePath();
            final String filePrefix = FilenameUtils.getBaseName(inputFileName);
            final String fileSuffix = FilenameUtils.getExtension(inputFileName);
            final String fileNameFilter = getConfiguration().getStoreFilePrefix();

            String baseFileName = null;

            if (fileNameFilter != null) {
                if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
                        && ("hdf4".equalsIgnoreCase(fileSuffix) || "hdf"
                                .equalsIgnoreCase(fileSuffix))) {
                    // etj: are we missing something here?
                    baseFileName = filePrefix;
                }
            } else if ("hdf4".equalsIgnoreCase(fileSuffix) || "hdf".equalsIgnoreCase(fileSuffix)) {
                baseFileName = filePrefix;
            }

            if (baseFileName == null) {
                LOGGER.error("Unexpected file '" + inputFileName + "'");
                throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
            }

            String baseName = FilenameUtils.getBaseName(inputFileName);
            String baseTime = null;
            String endTime = null;

            final DefaultFileDriver driver = new HDF4Driver();
            final File file = new File(inputFileName);
            final URL source = file.toURI().toURL();
            if (driver.canProcess(DriverOperation.CONNECT, source, null)) {

                // getting access to the file
                final CoverageAccess access = driver.process(DriverOperation.CONNECT, source, null,
                        null, null);
                if (access == null)
                    throw new IOException("Unable to connect");

                // get the names
                final List<Name> names = access.getNames(null);
                for (Name name : names) {
                    // get a source
                    final CoverageSource gridSource = access.access(name, null,
                            AccessType.READ_ONLY, null, null);
                    if (gridSource == null)
                        throw new IOException("Unable to access");

                    // TEMPORAL DOMAIN
                    final TemporalDomain temporalDomain = gridSource.getDomainManager(null)
                            .getTemporalDomain();
                    if (temporalDomain == null)
                        throw new IllegalStateException("Temporal domain is null");

                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                    // get the temporal domain elements
                    for (TemporalGeometricPrimitive tg : temporalDomain.getTemporalElements(null)) {
                        baseTime = sdf.format(((Period) tg).getBeginning().getPosition().getDate());
                        endTime = sdf.format(((Period) tg).getEnding().getPosition().getDate());
                    }

                    // HORIZONTAL DOMAIN
                    final HorizontalDomain horizontalDomain = gridSource.getDomainManager(null)
                            .getHorizontalDomain();
                    if (horizontalDomain == null)
                        throw new IllegalStateException("Horizontal domain is null");

                    // RANGE TYPE_KEY
                    final RangeType range = gridSource.getRangeType(null);

                    final CoverageReadRequest readRequest = new CoverageReadRequest();
                    // //
                    //
                    // Setting up a limited range for the request.
                    //
                    // //
                    Iterator<FieldType> ftIterator = range.getFieldTypes().iterator();

                    while (ftIterator.hasNext()) {
                        HashSet<FieldType> fieldSet = new HashSet<FieldType>();
                        FieldType ft = null;

                        ft = ftIterator.next();
                        if (ft != null) {
                            fieldSet.add(ft);
                        }
                        RangeType rangeSubset = new DefaultRangeType(range.getName(), range
                                .getDescription(), fieldSet);
                        readRequest.setRangeSubset(rangeSubset);
                        CoverageResponse response = gridSource.read(readRequest, null);
                        if (response == null || response.getStatus() != Status.SUCCESS
                                || !response.getExceptions().isEmpty())
                            throw new IOException("Unable to read");

                        final Collection<? extends Coverage> results = response.getResults(null);
                        for (Coverage c : results) {
                            GridCoverage2D coverage = (GridCoverage2D) c;
                            final File outDir = Utilities.createTodayDirectory(workingDir,
                                    FilenameUtils.getBaseName(inputFileName));

                            // Storing fields as GeoTIFFs

                            final List<Category> categories = coverage.getSampleDimension(0)
                                    .getCategories();
                            double noData = Double.NaN;
                            for (Category cat : categories) {
                                if (cat.getName().toString().equalsIgnoreCase("no data")) {
                                    noData = cat.getRange().getMinimum();
                                    break;
                                }
                            }

                            final String uom = getUom(ft);
                            final String varLongName = ft.getDescription().toString();
                            final String varBrief = getBrief(ft.getName().getLocalPart().toString());
                            final String coverageName = buildCoverageName(baseName, ft, endTime,
                                    noData);
                            final String coverageStoreId = coverageName.toString();
                            final GridCoverage2D resampledCoverage = (GridCoverage2D) OPERATIONS
                                    .resample(coverage, WGS84);

                            final File gtiffOutputDir = new File(outDir.getAbsolutePath()
                                    + File.separator + FilenameUtils.getBaseName(inputFileName)
                                    + "_" + varBrief.replaceAll("_", "") + "_T"
                                    + new Date().getTime());

                            LOGGER.info(gtiffOutputDir.getAbsolutePath());
                            boolean canProceed = false;
                            if (!gtiffOutputDir.exists())
                                canProceed = gtiffOutputDir.mkdirs();

                            canProceed = gtiffOutputDir.isDirectory();

                            if (canProceed) {
                                final File gtiffFile = Utilities.storeCoverageAsGeoTIFF(
                                        gtiffOutputDir, coverageName.toString(), resampledCoverage,
                                        DEFAULT_COMPRESSION_TYPE, DEFAULT_COMPRESSION_RATIO,
                                        DEFAULT_TILE_SIZE);

                                // ... setting up the appropriate event for the
                                // next action
                                events.add(new FileSystemEvent(gtiffOutputDir,
                                        FileSystemEventType.FILE_ADDED));
                            }
                        }
                    }
                }
            } else
                LOGGER.info("NOT ACCEPTED");

            return events;
        } catch (Throwable t) {
            LOGGER.error(t.getLocalizedMessage(), t);
            return null;
        } finally {
            JAI.getDefaultInstance().getTileCache().flush();
        }
    }
    private final static String getUom(final FieldType ft) {
        final String uom = ft.getUnitOfMeasure().toString();
        if (ft != null)
            if (ft.getName().getLocalPart().toString().contains("sst"))
                return "cel";
            else if (ft.getName().getLocalPart().toString().contains("lowcloud"))
                return "dimensionless";
        return uom;
    }

    private final static String getBrief(final String string) {
        if (string != null && string.trim().length() > 0) {
            if (string.equalsIgnoreCase("mcsst"))
                return "sst";
        }
        return string;
    }

    private String buildCoverageName(final String baseName, final FieldType ft,
            final String referenceTime, double noData) {
        final String varName = ft.getName().getLocalPart().toString();
        String description = ft.getDescription().toString();

        String source = "";
        String system = "";

        if (varName.equalsIgnoreCase("mcsst") || varName.equalsIgnoreCase("lowcloud")) {
            if (varName.equalsIgnoreCase("mcsst"))
                description = getBrief("mcsst");
            source = "terascan";
            system = "NOAA-AVHRR";
        }
        // ////
        // producing the Coverage name ...
        // ////
        final StringBuilder coverageName = new StringBuilder(source).append("_").append(system)
                .append("_").append(description.replaceAll(" ", "")).append("_").append("0000.000")
                .append("_").append("0000.000").append("_").append(referenceTime).append("_")
                .append(referenceTime).append("_").append(0).append("_").append(noData);

        return coverageName.toString();

    }
*/
}
