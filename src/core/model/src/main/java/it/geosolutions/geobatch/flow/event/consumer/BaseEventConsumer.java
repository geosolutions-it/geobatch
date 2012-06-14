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

import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.catalog.impl.BaseResource;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.misc.PauseHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alessio Fabiani, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 * @author Emanuele Tajariol <etj AT geo-solutions DOT it>, GeoSolutions S.A.S.
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version r1 - date: 2007<br>
 *          r2 - date: 26 Aug 2011 <br>
 */
public abstract class BaseEventConsumer<XEO extends EventObject, ECC extends EventConsumerConfiguration>
    extends BaseResource implements EventConsumer<XEO, ECC> {

    private static Logger LOGGER = LoggerFactory.getLogger(BaseEventConsumer.class);

    private String flowName;

    private final Calendar creationTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    private final Calendar endingTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    private volatile EventConsumerStatus eventConsumerStatus;

    /**
     * the context where action is running in...<br>
     * this is initialized by the FlowManager
     */
    private String runningContext;


    /**
     * The MailBox
     */
    protected final Queue<XEO> eventsQueue = new LinkedList<XEO>();

    protected final List<BaseAction<XEO>> actions = new ArrayList<BaseAction<XEO>>();

    protected volatile BaseAction<XEO> currentAction = null;

    final protected EventConsumerListenerForwarder listenerForwarder;

    protected PauseHandler pauseHandler = new PauseHandler(false);

    /**
     * @deprecated name and description not needed here
     */
    public BaseEventConsumer(String id, String name, String description) {
        this(id);
        LoggerFactory.getLogger("ROOT").error("Deprecated constructor called from " + getClass().getName() , new Throwable("TRACE!") );
    }

    public BaseEventConsumer(String id) {
        super(id);
        this.listenerForwarder = new EventConsumerListenerForwarder(this);
        this.setStatus(EventConsumerStatus.IDLE);
    }

    public Calendar getCreationTimestamp() {
        return (Calendar)creationTimestamp.clone();
    }

    public Calendar getEndingTimestamp() {
        return (Calendar)endingTimestamp.clone();
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    /**
     * @return the runningContext
     */
    public String getRunningContext() {
        return runningContext;
    }

    /**
     * @param runningContext the runningContext to set
     */
    public void setRunningContext(String runningContext) {
        this.runningContext = runningContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.geosolutions.geobatch.flow.event.consumer.EventConsumer#getStatus()
     */
    public EventConsumerStatus getStatus() {
        return this.eventConsumerStatus;
    }

    /**
     * Change status and fire events on listeners if status has really changed.
     */
    protected void setStatus(EventConsumerStatus eventConsumerStatus) {

        if (this.eventConsumerStatus != eventConsumerStatus) {
            listenerForwarder.fireStatusChanged(this.eventConsumerStatus, eventConsumerStatus);
            listenerForwarder.setTask(eventConsumerStatus.toString());
        }

        this.eventConsumerStatus = eventConsumerStatus;
    }

    public Action<XEO> getCurrentAction() {
        return currentAction;
    }

    /**
     * {@link it.geosolutions.geobatch.flow.event.consumer.EventConsumer}
     */
    public boolean consume(XEO event) {
        if (!eventsQueue.offer(event)) {
            return false;
        }

        return true;
    }

    /**
     * Once the configuring state has been successfully passed, by collecting
     * all the necessary Events, the EventConsumer invokes this method in order
     * to run the related actions.
     * <P>
     * <B>FIXME</B>: <I>on action errors the flow used to go on. Now it bails
     * out from the loop. <BR>
     * We may need to specify on a per-action basis if an error in the action
     * should stop the whole flow.</I>
     * 
     * @param events The incoming event queue to pass to the first action
     * @param runningContext The context in which the actions should be executed
     * 
     */
    protected Queue<XEO> applyActions(Queue<XEO> events) throws ActionException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Applying " + actions.size() + " actions on " + events.size() + " events.");
        }

        // apply all the actions
        int step = 0;
        try {
            for (BaseAction<XEO> action : this.actions) {
                try {
                    pauseHandler.waitUntilResumed();

                    float progress = 100f * (float)step / this.actions.size();
                    listenerForwarder.setProgress(progress);
                    listenerForwarder.setTask("Running " + action.getName() + "("
                                              + (step + 1) + "/" + this.actions.size() + ")");
                    // notify there has been some progressing
                    listenerForwarder.progressing();

                    // setting the action context same as the event consumer
                    action.setRunningContext(getRunningContext());

                    // setting current action
                    currentAction = action;

                    // // let child classes perform their init
                    setupAction(action, step);

                    // execute the action
                    events = action.execute(events);

                    if (events == null) {
                        throw new IllegalArgumentException("Action " + action.getName()
                                                           + " returns a null queue.");
                    }
                    if (events.isEmpty()) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Action " + action.getName()
                                        + " left no event in queue.");
                        }
                    }
                    step++;

                } catch (ActionException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    } else {
                        LOGGER.error(e.getLocalizedMessage());
                    }

                    listenerForwarder.setTask("Action " + action.getName() + " failed (" + e
                                              + ")");
                    listenerForwarder.progressing();

                    if (!currentAction.isFailIgnored()) {
                        events.clear();
                        throw e;
                    } else {
                        // CHECKME: eventlist is not modified in this case. will
                        // it
                        // work?
                    }

                } catch (Exception e) { // exception not handled by the Action
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Action threw an unhandled exception: " + e.getLocalizedMessage(), e);
                    }

                    listenerForwarder.setTask("Action " + action.getName() 
                                            + " failed (" + e + ")");
                    listenerForwarder.progressing();

                    if (!currentAction.isFailIgnored()) {
                        if (events == null) {
                            throw new IllegalArgumentException("Action " + action.getName()
                                                               + " left no event in queue.");
                        } else {
                            events.clear();
                        }
                        // wrap the unhandled exception
                        throw new ActionException(currentAction, e.getMessage(), e);
                    } else {
                        // CHECKME: eventlist is not modified in this case. will it work?
                    }
                } finally {
                    // currentAction = null; // don't null the action: we'd like to read which was the last action run
                }
            }
        } catch (Error ex) { // this catch in not in the loop: it will catch Errors, which cant usually be recovered
            LOGGER.error("Error in Action", ex);
            throw ex;
        } finally {
            // set ending time
            endingTimestamp.setTimeInMillis(System.currentTimeMillis());
        }

        // end of loop: all actions have been executed
        // checkme: what shall we do with the events left in the queue?
        if (events != null && !events.isEmpty()) {
            LOGGER.info("There are " + events.size() + " events left in the queue after last action ("
                        + currentAction.getName() + ")");
        }

        return events;
    }

    public boolean pause() {
        pauseHandler.pause();
        // set new status
        setStatus(EventConsumerStatus.PAUSED);
        return true; // we'll pause asap
    }

    public boolean pause(boolean sub) {
        final EventConsumerStatus status = getStatus();
        if (status.equals(EventConsumerStatus.EXECUTING) || status.equals(EventConsumerStatus.WAITING)
            || status.equals(EventConsumerStatus.IDLE)) {

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Pausing consumer " + getFlowName() + " [" + creationTimestamp + "]");
            }

            pauseHandler.pause();
            // set new status
            setStatus(EventConsumerStatus.PAUSED);

            if (currentAction != null) {
                LOGGER.info("Pausing action " + currentAction.getName() + " in flow "
                            + getFlowName() + " [" + creationTimestamp + "]");
                currentAction.pause();
            }
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Consumer " + getFlowName() + " [" + creationTimestamp + "] is already in state: "
                            + getStatus());
            }
        }
        return true; // we'll pause asap
    }

    public void resume() {
        LOGGER.info("Resuming consumer " + getFlowName() + " [" + creationTimestamp + "]");
        if (currentAction != null) {
            LOGGER.info("Resuming action " + currentAction.getName() + " in flow "
                        + getFlowName() + " [" + creationTimestamp + "]");
            currentAction.resume();
        }

        pauseHandler.resume();
        // set new status
        setStatus(EventConsumerStatus.EXECUTING);
    }

    public boolean isPaused() {
        return pauseHandler.isPaused();
    }

    /**
     * 
     * @return the list of the <TT>Action</TT>s associated to this consumer.
     * 
     *         TODO: returned list should be unmodifiable
     */
    public List<BaseAction<XEO>> getActions() {
        return actions;
    }

    protected void addActions(final List<BaseAction<XEO>> actions) {
        this.actions.addAll(actions);
    }

    public void dispose() {
        eventsQueue.clear();
        // actions.clear();
        // currentAction.destroy();
    }

    /**
     * Add listener to this consumer. If the listener is already registered, it
     * won't be added again.
     * 
     * @param fileListener Listener to add.
     */
    public synchronized void addListener(IProgressListener listener) {
        listenerForwarder.addListener(listener);
    }

    /**
     * Remove listener from this file monitor.
     * 
     * @param listener Listener to remove.
     */
    public synchronized void removeListener(IProgressListener listener) {
        listenerForwarder.removeListener(listener);
    }

    protected ProgressListenerForwarder getListenerForwarder() {
        return listenerForwarder;
    }

    @Override
    public Collection<IProgressListener> getListeners() {
        return listenerForwarder.getListeners();
    }

    @Override
    public Collection<IProgressListener> getListeners(Class clazz) {
        return listenerForwarder.getListeners(clazz);
    }

    /**
     * Create a temp dir for an action in a flow.<br/>
     * FIXME: Quick'n'dirty implementation; - should this info be set only by
     * FileBasedEventCOnsumer? Overridable method so that child classes may
     * perform further setup on actions.
     * 
     * @param action
     * @param aThis
     * @param step
     * @return
     */
    protected abstract void setupAction(BaseAction action, int step);

    protected class EventConsumerListenerForwarder extends ProgressListenerForwarder {

        protected EventConsumerListenerForwarder(BaseIdentifiable owner) {
            super(owner);
        }

        public void fireStatusChanged(EventConsumerStatus olds, EventConsumerStatus news) {
            for (IProgressListener l : listeners) {
                try {
                    if (l instanceof EventConsumerListener) {
                        ((EventConsumerListener)l).statusChanged(olds, news);
                    }
                } catch (Exception e) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Exception in event forwarder: " + e);
                }
            }
        }
    }
}
