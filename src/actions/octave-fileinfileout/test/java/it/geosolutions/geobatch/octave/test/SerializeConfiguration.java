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
import it.geosolutions.geobatch.octave.actions.OctaveActionConfiguration;

import com.thoughtworks.xstream.XStream;

public class SerializeConfiguration {
    
    public static void main(String[] args) {
        XStream stream = new XStream();
//        stream.processAnnotations(OctaveFunctionFile.class);
//        stream.processAnnotations(SerializableOctaveFile.class);
//        stream.processAnnotations(SerializableOctaveString.class);
        stream.processAnnotations(OctaveActionConfiguration.class);
        stream.processAnnotations(OctaveEnv.class);
        
        OctaveActionConfiguration oac=new OctaveActionConfiguration();
        
        OctaveEnv<OctaveExecutableSheet> oe=new OctaveEnv<OctaveExecutableSheet>();
        
        OctaveFunctionSheet ofs=new OctaveFunctionSheet();
        
        OctaveFunctionFile off=
            new OctaveFunctionFile("funzione");
        
        ofs.pushFunction(off);
        
//        off.setBuilder(new OctaveFunctionFile("funzione2"));
        off.pushDefinition(new SerializableOctaveFile("file_in",""));
        off.pushDefinition(new SerializableOctaveFile("file_out",""));
        
        //soo.setName("variable_name");
        OctaveFunctionSheet os=new OctaveFunctionSheet();
        os.pushDefinition(new SerializableOctaveFile("variable_name2","VALUE"));
        os.pushDefinition(new SerializableOctaveFile("returning_var_name2","VALUE"));
        os.pushCommand("COMMAND");
        
        oe.push(os);
        oe.push(ofs);
        
        System.out.println("-------------------------------------");
//        oac.setEnv(oe);
        

  //      System.out.println(stream.toXML(oac));
        System.out.println("-------------------------------------");
        
        
        stream.processAnnotations(OctaveFunctionFile.class);
        System.out.println(stream.toXML(oac));
        System.out.println("-------------------------------------");
    }
}
