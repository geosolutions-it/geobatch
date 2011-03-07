package it.geosolutions.geobatch.nurc.sem.rep10.shom;


public class SHOMConfiguration extends Configuration {

    //MENOR,MED
    private String type=null;
    
    // remove temporary files
    private boolean performBackup=false;
    
    public final boolean getPerformBackup(){
        return performBackup;
    }
    
    public SHOMConfiguration(String id, String name, String description) {
        super(id, name, description);
    }
    
    public final String getType(){
        if (type!=null)
            return type;
        else
            return "";
    }
}
