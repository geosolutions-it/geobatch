/**
 * 
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
