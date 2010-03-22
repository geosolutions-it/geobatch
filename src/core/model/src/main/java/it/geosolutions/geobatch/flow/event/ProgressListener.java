/*
 */

package it.geosolutions.geobatch.flow.event;

import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public abstract class ProgressListener<PLC extends ProgressListenerConfiguration>
        implements IProgressListener {

    protected PLC configuration;

    private String currentTask = null;
    private float progress = 0;

    protected ProgressListener(PLC configuration) {
        this.configuration = configuration;
    }

    protected ProgressListener() {
    }

//    public void started() {
//    }
//
//    public void progressing() {
//    }
//
//    public void paused() {
//    }
//
//    public void resumed() {
//    }
//
//    public void completed() {
//    }
//
//    public void failed(Throwable exception) {
//    }
//
//    public void terminated() {
//    }

    public float getProgress() {
        return progress;
    }

    public String getTask() {
        return currentTask;
    }

    /**
     * Used by the notifier.
     */
    public void setTask(String currentTask) {
        this.currentTask = currentTask;
    }

    /**
     * Used by the notifier.
     */
    public void setProgress(float progress) {
        this.progress = progress;
    }
}
