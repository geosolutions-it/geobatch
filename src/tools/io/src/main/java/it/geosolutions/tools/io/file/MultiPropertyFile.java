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
package it.geosolutions.tools.io.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a property file, where a single property may be defined more than once.
 * <br/>The internal value representation for a given propertyname will be <UL>
 * <li>a <TT>String</TT> if the property was defined once only</li>
 * <li>a <TT>List of Strings</TT> if the property was defined more than once.</li>
 * </ul>
 * Empty lines will not be taken into account.<br/>
 * Lines starting with a '<TT>#</TT>' will be considered as comments and will not be taken into account.<br/>
 * <br/>
 * If the file read procedure fails, the valid properties will still be accessibile.
 * <br/><br/>
 * Before accessing any accessor, the {@link #read()} method must be called.
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class MultiPropertyFile {
    private final static Logger LOGGER = LoggerFactory.getLogger(MultiPropertyFile.class);

    /** 
     * The input data. 
     * Only one between in and file is not null 
     */
    private InputStream in = null;

    /** 
     * The input data. 
     * Only one between in and file is not null 
     * <br/>File opening is deferred until {@link #read() read()} is called.
     */
    private File file = null;
        
    /**
     * The values may be String or List of String.
     */
    private Map<String, Object> properties = null;
    
    public MultiPropertyFile(File file) {
        if(file == null)
            throw new IllegalArgumentException("Can't be configured with a null file");

        // we'll open the stream only when needed
        this.file = file;

//        this(FileUtils.openInputStream(file));
    }

    public MultiPropertyFile(InputStream in) {
        if(in == null)
            throw new IllegalArgumentException("Can't be configured with a null inputstream");

        this.in = in;
    }
   
    /**
     * Process the file.
     * The return value tells if the processing was successful.
     * Even in a case of a failed parsing, the valid properties will be accessibile. 
     * <br/><br/>
     * At the end of the read procedure the InputStream will be closed.
     * 
     * @return true if the parsing was successful.
     */
    public boolean read() {
        properties = new HashMap<String, Object>();
        boolean ret = true;
        
        LineIterator it = null;
        try {
            in = getIS();
            it = IOUtils.lineIterator(in, "UTF-8");
            while (it.hasNext()) {
                String line = it.nextLine();
                if(line.trim().length()==0) // empty line
                    continue;
                if(line.startsWith("#"))  // comment line
                    continue;
                
                int idx = line.indexOf("=");
                if(idx == -1) {
                    LOGGER.warn("Missing '=' in line: ["+line+"]" + (file==null?"": " in file " + file));
                    ret = false;
                    continue;
                }
                
                String key = line.substring(0, idx);
                String value = line.substring(idx+1);
                
                putValue(key, value);
            }

            return ret;
            
        } catch (IOException ex) {
            LOGGER.error("Error processing input"
                    +(file==null?"":(" file " + file))
                    +": " + ex.getMessage(), ex);
            return false;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private InputStream getIS() throws IOException {
        if(in != null)
            return in;
        else {
            return(FileUtils.openInputStream(file));
        }
    }
    
    /**
     * @return true if the given propertyName is defined
     * @throws IllegalStateException if {@link #read() read()} has not been yet invoked.
     */
    public boolean exist(String propertyName) throws IllegalStateException {
        checkState();
        return properties.containsKey(propertyName);
    }
    
    /**
     * @return true if the given propertyName is bound to multiple values.
     * @throws IllegalStateException if {@link #read() read()} has not been yet invoked.
     */
    public boolean isMultiValue(String propertyName) throws IllegalStateException {
        checkState();
        Object entry = properties.get(propertyName);
        return entry != null && entry instanceof List;
    }
    
    /**
     * @return true if the given propertyName is bound to a single value.
     * @throws IllegalStateException if {@link #read() read()} has not been yet invoked.
     */
    public boolean isSingleValue(String propertyName) throws IllegalStateException {
        checkState();
        Object entry = properties.get(propertyName);
        return entry != null && entry instanceof String;
    }
    
    /**
     * @return the value for the given propertyName. It may be a String or a List of Strings.
     * @throws IllegalStateException if {@link #read() read()} has not been yet invoked.
     */
    public Object getValue(String propertyName) throws IllegalStateException {
        checkState();
        return properties.get(propertyName);
    }

    /**
     * @return the value for the given propertyName, already cast to a String single value.
     * @throws IllegalStateException if {@link #read() read()} has not been yet invoked.
     * @throws ClassCastException if the propertyis bound to multiple values.
     */
    public String getString(String propertyName) throws IllegalStateException, ClassCastException {
        checkState();
        return (String)properties.get(propertyName);
    }

    /**
     * @return the value for the given propertyName, already cast to a List of Strings.
     * @throws IllegalStateException if {@link #read() read()} has not been yet invoked.
     * @throws ClassCastException if the property is bound to a single value.
     */
    public List<String> getList(String propertyName) throws IllegalStateException, ClassCastException {
        checkState();
        return (List<String>)properties.get(propertyName);
    }
    
    /**
     * @return the Set of the defined properties. The Set is unmodifiable.
     * @throws IllegalStateException if {@link #read() read()} has not been yet invoked.
     */
    public Set<String> getPropertyNames() throws IllegalStateException {
        checkState();
        return Collections.unmodifiableSet(properties.keySet());
    }
    
    /**
     * @return the raw property Map. The Map is unmodifiable.
     * @throws IllegalStateException if {@link #read() read()} has not been yet invoked.
     */
    public Map<String, Object> getRawMap() throws IllegalStateException {
        checkState();
        return Collections.unmodifiableMap(properties);
    }

    //==========================================================================
    
    private void putValue(String key, String value) {
        Object entry = properties.get(key);
        if(entry == null) {
            // no key yet
            properties.put(key, value);
        } else if (entry instanceof List) {
            // The key is already a list; add the new value
            ((List)entry).add(value);
        } else if (entry instanceof String) {
            // The key is bound to a Single String; transform it into a List
            List list = new ArrayList();
            list.add(entry);
            list.add(value);
            // replace the entry
            properties.put(key, list);
        } else {
            // should not happen
            throw new IllegalStateException("Unknown entry type " + entry.getClass().getName());
        }
        
    }
    
    /**
     * Check if the read() operation has been invoked.
     * @throws IllegalStateException 
     */
    private void checkState() throws IllegalStateException {
        if(properties == null)
            throw new IllegalStateException(this.getClass().getSimpleName() + " requires to read() the file before accessing its elements."); 
    }

}
