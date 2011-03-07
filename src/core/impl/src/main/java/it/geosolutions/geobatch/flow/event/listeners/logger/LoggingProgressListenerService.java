/*
 */
package it.geosolutions.geobatch.flow.event.listeners.logger;

import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class LoggingProgressListenerService extends BaseService implements
        ProgressListenerService<LoggingProgressListener,LoggingProgressListenerConfiguration> {

    public LoggingProgressListenerService(String id, String name, String description) {
        super(id, name, description);
    }

    public LoggingProgressListener createProgressListener(
            LoggingProgressListenerConfiguration configuration, BaseIdentifiable owner) {
        return new LoggingProgressListener(configuration,owner);
    }
}
