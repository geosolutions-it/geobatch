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
package it.geosolutions.geobatch.actions.freemarker.test;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.freemarker.FreeMarkerAction;
import it.geosolutions.geobatch.actions.freemarker.FreeMarkerConfiguration;
import it.geosolutions.geobatch.actions.freemarker.TemplateModelEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Assert;
import org.junit.Test;
/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */

public class FreeMarkerActionTest extends BaseTest {
        
    @Test
    public void test() throws ActionException, IllegalAccessException, FileNotFoundException, IOException {

        File testDirAux = loadFile("test-data/test.xml");
        File testDir = testDirAux.getParentFile();

        FreeMarkerConfiguration fmc=new FreeMarkerConfiguration("ID","NAME","DESC");
        // SIMULATE THE XML FILE CONFIGURATION OF THE ACTION
        fmc.setDirty(false);
        fmc.setFailIgnored(false);
        fmc.setServiceID("serviceID");
//        fmc.setWorkingDirectory(workingDir.getAbsolutePath());
        fmc.setInput("test.xml");
        fmc.setOutput(getTempDir().getAbsolutePath()+"/out");
        // 2 incoming events generates 2 output files
        fmc.setNtoN(true);
        Map<String,Object> m=new HashMap<String, Object>();
        m.put("SHEET_NAME", "MY_NEW_SHEET_NAME");
        fmc.setRoot(m);
        
        // SIMULATE THE EventObject on the queue 
        Map<String,Object> mev=new HashMap<String, Object>();
        mev.put("SOURCE_PATH", testDir.getAbsolutePath()+"/in");
        mev.put("WORKING_DIR", getTempDir().getAbsolutePath());
        mev.put("FILE_IN", "in_test_file.dat");
        mev.put("FILE_OUT", "out_test_file.dat");
        List<String> list=new ArrayList<String>(4);
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        mev.put("LIST", list);
        
        // SIMULATE THE 2nd EventObject on the queue 
        Map<String,Object> mev2=new HashMap<String, Object>();
        mev2.put("SOURCE_PATH", testDir.getAbsolutePath()+"/in2");
        mev2.put("WORKING_DIR", getTempDir().getAbsolutePath());
        mev2.put("FILE_IN",  "in_test_file_2.dat");
        mev2.put("FILE_OUT", "out_test_file_2.dat");
        mev2.put("LIST", list);
        
        Queue<EventObject> q=new ArrayBlockingQueue<EventObject>(2);
        
        // 2 incoming events generates 2 output files
        q.add(new TemplateModelEvent(mev));
        q.add(new TemplateModelEvent(mev2));
        
        FreeMarkerAction fma=new FreeMarkerAction(fmc);
//        fma.setRunningContext(workingDir.getAbsolutePath());
        fma.setTempDir(getTempDir());
        fma.setConfigDir(testDir);
        q=fma.execute(q);
        try{
            FileSystemEvent res=(FileSystemEvent)q.remove();
            File out=res.getSource();
            Assert.assertTrue("FAIL: unable to create output file",out.exists());
        }
        catch (ClassCastException cce){
            Assert.fail("FAIL: "+cce.getLocalizedMessage());
        }        
        return;
    }
    
    @Test
    public void multipleTest() throws ActionException, IllegalAccessException {

        File testDirAux = loadFile("test-data/test.xml");
        File testDir = testDirAux.getParentFile();

        FreeMarkerConfiguration fmc=new FreeMarkerConfiguration("ID","NAME","DESC");
        // SIMULATE THE XML FILE CONFIGURATION OF THE ACTION
        fmc.setDirty(false);
        fmc.setFailIgnored(false);
        fmc.setServiceID("serviceID");
        

        File outDir = new File(getTempDir(), "out");
        outDir.mkdir(); // output dir is expected to exist

        fmc.setInput("test.xml");
        fmc.setOutput("out");
        Assert.assertTrue(outDir.exists());
        Map<String,Object> m=new HashMap<String, Object>();
        m.put("SHEET_NAME", "MY_NEW_SHEET_NAME");
        fmc.setRoot(m);
        
        // SIMULATE THE EventObject on the queue 
        Map<String,Object> mev=new HashMap<String, Object>();
        mev.put("SOURCE_PATH", testDir.getAbsolutePath()+"/in");
        mev.put("WORKING_DIR", getTempDir().getAbsolutePath());
        mev.put("FILE_IN", "in_test_file.dat");
        mev.put("FILE_OUT", "out_test_file.dat");
        
        List<String> list=new ArrayList<String>(4);
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        mev.put("LIST", list);
        
        Queue<EventObject> q=new ArrayBlockingQueue<EventObject>(2);
        
        q.add(new TemplateModelEvent(mev));
        q.add(new FileSystemEvent(getTempDir(), FileSystemEventType.FILE_ADDED));
        
        FreeMarkerAction fma=new FreeMarkerAction(fmc);
        fma.setTempDir(getTempDir());
        fma.setConfigDir(testDir);
        
        q=fma.execute(q);
        try{
            FileSystemEvent res=(FileSystemEvent)q.remove();
            File out=res.getSource();
            Assert.assertTrue("FAIL: unable to create output file",out.exists());
            
        }
        catch (ClassCastException cce){
            Assert.fail("FAIL: "+cce.getLocalizedMessage());
        }

        
        return;
    }
}
