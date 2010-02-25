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



package it.geosolutions.geobatch.catalog.dao.file.xstream;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.catalog.dao.DAO;
import it.geosolutions.geobatch.catalog.dao.file.BaseFileBaseDAO;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;

public abstract class XStreamDAO<T extends Configuration> 
        extends BaseFileBaseDAO<T>
        implements DAO<T, String> {

    protected final Alias alias;

    public XStreamDAO(String directory, Alias alias) {
        super(directory);
        this.alias = alias;
    }

    public T persist(T entity)throws IOException {
    	OutputStream os=null;
        try {
            XStream xstream = new XStream();
            alias.setAliases(xstream);
            final File outFile= new File(getBaseDirectory(), entity.getId() + ".xml");
            os = new FileOutputStream(outFile);
            xstream.toXML(entity, new BufferedOutputStream(os));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        finally{
        	if(os!=null)
        		IOUtils.closeQuietly(os);
        }

        return entity;
    }

    public T refresh(T entity)throws IOException {
        return find(entity.getId(), false);
    }

    public boolean remove(T entity)throws IOException {

        
        final File entityfile = new File(getBaseDirectory(), entity.getId() + ".xml");
        if (entityfile.exists())
        {
        	if(! entityfile.delete())
                IOUtils.deleteFile(entityfile);
        	return true;
        }
        return false;

    }

}
