package it.geosolutions.geobatch.octave;

import java.util.logging.Level;
import java.util.logging.Logger;


public class OctaveThread implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(OctaveThread.class.toString());
    private OctaveEnv<?> env;
    private Engine engine;
    
    public OctaveThread(OctaveEnv<?> e){
        env=e;
        engine=new Engine();
    }

    public void run() {
        String comm="";
        while (comm!="quit"){
            while (!env.hasNext())
            {   
        //TODO: check is clean comm needed?
                // formally we have to clean last command line
                comm="";
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.FINER, e.getMessage(), e);
                }
            }
            // extract next ExecutableSheet
            OctaveExecutableSheet sheet=env.pop();
            
            while (comm!="quit" && sheet.hasCommands()){
                // extract
                comm=sheet.popCommand();
                // put definitions into octave environment
                engine.put(sheet.getDefinitions());
                // evaluate commands (f.e.: source files)
                engine.eval(comm);
    // TODO: check-> is this formally correct?
                // sheet returns becomes global returns
                env.global.pushReturns(sheet.getReturns());
                // clear sheet environment
                engine.clear(sheet.getDefinitions());
                sheet=env.pop();
            }
            
        }
        engine.close();
        
//TODO  log
    }

}
