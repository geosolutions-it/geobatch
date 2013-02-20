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
package it.geosolutions.tools.commons.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Property {
    private final static Logger LOGGER = LoggerFactory.getLogger(Property.class);
    
    /**
     * Parse the systemProperty searching for the passed arg string
     * if it is found try parse its value returning an integer value.
     * If it is not found return NULL.
     * @throws NullPointerException if passed arg is null 
     * @param arg the system property to parse
     * @return an integer corresponding to the value of the property
     */
    public static Integer getIntProperty(final String arg) throws NullPointerException {
        String value;
        if (arg!=null)
            value=System.getProperty(arg);
        else
            throw new NullPointerException("Property.getIntProperty: Unable to parse a null string property!");
        Integer ret=null;
        if (value!=null){
            try {
                 ret=Integer.parseInt(value);
            }
            catch (NumberFormatException nfe){
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Property.getIntProperty: NumberFormatException for argument "+arg+"="+value);
            }
        }
        else {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Property.getIntProperty: Property "+arg+" not set, unable to parse it!");
        }
        
        return ret;
    }
    
    /**
     * Return the Integer value of the passed property logging
     * accordingly. Use this function if you do not want to handle
     * Exceptions.
     * @param property the string key representing the wanted value.
     * @return an Integer representing the value or null if it is
     * unavailable.
     * @see Property.getIntProperty()
     */
    public static Integer setIntProperty(final String property){
        Integer p=null;
        try {
            p=Property.getIntProperty("Property.getIntProperty: "+property);
        }
        catch (NullPointerException npe){
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Property.getIntProperty: "+property+": "+npe.getLocalizedMessage());
        }
        
        if (p!=null){
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Property.getIntProperty: "+property+": "+p);
            return p;
        }
        else
            return null;
    }
}
