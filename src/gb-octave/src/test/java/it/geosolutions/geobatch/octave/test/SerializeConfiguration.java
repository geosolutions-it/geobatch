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

package it.geosolutions.geobatch.octave.test;

import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveFunctionFile;
import it.geosolutions.geobatch.octave.OctaveFunctionSheet;
import it.geosolutions.geobatch.octave.SerializableOctaveFile;
import it.geosolutions.geobatch.octave.SerializableOctaveString;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public class SerializeConfiguration {
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        XStream stream = new XStream();
//        stream.processAnnotations(OctaveFunctionSheet.class);
//        stream.processAnnotations(SerializableOctaveFile.class);
//        stream.processAnnotations(SerializableOctaveString.class);
//        stream.processAnnotations(OctaveActionConfiguration.class);
        stream.processAnnotations(OctaveEnv.class);
        stream.processAnnotations(ArrayList.class);
        
        OctaveEnv<OctaveExecutableSheet> oe=new OctaveEnv<OctaveExecutableSheet>();
        
        OctaveFunctionSheet ofs=new OctaveFunctionSheet();
        
            ofs.pushDefinition(new SerializableOctaveString("STRING_NAME", "VALUE"));
            ofs.pushCommand("COMMAND_NAME");
            OctaveFunctionFile off=new OctaveFunctionFile("funzione");
        
                off.pushCommand("COMMAND1");
                off.pushCommand("COMMAND2");

                off.pushDefinition(new SerializableOctaveFile("file_in",""));
                off.pushDefinition(new SerializableOctaveFile("file_out",""));
                
        ofs.pushFunction(off);
        OctaveExecutableSheet ofs2=new OctaveExecutableSheet();
        
            ofs2.pushDefinition(new SerializableOctaveFile("variable_name2","VALUE"));
            ofs2.pushDefinition(new SerializableOctaveFile("returning_var_name2","VALUE"));
            ofs2.pushCommand("COMMAND");
        
        oe.push(ofs);
        oe.push(ofs2);
        
        System.out.println("-------------------------------------");
//        oac.setEnv(oe);
        
        String sob=stream.toXML(oe);
        System.out.println(sob);
        System.out.println("-------------------------------------");
        
        byte[] s=stream.toXML(oe).getBytes();
        
        InputStream is=new ByteArrayInputStream(s);
        
        // get an input stream on the buffer
        DataInputStream dis=new DataInputStream(is);
        
        // build a Hierarchical stream reader
        HierarchicalStreamReader preader=null;
        
//            preader = new BinaryStreamReader(stream.createObjectInputStream(dis));
            ObjectInputStream oos=stream.createObjectInputStream(dis);
            
            
        System.out.println("-------------------------------------");
        System.out.println(stream.fromXML(sob));
        System.out.println("-------------------------------------");
        System.out.println(stream.fromXML(new FileReader(new File("/home/carlo/work/data/rep10workingdir/meteoam/nettuno2/sheet.xml"))));
    }
}
