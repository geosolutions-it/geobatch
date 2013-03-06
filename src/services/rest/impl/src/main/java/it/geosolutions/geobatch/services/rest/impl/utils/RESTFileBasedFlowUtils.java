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
package it.geosolutions.geobatch.services.rest.impl.utils;

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.services.rest.model.RESTActionShort;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerList;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus.Status;
import it.geosolutions.geobatch.services.rest.model.RESTFlow;
import it.geosolutions.geobatch.services.rest.model.RESTFlowList;
import it.geosolutions.geobatch.services.rest.model.RESTFlowShort;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DamianoG
 * 
 */
public class RESTFileBasedFlowUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(RESTFileBasedFlowUtils.class);

    /**
     * 
     * @param status
     * @return
     */
    public static Status toRESTConsumerStatus(String status) {
        // Code here an awful switch that convert the input status into a RESTConsumerStatus.Status
        LOGGER.warn("dummy implementation");
        return Status.SUCCESS;
    }

    public static String fromCalendarToString(Calendar time) {
        // Code here an awful switch that convert the input status into a RESTConsumerStatus.Status
        LOGGER.warn("dummy implementation");
        return "dummy implementation";
    }

    public static List<FileBasedFlowManager> getFlowManagerList(Catalog catalog) {
        
        List<FileBasedFlowManager> fbfm = null;
        if(catalog != null){
            fbfm = catalog.getFlowManagers(FileBasedFlowManager.class);
        }
        if(fbfm == null){
            throw new IllegalStateException("The GB catalog is null or the requested resource 'FileBasedFlowManager' don't exist");
        }
        return fbfm;
    }

    public static FileBasedFlowManager getFlowManagerFromConsumerId(String consumerId,
            List<FileBasedFlowManager> flowManagerList) {

        FileBasedFlowManager fbfm = null;
        for (FileBasedFlowManager el : flowManagerList) {
            BaseEventConsumer bec = (BaseEventConsumer) el.getConsumer(consumerId);
            if (bec != null) {
                fbfm = el;
            }
        }
        return fbfm;
    }

    public static BaseEventConsumer getConsumer(String consumerId,
            List<FileBasedFlowManager> flowManagerList) {

        BaseEventConsumer bec = null;
        for (FileBasedFlowManager el : flowManagerList) {
            bec = (BaseEventConsumer) el.getConsumer(consumerId);
            if (bec != null) {
                break;
            }
        }
        return bec;
    }

    public static List getConsumerList(String flowId, List<FileBasedFlowManager> flowManagerList) {

        List<BaseEventConsumer> consumerList = new ArrayList<BaseEventConsumer>();
        FileBasedFlowManager currentFM = null;
        for (FileBasedFlowManager el : flowManagerList) {
            if (el.getId().equals(flowId)) {
                currentFM = el;
                break;
            }
        }
        if (currentFM == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Flow '" + flowId + "': don't exist!");
            }
            return consumerList;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Flow " + currentFM.getId() + ": retrieve consumers...");
        }
        RESTConsumerList rcl = new RESTConsumerList();
        Iterator<BaseEventConsumer> iter = currentFM.getEventConsumers().iterator();
        BaseEventConsumer bec = null;
        while (iter.hasNext()) {
            bec = iter.next();
            consumerList.add(bec);
        }
        return consumerList;
    }

    public static RESTFlowList getFlowsList(List<FileBasedFlowManager> flowManagerList) {

        StringBuilder loggerString = new StringBuilder();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Load needed data structures...");
        }
        List<RESTFlowShort> shortFlowsList = new ArrayList<RESTFlowShort>();
        loggerString.append("found ").append(flowManagerList.size()).append(" flow(s)...");

        RESTFlowList flowsList = new RESTFlowList();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(loggerString.toString());
        }

        for (FileBasedFlowManager el : flowManagerList) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Working on flow '" + el.getId() + "'");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Flow " + el.getId() + ": add current flow to shortFlowsList");
            }
            RESTFlowShort rfs = new RESTFlowShort();
            rfs.setId(el.getId());
            rfs.setName(el.getName());
            rfs.setDescription(el.getDescription());
            shortFlowsList.add(rfs);
        }
        flowsList.setList(shortFlowsList);

        return flowsList;
    }

    public static Map<String, RESTFlow> getFlowsMap(List<FileBasedFlowManager> flowManagerList) {

        StringBuilder loggerString = new StringBuilder();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Load needed data structures...");
        }

        Map<String, RESTFlow> flowsMap = new HashMap<String, RESTFlow>();

        loggerString.append("found ").append(flowManagerList.size()).append(" flow(s)...");
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(loggerString.toString());
        }
        for (FileBasedFlowManager el : flowManagerList) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Working on flow '" + el.getId() + "'");
            }
            EventConsumerConfiguration ecc = null;
            if (el.getConfiguration() != null
                    && el.getConfiguration().getEventConsumerConfiguration() != null) {
                ecc = el.getConfiguration().getEventConsumerConfiguration();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Flow ").append(el.getId())
                        .append(" configuration is not a valid geobatch configuration");
                throw new IllegalStateException();
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Flow " + el.getId() + ": add current flow to flowsMap");
            }
            RESTFlow rf = new RESTFlow();
            rf.setId(el.getId());
            rf.setName(el.getName());
            rf.setDescription(el.getDescription());
            List<RESTActionShort> actionList = new ArrayList<RESTActionShort>();
            for (ActionConfiguration el2 : ecc.getActions()) {
                RESTActionShort ras = new RESTActionShort();
                ras.setId(el2.getId());
                ras.setName(el2.getName());
                ras.setDescription(el2.getDescription());
                actionList.add(ras);
            }
            rf.setActionList(actionList);
            flowsMap.put(el.getId(), rf);
        }
        return flowsMap;
    }
}
