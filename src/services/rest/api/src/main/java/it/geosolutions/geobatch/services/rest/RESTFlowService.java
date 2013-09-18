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
package it.geosolutions.geobatch.services.rest;

import it.geosolutions.geobatch.services.rest.exception.BadRequestRestEx;
import it.geosolutions.geobatch.services.rest.exception.InternalErrorRestEx;
import it.geosolutions.geobatch.services.rest.exception.NotFoundRestEx;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerList;
import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus;
import it.geosolutions.geobatch.services.rest.model.RESTFlow;
import it.geosolutions.geobatch.services.rest.model.RESTFlowList;
import it.geosolutions.geobatch.services.rest.model.RESTRunInfo;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;

/**
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it)
 * @author DamianoG
 */
@Path("/")
public interface RESTFlowService {

    /**
     * Returns a List of flow that are available on the geobatch running instance.
     * Each flow is described by a RESTFlowShort object that provide to user the flowId, flowName and flowDescription.
     * 
     * @return A RESTFlowList object hold the short flow list. 
     * @throws InternalErrorRestEx
     */
    @GET
    @Path("/flows")
    @Produces(MediaType.APPLICATION_XML)
    RESTFlowList getFlowList() throws InternalErrorRestEx;

    /**
     * Returns a RESTFlow object that hold all the useful informations about a flow.
     * 
     * @param id the flowId provided by the getFlowList method.
     * @return A
     * @throws NotFoundRestEx
     * @throws InternalErrorRestEx
     */
    @GET
    @Path("/flows/{flowid}")
    @Produces(MediaType.APPLICATION_XML)
    RESTFlow getFlow(@PathParam("flowid") String id) throws NotFoundRestEx, InternalErrorRestEx;

    /**
     * Starts a flow copying the data provided into the watch dir of the flow.
     * 
     * @param flowId
     * @param fastfail
     * @param data It could be any file format.
     * @return
     * @throws BadRequestRestEx
     * @throws InternalErrorRestEx
     */
    @POST
    @Path("/flows/{flowid}/run")
    @Produces(MediaType.TEXT_PLAIN)
    String run(@PathParam("flowid") String flowId,
            @QueryParam("fastfail") Boolean fastfail,
            @Multipart("data") byte[]data)
                throws BadRequestRestEx, InternalErrorRestEx;

    /**
     * Starts a flow using the file list provided.
     *
     * The info param contains one or more filepaths that will be used to create the FileSystemEvents.
     *
     * <B>WARNING</B>: This operation exposes all system files accessible by GB, so we'll may want to implement
     * some sort of filtering to accessible resources.
     *
     * Since many actions are used to delete or manipulate the input files, the listed files will be copied in a work directory
     * and the process will be performed on the copied files.
     * <br/> To prevent filename clash when copying all the files, all the file should have different names.
     * Input files will not be renamed since the filename could have some extra info encoded.
     *
     * @param flowId
     * @param fastfail
     * @param info filepaths to be provided as FileSystemEvents to the Action's input queue
     * @return
     * @throws BadRequestRestEx
     * @throws InternalErrorRestEx
     * @throws NotFoundRestEx
     */
    @POST
    @Path("/flows/{flowid}/runlocal")
    @Produces(MediaType.TEXT_PLAIN)
    String runLocal(@PathParam("flowid") String flowId,
            @QueryParam("fastfail") Boolean fastfail,
            @Multipart("info") RESTRunInfo info)
                throws BadRequestRestEx, InternalErrorRestEx;

    /**
     * Returns all the consumers (with any state) for a given flow.
     * Each flow is described by a RESTConsumerShort object that provide to user the status, uuid, startDate attributes.
     * 
     * @param flowId
     * @return
     * @throws NotFoundRestEx
     * @throws InternalErrorRestEx
     */
    @GET
    @Path("/flows/{flowId}/consumers")
    @Produces(MediaType.APPLICATION_XML)
    RESTConsumerList getFlowConsumers(@PathParam("flowId") String flowId) throws NotFoundRestEx, InternalErrorRestEx;
    
    /**
     * Returns a RESTFlow object that hold all the useful informations about a flow.
     * 
     * @param consumerId
     * @return
     */
    @GET
    @Path("/consumers/{consumerid}/status")
    @Produces(MediaType.APPLICATION_XML)
    RESTConsumerStatus getConsumerStatus(@PathParam("consumerid") String consumerId) throws NotFoundRestEx;

    /**
     * Return a status log of the consumer
     * 
     * @param consumerId
     * @return
     */
    @GET
    @Path("/consumers/{consumerid}/log")
//    @Produces(MediaType.APPLICATION_XML)
    String getConsumerLog(@PathParam("consumerid") String consumerId) throws NotFoundRestEx;

    /**
     * Pause a running consumer
     * 
     */
    @PUT
    @Path("/consumers/{consumerid}/pause")
    void pauseConsumer(@PathParam("consumerid") String consumerId) throws NotFoundRestEx;

    /**
     * Resume a paused consumer
     * 
     */
    @PUT
    @Path("/consumers/{consumerid}/resume")
    void resumeConsumer(@PathParam("consumerid") String consumerId)  throws NotFoundRestEx;

    /**
     * Delete a consumer if it isn't in an active state.
     * 
     */
    @PUT
    @Path("/consumers/{consumerid}/clean")
    void cleanupConsumer(@PathParam("consumerid") String consumerId)  throws NotFoundRestEx, BadRequestRestEx ;

}
