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
package it.geosolutions.geobatch.actions.freemarker;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.tools.adapter.EventAdapter;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.tools.freemarker.filter.FreeMarkerFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action can be used to filter a data structure of type DATA_IN which must
 * be supported by FreeMarker (see its documentation)
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @param <DATA_CONF>
 */
public class FreeMarkerAction extends BaseAction<EventObject> implements
		EventAdapter<TemplateModelEvent> {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(FreeMarkerAction.class);

	/**
	 * configuration
	 */
	private final FreeMarkerConfiguration conf;

	// the filter
	final FreeMarkerFilter filter;

	public FreeMarkerAction(FreeMarkerConfiguration configuration) {
		super(configuration);
		conf = configuration;
		filter = new FreeMarkerFilter(Path.getAbsolutePath(conf
				.getWorkingDirectory()), conf.getInput());
	}

	/**
	 * Removes TemplateModelEvents from the queue and put
	 */
	public Queue<EventObject> execute(Queue<EventObject> events)
			throws ActionException {
		listenerForwarder.started();
		listenerForwarder.setTask("build the output absolute file name");
		// build the output absolute file name
		File outputDir;
		if (conf.getOutput() != null) {
			final File out = new File(conf.getOutput());
			if (out.isAbsolute()) {
				if (!out.exists()) {
					if (!out.mkdirs()) {
						final String message = "FreeMarkerAction.execute(): Unable create the output dir : "
								+ out.getAbsolutePath();
						if (LOGGER.isErrorEnabled())
							LOGGER.error(message);
						throw new ActionException(this, message);
					}
				}
				outputDir=out;
			} else {
				try {

					// the output
					outputDir = it.geosolutions.tools.commons.file.Path.findLocation(
							conf.getOutput(),
							new File(conf.getWorkingDirectory()));
					if (LOGGER.isInfoEnabled())
						LOGGER.info("Output directory name: "
								+ outputDir.toString());

				} catch (NullPointerException npe) {
					outputDir = new File(conf.getWorkingDirectory(),
							conf.getOutput());
					// failed to absolutize conf.getOutput()
					if (!outputDir.exists()) {
						if (!outputDir.mkdirs()) {
							final String message = "Unable to build the output dir path from : "
									+ conf.getOutput();
							if (LOGGER.isErrorEnabled())
								LOGGER.error(message);

							final ActionException e = new ActionException(this,
									message);
							listenerForwarder.failed(e);
							throw e;

						}
					} else if (!outputDir.canWrite()) {
						final String message = "Output dir is not writeable : "
								+ conf.getOutput();
						if (LOGGER.isErrorEnabled())
							LOGGER.error(message);
						listenerForwarder.failed(npe);
						throw new ActionException(this,
								npe.getLocalizedMessage());
					}
				}
			}
		} else {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("FreeMarkerAction.execute(): Output dir : "
						+ conf.getWorkingDirectory());
			outputDir = new File(conf.getWorkingDirectory());
		}

		// return
		final Queue<EventObject> ret = new LinkedList<EventObject>();

		listenerForwarder.setTask("Building/getting the root data structure");
		/*
		 * Building/getting the root data structure
		 */
		final Map<String, Object> root;
		if (conf.getRoot() != null)
			root = conf.getRoot();
		else
			root = new HashMap<String, Object>();

		// list of incoming event to inject into the root datamodel
		final List<TemplateModel> list;
		if (conf.isNtoN()) {
			list = new ArrayList<TemplateModel>(events.size());
		} else {
			list = new ArrayList<TemplateModel>(1);
		}
		// append the list of adapted event objects
		root.put(TemplateModelEvent.EVENT_KEY, list);

		while (events.size() > 0) {
			// the adapted event
			final TemplateModelEvent ev;
			final TemplateModel dataModel;
			try {
				if ((ev = adapter(events.remove())) != null) {
					listenerForwarder
							.setTask("Try to get a Template DataModel from the Adapted event");
					// try to get a Template DataModel from the Adapted event
					dataModel = ev.getModel(filter);

				} else {
					final String message = "Unable to append the event: unrecognized format. SKIPPING...";
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(message);
					}
					if (conf.isFailIgnored()) {
						continue;
					} else {
						final ActionException e = new ActionException(this,
								message);
						listenerForwarder.failed(e);
						throw e;
					}
				}
			} catch (TemplateModelException tme) {
				final String message = "FreeMarkerAction.execute(): Unable to wrap the passed object: "
						+ tme.getLocalizedMessage();
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				if (conf.isFailIgnored()) {
					continue;
				} else {
					listenerForwarder.failed(tme);
					throw new ActionException(this, tme.getLocalizedMessage());
				}
			} catch (Exception ioe) {
				final String message = "FreeMarkerAction.execute(): Unable to produce the output: "
						+ ioe.getLocalizedMessage();
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				if (conf.isFailIgnored()) {
					continue;
				} else {
					listenerForwarder.failed(ioe);
					throw new ActionException(this, ioe.getLocalizedMessage());
				}
			}

			listenerForwarder.setTask("Generating the output");
			/*
			 * If getNtoN: For each data incoming event (Template DataModel)
			 * build a file. Otherwise the entire queue of incoming object will
			 * be transformed in a list of datamodel. In this case only one file
			 * is generated.
			 */
			if (conf.isNtoN()) {

				if (list.size() > 0) {
					list.remove(0);
				}
				list.add(dataModel);

				final File outputFile;
				// append the incoming data structure
				try {
					outputFile = buildOutput(outputDir, root);
				} catch (ActionException e) {
					if (LOGGER.isErrorEnabled())
						LOGGER.error(e.getLocalizedMessage(), e);
					if (conf.isFailIgnored()) {
						continue;
					} else {
						listenerForwarder.failed(e);
						throw e;
					}
				}
				// add the file to the return
				ret.add(new FileSystemEvent(outputFile.getAbsoluteFile(),
						FileSystemEventType.FILE_ADDED));
			} else {
				list.add(dataModel);
			}
		}

		if (!conf.isNtoN()) {
			final File outputFile;
			// append the incoming data structure
			try {
				outputFile = buildOutput(outputDir, root);
			} catch (ActionException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error(e.getLocalizedMessage(), e);
				listenerForwarder.failed(e);
				throw e;
			}
			// add the file to the return
			ret.add(new FileSystemEvent(outputFile.getAbsoluteFile(),
					FileSystemEventType.FILE_ADDED));
		}

		listenerForwarder.completed();
		return ret;
	}

	private final File buildOutput(final File outputDir,
			final Map<String, Object> root) throws ActionException {
		// try to open the file to write into
		final FileWriter fw;
		final File outputFile;
		try {
			final String outputFilePrefix = FilenameUtils.getBaseName(conf
					.getInput()) + "_";
			final String outputFileSuffix = "."
					+ FilenameUtils.getExtension(conf.getInput());
			outputFile = File.createTempFile(outputFilePrefix,
					outputFileSuffix, outputDir);
			if (LOGGER.isInfoEnabled())
				LOGGER.info("FreeMarkerAction.buildOutput(): Output file name: "
						+ outputFile.toString());
			fw = new FileWriter(outputFile);
		} catch (IOException ioe) {
			final String message = "FreeMarkerAction.buildOutput(): Unable to build the output file writer: "
					+ ioe.getLocalizedMessage();
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message);
			throw new ActionException(this, message);
		}

		/*
		 * If available, process the output file using the TemplateModel data
		 * structure
		 */
		try {
			// process the input template file
			filter.process(filter.wrapRoot(root), fw);

			// flush the buffer
			if (fw != null)
				fw.flush();

		} catch (IOException ioe) {
			final String message = "FreeMarkerAction.execute(): Unable to flush buffer to the output file: "
					+ ioe.getLocalizedMessage();
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message, ioe);
			throw new ActionException(this, message);
		} catch (TemplateModelException tme) {
			final String message = "FreeMarkerAction.execute(): Unable to wrap the passed object: "
					+ tme.getLocalizedMessage();
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message, tme);
			throw new ActionException(this, message);
		} catch (Exception e) {
			final String message = "FreeMarkerAction.execute(): Unable to process the input file: "
					+ e.getLocalizedMessage();
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message, e);
			throw new ActionException(this, message);
		} finally {
			IOUtils.closeQuietly(fw);
		}
		return outputFile;
	}

	/**
	 * Used as key into the map for the incoming event. ${event[X].PARENT}
	 * 
	 * To use it into a template you have to use:<br>
	 * ${event[0].PARENT} -> first file into the queue<br>
	 * ${event[N-1].PARENT} -> (N)th file into the queue<br>
	 * 
	 * To compose the entire file name:<br>
	 * ${event[N].PARENT}/${event[N].FILENAME}.${event[N].EXTENSION}
	 */
	private static final String FILE_EVENT_PARENTFILE_KEY = "PARENT";

	private static final String FILE_EVENT_NAMEFILE_KEY = "FILENAME";

	private static final String FILE_EVENT_EXTENSION_KEY = "EXTENSION";

	/**
	 * Act as a Gateway interface (EIP):<br>
	 * Try to adapt the effective input EventObject to the expected input a
	 * TemplateDataModel
	 * 
	 * @param ieo
	 *            The Event Object to test or to transform
	 * @return Adapted data model or null if event cannot be adapted
	 */
	public TemplateModelEvent adapter(EventObject ieo) throws ActionException {
		if (ieo instanceof TemplateModelEvent) {
			return (TemplateModelEvent) ieo;
		} else if (ieo instanceof FileSystemEvent) {
			Map<String, Object> map = new HashMap<String, Object>();

			final File file = ((FileSystemEvent) ieo).getSource()
					.getAbsoluteFile();

			map.put(FILE_EVENT_PARENTFILE_KEY, file.getParent());
			map.put(FILE_EVENT_NAMEFILE_KEY,
					FilenameUtils.getBaseName(file.getName()));
			map.put(FILE_EVENT_EXTENSION_KEY,
					FilenameUtils.getExtension(file.getName()));

			return new TemplateModelEvent(map);
		} else {
			try {
				return new TemplateModelEvent(filter.wrapRoot(ieo.getSource()));
			} catch (NullPointerException npe) {
				// NullPointerException - if tm is null
				if (LOGGER.isErrorEnabled())
					LOGGER.error("FreeMarkerAction.adapter(): The passed event object is null");
			} catch (TemplateModelException tme) {
				// TemplateModelException - if defined objectWrapper can't wrap
				// the passed object
				if (LOGGER.isErrorEnabled())
					LOGGER.error("FreeMarkerAction.adapter(): Default wrapper can't wrap the passed object");
			}
		}
		return null;
	}

}
