/*
 */

package it.geosolutions.geobatch.misc;

import java.util.EventListener;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public interface ListenerRegistry<EL extends EventListener> {

    public void addListener(EL listener);

    public void removeListener(EL listener);
}
