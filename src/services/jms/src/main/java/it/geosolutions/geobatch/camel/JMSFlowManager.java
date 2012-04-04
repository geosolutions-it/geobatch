/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
package it.geosolutions.geobatch.camel;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.camel.beans.JMSFlowRequest;
import it.geosolutions.geobatch.camel.beans.JMSFlowResponse;
import it.geosolutions.geobatch.camel.beans.JMSFlowStatus;
import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.InOut;
import org.apache.camel.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
@ManagedResource
public class JMSFlowManager implements AsyncProcessor {
    private final static Logger LOGGER = LoggerFactory.getLogger(JMSFlowManager.class.toString());
    
    private FileBasedEventConsumerConfiguration configuration;
    private FileBasedFlowManager parent;
    private GBFileSystemEventConsumer consumer;
    
    @Resource(name="dataDirHandler")
    private DataDirHandler dataDirHandler;
    

    private void init(String FlowManagerID) throws Exception {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JMSFlowManager: INIT catalog");
        }
        
        Catalog catalog = CatalogHolder.getCatalog();
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JMSFlowManager: INIT parent flow manager");
        }
        parent = catalog.getResource(FlowManagerID,
                it.geosolutions.geobatch.flow.file.FileBasedFlowManager.class);
        if (parent == null)
            throw new IllegalArgumentException("JMSFlowManager: The flow id \'" + FlowManagerID
                    + "\' do not exists into catalog... -> parent == null");

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JMSFlowManager: INIT configuration");
        }
        configuration = ((FileBasedEventConsumerConfiguration) parent.getConfiguration()
                .getEventConsumerConfiguration()).clone();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JMSFlowManager: INIT consumer");
        }
        File configDir=parent.initConfigDir(parent.getConfiguration(), dataDirHandler.getBaseConfigDirectory());
        File tempDir=parent.initTempDir(parent.getConfiguration(), dataDirHandler.getBaseTempDirectory());
        consumer = new GBFileSystemEventConsumer(configuration, configDir, tempDir);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JMSFlowManager: INIT injecting consumer to the parent flow");
        }
        parent.addConsumer(consumer);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JMSFlowManager: INIT concluded");
        }
    }

    /**
     * try to match the message to the configuration building the list of FileSystemEvent
     * 
     * @param files
     * @return the list of FileSystemEvent(s) or null (if the argument files list is null)
     */
    private List<FileSystemEvent> buildEventList(List<String> files)
            throws IllegalArgumentException {
        if (files == null)
            return null;

        List<FileSystemEvent> list = new ArrayList<FileSystemEvent>();
        for (String file : files) {


                File theFile = new File(file);
                // if not exists or not readable throw exception
                if (!theFile.exists() || !theFile.canRead())
                    throw new IllegalArgumentException("JMSFlowManager: The file \"" + theFile
                            + "\" not exists or is not readable.");

                // get the right event from the generator configuration
                FileSystemEventType ev = ((FileBasedEventGeneratorConfiguration) parent
                        .getConfiguration().getEventGeneratorConfiguration()).getEventType();
                // add the event to the list
                list.add(new FileSystemEvent(theFile, ev));
            }
        return list;
    }

    private JMSFlowResponse workOn(JMSFlowRequest request) throws Exception {
        try {
            // initializing members
            init(request.getFlowId());
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("JMSFlowManager: INIT error:" + e.getLocalizedMessage());
            }
            throw e;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JMSFlowManager: Initialized");
        }

        List<FileSystemEvent> fsel = buildEventList(request.getFiles());
        if (LOGGER.isInfoEnabled())
            LOGGER.info("JMSFlowManager: EventList Built...");

        if (consumer.canConsumeAll(fsel)) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("JMSFlowManager: Can consume");
            
            // using the Flow manager task executor
            Future<Queue<FileSystemEvent>> future = parent.getExecutor().submit(consumer);

            // waiting for the result
            Queue<FileSystemEvent> result = future.get();

            // building the response
            List<String> outList = new ArrayList<String>();
            for (FileSystemEvent fse : result) {
                File f = fse.getSource();
                if (f != null)
                    outList.add(f.getAbsolutePath());
                else
                    outList.add(null);
            }
            return new JMSFlowResponse(JMSFlowStatus.SUCCESS, outList);

        } else {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("JMSFlowManager: CanNOT consume");

            throw new IllegalArgumentException("JMSFlowManager: Unable to start the flow: "
                    + "message do not match flow configuration");
        }
    }

    @InOut
    public void process(Exchange exchange) throws Exception {

        if (LOGGER.isInfoEnabled())
            LOGGER.info("JMSFlowManager: will reply to ->"
                    + exchange.getIn().getHeader("JMSReplyTo").toString());

        Message in = exchange.getIn();
        JMSFlowResponse response = null;
        List<String> responses = null;

        try {
            JMSFlowRequest request = in.getBody(JMSFlowRequest.class);

            if (request == null) {
                throw new Throwable("Bad message type");
            } else {

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("JMSFlowManager: (" + exchange.getIn().getMessageId()
                            + ") STARTING PROCESSING");
                }
                
                // do the work
                response = workOn(request);
            }
        } catch (Throwable t) {
            if (response != null) {
                responses = response.getResponses();
            } else {
                responses = new ArrayList<String>(1);
            }
            String message = "JMSFlowManager (" + exchange.getIn().getMessageId() + "): ERROR: "
                    + t.getMessage();

            responses.add(message);

            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message);
            }

//TODO            // exchange.getIn().setFault(true);
//TODO            // exchange.setException(t);

        } finally {
            // some error occurred in workOn()!
            if (response == null) {
                if (responses == null)
                    responses = new ArrayList<String>(1);
                String message = "JMSFlowManager (" + exchange.getIn().getMessageId()
                        + "): Problem occurred during flow execution";
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn(message);
                responses.add(message);
                response = new JMSFlowResponse(JMSFlowStatus.FAILURE, responses);
            }

            in.setBody(response, JMSFlowResponse.class);
            // exchange.setOut(in);//TODO check needed?
            if (LOGGER.isInfoEnabled())
                LOGGER.info("JMSFlowManager: Process ends for message ID:"
                        + exchange.getIn().getMessageId());
        }
    }

    /**
     * The AsyncProcessor defines a single process() method which is very similar to it's
     * synchronous Processor.process() brethren. Here are the differences: A non-null AsyncCallback
     * MUST be supplied which will be notified when the exchange processing is completed. It MUST
     * not throw any exceptions that occurred while processing the exchange. Any such exceptions
     * must be stored on the exchange's Exception property. It MUST know if it will complete the
     * processing synchronously or asynchronously. The method will return true if it does complete
     * synchronously, otherwise it returns false. When the processor has completed processing the
     * exchange, it must call the callback.done(boolean sync) method. The sync parameter MUST match
     * the value returned by the process() method.
     * 
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     */
    public boolean process(Exchange exchange, AsyncCallback callback) {
        try {
            process(exchange);
        } catch (Throwable t) {
//TODO            // exchange.getIn().setFault(true);
//TODO            // exchange.setException(t);
        } finally {
            callback.done(true);
        }
        return true;
    }

    public void setDataDirHandler(DataDirHandler dataDirHandler) {
        this.dataDirHandler = dataDirHandler;
    }
    
}
