import it.geosolutions.geobatch.action.scripting.Collector;
import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
import it.geosolutions.geobatch.tools.file.Extract;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.util.logging.Level
import java.util.logging.Logger

import it.geosolutions.geobatch.flow.event.IProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder

/** 
 * Script Main "execute" function
 * @eventFilePath
 **/
public List execute(ScriptingConfiguration configuration, String eventFilePath, ProgressListenerForwarder listenerForwarder) throws Exception {
    /* ----------------------------------------------------------------- */
    // Main Input Variables: must be configured
    /**
     * The physical folder where to extract emsa packages
     **/
    def emsaExchangePhysicalDir = "/home/carlo/work/data/emsa/out/";
    def readyFileName="PackagesReady.txt";

    final Logger LOGGER = Logger.getLogger(EMSA.class.toString());
    
    //
    List results = new ArrayList();
    
    listenerForwarder.started();


    System.out.println("FILE: "+eventFilePath);
    // check if in the incoming eventFilePath is present the PackagesReady.txt file
    FileFilter filter=FileFilterUtils.nameFileFilter(readyFileName);
    File inDir=new File(eventFilePath);
    File[] readyFile=inDir.listFiles((FileFilter)filter);
    if (readyFile.length<1){

        System.out.println("ERROR");
        String message="::EMSAService : do not contain \"" + readyFileName + "\" file..."

        LOGGER.log(Level.SEVERE, message);
        // ////
        // fake event to avoid Failed Status!
        results.add("DONE");
        return results;
    }
    // if the directory is complete move it
    try {
        outDir=new File(inDir.getParent()+"/../out/"+inDir.getName());
        FileUtils.moveDirectory(inDir,outDir.getCanonicalFile());
    } catch (IOException e) {
        String message="::EMSAService : problem moving dir " + inDir + " to out dir "+outDir;

        LOGGER.log(Level.SEVERE, message);
        Exception theCause = (cause != null ? cause : new Exception(message));
        listenerForwarder.failed(theCause);
        throw theCause;
    }

    // listing all the other files
    filter=FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(readyFileName));
    File[] list=outDir.listFiles((FileFilter)filter);
    for (file in list){
        String inputFileName = Extract.extract(file.getAbsolutePath());
        System.out.println("2FILE:"+inputFileName);
    }
    
    // search for needed files
    Collector c=new Collector(
        FileFilterUtils.or(
                new WildcardFileFilter("*_PCK.xml",IOCase.INSENSITIVE),
                new WildcardFileFilter("*_PRO",IOCase.INSENSITIVE)));
    list=c.collect(outDir);
    for (file in list){
        results.add(file.getAbsolutePath());
        System.out.println("3FILE: "+file);
    }

    // forwarding some logging information to Flow Logger Listener
    listenerForwarder.setTask("Processing event " + eventFilePath)

    // defining the Input Data Dir:
    //  - here the input packages will be extracted to be further processed
    //  - please provide an absolute path
    //  - where to extract the packages for further processing
   File outDataDir = new File(emsaExchangePhysicalDir);
   if (!outDataDir.exists()){
       if (!outDataDir.mkdirs()) {
            String message="::EMSAService : Could not create EMSA input data dir:"+emsaExchangePhysicalDir;

            LOGGER.log(Level.SEVERE, message);
            Exception theCause = (cause != null ? cause : new Exception(message));
            listenerForwarder.failed(theCause);
            throw theCause;
       }
   }

    // ////
    // forwarding event to the next action
    return results;
}
