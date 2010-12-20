package it.geosolutions.geobatch.octave;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OctaveProcessScheduler implements Runnable{
    private final static Logger LOGGER = Logger.getLogger(OctaveProcessScheduler.class.toString());
    /*
 * TODO we should run a daemon thread to make clean/close operation on this list
     */
    private static List<Engine> engineList=new ArrayList<Engine>(OctaveConfiguration.getProcessorsSz());
    
    private static OctaveProcessScheduler singleton=null;
    
    private static Lock l=new ReentrantLock(true);
    
    private static boolean initted=false;
    
    private OctaveProcessScheduler(){
        
    }
    
    /**
     * get the process scheduler.
     * @param es the executorService to use, can be null.
     * @return
     */
    public static OctaveProcessScheduler getOctaveProcessScheduler(ExecutorService es){
        if (singleton==null){
            try {
                l.tryLock(OctaveConfiguration.TIME_TO_WAIT,TimeUnit.SECONDS);
                if (singleton==null){
                    singleton=new OctaveProcessScheduler();
                    if (es!=null){
                        es.submit(singleton);
                    }
                    else {
                        Thread t=new Thread(singleton);
                        t.setDaemon(true);
                        t.start();
                    }
                    initted=true;
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("OctaveProcessScheduler is up and running");
System.out.println("OctaveProcessScheduler is up and running");
                }
            } catch (InterruptedException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe(e.getLocalizedMessage());
            }
            finally {
                l.unlock();
            }
        }
        return singleton;
    }
    

    protected static Engine getEngine()
        throws InterruptedException
    {
        if (!initted)
            getOctaveProcessScheduler(null);
        
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
        } // synchronized
        return eng;
    }
    
    protected static OctaveExecutor getProcessor(OctaveEnv<OctaveExecutableSheet> env)
            throws InterruptedException
    {
        return new OctaveExecutor(env,getEngine());
    }


    public void run() {
        boolean notExit=true;
        while (notExit){
            try {
System.out.println("Sleeping");
                Thread.sleep(10000);
System.out.println("Starting");
            } catch (InterruptedException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe(e.getLocalizedMessage());
            }

            synchronized (engineList) {
//                Iterator<Engine> i=engineList.iterator();
                int load=0;
                int size=engineList.size();
                while (size>0){//i.hasNext()){
                    Engine next_eng=engineList.get(--size);//i.next();
                    load=next_eng.getLoad();
                    if (load==0){
                        engineList.remove(next_eng);
                        next_eng.close();
System.out.println("Removing engine");
                        next_eng=null;
                    }
                }
                /*
                 * TODO if we found a load >1
                 * should be better to migrate the job
                 * to a lower load process...
                 */
                if (engineList.size()==0){
System.out.println("starting Shutdown");
                    notExit=false;
                    initted=false;
                    singleton=null;
                }
            }
        }
System.out.println("Shutdown done, bye!");
    }

}
