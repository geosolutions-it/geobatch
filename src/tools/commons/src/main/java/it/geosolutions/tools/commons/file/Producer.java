/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.commons.file;

import it.geosolutions.tools.commons.Conf;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class implementing a producer which write on a PipeWriter.
 * Using this class you can easily implement a producer consumer
 * application.
 *  
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public abstract class Producer {
    private final static Logger LOGGER = LoggerFactory.getLogger(Producer.class.toString());
    
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
            if (LOGGER.isInfoEnabled())
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
    //ExceptionWriter out2 = new OutputStreamWriter(System.out);
            
            productor=
                executor.submit(new Callable<Object>() {
                        public PipedWriter call() throws Exception {
                            try {
                                producer(out);
                                out.flush();
                            } catch (IOException e) {
                                if (LOGGER.isErrorEnabled())
                                    LOGGER.error(e.getLocalizedMessage());
                                throw e;
                            }
                            /*Exception aren't cached*/
                            finally{
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    if (LOGGER.isErrorEnabled())
                                        LOGGER.error("Unable to close the writer.\n"
                                                +e.getLocalizedMessage());
                                }
                            }
                            return null;
                        }
                    }
                ); //submit
        }
        catch (IOException ex){
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Unable to close the writer.\n"
                        +ex.getLocalizedMessage());
            throw ex;
        }
        return pr;
    }
    
    public void close(boolean force){
        try{
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Closing the producer");
            
            if (!force){
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Waiting for the producer");
                productor.get(Conf.getTimeToWait(), TimeUnit.SECONDS);
            }
            
        } catch (InterruptedException ie) {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace(ie.getMessage(), ie);
        } catch (ExecutionException ee) {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace(ee.getMessage(), ee);
        } catch (TimeoutException te) {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace(te.getMessage(), te);
        }
        finally{
            // be sure the thread ends
            productor.cancel(true);
            
            if (handleExecutor)
                executor.shutdownNow();
        }
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Producer is successfully closed");
    }

}
