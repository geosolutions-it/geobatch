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
package it.geosolutions.geobatch.octave.actions.templates.freemarker;

import it.geosolutions.geobatch.tools.file.Producer;

import java.io.PipedWriter;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class ObjectProducer extends Producer {
    private final static Logger LOGGER = Logger.getLogger(ObjectProducer.class.toString());
    
    private static XStream stream=null;
    private Object obj=null;
    /**
     * @param e
     * @param o the first object to produce
     */
    public ObjectProducer(ExecutorService e, Object o) {
        super(e);
        stream=new XStream();
        if (o!=null)
            obj=o;
        else
            obj=new Object(); //obj should never be null!
    }

    public void setObj(Object o){
        if (o!=null){
            synchronized (obj){
                obj=null;
                obj=o;
            }
        }
        else {
            String message="Unable to set the object to produce to NULL!";
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(message);
            throw new NullPointerException(message);
        }
    }
    
    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.tools.file.Producer#producer(java.io.PipedWriter)
     */
    @Override
    protected void producer(PipedWriter pw) throws Exception {
        synchronized (obj){
            stream.toXML(obj, pw);
        }
    }

}
