package it.geosolutions.geobatch.nurc.sem.rep10.shom;

import it.geosolutions.geobatch.octave.tools.file.processor.FilterConfiguration;

public class SHOMConfiguration extends FilterConfiguration {

    //MENOR,MED
    private String type=null;
    
    // remove temporary files
    private boolean performBackup=false;
    
    public final boolean getPerformBackup(){
        return performBackup;
    }
    
    public final String getType(){
        if (type!=null)
            return type;
        else
            return "";
    }
}
