/*
 */

package it.geosolutions.geobatch.flow.event.listeners.cumulator;

import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Cumulates all event messages into an internal List.
 * 
 * <P>
 * You can retrieve all cumulated messages using {@link #getMessages()}
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class CumulatingProgressListener extends
        ProgressListener<CumulatingProgressListenerConfiguration> {

    private Object source;

    private List<String> messages = new ArrayList<String>();

    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSSz");
    static {
        TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
        DATEFORMAT.setTimeZone(TZ_UTC);
    }

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

    protected void msg(String msg) {
        Calendar now = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append(DATEFORMAT.format(now.getTime())).append(' ').append(msg).append(' ').append(
                getProgress()).append("% --").append(getTask());
        if (source != null) {
            sb.append(" [").append(source.toString()).append(']');
        }
        messages.add(sb.toString());
    }

    public void started() {
        msg("Started");
    }

    public void progressing() {
        msg("Progressing");
    }

    public void paused() {
        msg("Paused");
    }

    public void resumed() {
        msg("Resumed");
    }

    public void completed() {
        msg("Completed");
    }

    public void failed(Throwable exception) {
        msg("Failed for '" + exception + "'");
    }

    public void terminated() {
        msg("Terminated");
    }

}
