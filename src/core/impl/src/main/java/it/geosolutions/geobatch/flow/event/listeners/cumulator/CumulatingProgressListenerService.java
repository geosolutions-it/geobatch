/*
 */
package it.geosolutions.geobatch.flow.event.listeners.cumulator;

import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;

/**
 * @author ETj <etj at geo-solutions.it>
 */
public class CumulatingProgressListenerService extends BaseService implements
        ProgressListenerService<CumulatingProgressListener,CumulatingProgressListenerConfiguration>{
    
    public CumulatingProgressListenerService(String id, String name, String description) {
        super(id, name, description);
    }

    // implements Service<FileSystemEvent,
    // GeoTiffOverviewsEmbedderConfiguration> {

    // private CumulatingProgressListenerService() {
    // }

//    private final static Logger LOGGER = Logger.getLogger(CumulatingProgressListenerService.class
//            .toString());


    public CumulatingProgressListener createProgressListener(CumulatingProgressListenerConfiguration configuration,BaseIdentifiable owner) {
        return new CumulatingProgressListener(configuration,owner);
    }
}
