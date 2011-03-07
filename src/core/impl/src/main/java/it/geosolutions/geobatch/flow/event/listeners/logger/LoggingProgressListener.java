/*
 */

package it.geosolutions.geobatch.flow.event.listeners.logger;

import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.util.logging.Logger;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class LoggingProgressListener extends ProgressListener {


    public LoggingProgressListener(LoggingProgressListenerConfiguration configuration, BaseIdentifiable owner) {
        super(configuration,owner);
    }
    
    public LoggingProgressListenerConfiguration getConfig(){
        return (LoggingProgressListenerConfiguration) configuration;
    }

    private Logger getLogger() {
        return Logger.getLogger(getConfig().getLoggerName());
    }

    public void started() {
        getLogger().info("Started [" + getOwner().getName() + "]");
    }

    public void progressing() {
        getLogger()
                .info("Progressing " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

    public void paused() {
        getLogger().info("Paused " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

    public void resumed() {
        getLogger().info("Resumed " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

    public void completed() {
        getLogger().info("Completed [" + getOwner().getName() + "]");
    }

    public void failed(Throwable exception) {
        getLogger().info("Failed " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

    public void terminated() {
        getLogger().info("Terminated " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

}
