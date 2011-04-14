package it.geosolutions.geobatch.action.scripting;

/** 
 * Java Imports ...
 **/
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
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
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

/**
 * ScriptingAction Class definition ...
 **/
public class ScriptingAction extends BaseAction<FileSystemEvent>implements Action<FileSystemEvent> {

    /**
     * Default Logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ScriptingAction.class.toString());

    protected ScriptEngineManager factory = new ScriptEngineManager();

    protected ScriptEngine engine = null;

    private ScriptingConfiguration configuration;

    /**
     * Constructor.
     * 
     * @param configuration
     * @throws IOException
     */
    public ScriptingAction(ScriptingConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration=configuration;
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

            // looking for file
            if (events.size() != 1) {
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            }
            FileSystemEvent event = events.remove();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                LOGGER.error("Conf is null.");
                throw new IllegalStateException("Conf is null.");
            }

            // final String configId = configuration.getName();

            listenerForwarder.setTask("Processing event " + event);

            /**
             * Dynamic class-loading ...
             */
            final File script = new File(configuration.getScriptFile());
            final String moduleFolder = new File(script.getParentFile(), "jars").getAbsolutePath();

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Runtime class-loading from moduleFolder -> " + moduleFolder);
            }

            File moduleDirectory = new File(moduleFolder);
            try {
                addFile(moduleDirectory.getParentFile());
                addFile(moduleDirectory);
            } catch (IOException e) {
                LOGGER.error("Error, could not add URL to system classloader", e);
            }
            String classpath = System.getProperty("java.class.path");
            File[] moduleFiles = moduleDirectory.listFiles();
            if (moduleFiles != null) {
                for (int i = 0; i < moduleFiles.length; i++) {
                    File moduleFile = moduleFiles[i];
                    if (moduleFile.getName().endsWith(".jar")) {
                        if (classpath.indexOf(moduleFiles[i].getName()) == -1) {
                            try {
                                addFile(moduleFiles[i]);
                            } catch (IOException e) {
                                LOGGER.error("Error, could not add URL to system classloader", e);
                            }
                        }
                    }
                }
            }

            /**
             * Evaluating script ...
             */
            try {
                // Now, pass a different script context
                ScriptContext newContext = new SimpleScriptContext();
                Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);

                // add new variable "scriptingConfiguration" to the new
                // engineScope
                engineScope.put("scriptingConfiguration", configuration);

                engine.eval(new FileReader(script), engineScope);

                Invocable inv = (Invocable) engine;
                List<String> outputFiles = (List<String>) inv.invokeFunction("execute",
                        new Object[]{configuration, event.getSource().getAbsolutePath(), listenerForwarder});

                // FORWARDING EVENTS
                for (String outputFile : outputFiles) {
                    events.add(new FileSystemEvent(new File(outputFile),
                            FileSystemEventType.FILE_ADDED));
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Can't create an Action for " + configuration, e);
            } catch (ScriptException e) {
                LOGGER.error("Can't create an Action for " + configuration, e);
            }

            listenerForwarder.completed();
            return events;
        } catch (Throwable t) {
            LOGGER.error(t.getLocalizedMessage(), t); // no need to
            // log,
            // we're
            // rethrowing
            // it
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

        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { moduleURL });
        } catch (Throwable t) {
            LOGGER.error("Error, could not add URL to system classloader", t);
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

}