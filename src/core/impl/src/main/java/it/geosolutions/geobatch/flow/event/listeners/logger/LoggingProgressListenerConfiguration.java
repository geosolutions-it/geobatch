/*
 */

package it.geosolutions.geobatch.flow.event.listeners.logger;

import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class LoggingProgressListenerConfiguration
    extends ProgressListenerConfiguration {
    
    protected String loggerName;

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }
    
}
