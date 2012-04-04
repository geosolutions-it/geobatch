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
package it.geosolutions.geobatch.actions.xstream.test;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.xstream.XstreamAction;
import it.geosolutions.geobatch.actions.xstream.XstreamConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.File;
import java.util.EventObject;
import java.util.HashMap;
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
public class XstreamActionTest {
    
    
    @Test
    public void test() throws ActionException {
        
        XstreamConfiguration fmc=new XstreamConfiguration("ID","NAME","DESC");
        // SIMULATE THE XML FILE CONFIGURATION OF THE ACTION
        fmc.setDirty(false);
        fmc.setFailIgnored(false);
        fmc.setServiceID("serviceID");
//        fmc.setWorkingDirectory("src/test/resources/data/"); TODO fixme
        
        fmc.setOutput("src/test/resources/data/out");
        final Map<String,String> m=new HashMap<String, String>();
        m.put("XstreamConfiguration", "it.geosolutions.geobatch.actions.xstream.XstreamConfiguration");
        m.put("FlowConfiguration", "it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration");
        m.put("EventConsumerConfiguration", "it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration");
        m.put("EventGeneratorConfiguration", "it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration");
        //TODO wildCard
        fmc.setAlias(m);
        
        Queue<EventObject> q=new ArrayBlockingQueue<EventObject>(1);
        
        //TODO q.add(new FileSystemEvent(new File("src/test/resources/data/XstreamFlow.xml"), FileSystemEventType.FILE_ADDED));
        
        XstreamAction fma=new XstreamAction(fmc);
        
        q=fma.execute(q);
        try{
            if (q.size()>0){
                FileSystemEvent res=(FileSystemEvent)q.remove();
                File out=res.getSource();
                if (!out.exists())
                    Assert.fail("FAIL: unable to create output file");
            }
            
        }
        catch (ClassCastException cce){
            Assert.fail("FAIL: "+cce.getLocalizedMessage());
        }

        
        return;
    }
}
