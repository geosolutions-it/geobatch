/**
 * 
 */
package it.geosolutions.geobatch.octave.actions.templates.freemarker;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import it.geosolutions.geobatch.octave.actions.OctaveActionConfiguration;;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class OctaveFreeMarkerConfiguration extends OctaveActionConfiguration {
    @XStreamOmitField
    protected static String SHEETNAME_KEY="SHEETNAME";
    // to set at runtime
    @XStreamOmitField
    protected static String WORKINGDIR_KEY="WORKINGDIR";
    // to update using workingdir
    @XStreamOmitField
    protected static String SOURCEDIR_KEY="SOURCEDIR";
    
    // specific for FileInFileOut!!!
    @XStreamOmitField
    protected static String FUNCTION_KEY="FUNCTION";
    
    // to set at runtime
    @XStreamOmitField
    protected static String IN_FILE_KEY="IN_FILE";
    @XStreamOmitField
    protected static String OUT_FILE_KEY="OUT_FILE";
    
    @XStreamAlias("root")
    private Map<String,Object> root;
    
    private String cruise;
    private String model;
    private String extension;
    
    private String outDir;
    
    public OctaveFreeMarkerConfiguration(OctaveFreeMarkerConfiguration ac){
        super(ac);
        root=new HashMap<String,Object>();
    }
    
    public Map<String,Object> getRoot(){
        return root;
    }

    /**
     * @param cruise the cruise to set
     */
    public void setCruise(String cruise) {
        this.cruise = cruise;
    }

    /**
     * @return the cruise
     */
    public String getCruise() {
        return cruise;
    }

    /**
     * @param model the model to set
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param outDir the outDir to set
     */
    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    /**
     * @return the outDir
     */
    public String getOutDir() {
        return outDir;
    }

}
