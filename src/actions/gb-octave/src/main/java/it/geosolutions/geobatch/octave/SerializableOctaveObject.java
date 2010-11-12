package it.geosolutions.geobatch.octave;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import dk.ange.octave.type.OctaveObject;

@XStreamAlias("var")
public abstract class SerializableOctaveObject<T extends OctaveObject>{
    
    /*
     * Can be IN or OUT variable value
     * if OUT:
     *  will be filled using javaOctave
     * if IN:
     *  will be filled using setVal and
     *  its value will be transferred to
     *  octave using javaOctave before
     *  its usage.
     */
    @XStreamOmitField
    private T _obj; // todo <T extends Number> or <T extends ...>
    
    //< contains the name of this variable
    @XStreamAlias("name")
    @XStreamAsAttribute
    final private String _name;
    
    public SerializableOctaveObject(String name, T obj){
        _obj=obj;
        _name=name;
    }
    
    public final String getName(){
        return _name;
    }
    
    protected final T getOctObj(){
        return _obj;
    }
    
    protected void setOctObj(T obj){
        // TODO: we may want to 
        //check if _obj is already !null
        // throwing exception
        _obj=obj;
    }
    
    protected abstract void setVal();
    

}
