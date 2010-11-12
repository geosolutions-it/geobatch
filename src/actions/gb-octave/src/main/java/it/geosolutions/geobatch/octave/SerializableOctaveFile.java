package it.geosolutions.geobatch.octave;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("OctaveFile")
public class SerializableOctaveFile extends SerializableOctaveString{
    
    //< contains the value of this variable
    @XStreamAsAttribute
    @XStreamAlias("output")
    private boolean _output=false;
    
    //< contains the value of this variable
    @XStreamAsAttribute
    @XStreamAlias("input")
    private boolean _input=true;
    
    public SerializableOctaveFile(String name,String val){
        super(name,val);
    }
    
    /**
     * is it an input file?
     * @return
     */
    public boolean isInput(){
        return _input;
    }
    
    /**
     * is it an output file?
     * @return
     */
    public boolean isOutput(){
        return _output;
    }
}
