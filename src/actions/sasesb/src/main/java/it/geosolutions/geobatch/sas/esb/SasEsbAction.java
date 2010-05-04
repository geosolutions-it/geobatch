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
import it.geosolutions.opensdi.sas.model.Contact;
import it.geosolutions.opensdi.sas.model.Leg;
import it.geosolutions.opensdi.sas.model.Mission;
import it.geosolutions.opensdi.sas.model.SonarMosaic;
import it.geosolutions.opensdi.sas.model.SonarTile;
import it.geosolutions.opensdi.sas.model.TileMetadata;
import it.geosolutions.opensdi.sas.services.SASManagerService;
import it.geosolutions.opensdi.sas.services.exception.ResourceNotFoundFault;
import it.geosolutions.opensdi.sas.services.request.GetLegRequest;
import it.geosolutions.opensdi.sas.services.request.GetMissionRequest;
import it.geosolutions.opensdi.sas.services.request.InsertLegRequest;
import it.geosolutions.opensdi.sas.services.request.InsertSonarMosaicRequest;
import it.geosolutions.opensdi.sas.services.request.InsertSonarTileRequest;
import it.geosolutions.opensdi.sas.services.response.Legs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    
//    private final static String NAMESPACE = "namespace";
//    private final static String STORE = "store";
//    private final static String LAYERNAME = "layername";

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

            if(mosaicEvents.isEmpty() && tileEvents.isEmpty() && detectEvents.isEmpty()) { // uhm, what if in case of detections?
                throw new ActionException(this, "No SASEvent event in the input queue");
            }

            listenerForwarder.progressing(10,
        			"Scanned "
        			+ mosaicEvents.size() + " mosaics, "
        			+ tileEvents.size() + " tiles, "
        			+ detectEvents.size() + " detections" );
            
            // ////////////////////////////////////////////////////////////////
            //
            // Processing Detecitons ...
            //
            // ////////////////////////////////////////////////////////////////
            if(!detectEvents.isEmpty()) {
            	for (SASDetectionEvent detection : detectEvents) {
            		String missionName = detection.getMissionName();
            		
            		Mission mission = null;
            		
            		try {
            			mission = getClient().getMissionByName(missionName);
            		} catch (Exception e) {
            			mission = null;
            		}
            		
            		if (mission == null) {
            			mission = new Mission();
            			mission.setName(missionName);
            			mission.setDate(new Date());
            			mission.setSrs(detection.getLayer().getNativeCRS());
            			
            			long missionId = getClient().insertMission(mission);
            			
            			if (missionId < 0) {
            				throw new ActionException(this, "Could not create the new Mission " + mission.getName());
            			}

            			mission.setId(missionId);
            		}
            		
            		// TODO: This will be replaced by ESB ...
            		if (detection.getLayer().getServerURL() == null) {
            			detection.getLayer().setServerURL(configuration.getGeoserverURL());
            		}
            		
            		Contact contact = new Contact();
            		contact.setDate(mission.getDate());
            		contact.setLayer(detection.getLayer());
					mission.setContact(contact);
					
					long missionId = getClient().updateMission(mission);

					if (missionId < 0) {
        				throw new ActionException(this, "Unable to update Mission " + mission.getName());
        			}
            	}
            } 

            // ////////////////////////////////////////////////////////////////
            //
            // Processing Tiles ...
            //
            // ////////////////////////////////////////////////////////////////
            if(!tileEvents.isEmpty()) {
            	for (SASTileEvent tile : tileEvents) {
            		String missionName = tile.getMissionName();
            		
            		Mission mission = null;
            		
            		try {
            			mission = getClient().getMissionByName(missionName);
            		} catch (Exception e) {
            			mission = null;
            		}
            		
            		if (mission == null) {
            			mission = new Mission();
            			mission.setName(missionName);
            			mission.setDate(new Date());
            			mission.setSrs(tile.getLayer().getNativeCRS());
            			
            			long missionId = getClient().insertMission(mission);
            			
            			if (missionId < 0) {
            				throw new ActionException(this, "Could not create the new Mission " + mission.getName());
            			}
            			
            			mission.setId(missionId);
            		}
            		
            		// TODO: This will be replaced by ESB ...
            		if (tile.getLayer().getServerURL() == null) {
            			tile.getLayer().setServerURL(configuration.getGeoserverURL());
            		}
            		
            		GetMissionRequest missionRequest = new GetMissionRequest();
            		missionRequest.setId(mission.getId());
					Legs legs = getClient().getLegs(missionRequest);
					
					long legId = -1;

					if (legs != null && legs.getLegs() != null) {
						for (Leg leg : legs.getLegs()) {
							if (leg.getName().equals(tile.getLegNames().get(0))) {
								legId = leg.getId();
								break;
							}
						}
					}
					
					if (legId < 0) {
						Leg leg = new Leg();
						leg.setName(tile.getLegNames().get(0));
						leg.setMission(mission);
						leg.setDate(mission.getDate());
						
						InsertLegRequest legRequest = new InsertLegRequest();
						legRequest.setId(mission.getId());
						legRequest.setLeg(leg);
						legId = getClient().insertLeg(legRequest);
					}

					if (legId < 0) {
						throw new ActionException(this, "Could not create/find a Leg for Mission " + mission.getName());
					}

					InsertSonarTileRequest tileRequest = new InsertSonarTileRequest();
					SonarTile sonarTile = new SonarTile();
					sonarTile.setLayer(tile.getLayer());
					GetLegRequest legRequest = new GetLegRequest();
            		legRequest.setLegId(legId);
            		legRequest.setMissionId(mission.getId());
					sonarTile.setLeg(getClient().getLeg(legRequest));
					sonarTile.setName(tile.getLayer().getName());
					TileMetadata tileMetadata = new TileMetadata();
					tileMetadata.setPath(tile.getLayer().getFileURL());
					sonarTile.setTileMetadata(tileMetadata);
					sonarTile.setType(tile.getType());
					tileRequest.setId(legId);
					tileRequest.setSonarTile(sonarTile);
					if (getClient().insertTile(tileRequest) < 0) {
						throw new ActionException(this, "Could not create Tile " + sonarTile.getName() + " for Mission " + mission.getName());
					}
            	}
            }
            	
            // ////////////////////////////////////////////////////////////////
            //
            // Processing Mosaics ...
            //
            // ////////////////////////////////////////////////////////////////
            if(!mosaicEvents.isEmpty()) {
            	for (SASMosaicEvent mosaic : mosaicEvents) {
            		String missionName = mosaic.getMissionName();
            		
            		Mission mission = null;
            		
            		try {
            			mission = getClient().getMissionByName(missionName);
            		} catch (Exception e) {
            			mission = null;
            		}
            		
            		if (mission == null) {
            			mission = new Mission();
            			mission.setName(missionName);
            			mission.setDate(new Date());
            			mission.setSrs(mosaic.getLayer().getNativeCRS());
            			
            			long missionId = getClient().insertMission(mission);
            			
            			if (missionId < 0) {
            				throw new ActionException(this, "Could not create the new Mission " + mission.getName());
            			}
            			
            			mission.setId(missionId);
            		}
            		
            		// TODO: This will be replaced by ESB ...
            		if (mosaic.getLayer().getServerURL() == null) {
            			mosaic.getLayer().setServerURL(configuration.getGeoserverURL());
            		}
            		
            		GetMissionRequest missionRequest = new GetMissionRequest();
            		missionRequest.setId(mission.getId());
					Legs legs = getClient().getLegs(missionRequest);
					
					long legId = -1;
					
					if (legs != null && legs.getLegs() != null) {
						for (Leg leg : legs.getLegs()) {
							if (leg.getName().equals(mosaic.getLegNames().get(0))) {
								legId = leg.getId();
								break;
							}
						}
					}
					
					if (legId < 0) {
						Leg leg = new Leg();
						leg.setName(mosaic.getLegNames().get(0));
						leg.setMission(mission);
						leg.setDate(mission.getDate());
						
						InsertLegRequest legRequest = new InsertLegRequest();
						legRequest.setId(mission.getId());
						legRequest.setLeg(leg);
						legId = getClient().insertLeg(legRequest);
					}

					if (legId < 0) {
						throw new ActionException(this, "Could not create/find a Leg for Mission " + mission.getName());
					}

            		InsertSonarMosaicRequest mosaicRequest = new InsertSonarMosaicRequest();
            		mosaicRequest.setId(legId);
            		SonarMosaic sonarMosaic = new SonarMosaic();
            		sonarMosaic.setDate(mission.getDate());
            		GetLegRequest legRequest = new GetLegRequest();
            		legRequest.setLegId(legId);
            		legRequest.setMissionId(mission.getId());
					sonarMosaic.setLeg(getClient().getLeg(legRequest));
            		sonarMosaic.setLayer(mosaic.getLayer());
            		sonarMosaic.setType(mosaic.getType());
					mosaicRequest.setSonarMosaic(sonarMosaic);
					if (getClient().insertSonarMosaic(mosaicRequest) < 0) {
						throw new ActionException(this, "Could not create Mosaic " + sonarMosaic.getLayer().getName() + " for Mission " + mission.getName());
					}
            	}
            }
            
            listenerForwarder.completed();
            events.clear();
            return events;
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
            //factory.getServiceFactory().setDataBinding(new AegisDatabinding());
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
