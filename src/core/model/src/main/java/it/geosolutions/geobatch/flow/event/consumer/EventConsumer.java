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
package it.geosolutions.geobatch.flow.event.consumer;

import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.flow.Job;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.misc.ListenerRegistry;

import java.util.EventObject;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * @author (r2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public interface EventConsumer<EO extends EventObject, ECC extends EventConsumerConfiguration>
        extends ListenerRegistry<IProgressListener>, Callable<Queue<EO>>, Job, Identifiable {
    /**
     * Clean up code for this {@link EventConsumer}.
     */
    public void dispose();

    /**
     * Retrieves the configuration for this {@link EventConsumer}.
     * 
     * @return the configuration for this {@link EventConsumer}.
     */
    public ECC getConfiguration();

    /**
     * Sets the configuration for this {@link EventConsumer}.
     * 
     * @param configuration
     *            to set for this {@link EventConsumer}.
     */
    public void setConfiguration(ECC configuration);

    /**
     * Retrieves the status for this  {@link EventConsumer}
     * @return  the status for this  {@link EventConsumer}
     */
    public EventConsumerStatus getStatus();

    /**
     * Tries to consume the provided event. In case the provided event cannot be consumed it return
     * false.
     * 
     * @param event
     *            The event to consume
     * @return <code>true</code> if we can consume the provided event, <code>false</code> otherwise.
     */
    public boolean consume(EO event);

    /**
     * Asks this {@link EventConsumer} to cancel its execution.
     * 
     */
    public void cancel();

    /**
     * Tells us whether or not this {@link EventConsumer} was asked to cancel its execution.
     * 
     * @return <code>true</code> in case this {@link EventConsumer} was asked to cancel its
     *         execution, <code>false</code> otherwise.
     */
    public boolean isCanceled();
    

}