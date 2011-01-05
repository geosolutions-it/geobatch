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

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

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
public class OctaveFunctionFile extends OctaveExecutableSheet{
    
    @XStreamOmitField
    protected boolean executable=false;

    public OctaveFunctionFile(){
        super("EMPTY_NAME_FUNCTION",
                new OctaveCommand(""),
                new ArrayList<SerializableOctaveObject<?>>(),
                new ArrayList<SerializableOctaveObject<?>>());
        executable=false;
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
        super("EMPTY_NAME_FUNCTION",
                new OctaveCommand(n),
                new ArrayList<SerializableOctaveObject<?>>(),
                new ArrayList<SerializableOctaveObject<?>>());
        executable=false;
    }
    
    /**
     * @note: this is NOT a copy constructor, use clone instead
     * @param es an OctaveFunctionFile
     */
    public OctaveFunctionFile(OctaveExecutableSheet es){
        super(es.getName(),
                es.getCommands(),
                es.getDefinitions(),
                es.getReturns());
        executable=false;
    }
    
    @Override
    public Object clone(){
        return new OctaveFunctionFile((OctaveExecutableSheet)super.clone());
    }

}
