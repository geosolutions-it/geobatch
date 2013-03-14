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
package it.geosolutions.geobatch.services.rest.impl.runutils;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DamianoG
 * 
 *         This class provides methods for create consumers, create events and starts the consumer,
 * 
 *         This class has been developed in order to support the start flow method but this code could be usefull also in other context... Externalize
 *         it in a core module?
 * 
 * 
 */
public class FlowRunner {

    private static Logger LOGGER = LoggerFactory.getLogger(FlowRunner.class);

    private FileBasedFlowManager flowManager;

    private DataDirHandler dataDirHandler;

    public FlowRunner(FileBasedFlowManager fbfm, DataDirHandler dataDirHandler) {

        this.flowManager = fbfm;
        this.dataDirHandler = dataDirHandler;
    }

    public String createConsumer() throws Exception {

        if (flowManager == null || flowManager.getConfiguration() == null) {
            throw new IllegalArgumentException("Unable to work with null configuration");
        }

        EventConsumerConfiguration ecc = flowManager.getConfiguration()
                .getEventConsumerConfiguration();

        final List<ActionConfiguration> actions = (List<ActionConfiguration>) ecc.getActions();

        final FileBasedEventConsumerConfiguration consumerConfig = new FileBasedEventConsumerConfiguration(
                "RESTCreatedConsumer");

        consumerConfig.setActions(actions);

        // TODO may we want to remove only when getStatus is remotely called???
        // consumerConfig.setKeepContextDir(true);

        // if you want to move the input you may call the action move!
        consumerConfig.setPreserveInput(true);

        File configDir = FileBasedFlowManager.initConfigDir(flowManager.getConfiguration(),
                dataDirHandler.getBaseConfigDirectory());
        File tempDir = FileBasedFlowManager.initTempDir(flowManager.getConfiguration(),
                dataDirHandler.getBaseTempDirectory());
        final FileBasedEventConsumer consumer = new FileBasedEventConsumer(consumerConfig,
                configDir, tempDir);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("INIT injecting consumer to the parent flow. UUID: " + consumer.getId());
        }

        // Add to the consumer map
        if (!flowManager.addConsumer(consumer)) {
            consumer.dispose();
            throw new IllegalStateException(
                    "Unable to add another consumer, consumer queue is full. "
                            + "Please dispose some completed consumer before submit a new one.");
        }

        return consumer.getId();
    }

    public void runConsumer(String uuid, File event) throws Exception {

        if (uuid == null || event == null) {
            throw new IllegalArgumentException("Unable to run using null arguments: uuid=" + uuid
                    + " event=" + event);
        }

        EventConsumer consumer = getConsumer(uuid);

        consumer.consume(new FileSystemEvent(event, FileSystemEventType.FILE_ADDED));

        // Run consumer
        flowManager.getExecutor().submit(consumer);
    }

    private EventConsumer getConsumer(String uuid) throws IllegalArgumentException {
        final EventConsumer consumer = flowManager.getConsumer(uuid);
        if (consumer == null)
            throw new IllegalArgumentException("Unable to get a consumer using uuid: " + uuid);
        return consumer;
    }

}
