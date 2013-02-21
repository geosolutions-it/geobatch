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
package it.geosolutions.tools.freemarker.filter;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * A file filter processor used to filter a file obtaining a filtered document
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class FreeMarkerFilter {

    // You should initialize this ONLY ONCE in the whole application life-cycle:
    private freemarker.template.Configuration cfg = null;

    // Hold the template
    private Template template = null;

    // register the init status of this object
    private boolean initted = false;

    /**
     * Constructor
     * 
     * @param wd workingDir (Set the explicit directory from which to load
     *            templates)
     * @param ifn [input file name] the template file name should be located
     *            into the wd
     * @throws IOException
     * @throws IllegalArgumentException Some arguments are null
     * @see Freemarker documentation for detailed accepted data types.
     */
    public FreeMarkerFilter(String wd, String ifn) throws IllegalArgumentException, IOException {
        super();
        if (wd == null || ifn == null) {
            throw new IllegalArgumentException("Some arguments are null");
        }
        initted = (initConfig(new File(wd)) && initTemplate(ifn));
    }

    /**
     * Constructor
     * 
     * @param stl the TemplateLoader to use (f.e.: {@link StringTemplateLoader}
     * @param name the default template name to set
     * @throws IOException
     * @throws IllegalArgumentException Some arguments are null
     * @see Freemarker documentation for detailed accepted data types.
     */
    public FreeMarkerFilter(TemplateLoader tl, String name) throws IllegalArgumentException, IOException {
        super();
        if (tl == null) {
            throw new IllegalArgumentException("Some arguments are null");
        }
        initted = (initConfig(tl,name));
    }
    

    /**
     * 
     * Initialize configuration
     * 
     * @param stl the string template to use
     * @param name the name of the default template to set
     * 
     * @return true if init ends successful
     * @throws IllegalArgumentException if workingDir is null
     * @throws IOException if workingDirectory is not a directory
     */
    private boolean initConfig(TemplateLoader stl, String name) throws IllegalArgumentException, IOException {
        if (initted)
            return initted;

        // singleton configuration pattern
        if (cfg == null) {
            cfg = new freemarker.template.Configuration();
        }
        
        /* Get or create a template */
        if (name == null) {
            throw new IllegalArgumentException(
                                               "Unable to set the template check the name parameter");
        }

        cfg.setTemplateLoader(stl);
        
        template = cfg.getTemplate(name);
        if (template==null)
            throw new IllegalArgumentException(
                    "Unable to set the default template to: "+name);

        return true;
    }
    
    /**
     * Constructor
     * 
     * @param the input file (the template path)
     * @throws IOException
     * @throws IllegalArgumentException Some arguments are null
     * @see Freemarker documentation for detailed accepted data types.
     */
    public FreeMarkerFilter(File template) throws IllegalArgumentException, IOException {
        super();
        initted = (initConfig(template.getParentFile()) && initTemplate(template.getName()));
    }

    /**
     * Constructor
     * 
     * @param wd workingDir (Set the explicit directory from which to load
     *            templates)
     * @param r the reader of a FreeMarker template
     * @throws IOException
     * @throws IllegalArgumentException Some arguments are null
     * @see Freemarker documentation for detailed accepted data types.
     */
    public FreeMarkerFilter(String workingDir, Reader reader) throws IllegalArgumentException, IOException {
        super();
        if (workingDir == null || reader == null) {
            throw new IllegalArgumentException("Some arguments are null");
        }
        initted = (initConfig(new File(workingDir)) && initTemplate(reader));
    }

    /**
     * Constructor
     * 
     * @param wd workingDir (Set the explicit directory from which to load
     *            templates)
     * @param r the reader of a FreeMarker template
     * @throws IOException
     * @throws IllegalArgumentException Some arguments are null
     * @see Freemarker documentation for detailed accepted data types.
     */
    public FreeMarkerFilter(File workingDir, Reader reader) throws IllegalArgumentException, IOException {
        super();
        if (workingDir == null || reader == null) {
            throw new IllegalArgumentException("Some arguments are null");
        }
        initted = (initConfig(workingDir) && initTemplate(reader));
    }

    /**
     * Try to wrap the passed object
     * 
     * @param tm the Object containing the TemplateModel to use as root data
     *            model TemplateHashModel TemplateSequenceModel
     *            TemplateCollectionModel TemplateScalarModel
     *            TemplateNumberModel TemplateTransformModel
     * @throws NullPointerException if tm is null
     * @throws TemplateModelException if defined objectWrapper can't wrap the
     *             passed object
     */
    public TemplateModel wrapRoot(Object tm) throws NullPointerException, TemplateModelException {
        if (!isInitted()) {
            throw new NullPointerException("Unable to initialize filter since it is not initialized!");
        }
        if (tm == null) {
            throw new NullPointerException("Unable to initialize filter using a null root data structure");
        }
        /*
         * From FreeMarked docs: Processes the template, using data from the
         * map, and outputs the resulting text to the supplied <tt>Writer</tt>
         * The elements of the map are converted to template models using the
         * default object wrapper returned by the {@link
         * Configuration#getObjectWrapper() getObjectWrapper()} method of the
         * <tt>Configuration</tt>.
         * 
         * @param rootMap the root node of the data model. If null, an empty
         * data model is used. Can be any object that the effective object
         * wrapper can turn into a <tt>TemplateHashModel</tt>. Basically, simple
         * and beans wrapper can turn <tt>java.util.Map</tt> objects into hashes
         * and the Jython wrapper can turn both a <tt>PyDictionary</tt> as well
         * as any object that implements <tt>__getitem__</tt> into a template
         * hash. Naturally, you can pass any object directly implementing
         * <tt>TemplateHashModel</tt> as well.
         */
        return cfg.getObjectWrapper().wrap(tm);
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
     * Initialize configuration
     * 
     * @return true if init ends successful
     * @throws IllegalArgumentException if workingDir is null
     * @throws IOException if workingDirectory is not a directory
     */
    private boolean initConfig(File workingDirectory) throws IllegalArgumentException, IOException {
        if (initted)
            return initted;

        // singleton configuration pattern
        if (cfg == null) {
            cfg = new freemarker.template.Configuration();
        }

        if (workingDirectory == null || cfg == null) {
            throw new IllegalArgumentException("Unable to get the working dir for this filter");
        }
        cfg.setDirectoryForTemplateLoading(workingDirectory);

        return true;
    }

    /**
     * 
     * @param reader
     * @return true
     * @throws IOException Unable to get the template
     * @throws IllegalArgumentException if reader is null
     */
    private boolean initTemplate(Reader reader) throws IOException, IllegalArgumentException {

        if (initted)
            return initted;

        /* Get or create a template */
        if (reader == null) {
            throw new IllegalArgumentException(
                                               "Unable to set the template file check the input_name parameter");
        }

        template = new Template(null, reader, cfg, cfg.getEncoding(cfg.getLocale()));

        return true;
    }

    /**
     * 
     * @param input_name
     * @return
     * @throws IOException Unable to get the template
     * @throws IllegalArgumentException input is null
     */
    private boolean initTemplate(String input) throws IOException, IllegalArgumentException {

        if (initted)
            return initted;

        /* Get or create a template */
        if (input != null) {
            template = cfg.getTemplate(input);
        } else {
            throw new IllegalArgumentException(
                                               "Unable to set the template file check the input_name parameter");
        }

        return true;
    }

    /**
     * This method is used to process the file using this configuration
     * 
     * @param out the Writer
     * @return true if success, false if the filter is not initialized
     * @throws TemplateException if an exception occurs during template
     *             processing
     * @throws IOException if an I/O exception occurs during writing to the
     *             writer.
     */
    public boolean process(TemplateModel root, Writer out) throws TemplateException, IOException {
        if (isInitted()) {
            template.process(root, out);
            return true;
        }
        return false;
    }

}
