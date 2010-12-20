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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 * @param <T> the specialization of this class
 */
@XStreamAlias("octave")
public class OctaveEnv<T extends OctaveExecutableSheet>{
    
    // used to make thread return synchronization
    @XStreamOmitField
    private final long uniqueID;
    
    @XStreamAlias("sheets")
    private final List<T> env;
    
    public final int size(){
        return env.size();
    }
    
    public final long getUniqueID(){
        return uniqueID;
    }
    
    public OctaveExecutableSheet pop(){
        if (env.isEmpty())
            return null;
        else {
            OctaveExecutableSheet os=getSheet(0);
            env.remove(os);
            return os;
        }
    }
    
    public T getSheet(int index) throws IndexOutOfBoundsException{
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
    
    /**
     * add a list of sheet to the environment
     * @param os
     */
    public void push(List<T> os){
        if (os!=null)
            for(T t:os)
                env.add(t);
        //else
// TODO LOG
    }
    
    private final long generateID(){
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).longValue();
    }
    
    public OctaveEnv(){
        env=new ArrayList<T>();
        uniqueID=generateID();
//        global=new OctaveExecutableSheet();
    }
    
    public OctaveEnv(List<T> e){
        env=new ArrayList<T>(e);
        uniqueID=generateID();
//        global=new OctaveExecutableSheet();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object clone(){
        OctaveEnv<T> oe=new OctaveEnv<T>();
        int index=0;
        int size=this.env.size();
        while(index<size){
            T t=this.env.get(index++);
            oe.push((T)t.clone());
        }
        return oe;
//        global(this.global);
    }

    public OctaveEnv(OctaveEnv<T> environment){
        uniqueID=generateID();
        if (environment!=null){
            this.env=environment.env;
//            this.global=new OctaveExecutableSheet(environment.global);
        }
        else{
// TODO LOG
            env=new ArrayList<T>();
//            global=new OctaveExecutableSheet();
        }
    }
}
