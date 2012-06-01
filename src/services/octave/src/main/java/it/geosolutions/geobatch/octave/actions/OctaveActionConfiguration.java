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

package it.geosolutions.geobatch.octave.actions;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveFunctionSheet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/*
 * @author Carlo Cancellieri
 *
 */
@XStreamAlias("OctaveConfiguration")
@XStreamInclude({
    OctaveEnv.class,
    OctaveExecutableSheet.class,
    OctaveFunctionSheet.class})
public class OctaveActionConfiguration extends ActionConfiguration {
    
    public OctaveActionConfiguration(ActionConfiguration ac){
        super(ac.getId(),ac.getName(),ac.getDescription());
    }
    /*
     * represents the file containing the Octave environment to execute
     */
    @XStreamAlias("execute")
    private String env;
    
    public final String getEnv(){
        return env;
    }
    
    public void setEnv(String ex){
        env=ex;
    }
}
