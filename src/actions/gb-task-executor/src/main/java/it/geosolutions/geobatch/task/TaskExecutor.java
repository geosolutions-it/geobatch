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

package it.geosolutions.geobatch.task;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Action to execute tasks such as invoking python scripts, gdal utilities and similar command
 * lines.
 * 
 * @author Daniele Romagnoli, GeoSolutions S.a.S.
 */
public class TaskExecutor extends BaseAction<FileSystemEvent> implements
        Action<FileSystemEvent> {

    private final static Logger LOGGER = Logger.getLogger(TaskExecutor.class.toString());

    private final static String SOURCE_TAG_OPEN = "<source>";

    private final static String SOURCE_TAG_CLOSE = "</source>";

    private final static String DESTINATION_TAG_OPEN = "<destination>";

    private final static String DESTINATION_TAG_CLOSE = "</destination>";

    private TaskExecutorConfiguration configuration;

    public TaskExecutor(final TaskExecutorConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
            throws ActionException {

        listenerForwarder.started();

        if (configuration == null) {
            throw new IllegalStateException("DataFlowConfig is null.");
        }

        Queue<FileSystemEvent> outEvents = new LinkedList<FileSystemEvent>();

        while (events.size() > 0) {
            // get the first event
            final FileSystemEvent event = events.remove();
            final File inputFile = event.getSource();
            if (inputFile == null) {
                throw new IllegalArgumentException("Input File is null");
            }
            if (!inputFile.exists()) {
                throw new IllegalArgumentException("Input File doesn't exist");
            }
            final String inputFilePath = inputFile.getAbsolutePath();

            final String inputFileExt = FilenameUtils.getExtension(inputFilePath);

            // Getting XSL file definition
            final String xslPath = configuration.getXsl();
            final boolean useDefaultScript;

            String defaultScriptPath = configuration.getDefaultScript();
            if (inputFileExt.equalsIgnoreCase("xml")) {
                defaultScriptPath = inputFilePath;
                useDefaultScript = false;
            } else {
                useDefaultScript = true;
            }

            final String outputName = configuration.getOutputName();

            File xslFile = null;
            InputStream is = null;

            try {

                if (xslPath != null && xslPath.trim().length() > 0) {
                    xslFile = Path.findLocation(xslPath, new File(
                            ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
                }
                if (xslFile == null || !xslFile.exists())
                    throw new IllegalArgumentException("The specified XSL file hasn't been found: "
                            + xslPath);

                File xmlFile = null;
                String outputFile = null;
                if (useDefaultScript) {
                    if (defaultScriptPath != null && defaultScriptPath.trim().length() > 0) {
                        xmlFile = Path.findLocation(defaultScriptPath, new File(
                                ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
                        final File outXmlFile = File.createTempFile("script", ".xml");
                        outXmlFile.deleteOnExit();
                        outputFile = setScriptArguments(xmlFile.getAbsolutePath(), inputFilePath,
                                outputName, outXmlFile);
                        xmlFile = outXmlFile;
                    }

                } else {
                    xmlFile = inputFile;
                }

                // Setup an XML source from the input XML file
                final Source xmlSource = new StreamSource(xmlFile);

                is = new FileInputStream(xslFile);

                // XML parsing to setup a command line
                final String argument = buildArgument(xmlSource, is);

                final Project project = new Project();
                project.init();

                final ExecTask execTask = new ExecTask();
                execTask.setProject(project);

                // Setting environment variables coming from the configuration
                // as an instance: PATH, LD_LIBRARY_PATH and similar
                Map<String, String> variables = configuration.getVariables();
                if (variables != null && !variables.isEmpty()) {
                    for (String key : variables.keySet()) {
                        Variable var = new Variable();
                        var.setKey(key);
                        final String value = variables.get(key);
                        if (value != null) {
                            var.setValue(variables.get(key));
                            execTask.addEnv(var);
                        }
                    }
                }

                // Setting executable
                execTask.setExecutable(configuration.getExecutable());

                // Setting Error logging
                final String errorPath = configuration.getErrorFile();
                if (errorPath != null && errorPath.trim().length() > 0) {
                    File errorFile = Path.findLocation(errorPath, new File(
                            ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
                    if (errorFile != null) {
                        if (!errorFile.exists()) {
                            try {
                                errorFile.createNewFile();
                            } catch (Throwable t) {
                                if (LOGGER.isLoggable(Level.WARNING))
                                    LOGGER.warning(new StringBuilder(
                                            "The specified errorFile doesn't exist.").append(
                                            " Unable to create it due to:").append(
                                            t.getLocalizedMessage()).toString());
                            }
                        }
                        if (errorFile.exists()) {
                            execTask.setLogError(true);
                            execTask.setError(errorFile);
                            execTask.setFailonerror(true);
                        }
                    }
                }

                // Setting the timeout
                Long timeOut = configuration.getTimeOut();
                if (timeOut != null) {
                    execTask.setTimeout(timeOut);
                }

                // Setting command line argument
                execTask.createArg().setLine(argument);

                File output = null;
                if (configuration.getOutput() != null) {
                    output = new File(configuration.getOutput());
                    if (output.exists() && output.isDirectory()) {
                        final File outXmlFile = File.createTempFile("script", ".xml");
                        outXmlFile.deleteOnExit();
                        String destFile = getScriptArguments(xmlFile.getAbsolutePath(), "srcfile");
                        if (output.isAbsolute()) {
                            output = new File(output, FilenameUtils.getBaseName(destFile)
                                    + configuration.getOutputName().substring(
                                            configuration.getOutputName().indexOf(".")));
                        } else {
                            output = Path.findLocation(configuration.getOutput(), inputFile
                                    .getParentFile());
                            output = new File(output, FilenameUtils
                                    .getBaseName(inputFile.getName())
                                    + configuration.getOutputName().substring(
                                            configuration.getOutputName().indexOf(".")));
                        }
                    }
                    execTask.setOutput(output);
                }

                // Executing
                execTask.execute();

                File outFile = (outputFile != null ? new File(outputFile) : null);
                if (configuration.getOutput() != null) {
                    if (new File(configuration.getOutput()).isAbsolute()) {
                        if (output.exists() && output.isFile()) {
                            // outFile = output;
                            final File outXmlFile = File.createTempFile("script", ".xml");
                            outXmlFile.deleteOnExit();
                            outputFile = setScriptArguments(xmlFile.getAbsolutePath(), output
                                    .getAbsolutePath(), outputName, outXmlFile);
                            outFile = new File(configuration.getOutput(), FilenameUtils
                                    .getBaseName(outputFile)
                                    + ".xml");
                            FileUtils.copyFile(outXmlFile, outFile);
                        }
                    } else {
                        if (outFile == null)
                            outFile = inputFile;
                    }
                } else if (outFile == null) {
                    outFile = inputFile;
                }

                outEvents.add(new FileSystemEvent(outFile,
                        FileSystemEventType.FILE_ADDED));
            } catch (Throwable e) {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(e.getLocalizedMessage());

                listenerForwarder.failed(e);

            } finally {
                if (is != null)
                    org.apache.commons.io.IOUtils.closeQuietly(is);
            }
        }

        listenerForwarder.completed();
        return outEvents;
    }

    private String buildArgument(final Source xmlSource, final InputStream is)
            throws TransformerException {
        // XML parsing to setup a command line
        final TransformerFactory f = TransformerFactory.newInstance();
        final StringWriter result = new StringWriter();
        final Templates transformation = f.newTemplates(new StreamSource(is));
        final Transformer transformer = transformation.newTransformer();
        transformer.transform(xmlSource, new StreamResult(result));
        final String argument = result.toString();
        return argument;
    }

    private String setScriptArguments(final String defaultScriptPath, final String inputFilePath,
            String outputName, final File outXmlFile) throws IOException {
        String destFilePath = null;
        boolean overwriteOutput = false;
        if (outputName != null && outputName.trim().length() > 0) {
            overwriteOutput = true;
            if (outputName.startsWith("*.")) {
                final String outputExt = outputName.substring(2, outputName.length());
                destFilePath = new StringBuilder(FilenameUtils.getFullPath(inputFilePath)).append(
                        File.separator).append(FilenameUtils.getBaseName(inputFilePath))
                        .append(".").append(outputExt).toString();
            } else {
                destFilePath = new StringBuilder(FilenameUtils.getFullPath(inputFilePath)).append(
                        File.separator).append(FilenameUtils.getBaseName(inputFilePath))
                        .append(".").append(outputName).toString();
            }
        }

        // Create FileReader Object
        FileReader inputFileReader = new FileReader(defaultScriptPath);
        FileWriter outputFileWriter = new FileWriter(outXmlFile);

        try {

            // Create Buffered/PrintWriter Objects
            BufferedReader inputStream = new BufferedReader(inputFileReader);
            PrintWriter outputStream = new PrintWriter(outputFileWriter);

            String inLine = null;
            boolean sourcePresent = false;
            boolean destinationPresent = false;

            while ((inLine = inputStream.readLine()) != null) {
                // Handle KeyWords

                if (inLine.trim().startsWith(SOURCE_TAG_OPEN)) {
                    if (inLine.trim().endsWith(SOURCE_TAG_CLOSE)) {
                        // source file specified on the same line
                        inLine = new StringBuilder(SOURCE_TAG_OPEN).append(inputFilePath).append(
                                SOURCE_TAG_CLOSE).toString();
                    } else {
                        while ((inLine = inputStream.readLine()) != null) {
                            if (inLine.trim().endsWith(SOURCE_TAG_CLOSE)) {
                                // source file specified on different lines
                                inLine = new StringBuilder(SOURCE_TAG_OPEN).append(inputFilePath)
                                        .append(SOURCE_TAG_CLOSE).toString();
                            }
                        }
                    }
                    sourcePresent = true;
                }

                if (inLine.trim().startsWith(DESTINATION_TAG_OPEN)) {
                    if (inLine.trim().endsWith(DESTINATION_TAG_CLOSE)) {
                        // source file specified on the same line
                        if (overwriteOutput) {
                            inLine = new StringBuilder(DESTINATION_TAG_OPEN).append(destFilePath)
                                    .append(DESTINATION_TAG_CLOSE).toString();
                        } else {
                            final int start = inLine.indexOf(DESTINATION_TAG_OPEN);
                            final int end = inLine.indexOf(DESTINATION_TAG_CLOSE, start + 1);
                            destFilePath = inLine.substring(start + DESTINATION_TAG_OPEN.length(),
                                    end);
                        }

                    } else {
                        while ((inLine = inputStream.readLine()) != null) {
                            if (overwriteOutput) {
                                if (inLine.trim().endsWith(DESTINATION_TAG_CLOSE)) {
                                    // source file specified on different lines
                                    inLine = new StringBuilder(DESTINATION_TAG_OPEN).append(
                                            destFilePath).append(DESTINATION_TAG_CLOSE).toString();
                                }
                            } else {
                                String newLine = inLine.trim();
                                if (newLine.trim().endsWith(DESTINATION_TAG_CLOSE)) {
                                    // source file specified on different lines
                                    if (!newLine.trim().startsWith(DESTINATION_TAG_CLOSE)) {
                                        destFilePath = newLine.substring(0, newLine
                                                .indexOf(DESTINATION_TAG_CLOSE));
                                    }
                                } else {
                                    if (newLine.length() > 0) {
                                        destFilePath = newLine;
                                    }
                                }
                            }
                        }
                    }
                    destinationPresent = true;
                }

                outputStream.println(inLine);
            }

            if (sourcePresent && !destinationPresent) {
                destFilePath = inputFilePath;
            }

        } catch (IOException e) {
        } finally {
            inputFileReader.close();
            outputFileWriter.close();
        }

        return destFilePath;
    }

    /**
     * 
     * @param defaultScriptPath
     * @param tagName
     * @return
     * @throws IOException
     */
    private static String getScriptArguments(final String defaultScriptPath, final String tagName)
            throws IOException {
        String value = null;

        // Create FileReader Object
        FileReader inputFileReader = new FileReader(defaultScriptPath);

        try {
            // Create Buffered/PrintWriter Objects
            BufferedReader inputStream = new BufferedReader(inputFileReader);

            String inLine = null;

            while ((inLine = inputStream.readLine()) != null) {
                // Handle KeyWords

                if (inLine.trim().startsWith("<" + tagName + ">")) {
                    if (inLine.trim().endsWith("</" + tagName + ">")) {
                        int beginIndex = inLine.indexOf("<" + tagName + ">")
                                + ("<" + tagName + ">").length();
                        int endIndex = inLine.length() - ("</" + tagName + ">").length();
                        value = inLine.substring(beginIndex, endIndex);
                    } else {
                        while ((inLine = inputStream.readLine()) != null) {
                            if (!inLine.trim().endsWith("</" + tagName + ">"))
                                value = inLine;
                            else
                                break;
                        }
                    }
                }
            }

        } catch (IOException e) {
        } finally {
            inputFileReader.close();
        }

        return value;
    }
}
