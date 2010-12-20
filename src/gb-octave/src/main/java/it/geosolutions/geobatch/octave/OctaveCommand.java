package it.geosolutions.geobatch.octave;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Represents an octave command instance with its execution flag 
 * @author Carlo Cancellieri
 *
 */
@XStreamAlias("OctaveCommand")
public class OctaveCommand {
    // Octave command to execute
    @XStreamAlias("command")
    private final String command;
    // execution flag
    @XStreamAsAttribute
    private boolean executed=false;
    
    public OctaveCommand(String comm){
        command=comm;
    }
    
    @Override
    public Object clone(){
        OctaveCommand oc=new OctaveCommand(new String(command));
        return oc;
    }
    
    public String getCommand(){
        return command;
    }
    
    public void reset(){
        executed=false;
    }
    
    public void set(){
        executed=true;
    }
    
    public boolean isExecuted(){
        return executed;
    }

}
