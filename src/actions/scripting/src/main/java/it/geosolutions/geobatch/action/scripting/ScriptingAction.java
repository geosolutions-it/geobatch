package it.geosolutions.geobatch.action.scripting;

/** 
 * Java Imports ...
 **/
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScriptingAction Class definition ...
 **/
public class ScriptingAction extends BaseAction<FileSystemEvent> implements
		Action<FileSystemEvent> {

	/**
	 * Default Logger
	 */
	private final static Logger LOGGER = LoggerFactory
			.getLogger(ScriptingAction.class);

	protected ScriptEngineManager factory = new ScriptEngineManager();

	protected ScriptEngine engine = null;

	private ScriptingConfiguration configuration = null;

	/**
	 * Constructor.
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public ScriptingAction(ScriptingConfiguration configuration)
			throws IOException {
		super(configuration);
		this.configuration = configuration;
		engine = factory.getEngineByName(configuration.getLanguage());
	}

	/**
	 * Default execute method...
	 */
	@SuppressWarnings("unchecked")
	public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
			throws ActionException {
		try {

			listenerForwarder.started();

			// //
			// data flow configuration and dataStore name must not be null.
			// //
			if (configuration == null) {
				final String message = "Configuration is null.";
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				throw new ActionException(this, message);
			}

			final String scriptName = Path.getAbsolutePath(configuration
					.getScriptFile());
			if (scriptName == null)
				throw new ActionException(this,
						"Unable to locate the script file name: "
								+ configuration.getScriptFile());

			final File script = new File(scriptName);

			/**
			 * Dynamic class-loading ...
			 */
			listenerForwarder.setTask("dynamic class loading ...");
			final String moduleFolder = new File(script.getParentFile(), "jars")
					.getAbsolutePath();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Runtime class-loading from moduleFolder -> "
						+ moduleFolder);
			}

			final File moduleDirectory = new File(moduleFolder);
			try {
				addFile(moduleDirectory.getParentFile());
				addFile(moduleDirectory);
			} catch (IOException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error(
							"Error, could not add URL to system classloader", e);
			}
			final String classpath = System.getProperty("java.class.path");
			final File[] moduleFiles = moduleDirectory.listFiles();
			if (moduleFiles != null) {
				for (File moduleFile : moduleFiles) {
					final String name = moduleFile.getName();
					if (name.endsWith(".jar")) {
						if (classpath.indexOf(name) == -1) {
							try {
								if (LOGGER.isInfoEnabled())
									LOGGER.info("Adding: " + name);
								addFile(moduleFile);
							} catch (IOException e) {
								if (LOGGER.isErrorEnabled())
									LOGGER.error(
											"Error, could not add URL to system classloader",
											e);
							}
						}
					}
				}
			}
			/**
			 * Evaluating script ...
			 */
			listenerForwarder.setTask("evaluating script ...");

			// Now, pass a different script context
			final ScriptContext newContext = new SimpleScriptContext();
			final Bindings engineScope = newContext
					.getBindings(ScriptContext.ENGINE_SCOPE);

			// add variables to the new engineScope
			engineScope.put("eventList", events);
			engineScope.put("runningContext", getRunningContext());

			// add properties as free vars in script
			final Map<String, Object> props = configuration.getProperties();
			if (props != null) {
				final Set<Entry<String, Object>> set = props.entrySet();
				final Iterator<Entry<String, Object>> it = set.iterator();
				while (it.hasNext()) {
					final Entry<String, ?> prop = it.next();
					if (prop == null) {
						continue;
					}
					if (LOGGER.isInfoEnabled())
						LOGGER.info(" Adding script property: " + prop.getKey()
								+ " : " + prop.getValue());
					engineScope.put(prop.getKey(), prop.getValue());
				}
			}
			// read the script
			FileReader reader = null;
			try {
				reader = new FileReader(script);
				engine.eval(reader, engineScope);
			} catch (FileNotFoundException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error(e.getLocalizedMessage(), e);
				throw e;
			} finally {
				IOUtils.closeQuietly(reader);
			}

			final Invocable inv = (Invocable) engine;

			listenerForwarder.setTask("Executing script: " + script.getName());

			// check for incoming event list
			Object ev = null;
			if (events != null) {
				final FileSystemEvent event = events.peek();
				if (event != null) {
					final File eventFile = event.getSource();
					if (eventFile != null) {
						ev = eventFile.getAbsolutePath();
					}
				}
			}
			final List<String> outputFiles = (List<String>) inv.invokeFunction(
					"execute", new Object[] { configuration, ev,
							listenerForwarder });

			// optionally clear input queue
			events.clear();

			// FORWARDING EVENTS
			final Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();

			for (String out : outputFiles) {
				if (out != null) {
					ret.add(new FileSystemEvent(new File(out),
							FileSystemEventType.FILE_ADDED));
				}
			}

			listenerForwarder.completed();

			return ret;

		} catch (Exception t) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(t.getLocalizedMessage(), t); // no need to
			listenerForwarder.failed(t);
			throw new ActionException(this, t.getMessage(), t);
		} finally {
			engine = null;
			factory = null;
		}
	}

	/**
	 * 
	 * @param moduleFile
	 * @throws IOException
	 */
	private static void addFile(File moduleFile) throws IOException {
		URL moduleURL = moduleFile.toURI().toURL();
		final Class[] parameters = new Class[] { URL.class };

		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { moduleURL });
		} catch (Throwable t) {
			LOGGER.error("Error, could not add URL to system classloader", t);
			throw new IOException(
					"Error, could not add URL to system classloader");
		}
	}

}