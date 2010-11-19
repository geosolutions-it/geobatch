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

import java.util.List;
import java.util.Vector;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveString;

/**
 * This is a primitive Octave Engine Layer
 * @author carlo cancellieri
 *
 */
public class Engine{
     
    private final OctaveEngine engine;

    public Engine(){
        engine=new OctaveEngineFactory().getScriptEngine();
    }
    
    protected void close(){
        engine.close();
    }
    
    /**
     * 
     * @param run
     */
    protected void eval(String run){
        engine.eval(run);
    }
    
    /**
     * Built-in Function: exist (function)
     * Return:
     * 1 - if the function exists as a variable
     * 2 - if the function (after appending `.m') is a function file in the path.
     * 0 - Otherwise.
     */
    protected boolean isRunnable(String _f){
        engine.eval("ret=exist(\'"+_f+"\');");
        OctaveDouble r=engine.get(OctaveDouble.class,"ret");
        if (r.get(1)==1)
            return false;
        else if (r.get(1)==2)
            return true;
        else
            return true;
    }
    
    /**
     * clear passed vector of definitions from environment
     * @param defs
     */
    protected void clear(Vector<SerializableOctaveObject<?>> defs){
        for (SerializableOctaveObject<?> d:defs){
            clear(d);
        }
    }
    
    /**
     * clear passed definition from environment
     * @param def
     */
    protected void clear(SerializableOctaveObject<?> def){
        engine.eval("clear \'"+def.getName()+"\';");
    }
    
    /**
     * (variable definition)
     * @param list
     */
    protected void put(List<SerializableOctaveObject<?>> list){
        synchronized (list) {
            // fill in serialized values into octave (variable definition)
            for (SerializableOctaveObject<?> soo : list){
                soo.setVal();
                engine.put(soo.getName(),soo.getOctObj());
            }   
        }
    }
    
    /**
     * reading variables from octave env
     * @param list
     */
    protected void get(List<SerializableOctaveObject<?>> list){
        synchronized(list){
            // store results
            for (SerializableOctaveObject<?> soo : list){
//TODO GENERALIZE
                engine.get(OctaveString.class,soo.getName());
            }
        }
    }
}
