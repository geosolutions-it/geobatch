package it.geosolutions.geobatch.metocs.base;

import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.util.EventObject;

/**
 * TODO this interface should be moved in a more general package
 * 
 * @author carlo cancellieri - carlo.cancellieri@geo-solutions.it
 *
 * @param <T>
 */
public interface EventAdapter <T extends EventObject> {
    
    /**
     * This method define the mapping between input and output EventObject instance
     * @param ieo is the object to transform
     * @return the EventObject adapted
     */
    public T adapter(EventObject ieo) throws ActionException;

}
