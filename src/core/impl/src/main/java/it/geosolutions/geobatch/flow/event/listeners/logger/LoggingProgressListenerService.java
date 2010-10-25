/*
 */
package it.geosolutions.geobatch.flow.event.listeners.logger;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.util.logging.Logger;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class LoggingProgressListenerService extends BaseService implements
        ProgressListenerService<LoggingProgressListenerConfiguration> {
    // implements Service<FileSystemMonitorEvent,
    // GeoTiffOverviewsEmbedderConfiguration> {

    private LoggingProgressListenerService() {
    }

    private final static Logger LOGGER = Logger.getLogger(LoggingProgressListenerService.class
            .toString());

    public ProgressListener createProgressListener(
            LoggingProgressListenerConfiguration configuration) {
        LoggingProgressListener ret = new LoggingProgressListener(configuration);
        return ret;
    }
}
