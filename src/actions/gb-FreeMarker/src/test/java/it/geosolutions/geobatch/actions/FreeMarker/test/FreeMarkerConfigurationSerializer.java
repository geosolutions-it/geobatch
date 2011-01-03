package it.geosolutions.geobatch.actions.FreeMarker.test;

import it.geosolutions.geobatch.actions.FreeMarker.FreeMarkerConfiguration;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

public class FreeMarkerConfigurationSerializer {
    
    public static void main(String[] args){
        XStream stream = new XStream();
        stream.processAnnotations(FreeMarkerConfiguration.class);
        
        FreeMarkerConfiguration fmc=new FreeMarkerConfiguration();
        fmc.setDescription("description");
        fmc.setDirty(false);
        fmc.setFailIgnored(false);
        fmc.setId("id");
        fmc.setName("name");
        fmc.setServiceID("serviceID");
        fmc.setWorkingDirectory("workingDirectory");
        fmc.setInput("TemplIn.xml");
        fmc.setOutput("TemplOut.xml");
        
        Map<String,Object> m=new HashMap<String, Object>();
        m.put("SHEET_NAME", "value1.xml");
        m.put("SOURCE_PATH", "value2");
        m.put("WORKING_DIR", "value3");
        m.put("FILE_IN", "IN");
        m.put("FILE_OUT", "OUT");
        fmc.setRoot(m);
        
        
        System.out.println("-------------------------------------");
        System.out.println(stream.toXML(fmc));
        System.out.println("-------------------------------------");
    }

}
