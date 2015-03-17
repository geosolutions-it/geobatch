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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.flow.event.ProgressListener;

import it.geosolutions.geobatch.flow.event.action.ActionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import junit.framework.Assert;

import org.geotools.test.TestData;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * 
 * @author etj
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class ScriptingActionTest extends Assert {

    

    @Test
    public void testGroovyAction() throws ActionException, IOException, URISyntaxException{

        //File script = new ClassPathResource("test-data/TestNoDeps.groovy").getFile();
        File script = null;
        script = new File(TestData.url(null, "TestNoDeps.groovy").toURI());
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
        File dir=new File(TestData.url(null, null).toURI());
        cfg.setOverrideConfigDir(dir);
        
        Queue <FileSystemEvent> inq = new LinkedList<FileSystemEvent>();
        ScriptingAction action = new ScriptingAction(cfg);
        action.setTempDir(dir);
        action.setConfigDir(dir);
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
    
    @Ignore
    @Test
    public void testMassiveGroovyAction() throws ActionException, IOException, URISyntaxException{
    	for(int i=0; i<10000; i++){
    		testGroovyAction();
    	}
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