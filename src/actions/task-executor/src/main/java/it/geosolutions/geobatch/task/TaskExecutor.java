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

package it.geosolutions.geobatch.task;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.tools.commons.file.Path;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to execute tasks such as invoking python scripts, gdal utilities and
 * similar command lines.
 * 
 * @author Daniele Romagnoli, GeoSolutions S.a.S.
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
@it.geosolutions.geobatch.annotations.Action(configurationClass=TaskExecutorConfiguration.class)
public class TaskExecutor extends BaseAction<FileSystemEvent> implements
		Action<FileSystemEvent> {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(TaskExecutor.class);

	private final static String SOURCE_TAG_OPEN = "<source>";

	private final static String SOURCE_TAG_CLOSE = "</source>";

	private final static String DESTINATION_TAG_OPEN = "<destination>";

	private final static String DESTINATION_TAG_CLOSE = "</destination>";

	private TaskExecutorConfiguration configuration;

	public TaskExecutor(final TaskExecutorConfiguration configuration)
			throws IOException {
		super(configuration);
		this.configuration = configuration;
	}

	@Override
	@CheckConfiguration
	public boolean checkConfiguration() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
			throws ActionException {

		listenerForwarder.started();

		if (configuration == null) {
			final ActionException e = new ActionException(this,
					"DataFlowConfig is null.");
			listenerForwarder.failed(e);
			throw e;
		}

		if (events == null || events.size() == 0) {
			final ActionException e = new ActionException(this,
					"Empty or null incoming events list");
			listenerForwarder.failed(e);
			throw e;
		}

		Queue<FileSystemEvent> outEvents = new LinkedList<FileSystemEvent>();

		while (events.size() > 0) {
			// get the first event
			final FileSystemEvent event = events.remove();
			final File inputFile = event.getSource();
			if (inputFile == null) {
				final ActionException e = new ActionException(this, "Input File is null");
				listenerForwarder.failed(e);
				throw e;
			}
			if (!inputFile.exists()) {
				final ActionException e = new ActionException(this, "Input File doesn't exist");
				listenerForwarder.failed(e);
				throw e;
			}
			final String inputFilePath = inputFile.getAbsolutePath();

			final String inputFileExt = FilenameUtils.getExtension(inputFilePath);

			// Getting XSL file definition
			final String xslPath = configuration.getXsl();
			final boolean useDefaultScript;

			String defaultScriptPath = configuration.getDefaultScript();
			if (inputFileExt.equalsIgnoreCase("xml")) {
				if (LOGGER.isInfoEnabled())
					LOGGER.info("Using input file as script: " + inputFilePath);
				defaultScriptPath = inputFilePath;
				useDefaultScript = false;
			} else {
				if (LOGGER.isInfoEnabled())
					LOGGER.info("Using default script: " + configuration.getDefaultScript());
				useDefaultScript = true;
			}

			final String outputName = configuration.getOutputName();

			File xslFile = null;
			InputStream is = null;

			try {

				if (xslPath != null && xslPath.trim().length() > 0) {
					final String path=Path.findLocation(xslPath,getConfigDir().getAbsolutePath());
					if (path==null){
						final ActionException e = new ActionException(this, "XSL file not found: " + path);
						listenerForwarder.failed(e);
						throw e;
					}
					xslFile = new File(path);
				}
				if (!xslFile.exists()) {
					final ActionException e = new ActionException(this, "XSL file not found: " + xslPath);
					listenerForwarder.failed(e);
					throw e;
				}

				File xmlFile = null;
				String outputFile = null;
				if (useDefaultScript) {
					if (defaultScriptPath != null && defaultScriptPath.trim().length() > 0) {
						final String path = Path.findLocation(xslPath, getConfigDir().getAbsolutePath());
						if (path == null){
							final ActionException e = new ActionException(this, "XSL file not found: " + path);
							listenerForwarder.failed(e);
							throw e;
						}
						xmlFile = new File(path);
						
						final File outXmlFile = File.createTempFile("script",".xml", getTempDir());
//						outXmlFile.deleteOnExit();
						outputFile = setScriptArguments(
								xmlFile.getAbsolutePath(), inputFilePath,
								outputName, outXmlFile);
						xmlFile = outXmlFile;
					}

				} else {
					xmlFile = inputFile;
				}
				if (!xmlFile.exists()) {
					final ActionException e = new ActionException(this, "XML file not found: " + xmlFile);
					listenerForwarder.failed(e);
					throw e;
				}

				// Setup an XML source from the input XML file
				final Source xmlSource = new StreamSource(xmlFile);

				is = new FileInputStream(xslFile);

				// XML parsing to setup a command line
				final String argument = buildArgument(xmlSource, is);
				if (LOGGER.isDebugEnabled()){
					LOGGER.debug("Arguments: "+argument);
				}
				
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
				final String errorFileName = configuration.getErrorFile();
				if (errorFileName != null ){
					File errorFile = new File(errorFileName);
					if (!errorFile.exists()) {
						errorFile = Path.findLocation(errorFileName, getTempDir());
						if (errorFile != null && !errorFile.exists()) {
							try {
								errorFile.createNewFile();
							} catch (Throwable t) {
								final ActionException e = new ActionException(
										this, t.getLocalizedMessage(), t);
								listenerForwarder.failed(e);
								throw e;
							}
						}
					}
					if (errorFile.exists()) {
	                    if(LOGGER.isDebugEnabled())
	                        LOGGER.debug("Using error file: " + errorFile);
						execTask.setLogError(true);
						execTask.setAppend(true);
						execTask.setError(errorFile);
						execTask.setFailonerror(true);
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
						final File outXmlFile = File.createTempFile("script",".xml",getTempDir()); // TODO CHECKME: is this var used?
//						outXmlFile.deleteOnExit();
						String destFile = getScriptArguments(xmlFile.getAbsolutePath(), "srcfile");
						if (output.isAbsolute()) {
//                            String basename = 
							output = new File(output,
                                                FilenameUtils.getBaseName(destFile)
                                                    + configuration.getOutputName().substring(configuration.getOutputName().indexOf(".")));
						} else {
							output = Path.findLocation(
									configuration.getOutput(),
									inputFile.getParentFile());
							output = new File(
									output,
									FilenameUtils.getBaseName(inputFile
											.getName())
											+ configuration
													.getOutputName()
													.substring(
															configuration
																	.getOutputName()
																	.indexOf(
																			".")));
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
							final File outXmlFile = File.createTempFile("script", ".xml", getTempDir());
//							outXmlFile.deleteOnExit();
							outputFile = setScriptArguments(
									xmlFile.getAbsolutePath(),
									output.getAbsolutePath(),
                                    outputName,
									outXmlFile);
							outFile = new File(configuration.getOutput(),
									FilenameUtils.getBaseName(outputFile)+ ".xml");
							FileUtils.copyFile(outXmlFile, outFile);
						}
					} else {
						if (outFile == null)
							outFile = inputFile;
					}
				} else if (outFile == null) {
					outFile = inputFile;
				}

				outEvents.add(new FileSystemEvent(outFile, FileSystemEventType.FILE_ADDED));
			} catch (Exception e) {
				listenerForwarder.failed(e);
				throw new ActionException(this, e.getMessage(), e);
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
		final String argument = result.toString().replace("\n", " ");
		return argument;
	}

	private String setScriptArguments(final String defaultScriptPath,
			final String inputFilePath, String outputName, final File outXmlFile)
			throws IOException {
		String destFilePath = null;
		boolean overwriteOutput = false;
		if (outputName != null && outputName.trim().length() > 0) {
			overwriteOutput = true;
			if (outputName.startsWith("*.")) {
				final String outputExt = outputName.substring(2,outputName.length());
				destFilePath = new StringBuilder(
						FilenameUtils.getFullPath(inputFilePath))
						.append(File.separator)
						.append(FilenameUtils.getBaseName(inputFilePath))
						.append(".").append(outputExt).toString();
			} else {
				destFilePath = new StringBuilder(
						FilenameUtils.getFullPath(inputFilePath))
						.append(File.separator)
						.append(FilenameUtils.getBaseName(inputFilePath))
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
						inLine = new StringBuilder(SOURCE_TAG_OPEN)
								.append(inputFilePath).append(SOURCE_TAG_CLOSE)
								.toString();
					} else {
						while ((inLine = inputStream.readLine()) != null) {
							if (inLine.trim().endsWith(SOURCE_TAG_CLOSE)) {
								// source file specified on different lines
								inLine = new StringBuilder(SOURCE_TAG_OPEN)
										.append(inputFilePath)
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
							inLine = new StringBuilder(DESTINATION_TAG_OPEN)
									.append(destFilePath)
									.append(DESTINATION_TAG_CLOSE).toString();
						} else {
							final int start = inLine
									.indexOf(DESTINATION_TAG_OPEN);
							final int end = inLine.indexOf(
									DESTINATION_TAG_CLOSE, start + 1);
							destFilePath = inLine.substring(start
									+ DESTINATION_TAG_OPEN.length(), end);
						}

					} else {
						while ((inLine = inputStream.readLine()) != null) {
							if (overwriteOutput) {
								if (inLine.trim().endsWith(DESTINATION_TAG_CLOSE)) {
									// source file specified on different lines
									inLine = new StringBuilder(DESTINATION_TAG_OPEN)
											.append(destFilePath)
											.append(DESTINATION_TAG_CLOSE)
											.toString();
								}
							} else {
								String newLine = inLine.trim();
								if (newLine.trim().endsWith(DESTINATION_TAG_CLOSE)) {
									// source file specified on different lines
									if (!newLine.trim().startsWith(DESTINATION_TAG_CLOSE)) {
										destFilePath = newLine.substring(0,newLine.indexOf(DESTINATION_TAG_CLOSE));
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
			try {
				inputFileReader.close();
			} catch (IOException e) {

			}
			try {
				outputFileWriter.close();
			} catch (IOException e) {

			}
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
	private static String getScriptArguments(final String defaultScriptPath,
			final String tagName) throws IOException {
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
						int endIndex = inLine.length()
								- ("</" + tagName + ">").length();
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
