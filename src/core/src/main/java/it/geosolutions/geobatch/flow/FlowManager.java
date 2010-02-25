/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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



package it.geosolutions.geobatch.flow;

import it.geosolutions.geobatch.catalog.PersistentResource;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.flow.FlowConfiguration;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.generator.EventGenerator;

import java.io.File;
import java.util.EventObject;
import java.util.List;

/**
 * @author Alessio Fabiani
 */
public interface FlowManager<T extends EventObject, C extends FlowConfiguration> extends
        PersistentResource<C> {
    /**
     * The Flow BaseEventConsumer identifier.
     */
    public void setName(String name);

    public EventGenerator<T> getEventGenerator();

    /**
     * Output Directory
     */
    public void setWorkingDirectory(File workingDir);

    /**
     *
     */
    public boolean isRunning();

    /**
     *
     */
    public void resume();

    /**
     *
     */
    public void pause();

    /**
     *
     */
    public void reset();

    /**
     *
     */
    public void dispose();

    /**
     * Output Directory
     */
    public File getWorkingDirectory();

    public void setEventGenerator(EventGenerator<T> eventGenerator);
    
    public <EC extends EventConsumerConfiguration>  List<EventConsumer<T,EC>> getEventConsumers();
}
