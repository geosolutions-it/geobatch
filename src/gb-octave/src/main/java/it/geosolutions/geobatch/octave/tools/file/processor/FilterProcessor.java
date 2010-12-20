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
package it.geosolutions.geobatch.octave.tools.file.processor;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A file filter processor used to filter a Configuration
 * to obtain a reader to a filtered document
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class FilterProcessor {
    
    private final static Logger LOGGER = Logger.getLogger(FilterProcessor.class.toString());
    
    /**
     * @param conf the Configuration of this filter
     * @return a PipedReader which should be used to consume the
     * output of this filter
     * @throws IOException
     * @note this method starts a new thread to read the input
     * be sure to consume all the reader data. Be sure to close
     * the reader.
     * 
     */
    public static PipedReader process(final FilterConfiguration conf) throws IOException{
        // reader for the NcML
        PipedReader pr=null;
        try {
            // reader for the NcML
            pr=new PipedReader();
            // writer for the processed text 
            final PipedWriter out = new PipedWriter(pr);
//DEBUG                
//Writer out2 = new OutputStreamWriter(System.out);
            
            final Thread t=new Thread(
                    new Runnable(){
                      public void run(){
                        try {
                            /*
                             * Merge data-model with template
                             * elaborate template
                             */
                            conf.process(out);
                            out.flush();
                        } catch (IOException e) {
                            if (LOGGER.isLoggable(Level.SEVERE))
                                LOGGER.severe(e.getLocalizedMessage());
                        }
                        finally{
                            try {
                                out.close();
                            } catch (IOException e) {
                                if (LOGGER.isLoggable(Level.SEVERE))
                                    LOGGER.severe("Unable to close the writer.\n"
                                            +e.getLocalizedMessage());
                                
                            }
                        }
                      }
                    }
                  );
            t.setDaemon(true);
            t.start();
        }
        catch (IOException e){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Unable to close the writer.\n"
                        +e.getLocalizedMessage());
            pr.close();
            throw e;
        }
        return pr;
    }
        

}
