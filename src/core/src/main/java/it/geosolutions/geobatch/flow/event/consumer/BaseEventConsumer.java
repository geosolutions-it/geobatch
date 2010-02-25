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



package it.geosolutions.geobatch.flow.event.consumer;

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.catalog.impl.BaseResource;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public abstract class BaseEventConsumer<EO extends EventObject, ECC extends EventConsumerConfiguration>
        extends BaseResource
		implements Runnable, EventConsumer<EO, ECC> {

    /**
     */
    private volatile EventConsumerStatus eventConsumerStatus;

    /**
     * The MailBox
     */
    protected final Queue<EO> eventsQueue = new LinkedList<EO>();

    protected final List<Action<EO>> actions = new ArrayList<Action<EO>>();

	private EventListenerList listeners = new EventListenerList();

    // ----------------------------------------------- PRIVATE ATTRIBUTES
    /**
     * Private Logger
     */
    private static Logger LOGGER = Logger.getLogger(BaseEventConsumer.class.toString());

    public BaseEventConsumer() {
        super();
        this.setStatus(EventConsumerStatus.IDLE);
    }

    public BaseEventConsumer(String id, String name, String description, Catalog catalog) {
        super(id, name, description, catalog);

        this.setStatus(EventConsumerStatus.IDLE);
    }

    public BaseEventConsumer(String id, String name, String description) {
        super(id, name, description);

    }

    /**
     * Default Constructor
     * 
     * @param catalog
     */
    public BaseEventConsumer(Catalog catalog) {
        super(catalog);
        this.setStatus(EventConsumerStatus.IDLE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.flow.event.consumer.EventConsumer#getStatus()
     */
    public EventConsumerStatus getStatus() {
        return this.eventConsumerStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.flow.event.consumer.EventConsumer#setStatus(it
     * .geosolutions.geobatch .flow.event.consumer.Status)
     */
    protected void setStatus(EventConsumerStatus eventConsumerStatus) {

        // //
        // now changing eventConsumerStatus
        // //
        this.eventConsumerStatus = eventConsumerStatus;

    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.flow.event.consumer.EventConsumer#put(it.geosolutions
     * .filesystemmonitor .monitor.FileSystemMonitorEvent)
     */
    public boolean consume(EO event) {
        if (!eventsQueue.offer(event))
            return false;

        return true;
    }

    /**
     * Once the configuring state has been successfully passed, by collecting all the necessary
     * Events, the BaseEventConsumer invokes this method in order to produce the DTOs. DTOs
     * represent the Java beans used by the Catalog BaseEventConsumer to configure GeoServer.
     * 
     * @param event
     * @return
     * 
     * @throws InterruptedException
     * @throws InitializationException
     */
    protected boolean applyActions(Queue<EO> events) {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "Applying " + actions.size() + " actions on "
					+ events.size() + " events.");

		// apply all the actions
        for (Action<EO> action : this.actions) {
            try {
                events = action.execute(events);
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                events.clear();
            }
            if (events == null || events.isEmpty())
                return false;
        }
        return true;
    }

    protected void addActions(final List<Action<EO>> actions) {
        this.actions.addAll(actions);
    }

    public void dispose() {
        eventsQueue.clear();
        actions.clear();
    }

	/**
	 * Add listener to this file monitor.
	 * 
	 * @param fileListener
	 *            Listener to add.
	 */
	public synchronized void addListener(EventConsumerListener fileListener) {
	    // Don't add if its already there
	
	    // Guaranteed to return a non-null array
	    final Object[] listenerArray = listeners.getListenerList();
	    // Process the listeners last to first, notifying
	    // those that are interested in this event
	    final int length = listenerArray.length;
	    for (int i = length - 2; i >= 0; i -= 2) {
	        if (listenerArray[i].equals(fileListener)) {
	            return;
	
	        }
	    }
	
	    listeners.add(EventConsumerListener.class, fileListener);
	}

	/**
	 * Remove listener from this file monitor.
	 * 
	 * @param fileListener
	 *            Listener to remove.
	 */
	public synchronized void removeListener(EventConsumerListener fileListener) {
	    listeners.remove(EventConsumerListener.class, fileListener);
	
	}
}
