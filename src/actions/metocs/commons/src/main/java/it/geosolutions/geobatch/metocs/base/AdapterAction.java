/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.metocs.base;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.IOException;
import java.util.EventObject;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO this class should be moved in a more general package
 * 
 * This class define an abstraction layer to handle and transform
 * the input EventObject to an output instance of a derived EventObject.
 * This is done implementing the 'adapter()' method. 
 */
public abstract class AdapterAction<T extends EventObject>
                        extends BaseAction<EventObject>
                        implements EventAdapter<T>{
    
    private final static Logger LOGGER = Logger.getLogger(AdapterAction.class.toString());

    protected AdapterAction(ActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }
    
    /**
     * EXECUTE METHOD
     */
    public Queue<EventObject> execute(Queue<EventObject> events)
            throws ActionException {

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Starting with processing...");
        // looking for file
        if (events.size() > 0){
            Queue<EventObject> queue=new LinkedBlockingQueue<EventObject>();
            while (!events.isEmpty()){
                EventObject event=events.remove();
                if ((event=adapter(event))!=null){
                    queue.add(event);
                }
                else
                    throw new ActionException(this, "Passed event is not a FileSystemMonitorEvent instance");
            }
            return queue;
        }
        else {
            throw new IllegalArgumentException("Wrong number of elements for this action: "+ events.size());
        }
    }
}