package it.geosolutions.geobatch.octave;

import java.util.logging.Logger;
/**
 * @TODO this class should implement a MessageDrivenPOJO's interface
 * to add messages ('OctaveSheet' to the octaveEnv environment which is
 * handled by the consumer OctaveThread.
 * @note A future work can consists in implementing a proxy or gateway pattern
 * to define number of running octave processes.  
 * 
 * ...
 * @TODO complete description
 * 
 * @author Carlo Cancellieri, ccancellieri AT geo-solutions.it, GeoSolutions
 */
public class OctaveProcess{

    private final static Logger LOGGER = Logger.getLogger(OctaveProcess.class.toString());
    
    private final OctaveConfiguration conf;
    
    private final OctaveThread octave;
    
    /**
     * Constructor
     * @param actionConfiguration configuration for this action.
     */
    public OctaveProcess(OctaveConfiguration configuration) {
        conf=configuration;
        octave=new OctaveThread(configuration.getEnv());
        new Thread(octave).start();
    }
    
    public final OctaveEnv getEnv(){
        return conf.getEnv();
    }
    
    /**
     * @note: commented out since this is no more needed
     * 
     * anyway still persists: 
// TODO: check... should we add this member to the BaseAction?
     * get configuration
     * @return configuration of this action
    protected final OctaveConfiguration getConfig(){
        return config;
    }
    */
}
