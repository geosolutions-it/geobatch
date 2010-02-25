/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
package it.geosolutions.geobatch.catalog;

import java.io.IOException;

import it.geosolutions.geobatch.catalog.dao.DAO;

public interface PersistentResource<C extends Configuration> extends Resource {

    /**
     * The Flow BaseEventConsumer Type.
     */
    public C getConfiguration();

    public void setConfiguration(C coonfiguration);

    public void persist()throws IOException;

    public void load()throws IOException;

    public boolean remove()throws IOException;

    public void setDAO(DAO<C, ?> dao);

    public DAO<C, ?> getDAO();

}
