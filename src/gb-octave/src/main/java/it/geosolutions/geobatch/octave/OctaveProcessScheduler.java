package it.geosolutions.geobatch.octave;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OctaveProcessScheduler {

    /*
 * TODO we should run a daemon thread to make clean/close operation on this list
     */
    private static List<Engine> engineList=new ArrayList<Engine>(OctaveConfiguration.getProcessorsSz());
    

    protected static Engine getEngine()
    throws InterruptedException
    {
        Engine eng=null;
        synchronized (engineList) {
            if (engineList.isEmpty()){
                eng=new Engine();
                engineList.add(eng);
            }
            else {
                Iterator<Engine> i=engineList.iterator();
                int load=0,minLoad=Integer.MAX_VALUE;
                while (i.hasNext()){
                    Engine next_eng=i.next();
                    load=next_eng.getLoad();
                    if (minLoad>load){
                        eng=next_eng;
                        minLoad=load=eng.getLoad();
                    }
                }
                /*
                 * If the load is >0 try to add a new engine
                 */
                if (load>0 && 
                        engineList.size()<OctaveConfiguration.getProcessorsSz()){
                    eng=new Engine();
                    engineList.add(eng);
                }
            }   
        }
        return eng;
    }
    
    protected static OctaveExecutor getProcessor(OctaveEnv<OctaveExecutableSheet> env)
            throws InterruptedException
    {
        return new OctaveExecutor(env,getEngine());
    }

}
