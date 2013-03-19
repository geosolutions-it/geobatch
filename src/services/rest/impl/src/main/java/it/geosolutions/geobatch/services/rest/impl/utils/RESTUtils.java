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
package it.geosolutions.geobatch.services.rest.impl.utils;

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus.Status;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.services.rest.model.RESTActionShort;
import it.geosolutions.geobatch.services.rest.model.RESTFlow;
import it.geosolutions.geobatch.services.rest.model.RESTFlowList;
import it.geosolutions.geobatch.services.rest.model.RESTFlowShort;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DamianoG
 * @author ETj
 */
public class RESTUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(RESTUtils.class);

    private final static Map<EventConsumerStatus, RESTConsumerStatus.Status> statusMapping;
    static {
        Map<EventConsumerStatus, RESTConsumerStatus.Status> tmp = new EnumMap<EventConsumerStatus, RESTConsumerStatus.Status>(EventConsumerStatus.class);
        tmp.put(EventConsumerStatus.CANCELED, Status.FAIL);
        tmp.put(EventConsumerStatus.COMPLETED, Status.SUCCESS);
        tmp.put(EventConsumerStatus.EXECUTING, Status.RUNNING);
        tmp.put(EventConsumerStatus.FAILED, Status.FAIL);
        tmp.put(EventConsumerStatus.IDLE, null); // should not happen
        tmp.put(EventConsumerStatus.PAUSED, Status.RUNNING);
        tmp.put(EventConsumerStatus.WAITING, null); // should not happen

        statusMapping = Collections.unmodifiableMap(tmp);
    }

    /** giving some more details about the internal status */
    private final static Map<EventConsumerStatus, String> extStatusMapping;
    static {
        Map<EventConsumerStatus, String> tmp = new EnumMap<EventConsumerStatus, String>(EventConsumerStatus.class);
        tmp.put(EventConsumerStatus.CANCELED, EventConsumerStatus.CANCELED.name()); 
        tmp.put(EventConsumerStatus.COMPLETED, null); // direct mapping, no details needed
        tmp.put(EventConsumerStatus.EXECUTING, null); // direct mapping, no details needed
        tmp.put(EventConsumerStatus.FAILED,  null); // direct mapping, no details needed
        tmp.put(EventConsumerStatus.IDLE, EventConsumerStatus.IDLE.name()); // should not happen
        tmp.put(EventConsumerStatus.PAUSED, EventConsumerStatus.PAUSED.name());
        tmp.put(EventConsumerStatus.WAITING, EventConsumerStatus.WAITING.name()); // should not happen

        extStatusMapping = Collections.unmodifiableMap(tmp);
    }

    /**
     * @param status
     * @return
     */
    public static Status convertStatus(EventConsumerStatus status) {
        Status ret = statusMapping.get(status);

        if(ret == null) {
            LOGGER.error("Event is in status " + status + ". It should not happen.");
            ret = Status.FAIL;
        }
        
        return ret;
    }

    public static String getExtStatus(EventConsumerStatus status) {
        return extStatusMapping.get(status);
    }

    public static String formatDate(Calendar time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss'.'SSS");
        TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
        formatter.setTimeZone(TZ_UTC);
        return formatter.format(time.getTime());
    }

    public static List<FileBasedFlowManager> getFlowManagerList(Catalog catalog) {
        
        if(catalog == null) {
            throw new IllegalStateException("The catalog is null");
        }

       List<FileBasedFlowManager> fbfm = catalog.getFlowManagers(FileBasedFlowManager.class);

        if(fbfm == null) { // should not happen
            throw new IllegalStateException("No flowManagers found");
        }

        return fbfm;
    }

    /**
     * @return the FlowManager or null
     */
    public static FileBasedFlowManager getFlowManager(Catalog catalog, String id) {

        if(catalog == null) {
            throw new IllegalStateException("The catalog is null");
        }

       FileBasedFlowManager ret = catalog.getFlowManager(id, FileBasedFlowManager.class);

        return ret;
    }

    public static RESTFlowList convertFlowList(List<FileBasedFlowManager> flowManagerList) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Load needed data structures...");
        }
                
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Found " + flowManagerList.size() + " flow(s)");
        }

        RESTFlowList flowsList = new RESTFlowList();

        for (FileBasedFlowManager flowManager : flowManagerList) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Examining flow '" + flowManager.getId() + "'");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Flow " + flowManager.getId() + ": add current flow to shortFlowsList");
            }
            
            RESTFlowShort flow = new RESTFlowShort();
            flow.setId(flowManager.getId());
            flow.setName(flowManager.getName());
            flow.setDescription(flowManager.getDescription());
            flowsList.add(flow);
        }

        return flowsList;
    }


    public static RESTFlow convertFlow(FileBasedFlowManager flowManager) throws IllegalStateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Converting flow '" + flowManager.getId() + "'");
        }
        EventConsumerConfiguration ecc = null;
        if (flowManager.getConfiguration() != null
                && flowManager.getConfiguration().getEventConsumerConfiguration() != null) {
            ecc = flowManager.getConfiguration().getEventConsumerConfiguration();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Flow ").append(flowManager.getId())
                    .append(" configuration is not a valid geobatch configuration");
            throw new IllegalStateException(sb.toString());
        }
        
        RESTFlow flow = new RESTFlow();
        flow.setId(flowManager.getId());
        flow.setName(flowManager.getName());
        flow.setDescription(flowManager.getDescription());

        for (ActionConfiguration action : ecc.getActions()) {
            RESTActionShort actionShort = new RESTActionShort();
            actionShort.setId(action.getId());
            actionShort.setName(action.getName());
            actionShort.setDescription(action.getDescription());
            flow.addAction(actionShort);
        }

        return flow;
    }
}
