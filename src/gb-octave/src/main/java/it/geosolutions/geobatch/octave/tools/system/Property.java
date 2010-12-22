package it.geosolutions.geobatch.octave.tools.system;

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
    public static Integer getIntProperty(String arg) throws NullPointerException {
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
}
