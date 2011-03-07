/*
 */

package it.geosolutions.geobatch.flow.event.listeners.cumulator;

import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class CumulatingProgressListenerConfiguration extends ProgressListenerConfiguration {

    public CumulatingProgressListenerConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    // here we may configure the size of a circular buffer.
}
