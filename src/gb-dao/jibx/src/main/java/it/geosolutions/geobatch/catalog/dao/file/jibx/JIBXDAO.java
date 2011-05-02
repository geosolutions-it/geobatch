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



package it.geosolutions.geobatch.catalog.dao.file.jibx;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.catalog.dao.DAO;
import it.geosolutions.geobatch.catalog.dao.file.BaseFileBaseDAO;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

public abstract class JIBXDAO<T extends Configuration> 
		extends BaseFileBaseDAO<T>
		implements DAO<T, String> {

    public JIBXDAO(String directory) {
        super(directory);
    }

    public T persist(T entity) {
        IBindingFactory bfact;
        try {
            bfact = BindingDirectory.getFactory(entity.getClass());
            final IMarshallingContext mctx = bfact.createMarshallingContext();
            mctx.marshalDocument(entity, "UTF-8", null, new BufferedOutputStream(
                    new FileOutputStream(new File(getBaseDirectory(), entity.getId() + ".xml"))));
        } catch (JiBXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return entity;
    }

    public T refresh(T entity) {
        return find(entity.getId(), false);
    }

    public boolean remove(T entity) {

        // XXX use file cleaner
        final File entityfile = new File(getBaseDirectory(), entity.getId() + ".xml");
        if (entityfile.exists() && entityfile.delete())
            return true;
        return false;

    }

}
