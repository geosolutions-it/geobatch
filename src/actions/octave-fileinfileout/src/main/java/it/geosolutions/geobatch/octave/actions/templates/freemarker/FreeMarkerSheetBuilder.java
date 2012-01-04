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

import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveFunctionFile;
import it.geosolutions.geobatch.octave.SheetBuilder;
import it.geosolutions.tools.freemarker.filter.FreeMarkerFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import dk.ange.octave.exception.OctaveException;
import dk.ange.octave.exception.OctaveParseException;
import freemarker.template.TemplateException;

public class FreeMarkerSheetBuilder extends SheetBuilder {
    private final static Logger LOGGER = LoggerFactory.getLogger(FreeMarkerSheetBuilder.class.toString());
    
    private OctaveFreeMarkerConfiguration conf=null;
    
    /**
     * 
     */
    public FreeMarkerSheetBuilder(OctaveFreeMarkerConfiguration config){
        super();
        conf=config;
    }
    
    /**
     * The prototype of the mars3d function is:
     * mars3d(file_in,file_out);
     */
    @Override
    protected OctaveExecutableSheet buildSheet(OctaveFunctionFile off) throws OctaveException{
        
        XStream stream=new XStream();
        stream.processAnnotations(OctaveFunctionFile.class);
        //stream.processAnnotations(SerializableOctaveFile.class);
        stream.processAnnotations(OctaveExecutableSheet.class);
        Reader reader=null;
        try{ 
            // a reader for the marshalled object
            reader=new StringReader(stream.toXML(off));
        }
        catch (XStreamException xse){
            //XStreamException - if the object cannot be serialized
            String message="FreeMarkerSheetBuilder: XStreamException - the object cannot be serialized.\n"
                +xse.getLocalizedMessage();
            if (LOGGER.isInfoEnabled())
                LOGGER.info(message);
            throw new OctaveParseException(message);
        }
        // the filter for this object
        final FreeMarkerFilter filter=new FreeMarkerFilter(Path.getAbsolutePath(conf.getWorkingDirectory()), reader);
        // the stream to a byte array (buffer)
        final ByteArrayOutputStream outStream= new ByteArrayOutputStream();
        // a writer to that buffer
        final OutputStreamWriter writer = new OutputStreamWriter(outStream);
        try {
            // process the input
            filter.process(filter.wrapRoot(conf.getRoot()), writer);
            
            outStream.flush();
            writer.close();
        }
        catch (TemplateException te){
            //TemplateException if an exception occurs during template processing
            String message="FreeMarkerSheetBuilder: XTemplateException - an exception occurs during template processing.\n"
                +te.getLocalizedMessage();
            if (LOGGER.isInfoEnabled())
                LOGGER.info(message);
            throw new OctaveParseException(message);
        }
        catch (IOException ioe){
            //IOException if an I/O exception occurs during writing to the writer.
            String message="FreeMarkerSheetBuilder: IOException - I/O exception occurs during writing to the writer.\n"
                +ioe.getLocalizedMessage();
            if (LOGGER.isInfoEnabled())
                LOGGER.info(message);
            throw new OctaveParseException(message);
        }

        // get an input stream on the buffer
        InputStream is=new ByteArrayInputStream(outStream.toByteArray());
        
        try{
            outStream.close();
            // use the default buildSheet method to build the sheet   
            return super.buildSheet((OctaveFunctionFile) stream.fromXML(is));
        }
        catch (XStreamException xse){
            //XStreamException - if the object cannot be deserialized
            String message="FreeMarkerSheetBuilder: XStreamException - the object cannot be deserialized.\n"
                +xse.getLocalizedMessage();
            if (LOGGER.isInfoEnabled())
                LOGGER.info(message);
            throw new OctaveParseException(message);
        } catch (IOException e) {
            LOGGER.trace(e.getMessage(), e);
        }
        finally{
            
            try {
                if (is!=null)
                    is.close();
            } catch (IOException e) {
                LOGGER.trace(e.getMessage(), e);
            }            
        }
        return null;
    }
}
