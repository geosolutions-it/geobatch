/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.action.scripting;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an Action from a scripting language.
 * 
 * @author etj
 */
public class ScriptingService extends BaseService implements
        ActionService<FileSystemEvent, ScriptingConfiguration> {

    public ScriptingService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(ScriptingService.class.toString());

    public ScriptingAction createAction(ScriptingConfiguration configuration) {
        try {
            return new ScriptingAction(configuration);
        } catch (IOException e) {
            LOGGER.error("Error occurred creating scripting Action... " + e.getLocalizedMessage(),
                    e);
        }

        return null;
    }

    /**
	 * 
	 */
    public boolean canCreateAction(ScriptingConfiguration configuration) {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName(configuration.getLanguage());

        if (engine == null) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Requested unhandled language '" + configuration.getLanguage() + "'");
            return false;
        }
        return true;
    }

}
