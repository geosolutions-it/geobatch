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
package it.geosolutions.geobatch.##NAME_APP##.##NAME_ACT##;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class ##NAME_ACT##GeneratorService extends BaseService implements
        ActionService<EventObject, ##NAME_ACT##Configuration> {

    public ##NAME_ACT##GeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(##NAME_ACT##GeneratorService.class);

    public ##NAME_ACT##Action createAction(##NAME_ACT##Configuration configuration) {
        try {
            return new ##NAME_ACT##Action(configuration);
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    private String getAbsolutePath(String working_dir) {
		if (working_dir == null)
			return null;
		final File working_dirFile = new File(working_dir);
		if (working_dirFile.isAbsolute() || working_dirFile.isFile()
				|| working_dirFile.isDirectory()) {
			try {
				return working_dirFile.getCanonicalPath();
			} catch (IOException e) {
				return null;
			}
		}

		final FileBaseCatalog c = (FileBaseCatalog) CatalogHolder.getCatalog();
		if (c == null)
			return null;

		try {
			File fo = it.geosolutions.tools.commons.file.Path.findLocation(
					working_dir, c.getConfigDirectory());
			if (fo != null) {
				return fo.toString();
			}
		} catch (Exception e) {
			// eat
		}
		return null;
	}
    
    public boolean canCreateAction(##NAME_ACT##Configuration configuration) {
        try {
            // absolutize working dir
            String wd = getAbsolutePath(configuration.getWorkingDirectory());
            if (wd != null) {
                configuration.setWorkingDirectory(wd);
                return true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("##NAME_ACT##GeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get an absolute working dir.");
            }
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

}
