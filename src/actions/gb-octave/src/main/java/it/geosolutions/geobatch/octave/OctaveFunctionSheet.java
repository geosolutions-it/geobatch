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


