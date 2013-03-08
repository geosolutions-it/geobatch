/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
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
package it.geosolutions.geobatch.services.rest.impl;

import static it.geosolutions.geobatch.services.rest.impl.RESTFileBasedFlowUtils.fromCalendarToString;
import static it.geosolutions.geobatch.services.rest.impl.RESTFileBasedFlowUtils.getConsumer;
import static it.geosolutions.geobatch.services.rest.impl.RESTFileBasedFlowUtils.getConsumerList;
import static it.geosolutions.geobatch.services.rest.impl.RESTFileBasedFlowUtils.getFlowManagerFromConsumerId;
import static it.geosolutions.geobatch.services.rest.impl.RESTFileBasedFlowUtils.toRESTConsumerStatus;
import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.event.generator.file.FileBasedEventGenerator;
import it.geosolutions.geobatch.flow.event.listeners.cumulator.CumulatingProgressListener;
import it.geosolutions.geobatch.flow.event.listeners.logger.LoggingProgressListener;
import it.geosolutions.geobatch.flow.event.listeners.status.StatusProgressListener;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.services.rest.RESTFlowService;
import it.geosolutions.geobatch.services.rest.exception.BadRequestRestEx;
import it.geosolutions.geobatch.services.rest.exception.InternalErrorRestEx;
import it.geosolutions.geobatch.services.rest.exception.NotFoundRestEx;
import it.geosolutions.geobatch.services.rest.model.RESTActionShort;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerList;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerShort;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus;
import it.geosolutions.geobatch.services.rest.model.RESTFlow;
import it.geosolutions.geobatch.services.rest.model.RESTFlowList;
import it.geosolutions.geobatch.services.rest.model.RESTFlowShort;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DamianoG
 *
 */
public class RESTFileBasedFlowServiceImpl implements RESTFlowService {

    private static Logger LOGGER = LoggerFactory.getLogger(RESTFileBasedFlowServiceImpl.class);
    
    private Catalog catalog;
    private List<FileBasedFlowManager> flowManagerList;
    private RESTFlowList flowsList;
    private Map<String,RESTFlow> flowsMap;
    
    public void setCatalog(Catalog catalog){
        this.catalog = catalog;
    }
    
    public RESTFileBasedFlowServiceImpl(){
        flowManagerList = new ArrayList<FileBasedFlowManager>();
        flowsList = new RESTFlowList();
        flowsMap = new HashMap<String, RESTFlow>();
    }
    
    public void init(){
        
        StringBuilder loggerString = new StringBuilder();
        if(LOGGER.isInfoEnabled()){LOGGER.info("Load needed data structures...");}
        flowManagerList = catalog.getFlowManagers(FileBasedFlowManager.class);
        List<RESTFlowShort> shortFlowsList = new ArrayList<RESTFlowShort>();
        
        loggerString.append("found ").append(flowManagerList.size()).append(" flow(s)...");
        if(LOGGER.isInfoEnabled()){LOGGER.info(loggerString.toString());}
        for(FileBasedFlowManager el : flowManagerList){
            if(LOGGER.isDebugEnabled()){LOGGER.debug("Working on flow '" + el.getId() + "'");}
            EventConsumerConfiguration ecc = null;
            if(el.getConfiguration()!=null && el.getConfiguration().getEventConsumerConfiguration()!=null){
                ecc = el.getConfiguration().getEventConsumerConfiguration();
            }
            else{
                StringBuilder sb = new StringBuilder();
                sb.append("Flow ").append(el.getId()).append(" configuration is not a valid geobatch configuration");
                throw new IllegalStateException();
            }
            if(LOGGER.isDebugEnabled()){LOGGER.debug("Flow " + el.getId() + ": add current flow to flowsMap");}
            RESTFlow rf = new RESTFlow();
            rf.setId(el.getId());
            rf.setName(el.getName());
            rf.setDescription(el.getDescription());
            List<RESTActionShort> actionList = new ArrayList<RESTActionShort>();
            for(ActionConfiguration el2 : ecc.getActions()){
                RESTActionShort ras = new RESTActionShort();
                ras.setId(el2.getId());
                ras.setName(el2.getName());
                ras.setDescription(el2.getDescription());
                actionList.add(ras);
            }
            rf.setActionList(actionList);
            flowsMap.put(el.getId(), rf);
            
            if(LOGGER.isDebugEnabled()){LOGGER.debug("Flow " + el.getId() + ": add current flow to shortFlowsList");}
            RESTFlowShort rfs = new RESTFlowShort();
            rfs.setId(el.getId());
            rfs.setName(el.getName());
            rfs.setDescription(el.getDescription());
            shortFlowsList.add(rfs);
        }
        flowsList.setList(shortFlowsList);
    }
    
    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getFlowList()
     */
    @Override
    public RESTFlowList getFlowList() throws InternalErrorRestEx {
        return flowsList;
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getFlow(java.lang.String)
     */
    @Override
    public RESTFlow getFlow(String id) throws NotFoundRestEx, InternalErrorRestEx {
        if(flowsMap == null || flowsMap.isEmpty()){
            StringBuilder sb = new StringBuilder();
            sb.append("An error occurs when search for Flow '").append(id).append("'. The Flow List is Null or Empty.");
            if(LOGGER.isInfoEnabled()){LOGGER.info(sb.toString());}
            throw new NotFoundRestEx(sb.toString());
        }
        RESTFlow fbfm = flowsMap.get(id);
        if(fbfm == null){
            StringBuilder sb = new StringBuilder();
            sb.append("Flow '").append(id).append("' not found.");
            if(LOGGER.isInfoEnabled()){LOGGER.info(sb.toString());}
            throw new NotFoundRestEx(sb.toString());
        }
        return fbfm;
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#run(java.lang.String, java.lang.Boolean, byte[])
     */
    @Override
    public String run(String flowId, Boolean fastfail, byte[] data) throws BadRequestRestEx,
            InternalErrorRestEx {
        OutputStream os = null;
        BufferedOutputStream bos = null;
        try{
            final FileBasedFlowManager fbfm =  catalog.getFlowManager(flowId, FileBasedFlowManager.class);
            final String watchDirRelativePath = ((FileBasedEventGeneratorConfiguration)(fbfm.getConfiguration().getEventGeneratorConfiguration())).getWatchDirectory();
            File watchDir = ((FileBasedEventGenerator)fbfm.getEventGenerator()).getWatchDirectory();
            
            if(watchDir != null && watchDir.canWrite() && watchDir.isDirectory()){
                StringBuffer fileName = new StringBuffer();
                fileName.append("inputConfig").append(System.currentTimeMillis());
                File temp = new File(fbfm.getFlowTempDir() + File.pathSeparator + fileName.toString() + ".tmp");
                os = new FileOutputStream(temp);
                bos = new BufferedOutputStream(os);
                bos.write(data);
                bos.flush();
                File input = new File(watchDir.getAbsolutePath().concat("/").concat(fileName.toString()).concat(".tif"));
                FileUtils.copyFile(temp, input);
            }
            else{
                if(LOGGER.isErrorEnabled()){LOGGER.error("The watch dir not exist or geobatch hasn't the ");}
            }
        }
        catch(Exception e){
            throw new InternalErrorRestEx(e.getLocalizedMessage());
        }
        finally{
            if(bos != null){
                try {
                    bos.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return "";
    }
    
    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getFlowConsumers(java.lang.String)
     */
    @Override
    public RESTConsumerList getFlowConsumers(String flowId) throws NotFoundRestEx,
            InternalErrorRestEx {
        List<FileBasedEventConsumer> consumerList = getConsumerList(flowId, flowManagerList);
        if(consumerList == null || consumerList.isEmpty()){
            StringBuilder sb = new StringBuilder();
            sb.append("An error occurs when search for Flow '").append(flowId).append("'. The Flow List is Null or Empty.");
            if(LOGGER.isInfoEnabled()){LOGGER.info(sb.toString());}
            throw new NotFoundRestEx(sb.toString());
        }
        RESTConsumerList rcl = new RESTConsumerList();
        for(FileBasedEventConsumer el : consumerList){
            RESTConsumerShort rcs = new RESTConsumerShort();
            rcs.setUuid(el.getId());
            rcs.setStatus(toRESTConsumerStatus(el.getStatus().toString()));
            rcs.setStartDate(fromCalendarToString(el.getCreationTimestamp()));
            rcl.add(rcs);
        }
        return rcl;
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getConsumerStatus(java.lang.String)
     */
    @Override
    public RESTConsumerStatus getConsumerStatus(String consumerId) {
        BaseEventConsumer bec = getConsumer(consumerId, flowManagerList);
        RESTConsumerStatus rcs = new RESTConsumerStatus();
        rcs.setUuid(bec.getId());
        rcs.setStatus(toRESTConsumerStatus(bec.getStatus().toString()));
        rcs.setErrorMessage("property ErrorMessage: Not Implemented yet");
        RESTActionShort lras = new RESTActionShort();
        BaseAction<EventObject> currentAction = (BaseAction)bec.getCurrentAction();
        lras.setId(currentAction.getId());
        lras.setName(currentAction.getName());
        lras.setDescription(currentAction.getDescription());
        rcs.setLatestAction(lras);
        return rcs;
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getConsumerLog(java.lang.String)
     */
    @Override
    public String getConsumerLog(String consumerId) {
        BaseEventConsumer bec = getConsumer(consumerId, flowManagerList);
        List<LoggingProgressListener> coll = (List) bec.getListeners(LoggingProgressListener.class);
        StringBuilder sb = new StringBuilder();
        for (IProgressListener listener : coll) {
            if (listener == null) {
                continue;
                // TODO warn
            } else if (listener instanceof CumulatingProgressListener) {
                CumulatingProgressListener cpl = (CumulatingProgressListener) listener;
                for (String msg : cpl.getMessages()) {
                    sb.append("Consumer: ").append(msg).append("<br />");
                }
            } else if (listener instanceof StatusProgressListener) {
                StatusProgressListener spl = (StatusProgressListener) listener;
                sb.append("Consumer status: ").append(spl.toString()).append("<br />");
            } else if (listener instanceof ProgressListener) {
                // get any pl
                ProgressListener anypl = (ProgressListener) listener;
                sb.append("Consumer action task: ").append(anypl.getTask()).append("<br />");
                sb.append("Consumer action progress: ").append(anypl.getProgress()).append("%").append("<br />");
            }
        }
        return sb.toString();
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#pauseConsumer(java.lang.String)
     */
    @Override
    public void pauseConsumer(String consumerId) {
        BaseEventConsumer bec = getConsumer(consumerId, flowManagerList);
        bec.pause();
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#resumeConsumer(java.lang.String)
     */
    @Override
    public void resumeConsumer(String consumerId) {
        BaseEventConsumer bec = getConsumer(consumerId, flowManagerList);
        bec.resume();
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#cleanupConsumer(java.lang.String)
     */
    @Override
    public void cleanupConsumer(String consumerId) {
        
        BaseEventConsumer bec = getConsumer(consumerId, flowManagerList);
        FileBasedFlowManager fbfm = getFlowManagerFromConsumerId(consumerId, flowManagerList);
        EventConsumerStatus ecs = bec.getStatus();

        if (ecs.equals(EventConsumerStatus.COMPLETED) || ecs.equals(EventConsumerStatus.CANCELED)
                || ecs.equals(EventConsumerStatus.FAILED)) {
            for (FileBasedFlowManager el : flowManagerList) {
                // dispose the object
                if (fbfm != null) {
                    fbfm.disposeConsumer(bec.getId());
                } else {
                    throw new IllegalArgumentException("ERROR: Consumer instance '" + bec.getId()
                            + "' not found");
                }
            }
        }
    }
    
    public void dispose(){
        catalog = null;
        flowManagerList = null;
        flowsList = null;
        flowsMap = null;
    }

   
}
