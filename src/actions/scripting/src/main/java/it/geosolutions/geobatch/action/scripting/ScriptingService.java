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

package it.geosolutions.geobatch.action.scripting;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import java.io.File;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates an Action from a scripting language.
 *
 * @author etj
 */
public class ScriptingService 
	extends BaseService
	implements ActionService<FileSystemMonitorEvent, ScriptingConfiguration> {

    private final static Logger LOGGER = Logger.getLogger(ScriptingService.class.toString());

    public ScriptingAction createAction(ScriptingConfiguration configuration) {
        if("groovy".equalsIgnoreCase(configuration.getLanguage())) {
            return createGroovyAction(configuration);
        } else {
            LOGGER.warning("Can't create an Action for " + configuration);
            return null;
        }
    }

    private ScriptingAction createGroovyAction(ScriptingConfiguration configuration) {
        try {
            // We are not using a generic scripting engine here, because
            // ScriptEngines usually run scripts but do not instantiate classes.

            File script = new File(configuration.getScriptFile());
            GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader());
            Class groovyClass = loader.parseClass(script);

            Constructor constr = groovyClass.getConstructor(ScriptingConfiguration.class);
            GroovyObject groovyObject = (GroovyObject) constr.newInstance(configuration);
            if( ! (groovyObject instanceof ScriptingAction) ) {
                LOGGER.warning("Groovy script does not inherit from ScriptingAction " + groovyObject);
                return null;
            }

            ScriptingAction action = (ScriptingAction)groovyObject;
            return action;

        } catch (InstantiationException ex) {
            LOGGER.log(Level.SEVERE, "Error creating ScriptingAction using " + configuration, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, "Error creating ScriptingAction using " + configuration, ex);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.SEVERE, "Error creating ScriptingAction using " + configuration, ex);
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, "Error creating ScriptingAction using " + configuration, ex);
        } catch (NoSuchMethodException ex) {
            LOGGER.log(Level.SEVERE, "Error creating ScriptingAction using " + configuration, ex);
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, "Error creating ScriptingAction using " + configuration, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error creating ScriptingAction using " + configuration, ex);
        }
        return null;
    }

    public boolean canCreateAction(ScriptingConfiguration configuration) {
        if("groovy".equalsIgnoreCase(configuration.getLanguage()))
            return true;

        // add here other handled languages

        LOGGER.info("Requested unhandled language '"+configuration.getLanguage()+"'");
        return false;
    }

}
