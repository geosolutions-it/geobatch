/*
 */

package it.geosolutions.geobatch.flow.event.listeners.cumulator;

import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Cumulates all event messages into an internal List.
 * 
 * <P> You can retrieve all cumulated messages using {@link #getMessages()}
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class CumulatingProgressListener extends ProgressListener<CumulatingProgressListenerConfiguration> {

    private Object source;
    private List<String> messages = new ArrayList<String>();

    public CumulatingProgressListener(CumulatingProgressListenerConfiguration configuration) {
        super(configuration);
    }

    public void setSource(Object source) {
        this.source = source;
    }

    /**
     * Retrieves all the event messages arrived so far.
     *
     * @return the internal List<String> instance.
     */
    public List<String> getMessages() {
        return messages;
    }

    public void started() {
        messages.add("Started ["+source+"]");
    }

    public void progressing() {
        messages.add("Progressing " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

    public void paused() {
        messages.add("Paused " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

    public void resumed() {
        messages.add("Resumed " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

    public void completed() {
        messages.add("Completed ["+source+"]");
    }

    public void failed(Throwable exception) {
        messages.add("Failed for '"+exception.getMessage()+"' " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

    public void terminated() {
        messages.add("Terminated " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

}
