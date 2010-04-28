/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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

package it.geosolutions.geobatch.sas.esb;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import it.geosolutions.geobatch.sas.event.SASDetectionEvent;
import it.geosolutions.geobatch.sas.event.SASMosaicEvent;
import it.geosolutions.geobatch.sas.event.SASTileEvent;

import it.geosolutions.opensdi.sas.model.Leg;
import it.geosolutions.opensdi.sas.model.Mission;
import it.geosolutions.opensdi.sas.services.SASManagerService;
import it.geosolutions.opensdi.sas.services.exception.ResourceNotFoundFault;
import it.geosolutions.opensdi.sas.services.request.GetMissionRequest;
import it.geosolutions.opensdi.sas.services.request.InsertLegRequest;
import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

/**
 * Takes input either from a 
 * <LI>SASComposerAction: there will be MosaicEvents and TileEvents in the event queue<LI>
 * <LI>SASDetectionAction: there will be DetectionEvents in the event queue<LI>
 * 
 */
public class SasEsbAction
		extends BaseAction<FileSystemMonitorEvent> {

    private final static Logger LOGGER = Logger.getLogger(SasEsbAction.class.getName());

    public final static String SAS_STYLE = "sas";
    
    public final static String SAS_RAW_STYLE = "sasraw";
    
    public final static String DEFAULT_STYLE = "raster";
    
    
    private final static String NAMESPACE = "namespace";
    private final static String STORE = "store";
    private final static String LAYERNAME = "layername";

    private final SasEsbConfiguration configuration;

    public SasEsbAction(SasEsbConfiguration configuration)
            throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
		throws ActionException {
    	try {
    		listenerForwarder.started();

            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "Config is null.");
                throw new IllegalStateException("Config is null.");
            }
            
//            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
//                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

//            final String dataType = configuration.getDatatype();
            
            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            
//            final String inputFileName = workingDir.getAbsolutePath();
//            String baseFileName = null;
//            final String coverageStoreId;

//            if (dataType.equalsIgnoreCase("imagemosaic")){
//                coverageStoreId = FilenameUtils.getName(inputFileName);
//                checkMosaic(workingDir);
//            } else if (dataType.equalsIgnoreCase("geotiff")){
//            	coverageStoreId = FilenameUtils.getBaseName(inputFileName);
//                checkGeotiff(workingDir);
//            } else {
//            	LOGGER.log(Level.SEVERE,"Unsupported format type '" + dataType + "'");
//                return null;
//            }


            //== scan event queue
            // if called after a SASComposerAction: there will be MosaicEvents and TileEvents in the event queue
            // if called after a SASDetectionAction: there will be DetectionEvents in the event queue

            List<SASMosaicEvent> mosaicEvents = new ArrayList<SASMosaicEvent>();
            List<SASTileEvent> tileEvents = new ArrayList<SASTileEvent>();

            List<SASDetectionEvent> detectEvents = new ArrayList<SASDetectionEvent>();

            Queue<FileSystemMonitorEvent> unhandledEvents = new LinkedList<FileSystemMonitorEvent>();

            while(! events.isEmpty()) {
                FileSystemMonitorEvent event = events.remove();

                if(event instanceof SASMosaicEvent) {
                    mosaicEvents.add((SASMosaicEvent)event);
                    break;
                } else if (event instanceof SASTileEvent) {
                    tileEvents.add((SASTileEvent)event);
                } else if (event instanceof SASDetectionEvent) {
                    detectEvents.add((SASDetectionEvent)event);
                } else {
                    if(LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Skipping unhandled event " + event);
                    }
                    unhandledEvents.add(event);
                }
            }


            if(mosaicEvents.isEmpty()) { // uhm, what if in case of detections?
                throw new ActionException(this, "No " + SASMosaicEvent.class.getSimpleName() + " event in the input queue");
            }

            listenerForwarder.progressing(10,
                        "Scanned "
                        + mosaicEvents.size() + " mosaics, "
                        + tileEvents.size() + " tiles, "
                        + detectEvents.size() + " detections" );

            // TODO ============================================================
            // create missions, legs, tiles from events.........................

            // HOW SHALL WE LINK CONTACTS TO MISSIONS?

            Mission mission = new Mission();
//            mission.setCruiseExperiment(mosaicEvent.);
            mission.setName(mosaicEvents.get(0).getMissionName());
            mission.setSrs("EPSG:4326"); // FIXME
            
            if(! tileEvents.isEmpty())
                mission.setDate(tileEvents.get(0).getDate());

            Long missionId = insertMission(mission);
            Mission iMission = getMission(missionId); // this one should also have default attribs ok

            List<Long> legIds = new ArrayList<Long>();
            for (SASTileEvent tileEvent : tileEvents) {
                Leg leg = new Leg();
                leg.setMission(iMission);
                leg.setDate(tileEvent.getDate());
                Long id = insertLeg(leg);
                legIds.add(id);
            }



//            TileMetadata tileMetadata = new TileMetadata();
//            tileMetadata.set
//
//            SonarMosaic mosaic;
//            SonarTile sonarTile = new SonarTile();
//
//
//            Layer layer = new Layer();
//            layer.set;
            
            listenerForwarder.completed();
            return unhandledEvents;
        } catch (ActionException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            listenerForwarder.failed(e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            listenerForwarder.failed(e);
            throw new ActionException(this, e.getMessage(), e);
        }
    }

    protected Long insertMission(Mission mission) throws ActionException {
        try {
            Long missionId = getClient().insertMission(mission);
            return missionId;
        } catch (Exception e) {
            throw new ActionException(this, "Error inserting " + mission, e);
        }
    }

    protected Mission getMission(long id) throws ActionException {
        try {
            GetMissionRequest request = new GetMissionRequest();
            request.setId(id);
            return getClient().getMission(request);
        } catch (ResourceNotFoundFault ex) {
            throw new ActionException(this, "Mission not found: " + id, ex);
        }
    }

    protected Long insertLeg(Leg leg) throws ActionException {
        try {
            InsertLegRequest request = new InsertLegRequest();
            request.setLeg(leg);
            Long id = getClient().insertLeg(request);
            return id;
        } catch (ResourceNotFoundFault ex) {
            throw new ActionException(this, "Error inserting " + leg, ex);
        } catch (Exception ex) {
            throw new ActionException(this, "Error inserting " + leg, ex);
        }
    }

    private SASManagerService __client = null;
    protected synchronized SASManagerService getClient() {
        if(__client == null) {
            JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
            factory.getInInterceptors().add(new LoggingInInterceptor());
            factory.getOutInterceptors().add(new LoggingOutInterceptor());
            factory.setServiceClass(SASManagerService.class);
            factory.setAddress(configuration.getServerURL());
            factory.getServiceFactory().setDataBinding(new AegisDatabinding());
            __client = (SASManagerService) factory.create();
        }

        return __client;
    }


    public static void main(String[] args) throws IOException, ActionException {
        SasEsbConfiguration configuration = new SasEsbConfiguration();
        configuration.setServerURL("http://localhost:8888");

        SASMosaicEvent mosaicEvent = new SASMosaicEvent(new File("/tmp"));
        mosaicEvent.setFormat("mosaic");
        mosaicEvent.setMissionName("testMission");
        mosaicEvent.setWmsPath("testPath");

        Queue<FileSystemMonitorEvent> queue = new LinkedList<FileSystemMonitorEvent>();
        queue.add(mosaicEvent);

        SasEsbAction action = new SasEsbAction(configuration);
        action.execute(queue);
    }
}
