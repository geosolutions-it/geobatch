/*
 *  Copyright (C) 2007 - 2012 GeoSolutions S.A.S.
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
package it.geosolutions.tools.dyntokens.model;

import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
abstract public class DynToken implements Cloneable {

    private final static Logger LOGGER = LoggerFactory.getLogger(DynToken.class);

    protected String name;
    protected String base;
    protected String regex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public boolean isValid() {
        if(name == null || name.isEmpty() )
            return false;

        if(base == null || base.isEmpty() )
            return false;

        if(regex == null || regex.isEmpty() )
            return false;

        try {
            Pattern.compile(regex);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public String resolve(Map<String, String> resolvedTokens) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Resolving dyn token '"+ name + "' with base '"+base+"'" );
        }

        String resolvedBase = expandTokens(base, resolvedTokens);
        return resolve(resolvedBase, resolvedTokens);
    }

    abstract String resolve(String resolvedBase, Map<String, String> resolvedTokens);


    protected static String expandTokens(final String input, Map<String, String> resolvedTokens) {
        String ret = input;

        for (Map.Entry<String, String> entry : resolvedTokens.entrySet()) {
            String tokenName = entry.getKey();
            String tokenValue = entry.getValue();

            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Expanding token '"+tokenName+"' into '"+tokenValue+"' in input string '"+ret+"'");
            }
            ret = ret.replaceAll("\\$\\{"+tokenName+"\\}", tokenValue);
        }

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Input expanded in '"+ret+"'");
        }

        return ret;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"["
                + "name=" + name
                + ", base=" + base
                + ", regex=" + regex
                + ']';
    }



}
