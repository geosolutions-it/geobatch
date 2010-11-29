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

package it.geosolutions.geobatch.octave.actions;

import java.io.File;
import java.io.IOException;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveFunctionSheet;
import it.geosolutions.geobatch.utils.IOUtils;

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
public class OctaveActionConfiguration extends ActionConfiguration implements Configuration {
    
    // Working directory
    private String workingDirectory;
    
    @XStreamAlias("octave")
    private OctaveEnv<OctaveExecutableSheet> env;
    
    public final OctaveEnv<OctaveExecutableSheet> getEnv(){
        return env;
    }
    
    /**
     * Obtaining the Absolute path of the working dir
     * @param working_dir the relative (or absolute) path to absolutize
     * @note it should be a sub-dir of ...
* @TODO open a ticket to get getBaseDirectory() into Catalog interface
     */
    public static String absolutize(String working_dir) /*throws FileNotFoundException */{ 
        FileBaseCatalog c=(FileBaseCatalog) CatalogHolder.getCatalog();
        File fo=null;
        try {
            fo=IOUtils.findLocation(working_dir,new File(c.getBaseDirectory()));
        }catch (IOException ioe){
            return null;
        }
        
        if (fo!=null){
            return fo.toString();
        }
        else {
//TODO LOG            throw new FileNotFoundException("Unable to locate the working dir");
//            throw new FileNotFoundException();
            return null;
        }
        
    }
    
    /**
     * return the workingdir 
     * @return 
     */
    public final String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * set workingdirectory
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}
