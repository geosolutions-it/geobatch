package it.geosolutions.geobatch.nurc.sem.rep10.mars3d;

import java.util.Iterator;
import java.util.Vector;

import it.geosolutions.geobatch.octave.*;

public class MARS3DFunctionBuilder extends DefaultFunctionBuilder {
    
    /**
     * The prototype of the mars3d function is:
     * mars3d(file_in,file_out);
     */
    @Override
    public String buildFunction(OctaveFunctionFile<?> off) throws Exception{
        // name should be -> mars3d
        String function=off.getName();
// no returns value
//        Vector<SerializableOctaveObject<?>> returns=off.getReturns();
        Vector<SerializableOctaveObject<?>> arguments=off.getArguments();
        /**
        String script="";
        
         * COMMENTED OUT SINCE NO RETURN IS NEEDED
         * if function returns more than a value
         * it is in the form:
         * [ret1,ret2,...,retN]=function(...);
         
        if (returns!=null){
            if (returns.size()>1) {
                Iterator<SerializableOctaveObject<?>> i=returns.iterator();
                script="["+i.next();
                while (i.hasNext()){
                    script+=","+i.next().getName();
                }
                script+="]=";
            }
            else if (returns.size()==1){
                Iterator<SerializableOctaveObject<?>> i=returns.iterator();
                script=i.next().getName()+"=";
            }
        }
         */
        String script=function;
        
        if (arguments!=null){
            /**
             * if function has more than a input parameter
             * it is in the form:
             * ... function(arg1,arg2);
             */
            if (arguments.size()==2) {
                /**
                 * @note: Here we suppose that
                 * getName returns serialized value which is
                 * modified into the execute() method of the
                 * MARS3DAction class.
                 * Variable Name should be substituted with
                 * the file name it is representing:
                 * arguments.get(0).getName() -> file_in
                 * arguments.get(1).getName() -> file_out
                 */
                script+="("+arguments.get(0).getName()+
                    ","+arguments.get(1).getName()+");";
            }
            /**
             * if function has only one input parameter
             * it is int the form:
             * ... function(arg1);
             */
            else
                throw new Exception("Argument list of "+function+
                        " should contain at least 2 arguments!");
        } //endif arguments!=null
        else
            throw new Exception("Argument list of "+function+" is empty!");
        
        return script;
    }
}
