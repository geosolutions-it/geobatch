package it.geosolutions.geobatch.octave;

import java.util.Vector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamAlias("sheet")
@XStreamInclude({
    OctaveFunctionFile.class,
    SerializableOctaveFile.class,
    SerializableOctaveString.class,
    SerializableOctaveObject.class})
public class OctaveFunctionSheet extends OctaveExecutableSheet{

    // functions
    @XStreamAlias("functions")
    private final Vector<OctaveFunctionFile> functions;
    
    
    public OctaveFunctionSheet(Vector<String> com,
            Vector<SerializableOctaveObject<?>> defs,
            Vector<OctaveFunctionFile> functs,
            Vector<SerializableOctaveObject<?>> rets){
        super(com,defs,rets);
        functions=functs;
    }
    
    public OctaveFunctionSheet(){
        super();
        functions=new Vector<OctaveFunctionFile>();
    }
    
    public Vector<OctaveFunctionFile> getFunctions(){
        return functions;
    }
    
    public boolean hasFunctions(){
        if (functions.isEmpty())
            return false;
        else
            return true;
    }
    
    public  OctaveFunctionFile popFunction(){
        if (functions.isEmpty())
            return null;
        else {
            OctaveFunctionFile f=functions.firstElement();
            functions.remove(0);
            return f;
        }
    }
    
    public void pushFunction(OctaveFunctionFile f){
        functions.add(f);
    }
    
    public void pushFunctions(Vector<OctaveFunctionFile> fs){
        functions.addAll(fs);
    }
}


