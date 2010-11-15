package it.geosolutions.geobatch.octave;

import java.util.Vector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("octave")
public class OctaveEnv<T extends OctaveExecutableSheet> {
    @XStreamAlias("sheets")
    private final Vector<T> env;
    
/**
 * @TODO change to simple variable environment container 
 */
    // containing global variables
    @XStreamOmitField
    public final OctaveExecutableSheet global;
    
    public OctaveExecutableSheet pop(){
        if (env.isEmpty())
            return null;
        else {
            OctaveExecutableSheet os=env.firstElement();
            env.remove(0);
            return os;
        }
    }
    
    public T getEnv(int index) throws IndexOutOfBoundsException{
        if (env.size()>index)
            return env.get(index);
        else
            throw
                new IndexOutOfBoundsException(
                    "Unable to get sheet at index "+index);
    }
    
    public boolean hasNext(){
        if (env.isEmpty())
            return false;
        else
            return true;
    }
    
    public void push(T os){
        env.add(os);
    }
    
    public OctaveEnv(){
        env=new Vector<T>();
        global=new OctaveExecutableSheet();
    }
    
    public OctaveEnv(Vector<T> e){
        env=new Vector<T>(e);
        global=new OctaveExecutableSheet();
    }
}
