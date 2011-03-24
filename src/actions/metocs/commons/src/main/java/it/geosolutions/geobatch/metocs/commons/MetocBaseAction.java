/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
package it.geosolutions.geobatch.metocs.commons;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.metocs.jaxb.model.Metocs;
import it.geosolutions.geobatch.metocs.utils.io.METOCSActionsIOUtils;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions S.A.S.
 * 
 */
public abstract class MetocBaseAction extends BaseAction<FileSystemEvent> {
    private final static Logger LOGGER = Logger.getLogger(MetocBaseAction.class.toString());

    private final MetocActionConfiguration configuration;

    public MetocBaseAction(MetocActionConfiguration configuration) {
        super(configuration.getId(), configuration.getName(), configuration.getDescription());
        this.configuration = configuration;

        // //
        //
        // get required parameters
        //
        // //
        if ((configuration.getMetocDictionaryPath() == null)
                || "".equals(configuration.getMetocHarvesterXMLTemplatePath())) {
            LOGGER.log(Level.SEVERE,
                    "MetcoDictionaryPath || MetocHarvesterXMLTemplatePath is null.");
            throw new IllegalStateException(
                    "MetcoDictionaryPath || MetocHarvesterXMLTemplatePath is null.");
        }

        final String cruise = configuration.getCruiseName();
        if (cruise != null && cruise.trim().length() > 0) {
            cruiseName = cruise.trim();
        }
    }

    protected String cruiseName = "lscv08";

    protected Map<String, Variable> foundVariables = new HashMap<String, Variable>();

    protected Map<String, String> foundVariableLongNames = new HashMap<String, String>();

    protected Map<String, String> foundVariableBriefNames = new HashMap<String, String>();

    protected Map<String, String> foundVariableUoM = new HashMap<String, String>();

    protected GeneralEnvelope envelope = null;

    protected Metocs metocDictionary;

    /**
	 * 
	 */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("MetocBaseAction:execute(): Starting with processing...");
        try {
            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException(
                        "MetocBaseAction:execute(): Wrong number of elements for this action: "
                                + events.size());

            FileSystemEvent event = events.remove();
            @SuppressWarnings("unused")
            final String configId = getName();

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
                String message = "GeoServerDataDirectory is null or does not exist.";
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, message);
                throw new IllegalStateException(message);
            }

            // ... BUSINESS LOGIC ... //
            File inputFile = event.getSource();
            String inputFileName = inputFile.getAbsolutePath();
            final String fileSuffix = FilenameUtils.getExtension(inputFileName);
            final String fileNameFilter = configuration.getStoreFilePrefix();

            if (fileNameFilter != null) {
                if (!inputFile.getName().matches(fileNameFilter)) {
                    final String message = "MetocBaseAction:execute(): Unexpected file '"
                            + inputFileName + "'.\nThis action expects 'one' NetCDF file using \'"
                            + fileNameFilter + "\' as name filter (String.matches()).";
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }
            } else {
                if (!"nc".equalsIgnoreCase(fileSuffix) && !"netcdf".equalsIgnoreCase(fileSuffix)) {
                    final String message = "MetocBaseAction:execute(): Unexpected file '"
                            + inputFileName
                            + "'.\n"
                            + "This action expects 'one' NetCDF file using \'.nc\' or \'.netcdf\' extension.";
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }
            }

            final File outDir = Utilities.createTodayDirectory(workingDir,
                    FilenameUtils.getBaseName(inputFileName));

            if (inputFile.isFile() && inputFile.canRead()) {
                if (FilenameUtils.getExtension(inputFileName).equalsIgnoreCase("nc")
                        || FilenameUtils.getExtension(inputFileName).equalsIgnoreCase("netcdf")) {
                    
                }
                //
                File outputFile = writeDownNetCDF(outDir, inputFileName);
                // ... setting up the appropriate event for the next action
                events.add(new FileSystemEvent(outputFile, FileSystemEventType.FILE_ADDED));
            } else {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, "MetocBaseAction:execute(): "
                            + "the input file is not a non-directory file or it is not readable.");
            }

        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
        }
        
        return events;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Utility and conversion specific methods implementations...
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * @param lon_dim
     * @param lat_dim
     * @param lonOriginalData
     * @param latOriginalData
     * @throws IndexOutOfBoundsException
     */
    protected void buildEnvelope(final Dimension lon_dim, final Dimension lat_dim,
            final Array lonOriginalData, final Array latOriginalData)
            throws IndexOutOfBoundsException {
        double[] bbox = METOCSActionsIOUtils.computeExtrema(latOriginalData, lonOriginalData,
                lat_dim, lon_dim);

        // building Envelope
        envelope = new GeneralEnvelope(METOCSActionsIOUtils.WGS_84);
        envelope.setRange(0, bbox[0], bbox[2]);
        envelope.setRange(1, bbox[1], bbox[3]);
    }

    /**
     * @return
     * @throws JAXBException
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected void getMetocsDictionary() throws JAXBException, IOException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(Metocs.class);
        Unmarshaller um = context.createUnmarshaller();

        File metocDictionaryFile = Path.findLocation(configuration.getMetocDictionaryPath(),
                new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        metocDictionary = (Metocs) um.unmarshal(new FileReader(metocDictionaryFile));
    }
    

    /**
     * @throws IOException
     * @throws InvalidRangeException
     * @throws JAXBException
     * @throws ParseException
     * 
     */
    protected abstract File writeDownNetCDF(File outDir, String inputFileName) throws IOException,
            InvalidRangeException, ParseException, JAXBException;
    

}
