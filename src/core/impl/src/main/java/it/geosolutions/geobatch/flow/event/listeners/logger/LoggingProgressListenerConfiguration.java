/*
 */

package it.geosolutions.geobatch.flow.event.listeners.logger;

import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class LoggingProgressListenerConfiguration extends ProgressListenerConfiguration {

    public LoggingProgressListenerConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * @uml.property  name="loggerName"
     */
    protected String loggerName;

    /**
     * @return
     * @uml.property  name="loggerName"
     */
    public String getLoggerName() {
        return loggerName;
    }

    /**
     * @param loggerName
     * @uml.property  name="loggerName"
     */
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

}
