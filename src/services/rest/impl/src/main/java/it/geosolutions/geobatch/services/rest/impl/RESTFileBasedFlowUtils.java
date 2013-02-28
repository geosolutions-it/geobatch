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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerList;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus.Status;

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

    static FileBasedFlowManager getFlowManagerFromConsumerId(String consumerId,
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

    static BaseEventConsumer getConsumer(String consumerId, List<FileBasedFlowManager> flowManagerList) {

        BaseEventConsumer bec = null;
        for (FileBasedFlowManager el : flowManagerList) {
            bec = (BaseEventConsumer) el.getConsumer(consumerId);
            if (bec != null) {
                break;
            }
        }
        return bec;
    }

    static List getConsumerList(String flowId, List<FileBasedFlowManager> flowManagerList) {

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

}
