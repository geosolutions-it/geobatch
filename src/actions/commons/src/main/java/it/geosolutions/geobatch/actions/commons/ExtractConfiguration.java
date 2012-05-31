/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.actions.commons;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.io.File;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class ExtractConfiguration extends ActionConfiguration {

    public ExtractConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * is a folder where incoming files will be moved after extraction
     */
    private File destination;

    /**
     * @return the destination
     */
    public final File getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public final void setDestination(File destination) {
        this.destination = destination;
    }

}
