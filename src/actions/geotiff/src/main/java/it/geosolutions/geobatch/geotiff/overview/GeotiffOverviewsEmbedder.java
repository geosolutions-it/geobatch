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

package it.geosolutions.geobatch.geotiff.overview;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.tools.file.Collector;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.media.jai.JAI;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.geotools.utils.imageoverviews.OverviewsEmbedder;
import org.geotools.utils.progress.ExceptionEvent;
import org.geotools.utils.progress.ProcessingEvent;
import org.geotools.utils.progress.ProcessingEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to add overview to an input geotif image. NOTE: only one image is
 * available.
 * 
 * @author Simone Giannechini, GeoSolutions
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version $GeoTIFFOverviewsEmbedder.java Revision: 0.1 $ 23/mar/07 11:42:25
 *          Revision: 0.2 $ 15/Feb/11 13:00:00
 */
public class GeotiffOverviewsEmbedder extends BaseAction<FileSystemEvent> {

	private GeotiffOverviewsEmbedderConfiguration configuration;

	private final static Logger LOGGER = LoggerFactory
			.getLogger(GeotiffOverviewsEmbedder.class);

	public GeotiffOverviewsEmbedder(
			GeotiffOverviewsEmbedderConfiguration configuration)
			throws IOException {
		super(configuration);
		this.configuration = configuration;		
	}

	public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
			throws ActionException {

		try {
			// looking for file
			if (events.size() == 0)
				throw new IllegalArgumentException(
						"GeotiffOverviewsEmbedder::execute(): Wrong number of elements for this action: "
								+ events.size());

			listenerForwarder.setTask("config");
			listenerForwarder.started();

			// //
			//
			// data flow configuration and dataStore name must not be null.
			//
			// //
			if (configuration == null) {
				final String message = "GeotiffOverviewsEmbedder::execute(): DataFlowConfig is null.";
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				throw new IllegalStateException(message);
			}

			// //
			//
			// check the configuration and prepare the overviews embedder
			//
			// //
			final int downsampleStep = configuration.getDownsampleStep();
			if (downsampleStep <= 0)
				throw new IllegalArgumentException(
						"GeotiffOverviewsEmbedder::execute(): Illegal downsampleStep: "
								+ downsampleStep);

			int numberOfSteps = configuration.getNumSteps();
			if (numberOfSteps <= 0)
				throw new IllegalArgumentException(
						"GeotiffOverviewsEmbedder::execute(): Illegal numberOfSteps: "
								+ numberOfSteps);

			final OverviewsEmbedder oe = new OverviewsEmbedder();
			oe.setDownsampleStep(downsampleStep);
			oe.setNumSteps(configuration.getNumSteps());
			// SG: this way we are sure we use the standard tile cache
			oe.setTileCache(JAI.getDefaultInstance().getTileCache());

			oe.setScaleAlgorithm(configuration.getScaleAlgorithm());
			oe.setTileHeight(configuration.getTileH());
			oe.setTileWidth(configuration.getTileW());

			/*
			 * TODO check this is formally wrong! this should be done into the
			 * configuration.
			 */
			// add logger/listener
			if (configuration.isLogNotification())
				oe.addProcessingEventListener(new ProcessingEventListener() {

					public void exceptionOccurred(ExceptionEvent event) {
						if (LOGGER.isInfoEnabled())
							LOGGER.info("GeotiffOverviewsEmbedder::execute(): "
									+ event.getMessage());
					}

					public void getNotification(ProcessingEvent event) {
						if (LOGGER.isInfoEnabled())
							LOGGER.info("GeotiffOverviewsEmbedder::execute(): "
									+ event.getMessage());
						listenerForwarder.progressing(
								(float) event.getPercentage(),
								event.getMessage());
					}
				});

			// The return
			Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();

			while (events.size() > 0) {

				// run
				listenerForwarder.progressing(0, "Embedding overviews");

				final FileSystemEvent event = events.remove();

				final File eventFile = event.getSource();

				if (eventFile.exists() && eventFile.canRead()
						&& eventFile.canWrite()) {
					/*
					 * If here: we can start retiler actions on the incoming
					 * file event
					 */

					if (eventFile.isDirectory()) {

						final FileFilter filter = new RegexFileFilter(
								".+\\.[tT][iI][fF]([fF]?)");
						final Collector collector = new Collector(filter);
						final List<File> fileList = collector
								.collect(eventFile);
						int size = fileList.size();
						for (int progress = 0; progress < size; progress++) {

							final File inFile = fileList.get(progress);

							try {
								oe.setSourcePath(inFile.getAbsolutePath());
								oe.run();
							} catch (UnsupportedOperationException uoe) {
								listenerForwarder.failed(uoe);
								if (LOGGER.isWarnEnabled())
									LOGGER.warn(
											"GeotiffOverviewsEmbedder::execute(): "
													+ uoe.getLocalizedMessage(),
											uoe);
							} catch (IllegalArgumentException iae) {
								listenerForwarder.failed(iae);
								if (LOGGER.isWarnEnabled())
									LOGGER.warn(
											"GeotiffOverviewsEmbedder::execute(): "
													+ iae.getLocalizedMessage(),
											iae);
							} finally {
								listenerForwarder.setProgress((progress * 100)
										/ ((size != 0) ? size : 1));
								listenerForwarder.progressing();
							}
						}
					} else {
						// file is not a directory
						try {
							oe.setSourcePath(eventFile.getAbsolutePath());
							oe.run();
						} catch (UnsupportedOperationException uoe) {
							listenerForwarder.failed(uoe);
							if (LOGGER.isWarnEnabled())
								LOGGER.warn(
										"GeotiffOverviewsEmbedder::execute(): "
												+ uoe.getLocalizedMessage(),
										uoe);
						} catch (IllegalArgumentException iae) {
							listenerForwarder.failed(iae);
							if (LOGGER.isWarnEnabled())
								LOGGER.warn(
										"GeotiffOverviewsEmbedder::execute(): "
												+ iae.getLocalizedMessage(),
										iae);
						} finally {
							listenerForwarder
									.setProgress(100 / ((events.size() != 0) ? events
											.size() : 1));
						}
					}

					// add the directory to the return
					ret.add(event);
				} else {
					final String message = "GeotiffOverviewsEmbedder::execute(): The passed file event refers to a not existent "
							+ "or not readable/writeable file! File: "
							+ eventFile.getAbsolutePath();
					if (LOGGER.isWarnEnabled())
						LOGGER.warn(message);
					listenerForwarder.failed(new IllegalArgumentException(
							message));
				}
			} // endwile
			listenerForwarder.completed();

			// return
			if (ret.size() > 0) {
				events.clear();
				return ret;
			} else {
				/*
				 * If here: we got an error no file are set to be returned the
				 * input queue is returned
				 */
				return events;
			}
		} catch (Exception t) {
			final String message = "GeotiffOverviewsEmbedder::execute(): "
					+ t.getLocalizedMessage();
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message, t);
			final ActionException exc = new ActionException(this, message, t);
			listenerForwarder.failed(exc);
			throw exc;
		}
	}

	public ActionConfiguration getConfiguration() {
		return configuration;
	}

}
