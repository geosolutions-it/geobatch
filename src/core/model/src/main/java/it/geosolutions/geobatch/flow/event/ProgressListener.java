/*
 */

package it.geosolutions.geobatch.flow.event;

import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 * @author (r2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public abstract class ProgressListener implements IProgressListener {

    /**
     * @uml.property  name="source"
     */
    private BaseIdentifiable owner;
    
    /**
     * @uml.property name="configuration"
     * @uml.associationEnd
     */
    protected ProgressListenerConfiguration configuration;

    /**
     * @uml.property name="currentTask"
     */
    private String currentTask = "TASK";

    /**
     * @uml.property name="progress"
     */
    private float progress = 0;

    protected ProgressListener(BaseIdentifiable caller) {
        this.owner=caller;
        this.configuration = null;
    }
    
    protected ProgressListener(ProgressListenerConfiguration configuration, BaseIdentifiable caller) {
        this.owner=caller;
        this.configuration = configuration;
    }

    /**
     * owner must be set
     */
    @SuppressWarnings("unused")
    private ProgressListener() {
    }
    
    public BaseIdentifiable getOwner(){
        return owner;
    }

    /**
     * @return
     * @uml.property name="progress"
     */
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
     * 
     * @uml.property name="progress"
     */
    public void setProgress(float progress) {
        this.progress = progress;
    }
}
