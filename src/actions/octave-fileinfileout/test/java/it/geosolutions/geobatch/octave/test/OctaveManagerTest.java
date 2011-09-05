package it.geosolutions.geobatch.octave.test;

import it.geosolutions.geobatch.octave.OctaveConfiguration;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveManager;
import it.geosolutions.geobatch.octave.OctaveProcessScheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dk.ange.octave.type.OctaveObject;

/**
 * This is a good (working) testing class for the octave environment.
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class OctaveManagerTest {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
System.out.println("Starting multithreaded");
        multithreaded();
        Thread.sleep(5000);
System.out.println("Starting runFromThread");        
        runFromThread();
        
//        if (args.length>0){
//            if (args[0].contains("m"))
//                multithreaded();
//            else
//                runFromThread();
//        }
//        else
//            runFromThread();
        return;
    }
    
    /**
     * This is an example on how you should use the octave
     * platform from a multithreaded application.
     * place this function into your thread.run() method to
     * make it wait for an octave response.
     * You can freely make multiple calls to the octave 
     * platform it will run a new octave process for each
     * request (up to the OctaveConfiguration.processors value).
     * Other requests are queued on those processes using a simple
     * load priority.
     * @see OctaveProcessScheduler
     * @see OctaveManager
     */
    private static void runFromThread(){
        try {
System.out.println("Starting");

ExecutorService e=Executors.newFixedThreadPool(10);
// can be null if you have called it 
            OctaveConfiguration oc=OctaveConfiguration.getOctaveConfiguration("./",e);
System.out.println("Starting the manager");            
            Thread.sleep(5000);
            
      //      OctaveManager.getOctaveManager(oc,e);
            
     //       OctaveProcessScheduler.getOctaveProcessScheduler(e);
            
            OctaveEnv<OctaveExecutableSheet> oes=new OctaveEnv<OctaveExecutableSheet>();
            
            
            // do something 
System.out.println("Doing something waiting for octave");
            Thread.sleep(1000);
            // now we have to wait for octave
System.out.println("Getting results from Octave");
            OctaveManager.process(oes,e);
System.out.println("Stopping the manager");
            Thread.sleep(5000);
System.out.println("SHUTTING_DOWN");
e.shutdown();
System.out.println("rExiting");
            return;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void multithreaded(){
        try {
            ExecutorService e=Executors.newFixedThreadPool(10);
System.out.println("Starting");
            OctaveConfiguration oc=OctaveConfiguration.getOctaveConfiguration("./",e);
System.out.println("Starting the manager");            
            Thread.sleep(5000);
            
            OctaveManager manager=OctaveManager.getOctaveManager(oc);
            
            OctaveEnv<OctaveExecutableSheet> oes=new OctaveEnv<OctaveExecutableSheet>();
            //run the job using a separated thread
            Future<List<OctaveObject>> task=manager.enqueue(oes);
            // do something 
System.out.println("Doing something waiting for octave");
            Thread.sleep(1000);
            // now we have to wait for octave
System.out.println("Getting results from Octave");
            task.get();
            manager.shutdown();
System.out.println("Stopping the manager");            
            Thread.sleep(5000);
            e.shutdown();
            Thread.sleep(5000);
System.out.println("mExiting");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
