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
package it.geosolutions.geobatch.actions.xstream;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */

public class XstreamConfiguration extends ActionConfiguration implements Configuration {

    public XstreamConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * 
     * @param id
     * @param name
     * @param description
     * @param out
     *            the output filtered (resulting) file
     */
    public XstreamConfiguration(final String id,final String name,final String description, final String out) {
        super(id, name, description);
        output = out;
        alias= new HashMap<String, String>();
    }

    // path where to write
    private String output;
    
    private Map<String, String> alias;

    public Map<String, String> getAlias() {
        return alias;
    }

    public void setAlias(Map<String, String> alias) {
        this.alias = alias ;
    }

    public void setOutput(String s) {
        output = s;
    }


    /**
     * return the template path
     * 
     * @return the output path
     * 
     */
    public final String getOutput() {
        return output;
    }

    @Override
    public XstreamConfiguration clone(){
        final XstreamConfiguration ret=(XstreamConfiguration)super.clone();
        ret.setWorkingDirectory(this.getWorkingDirectory());
        ret.setServiceID(this.getServiceID());
        ret.setOutput(this.getOutput());
        ret.setAlias(this.getAlias()); // TODO copy!!!
        ret.setListenerConfigurations(ret.getListenerConfigurations());
        return ret;
    }
}
