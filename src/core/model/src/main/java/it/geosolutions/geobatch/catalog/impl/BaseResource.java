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

package it.geosolutions.geobatch.catalog.impl;

import it.geosolutions.geobatch.catalog.Resource;
import org.slf4j.LoggerFactory;

public class BaseResource extends BaseIdentifiable implements Resource {
    /**
     * Constructor forcing initialization of: id ,name and description of this resource 
     * @param id
     * @param name
     * @param description
     * @deprecated name and description not needed here
     */
    public BaseResource(String id, String name, String description) {
        super(id);
        LoggerFactory.getLogger("ROOT").error("Deprecated constructor called from " + getClass().getName() , new Throwable("TRACE!") );
    }

    public BaseResource(String id) {
        super(id);
    }

    public void dispose() {
    }

}
