/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.type.OctaveObject;

@XStreamInclude({
//TODO add extensions here
    SerializableOctaveObject.class,
    SerializableOctaveFile.class,
    SerializableOctaveString.class,
    OctaveObject.class})
public abstract class SerializableOctaveObject<T extends OctaveObject>{
    
    /*
     * Can be IN or OUT variable value
     * if OUT (out from octave):
     *  will be filled using javaOctave
     * if IN (in from octave):
     *  will be filled using setVal and
     *  its value will be transferred to
     *  octave using javaOctave before
     *  its usage.
     */
    @XStreamOmitField
    protected T _obj; // todo <T extends Number> or <T extends ...>
    
    //< contains the name of this variable
    @XStreamAlias("name")
    @XStreamAsAttribute
    private String _name;
    
    public SerializableOctaveObject(String name, T obj){
        _obj=obj;
        _name=name;
    }
    
    public final String getName(){
        return _name;
    }
    
    public void setName(String name){
        _name=name;
    }
    
    /**
     * get the value from the octave env
     * @param engine - the engine to use to get
     */
    public abstract T get(OctaveEngine engine);
    
    @Override
    public abstract Object clone();
    
    protected abstract T getOctObj();
    
    protected abstract void setOctObj(T obj);
    
    public abstract void setVal();
    

}
