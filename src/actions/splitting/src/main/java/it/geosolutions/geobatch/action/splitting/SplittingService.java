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
package it.geosolutions.geobatch.action.splitting;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates an Action from a scripting language.
 * 
 * @author etj
 */
public class SplittingService extends BaseService implements
        ActionService<FileSystemMonitorEvent, SplittingConfiguration> {

    private final static Logger LOGGER = Logger.getLogger(SplittingService.class.toString());

    public SplittingAction createAction(SplittingConfiguration configuration) {
        try {
            return new SplittingAction(configuration);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurred creating scripting Action... "
                    + e.getLocalizedMessage(), e);
        }

        return null;
    }

    /**
	 * 
	 */
    public boolean canCreateAction(SplittingConfiguration configuration) {
        return true;
    }

}
