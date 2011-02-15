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

package it.geosolutions.geobatch.octave;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ange.octave.type.OctaveObject;

public class OctaveExecutor implements Callable<List<OctaveObject>> {
    
    private final static Logger LOGGER = Logger.getLogger(OctaveExecutor.class.toString());
    
    /**
     * Octave Environment
     */
    private OctaveEnv<OctaveExecutableSheet> env;
    
    /**
     * Octave Engine
     */
    private Engine engine;
    
    protected OctaveExecutor(OctaveEnv<OctaveExecutableSheet> e, Engine eng)throws InterruptedException{
        env=e;
        engine=eng;
    }
    
    /**
     * @note this is thread safe
     * @param env the environment to execute
     * @param eng the engine to use
     * @return returns the results obtained by this OctaveEnv
     * @throws Exception
     */
    public static List<OctaveObject> call(OctaveEnv<OctaveExecutableSheet> env, Engine eng) throws Exception {
        /**
         * Objects are extracted from the list
         * since each returning value should be returned
         * to the requesting process using an XML message
         */
        OctaveExecutableSheet es=null;
        int exit=1;
        while (exit!=0 && env.hasNext()){
            // extract next ExecutableSheet
            es=env.pop();
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Octave extracted a new OctaveExecutableSheet");
                /*
                 * Execute the 
                 */
                exit=eng.exec(es, true);

                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Octave extecuted sheet with exit status: "
                        +((exit>=0)?"GOOD":"BED"));
                
            }
            catch (Exception e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe("Octave throws an exception: "+e.getLocalizedMessage());
                throw e;
            }

            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Octave process exiting");
        } // comm!="quit"
        
        return eng.getResults();
    }
    
    /**
     * @note never call this method directly
     * This method should be called by the OctaveManager's ExecutorService
     * @return returns the results obtained by this OctaveEnv
     */
    public List<OctaveObject> call() throws Exception {
        /**
         * Objects are extracted from the list
         * since each returning value should be returned
         * to the requesting process using an XML message
         */
        OctaveExecutableSheet es=null;
        int exit=1;
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Octave extecutor started");
        while (exit!=0 && env.hasNext()){
            // extract next ExecutableSheet
            es=env.pop();
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Octave extracted a new ExecutableSheet from env "+env.getUniqueID());
                
                exit=engine.exec(es, true);

                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Octave extecuted sheet with exit status: "+((exit>=0)?"GOOD":"BED"));
                
            }
            catch (Exception e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe("Octave throws an exception: "+e.getLocalizedMessage());
                throw e;
            }
        } // comm!="quit"
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Octave process exiting");
//        if (exit>0)
            return engine.getResults();
//        else
//            return null;
    }

}
