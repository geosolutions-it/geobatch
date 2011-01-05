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

import it.geosolutions.geobatch.actions.freemarker.FreeMarkerAction;
import it.geosolutions.geobatch.actions.freemarker.FreeMarkerConfiguration;
import it.geosolutions.geobatch.actions.freemarker.TemplateModelEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
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
    public static void main(String[] args) throws ActionException {
        
        FreeMarkerConfiguration fmc=new FreeMarkerConfiguration();
        // SIMULATE THE XML FILE CONFIGURATION OF THE ACTION
        fmc.setDescription("description");
        fmc.setDirty(false);
        fmc.setFailIgnored(false);
        fmc.setId("id");
        fmc.setName("name");
        fmc.setServiceID("serviceID");
        fmc.setWorkingDirectory("/home/carlo/work/data/freemarker/");
        fmc.setInput("test.xml");
        fmc.setOutput("test_out.xml");
        Map<String,Object> m=new HashMap<String, Object>();
        m.put("SHEET_NAME", "MY_NEW_SHEET_NAME");
        fmc.setRoot(m);
        
        // SIMULATE THE EventObject on the queue 
        Map<String,Object> mev=new HashMap<String, Object>();
        mev.put("SOURCE_PATH", "/path/to/source");
        mev.put("WORKING_DIR", "/absolute/working/dir");
        mev.put("FILE_IN", "in_test_file.dat");
        mev.put("FILE_OUT", "out_test_file.dat");
        
        Queue<EventObject> q=new ArrayBlockingQueue(2);
        
        q.add(new TemplateModelEvent(mev));
        
        FreeMarkerAction fma=new FreeMarkerAction(fmc);
        
        fma.execute(q);
        return;
    }
}
