/**
 * 
 */
package it.geosolutions.geobatch.octave;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/*
 * @author Carlo Cancellieri
 *
 */
@XStreamAlias("OctaveActionConfiguration")
@XStreamInclude({OctaveFunctionFile.class})
public class OctaveActionConfiguration extends ActionConfiguration implements Configuration {
    
    // Working directory
    private String workingDirectory;
    
    // Octave/Matlab script path
    private ArrayList<String> addPath=null;
    
    // Function to run
    @XStreamAlias("OctaveFunction")
    private OctaveFunctionFile octaveFunction;


    /**
     * @note can return null
     * @return
     */
    public final ArrayList<String> getPathList() {
        return addPath;
    }

    public void addPath(String octavePath) {
        if (addPath==null)
            addPath=new ArrayList<String>();
        addPath.add(octavePath);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
    public final OctaveFunctionFile getFunction(){
        return octaveFunction;
    }
    
    public void setFunction(OctaveFunctionFile off){
        octaveFunction=off;
    }
}
