/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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

package it.geosolutions.geobatch.flow;

import it.geosolutions.geobatch.catalog.Descriptable;
import it.geosolutions.geobatch.catalog.PersistentResource;
import it.geosolutions.geobatch.configuration.flow.FlowConfiguration;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.generator.EventGenerator;

import java.util.Collection;
import java.util.EventObject;

/**
 * @author  Alessio Fabiani
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public interface FlowManager<EO extends EventObject, FC extends FlowConfiguration> 
    extends PersistentResource<FC>, Job, Descriptable {

    public boolean isRunning();

    public void reset();

    public void dispose();

    public EventGenerator<EO> getEventGenerator();

    public void setEventGenerator(EventGenerator<EO> eventGenerator);
    
    /**
     * Remove the given consumer instance from the ones handled by this flow.
     * <P>
     * It should only be used on instances that are not running, i.e. in a
     * COMPLETED or FAILED state.
     * 
     * @param fbec the consumer to be removed.
     * @throws IllegalArgumentException if param is null 
     */
    public void disposeConsumer(String uuid) throws IllegalArgumentException;
    
    /**
     * @return an unmodifiable Collection of all the consumers
     * @throws IllegalArgumentException if param is null 
     */
    public Collection<EventConsumer> getEventConsumers();

    /**
     * @return an unmodifiable Collection<String> of all the consumers id
     */
    public Collection<String> getEventConsumersId();

    /**
     * 
     * return the consumer instance with the passed uuid or null
     * 
     * @param uuid the uuid matching the consumer
     * @return The selected event consumer or null
     * @throws IllegalArgumentException if param is null
     * 
     * @see {@link FlowManager#getEventConsumersId()}
     */
    public EventConsumer getConsumer(final String uuid) throws IllegalArgumentException;

    /**
     * we don't want to manipulate the list externally. please enforce this.
     * 
     * @param consumer
     * @return true if consumer is successfully added to the consumers container, false otherwise
     * @throws IllegalArgumentException if param is null 
     */
    public boolean addConsumer(EventConsumer consumer)  throws IllegalArgumentException;

    /**
     * 
     * @param uuid the uid of the consumer to check for status
     * @return the status of the selected consumer or null if consumer is not
     *         found
     */
    public EventConsumerStatus getStatus(final String uuid);

    /**
     * Remove from the consumers map at least a 'quantity' of completed, failed
     * or canceled consumers. This method is thread-safe
     * 
     * @return the number of purged of the
     * 
     */
    public int purgeConsumers(int quantity);

    /**
     * Post an event to the flow
     */
    public void postEvent(EO event);
}
