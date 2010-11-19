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
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

public class MARS3DGeneratorService
    extends BaseService 
    implements ActionService<FileSystemMonitorEvent, MARS3DActionConfiguration> {

    public boolean canCreateAction(final MARS3DActionConfiguration configuration) {
// TODO test if this action can run given the configuration this means:
// 1 check if the m file is present and is readable
        return true;
    }

    public MARS3DAction createAction(final MARS3DActionConfiguration configuration) {
        if(canCreateAction(configuration))
            return new MARS3DAction(configuration);
        return null;
    }

}
