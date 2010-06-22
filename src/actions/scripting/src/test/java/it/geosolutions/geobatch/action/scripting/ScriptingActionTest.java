/*
 */

package it.geosolutions.geobatch.action.scripting;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author etj
 */
public class ScriptingActionTest extends TestCase {

    public ScriptingActionTest() {
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        System.out.println();
        System.out.println("Running test " + getName());
    }


	@Test
	@Ignore
	public void testLoop() throws ScriptException {

		String engineName = "Rhino";

		ScriptEngine foundEngine = null;
		// create a script engine manager
        ScriptEngineManager mgr = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = mgr.getEngineFactories();
		System.out.println("FOUND " + factories.size() + " factories");
		for(ScriptEngineFactory sef : factories) {
			System.out.println("FACTORY: " +
				"'" + sef.getEngineName() + "' " +
				"'" + sef.getLanguageName() + "' " +
				"'" + sef.getExtensions() + "' " +
				"'" + sef.getNames() + "' " );
			if(sef.getEngineName().contains("Rhino")) {
				foundEngine = sef.getScriptEngine();
			}

		}

		assertNotNull("Can't find engine '"+engineName+"'", foundEngine);
		foundEngine.eval("print('Hello, World')");
	}

	@Test
	@Ignore
	public void testGetEngineByExt() throws ScriptException {

		String engineExt = "js";

        ScriptEngineManager mgr = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = mgr.getEngineByExtension(engineExt);
		assertNotNull("Can't find engine '"+engineExt+"'", engine);

		ScriptEngineFactory sef = engine.getFactory();
		System.out.println("FACTORY for "+engineExt+": " +
			"'" + sef.getEngineName() + "' " +
			"'" + sef.getLanguageName() + "' " +
			"'" + sef.getExtensions() + "' " +
			"'" + sef.getNames() + "' " );

        // evaluate JavaScript code from String
        engine.eval("print('Hello, World')");

	}

	@Test
	@Ignore
	public void testGetEngineByName() throws ScriptException {

		String engineName = "JavaScript";

        ScriptEngineManager mgr = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = mgr.getEngineByName(engineName);
		assertNotNull("Can't find engine '"+engineName+"'", engine);

		ScriptEngineFactory sef = engine.getFactory();
		System.out.println("FACTORY for "+engineName+": " +
			"'" + sef.getEngineName() + "' " +
			"'" + sef.getLanguageName() + "' " +
			"'" + sef.getExtensions() + "' " +
			"'" + sef.getNames() + "' "
			);

        // evaluate JavaScript code from String
        engine.eval("print('Hello, World')");

	}

	@Test
	@Ignore
	public void testGroovy() throws ScriptException {

		String engineName = "groovy";

        ScriptEngineManager mgr = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = mgr.getEngineByName(engineName);
		assertNotNull("Can't find engine '"+engineName+"'", engine);

		ScriptEngineFactory sef = engine.getFactory();
		System.out.println("FACTORY for "+engineName+": " +
			"'" + sef.getEngineName() + "' " +
			"'" + sef.getLanguageName() + "' " +
			"'" + sef.getExtensions() + "' " +
			"'" + sef.getNames() + "' "
			);

        // evaluate code from String
        engine.eval("println \"hello, groovy\"");
	}

	
	public void testGroovyFileAndParam() throws ScriptException, IOException {

		String engineName = "groovy";

        ScriptEngineManager mgr = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = mgr.getEngineByName(engineName);
		assertNotNull("Can't find engine '"+engineName+"'", engine);

        File script = new ClassPathResource("test-data/test.groovy").getFile();

		assertNotNull("Can't find test script", script);

		engine.put("gbtest", "testok");

        // evaluate code from File
		engine.eval(new FileReader(script));
	}

	public void testGroovyClassReload() throws ScriptException, IOException, InstantiationException, IllegalAccessException {

		String engineName = "groovy";

        ScriptEngineManager mgr = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = mgr.getEngineByName(engineName);
		assertNotNull("Can't find engine '"+engineName+"'", engine);


        File script1 = new ClassPathResource("test-data/TestClassV1.groovy").getFile();
        File script2 = new ClassPathResource("test-data/TestClassV2.groovy").getFile();
		File dstFile = File.createTempFile("TestClassReload", ".groovy");

		try {
			{
				FileInputStream fis = new FileInputStream(script1);
				FileOutputStream fos = new FileOutputStream(dstFile);
				IOUtils.copy(fis, fos);
				fos.flush();
				IOUtils.closeQuietly(fis);
				IOUtils.closeQuietly(fos);
			}

			// let's call some method on an instance
			{
				ClassLoader parent = getClass().getClassLoader();
				GroovyClassLoader loader = new GroovyClassLoader(parent);
				Class groovyClass = loader.parseClass(dstFile);

				GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
				Object[] args = {};
				Object ret = groovyObject.invokeMethod("getVersion", args);
				assertEquals(new Integer(1), ret);
			}

			// Overwrite the class
			{
				FileInputStream fis = new FileInputStream(script2);
				FileOutputStream fos = new FileOutputStream(dstFile);
				IOUtils.copy(fis, fos);
				fos.flush();
				IOUtils.closeQuietly(fis);
				IOUtils.closeQuietly(fos);

				FileUtils.touch(dstFile); // just to be sure
			}

			{
				ClassLoader parent = getClass().getClassLoader();
				GroovyClassLoader loader = new GroovyClassLoader(parent);
//				Class groovyClass = loader.parseClass(dstFile);

				Class groovyClass2 = loader.parseClass(dstFile);
				GroovyObject groovyObject = (GroovyObject) groovyClass2.newInstance();
				Object[] args = {};
				Object ret = groovyObject.invokeMethod("getVersion", args);
				assertEquals("File " + dstFile + " not reparsed." , new Integer(2), ret);
			}
		} finally {
			FileUtils.deleteQuietly(dstFile);
		}
	}


	public void testGroovyAction() throws ScriptException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

        File script = new ClassPathResource("test-data/TestAction.groovy").getFile();
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		Class groovyClass = loader.parseClass(script);

//		GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
        ScriptingConfiguration scriptingConfiguration = new ScriptingConfiguration();
        scriptingConfiguration.setId("testId");
        scriptingConfiguration.setName("testName");
        scriptingConfiguration.setScriptFile("/tmp/scriptCfg");
        scriptingConfiguration.setServiceID("scriptingService");

        Constructor constr = groovyClass.getConstructor(ScriptingConfiguration.class);
        GroovyObject groovyObject = (GroovyObject) constr.newInstance(scriptingConfiguration);
		assertTrue(groovyObject instanceof ScriptingAction);
        ScriptingAction groovyAction = (ScriptingAction)groovyObject;
        
        TestListener listener = new TestListener();
        groovyAction.addListener(listener);
	}

}

class TestListener extends ProgressListener {

    private boolean started = false;
    private boolean paused = false;

    public void started() {
        started = true;
    }

    public void progressing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void paused() {
        paused = true;
    }

    public void resumed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void completed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void failed(Throwable exception) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void terminated() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}