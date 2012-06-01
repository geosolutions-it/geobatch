/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.tools.adapter;

import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.util.EventObject;

/**
 * An adapter interface which should be implemented by Actions
 * want to work using general EventObject
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
