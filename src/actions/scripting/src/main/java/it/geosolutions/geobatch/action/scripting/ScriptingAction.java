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

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScriptingAction Class definition ...
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 **/
public class ScriptingAction extends BaseAction<FileSystemEvent> implements
		Action<FileSystemEvent> {

	/**
	 * set of well known keys
	 */
	public static final String CONFIG_KEY="configuration";
	public static final String TEMPDIR_KEY="tempDir";
	@Deprecated
	public static final String CONTEXT_KEY=TEMPDIR_KEY;
	public static final String CONFIGDIR_KEY="configDir";
	public static final String LISTENER_KEY="listenerForwarder";
	public static final String EVENTS_KEY="events";
	public static final String RETURN_KEY="return";
	
	
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
				throw new ActionException(this, "Configuration is null.");
			}

			final String scriptName = it.geosolutions.tools.commons.file.Path.findLocation(configuration.getScriptFile(),getConfigDir().getAbsolutePath());
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
				throw new ActionException(this, e.getLocalizedMessage(),e);
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
								throw new ActionException(this, e.getLocalizedMessage(),e);
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
//			engineScope.put("eventList", events);
//			engineScope.put("runningContext", getRunningContext());

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
				throw new ActionException(this, e.getLocalizedMessage(),e);
			} finally {
				IOUtils.closeQuietly(reader);
			}

			final Invocable inv = (Invocable) engine;

			listenerForwarder.setTask("Executing script: " + script.getName());

			// check for incoming event list
			if (events == null) {
				throw new ActionException(this, "Unable to start the script using a null incoming list of events");
			}

			// call the script
			final Map<String,Object> argsMap = new HashedMap();
			argsMap.put(ScriptingAction.CONFIG_KEY,configuration);
			argsMap.put(ScriptingAction.TEMPDIR_KEY,getTempDir());
			argsMap.put(ScriptingAction.CONFIGDIR_KEY,getConfigDir());
			argsMap.put(ScriptingAction.EVENTS_KEY,events);
			argsMap.put(ScriptingAction.LISTENER_KEY,listenerForwarder);
			
			final Map<String,Object> mapOut = (Map<String,Object>) inv.invokeFunction(
					"execute", new Object[] { argsMap });

			// checking output
			final Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();
			if (mapOut == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Caution returned map from script "
							+ configuration.getScriptFile()
							+ " is null.\nSimulating an empty return list.");
				}
				return ret;
			}
			
			final Object obj=mapOut.get(ScriptingAction.RETURN_KEY);
			if (obj == null) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Caution returned object from script "
							+ configuration.getScriptFile()
							+ " is null.\nPassing an empty list to the next action!");
				}
				return ret;
			}
			
			if (obj instanceof List){
				final List<Object> list=(List<Object>)obj;
				for (final Object out : list) {
					if (out == null) {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("Caution returned object from script "
									+ configuration.getScriptFile()
									+ " is null.\nContinue with the next one.");
						}
						continue;
					}

					if (out instanceof FileSystemEvent) {
						FileSystemEvent ev = (FileSystemEvent) out;
						ret.add(ev);
					} else if (out instanceof File) {
						ret.add(new FileSystemEvent((File) out,
								FileSystemEventType.FILE_ADDED));
					} else {
						final File file = new File(out.toString());
						if (!file.exists() && LOGGER.isWarnEnabled()) {
							LOGGER.warn("Caution returned object from script "
									+ configuration.getScriptFile()
									+ " do not points to an existent file!");
						}
						ret.add(new FileSystemEvent(file,
								FileSystemEventType.FILE_ADDED));
					}
				}
			} else {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Caution returned object from script "
							+ configuration.getScriptFile()
							+ " is not a valid List.\nPassing an empty list to the next action!");
				}
				return ret;
			}

			listenerForwarder.completed();

			return ret;

		} catch (Exception t) {
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