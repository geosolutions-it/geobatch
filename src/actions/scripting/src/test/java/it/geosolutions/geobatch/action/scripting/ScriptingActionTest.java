/*
 */

package it.geosolutions.geobatch.action.scripting;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.flow.event.ProgressListener;

import it.geosolutions.geobatch.flow.event.action.ActionException;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * 
 * @author etj
 */
public class ScriptingActionTest extends Assert {

    

    @Test
    public void testGroovyAction() throws ActionException, IOException{

        File script = new ClassPathResource("test-data/TestNoDeps.groovy").getFile();

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("k1", "v1");
        props.put("k2", "v2");
        props.put("k3", "v3");
        Integer intVar=Integer.valueOf(12);
        props.put("intVar", intVar);
                
        ScriptingConfiguration cfg = new ScriptingConfiguration("testId", "testName", "testDesc");
        cfg.setScriptFile(script.getAbsolutePath());
        cfg.setServiceID("scriptingService");
        cfg.setLanguage("groovy");
        cfg.setProperties(props);
        cfg.setOverrideConfigDir(new File(Path.getAbsolutePath("./src/test/resources/")));
        
        Queue <FileSystemEvent> inq = new LinkedList<FileSystemEvent>();
        ScriptingAction action = new ScriptingAction(cfg);
        action.setTempDir(new File(Path.getAbsolutePath("./src/test/resources/")));
        Queue<FileSystemEvent> out = action.execute(inq);

        List<String> outs = new ArrayList<String>();
        for (FileSystemEvent fse: out) {
            String s = fse.getSource().getName();
            System.out.println("output from script: --> " + s);
            outs.add(s);
        }

        assertEquals("v1", outs.get(0));

        assertEquals("v1", outs.get(1));
        assertEquals("v2", outs.get(2));
        // modified copy by the script
        assertEquals("13", outs.get(3));
        // unchanged local variable
        assertEquals(intVar, Integer.valueOf(12));
    }

}

class TestListener extends ProgressListener {


    private boolean started = false;

    private boolean paused = false;

    protected TestListener(BaseIdentifiable caller) {
        super(caller);
    }
    
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