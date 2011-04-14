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
package it.geosolutions.geobatch.nurc.sem.rep10.shom;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
@XStreamAlias("filter")
@XStreamInclude(Configuration.class)
public class Configuration extends ActionConfiguration {

    @XStreamOmitField
    private boolean initted = false;

    @XStreamOmitField
    private final static Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    // Create a data-model
    @XStreamAlias("map")
    private Map<String, String> root = null;

    // path where to find ncml template
    @XStreamAlias("file")
    private String template_path = null;

    // You should do this ONLY ONCE in the whole application life-cycle:
    @XStreamOmitField
    private freemarker.template.Configuration cfg = null;

    // Hold the template
    @XStreamOmitField
    private Template template = null;

    /**
     * Default constructor
     * 
     * @note this is never called by XStream
     * @param id
     * @param name
     * @param description
     */
    public Configuration(String id, String name, String description) {
        super(id, name, description);
        if (init())
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Failed to initialize the configuration");
    }

    /**
     * Check the status of this configuration
     * 
     * @return true if the init() method has already run false otherwise
     */
    public boolean isInitted() {
        return initted;
    }

    /**
     * initialize members
     * 
     * @return true if init ends successful
     */
    public boolean init() {
        if (initted)
            return initted;

        // singleton configuration pattern
        if (cfg == null) {
            cfg = new freemarker.template.Configuration();
        }

        if (root == null) {
            root = new HashMap<String, String>();
        }
        String workingDirectory = getWorkingDirectory();
        if (workingDirectory != null && cfg != null) {
            try {
                setWorkingDirectory(Path.getAbsolutePath(workingDirectory));
                cfg.setDirectoryForTemplateLoading(new File(getWorkingDirectory()));
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Unable to get the working dir for the SHOM configuration: "
                            + e.getLocalizedMessage());
                return false;
            }
        } else {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Unable to get the working dir for the SHOM configuration");
            return false;
        }

        /* Get or create a template */
        if (template_path != null)
            try {
                template = cfg.getTemplate(template_path);
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Unable to get the template: " + e.getLocalizedMessage());
                return false;
            }
        else {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Unable to set the NcML template file check the SHOM configuration");
            return false;
        }
        initted = true;
        return true;
    }

    /**
     * return the substitution map
     * 
     * @return the substitution map
     * @note before you call init() method this object can be null
     */
    public Map<String, String> getRoot() {
        return root;
    }

    /**
     * return the template path
     * 
     * @return the template path
     * @note before you call init() method this object can be null anyway the template_path should
     *       be always present into the configuration.
     */
    public final String getTemplate() {
        return template_path;
    }

    /**
     * This method is used to process the file using this configuration
     * 
     * @param out
     *            the Writer
     * @return
     */
    protected final boolean process(Writer out) {
        try {
            template.process(root, out);
            return true;
        } catch (TemplateException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(),e);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(),e);
        }
        return false;
    }
}
