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
package it.geosolutions.geobatch.actions.freemarker;

import it.geosolutions.geobatch.tools.filter.FreeMarkerFilter;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 *         The default FreeMarker incoming event class it represent a FreeMarker datamodel
 */
public class TemplateModelEvent extends EventObject {
    private final static Logger LOGGER = LoggerFactory.getLogger(TemplateModelEvent.class);
    /**
     * Used as key into the map for the incoming event: ${event.NAME_VAR} where name var is a KEY
     * value of the incoming map event NOTE: for the default FileSystemEvent key:
     * 
     * @see FreeMarkerConfiguration
     */
    static final String EVENT_KEY = "event";

    private static final long serialVersionUID = -8211229935415131446L;

    // name of this Model structure
    private String name;

    /**
     * Constructor
     * 
     * @param source
     *            the object to use as data Structure (should be an implementation of TemplateModel)
     * @param n
     *            the name of this event
     */
    public TemplateModelEvent(Object source, String n) {
        super(source);
        name = n;
    }

    /**
     * Constructor, the name of this event will be set to the default one.
     * 
     * @see EVENT_KEY
     * @param source
     *            the object to use as data Structure (should be an implementation of TemplateModel)
     * 
     */
    public TemplateModelEvent(Object source) {
        super(source);
        name = EVENT_KEY;
    }

    /**
     * Using the specified filter try to get a valid TemplateModel useful to start a freemarker
     * process operation
     * 
     * @param f
     *            the FreeMarkerFilter to use
     * @return the wrapped template model
     * @throws NullPointerException
     *             if f or the source are null
     * @throws TemplateModelException
     *             if it is not possible to wrap the passed object
     */
    public TemplateModel getModel(FreeMarkerFilter f) throws NullPointerException,
            TemplateModelException {
        if (f != null)
            return f.wrapRoot(this.getSource());
        else {
            final String message="TemplateModelEvent.getModel(): Unable to get the model using a null FreeMarkerFilter";
            if (LOGGER.isErrorEnabled()){
                LOGGER.error(message);
            }
            throw new NullPointerException(message);
        }
    }

    /**
     * @return the name of this event
     */
    public String getName() {
        return name;
    }

}
