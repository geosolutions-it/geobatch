package it.geosolutions.geobatch.action.scripting;

/** 
 * Java Imports ...
 **/
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class ScriptingAction extends BaseAction<FileSystemMonitorEvent>implements Action<FileSystemMonitorEvent> {

    /**
     * Default Logger
     */
    private final static Logger LOGGER = Logger.getLogger(ScriptingAction.class.toString());

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
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws ActionException {
        try {

            listenerForwarder.started();

            // looking for file
            if (events.size() != 1) {
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            }
            FileSystemMonitorEvent event = events.remove();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "Conf is null.");
                throw new IllegalStateException("Conf is null.");
            }

            // final String configId = configuration.getName();

            listenerForwarder.setTask("Processing event " + event);

            /**
             * Dynamic class-loading ...
             */
            final File script = new File(configuration.getScriptFile());
            final String moduleFolder = new File(script.getParentFile(), "jars").getAbsolutePath();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Runtime class-loading from moduleFolder -> " + moduleFolder);
            }

            File moduleDirectory = new File(moduleFolder);
            try {
                addFile(moduleDirectory.getParentFile());
                addFile(moduleDirectory);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error, could not add URL to system classloader", e);
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
                                LOGGER.log(Level.SEVERE,
                                        "Error, could not add URL to system classloader", e);
                            }
                        }
                    }
                }
            }

            /**
             * Evalutaing script ...
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
                    events.add(new FileSystemMonitorEvent(new File(outputFile),
                            FileSystemMonitorNotifications.FILE_ADDED));
                }
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can't create an Action for " + configuration, e);
            } catch (ScriptException e) {
                LOGGER.log(Level.SEVERE, "Can't create an Action for " + configuration, e);
            }

            listenerForwarder.completed();
            return events;
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // no need to
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
            LOGGER.log(Level.SEVERE, "Error, could not add URL to system classloader", t);
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

}