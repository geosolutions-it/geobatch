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

package it.geosolutions.geobatch.flow.event.consumer.file;

import java.io.File;
import java.io.IOException;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileBasedEventConsumerService extends BaseService
{

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedEventConsumerService.class);

    public FileBasedEventConsumerService(String id, String name, String description)
    {
        super(id, name, description);
    }

    public boolean canCreateEventConsumer(FileBasedEventConsumerConfiguration configuration)
    {
        return true;
    }

    public FileBasedEventConsumer createEventConsumer(
        FileBasedEventConsumerConfiguration configuration)
    {
        throw new UnsupportedOperationException("Could not create consumer " + configuration.getId());
//        try
//        {
//            return new FileBasedEventConsumer(configuration, );
//        }
//        catch (IOException e)
//        {
//            if (LOGGER.isErrorEnabled())
//            {
//                LOGGER.error(e.getLocalizedMessage(), e);
//            }
//        }
//        catch (InterruptedException e)
//        {
//            if (LOGGER.isErrorEnabled())
//            {
//                LOGGER.error(e.getLocalizedMessage(), e);
//            }
//        }
//
//        return null;
    }

}
