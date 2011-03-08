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
package it.geosolutions.geobatch.flow.event;

import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.misc.ListenerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.resources.UnmodifiableArrayList;

/**
 * Dispatch ProgressListener events to registered sublistener. <BR>
 * Events are delivered to all the listener in sequence, first to the first registered and so on.
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class ProgressListenerForwarder extends ProgressListener implements
        ListenerRegistry<IProgressListener> {

    protected final static Logger LOGGER = Logger.getLogger(ProgressListenerForwarder.class
            .toString());

    /**
     * The list of the registered sublisteners that will get the events.
     * @uml.property  name="listeners"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="it.geosolutions.geobatch.flow.event.IProgressListener"
     */
    protected List<IProgressListener> listeners = new ArrayList<IProgressListener>();
    
    public ProgressListenerForwarder(Identifiable owner) {
        super(owner);
    }

    /**
     * A shortcut to set some info and call progressing();
     */
    public void progressing(float progress, String task) {
        setProgress(progress);
        setTask(task);
        progressing();
    }

    public void completed() {
        for (IProgressListener l : listeners) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("ProgressListenerForwarder: "+getClass().getSimpleName() + " FORWARDING completed message to "
                        + l.getClass().getSimpleName());
            try {
                l.setProgress(100f); // forcing 100% progress
                l.completed();
            } catch (Exception e) {
                LOGGER.warning("ProgressListenerForwarder: Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    public void failed(Throwable exception) {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(getClass().getSimpleName() + " FORWARDING failed message ("
                            + exception + ") to " + l.getClass().getSimpleName());
                l.failed(exception);
            } catch (Exception e) {
                LOGGER.warning("ProgressListenerForwarder: Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public float getProgress() {
        throw new UnsupportedOperationException("ProgressListenerForwarder: Forwarder does not hold a status");
    }

    @Override
    public String getTask() {
        throw new UnsupportedOperationException("ProgressListenerForwarder: Forwarder does not hold a status");
    }

    public void paused() {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("ProgressListenerForwarder: "+getClass().getSimpleName() + " FORWARDING paused message to "
                            + l.getClass().getSimpleName());
                l.paused();
            } catch (Exception e) {
                LOGGER.warning("ProgressListenerForwarder: Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Call progressing on all the handled 
     */
    public void progressing() {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("ProgressListenerForwarder: "+getClass().getSimpleName() + " FORWARDING progressing message to "
                            + l.getClass().getSimpleName());
                l.progressing();
            } catch (Exception e) {
                LOGGER.warning("ProgressListenerForwarder: Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    public void resumed() {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("ProgressListenerForwarder: "+getClass().getSimpleName() + " FORWARDING resumed message to "
                            + l.getClass().getSimpleName());
                l.resumed();
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void setProgress(float progress) {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("ProgressListenerForwarder: "+getClass().getSimpleName() + " FORWARDING setProgress message to "
                            + l.getClass().getSimpleName());
                l.setProgress(progress);
            } catch (Exception e) {
                LOGGER.warning("ProgressListenerForwarder: Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void setTask(String currentTask) {
        for (IProgressListener l : listeners) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("ProgressListenerForwarder: "+getClass().getSimpleName() + " FORWARDING setTask message to "
                        + l.getClass().getSimpleName());
            try {
                l.setTask(currentTask);
            } catch (Exception e) {
                LOGGER.warning("ProgressListenerForwarder: Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    public void started() {
        for (IProgressListener l : listeners) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("ProgressListenerForwarder: "+getClass().getSimpleName() + " FORWARDING started message to "
                        + l.getClass().getSimpleName());
            try {
                l.started();
            } catch (Exception e) {
                LOGGER.warning("ProgressListenerForwarder: Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    public void terminated() {
        for (IProgressListener l : listeners) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine(getClass().getSimpleName() + " FORWARDING terminated message to "
                        + l.getClass().getSimpleName());
            try {
                l.terminated();
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e.getLocalizedMessage());
            }
        }
    }

    public synchronized void addListener(IProgressListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(IProgressListener listener) {
        listeners.remove(listener);
    }

    public List<? extends IProgressListener> getListeners() {
        return UnmodifiableArrayList.wrap(listeners
                .toArray(new IProgressListener[listeners.size()]));
    }
}
