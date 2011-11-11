/*
 */

package it.geosolutions.geobatch.action.scripting;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
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

        Map<String, String> props = new HashMap<String, String>();
        props.put("k1", "v1");
        props.put("k2", "v2");
        props.put("k3", "v3");
                
        ScriptingConfiguration cfg = new ScriptingConfiguration("testId", "testName", "testDesc");
        cfg.setScriptFile(script.getAbsolutePath());
        cfg.setServiceID("scriptingService");
        cfg.setLanguage("groovy");
        cfg.setProperties(props);

        
        Queue <FileSystemEvent> inq = new LinkedList<FileSystemEvent>();
        ScriptingAction action = new ScriptingAction(cfg);
        Queue<FileSystemEvent> out = action.execute(inq);

        List<String> outs = new ArrayList<String>();
        for (FileSystemEvent fse: out) {
            String s = fse.getSource().getName();
            System.out.println("output from script: --> " + s);
            outs.add(s);
        }

        assertEquals("null", outs.get(0));
        assertEquals("v1", outs.get(1));

        assertEquals("v1", outs.get(2));
        assertEquals("v2", outs.get(3));
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