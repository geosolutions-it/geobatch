/*
 */

package it.geosolutions.geobatch.action.scripting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;


/**
 * 
 * @author etj
 */
public class ScriptingTest extends Assert {

    

    @Test
    public void testLoop() throws ScriptException {

        String engineName = "groovy";

        ScriptEngine foundEngine = null;
        // create a script engine manager
        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        System.out.println("FOUND " + factories.size() + " factories");
        for (ScriptEngineFactory sef : factories) {
            System.out.println("FACTORY: " + "'" + sef.getEngineName() + "' " + "'"
                    + sef.getLanguageName() + "' " + "'" + sef.getExtensions() + "' " + "'"
                    + sef.getNames() + "' ");
            if (sef.getEngineName().contains(engineName)) {
                foundEngine = sef.getScriptEngine();
            }

        }

        assertNotNull("Can't find engine '" + engineName + "'", foundEngine);
        foundEngine.eval("print('Hello, World')");
    }

    @Test
    public void testGetEngineByExt() throws ScriptException {

        String engineExt = "js";

        ScriptEngineManager mgr = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = mgr.getEngineByExtension(engineExt);
        assertNotNull("Can't find engine '" + engineExt + "'", engine);

        ScriptEngineFactory sef = engine.getFactory();
        System.out.println("FACTORY for " + engineExt + ": " + "'" + sef.getEngineName() + "' "
                + "'" + sef.getLanguageName() + "' " + "'" + sef.getExtensions() + "' " + "'"
                + sef.getNames() + "' ");

        // evaluate JavaScript code from String
        engine.eval("print('Hello, World')");

    }

    @Test
    public void testGroovy() throws ScriptException {

        String engineName = "groovy";

        ScriptEngineManager mgr = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = mgr.getEngineByName(engineName);
        assertNotNull("Can't find engine '" + engineName + "'", engine);

        ScriptEngineFactory sef = engine.getFactory();
        System.out.println("FACTORY for " + engineName + ": " + "'" + sef.getEngineName() + "' "
                + "'" + sef.getLanguageName() + "' " + "'" + sef.getExtensions() + "' " + "'"
                + sef.getNames() + "' ");

        // evaluate code from String
        engine.eval("println \"hello, groovy\"");
    }

    @Test
    public void testGroovyFileAndParam() throws ScriptException, IOException {

        String engineName = "groovy";

        ScriptEngineManager mgr = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = mgr.getEngineByName(engineName);
        assertNotNull("Can't find engine '" + engineName + "'", engine);

        File script = new ClassPathResource("test-data/test.groovy").getFile();

        assertNotNull("Can't find test script", script);

        engine.put("gbtest", "testok");

        // evaluate code from File
        engine.eval(new FileReader(script));
    }


}
