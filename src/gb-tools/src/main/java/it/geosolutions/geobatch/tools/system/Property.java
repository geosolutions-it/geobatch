package it.geosolutions.geobatch.tools.system;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Property {
    private final static Logger LOGGER = Logger.getLogger(Property.class.toString());
    
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
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.warning("Property.getIntProperty: NumberFormatException for argument "+arg+"="+value);
            }
        }
        else {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Property.getIntProperty: Property "+arg+" not set, unable to parse it!");
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
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Property.getIntProperty: "+property+": "+npe.getLocalizedMessage());
        }
        
        if (p!=null){
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Property.getIntProperty: "+property+": "+p);
            return p;
        }
        else
            return null;
    }
}
