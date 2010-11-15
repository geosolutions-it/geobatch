/**
 * 
 */
package it.geosolutions.geobatch.nurc.sem.rep10.mars3d;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import it.geosolutions.geobatch.octave.*;

/*
 * @author Carlo Cancellieri
 *
 */
@XStreamAlias("OctaveActionConfiguration")
@XStreamInclude({OctaveEnv.class,
    OctaveExecutableSheet.class,
    OctaveFunctionSheet.class})
public class MARS3DActionConfiguration extends ActionConfiguration implements Configuration {
    
    // Working directory
    private String workingDirectory;
    
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
