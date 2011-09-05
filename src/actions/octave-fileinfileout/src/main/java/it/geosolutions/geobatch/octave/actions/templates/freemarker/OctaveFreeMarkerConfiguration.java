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
package it.geosolutions.geobatch.octave.actions.templates.freemarker;

import it.geosolutions.geobatch.octave.actions.OctaveActionConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class OctaveFreeMarkerConfiguration extends OctaveActionConfiguration {
    
    private Map<String,Object> root;
    
    // TODO MOVE INTO A NURC SPECIFIC CONFIGURATION
    private String cruise;
    private String model;
    private String extension;
    
    private String outDir;
    
    public OctaveFreeMarkerConfiguration(OctaveFreeMarkerConfiguration ac){
        super(ac);
        root=new HashMap<String,Object>();
    }
    
    public Map<String,Object> getRoot(){
        return root;
    }

    /**
     * @param cruise the cruise to set
     */
    public void setCruise(String cruise) {
        this.cruise = cruise;
    }

    /**
     * @return the cruise
     */
    public String getCruise() {
        return cruise;
    }

    /**
     * @param model the model to set
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param outDir the outDir to set
     */
    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    /**
     * @return the outDir
     */
    public String getOutDir() {
        return outDir;
    }

}
