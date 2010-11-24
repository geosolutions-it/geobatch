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

package it.geosolutions.geobatch.nurc.sem.rep10.mars3d;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.octave.actions.templates.FileInFileOut.FileInFileOutActionConfiguration;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;

public class MARS3DGeneratorService
    extends BaseService 
    implements ActionService<FileSystemMonitorEvent, FileInFileOutActionConfiguration> {

    public boolean canCreateAction(final FileInFileOutActionConfiguration configuration)  {
        
        String base_dir=configuration.getWorkingDirectory();
        base_dir=absolutize(base_dir);
        if (base_dir!=null){
            configuration.setWorkingDirectory(base_dir);
            // NOW THE WORKING DIR IS AN ABSOLUTE PATH
        }
        else
            return false;
        
//TODO check if the m file is present and is readable
        
        return true;
    }
    
    /**
     * Obtaining the Absolute path of the working dir
     * @param working_dir the relative (or absolute) path to absolutize
     * @note it should be a sub-dir of ...
* @TODO open a ticket to get getBaseDirectory() into Catalog interface
     */
    String absolutize(String working_dir) /*throws FileNotFoundException */{ 
        FileBaseCatalog c=(FileBaseCatalog) CatalogHolder.getCatalog();
        File fo=null;
        try {
            fo=IOUtils.findLocation(working_dir,new File(c.getBaseDirectory()));
        }catch (IOException ioe){
            return null;
        }
        
        if (fo!=null){
            return fo.toString();
        }
        else {
//TODO LOG            throw new FileNotFoundException("Unable to locate the working dir");
//            throw new FileNotFoundException();
            return null;
        }
        
    }

    public MARS3DAction createAction(final FileInFileOutActionConfiguration configuration) {
        if(canCreateAction(configuration)){
            return new MARS3DAction(configuration);
        }
        return null;
    }

}
