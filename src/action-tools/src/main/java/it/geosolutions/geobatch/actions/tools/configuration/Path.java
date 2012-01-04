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
package it.geosolutions.geobatch.actions.tools.configuration;

import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.io.File;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public final class Path {

    /**
     * Obtaining the Absolute path of the working dir
     * @param working_dir the relative (or absolute) path to absolutize
     * @note it should be a relative or absolute path referring to a sub-dir of 
     * the FileBaseCatalog BaseDirectory
     * @see FileBaseCatalog
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     */
    public static String getAbsolutePath(String working_dir){ 
        FileBaseCatalog c = (FileBaseCatalog) CatalogHolder.getCatalog();
        
        File fo = it.geosolutions.tools.commons.file.Path.findLocation(working_dir, 
                        c==null? null : c.getBaseDirectory());
        
        if (fo!=null){
            return fo.toString();
        }
        else {
//TODO LOG            throw new FileNotFoundException("Unable to locate the working dir");
//            throw new FileNotFoundException();
            return null;
        }
    } 

}
