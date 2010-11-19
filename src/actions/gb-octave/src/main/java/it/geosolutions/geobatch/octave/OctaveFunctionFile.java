package it.geosolutions.geobatch.octave;

import java.util.Vector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * Function File
 * A function file must begin with the keyword function.
 * If it does, Octave will assume that it is a function file, and that 
 * it defines a single function that should be evaluated as soon as it
 * is defined.
 * In a function file variables are local variables.
 * 
 * Sub_functions:
 * A function file may contain secondary functions called subfunctions.
 * These secondary functions are only visible to the other functions in
 * the same function file.
 * For example, a file ‘f.m’ containing:
 * 
 *  function f ()
 *      printf ("in f, calling g\n");
 *      g ()
 *  endfunction
 *  function g ()
 *      printf ("in g, calling h\n");
 *      h ()
 *  endfunction
 *  function h ()
 *      printf ("in h\n")
 *   endfunction
 *   
 * defines a main function f and two subfunctions.
 * The subfunctions g and h may only be called from the main function
 * f or from the other subfunctions, but not from outside the file ‘f.m’.
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
@XStreamAlias("function")
@XStreamInclude({
    SerializableOctaveFile.class,
    SerializableOctaveString.class,
    SerializableOctaveObject.class})
public class OctaveFunctionFile{
    
    
    //< function name
    @XStreamAlias("name")
    @XStreamAsAttribute
    private String function;
    
    //< returning name variable list to set
    //@XStreamImplicit(itemFieldName="ret")
    private Vector<SerializableOctaveObject<?>> returns;
    
    //< arguments values
    //@XStreamImplicit(itemFieldName="arg")
    private Vector<SerializableOctaveObject<?>> arguments;
    
    public OctaveFunctionFile(){
        arguments=new Vector<SerializableOctaveObject<?>>();
        returns=new Vector<SerializableOctaveObject<?>>();
    }
    
    /**
     * constructor
     * @note:
     * - use the default function builder
     * - no returning value is set
     * - no arguments are used
     * @param n the function name to call
     */
    public OctaveFunctionFile(String n){
        function=n;
        arguments=new Vector<SerializableOctaveObject<?>>();
        returns=new Vector<SerializableOctaveObject<?>>();
    }
    
    public Vector<SerializableOctaveObject<?>> getReturns(){
        return returns;
    }
    
    public Vector<SerializableOctaveObject<?>> getArguments(){
        return arguments;
    }
    
    public void setName(String n){
        function=n;
    }
    
    public String getName(){
        return function;
    }
    
    public void pushArgument(SerializableOctaveObject<?> soo){
        arguments.add(soo);
    }
    public void pushReturn(SerializableOctaveObject<?> soo){
        returns.add(soo);
    }

}
