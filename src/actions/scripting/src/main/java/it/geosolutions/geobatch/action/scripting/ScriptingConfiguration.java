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

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import java.util.Collections;
import java.util.Map;

public class ScriptingConfiguration extends ActionConfiguration implements Configuration {

    public ScriptingConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Language of the script (e.g.: groovy, ruby, etc). Used by the Service to instantiate the
     * proper engine.
     */
    private String language = null;

    /**
     * The script file. Used by the Service.
     */
    private String scriptFile = null;

    /**
     * Script params are stored as String properties, since it must be really dynamic and we don't
     * want to configure the Conf marshaller (e.g. XStream) with its details.
     */
    private Map<String, String> properties = null;

//    public ScriptingConfiguration() {
//        super();
//    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(String scriptFullPath) {
        this.scriptFile = scriptFullPath;
    }

    @Override
    public ScriptingConfiguration clone() {
        final ScriptingConfiguration configuration = (ScriptingConfiguration) super.clone();

        configuration.setLanguage(getLanguage());
        if (getProperties()!=null)
            configuration.setProperties(Collections.unmodifiableMap(getProperties()));
        configuration.setScriptFile(getScriptFile());

        return configuration;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + ", serviceId:" + getServiceID()
                + ", name:" + getName() + ", lang:" + getLanguage() + ", script:" + getScriptFile()
                + "]";
    }

}
