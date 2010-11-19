package it.geosolutions.geobatch.octave;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import dk.ange.octave.type.OctaveString;

@XStreamAlias("OctaveString")
public class SerializableOctaveString  extends SerializableOctaveObject<OctaveString>{
    
    //< contains the value of this variable
    @XStreamAlias("value")
    @XStreamAsAttribute
    private String _val;
    
    @XStreamOmitField
    private boolean _sync=false;
    
    public SerializableOctaveString(String name,String val){
        super(name,new OctaveString(val));
        _val=val;
        _sync=true;
    }
    
    @Deprecated
    public String getSerializedValue(){
        return getOctObj().getString();
    }
    
    public String getValue(){
        if (!_sync){
            setVal();
        }
        return getOctObj().getString();
    }

    
    public void reSetVal(String s) {
        if (getOctObj()!=null)
            getOctObj().setString(s);
        else
            setOctObj(new OctaveString(s));
        _val=s;
        _sync=true;
    }
    
    /**
     * This is executed by getValue()
     * 
     * @note: this is public since constructor
     * is never called by XStream so, to synchronize
     * variables we need to call it manually.
     */
    public void setVal() {
        reSetVal(_val);
    }
    
    
}
