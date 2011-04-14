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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.ange.octave.exception.OctaveException;
import dk.ange.octave.exception.OctaveParseException;

public class SheetPreprocessor {
    private Map<String, SheetBuilder> preprocessors=new HashMap<String, SheetBuilder>();
    
    private final static Logger LOGGER = LoggerFactory.getLogger(SheetPreprocessor.class.toString());
    
    public final void addBuilder(String name, SheetBuilder sb){
        preprocessors.put(name, sb);
    }
    
    public final void removeBuilder(String name){
        preprocessors.remove(name);
    }
    
    /**
     * Transform all OctaveFunctionSheet in OctaveExecutableSheet
     * 
     * @param sheet input output variable:
     * input is a OctaveFunctionSheet to be preprocessed
     * output can be used as a OctaveExecutableSheet
     */
    public final void preprocess(OctaveEnv<OctaveExecutableSheet> env) throws OctaveException {
        int size=env.size();
        int i=0;
        while (i<size){
            // for each Sheet into the env
            OctaveExecutableSheet es=env.getSheet(i++);
            // if it is a FunctionSheet
            if (es instanceof OctaveFunctionSheet){
                /**
                 * this is a sheet containing a function
                 * which will produce a new OctaveExecutableSheet
                 */
                OctaveFunctionSheet sheet=(OctaveFunctionSheet) es;
                // pre-process all functions in the sheet
                while (sheet.hasFunctions()){
                    OctaveFunctionFile f=sheet.popFunction();
                    SheetBuilder sb=null;
                    if ((sb=preprocessors.get(f.getName()))!=null){
                        // build the executable sheet
                        try {
                            OctaveExecutableSheet oes=sb.buildSheet(f);
                            /*
                             * TODO check this could be redundant
                             * probably we may want to add on top of the list a sheet
                             * pushing definitions and at the bottom, a sheet getting
                             * returns. 
                             */
                            // appending parent definitions
                            oes.pushDefinitions(sheet.getDefinitions());
                            // appending parent returns
                            oes.pushReturns(sheet.getReturns());
                            
                            env.push(oes);
                        }
                        catch (OctaveException oe){
                            if (LOGGER.isErrorEnabled())
                                LOGGER.error("Unable to build the sheet named: "+f.getName()
                                        +" message is:\n"+oe.getLocalizedMessage());
// debug
oe.printStackTrace();
                            throw oe;
                        }
                    }
                    else {
                        String message="No preprocessor found for the OctaveFunctionSheet named "+f.getName();
                        if (LOGGER.isInfoEnabled())
                            LOGGER.info(message);
                        throw new OctaveParseException(message);
                    }
                }
            }
        }
    }

}
