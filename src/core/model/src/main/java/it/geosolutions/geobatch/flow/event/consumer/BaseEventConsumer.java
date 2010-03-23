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

import it.geosolutions.geobatch.catalog.impl.BaseResource;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.misc.Counter;
import it.geosolutions.geobatch.misc.PauseHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public abstract class BaseEventConsumer<XEO extends EventObject, ECC extends EventConsumerConfiguration>
        extends BaseResource
        implements Runnable, EventConsumer<XEO, ECC> {

    private static Logger LOGGER = Logger.getLogger(BaseEventConsumer.class.toString());
    private static Counter counter = new Counter();
    private final Calendar creationTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    /**
     */
    private volatile EventConsumerStatus eventConsumerStatus;
    /**
     * The MailBox
     */
    protected final Queue<XEO> eventsQueue = new LinkedList<XEO>();
    protected final List<Action<XEO>> actions = new ArrayList<Action<XEO>>();
    protected volatile Action<XEO> currentAction = null;
//    private EventListenerList listeners = new EventListenerList();
    protected EventConsumerListenerForwarder listenerForwarder =
            new EventConsumerListenerForwarder();
    protected PauseHandler pauseHandler = new PauseHandler(false);

    public BaseEventConsumer() {
        super();
        this.setStatus(EventConsumerStatus.IDLE);
        this.setId(getClass().getSimpleName() + "_" + counter.getNext());
    }

    public BaseEventConsumer(String id, String name, String description) {
        super(id, name, description);
        this.setStatus(EventConsumerStatus.IDLE);
    }

    public Calendar getCreationTimestamp() {
        return (Calendar) creationTimestamp.clone();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.flow.event.consumer.EventConsumer#getStatus()
     */
    public EventConsumerStatus getStatus() {
        return this.eventConsumerStatus;
    }

    /**
     * Change status and fire events on listeners if status has really changed.
     */
    protected void setStatus(EventConsumerStatus eventConsumerStatus) {

        EventConsumerStatus old = eventConsumerStatus;

        this.eventConsumerStatus = eventConsumerStatus;

        if (old != eventConsumerStatus) {
            listenerForwarder.fireStatusChanged(old, eventConsumerStatus);
            listenerForwarder.setTask(eventConsumerStatus.toString());
        }
    }

    public Action<XEO> getCurrentAction() {
        return currentAction;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.flow.event.consumer.EventConsumer#put(it.geosolutions
     * .filesystemmonitor .monitor.FileSystemMonitorEvent)
     */
    public boolean consume(XEO event) {
        if (!eventsQueue.offer(event)) {
            return false;
        }

        return true;
    }

    /**
     * Once the configuring state has been successfully passed, by collecting all the necessary
     * Events, the EventConsumer invokes this method in order to run the 
     * related actions.
     * <P>
     * <B>FIXME</B>: <I>on action errors the flow used to go on.
     *  Now it bails out from the loop.
     *  <BR>We may need to specify on a per-action basis if an error in the action
     *  should stop the whole flow.</I>
     */
    protected boolean applyActions(Queue<XEO> events) throws ActionException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Applying " + actions.size() + " actions on "
                    + events.size() + " events.");
        }

        // apply all the actions
        int step = 0;

        try {
            for (Action<XEO> action : this.actions) {

                pauseHandler.waitUntilResumed();

                listenerForwarder.setProgress(100f * step / this.actions.size());
                listenerForwarder.setTask("Running " + action.getClass().getSimpleName() + "(" + (step + 1) + "/" + this.actions.size() + ")");
                listenerForwarder.progressing(); // notify there has been some progressing
                
                currentAction = action;
                events = action.execute(events);

                if (events == null || events.isEmpty()) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning("Action " + action.getClass().getSimpleName() + " left no event in queue.");
                    }
                    return false;
                } 
            }
            // end of loop: all actions have been executed
            // checkme: what shall we do with the events left in the queue?
            if(events != null && ! events.isEmpty()) {
                LOGGER.info("There are " + events.size() + " events left in the queue after last action ("+currentAction.getClass().getSimpleName()+")");
            }

            return true;

        } catch (ActionException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            events.clear();

            // WIP do we want to manage the exception upstream?
            throw e;

        } catch (Exception e) { // exception not handled by the Action
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            events.clear();

            // WIP do we want to manage the exception upstream?
            // wrap the unhandled exception
            throw new ActionException(currentAction, e.getMessage(), e);

        } finally {
            currentAction = null;
        }
    }

    public boolean pause() {
        pauseHandler.pause();
        return true; // we'll pause asap
    }

    public boolean pause(boolean sub) {
        LOGGER.info("Pausing consumer " + getName() + " [" + creationTimestamp + "]");
        pauseHandler.pause();

        if (currentAction != null) {
            LOGGER.info("Pausing action " + currentAction.getClass().getSimpleName()
                    + " in consumer " + getName() + " [" + creationTimestamp + "]");
            currentAction.pause();
        }

        return true; // we'll pause asap
    }

    public void resume() {
        LOGGER.info("Resuming consumer " + getName() + " [" + creationTimestamp + "]");
        if (currentAction != null) {
            LOGGER.info("Resuming action " + currentAction.getClass().getSimpleName()
                    + " in consumer " + getName() + " [" + creationTimestamp + "]");
            currentAction.resume();
        }

        pauseHandler.resume();
    }

    public boolean isPaused() {
        return pauseHandler.isPaused();
    }

    protected void addActions(final List<Action<XEO>> actions) {
        this.actions.addAll(actions);
    }

    public void dispose() {
        eventsQueue.clear();
        actions.clear();
    }

    /**
     * Add listener to this consumer.
     * If hte listere is already registerd, it won't be added again.
     *
     * @param fileListener
     *            Listener to add.
     */
    public synchronized void addListener(EventConsumerListener listener) {
        listenerForwarder.addListener(listener);
    }

    /**
     * Remove listener from this file monitor.
     *
     * @param listener
     *            Listener to remove.
     */
    public synchronized void removeListener(EventConsumerListener listener) {
        listenerForwarder.removeListener(listener);
    }

    protected ProgressListenerForwarder getListenerForwarder() {
        return listenerForwarder;
    }

    public <PL extends IProgressListener> PL getProgressListener(Class<PL> clazz) {
        for (IProgressListener ipl : getListenerForwarder().getListeners()) {
            if (clazz.isAssignableFrom(ipl.getClass())) {
                return (PL) ipl;
            }
        }

        return null;
    }

    protected class EventConsumerListenerForwarder extends ProgressListenerForwarder {

        public void fireStatusChanged(EventConsumerStatus olds, EventConsumerStatus news) {
            for (IProgressListener l : listeners) {
                try {
                    if (l instanceof EventConsumerListener) {
                        ((EventConsumerListener) l).statusChanged(olds, news);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Exception in event forwarder: " + e);
                }
            }
        }
    }
}

