/*
 */

package it.geosolutions.geobatch.flow.event.listeners.logger;

import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.util.logging.Logger;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class LoggingProgressListener extends ProgressListener<LoggingProgressListenerConfiguration> {

    private Object source;

    public LoggingProgressListener(LoggingProgressListenerConfiguration configuration) {
        super(configuration);
    }

    public void setSource(Object source) {
        this.source = source;
    }

    private Logger getLogger() {
        return Logger.getLogger(configuration.getLoggerName());
    }

    public void started() {
        getLogger().info("Started ["+source+"]");
    }

    public void progressing() {
        getLogger().info("Progressing " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

    public void paused() {
        getLogger().info("Paused " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

    public void resumed() {
        getLogger().info("Resumed " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

    public void completed() {
        getLogger().info("Completed ["+source+"]");
    }

    public void failed(Throwable exception) {
        getLogger().info("Failed " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

    public void terminated() {
        getLogger().info("Terminated " + getProgress() + "% -- " + getTask() + " ["+source+"]");
    }

}
