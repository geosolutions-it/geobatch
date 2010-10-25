/*
 */

package it.geosolutions.geobatch.flow.event;

import java.util.EventListener;

/**
 * Listener interface for monitorable object.
 * 
 * <P>
 * This Listner is designed to have stateful information, such as current task running or current
 * progress percentage, so it should be bound to one object only.
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public interface IProgressListener extends EventListener {

    /**
     * The process has been started
     */
    void started();

    /**
     * The process has just advanced a bit.
     * 
     * @see #getProgress()
     * @see #getTask()
     */
    void progressing();

    /**
     * The process has been paused
     */
    void paused();

    /**
     * The process has been resumed
     */
    void resumed();

    /**
     * The process has successfully ended.
     */
    void completed();

    /**
     * The process has ended with an error
     */
    void failed(Throwable exception);

    /**
     * The process has been terminated
     */
    void terminated();

    /**
     * Get a progress indicator, between 0.0 and 100.0
     */
    float getProgress();

    /**
     * Get the name of the current task.
     */
    String getTask();

    /**
     * Used by the notifier.
     */
    void setProgress(float progress);

    /**
     * Used by the notifier.
     */
    void setTask(String currentTask);

}
