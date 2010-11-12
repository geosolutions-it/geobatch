package it.geosolutions.geobatch.octave;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveString;

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
@XStreamAlias("OctaveFunction")
@XStreamInclude({
    SerializableOctaveFile.class,
    SerializableOctaveString.class,
    SerializableOctaveObject.class})
public class OctaveFunctionFile implements Runnable {
    
    @XStreamOmitField
    private OctaveEngine engine=null;
    
    //< function name
    @XStreamAlias("name")
    @XStreamAsAttribute
    private String function;
    
    //< returning name variable list to set
    //@XStreamImplicit(itemFieldName="ret")
    private ArrayList<SerializableOctaveObject<?>> ret;
    
    //< arguments values
    //@XStreamImplicit(itemFieldName="arg")
    private ArrayList<SerializableOctaveObject<?>> arguments;
    
    public OctaveFunctionFile(){
        arguments=new ArrayList<SerializableOctaveObject<?>>();
        ret=new ArrayList<SerializableOctaveObject<?>>();
    }
    
    public OctaveFunctionFile(String n){
        function=n;
        arguments=new ArrayList<SerializableOctaveObject<?>>();
        ret=new ArrayList<SerializableOctaveObject<?>>();
    }
    
    private void init(){
        if (engine==null){
            engine=new OctaveEngineFactory().getScriptEngine();
        }
    }
    
    public void close(){
        if (engine!=null){
            engine.close();
        }
    }
    
    public List<SerializableOctaveObject<?>> getReturnList(){
        return ret;
    }
    
    public List<SerializableOctaveObject<?>> getArgumentsList(){
        return arguments;
    }
    
    /**
     * no 'runnable' tests are performed
     * @param run
     */
    public void eval(String run){
        init();
        engine.eval(run);
    }
    
    /**
     * Built-in Function: exist (function)
     * Return:
     * 1 - if the function exists as a variable
     * 2 - if the function (after appending `.m') is a function file in the path.
     * 0 - Otherwise.
     */
    public boolean isRunnable(String _f){
        if (engine==null) {
            init();
        }
        engine.eval("ret=exist(\'"+_f+"\');");
// TODO check
        OctaveDouble r=engine.get(OctaveDouble.class,"ret");
        
        if (r.get(1)==1)
            return false;
        else if (r.get(1)==2)
            return true;
        else
            return true;
    }

    public void run() {
        if (isRunnable(function))
            exec(arguments);
        else
;//TODO  log
    }
    
    private void exec(List<SerializableOctaveObject<?>> list){
        String script="";
        /**
         * if function returns more than a value
         * it is in the form:
         * [ret1,ret2,...,retN]=function(...);
         */
        if (ret!=null){
            if (ret.size()>1) {
                Iterator<SerializableOctaveObject<?>> i=ret.iterator();
                script="["+i.next();
                while (i.hasNext()){
                    script+=","+i.next().getName();
                }
                script+="]=";
            }
            /**
             * if function return only a value
             * it is int the form:
             * ret1 = function(...);
             */
            else if (ret.size()==1){
                Iterator<SerializableOctaveObject<?>> i=ret.iterator();
                script=i.next().getName()+"=";
            }
            /**
             * else
             * function do not return values
             * function(...);
             */
        }
        
        script+=function;
        
        if (arguments!=null){
            /**
             * if function has more than a input parameter
             * it is in the form:
             * ... function(arg1,arg2,...,argN);
             */
            if (arguments.size()>1) {
                Iterator<SerializableOctaveObject<?>> i=arguments.iterator();
                script+="("+i.next().getName();
                while (i.hasNext()){
                    script+=","+i.next().getName();
                }
                script+=")";
            }
            /**
             * if function has only one input parameter
             * it is int the form:
             * ... function(arg1);
             */
            else if (arguments.size()==1){
                Iterator<SerializableOctaveObject<?>> i=arguments.iterator();
                script+="("+i.next().getName()+")";
            }
            /**
             * else
             * function has not input parameter
             * ... function;
             */
        } //endif arguments!=null
        
        script+=";";
        
        try{
            // fill in serialized values into octave variables
            for (SerializableOctaveObject<?> soo : arguments){
                soo.setVal();
System.out.println("VAL:"+((OctaveString)soo.getOctObj()).getString());
                engine.put(soo.getName(),soo.getOctObj());
            }
            
            // run script in Octave    
            engine.eval(script);
            
            // store results
            for (SerializableOctaveObject<?> soo : ret){     
// TODO GENERALIZE
                engine.get(OctaveString.class,soo.getName());
            }
        }
        catch (OctaveEvalException oee){
//TODO LOG
            oee.printStackTrace();
        }
    }
    
    public void setName(String n){
        function=n;
    }
    
    public void addArg(SerializableOctaveObject<?> soo){
        arguments.add(soo);
    }
    public void addRet(SerializableOctaveObject<?> soo){
        ret.add(soo);
    }

}
