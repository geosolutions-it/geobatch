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

package it.geosolutions.geobatch.settings;

import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.tools.file.IOUtils;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class GBSettingsDAOXStreamImpl implements GBSettingsDAO  {
    private Logger LOGGER = LoggerFactory.getLogger(GBSettingsDAOXStreamImpl.class);

    private DataDirHandler dataDirHandler;
    private String relativeDir = "settings";
    private File settingsDir;

    protected Alias alias;

    public List<String> getIds() {
        List<String> ret = new ArrayList<String>();
        final Iterator<File> it = FileUtils.iterateFiles(settingsDir, new String[] { ".xml" }, false);
        while(it.hasNext()) {
            File file = it.next();
            String name = FilenameUtils.getBaseName(file.getName());
            ret.add(name);
        }
        return ret;
    }

    public GBSettings find(String id) throws IOException {

        InputStream inStream = null;
        try {
            final File entityfile = new File(settingsDir, id + ".xml");
            if (! entityfile.canRead()) {
                LOGGER.warn("Unreadable file " + entityfile);
                return null;
            } else if (entityfile.isDirectory()) {
                LOGGER.warn("File " + entityfile + " is a dir");
                return null;
            } else {

                inStream = new FileInputStream(entityfile);
                XStream xstream = new XStream();
                alias.setAliases(xstream);
                GBSettings obj = (GBSettings) xstream.fromXML(inStream);
                if (! id.equals(obj.getId())) {
                    LOGGER.error("Mismatching id in settings (id:"+id+")");
                    throw new RuntimeException("Mismatching id in settings (id:"+id+")");
                }

                if (LOGGER.isInfoEnabled())
                    LOGGER.info("FOUND " + id + ">" + obj + "<");
                return obj;
            }
        } catch (Exception e) {
            throw new IOException("Unable to load settings with id:" + id + " ("+e.getMessage()+")", e);
        } finally {
            IOUtils.closeQuietly(inStream);
        }
    }

    public boolean save(GBSettings settings) {
        String id =settings.getId();
        if(id == null)
            throw new NullPointerException("Id is null");

        try {
            final File outFile = new File(settingsDir, id + ".xml");
            if (! outFile.canWrite()) {
                LOGGER.warn("Unwritable file " + outFile);
                return false;
            } else if (outFile.isDirectory()) {
                LOGGER.warn("Output file " + outFile + " is a dir");
                return false;
            } else {
                XStream xstream = new XStream();
                alias.setAliases(xstream);
                String xml = xstream.toXML(settings);
                FileUtils.write(outFile, xml);

                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Stored settings " + id);
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("Unable to store settings with id:" + id, e);
            return false;
        }
    }

    //==========================================================================

    public void setDataDirHandler(DataDirHandler dataDirHandler) {
        this.dataDirHandler = dataDirHandler;
    }

    public void setRelativeDir(String relativeDir) {
        this.relativeDir = relativeDir;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }


    protected void init() {
        settingsDir = new File(dataDirHandler.getDataDirectory(), relativeDir);
        LOGGER.info("Settings dir is " + settingsDir);
    }

}
