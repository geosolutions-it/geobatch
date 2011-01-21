/*
 */

package it.geosolutions.geobatch.flow.event.listeners.status;

import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.util.List;

/**
 * Remember the state of the event firer.
 * 
 * <P>
 * You can retrieve the info using
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class StatusProgressListener extends ProgressListener<StatusProgressListenerConfiguration> {

    /**
     * @uml.property  name="source"
     */
    private Object source;

    /**
     * @uml.property  name="started"
     */
    protected boolean started = false;

    /**
     * @uml.property  name="paused"
     */
    protected boolean paused = false;

    /**
     * @uml.property  name="failed"
     */
    protected boolean failed = false;

    /**
     * @uml.property  name="completed"
     */
    protected boolean completed = false;

    /**
     * @uml.property  name="terminated"
     */
    protected boolean terminated = false;

    /**
     * @uml.property  name="failException"
     */
    protected Throwable failException = null;

    public StatusProgressListener(StatusProgressListenerConfiguration configuration) {
        super(configuration);
    }

    /**
     * @param source
     * @uml.property  name="source"
     */
    public void setSource(Object source) {
        this.source = source;
    }

    public void started() {
        started = true;
    }

    /**
     * @return
     * @uml.property  name="started"
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * This event should trigger some refresh on interactive displays, but we don't need it
     */
    public void progressing() {
    }

    public void paused() {
        paused = true;
    }

    /**
     * @return
     * @uml.property  name="paused"
     */
    public boolean isPaused() {
        return paused;
    }

    public void resumed() {
        paused = false;
    }

    public void completed() {
        completed = true;
    }

    /**
     * @return
     * @uml.property  name="completed"
     */
    public boolean isCompleted() {
        return completed;
    }

    public void failed(Throwable exception) {
        failed = true;
        failException = exception;
    }

    /**
     * @return
     * @uml.property  name="failed"
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * @return
     * @uml.property  name="failException"
     */
    public Throwable getFailException() {
        return failException;
    }

    public void terminated() {
        terminated = true;
    }

    /**
     * @return
     * @uml.property  name="terminated"
     */
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("Status of ").append(source).append('[');

        sb.append("Last task: '").append(getTask()).append("' ");
        sb.append(" at ").append(getProgress()).append("% ");
        if (started)
            sb.append("started ");
        if (paused)
            sb.append("paused ");
        if (completed)
            sb.append("completed");
        if (failed)
            sb.append("failed (").append(failException.getMessage()).append(")");
        if (terminated)
            sb.append("terminated");
        sb.append(']');

        return sb.toString();
    }
}
