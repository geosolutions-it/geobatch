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
package it.geosolutions.geobatch.nurc.sem.rep10.shom;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.metocs.base.NetcdfEvent;
import it.geosolutions.geobatch.tools.file.Extract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.ma2.Section;
import ucar.ma2.Section.Iterator;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;
/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class SHOMAction extends BaseAction<EventObject>{
    // logger
    private final static Logger LOGGER = Logger.getLogger(SHOMAction.class.toString());
    // time zone
    private final static TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
    // time format
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'");
    
    static {
        // setting time zone to UTC
        sdf.setTimeZone(TZ_UTC);
    }
    
    private SHOMConfiguration conf;
    
    /**
     * Constructor
     * @param configuration
     */
    public SHOMAction(SHOMConfiguration configuration) {
        super(configuration);
        conf=configuration;
        
        
    }

    public Queue<EventObject> execute(Queue<EventObject> events)
            throws ActionException {

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Starting with processing...");
        
        // looking for file
        if (events.size() != 1)
            throw new IllegalArgumentException("Wrong number of elements for this action: "+ events.size());
        
        EventObject event= events.remove();
        if (event instanceof FileSystemMonitorEvent){
            FileSystemMonitorEvent fs_event=(FileSystemMonitorEvent) event;
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Event is a FileSystemMonitorEvent");
            
            try{
                /*
                 * check the resulting object
                 */
                File f=null;
                String dir_name=null;
                if ((f=fs_event.getSource())!=null) {
                    // try to extract the received file
                    dir_name=Extract.extract(f.getAbsolutePath());
                }
                else
                    throw new FileNotFoundException("BAD FileSystemMonitorEvent path");
                f=null;

                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Scanning directory: "+dir_name);
                
//TODO
//NOTE take a look here                
//http://www.unidata.ucar.edu/software/netcdf-java/tutorial/NetcdfDataset.html
                
                /*
                 * Initialize configuration
                 */
                if (conf.init()){
                    if (LOGGER.isLoggable(Level.INFO)){
                        LOGGER.info("SHOM configuration initialized");
                    }
                }
                else {
                    if (LOGGER.isLoggable(Level.SEVERE)){
                        LOGGER.severe("Failed to initialize the SHOM configuration");
                    }
                    return null;
                }
                
                Map<String, String> map=conf.getRoot(); 
                if (map!=null){
// added into configuration
//                    map.put("joinVar", new SimpleScalar("time"));
                    /*
                     * append the searchPath variable to the 
                     * substitution map
                     */
                    map.put("searchPath", dir_name);
                }
                
                /*
                 * Processing the input model
 * TODO pass the main ThreadPool
 * using null will be used a SingleThreadPool
                 */
                NcMLFilter filter=new NcMLFilter(conf,null);

                PipedReader pr=filter.produce(filter);

                NetcdfDataset dataset=NcMLReader.readNcML(pr, null);

                pr.close();
                
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Closing the producer");
                // be sure the producer thread ends
                filter.close(false);
                
                /*
                 * Apply transformations to the dataset 
                 */
                dataset=transform(dataset);
//DEBUG
//NetcdfFile ncdnew = ucar.nc2.FileWriter.writeToFile(dataset, conf.getWorkingDirectory()+"/out.nc", true);
//ncdnew.close();
                
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Aggregating by time");
                
                NetcdfEvent ev=new NetcdfEvent(dataset,conf.getPerformBackup());
                events.add(ev);
                return events;
            }
            catch (IOException ioe){
                throw new ActionException(this, ioe.getLocalizedMessage());
            }
            catch (Exception e){
e.printStackTrace();
                throw new ActionException(this, e.getLocalizedMessage());
            }
            finally {
                NetcdfDataset.shutdown();
            }
        } // if (event instanceof FileSystemMonitorEvent)
        else
            throw new IllegalArgumentException("Wrong event type for this action: "+event.getClass().getName());
    }
    
    /**
     * Make transformations on the passed dataset
     * @note this is specific for the SHOM file format
     * @param dataset 
     * @throws IOException if reading variables fails
     */
    private NetcdfDataset transform(NetcdfDataset dataset) throws IOException {
        //////////////////////////////////////TRANSFORMATIONS //////////////////////////////
        /*
        * Setting location:
        * This is done since this file do not exists on the
        * filesystem so a fake name should be provided
        * @note this is due the Netcdf2Geotiff uses this name
        * to extract the cruise name.
        */
        dataset.setLocation("rep10_SHOM"+conf.getType()+"-Forecast-T" + new Date().getTime()+".nc");
        
        //not needed              
        //dataset.setTitle("TITLE_SHOM_WW3-MENOR-4000M");
        
        /*
        * Time shift
        */
//String var=conf.getRoot().get("JoinVar").toString();
//should result in ->time (for shom)
        Variable timeVar=dataset.findVariable("time");
        int [] timeShape=timeVar.getShape();
        Array timeArray=timeVar.read();
        
        GregorianCalendar gc=new GregorianCalendar(1900,Calendar.JANUARY,1,0,0,0);
        gc.setTimeZone(TZ_UTC);
        
        // 1970 Jan 1 0:0:0 UTC in secs
        long originTo70Diff=Math.abs(gc.getTimeInMillis()/1000);
        gc.clear();
        gc.set(1980,Calendar.JANUARY,1,0,0,0);
        long originTo80Diff=Math.abs(gc.getTimeInMillis()/1000);
        /*
         * time difference ins seconds from
         * 1900 Jan 1
         * to
         * 1980 Jan 1
         */
        long timeDiff=originTo70Diff+originTo80Diff;
        // to store origin time
        long base_time=-1;
        long tau=-1;
        
        Section s=new Section(timeShape);
        Iterator i=s.getIterator(timeShape);
        while (i.hasNext()){
            int index=i.next();
            // days since 1900-01-01T00:00:00Z
            double time=timeArray.getDouble(index);
            //secs since 1900
            long secsSince=(long)(time*24*3600);
            // shift to 1980
            long new_time=(secsSince-timeDiff);
            // write the new value
            timeArray.setDouble(index, new_time);
            // store time origin
            if (base_time<0){
                base_time=(secsSince-originTo70Diff);
                tau=-new_time;
                if (LOGGER.isLoggable(Level.FINER)) {            
                    LOGGER.finer("BASE_TIME is: "+base_time);
                }
            }
            else if (tau<0){
                // tau is negative (lock above) 
                tau=(int)((new_time+tau)/3600);
                if (LOGGER.isLoggable(Level.FINER)) {            
                    LOGGER.finer("TAU is: "+tau);
                }
            }
            
            if (LOGGER.isLoggable(Level.FINER)) {            
                gc.clear();
                gc.setTimeInMillis((new_time+originTo80Diff)*1000);
                LOGGER.finer("Rebuild "+index+" time is: "+sdf.format(gc.getTime()));
            }
        }
        
        gc.clear();
        gc.setTimeInMillis(base_time*1000);
        
        // Setting global attribute
        timeVar.addAttribute(new Attribute("base_time",sdf.format(gc.getTime())));
        
        // write data as cached data into the dataset
        timeVar.setCachedData(timeArray, true);
        
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.finer("TIME: "+sdf.format(gc.getTime()));

        /*
        * Setting global attribute(s)
        */
        Group root=dataset.getRootGroup();
        dataset.addAttribute(root,new Attribute("base_time",sdf.format(gc.getTime())));
        dataset.addAttribute(root, new Attribute("time_origin", "seconds since 1980-1-1 0:0:0"));
        dataset.addAttribute(root, new Attribute("tau", tau));
      /*
       * TODO read from file:
       * which file?
       */
        dataset.addAttribute(root, new Attribute("nodata", -3.4e38));
        return dataset;
    }

}
