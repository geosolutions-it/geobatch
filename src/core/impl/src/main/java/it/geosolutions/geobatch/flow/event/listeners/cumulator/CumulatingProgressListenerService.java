/*
 */
package it.geosolutions.geobatch.flow.event.listeners.cumulator;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.util.logging.Logger;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class CumulatingProgressListenerService extends BaseService implements
        ProgressListenerService<CumulatingProgressListenerConfiguration> {
    // implements Service<FileSystemEvent,
    // GeoTiffOverviewsEmbedderConfiguration> {

    private CumulatingProgressListenerService() {
    }

    private final static Logger LOGGER = Logger.getLogger(CumulatingProgressListenerService.class
            .toString());

    public ProgressListener createProgressListener(
            CumulatingProgressListenerConfiguration configuration) {
        CumulatingProgressListener ret = new CumulatingProgressListener(configuration);
        return ret;
    }
}
