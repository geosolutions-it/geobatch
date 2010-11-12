package it.geosolutions.geobatch.octave;

import dk.ange.octave.type.OctaveObject;

public class OctaveInt implements OctaveObject {

    private int _val;
    
    public OctaveObject shallowCopy() {
        return this;
    }
    
    public int getVal(){
        return _val;
    }
    
    public void setVal(int val){
        _val=val;
    }

}
