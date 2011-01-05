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

import java.util.EventObject;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TemplateModelEvent extends EventObject {
    private static final String EVENT_NAME="event";

    private static final long serialVersionUID = -8211229935415131446L;
    // name of this Model structure
    private String name;

    /**
     * Constructor
     * @param source the object to use as data Structure (should be an
     * implementation of TemplateModel)
     * @param n the name of this event
     */
    public TemplateModelEvent(Object source, String n) {
        super(source);
        name=n;
    }
    
    /**
     * Constructor, the name of this event will be set to the
     * default one.
     * @see EVENT_NAME
     * @param source the object to use as data Structure (should be an
     * implementation of TemplateModel)
     * 
     */
    public TemplateModelEvent(Object source) {
        super(source);
        name=EVENT_NAME;
    }
    
    /**
     * Using the specified filter try to get a valid TemplateModel
     * useful to start a freemarker process operation
     * @param f the FreeMarkerFilter to use
     * @return the wrapped template model 
     * @throws NullPointerException if f or the source are null 
     * @throws TemplateModelException if it is not possible to wrap the passed object
     */
    public TemplateModel getModel(FreeMarkerFilter f)
        throws NullPointerException, TemplateModelException
        {
        if (f!=null)
            return f.wrapRoot(this.getSource());
        else
            throw new NullPointerException("Unable to get the model using a null FreeMarkerFilter");
    }
    
    /**
     * @return the name of this event
     */
    public String getName(){
        return name;
    }

}
