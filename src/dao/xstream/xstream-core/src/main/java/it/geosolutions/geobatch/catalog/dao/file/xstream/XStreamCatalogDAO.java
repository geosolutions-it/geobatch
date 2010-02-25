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

import com.thoughtworks.xstream.XStream;
import it.geosolutions.geobatch.catalog.dao.CatalogConfigurationDAO;
import it.geosolutions.geobatch.configuration.CatalogConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedCatalogConfiguration;

import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.xstream.Alias;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class XStreamCatalogDAO extends XStreamDAO<CatalogConfiguration> implements
        CatalogConfigurationDAO {
	
	public final static Logger LOGGER= Logger.getLogger(XStreamCatalogDAO.class.toString());

    public XStreamCatalogDAO(String directory, Alias alias) {
        super(directory, alias);
    }

    public CatalogConfiguration find(CatalogConfiguration exampleInstance, boolean lock)  throws IOException{
        return find(exampleInstance.getId(), lock);
    }

    public CatalogConfiguration find(String id, boolean lock) throws IOException {
    	InputStream inStream=null;
        try {
            final File entityfile = new File(getBaseDirectory(), id + ".xml");
            if (entityfile.canRead() && !entityfile.isDirectory()) {

            	inStream=new FileInputStream(entityfile);
                XStream xstream = new XStream();
                alias.setAliases(xstream);
                FileBasedCatalogConfiguration obj = (FileBasedCatalogConfiguration) xstream.fromXML(inStream);
                if (obj.getWorkingDirectory() == null)
                    obj.setWorkingDirectory(getBaseDirectory());
                if(LOGGER.isLoggable(Level.INFO))
             	   LOGGER.info("XStreamCatalogDAO:: FOUND " + id + ">" + obj + "<");
                return obj;

            }
        } catch (Throwable e) {
        	final IOException ioe= new IOException("Unable to load flow config:"+id);
        	ioe.initCause(e);
        	throw ioe;
        }
        finally{
        	if(inStream!=null)
        		IOUtils.closeQuietly(inStream);
        }        
        return null;
    }

}
