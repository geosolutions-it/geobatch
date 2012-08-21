package it.geosolutions.geobatch.octave.actions;

import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.octave.OctaveConfiguration;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveManager;
import it.geosolutions.geobatch.octave.SheetPreprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.EventObject;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import dk.ange.octave.exception.OctaveEvalException;

/**
 * 
 * @author carlo cancellieri
 * 
 * @param <T>
 */
public abstract class OctaveAction<T extends EventObject> extends BaseAction<T>
		implements Action<T> {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(OctaveAction.class.toString());

	protected final SheetPreprocessor preprocessor = new SheetPreprocessor();

	protected final OctaveActionConfiguration config;

	public OctaveAction(OctaveActionConfiguration actionConfiguration) {
		super(actionConfiguration);
		config = actionConfiguration;
	}

	/**
	 * This method should add all the needed preprocessors to the preprocessors
	 * map modifying as needed the event queue.
	 * 
	 * @param events
	 * @return events
	 */
	public abstract Queue<T> load(Queue<T> events,
			OctaveEnv<OctaveExecutableSheet> env) throws ActionException;

	/**
	 * Action to execute on the FileSystemEvent event queue.
	 * 
	 * @param Queue
	 *            <FileSystemEvent> queue of events to handle in this (and next)
	 *            action executions.
	 * @return Queue<FileSystemEvent> the resulting list of events
	 */
	public Queue<T> execute(Queue<T> events) throws ActionException {

        if( config.getEmbeddedEnv() != null && config.getEnv() != null) {
            throw new ActionException(this, "Bad configuration: either <octave> or <env> should be specified, not both");
        }

        OctaveEnv<OctaveExecutableSheet> env;

        if(config.getEmbeddedEnv() != null)
            env = config.getEmbeddedEnv().clone();
        else if (config.getEnv() != null)
            env = loadEnv(config.getEnv());
        else
            throw new ActionException(this, "Bad configuration: either <octave> or <env> should be specified.");

        if (LOGGER.isInfoEnabled())
            LOGGER.info("Executing Octave script...");

        try {
			if (events != null) {
				/**
				 * here all the events are processed and preprocess map is
				 * build.
				 */
				events = load(events, env);

				/*
				 * try to preprocess the OctaveFunctionSheet this operation
				 * should transform all the OctaveFunction stored into the env
				 * into OctaveExecutableSheet which can be freely executed by
				 * the Octave Engine.class
				 * 
				 * @note each sheet is executed atomically so be careful with
				 * the 'cd' command (which change context dir) or other commands
				 * like so.
				 */
				try {
					preprocessor.preprocess(env);
				} catch (Exception e) {
					String message = "Exception during buildFunction:\n"
							+ e.getLocalizedMessage();
					if (LOGGER.isErrorEnabled())
						LOGGER.error(message);
					throw new ActionException(this, message, e);
				}

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Passing Octave sheet to Octave process... ");
				// TODO set number of the Thread pool or use the Catalog thread
				// pool
				ExecutorService es = Executors
						.newFixedThreadPool(OctaveConfiguration
								.getExecutionQueueSize());

				// pass to the octave manager a new environment to process
				OctaveManager.process(env, es);

			} // ev==null
			else {
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("Resulting a null event queue");
				throw new ActionException(this, "Resulting a null event queue");
			}

			if (LOGGER.isInfoEnabled())
				LOGGER.info("Evaluating: DONE");

		} catch (OctaveEvalException oee) {
			throw new ActionException(this, "Unable to run octave script:\n"
					+ oee.getLocalizedMessage(), oee);
		} catch (Exception e) {
			throw new ActionException(this, "Unable to run octave script:\n"
					+ e.getLocalizedMessage(), e);
		}
		return events;
	}

    private OctaveEnv<OctaveExecutableSheet> loadEnv(String filename) throws ActionException {
        File in_file = null;
        try {
            // !!!!!!!!!!!!!!!!1
            in_file = new File(filename); // TODO checkme
        } catch (NullPointerException npe) {
            // NullPointerException - If the pathname argument is null
            String message = "NullPointerException: You have to set the execution string in the config file. "
                    + npe.getLocalizedMessage();
//            if (LOGGER.isErrorEnabled()) {
//                LOGGER.error(message);
//            }
            throw new ActionException(this, message);

        }
        FileReader env_reader = null;
        try {
            env_reader = new FileReader(in_file);
        } catch (FileNotFoundException fnfe) {
            /*
             * FileNotFoundException - if the file does not exist, is a
             * directory rather than a regular file, or for some other
             * reason cannot be opened for reading.
             */
            String message = "Unable to find the OctaveEnv file: "
                    + fnfe.getMessage();
//            if (LOGGER.isErrorEnabled()) {
//                LOGGER.error(message);
//            }
            throw new ActionException(this, message);
        }
        OctaveEnv<OctaveExecutableSheet> env = null;
        Object o = null;

        XStream stream = null;
        try {
            // unmarshall the environment to the env
            stream = new XStream();
            stream.processAnnotations(OctaveEnv.class);
        } catch (InitializationException ie) {
            // InitializationException - in case of an initialization
            // problem
            String message = "InitializationException: Could not initialize the XStream object.\n"
                    + ie.getLocalizedMessage();
//            if (LOGGER.isErrorEnabled())
//                LOGGER.error(message);
            throw new ActionException(this, message, ie);
        }

        try {
            if (env_reader != null && stream != null) {
                o = stream.fromXML(env_reader);
                if (o instanceof OctaveEnv<?>) {
                    env = (OctaveEnv<OctaveExecutableSheet>) o;
                    return env;
                } else {
                    String message = "ClassCastException: Serialized object is not an OctaveEnv object";
//                    if (LOGGER.isErrorEnabled()) {
//                        LOGGER.error(message);
//                    }
                    throw new ActionException(this, message);
                }
            } else {
                String message = "Exception during execute: stream object:"
                        + stream + " env_reader:" + env_reader;
//                if (LOGGER.isErrorEnabled()) {
//                    LOGGER.error(message);
//                }
                throw new ActionException(this, message);
            }
        } catch (XStreamException xse) {
            // XStreamException - if the object cannot be deserialized
            String message = "XStreamException: Serialized object is not an OctaveEnv object:\n"
                    + xse.getLocalizedMessage();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message);
            }
            throw new ActionException(this, message, xse);
        } catch (ClassCastException cce) {
            // ClassCastException - if the execute string do not point
            // to a OctaveEnv serialized object
            String message = "ClassCastException: Serialized object is not an OctaveEnv object:\n"
                    + cce.getLocalizedMessage();
//            if (LOGGER.isErrorEnabled()) {
//                LOGGER.error(message);
//            }
            throw new ActionException(this, message, cce);
        } finally {
            IOUtils.closeQuietly(env_reader);
        }

    }
}
