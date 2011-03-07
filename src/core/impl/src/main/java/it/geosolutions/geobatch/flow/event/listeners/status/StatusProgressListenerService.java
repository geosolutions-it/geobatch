/*
 */
package it.geosolutions.geobatch.flow.event.listeners.status;

import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class StatusProgressListenerService extends BaseService 
    implements ProgressListenerService<StatusProgressListener,StatusProgressListenerConfiguration> {

//    private final static Logger LOGGER = Logger.getLogger(StatusProgressListenerService.class
//            .toString());
    
    /**
     * Constructor forcing initialization of: id ,name and description of this resource 
     * @param id
     * @param name
     * @param description
     */
    public StatusProgressListenerService(String id, String name, String description) {
        super(id, name, description);
    }

    public StatusProgressListener createProgressListener(StatusProgressListenerConfiguration configuration, BaseIdentifiable owner) {
        return new StatusProgressListener(configuration,owner);
    }
}
