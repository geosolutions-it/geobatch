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

import it.geosolutions.geobatch.catalog.Configuration;
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
@XStreamAlias("MARS3DActionConfiguration")
@XStreamInclude({
    OctaveEnv.class,
    OctaveExecutableSheet.class,
    OctaveFunctionSheet.class})
public class MARS3DActionConfiguration extends ActionConfiguration implements Configuration {
    
    // Working directory
    private String workingDirectory;
    
    @XStreamAlias("octave")
    private OctaveEnv env;
    
    public final OctaveEnv getEnv(){
        return env;
    }

    public final String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}
