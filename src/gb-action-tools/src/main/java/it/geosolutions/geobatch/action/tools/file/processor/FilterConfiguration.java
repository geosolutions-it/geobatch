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
package it.geosolutions.geobatch.action.tools.file.processor;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import it.geosolutions.geobatch.action.tools.configuration.Path;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
@XStreamAlias("filter")
@XStreamInclude(
    FilterConfiguration.class
)
public class FilterConfiguration extends ActionConfiguration {
    @XStreamOmitField
    private boolean initted=false;
    
    @XStreamOmitField
    private final static Logger LOGGER = Logger.getLogger(FilterConfiguration.class.toString());
    
    // Create a data-model
    @XStreamAlias("map")
    private Map<String, TemplateModel> root=null;
    
    // path where to find ncml template
    @XStreamAlias("file")
    private String template_path=null;
    
    /*
     * You should do this ONLY ONCE in the whole application life-cycle: 
     */
    @XStreamOmitField 
    private freemarker.template.Configuration cfg = null;

    // Hold the template 
    @XStreamOmitField 
    private Template template=null;
    
    /**
     * Default constructor
     * @note this is never called by XStream
     */
    public FilterConfiguration() {
        super();
        if (init())
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Failed to initialize the configuration");
    }
    
    /**
     * Check the status of this configuration
     * @return true if the init() method has already run
     * false otherwise
     */
    public boolean isInitted(){
        return initted;
    }
    
    /**
     * initialize members
     * @return true if init ends successful
     */
    public boolean init(){
        if (initted)
            return initted;
        
     // singleton configuration pattern
        if (cfg==null){
            cfg=new freemarker.template.Configuration();
        }
        
        if (root==null){
            root=new HashMap<String, TemplateModel>();
        }
        String workingDirectory=getWorkingDirectory();
        if (workingDirectory!=null && cfg!=null){
            try {
                setWorkingDirectory(Path.getAbsolutePath(workingDirectory));
                cfg.setDirectoryForTemplateLoading(new File(getWorkingDirectory()));
            }
            catch (IOException e){
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe("Unable to get the working dir for the SHOM configuration: "
                            +e.getLocalizedMessage());
                return false;
            }
        }
        else {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Unable to get the working dir for the SHOM configuration");
            return false;
        }
            
        /* Get or create a template */
        if (template_path!=null)
            try {
                template = cfg.getTemplate(template_path);
            } catch (IOException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.severe("Unable to get the template: "+e.getLocalizedMessage());
                return false;
            }
        else {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe("Unable to set the NcML template file check the SHOM configuration");
            return false;
        }
        initted=true;
        return true;
    }
    
    /**
     * return the substitution map
     * @return the substitution map
     * @note before you call init() method
     * this object can be null
     */
    public Map<String, TemplateModel> getRoot(){
        return root;
    }
    
    /**
     * return the template path
     * @return the template path
     * @note before you call init() method
     * this object can be null anyway
     * the template_path should be always
     * present into the configuration.
     */
    public final String getTemplate(){
        return template_path;
    }
    
    /**
     * This method is used to process the file using
     * this configuration
     * @param out the Writer
     * @return
     */
    protected final boolean process(Writer out){
        try {
            template.process(root,out);
            return true;
        } catch (TemplateException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(e.getLocalizedMessage());
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.severe(e.getLocalizedMessage());
        }
        return false;
    }
}
