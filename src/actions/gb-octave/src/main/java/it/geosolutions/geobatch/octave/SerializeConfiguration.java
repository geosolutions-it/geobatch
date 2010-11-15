package it.geosolutions.geobatch.octave;

import com.thoughtworks.xstream.XStream;

public class SerializeConfiguration {
    
    public static void main(String[] args) {
        XStream stream = new XStream();
//        stream.processAnnotations(OctaveFunctionFile.class);
//        stream.processAnnotations(SerializableOctaveFile.class);
//        stream.processAnnotations(SerializableOctaveString.class);
        stream.processAnnotations(OctaveEnv.class);
        OctaveEnv oac=new OctaveEnv();

        OctaveFunctionFile<DefaultFunctionBuilder> off=
            new OctaveFunctionFile<DefaultFunctionBuilder>("funzione");
        
//        off.setBuilder(new OctaveFunctionFile("funzione2"));
        off.addArg(new SerializableOctaveFile("file_in",""));
        off.addArg(new SerializableOctaveFile("file_out",""));
        
        //soo.setName("variable_name");
        OctaveFunctionSheet os=new OctaveFunctionSheet();
        os.pushDefinition(new SerializableOctaveFile("variable_name2","VALUE"));
        os.pushDefinition(new SerializableOctaveFile("returning_var_name2","VALUE"));
        os.pushCommand("COMMAND");
        os.pushFunction(off);
        System.out.println("-------------------------------------");
        oac.push(os);
        

        System.out.println(stream.toXML(oac));
        System.out.println("-------------------------------------");
        
        
        stream.processAnnotations(OctaveFunctionFile.class);
        System.out.println(stream.toXML(off));
        System.out.println("-------------------------------------");
    }
}
