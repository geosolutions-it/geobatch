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
package it.geosolutions.geobatch.actions.freemarker.merge;

import freemarker.template.Configuration;
import freemarker.template.Template;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.tools.file.MultiPropertyFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes a file in input, as a multiproperty file. The keys defined in the input file will be
 * substituted in the template file.
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class FreeMarkerMergeAction extends BaseAction<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(FreeMarkerMergeAction.class);

    FreeMarkerMergeConfiguration conf;

    public FreeMarkerMergeAction(FreeMarkerMergeConfiguration configuration) {
        super(configuration);
        conf = configuration;
    }

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {

        // get the input event
        FileSystemEvent event = events.poll();
        File inputFile = event.getSource();

        // parse the input file
        MultiPropertyFile multiPropertyFile = new MultiPropertyFile(inputFile);

        if (!multiPropertyFile.read()) {
            LOGGER.warn("Error reading input file");
            throw new ActionException(this, "Error reading input file");
        }

        Map<String, Object> values = new HashMap<String, Object>();
        values.putAll(conf.getDefaultValues()); // initial values
        values.putAll(multiPropertyFile.getRawMap()); // input values
        values.putAll(conf.getForcedValues()); // fixed overriding values

        File workingDir = new File(Path.getAbsolutePath(conf.getWorkingDirectory()));

        // build the output file
        File outputFile = new File(workingDir, conf.getOutputFile());
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Output file name: " + outputFile);

        // try to open the file to write into
        FileWriter fw = null;
        try {
            fw = new FileWriter(outputFile);
        } catch (IOException ioe) {
            final String message = "Unable to build the output file writer: "
                    + ioe.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this, message);
        }

        /*
         * If available, process the output file using the TemplateModel data structure
         */
        try {
            Configuration fmc = new Configuration();
            fmc.setDirectoryForTemplateLoading(workingDir);
            Template template = fmc.getTemplate(conf.getTemplateFile());
            template.process(values, fw);

            // flush the buffer
            if (fw != null)
                fw.flush();

        } catch (Exception e) {
            final String message = "Error in action: " + e.getLocalizedMessage();
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new ActionException(this, message);
        } finally {
            IOUtils.closeQuietly(fw);
        }

        // add the file to the queue
        events.add(new FileSystemEvent(outputFile.getAbsoluteFile(), FileSystemEventType.FILE_ADDED));
        return events;
    }
}
