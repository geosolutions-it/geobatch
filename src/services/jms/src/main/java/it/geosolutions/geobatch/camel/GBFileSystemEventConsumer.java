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
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A wrapper for the FileBasedEventConsumer which implements
 * the consumeAll(FileSystemEvent ...) method.
 * 
 * As discussed on 4 Feb 2011 with:
 * Carlo Cancellieri
 * Simone Giannecchini
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class GBFileSystemEventConsumer extends FileBasedEventConsumer {

    public GBFileSystemEventConsumer(FileBasedEventConsumerConfiguration configuration, File flowConfigDir, File flowBaseTempDir)
            throws InterruptedException, IOException {
        super(configuration,flowConfigDir, flowBaseTempDir);
    }

    public boolean canConsumeAll(List<FileSystemEvent> fileEventList) throws IllegalArgumentException{
        if (fileEventList==null)
            return false;
        for (FileSystemEvent event : fileEventList) {
            super.consume(event);
        }
        if (super.getStatus() == EventConsumerStatus.EXECUTING) //can consume all the events
            return true;
        else
            return false;
    }

}
