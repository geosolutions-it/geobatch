package it.geosolutions.geobatch.octave;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SheetPreprocessor {
    private Map<String, SheetBuilder> preprocessors=new HashMap<String, SheetBuilder>();
    
    private final static Logger LOGGER = Logger.getLogger(SheetPreprocessor.class.toString());
    
    public final void addBuilder(String name, SheetBuilder sb){
        preprocessors.put(name, sb);
    }
    
    public final void removeBuilder(String name){
        preprocessors.remove(name);
    }
    
    /**
     * Transform a OctaveFunctionSheet in a OctaveExecutableSheet
     * 
     * @param sheet input output variable:
     * input is a OctaveFunctionSheet to be preprocessed
     * output can be used as a OctaveExecutableSheet
     */
    public final void preprocess(OctaveEnv<OctaveExecutableSheet> env) throws Exception {
        int size=env.size();
        int i=0;
        while (i<size){
            // for each Sheet into the env
            OctaveExecutableSheet es=env.getSheet(i++);
            // if it is a FunctionSheet
            if (es instanceof OctaveFunctionSheet){
                /**
                 * this is a sheet that contains a function
                 * which will produce a new OctaveExecutableSheet
                 */
                OctaveFunctionSheet sheet=(OctaveFunctionSheet) es;
                // pre-process all functions in the sheet
                while (sheet.hasFunctions()){
                    OctaveFunctionFile f=sheet.popFunction();
                    SheetBuilder sb=null;
                    if ((sb=preprocessors.get(f.getName()))!=null){
                        // build the executable sheet
                        env.push(sb.buildSheet(f));
                    }
                    else
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.info("No preprocessor found for the OctaveFunctionSheet named "+f.getName());
                }
            }
        }
    }

}
