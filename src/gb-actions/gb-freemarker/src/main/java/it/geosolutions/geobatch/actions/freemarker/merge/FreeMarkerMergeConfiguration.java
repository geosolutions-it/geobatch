/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.freemarker.merge;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import java.util.HashMap;

import java.util.Map;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class FreeMarkerMergeConfiguration 
        extends ActionConfiguration 
        implements Configuration {
    
    public FreeMarkerMergeConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * The freemarker template file
     */
    private String templateFile;

    /**
     * output file
     */
    private String outputFile;

    /**
     * Values in the values map that can be overridden by the input file
     */
    private Map<String, Object> defaultValues = new HashMap<String, Object>();

    /**
     * Values in the values map that can <b>not</B> be overridden by the input file
     */    
    private Map<String, Object> forcedValues  = new HashMap<String, Object>();

    public Map<String, Object> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(Map<String, Object> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public Map<String, Object> getForcedValues() {
        return forcedValues;
    }

    public void setForcedValues(Map<String, Object> forcedValues) {
        this.forcedValues = forcedValues;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }


}
