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
package it.geosolutions.geobatch.services.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import it.geosolutions.geobatch.services.rest.exception.BadRequestRestEx;
import it.geosolutions.geobatch.services.rest.exception.InternalErrorRestEx;
import it.geosolutions.geobatch.services.rest.exception.NotFoundRestEx;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerList;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus;
import it.geosolutions.geobatch.services.rest.model.RESTFlow;
import it.geosolutions.geobatch.services.rest.model.RESTFlowList;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;

/**
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it)
 */
@Path("/")
public interface RESTFlowService {

    /**
     */
    @GET
    @Path("/flow")
    @Produces(MediaType.APPLICATION_XML)
    RESTFlowList getFlowList() throws InternalErrorRestEx;

    /**
     */
    @GET
    @Path("/flow/{id}")
    @Produces(MediaType.APPLICATION_XML)
    RESTFlow getFlow(@PathParam("id") String id) throws NotFoundRestEx, InternalErrorRestEx;

    /**
     */
    @POST
    @Path("/flow/{flowid}")
//    @Produces(MediaType.APPLICATION_XML)
    String run(@PathParam("flowid") String flowId,
            @QueryParam("fastfail") Boolean fastfail,
            @Multipart("data") byte[]data)
                throws BadRequestRestEx, InternalErrorRestEx;

    /**
     */
    @GET
    @Path("/flow/{flowId}")
    @Produces(MediaType.APPLICATION_XML)
    RESTConsumerList getFlowConsumers(@PathParam("flowId") String flowId) throws NotFoundRestEx, InternalErrorRestEx;
    
    /**
     */
    @GET
    @Path("/consumer/{consumerid}/status")
    @Produces(MediaType.APPLICATION_XML)
    RESTConsumerStatus getConsumerStatus(@PathParam("consumerid") String consumerId);

    /**
     */
    @GET
    @Path("/consumer/{consumerid}/log")
//    @Produces(MediaType.APPLICATION_XML)
    String getConsumerLog(@PathParam("consumerid") String consumerId);

    /**
     */
    @PUT
    @Path("/consumer/{consumerid}/pause")
    void pauseConsumer(@PathParam("consumerid") String consumerId);

    /**
     */
    @PUT
    @Path("/consumer/{consumerid}/resume")
    void resumeConsumer(@PathParam("consumerid") String consumerId);

    /**
     */
    @PUT
    @Path("/consumer/{consumerid}/clean")
    void cleanupConsumer(@PathParam("consumerid") String consumerId);

}
