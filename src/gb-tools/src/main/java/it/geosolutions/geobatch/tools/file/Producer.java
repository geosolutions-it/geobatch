/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.tools.file;

import it.geosolutions.geobatch.tools.Conf;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class implementing a producer which write on a PipeWriter.
 * Using this class you can easily implement a producer consumer
 * application.
 *  
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public abstract class Producer {
    private final static Logger LOGGER = Logger.getLogger(Producer.class.toString());
    
    /**
     * Implementing this method you have to write on the pipedWriter
     * @param pw the pipeWriter to use to write on
     * @throws Exception if needed you can throw an Exception which is not handled
     * by the produce() method.
     */
    protected abstract void producer(PipedWriter pw) throws Exception;
    
    
    private ExecutorService executor=null;
    /*
     * if the constructor build the ExecutorService it
     * should handle its shutdown 
     */
    private boolean handleExecutor=false;
    
    private Future<Object> productor=null;
    
    public Producer(ExecutorService e){
        if (e==null){
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Handling the producer with a newSingleThreadExecutor");
            executor=Executors.newSingleThreadExecutor();
            handleExecutor=true;
        }
        else {
            executor=e;
            handleExecutor=false;
        }
    }
    

    /**
     * @param e the executor service to use (if it is null a SingleThreadExecutor will be used)
     * @param the producer object which implements the producer method (which effectively write
     * on the pipedWriter)
     * @return a PipedReader which should be used to consume the
     * output of this filter
     * @throws IOException
     * @note this method starts a new thread to read the input
     * be sure to consume all the reader data. Be sure to close
     * the reader.
     */
    public final PipedReader produce() throws IOException{
        PipedReader pr=null;
        try {
            // reader
            pr=new PipedReader();
            
            // writer for the processed text 
            final PipedWriter out = new PipedWriter(pr);
    //DEBUG                
    //Writer out2 = new OutputStreamWriter(System.out);
            
            productor=
                executor.submit(new Callable<Object>() {
                        public PipedWriter call() throws Exception {
                            try {
                                producer(out);
                                out.flush();
                            } catch (IOException e) {
                                if (LOGGER.isLoggable(Level.SEVERE))
                                    LOGGER.severe(e.getLocalizedMessage());
                                throw e;
                            }
                            /*Exception aren't cached*/
                            finally{
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    if (LOGGER.isLoggable(Level.SEVERE))
                                        LOGGER.severe("Unable to close the writer.\n"
                                                +e.getLocalizedMessage());
                                }
                            }
                            return null;
                        }
                    }
                ); //submit
        }
        catch (IOException ex){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Unable to close the writer.\n"
                        +ex.getLocalizedMessage());
            throw ex;
        }
        return pr;
    }
    
    public void close(boolean force){
        try{
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Closing the producer");
            
            if (!force){
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Waiting for the producer");
                productor.get(Conf.getTimeToWait(), TimeUnit.SECONDS);
            }
            
        } catch (InterruptedException ie) {
            LOGGER.log(Level.FINER, ie.getMessage(), ie);
        } catch (ExecutionException ee) {
            LOGGER.log(Level.FINER, ee.getMessage(), ee);
        } catch (TimeoutException te) {
            LOGGER.log(Level.FINER, te.getMessage(), te);
        }
        finally{
            // be sure the thread ends
            productor.cancel(true);
            
            if (handleExecutor)
                executor.shutdownNow();
        }
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Producer is successfully closed");
    }

}
