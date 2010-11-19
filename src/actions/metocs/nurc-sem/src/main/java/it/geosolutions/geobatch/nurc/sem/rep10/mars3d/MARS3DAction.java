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

package it.geosolutions.geobatch.nurc.sem.rep10.mars3d;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.octave.DefaultFunctionBuilder;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveFunctionSheet;
import it.geosolutions.geobatch.octave.OctaveThread;

import java.io.File;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ange.octave.exception.OctaveEvalException;


public class MARS3DAction extends BaseAction<FileSystemMonitorEvent> implements Action<FileSystemMonitorEvent> {
       
    private final static Logger LOGGER = Logger.getLogger(MARS3DAction.class.toString());
    
//todo: remove me (this env should be owned by the OctaveThread and
// objects are pushed in it:
    private final static OctaveEnv<OctaveExecutableSheet>  bus=new OctaveEnv<OctaveExecutableSheet>();
    private final static Thread oct=new Thread(new OctaveThread(bus));
    
    // 
    // TODO: check... should we add this member to the BaseAction?
    private final MARS3DActionConfiguration config;
    
    public MARS3DAction(MARS3DActionConfiguration actionConfiguration) {
        super(actionConfiguration);
        config=actionConfiguration;
        
     // TODO implement a M.D.POJO
        //run thread manually
        if (!oct.isAlive())
            oct.start();
    }
    
    /**
     * @return a string representing the output file name of the script
     */
    private String buildFileName(){
        return "REP10_MARS3D-Forecast-T" + new Date().getTime()+".nc";
    }
    
    /**
     * Action to execute on the FileSystemMonitorEvent event queue.
     * 
     * @param Queue<FileSystemMonitorEvent> queue of events to handle in this (and next)
     * action executions.
     * @return Queue<FileSystemMonitorEvent> the resulting list of events
     */
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws ActionException {
        try {
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Executing Octave script...");
            
            FileSystemMonitorEvent ev=events.remove();
            
            if ((ev)!=null){
                /**
                 * getting reference to the first executable sheet
                 * this is supposed to be to only executed sheet
                 * in the configuration.
                 */
                OctaveFunctionSheet fs =(OctaveFunctionSheet) config.getEnv().getSheet(0);
                
                /**
                 * Obtaining the Absolute path
 * @TODO open a ticket to get getBaseDirectory() into Catalog interface
                 */
                FileBaseCatalog c=(FileBaseCatalog) CatalogHolder.getCatalog();
                String base_dir=c.getBaseDirectory()+"/"+config.getWorkingDirectory();
                
                /**
                 * Build output file name
                 */
                String out_name=base_dir+"/nc/"+buildFileName();
                
                /**
                 * Build the MARS3DFunctionBuilder
                 */
                DefaultFunctionBuilder fb=new MARS3DFunctionBuilder(ev.getSource().getAbsolutePath(),out_name);
                
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Preprocessing functions...");
                // try to preprocess the OctaveFunctionSheet
                try {
                    fb.preprocess(fs);
                }catch (Exception e){
                    LOGGER.warning("Exception during buildFunction:"+e.getMessage());
                }
                
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Passing Octave sheet to Octave process... ");
                /**
                 * Push the executable sheet on the thread executing queue
                 */
                synchronized (bus) {

                    fs.gate=new CountDownLatch(1);
                    bus.push(fs);
                    bus.notify();
                    bus.wait();
                    
                 //   fs.gate.await();//30*60, TimeUnit.SECONDS);
                    
                }
                
//                synchronized (fs) {
//                    fs.wait();
//                    System.out.println("DONE");
//                }
                /**
                 * add output file to the event queue
                 */
                events.add(new FileSystemMonitorEvent(
                        new File(out_name),FileSystemMonitorNotifications.FILE_ADDED));

            } // ev==null
            else if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning(
                    "Executing Octave script on empty event queue");
            
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Evaluating: DONE");
            
        }
        catch (InterruptedException e) {
            throw new ActionException(this,"Problem running MARS3D action:\n"
                    +e.getLocalizedMessage());
        }
        catch (OctaveEvalException oee){
            throw new ActionException(this,"Unable to run octave script:\n"
                        +oee.getLocalizedMessage());
        }
        return events;
    }
}
