/*
 */

package it.geosolutions.geobatch.flow.event;

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
     */
    protected List<IProgressListener> listeners = new ArrayList<IProgressListener>();

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
                LOGGER.fine(getClass().getSimpleName() + " FORWARDING completed message to "
                        + l.getClass().getSimpleName());
            try {
                l.setProgress(100f); // forcing 100% progress
                l.completed();
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e);
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
                LOGGER.warning("Exception in event forwarder: " + e);
            }
        }
    }

    @Override
    public float getProgress() {
        throw new UnsupportedOperationException("Forwarder does not hold a status");
    }

    @Override
    public String getTask() {
        throw new UnsupportedOperationException("Forwarder does not hold a status");
    }

    public void paused() {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(getClass().getSimpleName() + " FORWARDING paused message to "
                            + l.getClass().getSimpleName());
                l.paused();
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e);
            }
        }
    }

    public void progressing() {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(getClass().getSimpleName() + " FORWARDING progressing message to "
                            + l.getClass().getSimpleName());
                l.progressing();
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e);
            }
        }
    }

    public void resumed() {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(getClass().getSimpleName() + " FORWARDING resumed message to "
                            + l.getClass().getSimpleName());
                l.resumed();
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e);
            }
        }
    }

    @Override
    public void setProgress(float progress) {
        for (IProgressListener l : listeners) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(getClass().getSimpleName() + " FORWARDING setProgress message to "
                            + l.getClass().getSimpleName());
                l.setProgress(progress);
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e);
            }
        }
    }

    @Override
    public void setTask(String currentTask) {
        for (IProgressListener l : listeners) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine(getClass().getSimpleName() + " FORWARDING setTask message to "
                        + l.getClass().getSimpleName());
            try {
                l.setTask(currentTask);
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e);
            }
        }
    }

    public void started() {
        for (IProgressListener l : listeners) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine(getClass().getSimpleName() + " FORWARDING started message to "
                        + l.getClass().getSimpleName());
            try {
                l.started();
            } catch (Exception e) {
                LOGGER.warning("Exception in event forwarder: " + e);
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
                LOGGER.warning("Exception in event forwarder: " + e);
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
