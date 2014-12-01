/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2013 GeoSolutions S.A.S.
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

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerDetails;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.event.listeners.cumulator.CumulatingProgressListener;
import it.geosolutions.geobatch.flow.event.listeners.status.StatusProgressListener;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.services.rest.RESTFlowService;
import it.geosolutions.geobatch.services.rest.exception.BadRequestRestEx;
import it.geosolutions.geobatch.services.rest.exception.InternalErrorRestEx;
import it.geosolutions.geobatch.services.rest.exception.NotFoundRestEx;
import it.geosolutions.geobatch.services.rest.impl.runutils.FlowRunner;
import it.geosolutions.geobatch.services.rest.impl.utils.RESTUtils;
import it.geosolutions.geobatch.services.rest.model.RESTActionShort;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerList;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerShort;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus.Status;
import it.geosolutions.geobatch.services.rest.model.RESTFlow;
import it.geosolutions.geobatch.services.rest.model.RESTFlowList;
import it.geosolutions.geobatch.services.rest.model.RESTRunInfo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DamianoG
 * @author ETj (etj at geo-solutions.it)
 */
public class RESTFileBasedFlowServiceImpl implements RESTFlowService {

    private static Logger LOGGER = LoggerFactory.getLogger(RESTFileBasedFlowServiceImpl.class);
    
    private Catalog catalog;
    private DataDirHandler dataDirHandler;

    public RESTFileBasedFlowServiceImpl(){}
    
    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getFlowList()
     */
    @Override
    public RESTFlowList getFlowList() throws InternalErrorRestEx {
        
        return RESTUtils.convertFlowList(getAuthFlowManagers());
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getFlow(java.lang.String)
     */
    @Override
    public RESTFlow getFlow(String flowId) throws NotFoundRestEx, InternalErrorRestEx {

        FileBasedFlowManager flowManager = getAuthFlowManager(flowId);
        if(flowManager == null) {
            if(LOGGER.isInfoEnabled()){
                LOGGER.info("Flow not found: " + flowId);
            }
            throw new NotFoundRestEx("Flow not found: " + flowId);
        }

        return RESTUtils.convertFlow(flowManager);
    }
    
    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#run(java.lang.String, java.lang.Boolean, byte[])
     */
    @Override
    public String run(String flowId, Boolean fastfail, String fileName, byte[] data) throws BadRequestRestEx,
            InternalErrorRestEx {

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Running instance of flow " + flowId);
        }

        final FileBasedFlowManager flowMan = getAuthFlowManager(flowId);
        if(flowMan == null) {
            if(LOGGER.isInfoEnabled()) {
                LOGGER.info("Flow not found: " + flowId);
            }
            throw new NotFoundRestEx("Flow not found: " + flowId);
        }

        OutputStream os = null;
        BufferedOutputStream bos = null;
        File eventFile = null;

        try {
        	if(fileName == null || fileName.isEmpty()) {
        		fileName = "inputConfig" + System.currentTimeMillis() + ".tmp";
        	}

            // TODO: reconsider the usage of this temp dir: the consumer.flowinstancetempdir should be used,
            // or the watchdir. Creating such a dir will never be disposed automatically.
            eventFile = new File(flowMan.getFlowTempDir() + File.separator + fileName.toString() );
            LOGGER.warn("Creating temp input file " + eventFile + " . THIS FILE SHOULD BE PLACED SOMEWHERE ELSE");

            os = new FileOutputStream(eventFile);
            bos = new BufferedOutputStream(os);
            bos.write(data);
            bos.flush();
        }
        catch(Exception e){
            throw new InternalErrorRestEx(e.getLocalizedMessage());
        }
        finally{
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(os);
        }

        final FlowRunner fr = new FlowRunner(flowMan, dataDirHandler);
        FileBasedEventConsumer consumer = null;
        try {
            if(LOGGER.isInfoEnabled()){LOGGER.info("Creating new consumer for flow " + flowId);}
            consumer = fr.createConsumer();
            if(LOGGER.isInfoEnabled()){LOGGER.info("Starting the consumer " + flowId+"::"+consumer.getId());}

            fr.runConsumer(consumer.getId(), Collections.singletonList(eventFile));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new InternalErrorRestEx(e.getMessage());
        }
        return consumer.getId();
    }

    @Override
    public String runLocal(String flowId, Boolean fastfail, RESTRunInfo info) throws BadRequestRestEx, InternalErrorRestEx {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Running instance of flow " + flowId +" -- " + info);
        }

        if(info.getFileList() == null || info.size()==0) {
                throw new BadRequestRestEx("No file provided");
        }

        final FileBasedFlowManager flowMan = getAuthFlowManager(flowId);
        if(flowMan == null) {
            if(LOGGER.isInfoEnabled()) {
                LOGGER.info("Flow not found: " + flowId);
            }
            throw new NotFoundRestEx("Flow not found: " + flowId);
        }

        List<File> fileList = new ArrayList<File>();
        Set<String> filenames = new HashSet<String>(); // avoid name clash, files willb e copies
        for (String filepath : info) {
            File file = new File(filepath);
            if( ! file.exists() ) {
                LOGGER.warn("File not found " + filepath); // we don't want to expose this info outside
                throw new NotFoundRestEx(("File not found"));
            }
            // check for dups
            String name = file.getName();
            if(filenames.contains(name))
                throw new BadRequestRestEx("Duplicated names in list");
            filenames.add(name);

            fileList.add(file);
        }

        final FlowRunner fr = new FlowRunner(flowMan, dataDirHandler);
        FileBasedEventConsumer consumer = null;
        try {
            if(LOGGER.isInfoEnabled()){LOGGER.info("Creating new consumer for flow " + flowId);}
            consumer = fr.createConsumer();
            if(LOGGER.isInfoEnabled()){LOGGER.info("Starting the consumer " + flowId+"::"+consumer.getId());}

            List<File> copiedFiles = createWorkCopy(fileList, consumer.getFlowInstanceTempDir());

            fr.runConsumer(consumer.getId(), copiedFiles);
        } catch (InternalErrorRestEx e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new InternalErrorRestEx(e.getMessage());
        }
        return consumer.getId();
    }

    public static final String REST_INPUT_DIR = "rest_input";

    private static List<File> createWorkCopy(List<File> fileList, File flowInstanceTempDir) throws InternalErrorRestEx {
        File dstDir = new File(flowInstanceTempDir, REST_INPUT_DIR);
        List<File> ret = new ArrayList<File>(fileList.size());
        try {
            for (File file : fileList) {
                File dstFile = new File(dstDir, file.getName());
                FileUtils.copyFile(file, dstFile);
                ret.add(dstFile);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new InternalErrorRestEx("Error while copying files");
        }
        return ret;
    }



    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getFlowConsumers(java.lang.String)
     */
    @Override
    public RESTConsumerList getFlowConsumers(String flowId, boolean includeDetails) throws NotFoundRestEx,
            InternalErrorRestEx {
        
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Searching consumers for flow '" + flowId + "' ");
        }

        FileBasedFlowManager flowManager = getAuthFlowManager(flowId);
        if(flowManager == null) {
            if(LOGGER.isInfoEnabled()){
                LOGGER.info("Flow not found: " + flowId);
            }
            throw new NotFoundRestEx("Flow not found: " + flowId);
        }

        RESTConsumerList rcl = new RESTConsumerList();
        for(BaseEventConsumer bec : flowManager.getEventConsumers()){
            RESTConsumerShort rcs = new RESTConsumerShort();
            rcs.setUuid(bec.getId());
            rcs.setStatus(RESTUtils.convertStatus(bec.getStatus()));
            rcs.setStartDate(RESTUtils.formatDate(bec.getCreationTimestamp()));
            rcs.setDescription(bec.toString());
            if(includeDetails) {
	            EventConsumerDetails details = bec.getDetails();
	            rcs.setDetails(JSONSerializer.toJSON(details).toString());
            }
            rcl.add(rcs);
        }
        return rcl;
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getConsumerStatus(java.lang.String)
     */
    @Override
    public RESTConsumerStatus getConsumerStatus(String consumerId) throws NotFoundRestEx {
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Get consumer Status");
        }

        BaseEventConsumer bec = getConsumer(consumerId);

        RESTConsumerStatus rcs = new RESTConsumerStatus();
        rcs.setUuid(bec.getId());
        rcs.setStatus(RESTUtils.convertStatus(bec.getStatus()));
        rcs.setExtendedStatus(RESTUtils.getExtStatus(bec.getStatus()));
        if(bec.getStatus().equals(Status.FAIL)){
            rcs.setErrorMessage("property ErrorMessage: Not Implemented yet");
        }
        BaseAction<EventObject> currentAction = (BaseAction)bec.getCurrentAction();
        RESTActionShort lras = new RESTActionShort();
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
    public String getConsumerLog(String consumerId) throws NotFoundRestEx {

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Get consumer log");
        }

        BaseEventConsumer bec = getConsumer(consumerId);

        Collection<IProgressListener> coll = bec.getListeners();
        if (coll != null && coll.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("No listeners found for consumer " + consumerId);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (IProgressListener listener : coll) {
            if (listener == null) {
                continue;
            } else if (listener instanceof CumulatingProgressListener) {
                CumulatingProgressListener cpl = (CumulatingProgressListener) listener;
                for (String msg : cpl.getMessages()) {
                    sb.append("Consumer: ").append(msg).append("\n");
                }
            } else if (listener instanceof StatusProgressListener) {
                StatusProgressListener spl = (StatusProgressListener) listener;
                sb.append("Consumer status: ").append(spl.toString()).append("\n");
            } else if (listener instanceof ProgressListener) {
                ProgressListener anypl = (ProgressListener) listener;
                sb.append("Consumer action task: ").append(anypl.getTask()).append("\n");
                sb.append("Consumer action progress: ").append(anypl.getProgress()).append("%").append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#pauseConsumer(java.lang.String)
     */
    @Override
    public void pauseConsumer(String consumerId) {
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Pausing consumer " + consumerId );
        }

        BaseEventConsumer bec = getConsumer(consumerId);
        bec.pause();
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#resumeConsumer(java.lang.String)
     */
    @Override
    public void resumeConsumer(String consumerId) {
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Resuming consumer " + consumerId );
        }

        BaseEventConsumer bec = getConsumer(consumerId);
        bec.resume();
    }

    /**
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#cleanupConsumer(java.lang.String)
     */
    @Override
    public void cleanupConsumer(String consumerId) throws BadRequestRestEx {
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cleaning consumer " + consumerId );
        }

        ConsumerInfo consumerInfo = findConsumer(consumerId);
        if(consumerInfo == null) {
            if(LOGGER.isInfoEnabled()){
                LOGGER.info("Consumer not found: " + consumerId);
            }
            throw new NotFoundRestEx("Consumer not found: " + consumerId);
        }

        BaseEventConsumer bec = consumerInfo.bec;
        FileBasedFlowManager flowManager = consumerInfo.flowManager;

        EventConsumerStatus ecs = bec.getStatus();

        if (ecs.equals(EventConsumerStatus.COMPLETED) || ecs.equals(EventConsumerStatus.CANCELED)
                || ecs.equals(EventConsumerStatus.FAILED)) {
            flowManager.disposeConsumer(bec);
        } else {
            throw new BadRequestRestEx("Consumer not in a cleanable status");
        }
    }

    //========================================================================
    // Some private utils methods

    /**
     * TODO: implement authorization
     *
     * @return the list of the FlowManagers that the current user is allowed to access.
     */
    private List<FileBasedFlowManager> getAuthFlowManagers(){

        return RESTUtils.getFlowManagerList(catalog);
    }

    /**
     * TODO: implement authorization
     *
     * @return the FlowManager, or null if hte flowmangar does not exists or the user cannot access it,
     */
    private FileBasedFlowManager getAuthFlowManager(String flowId) {

        return RESTUtils.getFlowManager(catalog, flowId);
    }

    /**
     * @return the BaseEventConsumer
     * @throws NotFoundRestEx if consumer not found
     */
    private BaseEventConsumer getConsumer(String consumerId) throws NotFoundRestEx {

        ConsumerInfo consumerInfo = findConsumer(consumerId);
        if(consumerInfo == null) {
            if(LOGGER.isInfoEnabled()){
                LOGGER.info("Consumer not found: " + consumerId);
            }
            throw new NotFoundRestEx("Consumer not found: " + consumerId);
        }
        return consumerInfo.bec;
    }

    /**
     * @return the ConsumerInfo or null
     */
    private ConsumerInfo findConsumer(String consumerId) {
        List<FileBasedFlowManager> flowManagerList = getAuthFlowManagers();

        BaseEventConsumer bec = null;
        for (FileBasedFlowManager flowManager : flowManagerList) {
            bec = (BaseEventConsumer) flowManager.getConsumer(consumerId);
            if (bec != null) {
                ConsumerInfo ret = new ConsumerInfo();
                ret.bec = bec;
                ret.flowManager = flowManager;
                return ret;
            }
        }

        return null;
    }

    static class ConsumerInfo {
        BaseEventConsumer bec;
        FileBasedFlowManager flowManager;
    }

    //========================================================================
    // SETTERS

    public void setCatalog(Catalog catalog){
        this.catalog = catalog;
    }

    public void setDataDirHandler(DataDirHandler dataDirHandler){
        this.dataDirHandler = dataDirHandler;
    }


}
