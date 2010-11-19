/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geobatch.octave;

import java.util.Iterator;
import java.util.Vector;

public abstract class DefaultFunctionBuilder {
    
    /**
     * Transform a OctaveFunctionSheet in a OctaveExecutableSheet
     * 
     * @param sheet input output variable:
     * input is a OctaveFunctionSheet to be preprocessed
     * output can be used as a OctaveExecutableSheet
     */
    public final void preprocess(OctaveFunctionSheet sheet) throws Exception {
        // pre-process all functions in the sheet
        for (OctaveFunctionFile f:sheet.getFunctions()){
            // build the function string
            String comm=this.buildFunction(f);
            /**
             * [OPTIONAL]:
             * eventually define variables into environment
             * This is optional since buildFunction() can 
             * extract all the definitions putting their 'string
             * value representation' directly into the function
             * string.
             * F.E.:
             * A="123" [<- definition]
             * function(A); [<- function call]
             * becomes
             * function('123'); [<- function call]
             */
            sheet.pushDefinitions(f.getArguments());
            // evaluate function
            sheet.pushCommand(comm);
            /**
             * COMMENTED OUT
             * this can't be done here since function
             * should still be evaluated by the engine
             * clean will be done by the OctaveThread
             * on all the sheet's definitions
             * @see OctaveThread.run()
            // clear function environment
            // engine.clear(f.getArguments());
            */
            // function returns becomes sheet returns
            sheet.pushReturns(f.getReturns());
        }
    }
    
    /**
     * Generate a string functions serializing informations extracted
     * from returns ad arguments building something like:
     * 
     * [ret1,ret2,...,retN]=function_name(arg1,arg2,...,argN);
     * 
     * @return a string as above
     * @note this can be overrided
     */
    protected String buildFunction(OctaveFunctionFile off) throws Exception{
        String function=off.getName();
        Vector<SerializableOctaveObject<?>> returns=off.getReturns();
        Vector<SerializableOctaveObject<?>> arguments=off.getArguments();
        
        String script="";
        /**
         * if function returns more than a value
         * it is in the form:
         * [ret1,ret2,...,retN]=function(...);
         */
        if (returns!=null){
            if (returns.size()>1) {
                Iterator<SerializableOctaveObject<?>> i=returns.iterator();
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
            else if (returns.size()==1){
                Iterator<SerializableOctaveObject<?>> i=returns.iterator();
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
        
        return script;
    }
}
