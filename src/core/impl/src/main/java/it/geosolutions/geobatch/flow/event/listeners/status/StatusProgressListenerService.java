/*
 */
package it.geosolutions.geobatch.flow.event.listeners.status;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.util.logging.Logger;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class StatusProgressListenerService extends BaseService implements
        ProgressListenerService<StatusProgressListenerConfiguration> {
    // implements Service<FileSystemEvent,
    // GeoTiffOverviewsEmbedderConfiguration> {

    private StatusProgressListenerService() {
    }

    private final static Logger LOGGER = Logger.getLogger(StatusProgressListenerService.class
            .toString());

    public ProgressListener createProgressListener(StatusProgressListenerConfiguration configuration) {
        StatusProgressListener ret = new StatusProgressListener(configuration);
        return ret;
    }
}
