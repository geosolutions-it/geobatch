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

package it.geosolutions.geobatch.nurc.sem.rep10.meteoam.ecmwf.wave;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.octave.actions.OctaveActionConfiguration;

public class ECMWFWaveGeneratorService
    extends BaseService 
    implements ActionService<FileSystemMonitorEvent, OctaveActionConfiguration> {

    public boolean canCreateAction(final OctaveActionConfiguration configuration)  {
        
        String base_dir=configuration.getWorkingDirectory();
        base_dir=Path.getAbsolutePath(base_dir);
        if (base_dir!=null){
            configuration.setWorkingDirectory(base_dir);
            // NOW THE WORKING DIR IS AN ABSOLUTE PATH
        }
        else
            return false;
        
//TODO check if the m file is present and is readable
        
        return true;
    }


    public ECMWFWaveAction createAction(final OctaveActionConfiguration configuration) {
        if(canCreateAction(configuration)){
            return new ECMWFWaveAction(configuration);
        }
        return null;
    }

}
