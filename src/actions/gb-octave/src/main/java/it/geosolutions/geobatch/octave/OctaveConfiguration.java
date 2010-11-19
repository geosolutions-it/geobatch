/**
 * 
 */
package it.geosolutions.geobatch.octave;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/*
 * @author Carlo Cancellieri
 *
 */
@XStreamAlias("OctaveActionConfiguration")
@XStreamInclude({OctaveFunctionFile.class})
public class OctaveConfiguration {
    
    // Working directory
    private String workingDirectory;
    
    private static OctaveEnv<?> env;
    
    public final OctaveEnv<?> getEnv(){
        return env;
    }

    public final String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}
