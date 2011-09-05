/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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
public class FreeMarkerActionTest {
    
    /*
     * 
        <?xml version="1.0" encoding="UTF-8"?>
        <!-- OCTAVE ENV -->
        <octave>
          <sheets>
                <!-- OCTAVE SHEET -->
                <sheet name="${SHEET_NAME}">
                  <commands>
                        <OctaveCommand executed="false">
                                <command>source "${event.SOURCE_PATH}";</command>
                        </OctaveCommand>
                        <OctaveCommand executed="false">
                                <command>cd "${event.WORKING_DIR}";</command>
                        </OctaveCommand>
                        <OctaveCommand executed="false">
                                <command>mars3d("${event.FILE_IN}","${event.FILE_OUT}");</command>
                        </OctaveCommand>
                  </commands>
                  <definitions/>
                  <returns/>
                </sheet>
          </sheets>
        </octave>
     */
    
//    @Test
//    public void testXstream() throws ActionException {
//        //FileSystemEvent ev=queue.get(0);
//        FileInputStream fis=null;
//        try{
//            File mapFile=new File("/home/carlo/work/data/briseide/dynamic/raster/pph/20101012T210000_pph/20101012T210000_pph.xml");//ev.getSource();
//            XStream xstream=new XStream();
//            fis=new FileInputStream(mapFile);
//            Map<String,Object> map=(Map<String, Object>) xstream.fromXML(fis);
//            System.out.println(map.get("LAYERNAME"));
//            
//            String layerName=(String) map.get("LAYERNAME");
//            
//            String runtimeString=layerName.substring(0,layerName.indexOf('_'));
//            TimeParser parser=new TimeParser();
//            try {
//                List<Date> date=parser.parse(runtimeString);
//                final SimpleDateFormat iso801= new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
//                final String runtime=iso801.format(date.get(0));
//                System.out.println("RUNTIME: "+runtime);
//                map.put("RUNTIME",runtime);
//            } catch (ParseException e) {
//                
//            }
//            
//            
//            System.out.println(map.get("CRS"));
//        }
//        catch (IOException ioe){
//            
//        }
//        finally {
//            try{ 
//                if (fis!=null)
//                    fis.close();
//            }
//            catch (Exception e)
//            {}
//        }
//        
//    }
    
    
    @Test
    public void test() throws ActionException {
        
        FreeMarkerConfiguration fmc=new FreeMarkerConfiguration("ID","NAME","DESC");
        // SIMULATE THE XML FILE CONFIGURATION OF THE ACTION
        fmc.setDirty(false);
        fmc.setFailIgnored(false);
        fmc.setServiceID("serviceID");
        fmc.setWorkingDirectory("src/test/resources/data/");
        fmc.setInput("test.xml");
        fmc.setOutput("out");
        // 2 incoming events generates 2 output files
        fmc.setNtoN(true);
        Map<String,Object> m=new HashMap<String, Object>();
        m.put("SHEET_NAME", "MY_NEW_SHEET_NAME");
        fmc.setRoot(m);
        
        // SIMULATE THE EventObject on the queue 
        Map<String,Object> mev=new HashMap<String, Object>();
        mev.put("SOURCE_PATH", "/path/to/source");
        mev.put("WORKING_DIR", "/absolute/working/dir");
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
        mev2.put("SOURCE_PATH", "/path/to/source_2");
        mev2.put("WORKING_DIR", "/absolute/working/dir_2");
        mev2.put("FILE_IN", "in_test_file_2.dat");
        mev2.put("FILE_OUT", "out_test_file_2.dat");
        mev2.put("LIST", list);
        
        Queue<EventObject> q=new ArrayBlockingQueue<EventObject>(2);
        
        // 2 incoming events generates 2 output files
        q.add(new TemplateModelEvent(mev));
        q.add(new TemplateModelEvent(mev2));
        
        FreeMarkerAction fma=new FreeMarkerAction(fmc);
        
        q=fma.execute(q);
        try{
            FileSystemEvent res=(FileSystemEvent)q.remove();
            File out=res.getSource();
            if (!out.exists())
                Assert.fail("FAIL: unable to create output file");
            
        }
        catch (ClassCastException cce){
            Assert.fail("FAIL: "+cce.getLocalizedMessage());
        }

        
        return;
    }
    
    @Test
    public void multipleTest() throws ActionException {
        
        FreeMarkerConfiguration fmc=new FreeMarkerConfiguration("ID","NAME","DESC");
        // SIMULATE THE XML FILE CONFIGURATION OF THE ACTION
        fmc.setDirty(false);
        fmc.setFailIgnored(false);
        fmc.setServiceID("serviceID");
        fmc.setWorkingDirectory("src/test/resources/data/");
        fmc.setInput("test.xml");
        fmc.setOutput("out");
        Map<String,Object> m=new HashMap<String, Object>();
        m.put("SHEET_NAME", "MY_NEW_SHEET_NAME");
        fmc.setRoot(m);
        
        // SIMULATE THE EventObject on the queue 
        Map<String,Object> mev=new HashMap<String, Object>();
        mev.put("SOURCE_PATH", "/path/to/source");
        mev.put("WORKING_DIR", "/absolute/working/dir");
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
        q.add(new FileSystemEvent(new File("src/test/resources/data/"), FileSystemEventType.FILE_ADDED));
        
        FreeMarkerAction fma=new FreeMarkerAction(fmc);
        
        q=fma.execute(q);
        try{
            FileSystemEvent res=(FileSystemEvent)q.remove();
            File out=res.getSource();
            if (!out.exists())
                Assert.fail("FAIL: unable to create output file");
            
        }
        catch (ClassCastException cce){
            Assert.fail("FAIL: "+cce.getLocalizedMessage());
        }

        
        return;
    }
}
