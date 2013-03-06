/*
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

import it.geosolutions.geobatch.services.rest.RESTFlowService;
import it.geosolutions.geobatch.services.rest.exception.BadRequestRestEx;
import it.geosolutions.geobatch.services.rest.exception.InternalErrorRestEx;
import it.geosolutions.geobatch.services.rest.exception.NotFoundRestEx;
import it.geosolutions.geobatch.services.rest.model.RESTActionShort;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerList;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus;
import it.geosolutions.geobatch.services.rest.model.RESTFlow;
import it.geosolutions.geobatch.services.rest.model.RESTFlowList;
import it.geosolutions.geobatch.services.rest.model.RESTFlowShort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class RESTFlowServiceTestImpl
        implements RESTFlowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RESTFlowServiceTestImpl.class);

    // we'll probably need some gb service injected ....

    @Override
    public RESTFlowList getFlowList() throws InternalErrorRestEx {
        // TODO: this is dummy implementation
        LOGGER.warn("Dummy implementation");

        RESTFlowList list = new RESTFlowList();

        for (int i = 0; i < 3; i++) {
            RESTFlowShort flow = new RESTFlowShort();
            flow.setId("id"+i);
            flow.setName("name"+i);
            flow.setDescription("desc"+i);
            list.add(flow);
        }

        return list;
    }

    @Override
    public RESTFlow getFlow(String id) throws NotFoundRestEx, InternalErrorRestEx {
        // TODO: this is dummy implementation
        LOGGER.warn("Dummy implementation");

        RESTFlow flow = new RESTFlow();
        flow.setId("id");
        flow.setName("name");
        flow.setDescription("desc");

        for (int i = 0; i < 3; i++) {
            RESTActionShort action = new RESTActionShort();
            action.setId("id"+i);
            action.setName("name"+i);
            action.setDescription("desc"+i);
            flow.addAction(action);
        }
        return flow;
    }

    @Override
    public String run(String flowId, Boolean fastfail, byte[] data) throws BadRequestRestEx, InternalErrorRestEx {
        // TODO: this is dummy implementation
        LOGGER.warn("Dummy implementation");
        return "this-is-a-consumer-uuid-for-flow-"+flowId;
    }

    @Override
    public RESTConsumerStatus getConsumerStatus(String consumerId) {
        // TODO: this is dummy implementation
        LOGGER.warn("Dummy implementation");

        RESTConsumerStatus status = new RESTConsumerStatus();
        status = new RESTConsumerStatus();
        status.setUuid(consumerId);
        status.setStatus(RESTConsumerStatus.Status.SUCCESS);
        return status;
    }

    @Override
    public String getConsumerLog(String consumerId) {
        // TODO: this is dummy implementation
        LOGGER.warn("Dummy implementation");
        return "this is the first line\nthis is the 2nd line\n";
    }

    @Override
    public void pauseConsumer(String consumerId) {
        // TODO: this is dummy implementation
        LOGGER.warn("Dummy implementation");
    }

    @Override
    public void resumeConsumer(String consumerId) {
        // TODO: this is dummy implementation
        LOGGER.warn("Dummy implementation");
    }

    @Override
    public void cleanupConsumer(String consumerId) {
        // TODO: this is dummy implementation
        LOGGER.warn("Dummy implementation");
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.services.rest.RESTFlowService#getFlowConsumers(java.lang.String)
     */
    @Override
    public RESTConsumerList getFlowConsumers(String arg0) throws NotFoundRestEx,
            InternalErrorRestEx {
        // TODO Auto-generated method stub
        return null;
    }

}
